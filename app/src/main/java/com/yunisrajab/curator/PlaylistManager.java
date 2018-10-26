package com.yunisrajab.curator;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaylistManager {

//
//    private YouTube youtube;
//    private YouTube.PlaylistItems.List playlistItemRequest;
//    private String PLAYLIST_ID = "PLQPX9JJox0IpYX8WGb5pQplEqpbAj6y8Z";
//    public static final String KEY = DeveloperKey.DEVELOPER_KEY;
//
//    // Constructor
//    public PlaylistManager(Context context)
//    {
//        youtube = new YouTube.Builder(new NetHttpTransport(),
//                new JacksonFactory(), new HttpRequestInitializer()
//        {
//            @Override
//            public void initialize(HttpRequest hr) throws IOException {}
//        }).setApplicationName(context.getString(R.string.app_name)).build();
//    }
//
//    public ArrayList<Video> result()
//    {
//        List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();
//        try
//        {
//
//            /* HERE MUST BE MY PROBLEM ! */
//            playlistItemRequest = youtube.playlistItems().list("snippet,contentDetails");
//            playlistItemRequest.setPlaylistId(PLAYLIST_ID);
//            playlistItemRequest.setFields("items(snippet/title,snippet/description,snippet/thumbnails/default/url,contentDetails/videoId),nextPageToken,pageInfo");
//            playlistItemRequest.setKey(KEY);
//            String nextToken = "";
//            do {
//                playlistItemRequest.setPageToken(nextToken);
//                PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();
//
//                playlistItemList.addAll(playlistItemResult.getItems());
//
//                nextToken = playlistItemResult.getNextPageToken();
//            } while (nextToken != null);
//        }catch(IOException e)
//        {
//            Log.d("YC", "Could not initialize: "+e);
//        }
//        return new ArrayList<>();
//    }
//
////    public ArrayList<Video> getList ()  {
////        YouTube youtube = getYouTubeService();
////        try {
////            HashMap<String, String> parameters = new HashMap<>();
////            parameters.put("part", "snippet,contentDetails");
////            parameters.put("maxResults", "25");
////            parameters.put("playlistId", "PLBCF2DAC6FFB574DE");
////
////            YouTube.PlaylistItems.List playlistItemsListByPlaylistIdRequest = youtube.playlistItems().list(parameters.get("part").toString());
////            if (parameters.containsKey("maxResults")) {
////                playlistItemsListByPlaylistIdRequest.setMaxResults(Long.parseLong(parameters.get("maxResults").toString()));
////            }
////
////            if (parameters.containsKey("playlistId") && parameters.get("playlistId") != "") {
////                playlistItemsListByPlaylistIdRequest.setPlaylistId(parameters.get("playlistId").toString());
////            }
////
////            PlaylistItemListResponse response = playlistItemsListByPlaylistIdRequest.execute();
////            System.out.println(response);
////        }   catch   (Exception  e)  {
////
////        }
////        return new ArrayList<>();
////    }
}
