package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

public class VideoPlayerActivity extends AppCompatActivity {

    String mp4;
    String title;

    /**
     * 视频播放器
     */
    VideoView videoView;
    MediaController controller;
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

        Intent intent = getIntent();
        mp4 = intent.getStringExtra("url");
        title = intent.getStringExtra("title");

        app = (BApplication)getApplication();
        proBar = (ProgressBar) findViewById(R.id.progressBar);

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setBackgroundResource(R.drawable.zcgt);
        videoView.setOnPreparedListener(videoViewOnPreparedListener);
        videoView.setOnErrorListener(videoViewOnErrorListener);
        videoView.setOnTouchListener(videoViewOnTouchListener);
        videoView.setOnInfoListener(infoListener);

        controller = new MediaController(this);
        String url = UrlHelper.makeMp4PlayUrl(mp4);
        videoView.setVideoURI(Uri.parse(url));
        controller.setMediaPlayer(videoView);
        videoView.setMediaController(controller);
        videoView.start();
        proBar.setVisibility(View.VISIBLE);
    }

    MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            if(what==MediaPlayer.MEDIA_INFO_BUFFERING_START){
                videoView.setBackgroundResource(R.drawable.zcgt);
                proBar.setVisibility(View.VISIBLE);
            } else if(what==MediaPlayer.MEDIA_INFO_BUFFERING_END){
                videoView.setBackgroundResource(0);
                proBar.setVisibility(View.INVISIBLE);
            }
            return false;
        }
    };

    MediaPlayer.OnPreparedListener videoViewOnPreparedListener=new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //缓冲结束
            if(proBar.getVisibility()==View.VISIBLE) proBar.setVisibility(View.INVISIBLE);
            videoView.setBackgroundResource(0);
            proBar.setVisibility(View.INVISIBLE);
        }
    };

    MediaPlayer.OnErrorListener videoViewOnErrorListener=new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            MyLog.e("MediaPlayer onError", "int what " + what + ", int extra" + extra);
            //根据不同的错误进行信息提示
            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                //媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
                Toast.makeText(VideoPlayerActivity.this, R.string.wlfwcw,
                        Toast.LENGTH_LONG).show();
            } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                    //文件不存在或错误，或网络不可访问错误
                    Toast.makeText(VideoPlayerActivity.this, R.string.wlljcw,
                            Toast.LENGTH_LONG).show();
                } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    //超时
                    Toast.makeText(VideoPlayerActivity.this, R.string.wlcs,
                            Toast.LENGTH_LONG).show();
                }
            }

            //发生错误，关闭播放的视频
            videoView.stopPlayback();

            return false;
        }
    };

    View.OnTouchListener videoViewOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            videoView.pause();
            return false;
        }
    };

}