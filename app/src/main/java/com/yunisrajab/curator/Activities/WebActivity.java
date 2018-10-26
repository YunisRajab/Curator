package com.yunisrajab.curator.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.yunisrajab.curator.R;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

public class WebActivity    extends AppCompatActivity {

    XWalkView   mXWalkView;
    EditText    urlText;
    WebView mWebView;
    String  TAG =   "Curator webview";
    AlertDialog.Builder mBuilder;
    boolean isXwalk;
    String  domain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_browser);

        domain = getIntent().getStringExtra("videoID").replace("@",".");

        mBuilder    = new AlertDialog.Builder(this,    R.style.AlertDialogStyle);

        mBuilder.setMessage("Pick view").setPositiveButton("Xwalk", dialogClickListener)
                .setNegativeButton("Webview", dialogClickListener);
        mBuilder.setCancelable(false);

        urlText = (EditText) findViewById(R.id.browserUrl);
        urlText.setFilters(new InputFilter[] {
                new InputFilter.AllCaps() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return String.valueOf(source).toLowerCase().replace(" ","");
                    }
                }
        });
        urlText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(urlText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    String  url =   urlText.getText().toString().trim();
                    if (!url.startsWith("http://www."))    url =   "http://www."+url;
                    urlText.setText(url);
                    if (isXwalk)   mXWalkView.loadUrl(url);
                    else mWebView.loadUrl(url);
                    return true;
                }
                return false;
            }
        });

        mBuilder.show();
    }

    DialogInterface.OnClickListener dialogClickListener =   new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            String  url =   "http://www."+domain;
            switch (i){
                case DialogInterface.BUTTON_POSITIVE:
                    initXwalk();
                    mXWalkView.loadUrl(url);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    initWebview();
                    mWebView.loadUrl(url);
                    break;
            }
        }
    };

    private void initXwalk()  {
        isXwalk =   true;
        mXWalkView  =   (XWalkView) findViewById(R.id.xwalkWebView);
        mXWalkView.setResourceClient(mXWalkResourceClient);
    }

    private void initWebview()  {
        isXwalk =   false;
        mWebView    =   findViewById(R.id.webView);
        mWebView.setWebViewClient(mWebViewClient);
    }

    XWalkResourceClient mXWalkResourceClient    =   new XWalkResourceClient(mXWalkView) {
        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            if (!url.contains(domain))  return true;
            urlText.setText(url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    };

    WebViewClient   mWebViewClient  =   new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String  url =   request.getUrl().toString();
            Log.i(TAG,  url);
            view.loadUrl(url);
            urlText.setText(url);
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }   else super.onBackPressed();
    }
}
