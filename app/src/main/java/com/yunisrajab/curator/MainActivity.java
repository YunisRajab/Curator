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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity{


    Button addButton, childButton, logoutButton, listButton;
    EditText ratingText, urlText;
    String TAG = "Curator";
    String FILENAME = "MediaList.txt";
    String PACKAGE_NAME;
    ArrayList<String> mArray;
    String SOURCEFILE_PATH;
    String USER_DIRECTORY;
    VideoView mVideoView;
    MediaController vidControl;
    Button  loginButton,    registerButton;
    TextView    emailText, passText;
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference   mDatabaseReference;
    ArrayList<String>   mValues  =   new ArrayList<>();
    ArrayList<String>   mKeys  =   new ArrayList<>();
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener  mAuthListener;
    String  mEmail, mPassword,  mUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMain();
    }

    private View.OnClickListener loginListener  =   new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mEmail   =   emailText.getText().toString().trim();
            mPassword    =   passText.getText().toString().trim();

            mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful())    {
                        String error = task.getException().getMessage();
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Log.e(TAG,"Wrong password "+error);
                            Toast.makeText(getApplicationContext(), "The password is incorrect",    Toast.LENGTH_LONG).show();
                        }   else   if (task.getException() instanceof FirebaseAuthInvalidUserException)    {
                            Log.e(TAG,"Wrong email "+error);
                            Toast.makeText(getApplicationContext(), "The email doesn't exist. Click register to sign up",    Toast.LENGTH_LONG).show();
                        }   else    {
                            Log.e(TAG,error);
                            Toast.makeText(getApplicationContext(), error,    Toast.LENGTH_LONG).show();
                        }
                    }   else    {
                        mUid =   mAuth.getCurrentUser().getUid();
                        User    user    =   new User(mEmail, mPassword,   mUid);
                        userLocalData.setUserLoggedIn(true);
                        userLocalData.storeUserData(user);

                        initMain();
                    }
                }
            });
        }
    };

    private ChildEventListener childListener   =   new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String value    =   dataSnapshot.getValue().toString();
            String key    =   dataSnapshot.getKey();

            mValues.add(value);
            mKeys.add(key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String value    =   dataSnapshot.getValue().toString();
            String key    =   dataSnapshot.getKey();

            int index   =   mKeys.indexOf(key);
            mValues.set(index,   value);
            setTitle(mValues.get(index));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    /*
     *  captures the values of all children with their keys on start and when a value is changed
     *  it captures the entire list both times
     *  ex: {01=yunis, 02=george, 03=emma, -LJG-Fey5Ug_ZB-tUgNZ={Email=yunis.rajab@gmail.com, Password=123456}}
     */
    private ValueEventListener valueListener   =   new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue()    !=  null)   {
                String  value    =   dataSnapshot.getValue().toString();
            }


        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void createUser (String email,  String  password)  {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.e(TAG,"Success!");
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(),
                                    "Failed to create account "+task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG,"Failed to create account "+task.getException().getMessage());
                        }

                        // ...
                    }
                });
    }

    private void initLogin  ()  {
        setContentView(R.layout.login);
        Log.i(TAG,"Login layout");

        registerButton  =   findViewById(R.id.registerButton);
        loginButton =   findViewById(R.id.loginButton);
        emailText   =   findViewById(R.id.emailText);
        passText    =   findViewById(R.id.passText);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser(emailText.getText().toString().trim(),   passText.getText().toString().trim());
            }
        });
        loginButton.setOnClickListener(loginListener);
        emailText.addTextChangedListener(textWatcher);
        passText.addTextChangedListener(textWatcher);

        userLocalData.setUserLoggedIn(false);
        emailText.setText("");
        passText.setText("");
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
    }

    private void initMain   ()  {
        setContentView(R.layout.activity_main);
        Log.i("Curator","Main layout");

        mVideoView = (VideoView)    findViewById(R.id.myVideo);
        addButton = (Button)    findViewById(R.id.addButton);
        childButton = (Button) findViewById(R.id.childButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        listButton  =   (Button)    findViewById(R.id.listButton);

        ratingText = (EditText)    findViewById(R.id.ratingText);
        urlText = (EditText)    findViewById(R.id.urlText);

        ratingText.addTextChangedListener(textWatcher);
        urlText.addTextChangedListener(textWatcher);

        addButton.setEnabled(false);

        childButton.setOnClickListener(childSwitch);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String  url =   urlText.getText().toString().trim();
                int rating    =   0;
                if (ratingText.getText().toString().trim()!=null)   {
                    rating    =   Integer.parseInt(ratingText.getText().toString().trim());
                }
                upload(url,rating);
                ratingText.getText().clear();
                urlText.getText().clear();
                addButton.setEnabled(false);
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                initLogin();
            }
        });
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMain = new Intent(MainActivity.this , ListActivity.class);
                MainActivity.this.startActivity(intentMain);
                Log.i(TAG,"List layout");
                finish();
            }
        });

        userLocalData   =   new UserLocalData(this);
        mUser   =   userLocalData.getLoggedUser();

//        TODO share link into app instead of manual cop+paste

        if (!isReadStorageAllowed())    requestStoragePermission();

        if (!userLocalData.getUserLoggedIn())    {
            initLogin();
        }

        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);
        mDatabaseReference.addValueEventListener(valueListener);
        mDatabaseReference.child("URLs").addChildEventListener(childListener);
        mAuth   =   FirebaseAuth.getInstance();

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mUser.uid)) {
                    Toast.makeText(getApplicationContext(),"Welcome!",Toast.LENGTH_LONG).show();
                    rootRef.child("Main_List").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mDatabaseReference.setValue(dataSnapshot.getValue());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }   else {
                    Toast.makeText(getApplicationContext(),"Welcome back!",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    upload(sharedText,0);
                    finish();
                }
            }
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int rating=0;
            try{
                rating    =   Integer.parseInt(ratingText.getText().toString().trim());

            }catch(NumberFormatException e){
            }

            String  url =   urlText.getText().toString().trim();
            addButton.setEnabled(rating!=0 && !url.isEmpty());
            if (!userLocalData.getUserLoggedIn())   {
                String  email   =   emailText.getText().toString().trim();
                String  password    =   passText.getText().toString().trim();
                loginButton.setEnabled(email.contains("@")  &&  password.length()>5);
                registerButton.setEnabled(email.contains("@")  &&  password.length()>5);
            }
        }
        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private final View.OnClickListener addListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

        }
    };

    private final View.OnClickListener childSwitch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intentMain = new Intent(MainActivity.this , ChildActivity.class);
            MainActivity.this.startActivity(intentMain);
            Log.i(TAG,"Child layout");
            finish();

        }
    };

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
                    final   URL embededURL = new URL("http://www.youtube.com/oembed?url=" +
                            youtubeUrl + "&format=json");
                    final String  title   =
                            new JSONObject(org.apache.commons.io.IOUtils.toString(embededURL))
                                    .getString("title");

                    final Video video =   new Video(title,youtubeUrl,rating);

                    mDatabaseReference.child(videoID).setValue(video).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())    {
                                Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_LONG).show();
                            }   else {
                                Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_LONG).show();
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
    }

//    public void createFile() {
////            TODO  don't overwrite if file exists (only append)
//        try {
//            File root = new File(SOURCEFILE_PATH);
//            if (!root.exists()) {
//                boolean success = root.mkdir();
//                Log.d(TAG, "Create Directory: "+success);
//            }
//            File txtFile = new File(root, FILENAME);
//            FileWriter writer = new FileWriter(txtFile);
//            if (mArray!=null)   {
//                for (int i=0;   i<mArray.size();  i++)    {
//                    writer.append(mArray.get(i));
//                }
//            }
//            writer.flush();
//            writer.close();
//            mArray = null;
//            Toast.makeText(this, "Saved : " + txtFile.getAbsolutePath(),
//                    Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            Log.e(TAG,"Exception: "+e);
//            e.printStackTrace();
//        }
//    }
//
//    private void retrieveFile()  {
//        Thread t;
//        t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ftpClient.ftpConnect("192.168.0.16", "Yunis Rajab", "qwerty27", 21);
//                Log.e(TAG,"Connecting");
//                ftpClient.ftpDownload("/"+USER_DIRECTORY+"/"+FILENAME, SOURCEFILE_PATH+FILENAME);
//                Log.e(TAG,"Downloading");
//                ftpClient.ftpDisconnect();
//            }
//        });
//        try {
//            t.start();
//            t.join();
//        }   catch (Exception e) {
//            e.printStackTrace();
//        }
////        readFile();
//    }

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
