package com.example.dheeraj.myapplication;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private BroadcastReceiver mBroadCastReciever;

    private MyMapFragment mMapFragment;

    DBHelper databaseHelper ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = getTitle();

        //Create map view
        mMapFragment = new MyMapFragment();
        getFragmentManager().beginTransaction().replace(R.id.container,mMapFragment).commit();

        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        //start location service
        startService(new Intent(getApplicationContext(), LocationService.class));

        //start notification service
        startService(new Intent(getApplicationContext(), NotificationService.class));

        //Register broadcast receiver
        mBroadCastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if( intent.getAction() == DBHelper.BROADCAST_ACTION ) {
                    Bundle b = intent.getExtras();
                    DBHelper.enumBROADCAST_ACTION actionType = DBHelper.enumBROADCAST_ACTION.values()[b.getInt("ACTION_TYPE")];
                    if( actionType == DBHelper.enumBROADCAST_ACTION.LOC_UPDATE_PERSON)
                    {
                        String mobile = b.getString("MOBILE");
                        Location loc = new Location("");
                        loc.setLatitude(b.getDouble("LOCATION.LAT" ));
                        loc.setLongitude(b.getDouble("LOCATION.LNG") );
                        loc.setAccuracy(b.getFloat("LOCATION.ACCURACY") );
                        if( mMapFragment != null )
                            mMapFragment.UpdateLocation(mobile, loc);
                        DBHelper.LOG("New location came." + loc.getLatitude() + " " + loc.getLongitude());
                    }
                }
                else if( intent.getAction() == "com.example.dheeraj.myapplication.NOTIFICATIONS"  )
                {
                    Log.e("L", "New Notification comes");
                }
            }
        };
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction("com.example.dheeraj.myapplication.LOCATION_CHANGED");
        filterSend.addAction("com.example.dheeraj.myapplication.NOTIFICATIONS");
        registerReceiver(mBroadCastReciever, filterSend);

        //database
        databaseHelper = new DBHelper(this);
    }

    @Override
    public void onDestroy()
    {
        Log.e("LOG_DHEERAJ","Destroing acivity.");
        unregisterReceiver(mBroadCastReciever);
        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if( position == 0 )
        {
            getFragmentManager().beginTransaction().replace(R.id.container,mMapFragment).commit();
        }
        else if( position == 1)
        {
            FriendsFragment notificationsFragment =  new FriendsFragment();
            getFragmentManager().beginTransaction().replace(R.id.container,notificationsFragment).commit();
        }
        else
        {
            Intent myIntent = new Intent(MainActivity.this, UserProfileActivity.class);
            myIntent.putExtra("friend_mobile" , databaseHelper.getUserID() );
            MainActivity.this.startActivity(myIntent);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if( id== R.id.action_add_friend){
            Intent myIntent = new Intent(this, AddFriendActivity.class);
            this.startActivity(myIntent);

        }
        return super.onOptionsItemSelected(item);
    }

}
