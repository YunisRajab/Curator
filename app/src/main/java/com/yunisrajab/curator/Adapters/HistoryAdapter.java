package com.yunisrajab.curator.Adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.DownloadImageTask;
import com.yunisrajab.curator.History;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.User;
import com.yunisrajab.curator.UserLocalData;
import com.yunisrajab.curator.Video;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private ArrayList<History> mArrayList;
    private LayoutInflater mInflater;
    private Context mContext;
    private DatabaseManager mDatabaseManager;
    AlertDialog.Builder builder;
    String TAG = "curator fav adapter";

    public interface OnItemClickListener {
        public void onItemClicked(int position);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClicked(int position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(Context context, ArrayList<History> arrayList) {
        mArrayList = arrayList;
        mInflater = LayoutInflater.from(context);
        mContext    =   context;
        mDatabaseManager    =   new DatabaseManager(mContext);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.history_item, parent, false);
        return  new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        History history = mArrayList.get(position);
        holder.title.setText(history.getTitle());
        holder.time.setText(history.getTime());
        new DownloadImageTask((ImageView) holder.thumbnail)
                .execute("https://img.youtube.com/vi/"+history.getId()+"/0.jpg");
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
        public TextView title,  time;
        public ImageView    thumbnail;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.itemName);
            time = v.findViewById(R.id.itemTime);
            thumbnail   =   v.findViewById(R.id.thumbnail);

            v.setOnClickListener(mOnClickListener);
            v.setOnLongClickListener(mOnLongClickListener);

            builder  = new AlertDialog.Builder(mContext,    R.style.AlertDialogStyle);
            builder.setMessage("Do you want to delete this?").setPositiveButton("Delete", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener);
            builder.setCancelable(false);
        }

        public View.OnClickListener mOnClickListener    =   new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                            History history = mArrayList.get(pos);
                            mDatabaseManager.delete(history);
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
    public History getItem(int id) {
        return mArrayList.get(id);
    }
}
