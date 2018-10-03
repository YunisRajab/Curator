package com.yunisrajab.curator.Fragments;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.Adapters.ListAdapter;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    String TAG = "Curator";
    DatabaseReference mDatabaseReference;
    RecyclerView    mRecyclerView;
    ListAdapter mAdapter;
    DatabaseManager mDatabaseManager;
    ArrayList<Video>    mArrayList;
    ProgressDialog  mProgressDialog;
    SwipeRefreshLayout  mSwipeRefreshLayout;
    View    rootView;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgressDialog.setMessage("LOADING...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        getList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i("Curator","Main layout");
        rootView    =   inflater.inflate(R.layout.fragment_main,    container,  false);

        mDatabaseManager    =   new DatabaseManager(getActivity());
        mArrayList  =   new ArrayList<>();
        mProgressDialog =   new ProgressDialog(getActivity());
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        mRecyclerView   =   (RecyclerView)  rootView.findViewById(R.id.cloudListR);
        mSwipeRefreshLayout =   (SwipeRefreshLayout)    rootView.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getList();
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));

        return rootView;
    }

    public void getList(){

        final OnGetDataListener onGetDataListener =   new OnGetDataListener() {
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
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mAdapter = new ListAdapter(getActivity(),    mArrayList);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
                mRecyclerView.setAdapter(mAdapter);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        onGetDataListener.onStart();
        mArrayList.clear();
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
}
