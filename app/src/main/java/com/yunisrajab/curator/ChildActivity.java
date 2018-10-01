package com.yunisrajab.curator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ChildActivity  extends AppCompatActivity   implements NavigationView.OnNavigationItemSelectedListener {

    ListView mListView;
    String TAG = "Curator child";

    DatabaseReference   mDatabaseReference;
    User    mUser;
    UserLocalData   mUserLocalData;
    boolean doubleBackPressedOnce   =   false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        mListView = findViewById(R.id.childList);
        mListView.setOnItemClickListener(itemListener);

        mUserLocalData  =   new UserLocalData(getApplicationContext());
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);

        FirebaseListAdapter<Video>  adapter =   new FirebaseListAdapter<Video>(
                this,   Video.class,
                R.layout.child_list_item,    mDatabaseReference.child("White_List").orderByChild("title")
        ) {
            @Override
            protected void populateView(View v, Video model, int position) {
                ((TextView)v.findViewById(R.id.itemName)).setText(model.getTitle());
                String  id = model.getID();
                new DownloadImageTask((ImageView) v.findViewById(R.id.thumbnail))
                        .execute("https://img.youtube.com/vi/"+id+"/0.jpg");
            }
        };
        mListView.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Child View");
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
//
//    private BottomNavigationView.OnNavigationItemSelectedListener   mItemSelectedListener   =   new BottomNavigationView.OnNavigationItemSelectedListener() {
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            Intent  intent;
//            switch (item.getItemId())   {
//                case R.id.bn_cloud:
//                    intent = new Intent(ChildActivity.this , MainActivity.class);
//                    ChildActivity.this.startActivity(intent);
//                    Log.i(TAG,"Main layout");
//                    break;
//                case R.id.bn_fav:
//                    intent = new Intent(ChildActivity.this , ListActivity.class);
//                    ChildActivity.this.startActivity(intent);
//                    Log.i(TAG,"Fave layout");
//                    break;
//                case R.id.bn_history:
//                    intent = new Intent(ChildActivity.this , HistoryActivity.class);
//                    ChildActivity.this.startActivity(intent);
//                    Log.i(TAG,"History layout");
//                    break;
//                case R.id.bn_child:
//                    break;
//            }
//            return true;
//        }
//    };

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

    private final AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Video   video = (Video) mListView.getItemAtPosition(i);
            String videoID= video.getID();
//            String  url = "https://www.youtube.com/watch?v="+videoID;
            Log.e(TAG,""+videoID);

            DateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
            String date = df.format(Calendar.getInstance().getTime());

            String  key =   mDatabaseReference.child("History").push().getKey();

            History history =   new History(video.getTitle(),videoID,date,key);

            mDatabaseReference.child("History").child(key).setValue(history);
            launchVideo(videoID);
        }
    };

    private void launchVideo(String videoID)  {
        Intent intent;
        intent = new Intent(getApplicationContext() , YouTubeActivity.class);
        intent.putExtra("videoID",videoID);
        startActivity(intent);
        Log.i(TAG,"YouTube layout");
//        if (!url.contains("youtube")&&!url.contains("youtu.be"))  {
//            intent = new Intent(ChildActivity.this , VideoActivity.class);
//            intent.putExtra("url",url);
//            startActivity(intent);
//            Log.i(TAG,"Video layout");
//        }
    }

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
