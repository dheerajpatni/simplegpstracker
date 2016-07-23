package com.example.dheeraj.myapplication;

/**
 * A simple {@link Fragment} subclass.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;


/**
 * A fragment that launches other parts of the demo application.
 */
public class MyMapFragment extends android.app.Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    DBHelper databaseHelper;
    ImageLoader imageLoader;

    ArrayList<Person>friends = new ArrayList<Person>();
    Map<String,PersonMarker> markers = new HashMap<String,PersonMarker>();
    Map<String,ImageView> personViews = new HashMap<String,ImageView>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        // latitude and longitude
        double latitude = 17.385044;
        double longitude = 78.486671;

        //not using google map location change listener,
        /*googleMap.setMyLocationEnabled(true);
        GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                googleMap.clear();
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                UpdateLocation( loc , -1);
            }
        };
        googleMap.setOnMyLocationChangeListener(myLocationChangeListener);*/

        databaseHelper = new DBHelper(getActivity());
        imageLoader = new ImageLoader(getActivity());

        //get All persons icons and pos
        databaseHelper.GetFriendsList(friends);
        for( int i  = 0 ; i< friends.size();i++) {
            if( i==0 || friends.get(i).type == Person.FRIENDS_TYPE.ACCEPTED) {
                DBHelper.LOG(friends.get(i).mobile + "," +
                        friends.get(i).location.getLatitude() + "," +
                        friends.get(i).location.getLongitude() + "," +
                        friends.get(i).location.getAccuracy() + "," );

                Bitmap bitmap = databaseHelper.getProfilePic("",friends.get(i).mobile);

                //create canvas
                Bitmap imageBitmap = databaseHelper.getProfilePic("",friends.get(i).mobile);
                if(imageBitmap==null )
                {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inMutable = true;
                    imageBitmap=BitmapFactory.decodeResource(getResources(),
                            R.drawable.no_pic,opt);
                }
                Bitmap resized = Bitmap.createScaledBitmap(imageBitmap, 80 , 80 , true);
                Bitmap baloonBmp = _CreateBaloonBitmap( resized , 80 , 25 , 4  );
                Bitmap PersonBmp = _CreatePersonBitmap( resized , 80 , 4   );

                PersonMarker markerA = new PersonMarker();
                markerA.Create(friends.get(i) , baloonBmp);
                markers.put(friends.get(i).mobile,markerA);

                LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.fragmentMap_linearLayout);
                View row = inflater.inflate(R.layout.fragment_map_people_list_item, null);
                ImageView imgView = (ImageView)row.findViewById(R.id.fragmentMapPeopleListItem_imgView);
                //imgView.setImageBitmap(PersonBmp);
                imageLoader.LoadImageInToMapList(friends.get(i).mobile,imgView,100);
                linearLayout.addView(row);
                personViews.put(friends.get(i).mobile,imgView);

                class MyLovelyOnClickListener implements View.OnClickListener {
                    String mobile;
                    public MyLovelyOnClickListener(String mobile) {
                        this.mobile = mobile;
                    }
                    @Override
                    public void onClick(View v) {
                        markers.get(mobile).ZoomTo();
                    }
                };
                imgView.setOnClickListener( new MyLovelyOnClickListener( friends.get(i).mobile ));
            }
        }
        markers.get( databaseHelper.getUserID() ).ZoomTo();
        return v;
    }

    public void UpdateLocation(String mobile , Location loc)
    {
        LatLng latLng = new LatLng(  loc.getLatitude() , loc.getLongitude() );
        PersonMarker marker = markers.get( mobile );
        if( marker != null )
        {
            marker.UpdatePos(latLng);
            if(  mobile.equals( databaseHelper.getUserID()) )
            {
                marker.ZoomTo();
            }
        }


        /*
        //either use
        googleMap.clear();

        double latitude = loc.getLatitude();
        double longitute = loc.getLongitude();
        double accuracy = loc.getAccuracy();
        LatLng latLng = new LatLng(  latitude , longitute );

        String gText = "Me";
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(61, 61, 61));
        paint.setTextSize((int) (50));
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(100, 100, conf);
        Canvas canvas = new Canvas(bmp);
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bmp.getWidth() - bounds.width())/2;
        int y = (bmp.getHeight() + bounds.height())/2;
        canvas.drawText(gText, x, y, paint);
        mMarker = googleMap.addMarker(
                new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                            );

        if(googleMap != null){
            if( mAccuracyCircle != null)
                mAccuracyCircle.remove();
            mAccuracyCircle = null;
            CircleOptions circleOp;
            circleOp = new CircleOptions();
            circleOp.center(latLng);
            circleOp.radius(accuracy);
            circleOp.fillColor(0x559f9f9f); //argb
            circleOp.strokeColor(0x55666666); //argb
            circleOp.strokeWidth(1.0f);   //border of circle
            mAccuracyCircle = googleMap.addCircle(circleOp);

            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(18).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
        }*/

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    class PersonMarker{
        public Person m_person;
        Marker mMarker;

        public boolean Create( Person person , Bitmap profilePic)
        {
            LatLng latLng = new LatLng(person.location.getLatitude(),person.location.getLongitude());

            //create marker
            mMarker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            //.icon(BitmapDescriptorFactory.fromBitmap(profilePic))
                            .anchor(0.5f, 1.f)
            );

            imageLoader.LoadImageInToMarker(person.mobile,mMarker,100);

            //marker click callback
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker arg0) {
                    Toast.makeText(getActivity(), arg0.getTitle(), Toast.LENGTH_SHORT).show();// display toast
                    return true;//means consumed, false means that deaflaut action will also apper.
                }
            });

            m_person = person;
            return true;
        }

        public void UpdateImage( Bitmap profilePic ) {
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(profilePic));
        }

        public void UpdatePos(LatLng latlng)
        {
            mMarker.setPosition( latlng  );
        }

        public void ZoomTo()
        {
            LatLng latLng = new LatLng(m_person.location.getLatitude(),m_person.location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(18).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public Bitmap _CreateBaloonBitmap( Bitmap profilePic , int bitmapSize , int pinSize , int border)
    {
        //first create for baloon.
        Bitmap canvasBmp = Bitmap.createBitmap( bitmapSize + 2*border , bitmapSize + 2*border + pinSize , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBmp);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        canvas.drawRect(0, 0, bitmapSize + 2 * border, bitmapSize + 2 * border, color);
        //canvas.drawCircle( (bitmapSize + 2*border)/2, (bitmapSize + 2*border)/2, bitmapSize/2 , color);
        //color.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(profilePic, border , border , color);
        //color.setTextSize(10);
        //canvas.drawText("Le Messi", 30, 40, color);
        color.setXfermode(null);
        color.setColor(Color.BLACK);
        Point a = new Point( (bitmapSize + 2*border) / 2 -10  , bitmapSize + 2*border );
        Point b = new Point( (bitmapSize + 2*border) / 2 +10 , bitmapSize + 2*border );
        Point c = new Point( (bitmapSize + 2*border) / 2  , bitmapSize + 2*border + pinSize);
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();
        canvas.drawPath(path, color);
        //color.setStyle(Paint.Style.STROKE);
        //color.setStrokeWidth(border);
        //canvas.drawCircle( (bitmapSize + 2*border)/2, (bitmapSize + 2*border)/2, (bitmapSize+border)/2 , color );
        return canvasBmp;
    }

    public Bitmap _CreatePersonBitmap( Bitmap profilePic , int bitmapSize  , int border)
    {
        //first create for baloon.
        Bitmap canvasBmp = Bitmap.createBitmap( bitmapSize + 2*border , bitmapSize + 2*border  , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBmp);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        canvas.drawRect(0, 0, bitmapSize + 2 * border, bitmapSize + 2 * border, color);
        //canvas.drawCircle( (bitmapSize + 2*border)/2, (bitmapSize + 2*border)/2, bitmapSize/2 , color);
        //color.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(profilePic, border , border , color);
        //color.setTextSize(10);
        //canvas.drawText("Le Messi", 30, 40, color);
        color.setXfermode(null);
        color.setColor(Color.BLACK);
        Point a = new Point( (bitmapSize + 2*border) / 2 -10  , bitmapSize + 2*border );
        Point b = new Point( (bitmapSize + 2*border) / 2 +10 , bitmapSize + 2*border );
        Point c = new Point( (bitmapSize + 2*border) / 2  , bitmapSize + 2*border );
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();
        canvas.drawPath(path, color);
        //color.setStyle(Paint.Style.STROKE);
        //color.setStrokeWidth(border);
        //canvas.drawCircle( (bitmapSize + 2*border)/2, (bitmapSize + 2*border)/2, (bitmapSize+border)/2 , color );
        return canvasBmp;
    }
}