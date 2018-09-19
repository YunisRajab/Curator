package com.yunisrajab.curator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
        spEditor.putStringSet("votes", user.getVotesStrings());
        spEditor.putStringSet("favs", user.getFavs());

        spEditor.apply();
    }

    public User getLoggedUser   ()  {
        String  email   =   localUserDatabase.getString("email","");
        String  password   =   localUserDatabase.getString("password","");
        String  uid   =   localUserDatabase.getString("uid","");
        Set<String> votes =   localUserDatabase.getStringSet("votes",null);
        Set<String> favs =   localUserDatabase.getStringSet("favs",null);

        User    storedUser  =   new User(email,password,uid);
        storedUser.setVotes(votes);
        storedUser.setFavs(favs);
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
