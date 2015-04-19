package com.ngstudio.friendstep.components;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public final class GoogleCloudMessagingClient {

    private static final String TAG = GoogleCloudMessagingClient.class.getSimpleName();


    public static class PushNotificationsReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "push " + intent);
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GCMIntentService.class.getName());

            // Start the service, keeping the device awake while it is launching.

            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }

}
