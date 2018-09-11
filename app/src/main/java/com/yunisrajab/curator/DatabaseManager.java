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

public class DatabaseManager {

    String TAG = "Curator DbManager";
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference mDatabaseReference;
    Context mContext;

    DatabaseManager (Context    context)  {
        mContext    =   context;
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        userLocalData   =   new UserLocalData(mContext);
        mUser   =   userLocalData.getLoggedUser();
    }

    public void updateVote(DatabaseReference postRef, final int vote) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                Video video = mutableData.getValue(Video.class);

                if (video == null) return Transaction.success(mutableData);

                HashMap<String, Boolean> v   =   video.getVote();

                if (v.containsKey(mUser.uid)) {
                    Boolean  savedVote   =   Boolean.valueOf(v.get(mUser.uid));
                    switch (vote)   {
                        case    0:
                            // remove vote from the post and remove self from votes
                            if (savedVote)  video.setRating(video.getRating()-1);
                            else video.setRating(video.getRating()+1);
                            v.remove(mUser.uid);
                            video.setVote(v);
                            break;
                        case    1:
                            if (!savedVote)  {
                                video.setRating(video.getRating()+2);
                                v.put(mUser.uid,true);
                                video.setVote(v);
                            }
                            break;
                        case    -1:
                            if (savedVote)  {
                                video.setRating(video.getRating()-2);
                                v.put(mUser.uid,false);
                                video.setVote(v);
                            }
                            break;
                    }
                } else {
                    // vote on the video and add self to votes
                    if (vote    ==  1)  v.put(mUser.uid, true);
                    else v.put(mUser.uid, false);
                    video.setVote(v);
                    video.setRating(video.getRating()+vote);

                }

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
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild(videoID)) {

                                                                mDatabaseReference.child("Main_List").runTransaction(new Transaction.Handler() {
                                                                    @NonNull
                                                                    @Override
                                                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                                                                        Video video = mutableData.getValue(Video.class);
                                                                        HashMap<String, Boolean> vote    =   video.getVote();
                                                                        vote.put(mUser.uid,true);
                                                                        video.setVote(vote);
                                                                        video.setRating(video.getRating()+1);
                                                                        mutableData.setValue(video);

                                                                        mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                                                                                .child(videoID).setValue(video).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful())    {
                                                                                    Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
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
                                                            }   else {
                                                                HashMap<String, Boolean> vote    =   new HashMap<>();
                                                                vote.put("DUMMY",true);
                                                                vote.put(mUser.uid,true);

                                                                final Video video =   new Video(title,url,rating,vote);
                                                                mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                                                                        .child(videoID).setValue(video).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful())    {
                                                                            Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                                                                            mDatabaseReference.child("Main_List").child(videoID).setValue(video);
                                                                        }   else {
                                                                            Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                                                                        }
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

    public void delete  (final Video   video,  final String    id) {

        mDatabaseReference.child("Users").child(mUser.uid).child("Black_List")
                .child(id).setValue(video).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())    {
                    Toast.makeText(mContext, "Moved to blacklist", Toast.LENGTH_SHORT).show();
                    mDatabaseReference.child("Users").child(mUser.uid).child("White_List")
                            .child(id).removeValue();
                }   else {
                    Toast.makeText(mContext, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    public void addToHistory()

}
