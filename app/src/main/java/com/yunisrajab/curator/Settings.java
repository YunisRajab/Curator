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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.net.URL;

public class Settings   extends AppCompatActivity {

    Button addButton, logoutButton;
    EditText urlText;
    String TAG = "Curator";
    Button  loginButton,    registerButton;
    TextView emailText, passText;
    UserLocalData userLocalData;
    User    mUser;
    DatabaseReference mDatabaseReference;
    FirebaseAuth mAuth;
    DatabaseManager mDatabaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        addButton = (Button)    findViewById(R.id.addButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);

        urlText = (EditText)    findViewById(R.id.urlText);

        urlText.addTextChangedListener(textWatcher);

        addButton.setEnabled(false);

        userLocalData   =   new UserLocalData(getApplicationContext());
        mUser   =   userLocalData.getLoggedUser();

        mDatabaseManager    =   new DatabaseManager(this);

        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);
        mAuth   =   FirebaseAuth.getInstance();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String  url =   urlText.getText().toString().trim();
                int rating    =   1;
                mDatabaseManager.upload(url,rating);
                urlText.getText().clear();
                addButton.setEnabled(false);
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(Settings.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                Settings.this.startActivity(intent);
                Log.i("Curator", "Login layout");
                finish();
            }
        });
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            String  url =   urlText.getText().toString().trim();
            addButton.setEnabled(!url.isEmpty());
        }
        @Override
        public void afterTextChanged(Editable editable) {}
    };

}
