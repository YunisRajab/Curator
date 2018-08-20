package com.yunisrajab.curator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity  extends AppCompatActivity {

    Button  loginButton,    registerButton;
    TextView    emailText, passText;
    UserLocalData userLocalData;
    DatabaseReference   mDatabaseReference;
    String  TAG =   "Login";
    FirebaseAuth    mAuth;
    String  mEmail, mPassword,  mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        registerButton  =   findViewById(R.id.registerButton);
        loginButton =   findViewById(R.id.loginButton);
        emailText   =   findViewById(R.id.emailText);
        passText    =   findViewById(R.id.passText);

        loginButton.setOnClickListener(loginListener);
        emailText.addTextChangedListener(textWatcher);
        passText.addTextChangedListener(textWatcher);

        userLocalData   =   new UserLocalData(this);
        userLocalData.setUserLoggedIn(false);
        emailText.setText("");
        passText.setText("");
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        loginButton.setEnabled(false);

        mDatabaseReference  = FirebaseDatabase.getInstance().getReference();
        mAuth   =   FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser(emailText.getText().toString().trim(),   passText.getText().toString().trim());
            }
        });
    }

    private TextWatcher textWatcher =   new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (!userLocalData.getUserLoggedIn())   {
                String  email   =   emailText.getText().toString().trim();
                String  password    =   passText.getText().toString().trim();
                loginButton.setEnabled(email.contains("@")  &&  password.length()>5);
                registerButton.setEnabled(email.contains("@")  &&  password.length()>5);
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

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

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(intent);
                        Log.i("Curator", "Main layout");
                        finish();
                    }
                }
            });
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

}
