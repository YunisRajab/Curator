package com.yunisrajab.curator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class DatabaseManager {

    String TAG = "Curator DbManager";
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference mDatabaseReference;
    Context mContext;

    public DatabaseManager (Context    context)  {
        mContext    =   context;
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        userLocalData   =   new UserLocalData(mContext);
        mUser   =   userLocalData.getLoggedUser();
    }

    public void updateVote(String   videoID, final int vote) {

        mDatabaseReference.child("Main_List").child(videoID).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                Video video = mutableData.getValue(Video.class);

                if (video == null) return Transaction.success(mutableData);

                video.setRating(video.getRating()+vote);

                // Set value and report transaction success
                mutableData.setValue(video);
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError,boolean b,DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void updateVoteInProgress(final  String   videoID, final int vote) {

        mDatabaseReference.child("In_Progress").child(videoID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("rating"))    {
                    mDatabaseReference.child("In_Progress").child(videoID).child("rating").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child("In_Progress").child(videoID).child("rating")
                .runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                int rating  =   0;
                try {
                    rating  =   mutableData.getValue(Integer.class);
                }   catch (Exception    e)  {
                    e.printStackTrace();
                }
                rating  =   rating  +   vote;

                if (rating == 0) mDatabaseReference.child("In_Progress").child(videoID).removeValue();

                // Set value and report transaction success
                mutableData.setValue(rating);
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError,boolean b,DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    public void upload    (final    String   url)  {
        final   boolean isVideo;
        if (url.contains("youtube")||url.contains("youtu.be"))  {
            isVideo =   true;
        }   else isVideo    =   false;

         new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream response = null;
                try {
                    final String  videoID;
                    final String    title;
                    if (isVideo)    {
                        if (url.contains("=")) videoID = url.substring(url.lastIndexOf("=") + 1);
                        else videoID = url.substring(url.lastIndexOf("/") + 1);
                        final URL embededURL = new URL("http://www.youtube.com/oembed?url="+url+"&format=json");
                        title   = new JSONObject(IOUtils.toString(embededURL)).getString("title");
                    }
                    else    {
                        URI uri = new URI(url);
                        String  temp = uri.getHost().replace(".","@");
                        if (temp.contains("www@"))   videoID =   temp.replace("www@", "");
                        else videoID    =   temp;
                        response = new URL(url).openStream();
                        Scanner scanner = new Scanner(response);
                        String responseBody = scanner.useDelimiter("\\A").next();
                        title   =   (responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>")));
                    }

                    mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(videoID)) {
                                        Toast.makeText(mContext, "Failed! Already in favourites", Toast.LENGTH_SHORT).show();
                                    }   else {
                                        mDatabaseReference.child("Main_List").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull   final DataSnapshot dataSnapshot) {

                                                mDatabaseReference.child("Main_List").runTransaction(new Transaction.Handler() {
                                                    @NonNull
                                                    @Override
                                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                                                        final Video   video;
                                                        if (dataSnapshot.hasChild(videoID)) {
                                                            video = mutableData.getValue(Video.class);
                                                            video.setRating(video.getRating()+1);
                                                            mutableData.setValue(video);
                                                        }   else {
                                                            video =   new Video(title,videoID,1);
                                                            mDatabaseReference.child("Main_List")
                                                                    .child(videoID).setValue(video);
                                                        }


                                                        mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                                                                .child(videoID).setValue(video).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())    {
                                                                    Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                                                                    Set<String> set =   mUser.getFavs();
                                                                    HashMap<String, Boolean>    votes =   mUser.getVotes();
                                                                    set.add(videoID);
                                                                    votes.put(videoID,    true);
                                                                    mDatabaseReference.child("Users").child(mUser.uid)
                                                                            .child("Votes").child(videoID).setValue(true);
                                                                    mUser.setFavs(set);
                                                                    mUser.setVotes(votes);
                                                                    userLocalData.storeUserData(mUser);

                                                                    Intent intent  =   new Intent("update");
                                                                    Bundle bundle  =   new Bundle();
                                                                    HashMap<Boolean,    Video>  map =   new HashMap<>();
                                                                    map.put(true,  video);
                                                                    bundle.putSerializable("map", map);
                                                                    intent.putExtras(bundle);
                                                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                                                }   else {
                                                                    Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                        return Transaction.success(mutableData);
                                                    }

                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                                        // Transaction completed
                                                        Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                                                    }
                                                });

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
                }
                catch(Exception e)
                {
                    Log.e(TAG,"upload "+e);
                }   finally {
                    try {
                        response.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void delete  (Video   video) {
        final String    id  =   video.getID();
        mDatabaseReference.child("Users").child(mUser.uid).child("White_List").child(id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())    {
                    Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
                    Set<String> strings =   mUser.getFavs();
                    strings.remove(id);
                    mUser.setFavs(strings);
                    userLocalData.storeUserData(mUser);
                }   else {
                    Toast.makeText(mContext, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void delete  (History   history) {
        final String    key  =   history.getKey();
        mDatabaseReference.child("Users").child(mUser.uid).child("History").child(key)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())    {
                    Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
                }   else {
                    Toast.makeText(mContext, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addWebsite  (String site,   String  host)   {
        mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                .child(host).setValue(site).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())    {
                    Toast.makeText(mContext, "Success!", Toast.LENGTH_SHORT).show();
                }   else Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void retrieveFavsAndVotes()   {
        mDatabaseReference.child("Users").child(mUser.uid).child("Votes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())   {
                            HashMap<String, Boolean> map =
                                    (HashMap<String, Boolean>)   dataSnapshot.getValue();
                            mUser.setVotes(map);
                            userLocalData.storeUserData(mUser);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String, HashMap<String,String>> map =
                                (HashMap<String, HashMap<String,String>>)   dataSnapshot.getValue();
                        Set<String> favs =   new HashSet<>();
                        for (String key: map.keySet()) {
                            favs.add(key);
                        }
                        mUser.setFavs(favs);
                        userLocalData.storeUserData(mUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void setSU() {
        mDatabaseReference.child("Super_Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mUser.uid))   {
                    mUser.setSU(true);
                }   else mUser.setSU(false);
                userLocalData.storeUserData(mUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}