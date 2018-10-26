package com.yunisrajab.curator.Adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yunisrajab.curator.Activities.MainActivity;
import com.yunisrajab.curator.Activities.WebActivity;
import com.yunisrajab.curator.Activities.YouTubeActivity;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.DownloadImageTask;
import com.yunisrajab.curator.History;
import com.yunisrajab.curator.HtmlParser;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ViewHolder> {
    private ArrayList<Video> mArrayList;
    private LayoutInflater mInflater;
    private Context mContext;
    private DatabaseManager mDatabaseManager;
    AlertDialog.Builder builder;
    String TAG = "curator fav adapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChildAdapter(Context context, ArrayList<Video> arrayList) {
        mArrayList = arrayList;
        mInflater = LayoutInflater.from(context);
        mContext    =   context;
        mDatabaseManager    =   new DatabaseManager(mContext);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.child_list_item, parent, false);
        return  new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Video video = mArrayList.get(position);
        holder.title.setText(video.getTitle());
        if (video.getID().contains("@"))    {
            String  url =   video.getID().replace("@",".");
            if (URLUtil.isHttpsUrl("https://www."+url))    url =   "https://www."+url;
            else url    =   "http://www."+url;
//            new HtmlParser(holder.thumbnail).execute(url);
            holder.thumbnail.setImageResource(R.drawable.web_image);
        }   else    new DownloadImageTask((ImageView) holder.thumbnail)
                .execute("https://img.youtube.com/vi/"+video.getID()+"/0.jpg");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView    thumbnail;
        View    v;

        public ViewHolder(View v) {
            super(v);
            this.v  =   v;
            title = v.findViewById(R.id.itemName);
            thumbnail   =   v.findViewById(R.id.thumbnail);

            v.setOnClickListener(mOnClickListener);
//            v.setOnLongClickListener(mOnLongClickListener);

            builder  = new AlertDialog.Builder(mContext,    R.style.AlertDialogStyle);
            builder.setMessage("Do you want to delete this?").setPositiveButton("Delete", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener);
            builder.setCancelable(false);
        }

        public View.OnClickListener mOnClickListener    =   new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Video   video = mArrayList.get(getAdapterPosition());
                String videoID  = video.getID();

                Log.e(TAG,""+videoID);

                UserLocalData   userLocalData   =   new UserLocalData(mContext);
                User    user    =   userLocalData.getLoggedUser();
                DatabaseReference   databaseReference   =   FirebaseDatabase.getInstance().getReference()
                        .child("Users").child(user.uid).child("History");
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String date = df.format(Calendar.getInstance().getTime());
                String  key =   databaseReference.push().getKey();

                History history =   new History(video.getTitle(),videoID,date,key);

                databaseReference.child(key).setValue(history);

                Intent intent;
                if (videoID.contains("@"))  {
                    intent  =   new Intent(mContext, WebActivity.class);
                }   else {
                    intent = new Intent(mContext , YouTubeActivity.class);
                    Log.i(TAG,"YouTube layout");
                }
                intent.putExtra("videoID",videoID);
                mContext.startActivity(intent);
            }
        };

        public View.OnLongClickListener mOnLongClickListener    =   new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                builder.show();
                return true;
            }
        };

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        int pos =   getAdapterPosition();
                        if (pos !=  RecyclerView.NO_POSITION)   {
                            Video   video = mArrayList.get(pos);
                            mDatabaseManager.delete(video);
                            mArrayList.remove(pos);
                            notifyDataSetChanged();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
    }

    // convenience method for getting data at click position
    public Video getItem(int id) {
        return mArrayList.get(id);
    }
}
