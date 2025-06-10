package com.tencent.yolo11ncnn;

import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileTool {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveFile(String path, String content) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(content.getBytes());
            outStream.close();
        } catch (Exception e) {
            Log.e("FileTool","saveFile error:"+e.getMessage());
        }
    }
    @SuppressWarnings("unused,ResultOfMethodCallIgnored")
    //追加文件
    public static void appendFile(String path, String content) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file,true);
            outStream.write(content.getBytes());
            outStream.close();
        } catch (Exception e) {
            Log.e("FileTool","appendFile error:"+e.getMessage());
        }
    }
    public static boolean saveStream(InputStream in, String path){
        //保存in流到path指定文件
        try {

            FileOutputStream outStream = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int len;
            while((len = in.read(buffer))!=-1){
                outStream.write(buffer,0,len);
            }
            safelyClose(in);
            return true;
        } catch (Exception e) {
            Log.e("FileTool","saveStream error:"+e.getMessage());
            return false;
        }
    }


    @SuppressWarnings("unused")
    public static boolean mkdirs(String savePath) {
        if(!new File(savePath).exists()){
            return new File(savePath).mkdirs();
        }else return true;
    }

    public static String readFile(String path){
        try {
            return readAll(new FileInputStream(path),"utf-8");
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String head(File f, int lines) {
        //读取文件的前lines行
        try {
            FileInputStream in = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<lines;i++){
                String line = br.readLine();
                if(line == null) break;
                sb.append(line).append("\n");
            }
            br.close();
            in.close();
            return sb.toString();
        } catch (Exception e) {
            Log.e("FileTool","error:"+e.getMessage());
            return null;
        }
    }
    @SuppressWarnings("EmptyCatchBlock")
    public static void safelyClose(AutoCloseable obj) {
        if(obj == null) return;
        try {
            obj.close();
        } catch (Exception e) {	}
    }
    public static String readAll(InputStream in, String charset) {
        if (in == null)
            return null;

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(in, charset);
            br = new BufferedReader(reader);
            while (br.ready()) {
                sb.append(br.readLine());
                sb.append("\n");
            }
            reader.close();
        } catch (Exception e) {
            Log.e("FileTool","error:"+e.getMessage());
            return null;
        }finally {
            safelyClose(br);
        }

        return sb.toString();
    }


    public static boolean exist(String licenseFilePath) {
        return new File(licenseFilePath).exists();
    }
}
