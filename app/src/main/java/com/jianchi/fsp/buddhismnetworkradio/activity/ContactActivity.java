package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ContactActivity extends AppCompatActivity {

    private static final int SDCARD_PERMISSION = 3655;
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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    shareImage();
                } else {
                    if(checkStoragePermission()) {
                        shareImage();
                    } else {
                        requestStoragePermission();
                    }
                }
            }
        });

    }

    void shareImage() {
        //分享信息
        File initDir = new File(getCacheDir(), "init");
        if (!initDir.exists()) {
            initDir.mkdir();
        }
        File paper = new File(initDir, "paper.png");
        if (!paper.exists()) {
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
        Uri imageUri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            imageUri = Uri.fromFile(paper);
        } else {
            try {
                String murl = MediaStore.Images.Media.insertImage(getContentResolver(), paper.getAbsolutePath(), UUID.randomUUID().toString() + ".png", "图片: " + paper.getName());
                imageUri = Uri.parse(murl);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.app_name)));
    }

    public void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
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


    void requestStoragePermission() {
        if (!checkStoragePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    SDCARD_PERMISSION);
        }
    }

    boolean checkStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SDCARD_PERMISSION: {
                shareImage();
                return;
            }
        }
    }
}
