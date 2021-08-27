package com.jianchi.fsp.buddhismnetworkradio.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.model.FileItem;
import com.jianchi.fsp.buddhismnetworkradio.model.RenderTVItem;
import com.jianchi.fsp.buddhismnetworkradio.tools.UpnpDump;
import com.mikepenz.iconics.view.IconicsImageView;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

import java.util.ArrayList;
import java.util.List;

public class RenderTVActivity extends BaseActivity {
    RenderTVItem item;
    ProgressBar proBar;
    TextView item_name;
    ListView lv;
    UpnpDump upnpDump;
    List<Device> deviceList;
    DeviceAdapter adapter;

    @Override
    int getContentView() {
        return R.layout.activity_render_tv;
    }

    @Override
    void onCreateDo() {

        Intent intent = getIntent();
        String json = intent.getStringExtra("RenderTVItem");
        item = new Gson().fromJson(json, RenderTVItem.class);

        proBar = (ProgressBar) findViewById(R.id.proBar);
        item_name = (TextView) findViewById(R.id.item_name);
        lv = (ListView) findViewById(R.id.lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = deviceList.get(position);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UpnpDump.StateEnum stateEnum = upnpDump.play(item.mediaUrl, device, String.valueOf(position));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RenderTVActivity.this, stateEnum.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });
        item_name.setText(item.name);

        deviceList = new ArrayList<>();
        adapter = new DeviceAdapter();
        lv.setAdapter(adapter);

        upnpDump = new UpnpDump();
        upnpDump.addDeviceChangeListener(deviceChangeListener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                upnpDump.start();
            }
        }).start();

        Toast.makeText(this, R.string.wait_upnp, Toast.LENGTH_LONG).show();
    }

    void notifyDataSetChanged(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    DeviceChangeListener deviceChangeListener = new DeviceChangeListener() {
        @Override
        public void deviceRemoved(Device device) {
            if ("urn:schemas-upnp-org:device:MediaRenderer:1".equals(device.getDeviceType())) {
                deviceList.remove(device);
                notifyDataSetChanged();
            }
        }

        @Override
        public void deviceAdded(Device device) {
            // 判断是否为DMR
            if ("urn:schemas-upnp-org:device:MediaRenderer:1".equals(device.getDeviceType())) {
                deviceList.add(device);
                notifyDataSetChanged();
            }
        }
    };

    class DeviceAdapter extends BaseAdapter{

        LayoutInflater mInflater = getLayoutInflater();

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Device device = deviceList.get(position);
            //观察convertView随ListView滚动情况
            if(convertView==null)
                convertView = mInflater.inflate(R.layout.item_mp3, null);

            convertView.setTag(device);

            TextView txt = (TextView) convertView.findViewById(R.id.txt);
            txt.setText(device.getFriendlyName());

            return convertView;
        }
    }
}