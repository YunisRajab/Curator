package com.yunisrajab.curator;

import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {


    TextView view;
    Button sendButton, receiveButton, startButton, ytButton;
    Switch parentSwitch;
    EditText editAddress, editPort, uriText;
    String TAG = "Curator";
    String mAddress;
    int port, minBufSize = 1280;
    DatagramSocket mSocket;
    boolean status = false;
    VideoView mVideoView;
    MediaController vidControl;
    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vidControl = new MediaController(this);
        mVideoView = (VideoView)    findViewById(R.id.myVideo);
        view = (TextView)   findViewById(R.id.textView);
        view.setText(getLocalIPAddress());
        sendButton = (Button)   findViewById (R.id.send_button);
        receiveButton = (Button)    findViewById (R.id.receive_button);
        startButton = (Button)  findViewById (R.id.startButton);
        ytButton = (Button)  findViewById (R.id.ytButton);
        parentSwitch = (Switch) findViewById(R.id.parentSwitch);

        sendButton.setOnClickListener (sendListener);
        receiveButton.setOnClickListener (receiveListener);
        startButton.setOnClickListener(startListener);
        ytButton.setOnClickListener(startListener);

        editAddress = (EditText)    findViewById(R.id.addText);
        editPort = (EditText)   findViewById(R.id.portText);
        uriText = (EditText)    findViewById(R.id.uriText);
    }

    private final View.OnClickListener sendListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {

            mAddress = editAddress.getText().toString();

            Thread streamThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        mSocket = new DatagramSocket(/*port*/);
                        Log.d(TAG, "Socket Created");

                        byte[] buffer = new byte[minBufSize];

                        Log.d(TAG,"Buffer created of size " + minBufSize);
                        DatagramPacket packet;

                        final InetAddress destination = InetAddress.getByName(mAddress);

                        status = true;
                        while(status) {
                            //putting buffer in the packet
                            packet = new DatagramPacket (buffer,buffer.length,destination,port);
                            mSocket.send(packet);
//                        Log.d(TAG,"MinBufferSize: " +minBufSize);
                        }
                        status = false;
                        mSocket.disconnect();
                        mSocket.close();
                    }
                    catch(UnknownHostException e) {
                        Log.e(TAG, "UnknownHostException");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "IOException");
                    } catch (Exception e)
                    {
                        Log.e(TAG, "Exception: "+e);
                    }
                }

            });
            streamThread.start();

        }
    };

    private final View.OnClickListener receiveListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mSocket = new DatagramSocket(port);
                        byte[] buff = new byte[minBufSize];
                        status = true;
                        while(status) {
                            // Play back the audio received from packets
                            DatagramPacket packet = new DatagramPacket(buff, minBufSize);
                            mSocket.receive(packet);
                            Log.i(TAG, "Packet received: " + packet.getLength());
                        }
                        status = false;
                        mSocket.disconnect();
                        mSocket.close();
                    }catch (Exception e)
                    {
                        Log.e(TAG, "Exception: "+e);
                    }
                }
            }).start();

        }
    };

    private final View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view==startButton)  {
                uri =   uriText.getText().toString();
                Intent intentMain = new Intent(MainActivity.this , VideoActivity.class);
                MainActivity.this.startActivity(intentMain);
                Log.i(TAG,"Video layout");

            }   else if (view==ytButton)    {
                Intent intentMain = new Intent(MainActivity.this , YouTubeActivity.class);
                MainActivity.this.startActivity(intentMain);
                Log.i(TAG,"YouTube layout");
            }
            finish();
        }
    };

    private static String getLocalIPAddress () {
        String ip = "";
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
//                        ip= inetAddress.getAddress();
                        String sAddr = inetAddress.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (isIPv4)
                            ip = sAddr;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.i("SocketException ", ex.toString());
        }
        return ip;
    }

    public String getUri() {
        return uri;
    }

    public void setText()  {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String str = "NOT WORKING!";
                try {
                    String text = "WORKING!";
                    byte[] bytes = text.getBytes("UTF-8");
                    str = new String(bytes, "UTF-8");

                }   catch (Exception e)  {
                    Log.e(TAG,"Exception: "+e);
                }


//                view.setText(receiver.getText());
                Log.v(TAG, "aftr txt.settext");
//                            bar.setProgress(5);
                Log.d(TAG, "received");
                Toast.makeText(MainActivity.this,"OnClickListener : received",Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        Log.i(TAG,"Quitting!");
        finish();
        System.exit(0);
    }
}
