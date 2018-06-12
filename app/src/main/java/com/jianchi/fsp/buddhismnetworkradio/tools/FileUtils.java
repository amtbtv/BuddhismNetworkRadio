package com.jianchi.fsp.buddhismnetworkradio.tools;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fsp on 17-8-7.
 */

public class FileUtils {
    public static String readRawAllText(Context context, int rawId){
        StringBuilder sb = new StringBuilder();
        InputStream in = context.getResources().openRawResource(rawId);
        try {
            int blockLen = 10240;
            byte block[] = new byte[blockLen];
            int readLen = 0;
            while ((readLen = in.read(block)) != -1) {
                sb.append(new String(block,0,readLen));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static List<String> readRawAllLines(Context context, int rawId){
        List<String> lines = new ArrayList<>();
        InputStream in = context.getResources().openRawResource(rawId);
        BufferedReader dr = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = dr.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                dr.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
