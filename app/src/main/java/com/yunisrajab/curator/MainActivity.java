package com.yunisrajab.curator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONObject;
import org.mortbay.jetty.Main;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    Button addButton, logoutButton;
    EditText urlText;
    String TAG = "Curator";
    Button  loginButton,    registerButton;
    TextView    emailText, passText;
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference   mDatabaseReference;
    RecyclerView    mRecyclerView;
    ListAdapter    mAdapter;
    boolean doubleBackPressedOnce   =   false;
    DatabaseManager mDatabaseManager;
    ArrayList<Video>    mArrayList;
    ProgressDialog  mProgressDialog;

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
        mArrayList  =   new ArrayList<>();
        mProgressDialog =   new ProgressDialog(this);
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        mRecyclerView   =   (RecyclerView)  findViewById(R.id.cloudListR);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    mDatabaseManager.upload(sharedText,1);
                    getList();
                }
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Cloud");
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView    bottomNavigationView    =   findViewById(R.id.bottomNavigation);
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        Menu menu    =   bottomNavigationView.getMenu();
        MenuItem    menuItem    =   menu.getItem(0);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(mItemSelectedListener);

//        TODO check what this does
        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mUser.uid)) {
                    Toast.makeText(getApplicationContext(),"Welcome!",Toast.LENGTH_SHORT).show();
                    mDatabaseReference.child("Main_List").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mDatabaseReference.child("Users").child(mUser.uid).child("White_List").setValue(dataSnapshot.getValue());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public interface OnGetDataListener {
        void onStart();
        void onSuccess(ArrayList data);
        void onFailed(DatabaseError databaseError);
    }

    public void getList(){
        mProgressDialog.setMessage("LOADING...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        final OnGetDataListener   onGetDataListener =   new OnGetDataListener() {
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
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mAdapter = new ListAdapter(MainActivity.this,    mArrayList);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
                mRecyclerView.setAdapter(mAdapter);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        onGetDataListener.onStart();
        mDatabaseReference.child("Main_List").orderByChild("rating").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    mArrayList.add(child.getValue(Video.class));
                }
                Collections.reverse(mArrayList);
                onGetDataListener.onSuccess(mArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onGetDataListener.onFailed(databaseError);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getList();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener   mItemSelectedListener   =   new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent  intent;
            switch (item.getItemId())   {
                case R.id.bn_cloud:
                    break;
                case R.id.bn_fav:
                    intent = new Intent(MainActivity.this , ListActivity.class);
                    MainActivity.this.startActivity(intent);
                    Log.i(TAG,"Fave layout");
                    break;
                case R.id.bn_history:
                    intent = new Intent(MainActivity.this , HistoryActivity.class);
                    MainActivity.this.startActivity(intent);
                    Log.i(TAG,"History layout");
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
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

    private void setFragment(Fragment   fragment)  {
        FragmentTransaction fragmentTransaction =   getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame,  fragment);
        fragmentTransaction.commit();

    }

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
