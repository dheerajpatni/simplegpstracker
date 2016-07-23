package com.example.dheeraj.myapplication;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationService extends IntentService {

    public static final String NOTIFICATION_BROADCAST_ACTION = "com.example.dheeraj.myapplication.NOTIFICATIONS";

    public NotificationService() {
        super("NotificationService");
    }

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    Intent intent;
    DBHelper databaseHelper=null;

    @Override
    protected void onHandleIntent(Intent intent) {

        databaseHelper = new DBHelper(getApplicationContext());

        StringBuffer stringbuff = new StringBuffer();
        databaseHelper.getNotifications(stringbuff);

        intent = new Intent(NOTIFICATION_BROADCAST_ACTION);
        intent.putExtra("data", "data007");
        sendBroadcast(intent);

        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent launchIntent = new Intent(LaunchReceiver.NOTIFICATIONS_ALARM);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, launchIntent , PendingIntent.FLAG_CANCEL_CURRENT );

        int interval = 30 ;    //every 30 seconds
        long timeToAlarm = SystemClock.elapsedRealtime() + interval  * 1000;
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeToAlarm, alarmIntent);
        stopSelf();
    }
}
