package com.yunisrajab.curator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gdata.data.youtube.VideoEntry;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;
import java.util.*;
import java.net.URL;
import org.json.JSONObject;

public class ChildActivity  extends Activity {

    TextView titleView, urlView, ratingView;
    ListView mListView;
    String TAG = "Curator child";
    String url;

    DatabaseReference   mDatabaseReference;
    User    mUser;
    UserLocalData   mUserLocalData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        mListView = findViewById(R.id.listView);
        mListView.setOnItemClickListener(itemListener);

        mUserLocalData  =   new UserLocalData(getApplicationContext());
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);

        FirebaseListAdapter<Video>  adapter =   new FirebaseListAdapter<Video>(
                this,   Video.class,
                android.R.layout.simple_list_item_1,    mDatabaseReference
        ) {
            @Override
            protected void populateView(View v, Video model, int position) {
                TextView    textView    =   v.findViewById(android.R.id.text1);
                textView.setText(model.getTitle());
            }
        };
        mListView.setAdapter(adapter);
    }

    private final AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Video   video = (Video) mListView.getItemAtPosition(i);
            url = video.getUrl();
            Log.e(TAG,""+url);
            launchVideo();
        }
    };

//    private void readFile() {
//
//        try {
//            File txtFile = new File(SOURCEFILE_PATH+FILENAME);
//            if (!txtFile.exists()) {
//                Log.e(TAG, "File doesn't exist");
//            }   else {
//                FileReader fileReader = new FileReader(txtFile);
//                BufferedReader reader = new BufferedReader(fileReader);
//                String str;
//                List<Map<String, String>> data = new ArrayList<>();
//                while ((str = reader.readLine())!=null){
//                    mArrayList.add(str);
//                }
//                for (int i=0; i<mArrayList.size();i++)  {
//                    Map<String, String> datum = new HashMap<>(2);
//                    datum.put("title", mArrayList.get(i));
//                    if (i<(mArrayList.size()-1)){
//                        i++;
//                        datum.put("link", mArrayList.get(i));
//                    }
//                    data.add(datum);
//                }
//                SimpleAdapter adapter = new SimpleAdapter(this, data,
//                        android.R.layout.simple_list_item_2,
//                        new String[] {"title", "link"},
//                        new int[] {android.R.id.text1,
//                                android.R.id.text2});
//                mListView.setAdapter(adapter);
//                reader.close();
//                fileReader.close();
//            }
//        } catch (IOException e) {
//            Log.e(TAG,"Exception: "+e);
//            e.printStackTrace();
//        }
//    }

    private void launchVideo()  {
        Intent intent;
        if (!url.contains("youtube")&&!url.contains("youtu.be"))  {
            intent = new Intent(ChildActivity.this , VideoActivity.class);
            intent.putExtra("url",url);
            startActivity(intent);
            Log.i(TAG,"Video layout");
            finish();

        }   else    {
            intent = new Intent(getApplicationContext() , YouTubeActivity.class);
            intent.putExtra("url",url);
            startActivity(intent);
            Log.i(TAG,"YouTube layout");
            finish();
            }

    }

    @Override
    public void onBackPressed() {
        Intent intentMain = new Intent(ChildActivity.this ,
                MainActivity.class);
        ChildActivity.this.startActivity(intentMain);
        Log.i("Curator: ","Main layout");
        finish();
    }
}
