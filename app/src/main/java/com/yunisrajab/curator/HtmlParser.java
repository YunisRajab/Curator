package com.yunisrajab.curator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseError;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HtmlParser extends AsyncTask<String,Void,Document> {

    private ImageView mImageView;
    private String  url;

    public HtmlParser(ImageView imageView) {
        mImageView    =   imageView;
    }

    @Override
    protected Document doInBackground(String... urls) {
        url =   urls[0];
//        getElement();
        try {
//            HttpClient client = new DefaultHttpClient();
//            HttpGet request = new HttpGet(url);
//            HttpResponse response = client.execute(request);
//            Log.e("RESULT", "TEST4");
//
//            String html = "";
//            InputStream in = response.getEntity().getContent();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            StringBuilder str = new StringBuilder();
//            String line = null;
//            while((line = reader.readLine()) != null)
//            {
//                str.append(line);
//            }
//            in.close();
//            html = str.toString();
            Log.e("RESULT",url);
            return Jsoup.connect(url).get();
        }   catch (Exception    e)  {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Document s) {
        Document doc = s;
        Elements elements =   doc.select("link[rel]");
        HashMap<String,String> map =   new HashMap<>();
        String  iconUrl;

        for (Element    element :   elements)   {
            map.put(element.attr("rel"),element.attr("href"));
        }

        if (map.containsKey("apple-touch-icon"))    {
            iconUrl    =   url+map.get("apple-touch-icon");
        }   else if (map.containsKey("icon"))   {
            iconUrl    =   url+map.get("icon");
        }   else if (map.containsKey("shortcut icon"))  {
            iconUrl    =   url+map.get("shortcut icon");
        }   else  iconUrl    =   url+"/favicon.ico";
        new DownloadImageTask(mImageView).execute(iconUrl);
    }
}
