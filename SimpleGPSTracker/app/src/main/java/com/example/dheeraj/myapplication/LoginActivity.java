package com.example.dheeraj.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private DBHelper databaseHelper;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mmobileView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DBHelper(getApplicationContext());
        if( !databaseHelper.getUserID().equals("") )
        {
            Intent mainActivityIntent = new Intent(LoginActivity.this,Main2Activity.class);
            mainActivityIntent.putExtra("userid", databaseHelper.getUserID() );
            startActivity(mainActivityIntent);
            finish();
            return;
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mmobileView = (EditText) findViewById(R.id.mobile);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mmobileView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String mobile = mmobileView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if ( !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //check for valid mobile number
        if (TextUtils.isEmpty(mobile) || !TextUtils.isDigitsOnly(mobile) ) {
            mmobileView.setError(getString(R.string.error_invalid_mobile));
            focusView = mmobileView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);

            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait!");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();


            mAuthTask = new UserLoginTask(email, mobile, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mMobile;
        private final String mPassword;

        UserLoginTask(String email, String mobile , String password) {
            mEmail = email;
            mPassword = password;
            mMobile = mobile;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean isSuccess;
            try {
                // Simulate network access.
                Thread.sleep(500);
                StringBuffer responseString=new StringBuffer();
                isSuccess = databaseHelper.updateLoginInfo(mEmail,mPassword,mMobile,responseString);
                Log.e("LOG_DHEERAJ", "Login Response String: "+ responseString);
            } catch (InterruptedException e) {
                return false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            //showProgress(false);
            if (success) {
                Log.e("LOG_DHEERAJ", "Success, now starting activity.");
                //Intent mainActivityIntent = new Intent(LoginActivity.this,MainActivity.class);
                Intent mainActivityIntent = new Intent(LoginActivity.this,Main2Activity.class);
                mainActivityIntent.putExtra("userid",mMobile);
                startActivity( mainActivityIntent );
                finish();
            } else {
                Log.e("LOG_DHEERAJ", "Some error occured.Couldn't logon");
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            progressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            progressDialog.dismiss();
        }
    }
}

