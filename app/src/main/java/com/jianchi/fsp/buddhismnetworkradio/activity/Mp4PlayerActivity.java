package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.adapter.Mp3ListAdapter;
import com.jianchi.fsp.buddhismnetworkradio.db.Mp3RecDBManager;
import com.jianchi.fsp.buddhismnetworkradio.model.FileItem;
import com.jianchi.fsp.buddhismnetworkradio.model.FileListResult;
import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.TW2CN;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.List;

public class Mp4PlayerActivity extends AppCompatActivity {

    int dbRecId;
    Mp3Program mp3Program;

    ListView lv;
    Mp3ListAdapter mp3ListAdapter;

    Handler handler;
    BApplication app;
    ProgressBar proBar;
    List<FileItem> files;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp4_player);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (BApplication)getApplication();
        proBar = (ProgressBar) findViewById(R.id.mp3ProBar);

        //用于异步回调
        handler = new Handler();

        lv = (ListView) findViewById(R.id.lv_mp3);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileItem fileItem = (FileItem)view.getTag();
                Intent intent = new Intent(Mp4PlayerActivity.this, VideoPlayerActivity.class);
                intent.putExtra("url", fileItem.file);
                intent.putExtra("title", mp3Program.programListItem.name + String.valueOf(position+1));
                startActivity(intent);
            }
        });

        this.dbRecId = getIntent().getIntExtra("dbRecId", 0);
        Mp3RecDBManager db = new Mp3RecDBManager(this);
        this.mp3Program = db.getMp3RecByDbRecId(dbRecId);
        db.close();

        proBar.setVisibility(View.VISIBLE);
        AmtbApi<FileListResult> api = new AmtbApi<>(UrlHelper.takeMp4FilesUrl(mp3Program.programListItem.identifier), new AmtbApiCallBack<FileListResult>() {
            @Override
            public void callBack(FileListResult obj) {
                proBar.setVisibility(View.GONE);
                if(obj.isSucess) {
                    files = obj.files;
                    mp3ListAdapter = new Mp3ListAdapter(Mp4PlayerActivity.this, obj.files, 0);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setTitle(TW2CN.getInstance(Mp4PlayerActivity.this).toLocal(mp3Program.programListItem.name));
                            lv.setAdapter(mp3ListAdapter);
                            lv.setSelection(0);
                        }
                    });
                } else {
                    Toast.makeText(Mp4PlayerActivity.this, obj.msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        api.execute(FileListResult.class);
    }
}