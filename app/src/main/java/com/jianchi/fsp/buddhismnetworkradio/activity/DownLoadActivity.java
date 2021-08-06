package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.FileItem;
import com.jianchi.fsp.buddhismnetworkradio.model.FileListResult;
import com.jianchi.fsp.buddhismnetworkradio.model.Program;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.jianchi.fsp.buddhismnetworkradio.tools.Tools;
import com.jianchi.fsp.buddhismnetworkradio.tools.UrlHelper;

import java.util.List;

public class DownLoadActivity extends AppCompatActivity {

    private static final int PROGRAM_PICKER_CODE = 3657;

    ListView listView;
    Button bt_share;
    ProgressBar proBar;
    List<FileItem> files;
    Program program;
    boolean downMp3;

    @Override
    protected void attachBaseContext(Context newBase) {
        Tools.changeAppLanguage(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        proBar = findViewById(R.id.progressBar);
        listView = (ListView) findViewById(R.id.listView);
        bt_share = (Button) findViewById(R.id.bt_share);
        bt_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(files!=null){
                    StringBuilder sb = new StringBuilder();
                    for(FileItem f : files){
                        sb.append(downMp3 ? UrlHelper.makeMp3PlayUrl(f.file) : UrlHelper.makeMp4PlayUrl(f.file)).append("\n");
                    }
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                    startActivity(Intent.createChooser(intent, getString(R.string.bt_share_title)));
                }
            }
        });

        Intent intent = new Intent(this, SelectProgramActivity.class);
        startActivityForResult(intent, PROGRAM_PICKER_CODE);
    }
    /**
     * 单选对话框
     */
    public void selectDownType() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.down_type_dialog_title);
        String[] cities = {getString(R.string.down_mp3_choice), getString(R.string.down_mp4_choice)};
        downMp3 = true;
        builder.setSingleChoiceItems(cities, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downMp3  = which == 0;
            }
        });
        //设置正面按钮
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadFiles();
                dialog.dismiss();
            }
        });

        //设置反面按钮
        builder.setNegativeButton(R.string.dialog_cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadFiles(){
        proBar.setVisibility(View.VISIBLE);
        String url = downMp3 ? UrlHelper.takeMp3FilesUrl(program.identifier) : UrlHelper.takeMp4FilesUrl(program.identifier);
        AmtbApi<FileListResult> api = new AmtbApi<>(url, new AmtbApiCallBack<FileListResult>() {
            @Override
            public void callBack(FileListResult obj) {
                proBar.setVisibility(View.GONE);
                if(obj.isSucess) {
                    files = obj.files;
                    listView.setAdapter(new ArrayAdapter<FileItem>(DownLoadActivity.this, android.R.layout.simple_list_item_1, obj.files));
                } else {
                    Toast.makeText(DownLoadActivity.this, obj.msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        api.execute(FileListResult.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PROGRAM_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK && intent.hasExtra("data")) {
                String json = intent.getExtras().getString("data");
                program = new Gson().fromJson(json, Program.class);

                if(program.mp3.equals("1") && program.mp4.equals("1")){
                    selectDownType();
                } else {
                    downMp3 = program.mp3.equals("1");
                    loadFiles();
                }

            } else {
                finish();
            }
        }
    }
}
