package com.crapp.quesdesk;

// Import Statements

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends Activity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "LoginActivity";

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // If the User has successfully logged in
    Boolean successfulLogin = false;

    // User Details - Name and Email
    String name, email;

    // Google Sign In Button
    SignInButton signInButton;

    //


    // Server URL where user details needs to be stored
    private String userDetailsURL;
    // JSONObject Response from the Server
    private JSONObject jsonObject;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    // If the SignIn Button is clicked
    private boolean mSignInClicked;
    // Connection Result instance
    private ConnectionResult mConnectionResult;
    // TransferDataHTTP instance
    private TransferDataHTTP transferDataHTTP;

    // Method called when the Activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Setting up views
        signInButton = (SignInButton) findViewById(R.id.signInBtn);

        // Button click listeners
        signInButton.setOnClickListener(this);

        //User Details
        name = "name";
        email = "email";

        //Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }

    // Method called when the Activity is started
    protected void onStart() {
        super.onStart();
        // Connect the Google Client
        mGoogleApiClient.connect();
    }

    // Method called when the Activity is stopped
    protected void onStop() {
        super.onStop();
        // Check if the Google Client is connected
        if (mGoogleApiClient.isConnected()) {   // If the Google Client is connected then disconnect
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to resolve any signin errors
     * Called if there is any Sign In error
     */
    private void resolveSignInError() {
        // Check if the ConnectionResult Error has any resolution
        if (mConnectionResult.hasResolution()) {
            try {
                // Set IntentInProgress as true
                mIntentInProgress = true;
                // Start Resolution for the result
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } // Catch Exception
            catch (IntentSender.SendIntentException e) {
                // Set IntentInProgress as false
                mIntentInProgress = false;
                // Retry to connect Google Client
                mGoogleApiClient.connect();
            }
        }
    }

    // Method called when the connection failed
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // If the ConnectionResult has no resolution
        if (!result.hasResolution()) {
            // Show Error dialog
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    // Method concerning Activity Result
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    // Method called when Google Client is connected
    @Override
    public void onConnected(Bundle arg0) {
        // Set if SignIn Button clicked to false
        mSignInClicked = false;


        // Get user's information
        getProfileInformation();
        Map<String, String> params = new HashMap<>();
        params.put("Name", name);
        params.put("Email", email);
        jsonObject = transferDataHTTP.sendParams(userDetailsURL, params);
        boolean success;
        try {
            success = (boolean) jsonObject.get("success");
        } catch (JSONException e) {
            e.printStackTrace();
            success = false;
        }
        if (success) {
            successfulLogin = true;
            SharedPreferences userPreferences = getSharedPreferences("USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString("Name", name);
            editor.putString("Email", email);
            editor.putBoolean("LoginSuccess", successfulLogin);
            editor.commit();
            signOutFromGplus();
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
            finish();
            Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        } else {
            successfulLogin = false;
            signOutFromGplus();
            Toast.makeText(this, "Server Error!", Toast.LENGTH_LONG).show();
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personGooglePlusProfile = currentPerson.getUrl();
                String personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);

                Log.e(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + personEmail);

                name = personName;
                email = personEmail;
            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInBtn:
                // Signin button clicked
                signInWithGplus();
                break;
        }
    }

    /**
     * Sign-in into google
     */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Sign-out from google
     */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }


}
