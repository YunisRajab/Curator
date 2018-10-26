package com.yunisrajab.curator.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class TabAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater  mLayoutInflater;
    ArrayList<Video>    mainList;
    ArrayList<Video>    curatedList;
    String TAG = "Curator tabAdapter";
    DatabaseReference mDatabaseReference;
    RecyclerView    mRecyclerView;
    ListAdapter mAdapter;
    DatabaseManager mDatabaseManager;
    SwipeRefreshLayout  mSwipeRefreshLayout;

    public TabAdapter(Context context, ArrayList<Video> mainList,  ArrayList<Video>    curatedList)    {
        mContext    =   context;
        this.mainList   =   mainList;
        this.curatedList    =   curatedList;
    }
//TODO  going back to main after switching causes crash
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        mLayoutInflater =   (LayoutInflater)    mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View    view    =   mLayoutInflater.inflate(R.layout.tab,   container,  false);

        Log.e(TAG,  "position "+position);
        LinearLayout    tabLayout   =   (LinearLayout)  view.findViewById(R.id.tabLinearLayout);
        mRecyclerView    =   view.findViewById(R.id.cloudListR);
        mSwipeRefreshLayout  =   view.findViewById(R.id.swipeRefresh);
        mDatabaseManager    =   new DatabaseManager(mContext);
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();

        if (mainList  ==  null)   mainList  =   new ArrayList<>();
        if (curatedList  ==  null)   curatedList  =   new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        switch (position)   {
            case    0:
                mAdapter = new ListAdapter(mContext,    curatedList,   "curated");
                break;
            case    1:
                mAdapter = new ListAdapter(mContext,    curatedList,   "curated");
                break;
            case    2:
                mAdapter = new ListAdapter(mContext,    mainList,    "main");
                break;
        }
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setColorSchemeColors(mContext.getColor(R.color.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                getList();
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

//    public void getList(){
//
//        final OnGetDataListener onGetDataListener =   new OnGetDataListener() {
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onSuccess(HashMap<String,HashMap<String,String>> map) {
//                for (String key: map.keySet()) {
//                    HashMap<String,String>  sMap    =   map.get(key);
//                    Video   video   =   new Video(sMap.get("title"),
//                            sMap.get("id"), Integer.valueOf(String.valueOf(sMap.get("rating"))));
//                    mArrayList.add(video);
//                }
//                Collections.sort(mArrayList,    new Comparator<Video>() {
//                    @Override
//                    public int compare(Video video, Video t1) {
//                        return video.getRating()-t1.getRating();
//                    }
//                });
//                Collections.reverse(mArrayList);
//
//                // use this setting to improve performance if you know that changes
//                // in content do not change the layout size of the RecyclerView
//                mRecyclerView.setHasFixedSize(true);
//                // use a linear layout manager
//                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//                mAdapter = new ListAdapter(mContext,    mArrayList);
//                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
//                mRecyclerView.setAdapter(mAdapter);
//                mSwipeRefreshLayout.setRefreshing(false);
//            }
//
//            @Override
//            public void onFailed(DatabaseError databaseError) {
//                Log.e(TAG, databaseError.getMessage());
//                Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        onGetDataListener.onStart();
//        mArrayList.clear();
//        mDatabaseReference.child("Main_List").orderByChild("rating").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                HashMap<String,HashMap<String,String>>   map =
//                        (HashMap<String, HashMap<String, String>>)   dataSnapshot.getValue();
//                onGetDataListener.onSuccess(map);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                onGetDataListener.onFailed(databaseError);
//            }
//        });
//    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view    ==  o);
    }
}
