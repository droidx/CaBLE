package com.facebook.blebus;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by priteshsankhe on 09/12/16.
 */

public class NotificationHandlerService extends FirebaseMessagingService {

    public static final String TAG = NotificationHandlerService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Log.d(TAG, "onMessageReceived: " + remoteMessage.getData().toString());
        // HashMap<String, String> messageMap = (HashMap<String, String>) remoteMessage.getData();

        int mNotificationId = 001;
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icon_cab)
                        .setContentTitle(title)
                        .setContentText(String.valueOf(body));
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}
