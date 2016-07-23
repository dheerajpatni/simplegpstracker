package com.example.dheeraj.myapplication;

/**
 * Created by Dheeraj on 4/23/2016.
 */

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Base64;

public class DBHelper extends SQLiteOpenHelper {

    //public static DBHelper staticDBHelper;

    public static final String BROADCAST_ACTION = "com.example.dheeraj.myapplication.LOCATION_CHANGED";
    public enum enumBROADCAST_ACTION{
        LOC_UPDATE_PERSON,
        PROFILE_PIC_CHANGE,
        NEW_FRIEDN_ADDED,
        FRIEND_UPDATED
    }

    public static final String DATABASE_NAME = "dheeraj.gps.locater.db";
    public static final String hostAddr = "http://192.168.1.104/";

    Context m_Context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        m_Context = context;
    }

    public static String getMD5Checksum(String encTarget) {
        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5" );
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5" );
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    static void LOG(String str)
    {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        int depth = 1;
        String callerFunc =  ste[3].getMethodName();
        int line = ste[3].getLineNumber();
        Log.e( "LOG_DHEERAJ" , callerFunc +"(" + line + ")-->," + ", " + str);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        try {
            db.execSQL(
                    "create table person " +
                            "(USER_ID text, MOBILE text , PASSWORD text ,EMAIL text , " +
                            "NAME text , "+
                            "TYPE int , "+
                            "PROFILE_PIC text , PROFILE_PIC_CHECKSUM text , PROFILE_PIC_REFRESH int  DEFAULT 1 , " +
                            "LAT REAL DEFAULT -1 , LNG REAL DEFAULT -1 , ACCURACY REAL DEFAULT -1 , LOC_TIME text )"
            );

            db.execSQL(
                    "create table lastupdate " +
                            "(updatetime text)"
            );

            ContentValues contentValues = new ContentValues();
            contentValues.put("updatetime", "0000-00-00 00:00:00" );
            db.insert("lastupdate", null, contentValues);
        } catch (SQLiteException exp) {
            Log.e("LOG_DHEERAJ", exp.getMessage());
        }
        Log.e("LOG_DHEERAJ", "database is created." );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS person" );
        db.execSQL("DROP TABLE IF EXISTS lastupdate" );
        onCreate(db);
        Log.e("LOG_DHEERAJ", "database is updated." );
    }

    public String getUserID() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String userName = "";
        try {
            cursor = db.rawQuery("SELECT * FROM person ORDER BY ROWID ASC LIMIT 1 ", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                userName = cursor.getString(cursor.getColumnIndex("USER_ID" ));
            }
        }
        catch (Exception e) {
            Log.e( "LOG_DHEERAJ", "getUserID : " + e.getMessage()  );
        }
        finally{
            if( cursor != null)
                cursor.close();
        }
        return userName;
    }

    public boolean updatePersonLocationInDB( String mobile  , Location loc , Person.FRIENDS_TYPE type)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Person person = new Person(false);
        person.mobile = mobile;
        person.type = type;
        person.SetLocation(loc);
        ContentValues cv = person.GetDBContentValues();
        db.update("person", cv, "MOBILE=" + mobile, null);

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("ACTION_TYPE", enumBROADCAST_ACTION.LOC_UPDATE_PERSON);

        if(type == Person.FRIENDS_TYPE.USER )
            intent.putExtra("MOBILE", "" );
        else
            intent.putExtra("MOBILE", mobile );

        LOG( person.location.getLatitude() + " " +person.location.getLongitude() );

        intent.putExtra("LOCATION.LAT", loc.getLatitude());
        intent.putExtra("LOCATION.LNG", loc.getLongitude() );
        intent.putExtra("LOCATION.ACCURACY", loc.getAccuracy() );

        m_Context.sendBroadcast(intent);
        return true;
    }

    public Bitmap getProfilePic( String table , String mobile)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String encodedImage ="";
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT PROFILE_PIC FROM person where MOBILE=?" , new String[] {mobile}  );

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            encodedImage = cursor.getString(cursor.getColumnIndex("PROFILE_PIC"));
        }
        if( encodedImage != null && encodedImage.length() > 100 ) {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            return decodedByte;
        }
        return null;
    }

    // table is either firends or requests
    public boolean GetFriendInfoFromLocalDB( String table , String friendMobileNo , Person person)
    {
        boolean ret=false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM person where MOBILE=?" , new String[] {friendMobileNo}  );
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                person.SetValueFromCursor( cursor );
                ret = true;
            }
        }
        catch (Exception e)
        {
            Log.e("LOG_DHEERAJ","In GetFriendInfoFromLocalDB:"+e.getMessage());
        }
        finally {
            cursor.close();
        }
        return ret;
    }

    private boolean GetResponseFromServer( String url , StringBuffer responseString)
    {
        try {
            LOG(url);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(content));
            String response="";
            String s = "";
            while ((s = buffer.readLine()) != null) {
                response += s;
            }
            responseString.setLength(0);
            responseString.append(response);
        }
        catch(Exception e)
        {
            Log.e("LOG_DHEERAJ", "exception: " + e.toString());
            return false;
        }
        LOG( responseString.toString());
        return true;
    }

    public boolean updateLoginInfo  ( String email , String password , String phone , StringBuffer responseString)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS person" );
        onCreate(db);

        //http://localhost/add_new_user.php?uid=2&pass=%27helloworld%27&email=%27dheerajpatni@gmail.com%27&mobile=%279711761019%27
        String url = hostAddr + "add_new_user.php?"
                +"uid=" + phone +"&"
                +"pass=" + password+"&"
                +"email=" + email +"&"
                +"mobile=" +phone +"&";

        Log.e("LOG_DHEERAJ", url);

        GetResponseFromServer(url, responseString);
        if( responseString.toString().equals("SUCCESS")  ) {
            Person user = new Person(false);
            user.mobile = phone;
            user.email = email;
            user.type = Person.FRIENDS_TYPE.USER;
            ContentValues cv = user.GetDBContentValues();
            cv.put("PASSWORD", password);
            db.insert("person", null, cv);
            Log.e("LOG_DHEERAJ", "Database is successfully added");
            return true;
        }
        return false;
    }

    public boolean sendFriendRequest( String friendMobile , StringBuffer responseString)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String userID = getUserID();

        if(  userID == "")
            return false;

        //http://dhrj.eu5.org/request.php?from=1&to=2&action=0
        String url = hostAddr+"request.php?"
                +"from='" + userID +"'&"
                +"to='" + friendMobile +"'&"
                +"action=" + 0 +"&";
        GetResponseFromServer(url, responseString);
        Log.e("LOG_DHEERAJ", "Response from GetResponseFromServer is : "+responseString +" "+ responseString.length());
        if( responseString.toString().equals("SUCCESS")  ) {
            Person sent = new Person();
            sent.mobile = friendMobile;
            sent.type = Person.FRIENDS_TYPE.SENT;
            ContentValues contentValues = sent.GetDBContentValues();
            db.insert("person", null, contentValues);
            Log.e("LOG_DHEERAJ", "Database is successfully added");
            return true;
        }
        return false;
    }

    public boolean acceptFriendRequest( String friendMobile , StringBuffer responseString)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String userID = getUserID();

        if(  userID == "")
            return false;

        //http://dhrj.eu5.org/request.php?from=1&to=2&action=0
        String url = hostAddr+"request.php?"
                +"to=" + userID +"&"
                +"from=" + friendMobile +"&"
                +"action=" + 1 +"&";
        GetResponseFromServer(url, responseString);
        Log.e("LOG_DHEERAJ", "Response from GetResponseFromServer is : "+responseString +" "+ responseString.length());
        if( responseString.toString().equals("SUCCESS")  ) {

            Person person = new Person();
            GetFriendInfoFromLocalDB("person", friendMobile, person);
            person.type = Person.FRIENDS_TYPE.ACCEPTED;
            ContentValues contentValues = person.GetDBContentValues();
            db.update("person", contentValues, "MOBILE=" + friendMobile, null);
            Log.e("LOG_DHEERAJ", "Database is successfully updated.");
            return true;
        }
        return false;
    }

    public boolean getProfilePic( String mobile , String table,  StringBuffer responseString ) {
        SQLiteDatabase db = this.getReadableDatabase();

        String url = hostAddr + "profile_pic.php?"
                + "mobile=" + mobile + "&";
        url = url.replace(" ", "%20");

        GetResponseFromServer(url, responseString);
        Log.e("LOG_DHEERAJ", "Responce from GetResponseFromServer is : " + responseString + " " + responseString.length());

        try {
            JSONObject jsonObject = new JSONObject(responseString.toString());
            if( jsonObject == null || jsonObject.getString("RESULT") != "true")
                throw(null);

            Log.e("LOG_DHEERAJ" , " RESULT : " + jsonObject.getString("RESULT"));

            //get image
            JSONObject jsonObjNewReq = jsonObject.getJSONObject("USER");
            if(  jsonObjNewReq  != null )
            {
                JSONObject actor = jsonObjNewReq;
                String checksum = actor.getString("PROFILE_PIC_CHECKSUM");
                String encodedImg = actor.getString("PROFILE_PIC");
                String calcChecksum = getMD5Checksum(encodedImg);

                if( !calcChecksum.equals(checksum) )
                {
                    Log.e("LOG_DHEERAJ", " CHECKSUM 1,2 not matching:" + getMD5Checksum(encodedImg) +"," +  checksum +",");
                    throw(null);
                }

                //add this image in database
                ContentValues cv = new ContentValues();
                cv.put("PROFILE_PIC",encodedImg);
                cv.put("PROFILE_PIC_CHECKSUM",calcChecksum);
                cv.put("PROFILE_PIC_REFRESH", 1);
                db.update( "person" , cv, "MOBILE="+mobile , null);
            }
            return true;
        } catch (Exception e) {
            Log.e("LOG_DHEERAJ", "Some error occured." + e.getMessage());
        }
        return false;
    }

    public boolean getNotifications( StringBuffer responseString  )
    {
        String userID = getUserID();

        String lasetUpdate="";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT updatetime FROM lastupdate", null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            lasetUpdate = cursor.getString(cursor.getColumnIndex("updatetime"));
        }

        String url = hostAddr+"notification_updates.php?"
                +"userid=" + userID +"&"
                +"last_updated_time=" + lasetUpdate +"&";
        url = url.replace(" ","%20");

        GetResponseFromServer(url, responseString);

        try {
            JSONObject jsonObject = new JSONObject(responseString.toString());
            String strNotificationTill = jsonObject.getString("NOTIFICATION_TILL");

            //update last notifictaion update time
            ContentValues contentValues = new ContentValues();
            contentValues.put("updatetime", strNotificationTill);
            db.update("lastupdate", contentValues, "", null);
            LOG("lastupdate is " + strNotificationTill);

            JSONArray  jsonObjNewReq = jsonObject.getJSONArray("NEW_REQ");
            for (int i=0; i<jsonObjNewReq.length(); i++) {
                JSONObject actor = jsonObjNewReq.getJSONObject(i);
                String name = actor.getString("user");
                String mobile = actor.getString("mobile");
                Log.e( "LOG_DHEERAJ", "New Request Came " + i + "," + name + "," + mobile);

                //use them
                ContentValues newReqData = new ContentValues();
                db.delete("person", "MOBILE=?", new String[]{mobile});
                newReqData.put("NAME", name);
                newReqData.put("MOBILE", mobile);
                newReqData.put("TYPE", Person.FRIENDS_TYPE.RECEIVED.ordinal());
                db.insert("person", null, newReqData);

                //lets download this user's profile pic.
                getProfilePic(mobile, "person", responseString);

                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra("ACTION_TYPE", enumBROADCAST_ACTION.NEW_FRIEDN_ADDED);
                m_Context.sendBroadcast(intent);
            }

            JSONArray  jsonObjFriends = jsonObject.getJSONArray("FRIENDS");
            for (int i=0; i<jsonObjFriends.length(); i++) {
                JSONObject actor = jsonObjFriends.getJSONObject(i);
                String name = actor.getString("user");
                String mobile = actor.getString("mobile");
                String profile_pic_checksum = actor.getString("profile_pic_checksum");
                Log.e( "LOG_DHEERAJ", "Update for Friend : " + name + "," + mobile );

                Person person = new Person();
                boolean isFriendExist = GetFriendInfoFromLocalDB("person", mobile , person);

                //use them
                ContentValues newReqData = new ContentValues();
                newReqData.put("NAME", name);
                newReqData.put("MOBILE", mobile);
                newReqData.put("TYPE", Person.FRIENDS_TYPE.ACCEPTED.ordinal());

                LOG("1");

                if( !isFriendExist ) {
                    LOG("2");
                    db.insert("person", null, newReqData);
                    LOG("3");
                    //lets download this user's profile pic.
                    getProfilePic(mobile, "person", responseString);
                    LOG("New Friend Added " + person.name + " " + person.mobile);
                }else {
                    LOG("2.1");
                    db.update("person",  newReqData , "MOBILE="+mobile , null );
                    LOG("3.1");
                    if( person.profilePicChecksum == null || !person.profilePicChecksum.equals( profile_pic_checksum ) ) {
                        LOG("4.1");
                        //person change his pic,lets download it
                        getProfilePic(mobile, "person", responseString);
                    }
                    LOG(" Friend Updated " + person.name + " " + person.mobile + " " + person.profilePicChecksum + " " + profile_pic_checksum);
                }

                LOG("5");
            }

            JSONArray  jsonObjFriendPos = jsonObject.getJSONArray("FRIENDS_POS");
            for (int i=0; i<jsonObjFriendPos.length(); i++) {
                JSONObject actor = jsonObjFriendPos.getJSONObject(i);
                String mobile = actor.getString("mobile" );
                Person person = new Person();
                boolean isFriendExist = GetFriendInfoFromLocalDB("person", mobile , person);
                if( isFriendExist ) {

                    double lat = actor.getDouble("lat");
                    double lng = actor.getDouble("lng");
                    String time = actor.getString("time");
                    double accuracy = actor.getDouble("accuracy");

                    Location loc = new Location("");
                    loc.setLatitude(actor.getDouble("lat" ));
                    loc.setLongitude(actor.getDouble("lng" ));
                    loc.setTime( 0  /*actor.getString("time")*/) ;
                    loc.setAccuracy((float)actor.getDouble("accuracy"));
                    updatePersonLocationInDB(mobile,loc, Person.FRIENDS_TYPE.ACCEPTED);
                }
            }
        }catch ( Exception ex) {
            LOG(" Exception In getNotifications : " +ex.getMessage());
        }
        return true;
    }

    public boolean uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
        byte[] byte_arr = stream.toByteArray();
        String imageStr = Base64.encodeToString(byte_arr, Base64.DEFAULT);

        final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("image", imageStr));
        try {
            String checksum = getMD5Checksum(imageStr);
            String url = hostAddr + "change_pic.php?uid=" + getUserID() +"&imgchecksum="+checksum;
            Log.e("LOG_DHEERAJ", "doFileUpload URL : " + url);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader buffer = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String responseStr="";
            String s = "";
            while ((s = buffer.readLine()) != null) {
                responseStr += s;
            }
            LOG(" Responce :" + responseStr);
            if( responseStr.equals("SUCCESS")  ) {
                //add this image in database
                SQLiteDatabase db = this.getReadableDatabase();
                ContentValues cv = new ContentValues();
                cv.put("PROFILE_PIC",imageStr); //These Fields should be your String values of actual column names
                cv.put("PROFILE_PIC_CHECKSUM", checksum);
                db.update("person", cv, "MOBILE=" + getUserID() , null);
                return true;
            }

        } catch (Exception e) {
            Log.e("LOG_DHEERAJ",  "Error in http connection " + e.toString());
        }
        return false;
    }

    void GetFriendsList( ArrayList<Person> persons)
    {
        persons.clear();

        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM person", null);
            for(int i = 0 ; i < cursor.getCount() ;i++) {
                Person person = new Person();
                cursor.moveToPosition(i);
                person.SetValueFromCursor(cursor);
                persons.add(person);
            }
            cursor.close();
        }
        catch (Exception e)
        {
            LOG(e.getMessage());
        }
        finally {
            if( !cursor.isClosed( ))
                cursor.close();
        }
    }
}