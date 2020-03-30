package com.ak47.doNotDisturb.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity with No Layout
        SharedPreferences sharedPreferences = getSharedPreferences("initial_setup", MODE_PRIVATE);
        boolean initialSetupBoolean = sharedPreferences.getBoolean("initial_setup", false);

        if (initialSetupBoolean) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, InitialSetupActivity.class);
        }
        startActivity(intent);
        finish();

    }
}
