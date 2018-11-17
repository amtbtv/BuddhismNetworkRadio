package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
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
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3Service;
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3ServiceBinder;
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3ServiceListener;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;

import java.util.List;

/**
 * 将本类的方法全部转移到BMp3Service中，然后通过Binder与BMp3Service连接，获取数据，并绑定播放进度
 * 在本Activity中，接收来自状态栏的通知
 */
public class Mp3PlayerActivity extends AppCompatActivity {
    ListView lv;
    Mp3ListAdapter mp3ListAdapter;

    BApplication app;

    PlayerControlView playerControlView;

    ProgressBar proBar;

    Handler handler;

    BMp3ServiceBinder binder;

    BMp3ServiceListener bMp3ServiceListener = new BMp3ServiceListener() {
        @Override
        public void playChange(final int index) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mp3ListAdapter.curMediaIdx = index;
                    mp3ListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void downloadMp3s(final Mp3Program mp3Program, final List<String> mp3s) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setTitle(TW2CN.getInstance(Mp3PlayerActivity.this).toLocal(mp3Program.programListItem.name));
                    mp3ListAdapter = new Mp3ListAdapter(Mp3PlayerActivity.this, mp3s, mp3Program.curMediaIdx);
                    lv.setAdapter(mp3ListAdapter);

                    //载入结束
                    proBar.setVisibility(View.INVISIBLE);
                }
            });
        }

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
                binder.playMp3(position);
            }
        });

        playerControlView = (PlayerControlView) findViewById(R.id.playerControlView);
        playerControlView.setShowTimeoutMs(0);
        playerControlView.show();

        app = (BApplication)getApplication();

        //用于异步回调
        handler = new Handler();

        //载入开始
        proBar.setVisibility(View.VISIBLE);
        Intent bindIntent = new Intent(this, BMp3Service.class);
        bindService(bindIntent, bMp3ServiceConnection, this.BIND_AUTO_CREATE);
    }

    ServiceConnection bMp3ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (BMp3ServiceBinder) service;
            binder.setOnBMp3ServiceListener(bMp3ServiceListener);
            playerControlView.setPlayer(binder.getPlayer());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bMp3ServiceConnection!=null) {
            binder.setOnBMp3ServiceListener(null);
            unbindService(bMp3ServiceConnection);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stopservice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_stop_service) {
            if(bMp3ServiceConnection!=null) {
                binder.setOnBMp3ServiceListener(null);
                unbindService(bMp3ServiceConnection);
            }
            bMp3ServiceConnection = null;

            Intent stopIntent = new Intent(this, BMp3Service.class);
            stopService(stopIntent);

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
