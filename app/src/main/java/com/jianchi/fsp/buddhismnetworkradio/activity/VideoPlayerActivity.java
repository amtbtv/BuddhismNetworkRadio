package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.RenderTVItem;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;
import com.jianchi.fsp.buddhismnetworkradio.video.VideoMenuManager;
import com.mikepenz.iconics.IconicsColor;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSize;
import com.mikepenz.iconics.IconicsSizeDp;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.Formatter;
import java.util.Locale;

public class VideoPlayerActivity extends AppCompatActivity {

    String mp4;
    String title;

    /**
     * 视频播放器
     */
    VideoView video_player;

    private ProgressBar mProgress;
    private TextView mEndTime;
    private TextView mCurrentTime;

    private boolean mDragging;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;

    private IconicsImageView bt_render_tv;
    private IconicsImageView bt_play;

    RelativeLayout videoView_bottom;
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

        video_player = (VideoView) findViewById(R.id.videoView);
        video_player.setBackgroundResource(R.drawable.zcgt);
        video_player.setOnPreparedListener(videoViewOnPreparedListener);
        video_player.setOnErrorListener(videoViewOnErrorListener);
        video_player.setOnTouchListener(videoViewOnTouchListener);
        video_player.setOnInfoListener(infoListener);

        String url = UrlHelper.makeMp4PlayUrl(mp4);
        video_player.setVideoURI(Uri.parse(url));
        video_player.start();

        bt_play = findViewById(R.id.bt_play);
        bt_play.setOnClickListener(bt_playOnClickListener);

        bt_render_tv = findViewById(R.id.bt_render_tv);
        bt_render_tv.setOnClickListener(bt_render_tvOnClickListener);

        mProgress = findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        videoView_bottom = (RelativeLayout)findViewById(R.id.videoView_bottom);
        menuManager=new VideoMenuManager(this, videoView_bottom);//, videoView_top

        mEndTime = findViewById(R.id.time);
        mCurrentTime = findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        proBar.setVisibility(View.VISIBLE);
        playVideo();
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

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }

            long duration = video_player.getDuration();
            long newposition = (duration * progress) / 1000L;
            video_player.seekTo( (int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            videoView_bottom.post(mShowProgress);
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && video_player.isPlaying()) {
                videoView_bottom.postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private int setProgress() {
        if (video_player == null || mDragging) {
            return 0;
        }
        int position = video_player.getCurrentPosition();
        int duration = video_player.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = video_player.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            if(what==MediaPlayer.MEDIA_INFO_BUFFERING_START){
                video_player.setBackgroundResource(R.drawable.zcgt);
                proBar.setVisibility(View.VISIBLE);
            } else if(what==MediaPlayer.MEDIA_INFO_BUFFERING_END){
                video_player.setBackgroundResource(0);
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
            video_player.setBackgroundResource(0);
            setProgress();
            videoView_bottom.post(mShowProgress);
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
            stopVideo();

            return false;
        }
    };

    View.OnTouchListener videoViewOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            menuManager.displayMenu(true);
            return false;
        }
    };

    View.OnClickListener bt_playOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(video_player.isPlaying()){
                stopVideo();
            } else {
                playVideo();
            }
        }
    };

    //有三种情况会用到该函数：启动时、由打电话等其它APP影响恢复时、主动播放
    private void playVideo() {
        //让播放按扭延迟隐藏
        menuManager.displayMenu(true);//显示菜单
        video_player.setVideoURI(Uri.parse(UrlHelper.makeMp4PlayUrl(mp4)));
        video_player.start();
        bt_play.setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_pause).color(IconicsColor.colorInt(Color.WHITE)).size(new IconicsSizeDp(24)));
        proBar.setVisibility(View.VISIBLE);
    }

    //有三种情况会用到该函数：打电话等其它APP影响、出错、主动停止
    private void stopVideo() {
        try {
            video_player.stopPlayback();
        } catch (Exception e) {
        }
        videoView_bottom.removeCallbacks(mShowProgress);
        //让播放按扭一直显示
        menuManager.displayMenu(false);
        bt_play.setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_play).color(IconicsColor.colorInt(Color.WHITE)).size(new IconicsSizeDp(24)));
        video_player.setBackgroundResource(R.drawable.zcgt);
    }
}