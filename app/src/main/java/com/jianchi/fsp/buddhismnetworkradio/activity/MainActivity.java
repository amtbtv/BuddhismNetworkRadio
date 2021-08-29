package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.Live;
import com.jianchi.fsp.buddhismnetworkradio.model.RenderTVItem;
import com.jianchi.fsp.buddhismnetworkradio.tools.LanguageUtils;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;
import com.jianchi.fsp.buddhismnetworkradio.video.VideoMenuManager;
import com.mikepenz.iconics.IconicsColor;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSizeDp;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    VideoView videoView;
    AmtbMediaController videoView_bottom;

    ProgressBar proBar;

    VideoMenuManager menuManager;

    BApplication app;

    Live channel;

    int errTimes = 0;

    /**
     * 记录是否正在播放，以便在恢复时使用
     */
    boolean isPlayingResume = false;

    //默认横屏
    //boolean isPortraitFullScreen = true;

    //endregion

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

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        //获取自定义APP，APP内存在着数据，若为旋转屏幕，此处记录以前的内容
        app = (BApplication)getApplication();

        Intent intent = getIntent();
        channel = new Live();
        channel.name = intent.getStringExtra("name");
        channel.listUrl = intent.getStringExtra("listUrl");
        channel.mediaUrl = intent.getStringExtra("mediaUrl");

        //判断是否连接到网络
        if(!app.isNetworkConnected()){
            networkFailClose();
        }else {
            proBar = (ProgressBar) findViewById(R.id.progressBar);

            //初始化三个关键变量
            videoView = (VideoView) findViewById(R.id.videoView);
            videoView_bottom = (AmtbMediaController) findViewById(R.id.videoView_bottom);
            menuManager=new VideoMenuManager(this, videoView_bottom);//, videoView_top

            if(!app.isNetworkConnected()){
                networkFailClose();
            }else {
                videoView_bottom.setManager(videoView, menuManager, bt_render_tvOnClickListener, channel.mediaUrl, proBar, false);
            }
        }
    }

    //用来响应当打电话等事件后，重新开始播放
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(isPlayingResume)
            videoView_bottom.playVideo();
    }

    //暂停播放并记录播放状态
    @Override
    protected void onPause() {
        if(videoView!=null && videoView.isPlaying()) {
            isPlayingResume =true;
            videoView_bottom.stopVideo();
        } else {
            isPlayingResume =false;
        }
        super.onPause();
    }

    /**
     * 网络连接失败后关闭程序
     */
    void networkFailClose(){
        Toast.makeText(this, R.string.wljwl, Toast.LENGTH_LONG).show();//提示信息
        MyLog.v("onCreate", getString(R.string.wljwl));
        //提示过信息5秒后关闭程序
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //没有连接到网络，关系程序
                finish();
            }
        }).start();
    }

    View.OnClickListener bt_render_tvOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RenderTVItem item = new RenderTVItem();
            item.type = "live";
            item.mediaUrl = channel.mediaUrl;
            item.name = channel.name;
            Intent intent = new Intent(MainActivity.this, RenderTVActivity.class);
            intent.putExtra("RenderTVItem", new Gson().toJson(item));
            startActivity(intent);
            finish();

        }
    };

}
