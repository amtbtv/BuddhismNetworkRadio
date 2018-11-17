package com.jianchi.fsp.buddhismnetworkradio.mp3service;

import android.os.Binder;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.jianchi.fsp.buddhismnetworkradio.mp3.AudioPlayer;

public class BMp3ServiceBinder extends Binder {
    BMp3Service bMp3Service;
    public BMp3ServiceBinder(BMp3Service bMp3Service) {
        this.bMp3Service = bMp3Service;
    }

    public void setOnBMp3ServiceListener(BMp3ServiceListener bMp3ServiceListener){
        bMp3Service.bMp3ServiceListener = bMp3ServiceListener;
        if(bMp3Service.mp3s!=null && bMp3Service.mp3Program!=null && bMp3ServiceListener!=null){
            bMp3ServiceListener.downloadMp3s(bMp3Service.mp3Program, bMp3Service.mp3s);
        }
    }

    public void playMp3(int mp3Idx) {
        bMp3Service.play(mp3Idx);
    }

    public SimpleExoPlayer getPlayer(){
        return bMp3Service.audioPlayer.getPlayer();
    }

}
