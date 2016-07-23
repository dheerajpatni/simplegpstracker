package com.example.dheeraj.myapplication;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class LocationService extends Service
{

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    private LocationToServerTask locationToServerTask;

    private String mPulseUrl;
    private AlarmManager alarms;
    private PendingIntent alarmIntent;
    private ConnectivityManager cnnxManager;

    private DBHelper databaseHelper;


    Intent intent;
    int counter = 0;

    @Override
    public void onCreate()
    {
        super.onCreate();

        DBHelper.LOG(" On Create" );

        //setup alarm variables.
        cnnxManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentOnAlarm = new Intent( LaunchReceiver.ACTION_PULSE_SERVER_ALARM);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 234324243 , intentOnAlarm, 0 );

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();

        //we request for location continuously.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10 , 0 , listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 , 0 , listener);

        databaseHelper = new DBHelper(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        Log.e("LOG_DHEERAJ", "onStart.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LOG_DHEERAJ", "onStartCommand.");
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.e("LOG_DHEERAJ", "Service is stopped.");
        locationManager.removeUpdates(listener);
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    public class MyLocationListener implements LocationListener
    {
        LocationToServerTask task = new LocationToServerTask();
        public void onLocationChanged(final Location loc)
        {
            DBHelper.LOG( "Location changed.." + loc.getProvider() + loc.getLatitude() + "," + loc.getLongitude());

            if(isBetterLocation(loc, previousBestLocation)) {

                //stop the gps now.
                locationManager.removeUpdates(listener);

                //save to local database
                databaseHelper.updatePersonLocationInDB( databaseHelper.getUserID() , loc , Person.FRIENDS_TYPE.USER );

                //save data to server
                String url = "http://dhrj.eu5.org/update_loc.php?"
                        +"lat=" + loc.getLatitude()+"&"
                        +"lng=" + loc.getLatitude()+"&"
                        +"time=NOW()"+"&"
                        +"accuracy=" +loc.getAccuracy() ;
                try {
                    if (task != null && task.getStatus() != AsyncTask.Status.RUNNING)
                        task.execute(new String[]{url});
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }
    }

    private class LocationToServerTask extends AsyncTask<String, Integer, Long> {
        // these Strings / or String are / is the parameters of the task, that can be handed over via the excecute(params)
        // method of AsyncTask
        protected Long doInBackground(String ... urls) {
            try {
                // if we have no data connection, no point in proceeding.
                NetworkInfo ni = cnnxManager.getActiveNetworkInfo();
                if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
                    Log.e("LOG_DHEERAJ", "No usable network.");
                }
                else {
                    DefaultHttpClient client = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(urls[0]);
                    HttpResponse execute = client.execute(httpGet);
                    Log.e("LOG_DHEERAJ", "Successfuly Sent to client URL : "+ urls[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                // always set the next wakeup alarm.
                alarms.cancel(alarmIntent);
                alarmIntent.cancel();

                Intent intentOnAlarm = new Intent( LaunchReceiver.ACTION_PULSE_SERVER_ALARM );
                alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 234324243 , intentOnAlarm, 0 );
                int interval = 60 ;
                long timeToAlarm = SystemClock.elapsedRealtime() + interval  * 1000;
                alarms.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeToAlarm, alarmIntent);
                stopSelf();
            }
            return 0L;
        }

        // this is called whenever you call puhlishProgress(Integer), for example when updating a progressbar when downloading stuff
        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        // the onPostexecute method receives the return type of doInBackGround()
        protected void onPostExecute(Long result) {
            // do something with the result, for example display the received Data in a ListView
            // in this case, "result" would contain the "someLong" variable returned by doInBackground();
        }
    }
}