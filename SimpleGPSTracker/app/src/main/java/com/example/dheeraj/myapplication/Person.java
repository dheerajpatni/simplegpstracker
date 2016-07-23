package com.example.dheeraj.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;

import java.io.Serializable;

/**
 * Created by Dheeraj on 6/3/2016.
 */
public class Person implements Serializable {

    enum FRIENDS_TYPE {
        SENT,
        RECEIVED,
        ACCEPTED,
        USER,
        UNKNOWN;
    }

    Person(boolean b)
    {
        isPicOld = 1; type = FRIENDS_TYPE.UNKNOWN;
    }

    Person(  ) {

        type = FRIENDS_TYPE.UNKNOWN;

        name ="";
        mobile = "";
        email = "";
        profilePicChecksum = "";
        encodedProfilePic = "";
        SetLocation( -1 , -1 , 999 ,  0);
        isPicOld = 0;
    }

    void SetLocation(double lat , double lng ,  float accuracy , long time)
    {
        location = null;
        location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setAccuracy(accuracy);
        location.setTime(time);
    }

    void SetLocation(Location loc)
    {
        location = null;
        location = new Location(loc);
    }

    ContentValues GetDBContentValues()
    {
        ContentValues cv = new ContentValues();
        if( name!=null )
            cv.put("NAME", name);
        if( mobile!=null ) {
            cv.put("USER_ID", mobile);
            cv.put("MOBILE", mobile);
        }
        if( email!=null )
            cv.put("EMAIL", email);

        if( type != FRIENDS_TYPE.UNKNOWN)   cv.put("TYPE", type.ordinal());

        if( location!=null ) {
            cv.put("LAT", location.getLatitude());
            cv.put("LNG", location.getLongitude());
            cv.put("ACCURACY", location.getAccuracy());
            cv.put("LOC_TIME", String.valueOf(location.getTime()));
        }
        if( profilePicChecksum != null)
            cv.put("PROFILE_PIC_CHECKSUM", profilePicChecksum );

        return cv;
    }

    void SetValueFromCursor(Cursor cursor)
    {
        if( cursor.getColumnIndex("NAME") != -1)
            name = cursor.getString(cursor.getColumnIndex("NAME"));
        if( cursor.getColumnIndex("MOBILE") != -1)
            mobile = cursor.getString(cursor.getColumnIndex("MOBILE"));
        if( cursor.getColumnIndex("TYPE") != -1)
            type = FRIENDS_TYPE.values()[cursor.getInt(cursor.getColumnIndex("TYPE" ))];
        if( cursor.getColumnIndex("EMAIL") != -1)
            email = cursor.getString(cursor.getColumnIndex("EMAIL"));
        if( cursor.getColumnIndex("LAT") != -1)
            location.setLatitude(cursor.getDouble(cursor.getColumnIndex("LAT")) );
        if( cursor.getColumnIndex("LNG") != -1)
            location.setLongitude(cursor.getDouble(cursor.getColumnIndex("LNG" )));
        if( cursor.getColumnIndex("ACCURACY") != -1)
            location.setAccuracy(cursor.getFloat(cursor.getColumnIndex("ACCURACY" )));
        if( cursor.getColumnIndex("LOC_TIME") != -1)
            location.setTime(0 /*cursor.getString(cursor.getColumnIndex("LOC_TIME"))*/);
        if( cursor.getColumnIndex("PROFILE_PIC_CHECKSUM") != -1)
            profilePicChecksum = cursor.getString(cursor.getColumnIndex("PROFILE_PIC_CHECKSUM"));
        if( cursor.getColumnIndex("PROFILE_PIC_REFRESH") != -1)
            isPicOld = cursor.getInt(cursor.getColumnIndex("PROFILE_PIC_REFRESH"));
    }

    public FRIENDS_TYPE type;
    public String name;
    public String mobile;
    public String email;
    public String encodedProfilePic;
    public String profilePicChecksum;
    public Location location;
    public int isPicOld;
}
