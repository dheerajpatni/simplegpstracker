package com.example.dheeraj.myapplication;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class FriendsFragment extends ListFragment
        implements OnItemClickListener
{

    String[] menutitles;
    TypedArray menuIcons;

    CustomAdapter adapter;
    ProgressDialog progressDialog;

    DBHelper databaseHelper;
    AcceptFriendTask mAcceptFriendTask;
    ArrayList<Person> persons = new ArrayList<Person>();

    ImageLoader imageLoader;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        menutitles = getResources().getStringArray(R.array.titles);
        menuIcons = getResources().obtainTypedArray(R.array.icons);

        databaseHelper = new DBHelper(getActivity());
        databaseHelper.GetFriendsList(persons);
        persons.remove(0); //remove user himself

        adapter = new CustomAdapter(getActivity() , this );
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

        imageLoader = new ImageLoader(getActivity());
    }

    void RefreshList()
    {
        databaseHelper.GetFriendsList(persons);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        //Toast.makeText(getActivity(), menutitles[position], Toast.LENGTH_SHORT)
        //        .show();

        Person person = persons.get(position);
        Intent myIntent = new Intent(getActivity(), UserProfileActivity.class);
        myIntent.putExtra("friend_name", person.name);
        myIntent.putExtra("friend_mobile", person.mobile);
        getActivity().startActivity(myIntent);
    }

    public void onButtonClick( Person.FRIENDS_TYPE type , int  position ) {

        if (mAcceptFriendTask != null) {
            return;
        }

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait!");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAcceptFriendTask = new AcceptFriendTask( this , position, persons.get(position).mobile  );
        mAcceptFriendTask.execute((Void) null);
    }

    public class AcceptFriendTask extends AsyncTask<Void, Void, Boolean> {
        private  FriendsFragment mParentFragment;
        private final int mIndexItem;
        private final String mFriendMobile;
        private String mResponse;


        AcceptFriendTask(Fragment parentFragment , int indexItem,String mobile) {
            mIndexItem = indexItem;
            mFriendMobile = mobile;
            mParentFragment = (FriendsFragment)parentFragment;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean isSuccess = false;
            try {
                StringBuffer responseString=new StringBuffer();
                isSuccess = databaseHelper.acceptFriendRequest(mFriendMobile, responseString);
                Log.e("LOG_DHEERAJ", "Login Response String: " + responseString);
                mResponse = responseString.toString();
            } catch (Exception e) {
                Log.e( "LOG_DHEERAJ","Exception : "+e.getMessage());
                return false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAcceptFriendTask = null;
            if (success) {
                try {
                    mParentFragment.RefreshList();
                } catch (Exception e) {
                    Log.e("LOG_DHEERAJ", e.getMessage());
                }
            } else {
                Toast.makeText(getActivity(), "Some error occured for this.", Toast.LENGTH_SHORT);
            }
            progressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mAcceptFriendTask = null;
            //showProgress(false);
        }
    }

    //Customer adapter
    class CustomAdapter extends BaseAdapter {

        Context context;
        FriendsFragment mParentFragment;

        CustomAdapter(Context context, FriendsFragment parentFragment) {
            this.context = context;
            this.mParentFragment = parentFragment;
        }

        @Override
        public int getCount() {
            return persons.size();
        }

        @Override
        public Object getItem(int position) {
            return persons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return persons.indexOf(getItem(position));
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                LayoutInflater mInflater = (LayoutInflater) context
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

                Person person = persons.get(position);
                String title = "";
                if (person.type == Person.FRIENDS_TYPE.RECEIVED) {
                    //if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.friend_requests_list_item, null);
                    //}

                    ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imageView2);
                    TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
                    Button buttonAccept = (Button) convertView.findViewById(R.id.buttonAccept);
                    Button buttonIgnore = (Button) convertView.findViewById(R.id.buttonIgnore);

                    buttonAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mParentFragment.onButtonClick(Person.FRIENDS_TYPE.UNKNOWN, position);
                        }
                    });

                    title = txtTitle.getText().toString().replace("$1", person.name).replace("$2", person.mobile);
                    //title = person.name + "(mobile: " + person.mobile + ") want  to be your person.";
                    txtTitle.setText(title);

                    imageLoader.LoadImageInToFriendsList( person.mobile , imgIcon , 100 );

                } else if (person.type == Person.FRIENDS_TYPE.SENT) {
                    //if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.friend_pending_list_item, null);
                    //}
                    ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imageView2);
                    TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
                    Button buttonWithdraw = (Button) convertView.findViewById(R.id.buttonWithdraw);
                /*buttonWithdraw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mParentFragment.onButtonClick(DBHelper.FRIENDS_TYPE.UNKNOWN,position);
                    }
                });*/
                    txtTitle.setText(person.mobile);
                } else if (person.type == Person.FRIENDS_TYPE.ACCEPTED) {
                    //if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.friend_list_item, null);
                    //}
                    ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imageView2);
                    TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
                    TextView txtMobile = (TextView) convertView.findViewById(R.id.textView);
                    txtTitle.setText(person.name);
                    txtMobile.setText(txtMobile.getText().toString().replace("$1", person.mobile));

                    imageLoader.LoadImageInToFriendsList( person.mobile , imgIcon , 100 );
                }
                return convertView;
            }catch (Exception e) {
                DBHelper.LOG("" + e.getMessage());
                return null;
            }
        }

    }
}

