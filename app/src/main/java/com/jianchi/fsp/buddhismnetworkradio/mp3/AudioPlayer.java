package com.jianchi.fsp.buddhismnetworkradio.mp3;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

/**
 * Created by fsp on 17-8-4.
 */

public class AudioPlayer {

    public interface EventListener {
        void buffering();
        void ready();
        void ended();
        void start();
    }

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private Context mContext;
    private SimpleExoPlayer mPlayer;
    //private int state;
    private EventListener mEventListener;
    private DataSource.Factory mediaDataSourceFactory;
    private HlsDataSourceFactory hlsDataSourceFactory;

    AudioManager am;
    public SimpleExoPlayer getPlayer() {
        return mPlayer;
    }

    public void setEventListener(EventListener mEventListener) {
        this.mEventListener = mEventListener;
    }

    public AudioPlayer(Context context){
        mContext = context;

        //初始化播放器
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        RenderersFactory renderersFactory = new DefaultRenderersFactory(mContext);
        mPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, new DefaultLoadControl());
        mPlayer.addListener(new APEventListener());

        //
        am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mediaDataSourceFactory = new DefaultDataSourceFactory(mContext, "buddhismnetworkradio");
        hlsDataSourceFactory = new DefaultHlsDataSourceFactory(mediaDataSourceFactory);
    }

    //音频焦点管理
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

            } else {
                if (mPlayer.getPlayWhenReady()) {
                    pause();
                }
            }
        }
    };

    public void play(String url, int positionMs){
        Uri uri = Uri.parse(url);
        MediaSource cmediaSource = new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
        //MediaSource cmediaSource = new HlsMediaSource.Factory(hlsDataSourceFactory).createMediaSource(uri);
        mPlayer.prepare(cmediaSource, true, true);
        mPlayer.seekTo(positionMs);
        play();
    }

    public boolean isPaused() {
        return !mPlayer.getPlayWhenReady() || mPlayer.getPlaybackState()==ExoPlayer.STATE_IDLE;
    }

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady() && (mPlayer.getPlaybackState()==Player.STATE_BUFFERING || mPlayer.getPlaybackState()==Player.STATE_READY );
    }

    public void pause() {
        mPlayer.setPlayWhenReady(false);
    }

    public void play() {
        mPlayer.setPlayWhenReady(true);
        if(mEventListener!=null)
            mEventListener.start();
    }

    public void release(){
        mPlayer.release();
    }

    public long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    public void seekTo(long pos) {
        mPlayer.seekTo(pos);
    }

    public void stop() {
        mPlayer.stop();
    }

    /**
     * 监听事件
     */
    class APEventListener extends Player.DefaultEventListener{
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);
            if(mEventListener!=null){
                //if(isPlaying())
                if (playbackState == Player.STATE_BUFFERING) {
                    mEventListener.buffering();
                } else if (playbackState == Player.STATE_READY) {
                    mEventListener.ready();
                } else if (playbackState == Player.STATE_ENDED) {
                    mEventListener.ended();
                }
            }

        }
    }
}
