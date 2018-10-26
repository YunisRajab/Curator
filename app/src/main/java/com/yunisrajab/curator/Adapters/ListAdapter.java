package com.yunisrajab.curator.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private ArrayList<Video> mArrayList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private UserLocalData mUserLocalData;
    private User mUser;
    private DatabaseManager mDatabaseManager;
    private DatabaseReference   mDatabaseReference;
    String  TAG =   "curator adapter";
    String  type;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListAdapter(Context context, ArrayList<Video> arrayList, String  type) {
        mArrayList = arrayList;
        mInflater = LayoutInflater.from(context);
        mContext    =   context;
        mUserLocalData  =   new UserLocalData(context);
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseManager    =   new DatabaseManager(mContext);
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        this.type   =   type;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final  ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Video   video   =   mArrayList.get(position);
        holder.title.setText(video.getTitle());
        holder.rating.setText(String.valueOf(video.getRating()));

        final HashMap<String, Boolean> votes =   mUser.getVotes();
        if ((votes    !=  null)  &&  (votes.containsKey(video.getID())))  {
            holder.upbox.setChecked(votes.get(video.getID()));
            holder.downbox.setChecked(!votes.get(video.getID()));
        }

        if (mUser.getFavs().contains(video.getID()))   {
            holder.favbox.setChecked(true);
        }

        CompoundButton.OnCheckedChangeListener  listener    =   new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed()) {
                    if (!b) {
                        votes.remove(video.getID());
                        if (compoundButton.getId()  ==  R.id.upvote)    {
                            video.setRating(video.getRating()-1);
                            mDatabaseManager.updateVote(video.getID(), -1);
                        }   else    {
                            video.setRating(video.getRating()+1);
                            mDatabaseManager.updateVote(video.getID(), 1);
                        }
                    }
                            else {
                                if (compoundButton.getId()  ==  R.id.upvote)    {
                                    votes.put(video.getID(),    true);
                                    if (holder.downbox.isChecked()) {
                                        video.setRating(video.getRating()+2);
                                        mDatabaseManager.updateVote(video.getID(), 2);
                                    }   else    {
                                        video.setRating(video.getRating()+1);
                                        mDatabaseManager.updateVote(video.getID(), 1);
                                    }
                                    holder.downbox.setChecked(false);
                                }
                                if (compoundButton.getId()  ==  R.id.downvote)  {
                                    votes.put(video.getID(),    false);
                                    if (holder.upbox.isChecked()) {
                                        video.setRating(video.getRating()-2);
                                        mDatabaseManager.updateVote(video.getID(), -2);
                                    }   else {
                                        video.setRating(video.getRating()-1);
                                        mDatabaseManager.updateVote(video.getID(), -1);
                                    }
                                    holder.upbox.setChecked(false);
                                }
                            }
                    mUser.setVotes(votes);
                    mUserLocalData.storeUserData(mUser);
                    mDatabaseReference.child("Users").child(mUser.uid).child("Votes").setValue(votes);
                    notifyDataSetChanged();
                }
            }
        };

        holder.upbox.setOnCheckedChangeListener(listener);
        holder.downbox.setOnCheckedChangeListener(listener);
        holder.favbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed()) {
                    Set<String> strings =   mUser.getFavs();
                    HashMap<String, Boolean> votes =   mUser.getVotes();
                    if (b)  {
                        strings.add(video.getID());
                        mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                                .child(video.getID()).setValue(video);
                        mDatabaseReference.child("In_Progress").child(video.getID()).child(mUser.uid).setValue(true);
                        mDatabaseManager.updateVoteInProgress(video.getID(),    1);
                        if (votes.containsKey(video.getID()))    {
                            if (!votes.get(video.getID()))   {
                                video.setRating(video.getRating()+2);
                                mDatabaseManager.updateVote(video.getID(), 2);
                            }
                        }   else {
                            video.setRating(video.getRating()+1);
                            mDatabaseManager.updateVote(video.getID(), 1);
                        }
                        votes.put(video.getID(),    true);
                    }   else {
                        strings.remove(video.getID());
                        mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                                .child(video.getID()).removeValue();
                        mDatabaseReference.child("In_Progress").child(video.getID()).child(mUser.uid).setValue(false);
                        mDatabaseManager.updateVoteInProgress(video.getID(),    -1);
                    }
                    Intent  intent  =   new Intent("update");
                    Bundle  bundle  =   new Bundle();
                    HashMap<Boolean,    Video>  map =   new HashMap<>();
                    map.put(b,  video);
                    bundle.putSerializable("map", map);
                    intent.putExtras(bundle);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                    mDatabaseReference.child("Users").child(mUser.uid).child("Votes").setValue(votes);
                    mUser.setFavs(strings);
                    mUser.setVotes(votes);
                    mUserLocalData.storeUserData(mUser);
                    notifyDataSetChanged();
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView title,  rating;
        public CheckBox upbox,  downbox,    favbox;
        public ViewHolder(View v) {
            super(v);
            title   =   v.findViewById(R.id.titleView);
            rating  =   v.findViewById(R.id.ratingView);
            upbox   =   v.findViewById(R.id.upvote);
            downbox   =   v.findViewById(R.id.downvote);
            favbox   =   v.findViewById(R.id.favbox);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Video getItem(int id) {
        return mArrayList.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
