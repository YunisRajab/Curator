package com.yunisrajab.curator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LoginActivity  extends Activity {

    Button  loginButton;
    TextView    emailText, passText;
    UserLocalData userLocalData;
    DatabaseReference   mDatabaseReference;
    String  TAG =   "Login";
    ArrayList<String>   mUsers  =   new ArrayList<>();
    ArrayList<String>   mKeys  =   new ArrayList<>();
    FirebaseAuth    mAuth;
    FirebaseAuth.AuthStateListener  mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginButton =   findViewById(R.id.loginButton);
        emailText   =   findViewById(R.id.emailText);
        passText    =   findViewById(R.id.passText);

        loginButton.setOnClickListener(loginListener);
        emailText.addTextChangedListener(textWatcher);
        passText.addTextChangedListener(textWatcher);

        loginButton.setEnabled(false);

        userLocalData   =   new UserLocalData(this);

        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.addValueEventListener(valueListener);
        mDatabaseReference.addChildEventListener(childListener);
        mAuth   =   FirebaseAuth.getInstance();
        mAuthListener   =   new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser()   !=  null)   {

//                    intent User Account
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    private TextWatcher textWatcher =   new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String  email   =   emailText.getText().toString().trim();
            String  password    =   passText.getText().toString().trim();

            loginButton.setEnabled(email.contains("@")  &&  password.length()>5);

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private View.OnClickListener loginListener  =   new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String  email   =   emailText.getText().toString().trim();
            String  password    =   passText.getText().toString().trim();
            String  uid =   mAuth.getCurrentUser().getUid();

            if (!userExists())    {
//                TODO get user password from server
//                if (!password==password on FTP) {
//                    passText.setText("");
//                    Toast.makeText(getApplicationContext(), "Incorrect password",   Toast.LENGTH_LONG);
//                }

            }   else {
                User    user    =   new User(email, password,   uid);
                userLocalData.setUserLoggedIn(true);
                userLocalData.storeUserData(user);

                createUser(email,password);

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful())    {
                            Toast.makeText(getApplicationContext(), "Sign in error",    Toast.LENGTH_LONG).show();
                        }   else    {
                            Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                            getApplicationContext().startActivity(intent);
                            Log.i("Curator","Main layout");
                            finish();
                        }
                    }
                });

//                HashMap<String,String> dataMap  =   new HashMap<>();
//                dataMap.put("Email",email);
//                dataMap.put("Password",password);
//                mDatabaseReference.push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful())    {
//                            Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
//                        }   else {
//                            Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
            }
        }
    };

    private ChildEventListener  childListener   =   new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String value    =   dataSnapshot.getValue().toString();
            String key    =   dataSnapshot.getKey();

            mUsers.add(value);
            mKeys.add(key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String value    =   dataSnapshot.getValue().toString();
            String key    =   dataSnapshot.getKey();

            int index   =   mKeys.indexOf(key);
            mUsers.set(index,   value);
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
    private ValueEventListener  valueListener   =   new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String  value    =   dataSnapshot.getValue().toString();

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
                            Log.e(TAG,"Failure!");
                        }

                        // ...
                    }
                });
    }

    private boolean userExists  ()  {
//        TODO check FTP server for user and return true if it exists
        return true;
    }
}
