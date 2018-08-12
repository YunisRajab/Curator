package com.yunisrajab.curator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends Activity {

    ListView mListView;
    String TAG = "Curator list";
    String url;
    DatabaseReference mDatabaseReference;
    User    mUser;
    UserLocalData   mUserLocalData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        mListView   =   (ListView)  findViewById(R.id.mainList);
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
            finish();

        }   else    {
            intent = new Intent(getApplicationContext() , YouTubeActivity.class);
            intent.putExtra("url",url);
            startActivity(intent);
            Log.i(TAG,"YouTube layout");
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        Intent intentMain = new Intent(ListActivity.this ,
                MainActivity.class);
        ListActivity.this.startActivity(intentMain);
        Log.i("Curator: ","Main layout");
        finish();
    }
}

