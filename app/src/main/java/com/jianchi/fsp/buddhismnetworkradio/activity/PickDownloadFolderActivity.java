package com.jianchi.fsp.buddhismnetworkradio.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jianchi.fsp.buddhismnetworkradio.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PickDownloadFolderActivity extends AppCompatActivity {

    ArrayList<String> foldersList;

    File rootDir = Environment.getExternalStorageDirectory();
    File location;
    TextView tv_location;

    FolderAdapter folderAdapter;
    ListView listView;

    Button bt_new_folder;
    Button bt_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_download_folder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isExternalStorageReadable()) {
            Toast.makeText(this, R.string.no_storage_access_permission, Toast.LENGTH_LONG).show();
            finish();
        }
        listView = (ListView) findViewById(R.id.fp_listView);
        folderAdapter = new FolderAdapter();
        bt_new_folder = (Button) findViewById(R.id.bt_new_folder);
        bt_ok = (Button) findViewById(R.id.bt_ok);

        bt_new_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newFolderDialog();
            }
        });

        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                location = new File(location, foldersList.get(position));
                loadLists(location);
            }
        });

        tv_location = (TextView) findViewById(R.id.fp_tv_location);

        foldersList = new ArrayList<>();
        location = rootDir;
        loadLists(location);

        listView.setAdapter(folderAdapter);
    }


    /* 检测是否有权限 */
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //载入列表
    void loadLists(File location) {
        File folder = location;
        if (!folder.isDirectory())
            exit();

        tv_location.setText("Path: "+folder.getAbsolutePath().substring(rootDir.getAbsolutePath().length()));

        foldersList.clear();
        folderAdapter.notifyDataSetChanged();

        File[] files = folder.listFiles();
        for (File currentFile : files) {
            if (currentFile.isDirectory()) {
                foldersList.add(currentFile.getName());
            }
        }
        Collections.sort(foldersList, comparatorAscending);
        folderAdapter.notifyDataSetChanged();
    }

    //排序
    Comparator<String> comparatorAscending = new Comparator<String>() {
        @Override
        public int compare(String f1, String f2) {
            return f1.compareTo(f2);
        }
    };

    //返回上一级
    public void goBack() {
        if(location.getParentFile().getAbsolutePath().startsWith(rootDir.getAbsolutePath())){
            location = location.getParentFile();
            loadLists(location);
        }
    }

    //退出
    void exit(){
        Intent receivedIntent = getIntent();
        setResult(RESULT_CANCELED, receivedIntent);
        finish();
    }

    //创建目录
    void createNewFolder(String filename) {
        try {
            File file = new File(location + File.separator + filename);
            file.mkdirs();
            loadLists(location);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error:" + e.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    //创建目录对话框
    public void newFolderDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(R.string.create_folder_dialog_title);

        final EditText et = new EditText(this);
        dialog.setView(et);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.dialog_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        createNewFolder(et.getText().toString());
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.dialog_cancle),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        dialog.show();

    }

    //确定按扭被点击
    public void select() {
        Intent receivedIntent = getIntent();
        receivedIntent.putExtra("data", location.getAbsolutePath());
        setResult(RESULT_OK, receivedIntent);
        finish();
    }

    class FolderAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            if(foldersList==null)
                return 0;
            else
                return foldersList.size();
        }

        @Override
        public Object getItem(int position) {
            return foldersList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String folder = foldersList.get(position);
            //观察convertView随ListView滚动情况
            if(convertView==null) {
                LayoutInflater inflater = PickDownloadFolderActivity.this.getLayoutInflater();
                convertView = inflater.inflate(R.layout.item_folder, null);
            }
            TextView txt = (TextView) convertView.findViewById(R.id.txt);
            txt.setText(folder);
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_folder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_folder_up) {
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
