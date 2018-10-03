package com.yunisrajab.curator.Activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yunisrajab.curator.Adapters.ChildAdapter;
import com.yunisrajab.curator.Adapters.FavouritesAdapter;
import com.yunisrajab.curator.DownloadImageTask;
import com.yunisrajab.curator.History;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ChildActivity  extends AppCompatActivity   implements NavigationView.OnNavigationItemSelectedListener {

    String TAG = "Curator child";
    DatabaseReference   mDatabaseReference;
    User mUser;
    UserLocalData mUserLocalData;
    boolean doubleBackPressedOnce   =   false;
    DevicePolicyManager mDevicePolicyManager;
    ArrayList<Video>    mArrayList;
    RecyclerView    mRecyclerView;
    ChildAdapter    mAdapter;
    ProgressDialog mProgressDialog;
    SwipeRefreshLayout  mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        mRecyclerView = findViewById(R.id.childList);
        mArrayList  =   new ArrayList<>();

        mUserLocalData  =   new UserLocalData(getApplicationContext());
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);

        mProgressDialog =   new ProgressDialog(this);
        mSwipeRefreshLayout =   (SwipeRefreshLayout)    findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFavourites();
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(getApplicationContext().getColor(R.color.colorPrimary));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Child View");
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
                // start lock task mode if its not already active
        if(mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())){
            ActivityManager am = (ActivityManager) getSystemService(
                    Context.ACTIVITY_SERVICE);
            if(am.getLockTaskModeState() ==
                    ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgressDialog.setMessage("LOADING...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        getFavourites();
    }

    public void getFavourites(){

        final OnGetDataListener onGetDataListener   =   new OnGetDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ArrayList data) {
                mArrayList  =   data;
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                mRecyclerView.setHasFixedSize(true);
                // use a linear layout manager
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                mAdapter = new ChildAdapter(ChildActivity.this,    mArrayList);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
                mRecyclerView.setAdapter(mAdapter);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        };

        onGetDataListener.onStart();
        mArrayList.clear();
        FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid).child("White_List")
                .orderByChild("title").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            mArrayList.add(child.getValue(Video.class));
                        }
                        onGetDataListener.onSuccess(mArrayList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG,  databaseError.getDetails());
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.nav_settings:
                intent = new Intent(ChildActivity.this , Settings.class);
                ChildActivity.this.startActivity(intent);
                Log.i(TAG,"Settings layout");
                break;

            case R.id.nav_parent:
//                intent = new Intent(MainActivity.this , ListActivity.class);
//                MainActivity.this.startActivity(intent);
//                Log.i(TAG,"List layout");
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


//    private void launchVideo(String videoID)  {
//        Intent intent;
//        intent = new Intent(getApplicationContext() , YouTubeActivity.class);
//        intent.putExtra("videoID",videoID);
//        startActivity(intent);
//        Log.i(TAG,"YouTube layout");
//        if (!url.contains("youtube")&&!url.contains("youtu.be"))  {
//            intent = new Intent(ChildActivity.this , VideoActivity.class);
//            intent.putExtra("url",url);
//            startActivity(intent);
//            Log.i(TAG,"Video layout");
//        }
//    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackPressedOnce)  {
                Intent intent = new Intent(ChildActivity.this, MainActivity.class);
                startActivity(intent);
            }   else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                doubleBackPressedOnce   =   true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackPressedOnce   =   false;
                    }
                },  2000);
            }

        }
    }
}
