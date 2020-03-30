package com.ak47.doNotDisturb.Activities;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ak47.doNotDisturb.R;
import com.ak47.doNotDisturb.Service.HelperForegroundService;
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

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private android.app.AlertDialog enableNotificationListenerAlertDialog;

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

    private void openDialog() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void basicChecking() {

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!checkPermission()) {
            //If Not Granted
            requestPermission();
        }

        assert notificationManager != null;
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setTitle("Alert")
                    .setMessage("Please Allow Notification Policy to Access Your Call States")
                    .setCancelable(false)
                    .setPositiveButton("Open Notification Policy", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(
                                    Settings
                                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);

                        }
                    })
                    .show();
        }

        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
                    requestPermissions(new String[]{READ_PHONE_STATE, READ_CONTACTS},
                            PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "OnResume");
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
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private android.app.AlertDialog buildNotificationServiceAlertDialog() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.notification_listener_service)
                .setMessage(R.string.notification_listener_service_explanation)
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                        })
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // If you choose to not enable the notification listener
                                // the app. will not work as expected
                            }
                        });
        return (alertDialogBuilder.create());
    }
}
