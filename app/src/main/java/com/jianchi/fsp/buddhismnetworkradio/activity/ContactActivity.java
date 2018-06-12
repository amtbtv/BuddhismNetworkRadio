package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ContactActivity extends AppCompatActivity {

    BootstrapLabel version;

    BootstrapButton bt_site;
    BootstrapButton bt_pl;
    BootstrapButton bt_xlwb;
    BootstrapButton bt_txwb;

    BootstrapButton bt_dzxx;

    BootstrapButton bt_invite_friends;

    BApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (BApplication) getApplication();

        version = (BootstrapLabel) findViewById(R.id.version);

        bt_site = (BootstrapButton) findViewById(R.id.bt_site);
        bt_pl = (BootstrapButton) findViewById(R.id.bt_pl);
        bt_xlwb = (BootstrapButton) findViewById(R.id.bt_xlwb);
        bt_txwb = (BootstrapButton) findViewById(R.id.bt_txwb);

        bt_dzxx = (BootstrapButton) findViewById(R.id.bt_dzxx);

        bt_invite_friends = (BootstrapButton) findViewById(R.id.bt_invite_friends);

        PackageInfo pi = getVersion();
        if(pi!=null)
            version.setText(pi.versionName);
        else
            version.setText("2.0.2");

        bt_site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.site_url));
            }
        });

        bt_pl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.pl_url));
            }
        });

        bt_xlwb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.xlwb_url));
            }
        });

        bt_txwb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.txwb_url));
            }
        });

        bt_invite_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享信息
                File initDir = getDiskCacheDir("init");
                if(!initDir.exists()){
                    initDir.mkdir();
                }
                File paper = new File(initDir, "paper.png");
                if(!paper.exists()){
                    try {
                        InputStream in = getResources().openRawResource(R.raw.amtbpng);
                        FileOutputStream out = new FileOutputStream(paper);
                        copyFile(in, out);
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Uri imageUri = Uri.fromFile(paper);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.app_name)));
            }
        });

    }

    public void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public File getDiskCacheDir(String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = getExternalCacheDir().getPath();
        } else {
            cachePath = getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
        /**
         * 获取当前APP版本号
         * @return
         */
    PackageInfo getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打开网址
     * @return
     */
    void openUrl(String url){
        Uri uri = Uri.parse(url);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it);
    }
}
