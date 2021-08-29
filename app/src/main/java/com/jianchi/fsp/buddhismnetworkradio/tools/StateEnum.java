package com.jianchi.fsp.buddhismnetworkradio.tools;

import androidx.annotation.NonNull;

import org.cybergarage.http.HTTPStatus;
import org.cybergarage.upnp.UPnPStatus;

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
