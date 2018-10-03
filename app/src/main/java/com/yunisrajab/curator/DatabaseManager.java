package com.yunisrajab.curator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.net.URL;
import java.util.HashMap;
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

    public void upload    (final    String   url,    final   int rating)  {
        final String  videoID;
        if (url.contains("=")) {
            videoID = url.substring(url.lastIndexOf("=") + 1);
        }   else {
            videoID = url.substring(url.lastIndexOf("/") + 1);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final URL embededURL = new URL("http://www.youtube.com/oembed?url="+url+"&format=json");
                    final String  title   = new JSONObject(IOUtils.toString(embededURL)).getString("title");

                    mDatabaseReference.child("Users").child(mUser.uid).child("Black_List")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(videoID))    {
                                Toast.makeText(mContext, "Failed! Video on black list", Toast.LENGTH_SHORT).show();
                            }   else    {
                                mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild(videoID)) {
                                                    Toast.makeText(mContext, "Failed! Video on white list", Toast.LENGTH_SHORT).show();
                                                }   else {
                                                    mDatabaseReference.child("Main_List").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull   final DataSnapshot dataSnapshot) {

                                                            mDatabaseReference.child("Main_List").runTransaction(new Transaction.Handler() {
                                                                @NonNull
                                                                @Override
                                                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                                                                    Video   video;
                                                                    if (dataSnapshot.hasChild(videoID)) {
                                                                        video = mutableData.getValue(Video.class);
                                                                        video.setRating(video.getRating()+1);
                                                                        mutableData.setValue(video);
                                                                    }   else {
                                                                        video =   new Video(title,videoID,rating);
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
                                                                                HashMap<String, Boolean>    map =   mUser.getVotes();
                                                                                set.add(videoID);
                                                                                map.put(videoID,    true);
                                                                                mUser.setFavs(set);
                                                                                mUser.setVotes(map);
                                                                                userLocalData.storeUserData(mUser);
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
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                catch(Exception e)
                {
                    Log.e(TAG,"upload "+e);
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
        mDatabaseReference.child("Users").child(mUser.uid).child("Websites")
                .child(host).setValue(site).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())    {
                    Toast.makeText(mContext, "Success!", Toast.LENGTH_SHORT).show();
                }   else Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //                TODO save favourites and votes on server to be retrieved on re-login
    public void backup()    {
        mUser.getFavs();
        mUser.getVotes();
    }

    public void retrieve()  {
        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mUser.uid))   {
//                    get favs and votes and save them user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}