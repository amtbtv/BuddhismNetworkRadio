package com.jianchi.fsp.buddhismnetworkradio.mp3service;

import com.jianchi.fsp.buddhismnetworkradio.mp3.Mp3Program;

import java.util.List;

public interface BMp3ServiceListener {
    void playChange(int index);
    void downloadMp3s(Mp3Program mp3Program, List<String> mp3s);
    void buffering();
    void ready();
}
