package com.example.dheeraj.myapplication;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFriendActivity extends ActionBarActivity
        implements android.view.View.OnClickListener{

    View m_buttonOK;
    View m_buttonCancel;
    View mProgressView;
    EditText mTextField;
    TextView mErrorText;
    DBHelper databaseHelper;
    AddFriendTask mAddFriendTask = null;
    ProgressDialog progressDialog;

    public AddFriendActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        setContentView(R.layout.fragment_add_friend);

        //actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //listners
        m_buttonOK = this.findViewById(R.id.btn_OK);
        m_buttonOK.setOnClickListener(this);
        m_buttonCancel = this.findViewById(R.id.btn_CANCEL);
        m_buttonCancel.setOnClickListener(this);
        mTextField = (EditText)this.findViewById(R.id.addFriendText);
        mProgressView = this.findViewById(R.id.progressBar);
        mErrorText = (TextView)this.findViewById(R.id.addFriendErrorText);

        databaseHelper = new DBHelper(this);
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_OK:
                if (mAddFriendTask != null) {
                    return;
                }

                progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Please wait!");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();

                String mobile = mTextField.getText().toString();
                mAddFriendTask = new AddFriendTask( mobile  );
                mAddFriendTask.execute((Void) null);
                break;
        }
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

    public class AddFriendTask extends AsyncTask<Void, Void, Boolean> {
        private final String mFirendMobile;
        private String mResponse;

        AddFriendTask( String mobile ) {
            mFirendMobile = mobile;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean isSuccess;
            try {
                StringBuffer responseString=new StringBuffer();
                isSuccess = databaseHelper.sendFriendRequest(mFirendMobile, responseString);
                Log.e("LOG_DHEERAJ", "Login Response String: "+ responseString);
                mResponse = responseString.toString();
            } catch (Exception e) {
                Log.e( "LOG_DHEERAJ","Exception : "+e.getMessage());
                return false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddFriendTask = null;
            progressDialog.dismiss();
            if (success) {
                Toast.makeText(AddFriendActivity.this,"Person request has been send to user.",Toast.LENGTH_SHORT);
                mErrorText.setText("Person Request is sent to the user.");
            } else {
                mErrorText.setVisibility(View.VISIBLE);
                if( mResponse.startsWith("Error :"))
                    mErrorText.setText(mResponse);
                else
                    Log.e("LOG_DHEERAJ", "Some error occured.Couldn't add as a friend.");
            }
        }

        @Override
        protected void onCancelled() {
            mAddFriendTask = null;
            progressDialog.dismiss();
        }
    }
}
