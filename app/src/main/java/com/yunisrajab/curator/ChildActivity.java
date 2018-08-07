package com.yunisrajab.curator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChildActivity  extends Activity {

    FTPActivity ftpClient;
    String FILENAME = "MediaList.txt";
    String PACKAGE_NAME;
    String SOURCEFILE_PATH;
    ArrayList<String> mArrayList;
    ListView mListView;
    String TAG = "Curator child";
    String url;
    String  USER_DIRECTORY;
    DatabaseReference   mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        SOURCEFILE_PATH = Environment.getExternalStorageDirectory()+"/Android/data/"+PACKAGE_NAME+"/";

        ftpClient = new FTPActivity();

        mListView = findViewById(R.id.listView);
        mListView.setOnItemClickListener(itemListener);

        mArrayList = new ArrayList<>();

        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Test");

        FirebaseListAdapter<String> firebaseListAdapter =   new FirebaseListAdapter<String>(
                this,
                String.class,
                android.R.layout.simple_list_item_1,
                mDatabaseReference
        ) {
            @Override
            protected void populateView(View v, String model, int position) {

                TextView    textView    =   v.findViewById(android.R.id.text1);
                textView.setText(model);
            }
        };

        mListView.setAdapter(firebaseListAdapter);

//        retrieveFile();

//        TODO mListView click

    }

    private final AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Object obj = mListView.getItemAtPosition(i);
            url = obj.toString();
            url = url.substring(url.lastIndexOf("link=")+1, url.lastIndexOf("}")).trim();
            launchVideo();
        }
    };

    private void retrieveFile()  {
        Thread t;
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                ftpClient.ftpConnect("192.168.0.17", "Yunis Rajab", "qwerty27", 21);
                ftpClient.ftpDownload("/"+USER_DIRECTORY+"/"+FILENAME, SOURCEFILE_PATH+FILENAME);
                ftpClient.ftpDisconnect();
            }
        });
        try {
            t.start();
            t.join();
        }   catch (Exception e) {
            e.printStackTrace();
        }
        readFile();
    }

    private void readFile() {

        try {
            File txtFile = new File(SOURCEFILE_PATH+FILENAME);
            if (!txtFile.exists()) {
                Log.e(TAG, "File doesn't exist");
            }   else {
                FileReader fileReader = new FileReader(txtFile);
                BufferedReader reader = new BufferedReader(fileReader);
                String str;
                List<Map<String, String>> data = new ArrayList<>();
                while ((str = reader.readLine())!=null){
                    mArrayList.add(str);
                }
                for (int i=0; i<mArrayList.size();i++)  {
                    Map<String, String> datum = new HashMap<>(2);
                    datum.put("title", mArrayList.get(i));
                    if (i<(mArrayList.size()-1)){
                        i++;
                        datum.put("link", mArrayList.get(i));
                    }
                    data.add(datum);
                }
                SimpleAdapter adapter = new SimpleAdapter(this, data,
                        android.R.layout.simple_list_item_2,
                        new String[] {"title", "link"},
                        new int[] {android.R.id.text1,
                                android.R.id.text2});
                mListView.setAdapter(adapter);
                reader.close();
                fileReader.close();
            }
        } catch (IOException e) {
            Log.e(TAG,"Exception: "+e);
            e.printStackTrace();
        }
    }

    private void launchVideo()  {
        Intent intent;
        if (!url.contains("youtube")&&!url.contains("youtu.be"))  {
            intent = new Intent(ChildActivity.this , VideoActivity.class);
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
