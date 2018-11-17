package com.jianchi.fsp.buddhismnetworkradio.mp3service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.model.FileListResult;
import com.jianchi.fsp.buddhismnetworkradio.mp3.AudioPlayer;
import com.jianchi.fsp.buddhismnetworkradio.mp3.MediaNotificationManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;

import java.util.List;

public class BMp3Service extends Service {

    AudioPlayer audioPlayer;
    MediaNotificationManager mediaNotificationManager;

    Mp3Program mp3Program;
    BMp3ServiceListener bMp3ServiceListener;

    boolean waitPhoneIdleToPlaye = false;

    List<String> mp3s;
    int dbRecId = -1;

    AudioPlayer.EventListener audioPlayerEventListener = new AudioPlayer.EventListener() {
        @Override
        public void buffering() {
            if(bMp3ServiceListener!=null)
                bMp3ServiceListener.buffering();
        }

        @Override
        public void ready() {
            if(bMp3ServiceListener!=null)
                bMp3ServiceListener.ready();
        }

        @Override
        public void ended() {
            //判断是否存在下一首歌
            if(mp3s.size()>mp3Program.curMediaIdx+1){
                mp3Program.curMediaIdx++;
                String mp3 = mp3s.get(mp3Program.curMediaIdx);
                mp3Program.postion = 0;
                audioPlayer.play(makeMp3Url(mp3), mp3Program.postion);
                if(bMp3ServiceListener!=null)
                    bMp3ServiceListener.playChange(mp3Program.curMediaIdx);
            }
        }

        @Override
        public void start() {
            mediaNotificationManager.startNotification(
                    mp3Program.programListItem.name,
                    mp3s.get(mp3Program.curMediaIdx)
            );
        }
    };

    public BMp3Service() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioPlayer = new AudioPlayer(this);
        audioPlayer.setEventListener(audioPlayerEventListener);
        mediaNotificationManager = new MediaNotificationManager(this);
        TelephonyManager tm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int dbRecId = intent.getIntExtra("dbRecId", 0);
        if(this.dbRecId!=dbRecId) {
            if(this.dbRecId!=-1 && mp3Program!=null){
                saveMp3Program();
            }
            this.dbRecId = dbRecId;
            Mp3RecDBManager db = new Mp3RecDBManager(this);
            //mp3Program必不为null，因为这是点击这个才来到这里的
            mp3Program = db.getMp3RecByDbRecId(dbRecId);
            db.close();

            //载入数据并播放
            AmtbApi<FileListResult> api = new AmtbApi<>(AmtbApi.takeFilesUrl(mp3Program.programListItem.identifier), new AmtbApiCallBack<FileListResult>() {
                @Override
                public void callBack(FileListResult obj) {
                    if (obj != null) {
                        //异步加载
                        mp3s = obj.files;
                        if (bMp3ServiceListener != null)
                            bMp3ServiceListener.downloadMp3s(mp3Program, mp3s);

                        String mp3 = obj.files.get(mp3Program.curMediaIdx);
                        String url = makeMp3Url(mp3);
                        audioPlayer.play(url, mp3Program.postion);

                    }
                }
            });
            api.execute(FileListResult.class);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //保存进度
        if(audioPlayer!=null) {
            audioPlayer.stop();
            audioPlayer.release();
            //unregisterMp3Receiver();
            mediaNotificationManager.stopNotification();
            saveMp3Program();
        }
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        BMp3ServiceBinder binder = new BMp3ServiceBinder(this);
        return binder;
    }

    public void play(int mp3Idx){
        if(mp3Program.curMediaIdx != mp3Idx){
            String mp3 = mp3s.get(mp3Idx);
            mp3Program.curMediaIdx = mp3Idx;
            mp3Program.postion = 0;
            audioPlayer.play(makeMp3Url(mp3), mp3Program.postion);

            //发送通知到 Notification 和 Activity
            if(bMp3ServiceListener!=null)
                bMp3ServiceListener.playChange(mp3Idx);
        }
    }

    PhoneStateListener listener=new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    if(waitPhoneIdleToPlaye){
                        audioPlayer.play();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("接听");
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    if(audioPlayer!=null && audioPlayer.isPlaying()){
                        waitPhoneIdleToPlaye = true;
                        audioPlayer.pause();
                    } else {
                        waitPhoneIdleToPlaye = false;
                    }
                    //输出来电号码
                    break;
            }
        }
    };

    //12-017-0019.mp3
    private String makeMp3Url(String mp3) {
        //                  http://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/02/02-041/02-041-0001.mp3/playlist.m3u8
        //                  http://amtbsg.cloudapp.net/redirect/media/mp3/02/02-041/02-041-0001.mp3
        //String urlFormat = "http://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/%s/%s/%s/playlist.m3u8";
        String urlFormat = "http://amtbsg.cloudapp.net/redirect/media/mp3/%s/%s/%s";
        String[] sp = mp3.split("-");
        String url = String.format(urlFormat, sp[0], mp3Program.programListItem.identifier, mp3);
        return url;
    }

    /**
     * 在暂停或更换了节目表时保存
     */
    private void saveMp3Program() {
        //获取进度，其它数据都在更改时自动获取
        mp3Program.postion = (int) audioPlayer.getCurrentPosition();
        Mp3RecDBManager db = new Mp3RecDBManager(this);
        db.update(mp3Program);
        db.close();
    }

}
