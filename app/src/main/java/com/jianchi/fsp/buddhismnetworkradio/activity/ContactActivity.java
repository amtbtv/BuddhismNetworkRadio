package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.tools.SharedPreferencesHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContactActivity extends BaseActivity {

    private static final int SDCARD_PERMISSION = 3655;
    private static final String MediaStoreSharedImageName = "BuddhismNetworkRadioSharedImage.png";

    BootstrapLabel version;

    BootstrapButton bt_site;
    BootstrapButton bt_facebook;
    BootstrapButton bt_xlwb;
    BootstrapButton bt_line;

    BootstrapButton bt_dzxx;

    BootstrapButton bt_invite_friends;

    ProgressBar proBar;

    @Override
    int getContentView() {
        return R.layout.activity_contact;
    }

    @Override
    void onCreateDo() {
        proBar = (ProgressBar) findViewById(R.id.proBar);
        version = (BootstrapLabel) findViewById(R.id.version);

        bt_site = (BootstrapButton) findViewById(R.id.bt_site);
        bt_facebook = (BootstrapButton) findViewById(R.id.bt_facebook);
        bt_xlwb = (BootstrapButton) findViewById(R.id.bt_xlwb);
        bt_line = (BootstrapButton) findViewById(R.id.bt_line);

        bt_dzxx = (BootstrapButton) findViewById(R.id.bt_dzxx);

        bt_invite_friends = (BootstrapButton) findViewById(R.id.bt_invite_friends);

        try {
            PackageInfo pi = getVersion();
            version.setText(pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version.setText("未知");
        }

        bt_site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.site_url));
            }
        });

        bt_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.facebook_url));
            }
        });

        bt_xlwb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.xlwb_url));
            }
        });

        bt_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl(getString(R.string.line_url));
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
                byte[] buffer = new byte[1024];
                int read;
                while((read = in.read(buffer)) != -1){
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (paper.exists()) {
            shareImage2(paper);
        } else {
            Toast.makeText(ContactActivity.this, R.string.share_amtb_png_fail, Toast.LENGTH_LONG).show();
        }
    }

    boolean findImage(Uri uri){
        boolean result = false;
        Cursor cursor = getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
        if ( null != cursor ) {
            if ( cursor.moveToFirst() ) {
                int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                if ( index > -1 ) {
                    String d = cursor.getString( index );
                    File file = new File(d);
                    if(file.exists()){
                        result = true;
                    } else {
                        getContentResolver().delete(uri, null, null);
                    }
                }
            }
            cursor.close();
        }
        return result;
    }

    Uri insertImage(File paper, SharedPreferencesHelper helper){
        Uri imageUri = null;
        try {
            String murl = MediaStore.Images.Media.insertImage(getContentResolver(), paper.getAbsolutePath(), MediaStoreSharedImageName, "图片: " + paper.getName());
            if(!murl.isEmpty()) {
                helper.putString("URI", murl);
                imageUri = Uri.parse(murl);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return imageUri;
    }

    void shareImage2(File paper){
        Uri imageUri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            imageUri = Uri.fromFile(paper);
        } else {
            SharedPreferencesHelper helper = new SharedPreferencesHelper(getThisActivity(), "shareimage");
            String murl = helper.getString("URI");

            if(murl.isEmpty()){
                imageUri = insertImage(paper, helper);
            } else {
                imageUri = Uri.parse(murl);
                if(!findImage(imageUri)){
                    imageUri = insertImage(paper, helper);
                }
            }
        }

        if(imageUri!=null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.app_name)));
        } else {
            Toast.makeText(ContactActivity.this, R.string.share_amtb_png_fail, Toast.LENGTH_LONG).show();
        }
    }
        /**
         * 获取当前APP版本号
         * @return
         */
    PackageInfo getVersion() throws PackageManager.NameNotFoundException {
        PackageManager manager = getPackageManager();
        PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
        return info;
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
        if (requestCode == SDCARD_PERMISSION) {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                shareImage();
            } else {
                Toast.makeText(getThisActivity(), R.string.share_need_storage_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
