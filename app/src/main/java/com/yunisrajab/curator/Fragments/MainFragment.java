package com.yunisrajab.curator.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yunisrajab.curator.Adapters.TabAdapter;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.Adapters.ListAdapter;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainFragment extends Fragment {

    String TAG = "Curator";
    DatabaseReference mDatabaseReference;
    RecyclerView    mRecyclerView;
    ListAdapter mAdapter;
    DatabaseManager mDatabaseManager;
    ArrayList<Video>    mainList;
    ArrayList<Video>    curatedList;
    SwipeRefreshLayout  mSwipeRefreshLayout;
    View    rootView;
    ViewPager   mViewPager;
    TabAdapter  mTabAdapter;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        String  type    =   (String)    args.getString("type");
        if (type    ==  "main") {
            mainList  =   (ArrayList<Video>)  args.getSerializable("arraylist");
        }
        if (type    ==  "curated") {
            curatedList  =   (ArrayList<Video>)  args.getSerializable("arraylist");
        }

    }

    public ArrayList<Video> getArrayList() {
        return mainList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i("Curator","Main layout");
        rootView    =   inflater.inflate(R.layout.fragment_main,    container,  false);

        TabLayout   tabLayout   =   rootView.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Curated"));
        tabLayout.addTab(tabLayout.newTab().setText("Recently added"));
        tabLayout.addTab(tabLayout.newTab().setText("Community"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabTextColors(getActivity().getColor(R.color.colorPrimaryDark),
                getActivity().getColor(R.color.colorWhite));

        mViewPager  =   rootView.findViewById(R.id.viewPager);
        mTabAdapter =   new TabAdapter(getActivity(),   mainList,   curatedList);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
//        mDatabaseManager    =   new DatabaseManager(getActivity());
//        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
//
//        if (mArrayList  ==  null)   mArrayList  =   new ArrayList<>();
//        mRecyclerView   =   (RecyclerView)  rootView.findViewById(R.id.cloudListR);
//        mRecyclerView.setHasFixedSize(true);
//        // use a linear layout manager
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mAdapter = new ListAdapter(getActivity(),    mArrayList);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
//        mRecyclerView.setAdapter(mAdapter);
//
//        mSwipeRefreshLayout =   (SwipeRefreshLayout)    rootView.findViewById(R.id.swipeRefresh);
//        mSwipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getList();
//            }
//        });

        return rootView;
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
//                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//                mAdapter = new ListAdapter(getActivity(),    mArrayList);
//                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
//                mRecyclerView.setAdapter(mAdapter);
//                mSwipeRefreshLayout.setRefreshing(false);
//            }
//
//            @Override
//            public void onFailed(DatabaseError databaseError) {
//                Log.e(TAG, databaseError.getMessage());
//                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
}
