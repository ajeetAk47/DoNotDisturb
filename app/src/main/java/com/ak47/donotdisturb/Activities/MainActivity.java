package com.ak47.donotdisturb.Activities;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ak47.donotdisturb.R;
import com.ak47.donotdisturb.Service.HelperForegroundService;
import com.google.android.material.button.MaterialButton;
import com.polyak.iconswitch.IconSwitch;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private IconSwitch serviceIconSwitch;
    private TextView statusInfoActiveTextView, statusInfoInactiveTextView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Intent helperForegroundServiceIntent;

    String TAG = "Logging - MainActivity ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        basicChecking();
        helperForegroundServiceIntent = new Intent(getBaseContext(), HelperForegroundService.class);
        sharedPreferences = getSharedPreferences("initial_setup", Context.MODE_PRIVATE);

        MaterialButton settingButton = findViewById(R.id.button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openDialog();
            }
        });

        serviceIconSwitch = findViewById(R.id.serviceIconSwitch);
        statusInfoActiveTextView = findViewById(R.id.statusInfoActiveTextView);
        statusInfoInactiveTextView = findViewById(R.id.statusInfoInactiveTextView);

        if (isHelperServiceRunning()) {
            editor = sharedPreferences.edit();
            editor.putBoolean("foregroundServiceStateUserPreference", true);
            editor.apply();
            statusInfoActiveTextView.setVisibility(View.VISIBLE);
            statusInfoInactiveTextView.setVisibility(View.INVISIBLE);
            serviceIconSwitch.setChecked(IconSwitch.Checked.RIGHT);
        } else {
            editor = sharedPreferences.edit();
            editor.putBoolean("foregroundServiceStateUserPreference", false);
            editor.apply();
            statusInfoActiveTextView.setVisibility(View.INVISIBLE);
            statusInfoInactiveTextView.setVisibility(View.VISIBLE);
            serviceIconSwitch.setChecked(IconSwitch.Checked.LEFT);
        }

        serviceIconSwitch.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                Log.e(TAG, "onCheckChanged: " + current);
                editor = sharedPreferences.edit();
                if (current == IconSwitch.Checked.RIGHT) {
                    editor.putBoolean("foregroundServiceStateUserPreference", true);
                    statusInfoActiveTextView.setVisibility(View.VISIBLE);
                    statusInfoInactiveTextView.setVisibility(View.INVISIBLE);
                    ContextCompat.startForegroundService(getBaseContext(), helperForegroundServiceIntent);
                } else if (current == IconSwitch.Checked.LEFT) {
                    editor.putBoolean("foregroundServiceStateUserPreference", false);
                    statusInfoActiveTextView.setVisibility(View.INVISIBLE);
                    statusInfoInactiveTextView.setVisibility(View.VISIBLE);
                    stopService(helperForegroundServiceIntent);
                }
                editor.apply();
            }
        });


    }

    private boolean isHelperServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (HelperForegroundService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void openDialog()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void basicChecking() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            if (!checkPermission()) {
                //If Not Granted
                requestPermission();
            }

            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        .setTitle("Alert")
                        .setMessage("Please Allow Notification Policy to Access Your Call States")
                        .setCancelable(false)
                        .setPositiveButton("Open Notification Policy", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        android.provider.Settings
                                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                startActivity(intent);

                            }
                        })
                        .show();
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_CONTACTS, READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);

    }

    public void onRequestPermissionsResult(int requestcode, String[] premissions, int[] grantResults) {

        switch (requestcode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {


                    if (shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
                        requestPermissions(new String[]{READ_PHONE_STATE, READ_CONTACTS},
                                PERMISSION_REQUEST_CODE);
                        return;
                    }
                }


                break;
        }
    }
}
