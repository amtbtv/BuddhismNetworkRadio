package com.jianchi.fsp.buddhismnetworkradio.mp3;

import com.jianchi.fsp.buddhismnetworkradio.model.Program;

/**
 * Created by fsp on 17-8-7.
 * 显示于外部的MP3
 * 保存 名称，id，信息，下载量
 * 进度 文件名，位置
 * 最好存入sqlite中
 */

public class Mp3Program {

    public Mp3Program(){}

    //数据库ID
    public int dbRecId = -1;
    //播放进度时间
    public int postion = 0;
    //当前文件序号
    public int curMediaIdx = 0;
    //节目数据
    public Program programListItem;
}
