package com.facebook.blebus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.HashMap;
import java.util.Map;


/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MapsActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ClusterManager.OnClusterClickListener<BusDevice>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener,
        DialogButtonClickListener {

    public static final String TAG = MapsActivity.class.getSimpleName();
    static final float COORDINATE_OFFSET = 0.00002f;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    HashMap<String, BusDevice> mBusDeviceHashMap = new HashMap<>();
    HashMap<String, String> markerLocation;
    private ClusterManager<BusDevice> mClusterManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        markerLocation = new HashMap<>();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myLatestRef = database.getReference("latest-records");
        myLatestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String hexId = postSnapshot.getKey();
                    BusDevice busDevice = postSnapshot.getValue(BusDevice.class);
                    busDevice.hexID = hexId;
                    busDevice.mPosition = new LatLng(busDevice.latitude, busDevice.longitude);
                    mBusDeviceHashMap.put(hexId, busDevice);
                }
                modifyMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (TextUtils.isEmpty(mPrefs.getString(BundleParams.CAB_NO, null))) {
            DialogFragment newFragment = RegisterDialogFragment.newInstance(this, null);
            newFragment.show(getSupportFragmentManager(), "register dialog");
        }

        // sendNotificationToUser("puf", "Hi there puf!");
    }

    private void modifyMap() {

        int i = 0;
        for (Map.Entry<String, BusDevice> entry : mBusDeviceHashMap.entrySet()) {
            String hex = entry.getKey();
            BusDevice device = entry.getValue();
            double[] newLatLong = isLatLongClose(i, hex, device.latitude, device.longitude);
            i++;
            device.latitude = newLatLong[0];
            device.longitude = newLatLong[1];
            mBusDeviceHashMap.put(hex, device);
            Log.d(TAG, device.hexID + "\t" + device.latitude + "\t" + device.longitude);
            CabModel model = Utils.getBusDetails(hex);
            if (mMap != null) {
                LatLng deviceLocation = new LatLng(device.latitude, device.longitude);
                String lastSeen = Utils.getTimeAgo(device.timestamp, this);
                Marker marker = mMap.addMarker(new MarkerOptions().position(deviceLocation)
                        .title(String.valueOf(model.cabNo)).snippet(lastSeen));
                marker.setTag(hex);
            }
        }
        // onMapReady(mMap);
    }


    private double[] isLatLongClose(int i, String hexId, double latitude, double longitude) {
        for (Map.Entry<String, BusDevice> entry : mBusDeviceHashMap.entrySet()) {
            BusDevice device = entry.getValue();
            if (!device.hexID.equalsIgnoreCase(hexId) &&
                    (latitude > device.latitude - 0.00002) && (latitude < device.latitude + 0.00002) &&
                    (longitude > device.longitude - 0.00002) && (longitude < device.longitude + 0.00002) && i != 0) {
                latitude = latitude + 0.00002;
                longitude = longitude + 0.00002;
            }
        }
        return new double[]{latitude, longitude};
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mClusterManager = new ClusterManager<BusDevice>(this, mMap);
        mMap.clear();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        if (mLastLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 10));
        }
        enableMyLocation();
        for (Map.Entry<String, BusDevice> entry : mBusDeviceHashMap.entrySet()) {
            String key = entry.getKey();
            BusDevice value = entry.getValue();
            LatLng deviceLocation = new LatLng(value.latitude, value.longitude);
            CabModel model = Utils.getBusDetails(key);
            String lastSeen = Utils.getTimeAgo(value.timestamp, this);
            mMap.addMarker(new MarkerOptions().position(deviceLocation)
                    .title(String.valueOf(model.cabNo)).snippet(lastSeen));
            //mClusterManager.addItem(value);
        }

        // mMap.setOnCameraIdleListener(mClusterManager);
        // mMap.setOnMarkerClickListener(mClusterManager);
        // mClusterManager.setOnClusterClickListener(this);
        // mClusterManager.cluster();

    }

    @Override
    public boolean onClusterClick(Cluster<BusDevice> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().hexID;
        // Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
        if (mLastLocation != null && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        String tag = (String) marker.getTag();
        BusDevice device = mBusDeviceHashMap.get(tag);
        marker.setTitle(String.valueOf(Utils.getBusDetails(tag).cabNo));
        marker.setSnippet(Utils.getTimeAgo(device.timestamp, this));
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String tag = (String) marker.getTag();
        BusDevice device = mBusDeviceHashMap.get(tag);
        String uriStr = "geo:0,0?q=" + device.latitude + "," + device.longitude + "(Cab location)";
        Uri gmmIntentUri = Uri.parse(uriStr);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Nullable
    @Override
    public void doPositiveClick(Bundle args) {
        String cabNo = args.getString(BundleParams.CAB_NO);
        mPrefs.edit().putString(BundleParams.CAB_NO, cabNo).commit();
        mPrefs.edit().putInt(cabNo, 0).commit();
        FirebaseMessaging.getInstance().subscribeToTopic(cabNo);
    }

    @Nullable
    @Override
    public void doNegativeClick(Bundle args) {
    }
}