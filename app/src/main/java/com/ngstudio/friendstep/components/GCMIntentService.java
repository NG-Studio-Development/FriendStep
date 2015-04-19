package com.ngstudio.friendstep.components;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.ui.activities.MainActivity;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;

public class GCMIntentService extends IntentService {

    public static final String TYPE_SEND_MESSAGE = "sendmessage";

    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super("GCMIntentService");
    }

    public static final int NOTIFICATION_ID = 1;
    private android.app.NotificationManager mNotificationManager;

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        String mess = extras.getString("price");

        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_MESSAGE_INCOMING, intent);
            sendNotification("Received: "+mess);
        }
    }

    private void sendNotification(String msg) {
        mNotificationManager = (android.app.NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        //.setSmallIcon(R.drawable.ic_stat_gcm)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        //callSound();

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}