package com.yunisrajab.curator.Fragments;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yunisrajab.curator.Adapters.FavouritesAdapter;
import com.yunisrajab.curator.Adapters.ListAdapter;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.DownloadImageTask;
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
public class FavouritesFragment extends Fragment {

    RecyclerView    mRecyclerView;
    String TAG = "Curator list";
    View    rootView;
    ArrayList<Video>    mArrayList;
    FavouritesAdapter   mAdapter;


    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG,"Fave layout");
        rootView    =   inflater.inflate(R.layout.fragment_favourites,    container,  false);

        mRecyclerView   =   (RecyclerView)  rootView.findViewById(R.id.favList);
        mArrayList  =   new ArrayList<>();
        getFavourites();

        return rootView;
    }

    public void getFavourites(){

        final OnGetDataListener   onGetDataListener   =   new OnGetDataListener() {
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
                mAdapter = new FavouritesAdapter(getActivity(),    mArrayList);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        };

        onGetDataListener.onStart();
        UserLocalData userLocalData    =   new UserLocalData(getActivity());
        User user  =   userLocalData.getLoggedUser();
        mArrayList.clear();
        FirebaseDatabase.getInstance().getReference().child("Users").child(user.uid).child("White_List").orderByChild("title")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            mArrayList.add(child.getValue(Video.class));
                        }
                        onGetDataListener.onSuccess(mArrayList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG,  databaseError.getDetails());
                    }
                });
    }
}
