package com.yunisrajab.curator;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;


public class MainActivity extends AppCompatActivity{


    Button addButton, submitButton, childButton, logoutButton;
    EditText nameText, urlText;
    String TAG = "Curator";
    String FILENAME = "MediaList.txt";
    String PACKAGE_NAME;
    ArrayList<String> mArray;
    String SOURCEFILE_PATH;
    String USER_DIRECTORY;
    VideoView mVideoView;
    MediaController vidControl;
    private FTPActivity ftpClient = null;
    UserLocalData   useLocalData;
//    DatabaseReference   mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        SOURCEFILE_PATH = Environment.getExternalStorageDirectory()+"/Android/data/"+PACKAGE_NAME+"/";

        vidControl = new MediaController(this);
        mVideoView = (VideoView)    findViewById(R.id.myVideo);
        addButton = (Button)    findViewById(R.id.addButton);
        submitButton = (Button)    findViewById(R.id.submitButton);
        childButton = (Button) findViewById(R.id.childButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);

        nameText = (EditText)    findViewById(R.id.nameText);
        urlText = (EditText)    findViewById(R.id.urlText);

        nameText.addTextChangedListener(textWatcher);
        urlText.addTextChangedListener(textWatcher);

        addButton.setEnabled(false);
        submitButton.setEnabled(false);

        childButton.setOnClickListener(childListener);
        addButton.setOnClickListener(addListener);
        submitButton.setOnClickListener(addListener);
        logoutButton.setOnClickListener(logoutListener);

        ftpClient = new FTPActivity();

        useLocalData   =   new UserLocalData(this);

//        TODO share link into app instead of manual cop+paste
//        TODO add playlists instead of individual videos
//        TODO only take urls and extract titles(and thumbnails)
        if (!isReadStorageAllowed())    requestStoragePermission();

        if (!useLocalData.getUserLoggedIn())    {
            Intent intentMain = new Intent(getApplicationContext() , LoginActivity.class);
            getApplicationContext().startActivity(intentMain);
            Log.i(TAG,"Login layout");
            finish();
        }
//        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();

//        retrieveFile();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String  name    =   nameText.getText().toString().trim();
            String  url =   urlText.getText().toString().trim();
            addButton.setEnabled(!name.isEmpty() && !url.isEmpty());
        }
        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private final View.OnClickListener addListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if  (view==addButton) {

                if (mArray==null)   {
                    mArray = new ArrayList<>();
                }
                mArray.add(nameText.getText().toString()+"\n");
                mArray.add(urlText.getText().toString().trim()+"\n");
                nameText.setText("");
                urlText.setText("");
                addButton.setEnabled(false);
                submitButton.setEnabled(true);

            }   else    if (view==submitButton) {
                createFile();
                new Thread(new Runnable() {
                    public void run() {
                        // host – your FTP address
                        // username & password – for your secured login
                        // 21 default gateway for FTP
                        ftpClient.ftpConnect("192.168.0.17", "Yunis Rajab", "qwerty27", 21);
                        ftpClient.ftpUpload(SOURCEFILE_PATH+FILENAME, FILENAME,
                                "", getApplicationContext());
                        ftpClient.ftpDisconnect();
                    }
                }).start();
                submitButton.setEnabled(false);
            }
        }
    };

    private final View.OnClickListener childListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intentMain = new Intent(getApplicationContext() , ChildActivity.class);
            getApplicationContext().startActivity(intentMain);
            Log.i(TAG,"Child layout");
            finish();

        }
    };

    private final View.OnClickListener  logoutListener  =   new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            useLocalData.setUserLoggedIn(false);
            Intent intent = new Intent(getApplicationContext() , LoginActivity.class);
            getApplicationContext().startActivity(intent);
            Log.i(TAG,"Login layout");
            finish();
        }
    };

    public void createFile() {
//            TODO  don't overwrite if file exists (only append)
        try {
            File root = new File(SOURCEFILE_PATH);
            if (!root.exists()) {
                boolean success = root.mkdir();
                Log.d(TAG, "Create Directory: "+success);
            }
            File txtFile = new File(root, FILENAME);
            FileWriter writer = new FileWriter(txtFile);
            if (mArray!=null)   {
                for (int i=0;   i<mArray.size();  i++)    {
                    writer.append(mArray.get(i));
                }
            }
            writer.flush();
            writer.close();
            mArray = null;
            Toast.makeText(this, "Saved : " + txtFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG,"Exception: "+e);
            e.printStackTrace();
        }
    }

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
//        readFile();
    }

    //Requesting permission
    private void requestStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},23);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == 23){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                Toast.makeText(this,"Permission granted now you can read the storage",Toast.LENGTH_LONG).show();
            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }
}
