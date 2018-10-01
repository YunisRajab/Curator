package com.yunisrajab.curator.Fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.DownloadImageTask;
import com.yunisrajab.curator.History;
import com.yunisrajab.curator.HistoryActivity;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    ListView mListView;
    String TAG = "Curator list";
    String key;
    DatabaseReference mDatabaseReference;
    User mUser;
    UserLocalData mUserLocalData;
    boolean doubleBackPressedOnce   =   false;
    AlertDialog.Builder builder;
    History mHistory;
    DatabaseManager mDatabaseManager;
    View    rootView;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"History layout");
        rootView    =   inflater.inflate(R.layout.fragment_history,    container,  false);

        mDatabaseManager    =   new DatabaseManager(getActivity());
        mListView   =   (ListView)  rootView.findViewById(R.id.historyList);
        mUserLocalData  =   new UserLocalData(getActivity());
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);

        ReverseFirebaseListAdapter<History> adapter =   new ReverseFirebaseListAdapter<History>(
                getActivity(),   History.class,
                R.layout.history_item,    mDatabaseReference.child("History").orderByChild("time")
        ) {
            @Override
            protected void populateView(View v, History model, int position) {
                ((TextView)v.findViewById(R.id.itemName)).setText(model.getTitle());
                ((TextView)v.findViewById(R.id.itemTime)).setText(model.getTime());
                String  uri = model.getId();
                new DownloadImageTask((ImageView) v.findViewById(R.id.thumbnail))
                        .execute("https://img.youtube.com/vi/"+uri+"/0.jpg");
            }
        };
        mListView.setAdapter(adapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                mHistory = (History) mListView.getItemAtPosition(pos);
                key =   mHistory.getKey();
                builder.show();
                return true;
            }
        });

        builder  = new AlertDialog.Builder(getActivity(),    R.style.AlertDialogStyle);
        builder.setMessage("Do you want to delete this?").setPositiveButton("Delete", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener);

        return rootView;
    }


    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mDatabaseReference.child("History").child(key).removeValue();
                    break;
//                case DialogInterface.BUTTON_NEGATIVE:
//                    //No button clicked
//                    Toast.makeText(ListActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    };

    public abstract class ReverseFirebaseListAdapter<T> extends FirebaseListAdapter<T> {

        public ReverseFirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, Query ref) {
            super(activity, modelClass, modelLayout, ref);
        }

        public ReverseFirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, DatabaseReference ref) {
            super(activity, modelClass, modelLayout, ref);
        }

        @Override
        public T getItem(int position) {
            return super.getItem(getCount() - (position + 1));
        }
    }
}
