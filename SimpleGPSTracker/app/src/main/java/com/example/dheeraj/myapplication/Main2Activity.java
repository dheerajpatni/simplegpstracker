package com.example.dheeraj.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MyMapFragment mMapFragment;
    private BroadcastReceiver mBroadCastReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //change image,nae,imal in navigation header.
        View headerLayout = navigationView.getHeaderView(0);
        ImageView imageViewProfile = (ImageView)headerLayout.findViewById(R.id.imageView);
        ImageLoader loader = new ImageLoader(this);
        DBHelper dbHelper = new DBHelper(this);
        Person user = new Person();
        dbHelper.GetFriendInfoFromLocalDB( "",dbHelper.getUserID() , user );
        loader.LoadImageInToView( user.mobile  , imageViewProfile , 200 );
        TextView txtBoxName = (TextView)headerLayout.findViewById(R.id.textViewName);
        txtBoxName.setText(user.name);
        TextView txtBoxEmail = (TextView)headerLayout.findViewById(R.id.textViewEmail);
        txtBoxEmail.setText(user.email);
        DBHelper.LOG( "dheeraj_mytext " + user.name );
        //
        AfterNavigationViewLoaded();
    }

    void AfterNavigationViewLoaded()
    {


        //Create map view
        mMapFragment = new MyMapFragment();
        getFragmentManager().beginTransaction().replace(R.id.container,mMapFragment).commit();

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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_1) {
            getFragmentManager().beginTransaction().replace(R.id.container,mMapFragment).commit();
        } else if (id == R.id.nav_2) {
            FriendsFragment notificationsFragment =  new FriendsFragment();
            getFragmentManager().beginTransaction().replace(R.id.container,notificationsFragment).commit();
        } else if (id == R.id.nav_3) {

        } else if (id == R.id.nav_4) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onDestroy()
    {
        Log.e("LOG_DHEERAJ","Destroing acivity.");
        unregisterReceiver(mBroadCastReciever);
        super.onDestroy();
    }
}
