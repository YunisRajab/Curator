package com.yunisrajab.curator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity  extends Activity {

    Button  loginButton;
    TextView    emailText, passText;
    UserLocalData userLocalData;

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

            if (userExists())    {
//                TODO get user password from server
//                if (!password==password on FTP) {
//                    passText.setText("");
//                    Toast.makeText(getApplicationContext(), "Incorrect password",   Toast.LENGTH_LONG);
//                }

            }   else {
                User    user    =   new User(email, password);
                userLocalData.setUserLoggedIn(true);
                userLocalData.storeUserData(user);

                Intent intent = new Intent(getApplicationContext() , MainActivity.class);
                getApplicationContext().startActivity(intent);
                Log.i("Curator","Main layout");
                finish();
            }
        }
    };

    private boolean userExists  ()  {
//        TODO check FTP server for user and return true if it exists
        return false;
    }
}
