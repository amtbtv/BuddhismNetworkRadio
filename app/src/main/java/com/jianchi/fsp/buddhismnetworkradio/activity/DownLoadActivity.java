package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapBadge;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.db.DownloadTaskInfoDBManager;
import com.jianchi.fsp.buddhismnetworkradio.model.FileListResult;
import com.jianchi.fsp.buddhismnetworkradio.model.Program;
import com.jianchi.fsp.buddhismnetworkradio.mp3.DownloadTaskInfo;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApi;
import com.jianchi.fsp.buddhismnetworkradio.tools.AmtbApiCallBack;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadSerialQueue;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownLoadActivity extends AppCompatActivity {

    enum  TaskState{
        未选择,等待开始,开始,出错,完成
    }
    private static final int PROGRAM_PICKER_CODE = 3657;
    private static final int SETTING_CODE = 3654;

    ListView listView;
    BootstrapButton bt_stop;
    BootstrapButton bt_start;

    DownloadTaskInfoDBManager db;
    List<DownloadTaskInfo> taskInfos;

    TaskInfoAdapter taskInfoAdapter;
    String folderLocation;

    DownloadSerialQueue serialQueue;

    String serverDomain = "";

    ProgressBar proBar;

    boolean delMenuShowing = false;
    List<DownloadTaskInfo> delTaskInfos;
    BootstrapButton bt_clean, bt_del_over, bt_del_selected;
    LinearLayout bottom_del_menu, bottom_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(delMenuShowing){
                    delMenuShowing = false;
                    bottom_del_menu.setVisibility(View.GONE);
                    //bottom_menu.setVisibility(View.VISIBLE);
                } else {
                    delMenuShowing = true;
                    bottom_del_menu.setVisibility(View.VISIBLE);
                    //bottom_menu.setVisibility(View.GONE);
                    stopDownload();
                    delTaskInfos = new ArrayList<>();
                    taskInfoAdapter.notifyDataSetChanged();
                }
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        bt_stop = (BootstrapButton) findViewById(R.id.bt_stop);
        bt_start = (BootstrapButton) findViewById(R.id.bt_start);

        bt_clean = (BootstrapButton) findViewById(R.id.bt_clean);
        bt_del_over = (BootstrapButton) findViewById(R.id.bt_del_over);
        bt_del_selected = (BootstrapButton) findViewById(R.id.bt_del_selected);

        bt_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.clean();
                taskInfos.clear();
                taskInfoAdapter.notifyDataSetChanged();
            }
        });
        bt_del_over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Iterator<DownloadTaskInfo> it = taskInfos.iterator();
                while (it.hasNext()){
                    DownloadTaskInfo d = it.next();
                    if(d.state.equals(TaskState.完成.toString())) {
                        db.del(d);
                        it.remove();
                    }
                }
                taskInfoAdapter.notifyDataSetChanged();
            }
        });
        bt_del_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(delTaskInfos.size()>0) {
                    for (DownloadTaskInfo d : delTaskInfos)
                        db.del(d);
                    taskInfos.removeAll(delTaskInfos);
                    taskInfoAdapter.notifyDataSetChanged();
                }
            }
        });

        bottom_del_menu = (LinearLayout) findViewById(R.id.bottom_del_menu);
        bottom_menu = (LinearLayout) findViewById(R.id.bottom_menu);

        db = new DownloadTaskInfoDBManager();
        taskInfos = db.getAll();
        taskInfoAdapter = new TaskInfoAdapter();
        listView.setAdapter(taskInfoAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DownloadTaskInfo d = (DownloadTaskInfo) view.getTag();
                if(delMenuShowing){
                    if(delTaskInfos.contains(d)){
                        delTaskInfos.remove(d);
                    } else {
                        delTaskInfos.add(d);
                    }
                    taskInfoAdapter.notifyDataSetChanged();
                } else {
                    if (d.checked.equals("F")) {
                        d.checked = "T";
                        d.state = TaskState.等待开始.toString();
                        db.update(d);
                        taskInfoAdapter.notifyDataSetChanged();
                        if (serialQueue != null) {
                            File parentFile = new File(folderLocation);
                            String url = makeMp3Url(d.fileName);
                            DownloadTask task = new DownloadTask.Builder(url, parentFile).build();
                            task.setTag(d);
                            serialQueue.enqueue(task);
                        }
                    } else {
                        if(serialQueue != null){
                            Toast.makeText(DownLoadActivity.this, R.string.stop_to_remove, Toast.LENGTH_LONG).show();
                        } else {
                            d.checked = "F";
                            d.state = TaskState.未选择.toString();
                            db.update(d);
                            taskInfoAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        folderLocation = readFolderLocation();
        proBar = (ProgressBar) findViewById(R.id.mp3ProBar);

        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDownload();
            }
        });

        if(!checkStoragePermission() || folderLocation.isEmpty()){
            showNeedSettingDialog();
        }

        bt_start.setEnabled(false);
        new LoadServerDomain().execute();
    }

    class LoadServerDomain extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("http://amtbsg.cloudapp.net/loadbalancer/amtbservers.php?singleserver=1&servertype=httpserver&mediatype=media&media=mp3").build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if(response.isSuccessful()) {
                    Pattern p = Pattern.compile("\"domain\":\"(.*?)\"");
                    Matcher m = p.matcher(response.body().string());
                    if(m.find()){
                        return "http://"+m.group(1)+"/media/mp3/";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "http://amtbsg.cloudapp.net/redirect/media/mp3/";
        }

        @Override
        protected void onPostExecute(String domain) {
            super.onPostExecute(domain);
            serverDomain = domain;
            bt_start.setEnabled(true);
        }
    }

    private void showNeedSettingDialog() {
        AlertDialog dialog = new AlertDialog.Builder(DownLoadActivity.this)
                .setIcon(R.mipmap.ic_launcher)//设置标题的图片
                .setTitle(R.string.need_setting_title)//设置对话框的标题
                .setMessage(R.string.need_setting_msg)//设置对话框的内容
                .setNegativeButton(R.string.dialog_cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(DownLoadActivity.this, DownloadSettingActivity.class);
                        startActivityForResult(intent, SETTING_CODE);
                    }
                }).create();
        dialog.show();
    }

    private void startDownload() {
        if(serialQueue==null) {
            File parentFile = new File(folderLocation);
            if (!parentFile.exists())
                parentFile.mkdirs();

            Util.enableConsoleLog();
            serialQueue = new DownloadSerialQueue(downloadListener);

            for(DownloadTaskInfo d : taskInfos){
                if(d.checked.equals("T")){
                    String url = makeMp3Url(d.fileName);
                    DownloadTask task = new DownloadTask.Builder(url, parentFile).build();
                    task.setTag(d);
                    serialQueue.enqueue(task);
                }
            }
            serialQueue.resume();
        }
    }

    private void stopDownload() {
        if (serialQueue != null) {
            serialQueue.shutdown();
            serialQueue = null;
        }
    }

    //由XML中的URL转为真实的URL  <fileurl>56k/12/12-017-0019.mp3</fileurl>
    private String makeMp3Url(String xmlFileUrl) {
        return serverDomain+xmlFileUrl;
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
        if (requestCode == SETTING_CODE) {
            folderLocation = readFolderLocation();
            if(!checkStoragePermission() || folderLocation.isEmpty()){
                showNeedSettingDialog();
            }
        } else if (requestCode == PROGRAM_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK && intent.hasExtra("data")) {
                String json = intent.getExtras().getString("data");
                final Program program = new Gson().fromJson(json, Program.class);

                proBar.setVisibility(View.VISIBLE);
                AmtbApi<FileListResult> api = new AmtbApi<>(AmtbApi.takeFilesUrl(program.identifier), new AmtbApiCallBack<FileListResult>() {
                    @Override
                    public void callBack(FileListResult obj) {
                        proBar.setVisibility(View.GONE);
                        if(obj!=null) {
                            for (String file : obj.files) {
                                DownloadTaskInfo taskInfo = new DownloadTaskInfo();
                                //56k/12/12-017-0016.mp3
                                String[] sp = file.split("-");
                                taskInfo.fileName = sp[0]+"/"+program.identifier+"/"+file;
                                taskInfo.state = TaskState.未选择.toString();
                                taskInfo.checked = "F";
                                //检测是否存在
                                for(DownloadTaskInfo d : taskInfos){
                                    if(d.fileName.equals(taskInfo.fileName))
                                        continue;
                                }
                                taskInfo.dbRecId = db.add(taskInfo);
                                taskInfos.add(taskInfo);
                            }
                            taskInfoAdapter.notifyDataSetChanged();
                            int count = obj.files.size();
                            Toast.makeText(
                                    DownLoadActivity.this,
                                    String.format(getResources().getString(R.string.load_download_list_over), count),
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            Toast.makeText(DownLoadActivity.this, R.string.load_fail, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                api.execute(FileListResult.class);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serialQueue!=null)
            serialQueue.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.download, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_download_mp3_program) {
            Intent intent = new Intent(DownLoadActivity.this, SelectProgramActivity.class);
            startActivityForResult(intent, PROGRAM_PICKER_CODE);
            return true;
        } else if(id == R.id.action_download_setting) {
            Intent intent = new Intent(DownLoadActivity.this, DownloadSettingActivity.class);
            startActivityForResult(intent, SETTING_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class TaskInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return taskInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return taskInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DownloadTaskInfo taskInfo = taskInfos.get(position);

            LayoutInflater inflater = DownLoadActivity.this.getLayoutInflater();
            if (convertView == null)
                convertView = inflater.inflate(R.layout.item_queue, null);
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            if(delMenuShowing){
                checkBox.setChecked(delTaskInfos.contains(taskInfo));
                convertView.setBackgroundResource(R.color.bootstrap_gray_lightest);
            } else {
                if (taskInfo.checked.equals("T")) { //未添加到数据库，即未被选择，选择的必定已经添加到了数据库
                    checkBox.setChecked(true);
                    convertView.setBackgroundResource(R.color.bootstrap_brand_warning);
                } else {
                    checkBox.setChecked(false);
                    convertView.setBackgroundResource(R.color.bootstrap_gray_lightest);
                }
            }

            TextView txt = convertView.findViewById(R.id.txt);
            BootstrapBadge badge = convertView.findViewById(R.id.badge);

            txt.setText(taskInfo.fileName);
            badge.setBadgeText(taskInfo.state);

            convertView.setTag(taskInfo);
            return convertView;
        }
    }

    DownloadListener downloadListener = new DownloadListener1() {
        @Override
        public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {
            DownloadTaskInfo taskInfo = (DownloadTaskInfo) task.getTag();
            taskInfo.state = TaskState.开始.toString();
            taskInfoAdapter.notifyDataSetChanged();
        }

        @Override
        public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {
            Log.i("DownloadListener", cause.toString());
        }

        @Override
        public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
            Log.i("DownloadListener", task.getFilename());
        }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
            DownloadTaskInfo taskInfo = (DownloadTaskInfo) task.getTag();
            taskInfo.state = String.valueOf(currentOffset*100/totalLength);
            taskInfoAdapter.notifyDataSetChanged();
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
            if(EndCause.COMPLETED == cause) {
                DownloadTaskInfo taskInfo = (DownloadTaskInfo) task.getTag();
                taskInfo.state = TaskState.完成.toString();
                taskInfo.checked = "F";
                db.update(taskInfo);

                taskInfoAdapter.notifyDataSetChanged();
            } else if(EndCause.ERROR == cause){
                DownloadTaskInfo taskInfo = (DownloadTaskInfo) task.getTag();
                taskInfo.state = TaskState.出错.toString();
                Toast.makeText(DownLoadActivity.this, R.string.download_error_retry, Toast.LENGTH_LONG).show();
                taskInfoAdapter.notifyDataSetChanged();
            }
        }
    };
}
