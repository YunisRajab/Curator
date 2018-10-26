package com.yunisrajab.curator.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.Fragments.FavouritesFragment;
import com.yunisrajab.curator.Fragments.HistoryFragment;
import com.yunisrajab.curator.Fragments.MainFragment;
import com.yunisrajab.curator.History;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    String TAG = "Curator";
    BottomNavigationView    mBottomNavigationView;
    FrameLayout mainFrame;
    Toolbar mToolbar;
    UserLocalData userLocalData;
    User mUser;
    DatabaseManager mDatabaseManager;
    MainFragment mMainFragment;
    FavouritesFragment mFavouritesFragment;
    HistoryFragment mHistoryFragment;
    boolean doubleBackPressedOnce   =   false;
    int listCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.i("Curator","Main layout");

        userLocalData   =   new UserLocalData(this);

//        TODO check if user logged in with mAuth
        if (!userLocalData.getUserLoggedIn())    {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            Log.i("Curator", "Login layout");
            finish();
        }   else    {
            mUser   =   userLocalData.getLoggedUser();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(mUser.email, mUser.password);
        }

        mDatabaseManager    =   new DatabaseManager(this);
        mDatabaseManager.setSU();

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    mDatabaseManager.upload(sharedText);
                }
            }
        }

        mDatabaseManager.retrieveFavsAndVotes();
        getCurated();
        getFavourites();
        getMain();
        getHistory();

        mainFrame   =   (FrameLayout)   findViewById(R.id.mainFrame);
        mMainFragment   =   new MainFragment();
        mFavouritesFragment =   new FavouritesFragment();
        mHistoryFragment    =   new HistoryFragment();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Lighthouse");
        setSupportActionBar(mToolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(drawerListener);

        mBottomNavigationView    =   findViewById(R.id.bottomNavigation);
        mBottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu    =   mBottomNavigationView.getMenu();
        MenuItem    menuItem    =   menu.getItem(0);
        menuItem.setChecked(true);
        mBottomNavigationView.setOnNavigationItemSelectedListener(bottomListener);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter("update"));
    }

    BroadcastReceiver mBroadcastReceiver  =   new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Video>    arrayList   =   mFavouritesFragment.getArrayList();
            Bundle  bundle  =   intent.getExtras();
            HashMap<Boolean,    Video>  map =   (HashMap<Boolean, Video>)   bundle.getSerializable("map");
            if (map.get(true)   !=  null)   arrayList.add(map.get(true));
            else    for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i) !=null && arrayList.get(i).getID().equals(map.get(false).getID())) {
                    arrayList.remove(i);
                }
            }
            bundle   =   new Bundle();
            bundle.putSerializable("arraylist",  arrayList);
            mFavouritesFragment.setArguments(bundle);

            arrayList   =   mMainFragment.getArrayList();
            if (!arrayList.contains(map.get(true)))  {
                arrayList.add(map.get(true));
                bundle   =   new Bundle();
                bundle.putString("type","main");
                bundle.putSerializable("arraylist",  arrayList);
                mMainFragment.setArguments(bundle);
            }
        }
    };

    public void getFavourites(){

        final OnGetDataListener   onGetDataListener   =   new OnGetDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(HashMap<String,HashMap<String,String>> map) {
                ArrayList<Video>    arrayList   =   new ArrayList<>();
                for (String key: map.keySet()) {
                    HashMap<String,String> sMap    =   map.get(key);
                    Video   video   =   new Video(sMap.get("title"),
                            sMap.get("id"), Integer.valueOf(String.valueOf(sMap.get("rating"))));
                    arrayList.add(video);
                }
                Collections.sort(arrayList,    new Comparator<Video>() {
                    @Override
                    public int compare(Video video, Video t1) {
                        return video.getTitle().compareTo(t1.getTitle());
                    }
                });
                Bundle  bundle   =   new Bundle();
                bundle.putSerializable("arraylist",  (Serializable)  arrayList);
                mFavouritesFragment.setArguments(bundle);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        };

        onGetDataListener.onStart();
        FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid).child("White_List").orderByChild("title")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        HashMap<String,HashMap<String,String>>   map =
                                (HashMap<String, HashMap<String, String>>)   dataSnapshot.getValue();
                        onGetDataListener.onSuccess(map);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG,  databaseError.getDetails());
                    }
                });
    }

    public void getMain(){

        final OnGetDataListener onGetDataListener =   new OnGetDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(HashMap<String,HashMap<String,String>> map) {
                ArrayList<Video>    arrayList   =   new ArrayList<>();
                for (String key: map.keySet()) {
                    HashMap<String,String>  sMap    =   map.get(key);
                    Video   video   =   new Video(sMap.get("title"),
                            sMap.get("id"), Integer.valueOf(String.valueOf(sMap.get("rating"))));
                    arrayList.add(video);
                }
                Collections.sort(arrayList,    new Comparator<Video>() {
                    @Override
                    public int compare(Video video, Video t1) {
                        return video.getRating()-t1.getRating();
                    }
                });
                Collections.reverse(arrayList);
                Bundle  bundle   =   new Bundle();
                bundle.putString("type","main");
                bundle.putSerializable("arraylist",  (Serializable)  arrayList);
                mMainFragment.setArguments(bundle);
                if (listCount   ==  1)  setFragment(mMainFragment);
                else listCount++;
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        onGetDataListener.onStart();
        FirebaseDatabase.getInstance().getReference().child("Main_List").orderByChild("rating")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String,HashMap<String,String>>   map =
                        (HashMap<String, HashMap<String, String>>)   dataSnapshot.getValue();
                onGetDataListener.onSuccess(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onGetDataListener.onFailed(databaseError);
            }
        });
    }

    public void getHistory(){

        final OnGetDataListener onGetDataListener   =   new OnGetDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(HashMap<String,HashMap<String,String>> map) {
                ArrayList<History>  arrayList   =   new ArrayList<>();
                for (String key: map.keySet()) {
                    HashMap<String,String>  sMap    =   map.get(key);
                    History history   =   new History(sMap.get("title"),
                            sMap.get("id"), sMap.get("time"),   sMap.get("key"));
                    arrayList.add(history);
                }
                Collections.sort(arrayList,    new Comparator<History>() {
                    @Override
                    public int compare(History history, History t1) {
                        return history.getTime().compareTo(t1.getTime());
                    }
                });
                Collections.reverse(arrayList);
                Bundle  bundle   =   new Bundle();
                bundle.putSerializable("arraylist",  (Serializable)  arrayList);
                mHistoryFragment.setArguments(bundle);

            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        };

        onGetDataListener.onStart();
        FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid).child("History").orderByChild("time")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        HashMap<String,HashMap<String,String>>   map =
                                (HashMap<String, HashMap<String, String>>)   dataSnapshot.getValue();
                        onGetDataListener.onSuccess(map);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG,  databaseError.getDetails());
                    }
                });
    }

    private void getCurated()   {
        final OnGetDataListener onGetDataListener =   new OnGetDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(HashMap<String,HashMap<String,String>> map) {
                ArrayList<Video>    arrayList   =   new ArrayList<>();
                for (String key: map.keySet()) {
                    HashMap<String,String>  sMap    =   map.get(key);
                    Video   video   =   new Video(sMap.get("title"),
                            sMap.get("id"), Integer.valueOf(String.valueOf(sMap.get("rating"))));
                    arrayList.add(video);
                }
                Collections.sort(arrayList,    new Comparator<Video>() {
                    @Override
                    public int compare(Video video, Video t1) {
                        return video.getRating()-t1.getRating();
                    }
                });
                Collections.reverse(arrayList);
                Bundle  bundle   =   new Bundle();
                bundle.putString("type","curated");
                bundle.putSerializable("arraylist",  (Serializable)  arrayList);
                mMainFragment.setArguments(bundle);
                if (listCount   ==  1)  setFragment(mMainFragment);
                else listCount++;
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        onGetDataListener.onStart();
        FirebaseDatabase.getInstance().getReference().child("Curated_List").orderByChild("rating")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        HashMap<String,HashMap<String,String>>   map =
                                (HashMap<String, HashMap<String, String>>)   dataSnapshot.getValue();
                        onGetDataListener.onSuccess(map);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        onGetDataListener.onFailed(databaseError);
                    }
                });
    }

    private void setFragment(Fragment   fragment)  {
        FragmentTransaction fragmentTransaction =   getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame,  fragment);
        fragmentTransaction.commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener   bottomListener   =   new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent  intent;
            switch (item.getItemId())   {
                case R.id.bn_cloud:
                    setFragment(mMainFragment);
                    mToolbar.setTitle("Lighthouse");
                    break;
                case R.id.bn_fav:
                    setFragment(mFavouritesFragment);
                    mToolbar.setTitle("Favourites");
                    break;
                case R.id.bn_history:
                    setFragment(mHistoryFragment);
                    mToolbar.setTitle("History");
                    break;
                case R.id.bn_child:
                    intent = new Intent(MainActivity.this , ChildActivity.class);
                    MainActivity.this.startActivity(intent);
                    Log.i(TAG,"Child layout");
                    break;
            }
            return true;
        }
    };

    private NavigationView.OnNavigationItemSelectedListener drawerListener  =   new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            Intent  intent;
            switch (id) {
                case R.id.nav_child:
                    break;
                case R.id.nav_parent:
                    break;
                case R.id.nav_browser:
                    intent = new Intent(MainActivity.this , WebActivity.class);
                    MainActivity.this.startActivity(intent);
                    Log.i(TAG,"Browser layout");
                    break;
                case R.id.nav_settings:
                    intent = new Intent(MainActivity.this , Settings.class);
                    MainActivity.this.startActivity(intent);
                    Log.i(TAG,"Settings layout");
                    break;

            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackPressedOnce)  {
                Intent intent = new Intent(MainActivity.this, CloseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
