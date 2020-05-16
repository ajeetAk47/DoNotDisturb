package com.ak47.doNotDisturb.Activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ak47.doNotDisturb.R;
import com.ak47.doNotDisturb.Service.HelperForegroundService;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.material.button.MaterialButton;
import com.polyak.iconswitch.IconSwitch;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    String TAG = "Logging - MainActivity ";
    private IconSwitch serviceIconSwitch;
    private TextView statusInfoActiveTextView, statusInfoInactiveTextView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Intent helperForegroundServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeMobileAdsLoadAndShow();

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
        ImageView warningImageButton = findViewById(R.id.warning_msg);

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

                    if (isNotificationServiceEnabled()) {
                        editor.putBoolean("foregroundServiceStateUserPreference", true);
                        statusInfoActiveTextView.setVisibility(View.VISIBLE);
                        statusInfoInactiveTextView.setVisibility(View.INVISIBLE);
                        ContextCompat.startForegroundService(getBaseContext(), helperForegroundServiceIntent);
                    } else {
                        android.app.AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
                        enableNotificationListenerAlertDialog.show();
                    }
                } else if (current == IconSwitch.Checked.LEFT) {
                    editor.putBoolean("foregroundServiceStateUserPreference", false);
                    statusInfoActiveTextView.setVisibility(View.INVISIBLE);
                    statusInfoInactiveTextView.setVisibility(View.VISIBLE);
                    stopService(helperForegroundServiceIntent);
                }
                editor.apply();
            }
        });


        warningImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle)
                        .setTitle("Warning")
                        .setMessage("After making any change in settings of this app the above Switch will automatically Turn Off. If Not, then your requested to turn Off Above Switch and again turn it On.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", null)
                        .show();

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

        if (!checkPermission()) {
            //If Not Granted
            requestPermission();
        }

        if (!isNotificationServiceEnabled()) {
            android.app.AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }
    }

    private void initializeMobileAdsLoadAndShow() {
        try {
            Log.d(TAG, "initializeMobileAdsLoadAndShow: the ad was loading.");
            AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-9120224455161858/7937487858")
                    .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                        @Override
                        public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                            NativeTemplateStyle styles = new
                                    NativeTemplateStyle.Builder().build();

                            TemplateView template = findViewById(R.id.my_template);
                            template.setStyles(styles);
                            template.setNativeAd(unifiedNativeAd);

                        }
                    })
                    .build();

            adLoader.loadAds(new AdRequest.Builder().build(), 3);
            Log.d(TAG, "initializeMobileAdsLoadAndShow: the ad was loaded");

        } catch (Exception e) {
            Log.e(TAG, "initializeMobileAdsLoadAndShow: Error");
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {

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
                .setPositiveButton("Enable Notification Access",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                        });
        return (alertDialogBuilder.create());
    }
}
