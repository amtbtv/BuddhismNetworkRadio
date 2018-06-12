package com.jianchi.fsp.buddhismnetworkradio.mp3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.jianchi.fsp.buddhismnetworkradio.tools.MyLog;

/**
 * Created by fsp on 17-8-12.
 */

public class Mp3Receiver extends BroadcastReceiver {

    public interface EventListener {
        void actionToPause();
    }

    EventListener eventListener;
    public Mp3Receiver(EventListener eventListener){
        this.eventListener = eventListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Constant.PLAY_STOP_BUTTON)) {
            //用context获取service，然后控制service来停止音乐
            eventListener.actionToPause();
        } else if(action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
            String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            int state = telephony.getCallState();
            String TAG = "PhoneCall";
            switch(state){
                case TelephonyManager.CALL_STATE_RINGING:
                    eventListener.actionToPause();
                    MyLog.i(TAG, "[Broadcast]等待接电话="+phoneNumber);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
            }
        }
    }
}
