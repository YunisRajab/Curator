package com.yunisrajab.curator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ListView mListView;
    String TAG = "Curator list";
    String url;
    DatabaseReference mDatabaseReference;
    User    mUser;
    UserLocalData   mUserLocalData;
    boolean doubleBackPressedOnce   =   false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        mListView   =   (ListView)  findViewById(R.id.favList);
        mUserLocalData  =   new UserLocalData(getApplicationContext());
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);

        FirebaseListAdapter<Video>  adapter =   new FirebaseListAdapter<Video>(
                this,   Video.class,
                R.layout.list_item,    mDatabaseReference
        ) {
            @Override
            protected void populateView(View v, Video model, int position) {
                ((TextView)v.findViewById(R.id.titleView)).setText(model.getTitle());
                ((TextView)v.findViewById(R.id.urlView)).setText(model.getUrl());
                ((TextView)v.findViewById(R.id.ratingView)).setText(String.valueOf(model.getRating()));
            }
        };
        mListView.setAdapter(adapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Video   video = (Video) mListView.getItemAtPosition(pos);
                String  url =   video.getUrl();
                String  videoID;
                if (url.contains("=")) {
                    videoID = url.substring(url.lastIndexOf("=") + 1);
                }   else {
                    videoID = url.substring(url.lastIndexOf("/") + 1);
                }
                return true;
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Favourites");
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView bottomNavigationView    =   findViewById(R.id.bottomNavigation);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu   menu    =   bottomNavigationView.getMenu();
        MenuItem    menuItem    =   menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(mItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener   mItemSelectedListener   =   new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent  intent;
            switch (item.getItemId())   {
                case R.id.bn_cloud:
                    intent = new Intent(ListActivity.this , MainActivity.class);
                    ListActivity.this.startActivity(intent);
                    Log.i(TAG,"Main layout");
                    break;
                case R.id.bn_fav:
                    break;
                case R.id.bn_history:
                    Toast.makeText(ListActivity.this, "add", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bn_child:
                    intent = new Intent(ListActivity.this , ChildActivity.class);
                    ListActivity.this.startActivity(intent);
                    Log.i(TAG,"Child layout");
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent  intent;
        switch (id) {
            case R.id.nav_child:
//                intent = new Intent(MainActivity.this , ChildActivity.class);
//                MainActivity.this.startActivity(intent);
//                Log.i(TAG,"Child layout");
                break;

            case R.id.nav_settings:
                intent = new Intent(ListActivity.this , Settings.class);
                ListActivity.this.startActivity(intent);
                Log.i(TAG,"Settings layout");
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

//            url = mURLs.get(index2);
//            Log.e(TAG,""+url);
//            launchVideo();
        }
    };

    private void launchVideo()  {
        Intent intent;
        if (!url.contains("youtube")&&!url.contains("youtu.be"))  {
            intent = new Intent(ListActivity.this , VideoActivity.class);
            intent.putExtra("url",url);
            startActivity(intent);
            Log.i(TAG,"Video layout");

        }   else    {
            intent = new Intent(getApplicationContext() , YouTubeActivity.class);
            intent.putExtra("url",url);
            startActivity(intent);
            Log.i(TAG,"YouTube layout");
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackPressedOnce)  {
                Intent intent = new Intent(ListActivity.this, CloseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
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

