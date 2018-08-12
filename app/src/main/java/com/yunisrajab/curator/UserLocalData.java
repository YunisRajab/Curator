package com.yunisrajab.curator;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalData {

    private static final String SP_NAME =   "userData";
    SharedPreferences   localUserDatabase;

    public UserLocalData    (Context    context)    {
        localUserDatabase   =   context.getSharedPreferences(SP_NAME,   0);
    }

    public void storeUserData   (User   user)  {
        SharedPreferences.Editor    spEditor    =   localUserDatabase.edit();
        spEditor.putString("email", user.email);
        spEditor.putString("password", user.password);
        spEditor.putString("uid", user.uid);
        spEditor.apply();

    }

    public User getLoggedUser   ()  {
        String  email   =   localUserDatabase.getString("email","");
        String  password   =   localUserDatabase.getString("password","");
        String  uid   =   localUserDatabase.getString("uid","");

        User    storedUser  =   new User(email,password,uid);

        return storedUser;
    }

    public void setUserLoggedIn (boolean    loggedIn)   {
         SharedPreferences.Editor       spEditor    =   localUserDatabase.edit();
         spEditor.putBoolean("loggedIn",loggedIn);
         spEditor.apply();
    }

    public boolean  getUserLoggedIn ()  {
        if (localUserDatabase.getBoolean("loggedIn",false)==true) return true;
        else    return false;
    }

    public void clearUserData   ()  {
        SharedPreferences.Editor    spEditor    =   localUserDatabase.edit();
        spEditor.clear();
        spEditor.apply();
    }
}
