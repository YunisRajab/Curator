package com.yunisrajab.curator;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public interface OnGetDataListener {
    void onStart();
    void onSuccess(ArrayList data);
    void onFailed(DatabaseError databaseError);
}