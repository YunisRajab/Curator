package com.yunisrajab.curator.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.yunisrajab.curator.Adapters.FavouritesAdapter;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.Video;
import java.util.ArrayList;

public class FavouritesFragment extends Fragment {

    RecyclerView    mRecyclerView;
    String TAG = "Curator list";
    View    rootView;
    ArrayList<Video>    mArrayList;
    FavouritesAdapter   mAdapter;


    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        mArrayList  =   (ArrayList<Video>)  args.getSerializable("arraylist");
    }

    public ArrayList<Video> getArrayList() {
        return mArrayList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG,"Fave layout");
        rootView    =   inflater.inflate(R.layout.fragment_favourites,    container,  false);

        if (mArrayList  ==  null)   mArrayList  =   new ArrayList<>();
        mRecyclerView   =   (RecyclerView)  rootView.findViewById(R.id.favList);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FavouritesAdapter(getActivity(),    mArrayList);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }
}
