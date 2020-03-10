package com.ak47.donotdisturb.Service;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class StartHelperForegroundTileService extends TileService {
    String TAG = "Logging - StartHelperForegroundTileService";
    Tile doNotDisturbTile;
    Intent helperForegroundServiceIntent;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        doNotDisturbTile = getQsTile();
        Log.e(TAG, "onTileAdded: " + "called");
        if (isHelperServiceRunning()) {
            doNotDisturbTile.setState(Tile.STATE_ACTIVE);
        } else {
            doNotDisturbTile.setState(Tile.STATE_INACTIVE);
        }
        doNotDisturbTile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        Log.e(TAG, "onClick: " + "called");
        doNotDisturbTile = getQsTile();
        SharedPreferences sharedPreferences = getSharedPreferences("initial_setup", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Log.e(TAG, "onClick: " + getQsTile().getState());

        helperForegroundServiceIntent = new Intent(getApplicationContext(), HelperForegroundService.class);

        if (getQsTile().getState() == Tile.STATE_ACTIVE) {
            editor.putBoolean("foregroundServiceStateUserPreference", false);
            getApplicationContext().stopService(helperForegroundServiceIntent);
            doNotDisturbTile.setState(Tile.STATE_INACTIVE);
            doNotDisturbTile.updateTile();
        } else if (getQsTile().getState() == Tile.STATE_INACTIVE) {
            if (sharedPreferences.getBoolean("initial_setup", false)) {
                editor.putBoolean("foregroundServiceStateUserPreference", true);
                ContextCompat.startForegroundService(getApplicationContext(), helperForegroundServiceIntent);
                doNotDisturbTile.setState(Tile.STATE_ACTIVE);
                doNotDisturbTile.updateTile();

            } else {
                Log.e(TAG, "Complete the initial setup first!");
            }
        }
        editor.apply();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.e(TAG, "onTileRemoved: " + "called");
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.e(TAG, "onStartListening: " + "called");
        doNotDisturbTile = getQsTile();
        if (isHelperServiceRunning()) {
            doNotDisturbTile.setState(Tile.STATE_ACTIVE);
        } else {
            doNotDisturbTile.setState(Tile.STATE_INACTIVE);
        }
        doNotDisturbTile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.e(TAG, "onStopListening: " + "called");
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
}
