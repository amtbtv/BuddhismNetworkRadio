package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.model.FileListResult;
import com.jianchi.fsp.buddhismnetworkradio.mp3.AudioPlayer;
import com.jianchi.fsp.buddhismnetworkradio.mp3.MediaNotificationManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;

import java.util.List;

public class Mp3PlayerActivity extends AppCompatActivity {

    AudioPlayer audioPlayer;
    MediaNotificationManager mediaNotificationManager;
    //Mp3Receiver mp3Receiver;

    ListView lv;
    Mp3ListAdapter mp3ListAdapter;

    BApplication app;

    Mp3Program mp3Program;

    PlayerControlView playerControlView;

    ProgressBar proBar;

    Handler handler;

    AudioPlayer.EventListener audioPlayerEventListener = new AudioPlayer.EventListener() {
        @Override
        public void buffering() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    proBar.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void ready() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    proBar.setVisibility(View.INVISIBLE);
                }
            });
        }

        @Override
        public void ended() {
            //判断是否存在下一首歌
            List<String> mediaList = mp3ListAdapter.mediaList;
            if(mediaList.size()>mp3Program.curMediaIdx+1){
                mp3Program.curMediaIdx++;
                String mp3 = mediaList.get(mp3Program.curMediaIdx);
                mp3Program.postion = 0;
                audioPlayer.play(makeMp3Url(mp3), mp3Program.postion);
                mp3ListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void start() {
            mediaNotificationManager.startNotification(
                    mp3Program.programListItem.name,
                    mp3ListAdapter.mediaList.get(mp3Program.curMediaIdx)
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        proBar = (ProgressBar) findViewById(R.id.mp3ProBar);

        lv = (ListView) findViewById(R.id.lv_mp3);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mp3Program.curMediaIdx != position){
                    String mp3 = (String) view.getTag();
                    mp3Program.curMediaIdx = position;
                    mp3Program.postion = 0;
                    mp3ListAdapter.notifyDataSetChanged();
                    audioPlayer.play(makeMp3Url(mp3), mp3Program.postion);
                }
            }
        });

        playerControlView = (PlayerControlView) findViewById(R.id.playerControlView);
        playerControlView.setShowTimeoutMs(0);
        playerControlView.show();

        int dbRecId = getIntent().getIntExtra("dbRecId", 0);
        Mp3RecDBManager db = new Mp3RecDBManager();
        //mp3Program必不为null，因为这是点击这个才来到这里的
        mp3Program = db.getMp3RecByDbRecId(dbRecId);

        setTitle(TW2CN.getInstance(this).toLocal(mp3Program.programListItem.name));

        app = (BApplication)getApplication();

        //用于异步回调
        handler = new Handler();

        if(app.isNetworkConnected()){

            audioPlayer = new AudioPlayer(this, audioPlayerEventListener);
            mediaNotificationManager = new MediaNotificationManager(this);

            playerControlView.setPlayer(audioPlayer.getPlayer());

            TelephonyManager tm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

            //载入数据并播放
            proBar.setVisibility(View.VISIBLE);
            AmtbApi<FileListResult> api = new AmtbApi<>(AmtbApi.takeFilesUrl(mp3Program.programListItem.identifier), new AmtbApiCallBack<FileListResult>() {
                @Override
                public void callBack(FileListResult obj) {
                    proBar.setVisibility(View.GONE);
                    if(obj!=null) {
                        //异步加载
                        proBar.setVisibility(View.INVISIBLE);
                        mp3ListAdapter = new Mp3ListAdapter(Mp3PlayerActivity.this, mp3Program, obj.files);
                        lv.setAdapter(mp3ListAdapter);

                        if (obj.files.size() > mp3Program.curMediaIdx) {
                            String mp3 = obj.files.get(mp3Program.curMediaIdx);
                            String url = makeMp3Url(mp3);
                            audioPlayer.play(url, mp3Program.postion);
                        } else {
                            Toast.makeText(Mp3PlayerActivity.this, R.string.load_fail_reload, Toast.LENGTH_LONG).show();
                        }
                        mp3ListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(Mp3PlayerActivity.this, R.string.load_fail, Toast.LENGTH_LONG).show();
                    }
                }
            });
            api.execute(FileListResult.class);
        } else {
            networkFailClose();
        }

    }

    boolean waitPhoneIdleToPlaye = false;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存进度
        if(audioPlayer!=null) {
            audioPlayer.stop();
            audioPlayer.release();
            //unregisterMp3Receiver();
            mediaNotificationManager.stopNotification();
            saveMp3Program();
        }
    }

    /**
     * 在暂停或更换了节目表时保存
     */
    private void saveMp3Program() {
        //获取进度，其它数据都在更改时自动获取
        mp3Program.postion = (int) audioPlayer.getCurrentPosition();
        Mp3RecDBManager db = new Mp3RecDBManager();
        db.update(mp3Program);
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
}
