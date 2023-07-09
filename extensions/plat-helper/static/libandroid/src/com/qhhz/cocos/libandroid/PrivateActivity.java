package com.qhhz.cocos.libandroid;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;


public class PrivateActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocol_view);
        WebView theWebPage = findViewById(R.id.webView);
        theWebPage.loadUrl(BuildConfig.PRIVATE_URL);
        Button btn = (Button) findViewById(R.id.btnBack);
        btn.setOnClickListener(v -> {
            this.finish();
        });
        Runkit.FullScreen(getWindow());
    }
}