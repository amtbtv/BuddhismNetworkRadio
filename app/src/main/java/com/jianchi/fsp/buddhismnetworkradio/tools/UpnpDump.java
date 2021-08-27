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

    public enum StateEnum
    {
        //添加枚举的指定常量
        INVALID_ACTION(UPnPStatus.INVALID_ACTION),
        INVALID_ARGS(UPnPStatus.INVALID_ARGS),
        OUT_OF_SYNC(UPnPStatus.OUT_OF_SYNC),
        INVALID_VAR(UPnPStatus.INVALID_VAR),
        PRECONDITION_FAILED(UPnPStatus.PRECONDITION_FAILED),
        ACTION_FAILED(UPnPStatus.ACTION_FAILED),
        SUCESS(200),
        UNKNOW(0);

        //必须增加一个构造函数,变量,得到该变量的值
        private int  mState=0;
        private StateEnum(int value)
        {
            mState=value;
        }
        /**
         * @return 枚举变量实际返回值
         */
        public static StateEnum getState(int v)
        {
            switch (v) {
                case UPnPStatus.INVALID_ACTION: return INVALID_ACTION;
                case UPnPStatus.INVALID_ARGS: return INVALID_ARGS;
                case UPnPStatus.OUT_OF_SYNC: return OUT_OF_SYNC;
                case UPnPStatus.INVALID_VAR: return INVALID_VAR;
                case UPnPStatus.PRECONDITION_FAILED: return PRECONDITION_FAILED;
                case UPnPStatus.ACTION_FAILED: return ACTION_FAILED;
                case 200: return SUCESS;
                default: {
                    StateEnum e = SUCESS;
                    e.mState = v;
                    return e;
                }
            }
        }

        @NonNull
        @Override
        public String toString() {
            switch (mState) {
                case UPnPStatus.INVALID_ACTION: return "Invalid Action";
                case UPnPStatus.INVALID_ARGS: return "Invalid Args";
                case UPnPStatus.OUT_OF_SYNC: return "Out of Sync";
                case UPnPStatus.INVALID_VAR: return "Invalid Var";
                case UPnPStatus.PRECONDITION_FAILED: return "Precondition Failed";
                case UPnPStatus.ACTION_FAILED: return "Action Failed";
                case 200: return "Sucess";
                default: return HTTPStatus.code2String(mState);
            }
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

    public void stop(Device device, String instanceID){
        Service avTransService = device.getService("urn:schemas-upnp-org:service:AVTransport:1");
        if(avTransService==null) return;
        Action action = avTransService.getAction("Stop");
        if(action==null) return;
        action.setArgumentValue("InstanceID", instanceID);
        action.postControlAction();
    }

    public StateEnum play(String mp4Url, Device device, String instanceID){
        stop(device, instanceID);
        // 获取服务
        Service service = device.getService("urn:schemas-upnp-org:service:AVTransport:1");
        // 获取动作
        Action transportAction = service.getAction("SetAVTransportURI");
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
