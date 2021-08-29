package com.jianchi.fsp.buddhismnetworkradio.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
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
import com.jianchi.fsp.buddhismnetworkradio.tools.StateEnum;
import com.jianchi.fsp.buddhismnetworkradio.tools.TransportState;
import com.jianchi.fsp.buddhismnetworkradio.tools.UpnpDump;
import com.mikepenz.iconics.IconicsColor;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.IconicsSizeDp;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.view.IconicsImageView;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RenderTVActivity extends BaseActivity {
    RenderTVItem item;
    ProgressBar proBar;
    TextView item_name;
    ListView lv;
    UpnpDump upnpDump;
    List<Device> deviceList;
    HashMap<Device, TransportState> deviceTransportStateHashMap;
    DeviceAdapter adapter;
    boolean renderIng = false;
    String tm = "00:00";

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
                TransportState state = deviceTransportStateHashMap.get(device);
                deviceList.clear();
                deviceTransportStateHashMap.clear();
                deviceList.add(device);
                deviceTransportStateHashMap.put(device, state);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(deviceTransportStateHashMap.get(device) == TransportState.STATE_NOT_RENDER){
                            StateEnum stateEnum = upnpDump.play(item.mediaUrl, device, String.valueOf(position));
                            deviceTransportStateHashMap.put(device, TransportState.STATE_WAITTING);
                            renderIng = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RenderTVActivity.this, stateEnum.toString(), Toast.LENGTH_LONG).show();
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            while (renderIng) {
                                updateState(device);
                            }
                        } else {
                            upnpDump.stop(device, String.valueOf(position));
                            renderIng = false;
                            deviceTransportStateHashMap.put(device, TransportState.STATE_NOT_RENDER);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        item_name.setText(item.name);

        deviceList = new ArrayList<>();
        deviceTransportStateHashMap = new HashMap<>();
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

    void updateState(Device device){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TransportState state = deviceTransportStateHashMap.get(device);
        if (state != TransportState.STATE_NOT_RENDER) {
            if(state == TransportState.STATE_PLAYING){
                tm = upnpDump.getPositionInfoSync(device);
            } else {
                state = upnpDump.getTransportStateSync(device);
                deviceTransportStateHashMap.put(device, state);
            }
            if(renderIng) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
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
                deviceTransportStateHashMap.remove(device);
                notifyDataSetChanged();
            }
        }

        @Override
        public void deviceAdded(Device device) {
            // 判断是否为DMR
            if ("urn:schemas-upnp-org:device:MediaRenderer:1".equals(device.getDeviceType())) {
                deviceList.add(device);
                deviceTransportStateHashMap.put(device, TransportState.STATE_NOT_RENDER);
                notifyDataSetChanged();
            }
        }
    };

    String TransportStateToString(TransportState state){
        String msg = "";
        switch (state){
            case STATE_ERR:
                msg = getString(R.string.render_err);
                break;
            case STATE_NOT_RENDER:
                msg = getString(R.string.no_render);
                break;
            case STATE_PLAYING:
                msg = tm;
                break;
            case STATE_NO_MEDIA_PRESENT:
                msg = getString(R.string.no_media);
                break;
            case STATE_PAUSED_PLAYBACK:
                msg = getString(R.string.paused_playback);
                break;
            case STATE_PAUSED_RECORDING:
                msg = getString(R.string.paused_recording);
                break;
            case STATE_RECORDING:
                msg = getString(R.string.recording);
                break;
            case STATE_STOPPED:
                msg = getString(R.string.stoped);
                break;
            case STATE_WAITTING:
                msg = getString(R.string.wait);
                break;
            case STATE_TRANSITIONING:
                msg = getString(R.string.transport);
                break;
        }
        return msg;
    }

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
                convertView = mInflater.inflate(R.layout.item_device, null);

            convertView.setTag(device);
            IconicsImageView icon = convertView.findViewById(R.id.icon);
            TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
            TextView info = convertView.findViewById(R.id.info);

            if(deviceTransportStateHashMap.get(device) == TransportState.STATE_NOT_RENDER){
                icon.setIcon(new IconicsDrawable(RenderTVActivity.this, FontAwesome.Icon.faw_play).size(new IconicsSizeDp(24)));
            } else {
                icon.setIcon(new IconicsDrawable(RenderTVActivity.this, FontAwesome.Icon.faw_stop).size(new IconicsSizeDp(24)));
            }

            device_name.setText(device.getFriendlyName());
            info.setText(TransportStateToString(deviceTransportStateHashMap.get(device)));

            return convertView;
        }
    }
}