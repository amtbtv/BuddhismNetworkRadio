package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.RenderTVItem;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;
import com.jianchi.fsp.buddhismnetworkradio.video.VideoMenuManager;

public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    String mp4;
    String title;
    /**
     * 视频播放器
     */
    VideoView videoView;
    AmtbMediaController videoView_bottom;
    VideoMenuManager menuManager;
    /**
     * 加载视频动画
     */
    ProgressBar proBar;
    /**
     * 自定义APP类
     */
    BApplication app;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();

        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_video_player);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        Intent intent = getIntent();
        mp4 = intent.getStringExtra("url");
        title = intent.getStringExtra("title");

        app = (BApplication)getApplication();
        proBar = (ProgressBar) findViewById(R.id.progressBar);
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView_bottom = (AmtbMediaController) findViewById(R.id.videoView_bottom);
        menuManager=new VideoMenuManager(this, videoView_bottom);//, videoView_top

        videoView_bottom.setManager(videoView, menuManager, bt_render_tvOnClickListener, UrlHelper.makeMp4PlayUrl(mp4), proBar);

    }

    View.OnClickListener bt_render_tvOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RenderTVItem item = new RenderTVItem();
            item.type = "live";
            item.mediaUrl = UrlHelper.makeMp4PlayUrl(mp4);
            item.name = title;
            Intent intent = new Intent(VideoPlayerActivity.this, RenderTVActivity.class);
            intent.putExtra("RenderTVItem", new Gson().toJson(item));
            startActivity(intent);
            finish();
        }
    };

}