package com.yunisrajab.curator.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
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
import com.yunisrajab.curator.Adapters.FavouritesAdapter;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.Fragments.FavouritesFragment;
import com.yunisrajab.curator.Fragments.HistoryFragment;
import com.yunisrajab.curator.Fragments.MainFragment;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

import java.util.ArrayList;
import java.util.Collections;


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

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    mDatabaseManager.upload(sharedText,1);
                }
            }
        }

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

        setFragment(mMainFragment);
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
