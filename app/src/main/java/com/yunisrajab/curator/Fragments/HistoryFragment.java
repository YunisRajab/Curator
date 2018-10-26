package com.yunisrajab.curator.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yunisrajab.curator.Adapters.HistoryAdapter;
import com.yunisrajab.curator.History;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    RecyclerView mRecyclerView;
    String TAG = "Curator list";
    View    rootView;
    ArrayList<History>  mArrayList;
    HistoryAdapter  mAdapter;
    SwipeRefreshLayout  mSwipeRefreshLayout;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        mArrayList  =   (ArrayList<History>)  args.getSerializable("arraylist");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"History layout");
        rootView    =   inflater.inflate(R.layout.fragment_history,    container,  false);

        if (mArrayList  ==  null)   mArrayList  =   new ArrayList<>();
        mRecyclerView   =   (RecyclerView)  rootView.findViewById(R.id.historyList);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new HistoryAdapter(getActivity(),    mArrayList);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout =   (SwipeRefreshLayout)    rootView.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHistory();
            }
        });

        return rootView;
    }

    public void getHistory(){

        final OnGetDataListener onGetDataListener   =   new OnGetDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(HashMap<String,HashMap<String,String>> map) {
                for (String key: map.keySet()) {
                    HashMap<String,String>  sMap    =   map.get(key);
                    History history   =   new History(sMap.get("title"),
                            sMap.get("id"), sMap.get("time"),   sMap.get("key"));
                    mArrayList.add(history);
                }
                Collections.sort(mArrayList,    new Comparator<History>() {
                    @Override
                    public int compare(History history, History t1) {
                        return history.getTime().compareTo(t1.getTime());
                    }
                });
                Collections.reverse(mArrayList);

                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                mRecyclerView.setHasFixedSize(true);
                // use a linear layout manager
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mAdapter = new HistoryAdapter(getActivity(),    mArrayList);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
                mRecyclerView.setAdapter(mAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        };

        onGetDataListener.onStart();
        UserLocalData userLocalData    =   new UserLocalData(getActivity());
        User user  =   userLocalData.getLoggedUser();
        mArrayList.clear();
        FirebaseDatabase.getInstance().getReference().child("Users").child(user.uid).child("History").orderByChild("time")
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
}
