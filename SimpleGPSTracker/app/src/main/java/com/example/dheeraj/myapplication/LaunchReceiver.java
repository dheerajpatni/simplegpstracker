package com.example.dheeraj.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LaunchReceiver extends BroadcastReceiver {
    public static final String ACTION_PULSE_SERVER_ALARM =
            "com.proofbydesign.homeboy.ACTION_PULSE_SERVER_ALARM";

    public static final String NOTIFICATIONS_ALARM =
            "com.proofbydesign.homeboy.NOTIFICATIONS_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if( intent.getAction() == ACTION_PULSE_SERVER_ALARM) {
            Intent serviceIntent = new Intent(context, LocationService.class);
            context.startService(serviceIntent);
        }
        else if(intent.getAction() == NOTIFICATIONS_ALARM) {
            Log.e("DHEERAJ","intent.getAction() == NOTIFICATIONS_ALARM");
            Intent serviceIntent = new Intent(context, NotificationService.class);
            context.startService(serviceIntent);
        }
    }
}
