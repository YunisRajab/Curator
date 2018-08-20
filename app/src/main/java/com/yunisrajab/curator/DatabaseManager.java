package com.yunisrajab.curator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;

public class DatabaseManager {

    String TAG = "Curator DbManager";
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference mDatabaseReference;
    boolean success;
    Context mContext;

    DatabaseManager (Context    context)  {
        mContext    =   context;
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        userLocalData   =   new UserLocalData(mContext);
        mUser   =   userLocalData.getLoggedUser();
    }

    public void upload    (String   url,    int rate)  {
        final String   youtubeUrl   =   url;
        final String  videoID;
        final int   rating  =   rate;
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
                            youtubeUrl + "&format=json");
                    final String  title   =
                            new JSONObject(IOUtils.toString(embededURL))
                                    .getString("title");

                    final Video video =   new Video(title,youtubeUrl,rating);

                    mDatabaseReference.child("Users").child(mUser.uid).child(videoID).setValue(video).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())    {
                                Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                                success =   true;
                            }   else {
                                Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                                success =   false;
                            }
                        }
                    });
                }
                catch(Exception e)
                {
                    Log.e(TAG,"upload "+e);
                }
            }
        }).start();
//        return success;
    }
}
