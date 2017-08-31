package com.facebook.blebus;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by priteshsankhe on 02/12/16.
 */

public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static void writeNewRecord(String hexID, int cabNo, double latitude, double longitude, long timestamp) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("records");
        BusDevice post = new BusDevice(hexID, latitude, longitude, timestamp);
        Map<String, Object> postValues = post.toMap();


        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + cabNo + "/" + timestamp, postValues);

        Map<String, Object> childLatestUpdates = new HashMap<>();
        DatabaseReference myLatestRef = database.getReference("latest-records");
        childLatestUpdates.put("/" + cabNo + "/", postValues);

        myRef.updateChildren(childUpdates);
        myLatestRef.updateChildren(childLatestUpdates);

    }

    public static CabModel getBusDetails(String hexId) {
        CabModel model;
        switch (hexId) {
            case "2631":
                model = new CabModel(3247, "SHESHNARAYAN PANDEY");
                break;
            case "5220":
                model = new CabModel(3103, "PRABHAKAR TIWARI");
                break;
            case "5757":
                model = new CabModel(3256, "PRAKASH DESAI");
                break;
            case "6525":
                model = new CabModel(3265, "PRAMOD KOLTE");
                break;
            case "9853":
                model = new CabModel(2453, "DATTASHINDE");
                break;
            case "077D":
                model = new CabModel(2454, "KALAPPA SAKHRE ");
                break;
            case "0CF0":
                model = new CabModel(2455, "MANOJ TIWARI");
                break;
            case "0E99":
                model = new CabModel(2473, "MAHENDRA SAROJ");
                break;
            case "2FDD":
                model = new CabModel(2780, "MANOHAR KOLI");
                break;
            case "39E0":
                model = new CabModel(3028, "UMESH CHAUHAN");
                break;
            case "406B":
                model = new CabModel(2781, "KISHOR");
                break;
            case "41F4":
                model = new CabModel(2782, "ASHOK");
                break;
            case "4A3A":
                model = new CabModel(2456, "SAMBHAJI VEREKAR");
                break;
            case "592B":
                model = new CabModel(2457, "SHAILESH BOBASKAR");
                break;
            case "5D7A":
                model = new CabModel(2459, "RAJESH DABADE");
                break;
            case "7B76":
                model = new CabModel(9354, "VIJAY GAIKWAD");
                break;
            case "7F0C":
                model = new CabModel(9766, "SHIV KUMAR");
                break;
            case "FE15":
                model = new CabModel(9169, "RAKESH");
                break;
            case "91CE":
                model = new CabModel(0707, "SACHIN");
                break;
            case "93DD":
                model = new CabModel(2649, "SUBHASH");
                break;
            case "951B":
                model = new CabModel(6042, "KOMAL");
                break;
            case "9BED":
                model = new CabModel(2473, "MAHENDRA SAROJ");
                break;
            case "E4ED":
                model = new CabModel(870, "SURYAJEET");
                break;
            case "D552":
                model = new CabModel(6782, "MUKUND");
                break;
            default:
                model = new CabModel(Integer.parseInt(hexId), "Not found");
                break;
        }
        return model;
    }


    public static String getHexFromCabNo(int cabNo) {
        switch (cabNo) {
            case 2780:
                return "2FDD";
        }

        return "0";
    }

    public static void sendNotification(Context ctx, String cabNo) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference notifications = ref.child("notificationRequests");
        Log.d(TAG, FirebaseInstanceId.getInstance().getToken());
        Map notification = new HashMap<>();
        notification.put("cabNo", cabNo);
        notification.put("deviceId", FirebaseInstanceId.getInstance().getToken());
        notifications.push().setValue(notification);
    }
}
