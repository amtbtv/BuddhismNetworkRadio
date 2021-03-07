package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.FaYin;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;

public class WebViewActivity extends AppCompatActivity {

    WebView webView;
    FaYin faYin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String json = getIntent().getExtras().getString("fayin");
        faYin = new Gson().fromJson(json, FaYin.class);
        //intent.putExtra("fayin", new Gson().toJson(faYin));

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        String head = Tools.readRawFile(R.raw.fayin_html_head, this);
        String foot = Tools.readRawFile(R.raw.fayin_html_foot, this);
        String content = faYin.content.rendered.replaceAll(" href=\"[^\"]*\"", " href=\"#\"");
        String title = "<h3>"+faYin.title.rendered+"</h3>";
        String html = head + title + content + foot;

        webView.loadDataWithBaseURL(faYin.link, html, "text/html", "utf-8", null);
    }
}