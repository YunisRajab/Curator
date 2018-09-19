package com.yunisrajab.curator;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.database.Query;

import java.io.InputStream;

public class HistoryActivity    extends AppCompatActivity   implements NavigationView.OnNavigationItemSelectedListener {

    ListView mListView;
    String TAG = "Curator list";
    String url, videoID;
    DatabaseReference mDatabaseReference;
    User    mUser;
    UserLocalData   mUserLocalData;
    boolean doubleBackPressedOnce   =   false;
    AlertDialog.Builder builder;
    History mHistory;
    DatabaseManager mDatabaseManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mDatabaseManager    =   new DatabaseManager(this);
        mListView   =   (ListView)  findViewById(R.id.historyList);
        mUserLocalData  =   new UserLocalData(getApplicationContext());
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);

        ReverseFirebaseListAdapter<History> adapter =   new ReverseFirebaseListAdapter<History>(
                this,   History.class,
                R.layout.history_item,    mDatabaseReference.child("History").orderByChild("time")
        ) {
            @Override
            protected void populateView(View v, History model, int position) {
                ((TextView)v.findViewById(R.id.itemName)).setText(model.getTitle());
                ((TextView)v.findViewById(R.id.itemTime)).setText(model.getTime());
                String  uri = model.getId();
                new DownloadImageTask((ImageView) v.findViewById(R.id.thumbnail))
                        .execute("https://img.youtube.com/vi/"+uri+"/0.jpg");
            }
        };
        mListView.setAdapter(adapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                mHistory = (History) mListView.getItemAtPosition(pos);
                videoID =   mHistory.getId();
                builder.show();
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
        Menu menu    =   bottomNavigationView.getMenu();
        MenuItem    menuItem    =   menu.getItem(2);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(mItemSelectedListener);

        builder  = new AlertDialog.Builder(this,    R.style.AlertDialogStyle);
        builder.setMessage("Do you want to delete this?").setPositiveButton("Delete", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mDatabaseReference.child("History").child(videoID).removeValue();
                    break;
//                case DialogInterface.BUTTON_NEGATIVE:
//                    //No button clicked
//                    Toast.makeText(ListActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
//                    break;
            }
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

    private BottomNavigationView.OnNavigationItemSelectedListener   mItemSelectedListener   =   new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId())   {
                case R.id.bn_cloud:
                    intent = new Intent(HistoryActivity.this , MainActivity.class);
                    HistoryActivity.this.startActivity(intent);
                    Log.i(TAG,"Main layout");
                    break;
                case R.id.bn_fav:
                    intent = new Intent(HistoryActivity.this , ListActivity.class);
                    HistoryActivity.this.startActivity(intent);
                    Log.i(TAG,"Fav layout");
                    break;
                case R.id.bn_history:
                    break;
                case R.id.bn_child:
                    intent = new Intent(HistoryActivity.this , ChildActivity.class);
                    HistoryActivity.this.startActivity(intent);
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
                intent = new Intent(HistoryActivity.this , Settings.class);
                HistoryActivity.this.startActivity(intent);
                Log.i(TAG,"Settings layout");
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//            int height = displayMetrics.heightPixels;
//            int width = displayMetrics.widthPixels;
            bmImage.setImageBitmap(result);
//            bmImage.setAdjustViewBounds(true);
//            bmImage.setMaxHeight(height);
//            bmImage.setMaxWidth(width);
//            bmImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackPressedOnce)  {
                Intent intent = new Intent(HistoryActivity.this, CloseActivity.class);
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
