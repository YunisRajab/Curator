package com.yunisrajab.curator.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.DownloadImageTask;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouritesFragment extends Fragment {

    ListView mListView;
    String TAG = "Curator list";
    String videoID;
    DatabaseReference mDatabaseReference;
    User mUser;
    UserLocalData mUserLocalData;
    AlertDialog.Builder builder;
    Video video;
    DatabaseManager mDatabaseManager;
    View    rootView;

    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i(TAG,"Fave layout");
        rootView    =   inflater.inflate(R.layout.fragment_favourites,    container,  false);

        mDatabaseManager    =   new DatabaseManager(getActivity());
        mListView   =   (ListView)  rootView.findViewById(R.id.favList);
        mUserLocalData  =   new UserLocalData(getActivity());
        mUser   =   mUserLocalData.getLoggedUser();
        mDatabaseReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.uid);

        FirebaseListAdapter<Video> adapter =   new FirebaseListAdapter<Video>(
                getActivity(),   Video.class,
                R.layout.fav_list_item,    mDatabaseReference.child("White_List").orderByChild("title")
        ) {
            @Override
            protected void populateView(View v, Video model, int position) {
                ((TextView)v.findViewById(R.id.itemName)).setText(model.getTitle());
                String  id = model.getID();
                new DownloadImageTask((ImageView) v.findViewById(R.id.thumbnail))
                        .execute("https://img.youtube.com/vi/"+id+"/0.jpg");
            }
        };
        mListView.setAdapter(adapter);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                video = (Video) mListView.getItemAtPosition(pos);
                videoID =   video.getID();
                builder.show();
                return true;
            }
        });



        builder  = new AlertDialog.Builder(getActivity(),    R.style.AlertDialogStyle);
        builder.setMessage("Do you want to delete this?").setPositiveButton("Delete", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener);
        builder.setCancelable(false);

        return rootView;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mDatabaseManager.delete(video,  videoID);
                    break;
//                case DialogInterface.BUTTON_NEGATIVE:
//                    //No button clicked
//                    Toast.makeText(ListActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    };
}
