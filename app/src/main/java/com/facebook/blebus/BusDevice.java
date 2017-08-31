package com.facebook.blebus;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.maps.android.clustering.ClusterItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by priteshsankhe on 02/12/16.
 */


@IgnoreExtraProperties
public class BusDevice implements ClusterItem {

    public String hexID;
    public double latitude;
    public double longitude;
    public long timestamp;
    public LatLng mPosition;

    public Map<String, Boolean> records = new HashMap<>();

    public BusDevice() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public BusDevice(String hexID, double latitude, double longitude, long timestamp) {
        this.hexID = hexID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.mPosition = new LatLng(latitude, longitude);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("timestamp", timestamp);
        return result;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
