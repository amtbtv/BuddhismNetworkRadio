package com.jianchi.fsp.buddhismnetworkradio.tools;

import androidx.annotation.NonNull;

import org.cybergarage.http.HTTPStatus;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.ssdp.*;
import org.cybergarage.upnp.device.*;
import org.cybergarage.upnp.event.*;
import org.cybergarage.util.Debug;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.Parser;
import org.cybergarage.xml.ParserException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpnpDump extends ControlPoint implements NotifyListener, EventListener, SearchResponseListener {
    private static final String AVTransport1 = "urn:schemas-upnp-org:service:AVTransport:1";


    private TransportState formatTransportState(String value) {
        if ("STOPPED".equals(value)) {
            return TransportState.STATE_STOPPED;
        }
        if ("PLAYING".equals(value)) {
            return TransportState.STATE_PLAYING;
        }
        if ("TRANSITIONING".equals(value)) {
            return TransportState.STATE_TRANSITIONING;
        }
        if ("PAUSED_PLAYBACK".equals(value)) {
            return TransportState.STATE_PAUSED_PLAYBACK;
        }
        if ("PAUSED_RECORDING".equals(value)) {
            return TransportState.STATE_PAUSED_RECORDING;
        }
        if ("RECORDING".equals(value)) {
            return TransportState.STATE_RECORDING;
        }
        if ("NO_MEDIA_PRESENT".equals(value)) {
            return TransportState.STATE_NO_MEDIA_PRESENT;
        }
        return TransportState.STATE_ERR;
    }

    public synchronized TransportState getTransportStateSync(Device device) {
        Service localService = device.getService(AVTransport1);
        if (localService == null) {
            return formatTransportState(null);
        }

        final Action localAction = localService.getAction("GetTransportInfo");
        if (localAction == null) {
            return formatTransportState(null);
        }

        localAction.setArgumentValue("InstanceID", "0");

        if (localAction.postControlAction()) {
            String value = localAction.getArgumentValue("CurrentTransportState");
            System.out.println("current state:" + value);
            return formatTransportState(localAction.getArgumentValue("CurrentTransportState"));
        } else {
            return formatTransportState(null);
        }
    }

    public synchronized String getPositionInfoSync(Device device) {
        Service localService = device.getService(AVTransport1);

        if (localService == null)
            return null;

        final Action localAction = localService.getAction("GetPositionInfo");
        if (localAction == null) {
            return null;
        }

        localAction.setArgumentValue("InstanceID", "0");
        boolean isSuccess = localAction.postControlAction();
        if (isSuccess) {
            return localAction.getArgumentValue("AbsTime");
        } else {
            return null;
        }
    }


    private synchronized String getMediaDurationSync(Device device) {
        Service localService = device.getService(AVTransport1);
        if (localService == null) {
            return null;
        }

        final Action localAction = localService.getAction("GetMediaInfo");
        if (localAction == null) {
            return null;
        }

        localAction.setArgumentValue("InstanceID", "0");
        if (localAction.postControlAction()) {
            return localAction.getArgumentValue("MediaDuration");
        } else {
            return null;
        }

    }

    public List<Device> getTvDeviceList() {
        return tvDeviceList;
    }

    List<Device> tvDeviceList;


    ////////////////////////////////////////////////
    //	Constractor
    ////////////////////////////////////////////////

    public UpnpDump() {
        tvDeviceList = new ArrayList<>();
        addNotifyListener(this);
        addSearchResponseListener(this);
        addEventListener(this);
    }


    ////////////////////////////////////////////////
    //	Listener
    ////////////////////////////////////////////////

    public void deviceNotifyReceived(SSDPPacket ssdpPacket) {
        System.out.println(ssdpPacket.toString());

        if (ssdpPacket.isDiscover() == true) {
            String st = ssdpPacket.getST();
            System.out.println("ssdp:discover : ST = " + st);
            String location = ssdpPacket.getLocation();
            //getXml(location, ssdpPacket);
        } else if (ssdpPacket.isAlive() == true) {
            String usn = ssdpPacket.getUSN();
            String nt = ssdpPacket.getNT();
            String location = ssdpPacket.getLocation();
            System.out.println("ssdp:alive : uuid = " + usn + ", NT = " + nt + ", location = " + location);

        } else if (ssdpPacket.isByeBye() == true) {
            String usn = ssdpPacket.getUSN();
            String nt = ssdpPacket.getNT();
            System.out.println("ssdp:byebye : uuid = " + usn + ", NT = " + nt);
        }
    }

    public void deviceSearchResponseReceived(SSDPPacket packet) {
        String uuid = packet.getUSN();
        String st = packet.getST();
        String url = packet.getLocation();
        System.out.println("device search res : uuid = " + uuid + ", ST = " + st + ", location = " + url);
    }

    public void eventNotifyReceived(String uuid, long seq, String name, String value) {
        System.out.println("event notify : uuid = " + uuid + ", seq = " + seq + ", name = " + name + ", value =" + value);
    }

    public synchronized boolean stop(Device device, String instanceID){
        Service avTransService = device.getService(AVTransport1);
        if(avTransService==null) return false;
        Action action = avTransService.getAction("Stop");
        if(action==null) return false;
        action.setArgumentValue("InstanceID", instanceID);
        return action.postControlAction();
    }

    public synchronized StateEnum play(String mp4Url, Device device, String instanceID){
        stop(device, instanceID);
        // 获取服务
        Service service = device.getService(AVTransport1);
        // 获取动作
        Action transportAction = service.getAction( "SetAVTransportURI");

        // 设置参数
        transportAction.setArgumentValue("InstanceID", instanceID);
        transportAction.setArgumentValue("CurrentURI", mp4Url);

        // SetAVTransportURI
        if(transportAction.postControlAction()) {
            // 成功
            Action playAction = service.getAction("Play");
            playAction.setArgumentValue("InstanceID", instanceID);
            // Play
            if (playAction.postControlAction()) {
                return StateEnum.SUCESS;
            } else {
                return StateEnum.getState(transportAction.getStatus().getCode());
            }
        } else {
            // 失败
            return StateEnum.getState(transportAction.getStatus().getCode());
        }
    }
}
