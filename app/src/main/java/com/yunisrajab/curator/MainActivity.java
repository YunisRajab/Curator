package com.yunisrajab.curator;

import android.app.Activity;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    Button addButton, logoutButton;
    EditText urlText;
    String TAG = "Curator";
    Button  loginButton,    registerButton;
    TextView    emailText, passText;
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference   mDatabaseReference;
    ListView    mListView;
    boolean doubleBackPressedOnce   =   false;
    DatabaseManager mDatabaseManager;
    String videoID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.i("Curator","Main layout");

        userLocalData   =   new UserLocalData(this);
        mUser   =   userLocalData.getLoggedUser();

//        TODO check if user logged in with mAuth
        if (!userLocalData.getUserLoggedIn())    {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            Log.i("Curator", "Login layout");
            finish();
        }   else FirebaseAuth.getInstance().signInWithEmailAndPassword(mUser.email, mUser.password);

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

        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();

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

        mListView   =   (ListView)  findViewById(R.id.cloudList);

        ReverseFirebaseListAdapter<Video> adapter =   new ReverseFirebaseListAdapter<Video>(
                this,   Video.class,
                R.layout.list_item,    mDatabaseReference.child("Main_List").orderByChild("rating")
        ) {
            @Override
            protected void populateView(View v, Video model, int position) {
                ((TextView)v.findViewById(R.id.titleView)).setText(model.getTitle());
                ((TextView)v.findViewById(R.id.urlView)).setText(model.getUrl());
                ((TextView)v.findViewById(R.id.ratingView)).setText(String.valueOf(model.getRating()));
                CheckBox    upbox    =   (CheckBox)v.findViewById(R.id.upvote);
                CheckBox    downbox    =   (CheckBox)v.findViewById(R.id.downvote);
                if ((model.getVote() !=  null)&&(model.getVote().containsKey(mUser.uid)))   {
                    upbox.setChecked(model.getVote().get(mUser.uid));
                    downbox.setChecked(!model.getVote().get(mUser.uid));
                }

                videoID =   model.getUrl();
                if (videoID.contains("=")) videoID = videoID.substring(videoID.lastIndexOf("=") + 1);
                else videoID = videoID.substring(videoID.lastIndexOf("/") + 1);
//                TODO  BUG:    changing votes only affects the last video
//                TODO may be caused by ordering

                CompoundButton.OnCheckedChangeListener  listener    =   new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (compoundButton.isPressed())  {
//                            Log.e(TAG,  videoID);
                            if (!b) mDatabaseManager.updateVote(videoID, 0);
                            else {
                                if (compoundButton.getId()  ==  R.id.upvote)    {
                                    mDatabaseManager.updateVote(videoID, 1);
                                }
                                if (compoundButton.getId()  ==  R.id.downvote)  {
                                    mDatabaseManager.updateVote(videoID, -1);
                                }
                            }

                        }
                    }
                };
                upbox.setOnCheckedChangeListener(listener);
                downbox.setOnCheckedChangeListener(listener);

            }
        };
        mListView.setAdapter(adapter);

        mListView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });

        mDatabaseReference.child("Main_List").addChildEventListener(mChildEventListener);
    }

    private ChildEventListener  mChildEventListener =   new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.e(TAG,  "added "+dataSnapshot.getKey());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.e(TAG,  "changed "+dataSnapshot.getKey());
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Log.e(TAG,  "removed "+dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

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
