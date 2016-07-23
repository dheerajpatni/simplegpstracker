package com.example.dheeraj.myapplication;


import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class UserProfileActivity extends ActionBarActivity {

    private static Bitmap finalBitmap = null;
    private ImageView imageView;
    private static final int GALLERY = 1;
    private final int PIC_CROP = 3;
    DBHelper databaseHelper;
    ImageUploadTask imageUploadTask;
    private ProgressDialog progressDialog;

    ImageLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        setContentView(R.layout.fragment_user_profile);

        TextView textUsername = (TextView)this.findViewById(R.id.textUsername);
        TextView textMobile = (TextView)this.findViewById(R.id.textView5);
        TextView textEmail = (TextView)this.findViewById(R.id.textView7);

        databaseHelper = new DBHelper(this);

        Intent intent = getIntent();
        Person person = new Person();
        String mobile = intent.getStringExtra("friend_mobile");
        databaseHelper.GetFriendInfoFromLocalDB("", mobile , person  );
        textUsername.setText(person.name);

        if( person.email==null || person.email.length()<=0  )
            textEmail.setText("<unknown>");
        else
            textEmail.setText(person.email);

        // get action bar
        ActionBar actionBar = getSupportActionBar();
        // Enabling Up / Back navigation
        actionBar.setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) this.findViewById(R.id.imageView3);
        if( mobile != null && mobile.equals(databaseHelper.getUserID())) {
            Button buttonname;
            buttonname = (Button) this.findViewById(R.id.button);
            buttonname.setVisibility(View.VISIBLE);
            buttonname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImageFromGallery();
                }
            });
        }

        //Bitmap currentProfilePic = databaseHelper.getProfilePic("person", intent.getStringExtra("friend_mobile") );
        //if( currentProfilePic != null)
        //    imageView.setImageBitmap(currentProfilePic);

        loader = new ImageLoader(this);
        loader.LoadImageInToView( intent.getStringExtra("friend_mobile") , imageView , 0 );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Opens dialog picker, so the user can select image from the gallery. The
     * result is returned in the method <code>onActivityResult()</code>
     */
    public void selectImageFromGallery() {
        //THIS start new Gallary that crash in the case of kitkat
        Intent intent;
        if( true ){ //(Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY);
        } else {
            intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, GALLERY);
        }
    }

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Toast.makeText(UserProfileActivity.this, "ON onActivityResult", Toast.LENGTH_SHORT);
        Log.e("LOG_DHEERAJ","onActivityResult");

        if (requestCode == GALLERY && resultCode == RESULT_OK && null != data && data.getData() != null) {

            Log.e("LOG_DHEERAJ","onActivityResult2");

            Uri filePath = data.getData();
            try {

                performCrop( filePath );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == PIC_CROP &&  resultCode == RESULT_OK && null != data ) {
            // get the returned data
            Bundle extras = data.getExtras();
            // get the cropped bitmap
            finalBitmap = extras.getParcelable("data");
            OnGetCroppedImage();
        }
    }

    void OnGetCroppedImage()
    {
        if( finalBitmap != null ) {
            //first set this image in imageview
            imageView.setImageBitmap(finalBitmap);

            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait!");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

            //start uploading image
            imageUploadTask = new ImageUploadTask();
            imageUploadTask.execute();
        }
    }

    class ImageUploadTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            if(databaseHelper.uploadImage(finalBitmap) )
            {
                return "SUCCESS";
            }
            return "FAILED";
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if( result == "SUCCESS" )
                Toast.makeText(getApplicationContext(), "file uploaded successfully",Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(), "couldn't upload.Some error occured.",Toast.LENGTH_LONG).show();
        }
    }
}
