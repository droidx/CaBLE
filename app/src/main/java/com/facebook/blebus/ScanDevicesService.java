// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.blebus;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScanDevicesService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = ScanDevicesService.class.getSimpleName();
    public static final String SCAN_IN_PROGRESS = "SCAN_IN_PROGRESS";
    public static final String SCAN_COMPLETED = "SCAN_COMPLETED";
    public static final String SCAN_NOT_COMPLETED = "SCAN_NOT_COMPLETED";

    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 5000;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private Map<String, Integer> deviceHashMap = new HashMap<>();
    private ScanDevicesBinder mBinder = new ScanDevicesBinder();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SharedPreferences mSharedPreferences;

    public ScanDevicesService() {
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

    public class ScanDevicesBinder extends Binder {
        public ScanDevicesService getService() {
            return ScanDevicesService.this;
        }
    }

    public void startScan() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        deviceHashMap = new HashMap<>();
        if (mBluetoothAdapter == null || mBluetoothAdapter.isEnabled()) {
            scanLeDevice(!mScanning);
        }
    }

    //todo improve this code - use other data structure if needed
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            mScanning = false;
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            // storeDeviceList();
                            // broadcastUpdate(SCAN_COMPLETED);
                            sendInfoToServer();
                        }
                    },
                    SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void sendInfoToServer() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ScanDevicesService.this);
        String userCabNoStr = mSharedPreferences.getString(BundleParams.CAB_NO, null);
        int userCabNo = 0;
        if (!TextUtils.isEmpty(userCabNoStr)) {
            userCabNo = Integer.parseInt(userCabNoStr);
        }

        boolean userCabFound = false;
        if (mLastLocation != null) {
            for (Map.Entry<String, Integer> entry : deviceHashMap.entrySet()) {
                int value = entry.getValue();
                String hex = BleUtils.getHexId(value);
                int cabNo = Utils.getBusDetails(hex).cabNo;
                Utils.writeNewRecord(hex, cabNo, mLastLocation.getLatitude(), mLastLocation.getLongitude(), new Date().getTime());
                // Utils.sendNotification(this, hex);
                if (userCabNo == cabNo) {
                    userCabFound = true;
                    int notifSent = mSharedPreferences.getInt(userCabNoStr, 0);
                    if (notifSent == 0) {
                        mSharedPreferences.edit().putInt(userCabNoStr, 1).commit();
                        Utils.sendNotification(this, userCabNoStr);
                    }
                }
            }
        }
        if (!userCabFound) {
            mSharedPreferences.edit().putInt(userCabNoStr, 0).commit();
        }
        stopSelf();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                    if (mScanning) {
                        //broadcastUpdate(SCAN_IN_PROGRESS);
                    }
                    // Our Devices should be FB devices
                    if (!BleUtils.isValidDevice(device.getAddress(), scanRecord)) {
                        return;
                    } else {
                        if (device != null) {
                            Log.d(TAG, device.getAddress() + "");
                            String deviceMacID = device.getAddress();
                            try {
                                int deviceId = BleUtils.getCrc16Integer(deviceMacID);
                                if (deviceHashMap.put(deviceMacID, deviceId) == null) {
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int from = 600;
        int to = 2100;
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int t = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
        boolean isBetween = to > from && t >= from && t <= to || to < from && (t >= from || t <= to);

        if (isBetween) {
            startScan();
            mLastLocation = null;
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }
            mGoogleApiClient.connect();
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
