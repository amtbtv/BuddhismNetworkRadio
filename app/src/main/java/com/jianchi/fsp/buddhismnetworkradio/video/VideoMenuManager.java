package com.jianchi.fsp.buddhismnetworkradio.video;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by fsp on 16-7-13.
 */
public class VideoMenuManager {

    private int hideMenuTime = -1;

    public boolean menuVisible = true;

    //LinearLayout videoView_top;
    RelativeLayout videoView_bottom;
    Activity activity;


    //在播放中，菜单显示5秒后自动隐藏
    private Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (menuVisible && hideMenuTime>=0) {
                    hideMenuTime++;
                    if (hideMenuTime>60) {
                        hideMenuTime = 0;
                        hideMenu();
                    }
                }
            }
        }
    });

    //LinearLayout videoView_top,
    public VideoMenuManager(Activity activity,  RelativeLayout videoView_bottom) {
        this.videoView_bottom = videoView_bottom;
        //this.videoView_top = videoView_top;
        this.activity=activity;
        t.start();
    }

    public void delayHide(){
        hideMenuTime = 0;
    }
    public void alwaysShow(){
        hideMenuTime = -1;
        if(!menuVisible)
            show();
    }

    public void displayMenu(boolean delayHide){
        if(delayHide)
            hideMenuTime=0;
        else
            hideMenuTime=-1;
        if(!menuVisible)
            show();
    }

    public void hideMenu(){
        hideMenuTime=-1;
        hide();
    }

    /**
     * 显示菜单
     */
    private void show(){
        menuVisible = true;
        videoView_bottom.bringToFront();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AnimationSet animationSet = new AnimationSet(true);
                AlphaAnimation alphaAnimation;
                alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(500);
                animationSet.addAnimation(alphaAnimation);
                //videoView_top.startAnimation(animationSet);
                videoView_bottom.startAnimation(animationSet);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //videoView_top.setVisibility(View.VISIBLE);
                        videoView_bottom.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

    }

    /**
     * 隐藏菜单
     */
    private void hide() {
        menuVisible = false;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AnimationSet animationSet = new AnimationSet(true);
                AlphaAnimation alphaAnimation;
                alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(500);
                animationSet.addAnimation(alphaAnimation);
                //videoView_top.startAnimation(animationSet);
                videoView_bottom.startAnimation(animationSet);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //videoView_top.setVisibility(View.INVISIBLE);
                        videoView_bottom.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

    }

}
