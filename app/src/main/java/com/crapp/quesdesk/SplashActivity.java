package com.crapp.quesdesk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 2000;

    //Method called when activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            //Showing splash screen with a timer. This will be useful when you want to show case your app logo / company
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("USER", MODE_PRIVATE);
                Boolean alreadyLoggedIn = sharedPreferences.getBoolean("LoginSuccess",false);
                if (alreadyLoggedIn){
                    Intent i = new Intent(getApplicationContext(),DashboardActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(i);
                    finish();
                }

            }
        },SPLASH_TIME_OUT);


    }


}
