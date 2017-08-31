package com.facebook.blebus;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by priteshsankhe on 05/12/16.
 */

public class ScheduledService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = ScheduledService.class.getSimpleName();
    public static final String SCAN_IN_PROGRESS = "SCAN_IN_PROGRESS";
    public static final String SCAN_COMPLETED = "SCAN_COMPLETED";

    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 3000;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private Map<String, Integer> deviceHashMap = new HashMap<>();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public ScheduledService() {
        super("ScheduledService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getSimpleName(), "I ran!");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
            mGoogleApiClient.connect();
        }
        Intent serviceIntent = new Intent(this, ScanDevicesService.class);
        startService(serviceIntent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
