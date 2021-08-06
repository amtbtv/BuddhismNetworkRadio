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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.Live;
import com.jianchi.fsp.buddhismnetworkradio.tools.LanguageUtils;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.video.VideoMenuManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //region 变量区
    /**
     * 视频播放器
     */
    VideoView videoView;

    /**
     * 播放按扭
     */
    private ImageButton bt_play;

    private ImageButton bt_full_screen;

    /**
     * 加载视频动画
     */
    ProgressBar proBar;
    int proBarThreadId = 0;

    /**
     * 管理播放器周边按扭的类
     */
    VideoMenuManager menuManager;

    /**
     * 自定义APP类
     */
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
            videoView.setBackgroundResource(R.drawable.zcgt);
            videoView.setOnPreparedListener(videoViewOnPreparedListener);
            videoView.setOnErrorListener(videoViewOnErrorListener);
            videoView.setOnTouchListener(videoViewOnTouchListener);
            videoView.setOnInfoListener(infoListener);

            RelativeLayout videoView_bottom = (RelativeLayout)findViewById(R.id.videoView_bottom);
            menuManager=new VideoMenuManager(MainActivity.this, videoView_bottom);//, videoView_top

            //按放按扭
            bt_play = (ImageButton) findViewById(R.id.bt_play);
            bt_play.setOnClickListener(bt_playOnClickListener);

            /*
            bt_full_screen = (ImageButton) findViewById(R.id.bt_full_screen);
            bt_full_screen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 全屏或半屏显示，默认是全屏显示

                    bt_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_exit);
                    bt_full_screen.setImageResource(R.drawable.exo_controls_fullscreen_enter);
                }
            });
*/
            if(!app.isNetworkConnected()){
                networkFailClose();
            }else {
                playVideo();
            }
        }
    }


    /*
    退出流程设计
    若为打电话或按HOME键
        执行 onStop 事件后不执行 onDestory
        在返回时 不执行 onCreate ，而是执行 onSavedInstanceState。onRestart()开始-onStart()-onResume()

    若为back键
        finish前台的activity，即activity的状态为onDestory为止
        再次启动该activity则从onCreate开始，不会调用onSavedInstanceState方法
     */

    //用来响应当打电话等事件后，重新开始播放
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(isPlayingResume)
            playVideo();
    }

    //暂停播放并记录播放状态
    @Override
    protected void onPause() {
        if(videoView!=null && videoView.isPlaying()) {
            isPlayingResume =true;
            stopVideo();
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

    MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        /**
         * Called to indicate an info or a warning.
         *
         * param mp      the MediaPlayer the info pertains to.
         * param what    the type of info or warning.
         * <ul>
         * <li>{link #MEDIA_INFO_UNKNOWN}
         * <li>{link #MEDIA_INFO_VIDEO_TRACK_LAGGING}
         * <li>{link #MEDIA_INFO_VIDEO_RENDERING_START}
         * <li>{link #MEDIA_INFO_BUFFERING_START}
         * <li>{link #MEDIA_INFO_BUFFERING_END}
         * <li><code>MEDIA_INFO_NETWORK_BANDWIDTH (703)</code> -
         *     bandwidth information is available (as <code>extra</code> kbps)
         * <li>{link #MEDIA_INFO_BAD_INTERLEAVING}
         * <li>{link #MEDIA_INFO_NOT_SEEKABLE}
         * <li>{link #MEDIA_INFO_METADATA_UPDATE}
         * <li>{link #MEDIA_INFO_UNSUPPORTED_SUBTITLE}
         * <li>{link #MEDIA_INFO_SUBTITLE_TIMED_OUT}
         * </ul>
         * @param extra an extra code, specific to the info. Typically
         * implementation dependent.
         * @return True if the method handled the info, false if it didn't.
         * Returning false, or not having an OnInfoListener at all, will
         * cause the info to be discarded.
         */
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
            errTimes=0;
        }
    };

    MediaPlayer.OnErrorListener videoViewOnErrorListener=new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            //region 错误信息翻译说明
                    /*
                    错误常数

MEDIA_ERROR_IO
文件不存在或错误，或网络不可访问错误
值: -1004 (0xfffffc14)

MEDIA_ERROR_MALFORMED
流不符合有关标准或文件的编码规范
值: -1007 (0xfffffc11)

MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK
视频流及其容器不适用于连续播放视频的指标（例如：MOOV原子）不在文件的开始.
值: 200 (0x000000c8)

MEDIA_ERROR_SERVER_DIED
媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
值: 100 (0x00000064)

MEDIA_ERROR_TIMED_OUT
一些操作使用了过长的时间，也就是超时了，通常是超过了3-5秒
值: -110 (0xffffff92)

MEDIA_ERROR_UNKNOWN
未知错误
值: 1 (0x00000001)

MEDIA_ERROR_UNSUPPORTED
比特流符合相关编码标准或文件的规格，但媒体框架不支持此功能
值: -1010 (0xfffffc0e)


what 	int: the type of error that has occurred:
    MEDIA_ERROR_UNKNOWN
    MEDIA_ERROR_SERVER_DIED
extra 	int: an extra code, specific to the error. Typically implementation dependent.
    MEDIA_ERROR_IO
    MEDIA_ERROR_MALFORMED
    MEDIA_ERROR_UNSUPPORTED
    MEDIA_ERROR_TIMED_OUT
    MEDIA_ERROR_SYSTEM (-2147483648) - low-level system error.

* */
            //endregion

            MyLog.e("MediaPlayer onError", "int what " + what + ", int extra" + extra);

            //根据不同的错误进行信息提示
            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                //媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
                Toast.makeText(MainActivity.this, R.string.wlfwcw,
                        Toast.LENGTH_LONG).show();
            } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                    //文件不存在或错误，或网络不可访问错误
                    Toast.makeText(MainActivity.this, R.string.wlljcw,
                            Toast.LENGTH_LONG).show();
                } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    //超时
                    Toast.makeText(MainActivity.this, R.string.wlcs,
                            Toast.LENGTH_LONG).show();
                }
            }

            //发生错误，关闭播放的视频
            stopVideo();

            return false;
        }
    };

    View.OnTouchListener videoViewOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (menuManager.menuVisible) {
                if(videoView.isPlaying())
                    menuManager.hideMenu();
            } else {
                menuManager.displayMenu(true);
            }
            return false;
        }
    };

    View.OnClickListener bt_playOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(videoView.isPlaying()){
                stopVideo();
            } else {
                playVideo();
            }
        }
    };

    //有三种情况会用到该函数：启动时、由打电话等其它APP影响恢复时、主动播放
    private void playVideo() {
        //让播放按扭延迟隐藏
        if (menuManager.menuVisible)
            menuManager.delayHide();//已经在显示了，只需要重置显示时间就可以了
        else
            menuManager.displayMenu(true);//显示菜单

        videoView.setVideoURI(Uri.parse(channel.mediaUrl));
        videoView.start();

        bt_play.setImageResource(R.mipmap.ic_stop);//设置按扭图标为暂停
        proBar.setVisibility(View.VISIBLE);
    }

    //有三种情况会用到该函数：打电话等其它APP影响、出错、主动停止
    private void stopVideo() {
        try {
            videoView.stopPlayback();
        } catch (Exception e) {
        }

        //让播放按扭一直显示
        if (menuManager.menuVisible)
            menuManager.alwaysShow();
        else
            menuManager.displayMenu(false);

        bt_play.setImageResource(R.mipmap.ic_play);
        videoView.setBackgroundResource(R.drawable.zcgt);
    }
}
