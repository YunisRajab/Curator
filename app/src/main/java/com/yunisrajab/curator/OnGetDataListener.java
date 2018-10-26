package com.yunisrajab.curator;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnGetDataListener {
    void onStart();
    void onSuccess(HashMap<String,HashMap<String,String>> data);
    void onFailed(DatabaseError databaseError);
}