package com.yunisrajab.curator;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.xwalk.core.XWalkView;

public class WebActivity    extends AppCompatActivity {

    XWalkView   mXWalkView;
    EditText    urlText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_browser);

        mXWalkView  =   (XWalkView) findViewById(R.id.xwalkWebView);
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
                    mXWalkView.load(url,  null);
                    System.out.println("xwalk"+mXWalkView.getTitle());
                    System.out.println("xwalk"+mXWalkView.getUrl());
                    return true;
                }
                return false;
            }
        });

    }
}
