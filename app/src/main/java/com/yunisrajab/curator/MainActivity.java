package com.yunisrajab.curator;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.yunisrajab.curator.Fragments.FavouritesFragment;
import com.yunisrajab.curator.Fragments.HistoryFragment;
import com.yunisrajab.curator.Fragments.MainFragment;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    Button addButton, logoutButton;
    EditText urlText;
    String TAG = "Curator";
    Button  loginButton,    registerButton;
    TextView    emailText, passText;
    BottomNavigationView    mBottomNavigationView;
    FrameLayout mainFrame;
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference   mDatabaseReference;
    RecyclerView    mRecyclerView;
    ListAdapter    mAdapter;
    boolean doubleBackPressedOnce   =   false;
    DatabaseManager mDatabaseManager;
    MainFragment mMainFragment;
    FavouritesFragment mFavouritesFragment;
    HistoryFragment mHistoryFragment;
    ArrayList<Video>    mArrayList;
    ProgressDialog  mProgressDialog;
    SwipeRefreshLayout  mSwipeRefreshLayout;

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
//        mArrayList  =   new ArrayList<>();
//        mProgressDialog =   new ProgressDialog(this);
//        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
//        mRecyclerView   =   (RecyclerView)  findViewById(R.id.cloudListR);
//        mSwipeRefreshLayout =   (SwipeRefreshLayout)    findViewById(R.id.swipeRefresh);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    mDatabaseManager.upload(sharedText,1);
//                    getList();
                }
            }
        }

        mainFrame   =   (FrameLayout)   findViewById(R.id.mainFrame);
        mMainFragment   =   new MainFragment();
        mFavouritesFragment =   new FavouritesFragment();
        mHistoryFragment    =   new HistoryFragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Cloud");
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(drawerListener);

        mBottomNavigationView    =   findViewById(R.id.bottomNavigation);
        mBottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu    =   mBottomNavigationView.getMenu();
        MenuItem    menuItem    =   menu.getItem(0);
        menuItem.setChecked(true);
        mBottomNavigationView.setOnNavigationItemSelectedListener(bottomListener);

        setFragment(mMainFragment);

//        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
//        mSwipeRefreshLayout.setColorSchemeColors(this.getColor(R.color.colorPrimary));
    }
//
//    SwipeRefreshLayout.OnRefreshListener    mOnRefreshListener  =   new SwipeRefreshLayout.OnRefreshListener() {
//        @Override
//        public void onRefresh() {
//            mAdapter    =   new ListAdapter(MainActivity.this,   new ArrayList<Video>());
//            getList();
//        }
//    };
//
//    public interface OnGetDataListener {
//        void onStart();
//        void onSuccess(ArrayList data);
//        void onFailed(DatabaseError databaseError);
//    }
//
//    public void getList(){
//
//        final OnGetDataListener   onGetDataListener =   new OnGetDataListener() {
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onSuccess(ArrayList data) {
//                mArrayList  =   data;
//                // use this setting to improve performance if you know that changes
//                // in content do not change the layout size of the RecyclerView
//                mRecyclerView.setHasFixedSize(true);
//                // use a linear layout manager
//                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//                mAdapter = new ListAdapter(MainActivity.this,    mArrayList);
//                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
//                mRecyclerView.setAdapter(mAdapter);
//                if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                    mProgressDialog.dismiss();
//                }
//                mSwipeRefreshLayout.setRefreshing(false);
//            }
//
//            @Override
//            public void onFailed(DatabaseError databaseError) {
//                Log.e(TAG, databaseError.getMessage());
//                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        onGetDataListener.onStart();
//        mArrayList.clear();
//        mDatabaseReference.child("Main_List").orderByChild("rating").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    mArrayList.add(child.getValue(Video.class));
//                }
//                Collections.reverse(mArrayList);
//                onGetDataListener.onSuccess(mArrayList);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                onGetDataListener.onFailed(databaseError);
//            }
//        });
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        mProgressDialog.setMessage("LOADING...");
//        mProgressDialog.setIndeterminate(true);
//        mProgressDialog.show();
//        getList();
//    }

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
                    break;
                case R.id.bn_fav:
                    setFragment(mFavouritesFragment);
                    break;
                case R.id.bn_history:
                    setFragment(mHistoryFragment);
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
            // Handle navigation view item clicks here.
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


    public abstract class ReverseFirebaseListAdapter<T> extends FirebaseListAdapter<T> {

        public ReverseFirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, Query ref) {
            super(activity, modelClass, modelLayout, ref);
        }

        public ReverseFirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, DatabaseReference ref) {
            super(activity, modelClass, modelLayout, ref);
        }

        @Override
        public T getItem(int position) {
            return super.getItem(getCount() - (position + 1));
        }
    }
}
