package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.model.FileItem;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3Service;
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3ServiceBinder;
import com.jianchi.fsp.buddhismnetworkradio.mp3service.BMp3ServiceListener;
import com.jianchi.fsp.buddhismnetworkradio.tools.SharedPreferencesHelper;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

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

    WebView webView;

    boolean isShowHtml = true;

    //Mp3Program mp3Program;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    BMp3ServiceListener bMp3ServiceListener = new BMp3ServiceListener() {
        @Override
        public void playChange(int index) {
            if(mp3ListAdapter!=null) {
                mp3ListAdapter.curMediaIdx = index;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mp3ListAdapter.notifyDataSetChanged();

                        FileItem fileItem = mp3ListAdapter.getCurFileItem();
                        if (fileItem.txt == 1) {
                            isShowHtml = true;
                            webView.setVisibility(isShowHtml ? View.VISIBLE : View.INVISIBLE);
                            String mp3FileName = fileItem.file;
                            String itemId = mp3FileName.substring(0, mp3FileName.length() - 4);
                            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(Mp3PlayerActivity.this, "setting");
                            String country = sharedPreferencesHelper.getString("local");
                            country = country.replace("\"", "");
                            if (country.equals("ZH")) country = "CN";
                            else country = "TW";
                            webView.loadUrl(UrlHelper.makeMp3DocUrl(itemId, country));
                        } else {
                            isShowHtml = false;
                            webView.setVisibility(isShowHtml ? View.VISIBLE : View.INVISIBLE);
                            Toast.makeText(Mp3PlayerActivity.this, R.string.no_doc, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        /**
         * 下载完数据，初始化列表和网页
         * 这里曾发生一个错误，原因应该是在注册BMp3ServiceListener后，直接返回了结果，但这时结果仍然在下载中，结果返回的是上一次的结果集。
         * @param mp3Program
         * @param mp3s
         */
        @Override
        public void downloadMp3s(Mp3Program mp3Program, List<FileItem> mp3s) {
            if(mp3s==null || mp3s.size()==0){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Mp3PlayerActivity.this, R.string.load_fail, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                //Mp3PlayerActivity.this.mp3Program = mp3Program;
                mp3ListAdapter = new Mp3ListAdapter(Mp3PlayerActivity.this, mp3s, mp3Program.curMediaIdx);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setTitle(TW2CN.getInstance(Mp3PlayerActivity.this).toLocal(mp3Program.programListItem.name));
                        lv.setAdapter(mp3ListAdapter);
                        lv.setSelection(mp3ListAdapter.curMediaIdx);

                        //这里会报错
                        FileItem fileItem = mp3ListAdapter.getCurFileItem();
                        if (fileItem.txt == 1) {
                            webView.setVisibility(isShowHtml ? View.VISIBLE : View.INVISIBLE);

                            String mp3FileName = fileItem.file;
                            String itemId = mp3FileName.substring(0, mp3FileName.length() - 4);

                            SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(Mp3PlayerActivity.this, "setting");
                            String country = sharedPreferencesHelper.getString("local");
                            if (country.equals("ZH")) country = "CN";
                            else country = "TW";

                            webView.loadUrl(UrlHelper.makeMp3DocUrl(itemId, country));
                        }

                        mp3ListAdapter.notifyDataSetChanged();
                        //载入结束
                        proBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
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
        webView = (WebView) findViewById(R.id.webView);
        webView.setVisibility(isShowHtml? View.VISIBLE : View.INVISIBLE);

        lv = (ListView) findViewById(R.id.lv_mp3);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                binder.playMp3(position);
                isShowHtml = !isShowHtml;
                webView.setVisibility(isShowHtml ? View.VISIBLE : View.INVISIBLE);
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
        } else if (id == R.id.action_show_html){
            FileItem fileItem = null;
            if(mp3ListAdapter!=null)
                fileItem = mp3ListAdapter.getCurFileItem();
            if(fileItem!=null && fileItem.txt==1) {
                isShowHtml = !isShowHtml;
                webView.setVisibility(isShowHtml ? View.VISIBLE : View.INVISIBLE);
            } else {
                isShowHtml = false;
                webView.setVisibility(isShowHtml ? View.VISIBLE : View.INVISIBLE);
                Toast.makeText(this, R.string.no_doc, Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
