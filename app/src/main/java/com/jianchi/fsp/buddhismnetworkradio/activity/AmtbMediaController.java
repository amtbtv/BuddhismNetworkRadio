package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;
import com.jianchi.fsp.buddhismnetworkradio.video.VideoMenuManager;
import com.mikepenz.iconics.IconicsColor;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSizeDp;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.Formatter;
import java.util.Locale;

public class AmtbMediaController extends RelativeLayout {
    private static final String TAG = "AmtbMediaController";
    VideoView video_player;
    int canPause = 0; // 0 未设置 1 可以 -1 不可以

    boolean showSeekBar;

    private ProgressBar mProgress;
    private TextView mEndTime;
    private TextView mCurrentTime;

    private boolean mDragging;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    String url;
    ProgressBar proBar;

    VideoMenuManager menuManager;

    private IconicsImageView bt_render_tv;
    private IconicsImageView bt_play;

    public AmtbMediaController(Context context) {
        super(context);
        init(context,null, 0);
    }

    public AmtbMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AmtbMediaController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }


    private void init(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.layout_amtb_media_controller, null);

        RelativeLayout.LayoutParams frameParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        addView(v, frameParams);

        this.bt_play = v.findViewById(R.id.bt_play);
        this.bt_render_tv = v.findViewById(R.id.bt_render_tv);
        this.mProgress = v.findViewById(R.id.mediacontroller_progress);
        if (this.mProgress != null) {
            this.mProgress.setMax(1000);
        }
        this.mEndTime = v.findViewById(R.id.time);
        this.mCurrentTime = v.findViewById(R.id.time_current);
        this.mFormatBuilder = new StringBuilder();
        this.mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }
    public void setManager(VideoView video_player, VideoMenuManager videoMenuManager, OnClickListener bt_render_tvOnClickListener, String url, ProgressBar proBar) {
        setManager(video_player, videoMenuManager, bt_render_tvOnClickListener, url, proBar, true);
    }

    public void setManager(VideoView video_player, VideoMenuManager videoMenuManager, OnClickListener bt_render_tvOnClickListener, String url, ProgressBar proBar, boolean showSeekBar){
        this.video_player = video_player;
        this.menuManager = videoMenuManager;
        this.url = url;
        this.proBar = proBar;
        this.showSeekBar = showSeekBar;

        this.bt_render_tv.setOnClickListener(bt_render_tvOnClickListener);
        this.bt_play.setOnClickListener(bt_playOnClickListener);

        if(showSeekBar) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
        } else {
            mProgress.setVisibility(INVISIBLE);
            mEndTime.setVisibility(INVISIBLE);
            mCurrentTime.setVisibility(INVISIBLE);
        }

        this.video_player.setBackgroundResource(R.drawable.zcgt);
        this.video_player.setOnPreparedListener(videoViewOnPreparedListener);
        this.video_player.setOnErrorListener(videoViewOnErrorListener);
        this.video_player.setOnTouchListener(videoViewOnTouchListener);
        this.video_player.setOnInfoListener(infoListener);

        this.proBar.setVisibility(View.VISIBLE);
        playVideo();
    }

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
            post(mShowProgress);
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
            if(!showSeekBar) return;
            int pos = setProgress();
            if (!mDragging && video_player.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    private int setProgress() {
        if (video_player == null || mDragging || !showSeekBar) {
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

    /**
     * 当快进后，缓存时发生
     */
    MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            if(what==MediaPlayer.MEDIA_INFO_BUFFERING_START){
                video_player.setBackgroundResource(R.drawable.zcgt);
                proBar.setVisibility(View.VISIBLE);
                Log.v(TAG, "MEDIA_INFO_BUFFERING_START");
            } else if(what==MediaPlayer.MEDIA_INFO_BUFFERING_END){
                video_player.setBackgroundResource(0);
                proBar.setVisibility(View.INVISIBLE);
                Log.v(TAG, "MEDIA_INFO_BUFFERING_END");
            }
            return false;
        }
    };

    /**
     * 准备媒体时发生，在后面进退里并不发生
     */
    MediaPlayer.OnPreparedListener videoViewOnPreparedListener=new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //缓冲结束
            if(proBar.getVisibility()==View.VISIBLE) proBar.setVisibility(View.INVISIBLE);
            video_player.setBackgroundResource(0);
            setProgress();
            post(mShowProgress);
            canPause = video_player.canPause() ? 1 : -1;
            proBar.setVisibility(View.INVISIBLE);
            Log.v(TAG, "onPrepared");
        }
    };

    MediaPlayer.OnErrorListener videoViewOnErrorListener=new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            MyLog.e("MediaPlayer onError", "int what " + what + ", int extra" + extra);
            //根据不同的错误进行信息提示
            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                //媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
                Toast.makeText(getContext(), R.string.wlfwcw,
                        Toast.LENGTH_LONG).show();
            } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                    //文件不存在或错误，或网络不可访问错误
                    Toast.makeText(getContext(), R.string.wlljcw,
                            Toast.LENGTH_LONG).show();
                } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    //超时
                    Toast.makeText(getContext(), R.string.wlcs,
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
            AmtbMediaController.this.bringToFront();
            menuManager.displayMenu(true);
            return false;
        }
    };

    //有三种情况会用到该函数：启动时、由打电话等其它APP影响恢复时、主动播放
    public void playVideo() {
        //让播放按扭延迟隐藏
        menuManager.displayMenu(true);//显示菜单
        if(canPause!=1)
            video_player.setVideoURI(Uri.parse(url));
        video_player.start();
        bt_play.setIcon(new IconicsDrawable(getContext(), FontAwesome.Icon.faw_pause).color(IconicsColor.colorInt(Color.WHITE)).size(new IconicsSizeDp(24)));
        if(video_player.getBufferPercentage()>0) {
            video_player.setBackgroundResource(0); //已经缓存，可以直接播放
            if(canPause == 1) //已经准备好并且能暂停
                post(mShowProgress);
        }
    }

    //有三种情况会用到该函数：打电话等其它APP影响、出错、主动停止
    public void stopVideo() {
        //让播放按扭一直显示
        menuManager.displayMenu(false);
        if(video_player.isPlaying()) {
            if (video_player.canPause())
                video_player.pause();
            else
                video_player.stopPlayback();
        }
        removeCallbacks(mShowProgress);
        bt_play.setIcon(new IconicsDrawable(getContext(), FontAwesome.Icon.faw_play).color(IconicsColor.colorInt(Color.WHITE)).size(new IconicsSizeDp(24)));
        video_player.setBackgroundResource(R.drawable.zcgt);
    }
}