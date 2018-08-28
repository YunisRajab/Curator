package com.yunisrajab.curator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;

public class DatabaseManager {

    String TAG = "Curator DbManager";
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference mDatabaseReference;
    Context mContext;
    int counter =   0;
    String  videoID;

    DatabaseManager (Context    context)  {
        mContext    =   context;
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        userLocalData   =   new UserLocalData(mContext);
        mUser   =   userLocalData.getLoggedUser();
    }

    public void updateMain ()  {
        mDatabaseReference.child("Main_List").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot   videoSnap:  dataSnapshot.getChildren()) {

                    mDatabaseReference.child("Users").child("White_List")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (final DataSnapshot   userSnap:  dataSnapshot.getChildren()) {

                                if (userSnap.hasChild(videoSnap.getKey()))    {
                                    if (videoID  !=  videoSnap.getKey())  {
                                        videoID  =   videoSnap.getKey();
                                        counter =   0;
                                    }
                                    counter++;
                                    mDatabaseReference.child("Main_List").child(videoSnap.getKey())
                                                .child("rating").setValue(counter);
                                    mDatabaseReference.child("Users").child(userSnap.getKey()).child(videoSnap.getKey())
                                                .child("rating").setValue(counter);
                                }
//                                TODO check if the below code functions as intended
                                mDatabaseReference.child("Users").child(mUser.uid).child("Black_List")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                mDatabaseReference.child("Users").child(mUser.uid)
                                                        .child("Black_List").child(videoID).child("rating")
                                                        .setValue(counter);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
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
                    final URL embededURL = new URL("http://www.youtube.com/oembed?url=" +
                            url + "&format=json");
                    final String  title   =
                            new JSONObject(IOUtils.toString(embededURL))
                                    .getString("title");

                    final Video video =   new Video(title,url,rating);
                    mDatabaseReference.child("Users").child(mUser.uid).child("Black_List")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(videoID)) {
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

                    updateMain();
                }
                catch(Exception e)
                {
                    Log.e(TAG,"upload "+e);
                }
            }
        }).start();
    }
}
