package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.AudioPlayer;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Constant;
import com.jianchi.fsp.buddhismnetworkradio.mp3.MediaNotificationManager;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Receiver;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbQuery;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.MediaList;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.MediaListItem;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.MediaListResult;
import com.jianchi.fsp.buddhismnetworkradio.xmlbean.VolListItem;

import java.util.ArrayList;
import java.util.List;

public class Mp3PlayerActivity extends AppCompatActivity {

    AudioPlayer audioPlayer;
    MediaNotificationManager mediaNotificationManager;
    Mp3Receiver mp3Receiver;

    ExpandableListView lv;
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
            MediaList mediaList = mp3ListAdapter.mediaListHashMap.get(mp3Program.curVolIdx);
            if(mp3Program.curMediaIdx<mediaList.getItem().size()-1){
                mp3Program.curMediaIdx++;
                MediaListItem mediaListItem = mediaList.getItem().get(mp3Program.curMediaIdx);
                mp3Program.postion = 0;
                audioPlayer.play(makeMp3Url(mediaListItem.getFileurl()), mp3Program.postion);
                mp3ListAdapter.notifyDataSetChanged();
            } else {
                //判断是否存在下一个vol
                if(mp3Program.curVolIdx < mp3ListAdapter.volList.size()-1){
                    mp3Program.curVolIdx++;
                    mp3Program.curVol = mp3ListAdapter.volList.get(mp3Program.curVolIdx).getItem().getVolid();
                    mp3Program.curMediaIdx = 0;
                    mp3Program.postion = 0;

                    MediaList nextMediaList = mp3ListAdapter.mediaListHashMap.get(mp3Program.curVolIdx);
                    //如果下一个列表已经加载了，直接播放就好了
                    if(nextMediaList!=null){
                        MediaListItem mediaListItem = nextMediaList.getItem().get(mp3Program.curMediaIdx);
                        audioPlayer.play(makeMp3Url(mediaListItem.getFileurl()), mp3Program.postion);
                        mp3ListAdapter.notifyDataSetChanged();
                    } else {
                        //没有加载，异步加载并播放
                        new LoadMediaListResult(mp3Program.curVolIdx, mp3Program.curVol, true).execute();
                    }
                }
            }
        }

        @Override
        public void start() {
            mediaNotificationManager.startNotification(
                    mp3Program.programListItem.getLecturename(),
                    ((MediaListItem) mp3ListAdapter.getChild(mp3Program.curVolIdx, mp3Program.curMediaIdx)).getFileno()
            );
        }
    };

    Mp3Receiver.EventListener mp3RecevierEventListener = new Mp3Receiver.EventListener() {
        @Override
        public void actionToPause() {
            audioPlayer.pause();
        }
    };

    private void registerMp3Receiver(){
        //通知广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.PLAY_STOP_BUTTON);
        registerReceiver(mp3Receiver, intentFilter);

        //来电广播
        IntentFilter phoneStateIntentFilter = new IntentFilter();
        phoneStateIntentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mp3Receiver, phoneStateIntentFilter);
    }
    private void unregisterMp3Receiver(){
        unregisterReceiver(mp3Receiver);
    }

    class LoadMediaListResult extends AsyncTask<Integer, Integer, MediaListResult> {
        int volidIdx;
        int volid;
        boolean toPlay;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            proBar.setVisibility(View.VISIBLE);
        }

        public LoadMediaListResult(int volidIdx, int volid, boolean toPlay){
            this.toPlay = toPlay;
            this.volid = volid;
            this.volidIdx = volidIdx;
        }

        @Override
        protected MediaListResult doInBackground(Integer... integers) {
            if (volid == -1) { //说明是第一次载入
                return AmtbQuery.queryMediaListResult(
                        mp3Program.programListItem.getAmtbid(),
                        mp3Program.programListItem.getSubamtbid(),
                        mp3Program.programListItem.getLectureid());
            } else {
                return AmtbQuery.queryMediaListResult(
                        mp3Program.programListItem.getAmtbid(),
                        mp3Program.programListItem.getSubamtbid(),
                        mp3Program.programListItem.getLectureid(),
                        volid);
            }
        }

        @Override
        protected void onPostExecute(MediaListResult mediaListResult) {
            super.onPostExecute(mediaListResult);

            //异步加载
            proBar.setVisibility(View.INVISIBLE);

            if(mediaListResult==null){
                Toast.makeText(Mp3PlayerActivity.this, R.string.load_nothing, Toast.LENGTH_LONG).show();
            } else {
                List<MediaListItem> mediaListItemList = new ArrayList<>();
                for(MediaListItem item : mediaListResult.getList().getItem()){
                    if(item.getFiletype().equals("mp3")){
                        mediaListItemList.add(item);
                    }
                }
                MediaList mediaList = new MediaList();
                mediaList.setItem(mediaListItemList);

                //首次加载，载入数据即可
                if (mp3ListAdapter == null) {
                    mp3ListAdapter = new Mp3ListAdapter(Mp3PlayerActivity.this, mp3Program, mediaListResult.getVollist(), volidIdx, mediaList);
                    lv.setAdapter(mp3ListAdapter);
                } else {
                    mp3ListAdapter.mediaListHashMap.put(volidIdx, mediaList);
                }

                lv.expandGroup(volidIdx);

                if (toPlay) {
                    MediaListItem mediaListItem = mediaList.getItem().get(mp3Program.curMediaIdx);
                    String url = makeMp3Url(mediaListItem.getFileurl());
                    audioPlayer.play(url, mp3Program.postion);
                    mp3ListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        proBar = (ProgressBar) findViewById(R.id.mp3ProBar);

        lv = (ExpandableListView) findViewById(R.id.lv_mp3);
        lv.setOnGroupClickListener(groupClickListener);
        lv.setOnChildClickListener(childClickListener);

        playerControlView = (PlayerControlView) findViewById(R.id.playerControlView);
        playerControlView.setShowTimeoutMs(0);
        playerControlView.show();

        int dbRecId = getIntent().getIntExtra("dbRecId", 0);
        Mp3RecDBManager db = new Mp3RecDBManager();
        //mp3Program必不为null，因为这是点击这个才来到这里的
        mp3Program = db.getMp3RecByDbRecId(dbRecId);

        setTitle(TW2CN.getInstance(this).toLocal(mp3Program.programListItem.getLecturename()));

        app = (BApplication)getApplication();
        if(app.isNetworkConnected()){
            //用于异步回调
            handler = new Handler();

            audioPlayer = new AudioPlayer(this, audioPlayerEventListener);
            mp3Receiver = new Mp3Receiver(mp3RecevierEventListener);
            registerMp3Receiver();
            mediaNotificationManager = new MediaNotificationManager(this);

            playerControlView.setPlayer(audioPlayer.getPlayer());

            //载入数据并播放
            new LoadMediaListResult(mp3Program.curVolIdx, mp3Program.curVol, true).execute();
        } else {
            networkFailClose();
        }

    }

    ExpandableListView.OnGroupClickListener groupClickListener = new ExpandableListView.OnGroupClickListener() {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            //如果分组被打开 直接关闭
            if (lv.isGroupExpanded(groupPosition) ) {
                lv.collapseGroup(groupPosition);
            } else {
                if(mp3ListAdapter.mediaListHashMap.containsKey(groupPosition)){
                    lv.expandGroup(groupPosition);
                } else {
                    int volid = mp3ListAdapter.volList.get(groupPosition).getItem().getVolid();
                    new LoadMediaListResult(groupPosition, volid, false).execute();
                }
            }
            //返回false表示系统自己处理展开和关闭事件 返回true表示调用者自己处理展开和关闭事件
            return true;
        }
    };

    ExpandableListView.OnChildClickListener childClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if(mp3Program.curMediaIdx != childPosition || mp3Program.curVolIdx != groupPosition){
                MediaListItem mediaListItem = (MediaListItem) v.getTag();
                mp3Program.curMediaIdx = childPosition;
                mp3Program.curVolIdx = groupPosition;
                mp3Program.curVol = mp3ListAdapter.volList.get(groupPosition).getItem().getVolid();
                mp3Program.postion = 0;
                mp3ListAdapter.notifyDataSetChanged();
                audioPlayer.play(makeMp3Url(mediaListItem.getFileurl()), mp3Program.postion);
            }
            return true;
        }
    };

    //由XML中的URL转为真实的URL  <fileurl>56k/12/12-017-0019.mp3</fileurl>
    private String makeMp3Url(String xmlFileUrl) {
        //                  http://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/02/02-041/02-041-0001.mp3/playlist.m3u8
        //                  http://amtbsg.cloudapp.net/redirect/media/mp3/02/02-041/02-041-0001.mp3
        //String urlFormat = "http://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/%s/%s/%s/playlist.m3u8";
        String urlFormat = "http://amtbsg.cloudapp.net/redirect/media/mp3/%s/%s/%s";
        String[] sp = xmlFileUrl.split("/");
        String url = String.format(urlFormat, sp[1], mp3Program.programListItem.getLectureno(), sp[2]);
        return url;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存进度
        audioPlayer.stop();
        audioPlayer.release();
        unregisterMp3Receiver();
        mediaNotificationManager.stopNotification();
        saveMp3Program();
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
