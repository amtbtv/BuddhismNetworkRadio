package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;

public class ScheduleActivity extends AppCompatActivity {
    BApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String title = intent.getStringExtra("name");
        String listUrl = intent.getStringExtra("listUrl");

        app = (BApplication)getApplication();

        toolbar.setTitle(TW2CN.getInstance(this).toLocal(title+"節目時間"));

        BApplication app = (BApplication) getApplication();

        WebView webView = (WebView) findViewById(R.id.webView);

        if(app.isNetworkConnected()) {
            if (listUrl.endsWith(".png")) {
                String html = "<!DOCTYPE html><html lang='zh-cn'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1' /><style type='text/css'>img{ width:100%; max-width:100%;}</style></head><body><div><img src='listUrl' /></div></body></html>";
                html = html.replace("listUrl", listUrl);
                webView.loadData(html, "text/html", "UTF-8");
            } else {
                webView.loadUrl(listUrl);
            }
        } else {
            Toast.makeText(ScheduleActivity.this, R.string.wlljcw, Toast.LENGTH_LONG).show();
        }
    }

}
