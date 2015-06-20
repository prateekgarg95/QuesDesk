package com.crapp.quesdesk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class LoginActivity extends Activity implements View.OnClickListener {

    Boolean successfulLogin = false;
    String firstName, lastName, email;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Setting up views
        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        //User Details
        firstName = "firstname";
        lastName = "lastname";
        email = "email";


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                successfulLogin = true;
                if (successfulLogin) {
                    SharedPreferences userPreferences = getSharedPreferences("USER",MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPreferences.edit();
                    editor.putString("FirstName", firstName);
                    editor.putString("LastName", lastName);
                    editor.putString("Email", email);
                    editor.putBoolean("LoginSuccess", successfulLogin);
                    editor.commit();
                    finish();
                }
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(intent);

        }
    }
}
