package com.yunisrajab.curator.Adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.yunisrajab.curator.DatabaseManager;
import com.yunisrajab.curator.DownloadImageTask;
import com.yunisrajab.curator.HtmlParser;
import com.yunisrajab.curator.OnGetDataListener;
import com.yunisrajab.curator.R;
import com.yunisrajab.curator.Video;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {
    private ArrayList<Video> mArrayList;
    private LayoutInflater mInflater;
    private Context mContext;
    private DatabaseManager mDatabaseManager;
    AlertDialog.Builder builder;
    String TAG = "curator fav adapter";

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavouritesAdapter(Context context, ArrayList<Video> arrayList) {
        mArrayList = arrayList;
        mInflater = LayoutInflater.from(context);
        mContext    =   context;
        mDatabaseManager    =   new DatabaseManager(mContext);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.fav_list_item, parent, false);
        return  new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Video video = mArrayList.get(position);
        holder.title.setText(video.getTitle());

        if (video.getID().contains("@"))    {
            String  url =   video.getID().replace("@",".");
            if (URLUtil.isHttpsUrl("https://www."+url))    url =   "https://www."+url;
            else url    =   "http://www."+url;
//            new HtmlParser(holder.thumbnail).execute(url);
            holder.thumbnail.setImageResource(R.drawable.web_image);
            holder.customText.setVisibility(View.VISIBLE);
        }   else    new DownloadImageTask((ImageView) holder.thumbnail)
                .execute("https://img.youtube.com/vi/"+video.getID()+"/0.jpg");
    }

    public void getElement(String   url,    final   ImageView   mImageView)    {
        OnGetDataListener onGetDataListener   =   new OnGetDataListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(HashMap<String, HashMap<String, String>> data) {

                String html = data.get("key").get("ref");
                Log.e(TAG, "HELLO");
                try {
                    System.out.println(html);
                    Bitmap mIcon11 = null;
                    InputStream inputStream = new java.net.URL(html).openStream();
                    mIcon11 = BitmapFactory.decodeStream(inputStream);
                    mImageView.setImageBitmap(mIcon11);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        };

        onGetDataListener.onStart();
        try {
            Log.e(TAG, url+2);
            Document doc = Jsoup.connect(url).get();
            Log.e(TAG, url+3);
            Element element =   doc.select("apple-touch-icon").first();
            String  html    =   url+element.attr("href");

            HashMap<String, HashMap<String,String>> map =   new HashMap<>();
            HashMap<String,String> map1 =   new HashMap<>();
            map1.put("ref",html);
            map.put("key",map1);
            onGetDataListener.onSuccess(map);
        }   catch (Exception    e)  {
            e.printStackTrace();
        }
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
        public TextView    customText;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.itemName);
            thumbnail   =   v.findViewById(R.id.thumbnail);
            customText   =   v.findViewById(R.id.customtext);

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
