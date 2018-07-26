package com.yunisrajab.curator;

import android.net.Uri;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class VideoActivity extends Activity{

    MediaController vidControl;
    VideoView mVideoView;
    String TAG = "VA";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        vidControl = new MediaController(this);
        mVideoView = (VideoView)    findViewById(R.id.myVideo);

        vidControl.setAnchorView(mVideoView);
        mVideoView.setMediaController(vidControl);
//        MainActivity mainActivity = new MainActivity();
        String vidAddress = "https://www.youtube.com/watch?v=_drAHMoi6Rs";
        Log.e(TAG,  "URI: "+vidAddress);
        Uri vidUri = Uri.parse(vidAddress);
        mVideoView.setVideoURI(vidUri);
        mVideoView.start();

    }

    @Override
    public void onBackPressed() {
        Intent intentMain = new Intent(VideoActivity.this ,
                MainActivity.class);
        VideoActivity.this.startActivity(intentMain);
        Log.i("Curator: ","Main layout");
        finish();
    }

}
