package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.BApplication;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.tools.LanguageUtils;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;

import java.util.Locale;

public class DownloadSettingActivity extends PreferenceActivity {

    private static final int SDCARD_PERMISSION = 3655;
    private static final int FOLDER_PICKER_CODE = 3656;

    String folderLocation;
    private Toolbar mActionBar;

    Preference preference_storagePermission;
    Preference preference_folderLocation;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_download);
        mActionBar.setTitle(getTitle());

        folderLocation = readFolderLocation();

        preference_storagePermission = findPreference("storagePermission");
        preference_folderLocation = findPreference("folderLocation");

        if(checkStoragePermission())
            preference_storagePermission.setSummary(R.string.has_set);
        if(!folderLocation.isEmpty())
            preference_folderLocation.setSummary(folderLocation);

        preference_storagePermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(checkStoragePermission()) {
                    Toast.makeText(DownloadSettingActivity.this, R.string.has_set, Toast.LENGTH_SHORT).show();
                } else {
                    requestStoragePermission();
                }
                return true;
            }
        });

        preference_folderLocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(checkStoragePermission()){
                    Intent intent = new Intent(DownloadSettingActivity.this, PickDownloadFolderActivity.class);
                    startActivityForResult(intent, FOLDER_PICKER_CODE);
                } else {
                    Toast.makeText(DownloadSettingActivity.this, R.string.need_request_storage_permission, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }



    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.settings_activity, new LinearLayout(this), false);

        mActionBar = (Toolbar) contentView.findViewById(R.id.action_bar);
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content_wrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    private void writeFolderLocation(String folderLocation) {
        SharedPreferences.Editor editor = getSharedPreferences("downloadsetting", MODE_PRIVATE).edit();
        editor.putString("folderLocation", folderLocation);
        editor.commit();
    }

    private String readFolderLocation() {
        SharedPreferences read = getSharedPreferences("downloadsetting", MODE_PRIVATE);
        return read.getString("folderLocation", "");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FOLDER_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK && intent.hasExtra("data")) {
                folderLocation = intent.getExtras().getString("data");
                writeFolderLocation(folderLocation);
                preference_folderLocation.setSummary(folderLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SDCARD_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    preference_storagePermission.setSummary(R.string.has_request_permission);
                }
                return;
            }
        }
    }
}
