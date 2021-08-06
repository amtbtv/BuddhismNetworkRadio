package com.jianchi.fsp.buddhismnetworkradio.mp3service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.model.FileItem;
import com.jianchi.fsp.buddhismnetworkradio.model.FileListResult;
import com.jianchi.fsp.buddhismnetworkradio.mp3.AudioPlayer;
import com.jianchi.fsp.buddhismnetworkradio.mp3.MediaNotificationManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.SharedPreferencesHelper;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.List;

/**
 * 播放服务说明
 * 1. 在StartActivity中启动播放服务，并自动调用onStartCommand，开始下载数据，下载成功后，播放
 * 2.在Mp3PlayerActivity中
 *      1.在启动后，bindService，成功后，绑定事件监听，并绑定播放器到播放控件
 *      2.仅在点右上角菜单action_stop_service后，明确关闭服务，并退出Activity
 *      3.监听事件共有4个
 *          void playChange(int index);
 *          void downloadMp3s(Mp3Program mp3Program, List<FileItem> mp3s);
 *          void buffering();
 *          void ready();
 *          在绑定后，若已经载入，会调用downloadMp3s传数据到Activity，若正在加载，则加载成功后，会调用些方法
 *      4.在onDestory中，注册事件，解绑服务。其它情况如Activity转入后台，不再考虑，经尝试不会出什么问题
 * 3.BMp3ServiceBinder中，只有4个方法：绑定事件setOnBMp3ServiceListener; 播放playMp3; 获取播放器用于绑定播放控件getPlayer
 * 4.在AudioPlayer中，有播放停止等方法，并把播放事件传递给Service，并处理音频焦点管理
 * 5.在BMp3Service中
 *      1.接收AudioPlayer的事件，并传递给Activity
 *      2.处理来电，暂停并重启播放服务
 *      3.当服务被停止后，此时APP应该已经退出了，不必再将服务被停事件传给Activity了
 *      4.处理服务被停后重启。被停前保存状态，重启后读取状态，并继续
 *      5.启动时仅传递一个ID进来，重启时，载入保存的mp3Program，之后从网络上载入数据，开始播放
 */
public class BMp3Service extends Service {

    AudioPlayer audioPlayer;
    MediaNotificationManager mediaNotificationManager;

    Mp3Program mp3Program;
    BMp3ServiceListener bMp3ServiceListener;

    boolean waitPhoneIdleToPlaye = false;

    List<FileItem> mp3s;
    int mp3sDbRecId = -1; //记录，当前mp3s中保存的数据是的DbRecId

    int dbRecId = -1;
    boolean isDowning = false;

    public void setbMp3ServiceListener(BMp3ServiceListener bMp3ServiceListener) {
        this.bMp3ServiceListener = bMp3ServiceListener;
        if(isDowning){
            System.out.println("正在下载，下载完后会引发更新内容的事件");
        } else {
            System.out.println("没在下载，若内容可用，直接返回");
            //mp3Program.dbRecId == mp3sDbRecId ，防止当前的mp3s中存在的不是当前请求的
            if (mp3s != null && mp3Program != null && bMp3ServiceListener != null && mp3Program.dbRecId == mp3sDbRecId) {
                bMp3ServiceListener.downloadMp3s(mp3Program, mp3s);
            }
        }
    }

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
                mp3Program.postion = 0;
            } else {
                mp3Program.curMediaIdx = 0;
                mp3Program.postion = 0;
            }
            saveCurMp3Program();
            saveMp3Program();
            mp3Program.postion = 0;
            FileItem mp3 = mp3s.get(mp3Program.curMediaIdx);
            audioPlayer.play(makeMp3Url(mp3.file), mp3Program.postion);
            if(bMp3ServiceListener!=null) {
                bMp3ServiceListener.playChange(mp3Program.curMediaIdx);
            }
        }

        /**
         * 开始播放后，启动通知，并设置为前台服务
         */
        @Override
        public void start() {
            Notification notification = mediaNotificationManager.startNotification(
                    mp3Program.programListItem.name,
                    mp3s.get(mp3Program.curMediaIdx).file
            );
            //这里需要android.permission.FOREGROUND_SERVICE权限，已添加到
            startForeground(MediaNotificationManager.NOTI_CTRL_ID, notification);
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

    /**
     * 这个方法在服务被中断后重启时会被执行，会在StartActivity中使用，每次更换节目，都会调用这个方法
     *                 Intent startIntent = new Intent(StartActivity.this, BMp3Service.class);
     *                 startIntent.putExtra("dbRecId", mp3Program.dbRecId);
     *                 ComponentName name = startService(startIntent);
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {
            int dbRecId = intent.getIntExtra("dbRecId", 0);
            if (this.dbRecId != dbRecId) {
                //保存旧节目，下面会载入新节目
                if(mp3Program!=null) {
                    mp3Program.postion = (int) audioPlayer.getCurrentPosition();
                    saveMp3Program();
                }

                this.dbRecId = dbRecId;
                Mp3RecDBManager db = new Mp3RecDBManager(this);
                //mp3Program必不为null，因为这是点击这个才来到这里的
                mp3Program = db.getMp3RecByDbRecId(dbRecId);
                db.close();

                saveCurMp3Program();

                //载入数据并播放
                isDowning = true;
                AmtbApi<FileListResult> api = new AmtbApi<>(UrlHelper.takeMp3FilesUrl(mp3Program.programListItem.identifier), new DownloadMp3FileListResult(mp3Program));
                api.execute(FileListResult.class);
            } else {
                //此处只有一种可能，即用户退出后又进入了播放界面
                if(mp3Program!=null && mp3s!=null)
                    if (bMp3ServiceListener != null)
                        bMp3ServiceListener.downloadMp3s(mp3Program, mp3s);
            }
        } else {
            // 播放服务由于内存紧张等原因暂停，当环境适合时，服务恢复时，读取节目信息
            mp3Program = takeCurMp3Program();
            if(mp3Program!=null && mp3Program.dbRecId>0){
                //载入数据并播放
                isDowning = true;
                AmtbApi<FileListResult> api = new AmtbApi<>(UrlHelper.takeMp3FilesUrl(mp3Program.programListItem.identifier), new DownloadMp3FileListResult(mp3Program));
                api.execute(FileListResult.class);
            }
        }
        return super.onStartCommand(intent, flags, startId);
        //return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId);
        //return START_NOT_STICKY;
    }

    /**
     * 载入数据
     */
    class DownloadMp3FileListResult implements AmtbApiCallBack<FileListResult> {
        //dMp3Program就是父类中的mp3Program，但也可能不同，比如正在下载时，又读取了新的mp3Program，则两个就不同了
        private Mp3Program dMp3Program;

        public DownloadMp3FileListResult(Mp3Program mp3Program){
            this.dMp3Program = mp3Program;
        }

        /**
         * 下载完后，异步执行
         * @param obj
         */
        @Override
        public void callBack(FileListResult obj) {

            //在下载时又列换了下载任务，直接返回就好了
            if(dMp3Program.dbRecId != mp3Program.dbRecId){
                return;
            }

            if (obj.isSucess) {
                //异步加载
                mp3s = obj.files;

                //记录，当前mp3s中保存的数据是的DbRecId
                mp3sDbRecId = dMp3Program.dbRecId;

                //一般不会出现这种情况，唯一可能出现的，是节目列表变了
                if(dMp3Program.curMediaIdx>=mp3s.size()){
                    dMp3Program.curMediaIdx = 0;
                    dMp3Program.postion = 0;
                    saveCurMp3Program();
                    saveMp3Program();
                }

                FileItem mp3 = obj.files.get(dMp3Program.curMediaIdx);
                String url = makeMp3Url(mp3.file);
                audioPlayer.play(url, dMp3Program.postion);

                //引发事件，更新UI
                if (bMp3ServiceListener != null)
                    bMp3ServiceListener.downloadMp3s(dMp3Program, mp3s);

                System.out.println(String.format("播放节目 %s : %d : %d", dMp3Program.programListItem.name, dMp3Program.curMediaIdx, mp3s.size()));
            } else {
                if(bMp3ServiceListener!=null)
                    bMp3ServiceListener.downloadMp3s(dMp3Program, null);
            }
            isDowning = false;
        }
    }

    /**
     * 在内存出紧张时，保存进度
     * @param level
     */
    @Override
    public void onTrimMemory(int level) {
        saveCurMp3Program();
        super.onTrimMemory(level);
    }

    @Override
    public void onDestroy() {
        //保存进度
        if(audioPlayer!=null) {
            if(mp3Program!=null) {
                mp3Program.postion = (int) audioPlayer.getCurrentPosition();
                saveMp3Program();
                saveCurMp3Program();
            }
            audioPlayer.stop();
            audioPlayer.release();
            //unregisterMp3Receiver();
            mediaNotificationManager.stopNotification();
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
            FileItem mp3 = mp3s.get(mp3Idx);
            mp3Program.curMediaIdx = mp3Idx;
            mp3Program.postion = 0;
            String url = makeMp3Url(mp3.file);
            audioPlayer.play(url, mp3Program.postion);

            //发送通知到 Notification 和 Activity
            if(bMp3ServiceListener!=null)
                bMp3ServiceListener.playChange(mp3Idx);
        }
    }

    //监听来电，来电时暂停，返回时继续播放
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
        return UrlHelper.makeMp3PlayUrl(mp3);
    }

    /**
     * 在暂停或更换了节目表时保存
     */
    private void saveMp3Program() {
        //获取进度，其它数据都在更改时自动获取
        Mp3RecDBManager db = new Mp3RecDBManager(this);
        db.update(mp3Program);
        db.close();
    }

    /**
     * 内存紧张时，保存
     */
    private void saveCurMp3Program(){
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, "CurMp3Program");
        sharedPreferencesHelper.put("CurMp3Program", mp3Program);
    }

    /**
     * 服务恢复时，读取节目信息
     * @return
     */
    private Mp3Program takeCurMp3Program(){
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this, "CurMp3Program");
        return sharedPreferencesHelper.getSharedPreference("CurMp3Program", Mp3Program.class);
    }
}
