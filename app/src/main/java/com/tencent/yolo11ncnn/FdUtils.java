package com.tencent.yolo11ncnn;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FdUtils {

    private static final String TAG = "FdUtils";
    static Map<String, Integer> lastFdTypeCount = new HashMap<>();

    /**
     * 获取当前应用能访问的 /dev/fd 目录下的所有文件描述符
     *
     * @param onlyInc 是否只输出新增的 FD（对比上次调用）
     * @return 当前打开的 FD 总数，失败返回 -1
     */
    public static int countOpenFileDescriptors(boolean onlyInc) {
        File fdDir = new File("/dev/fd");
        if (!fdDir.exists() || !fdDir.isDirectory()) {
            Log.w(TAG, "/dev/fd does not exist or is not accessible.");
            return -1;
        }

        File[] fds = fdDir.listFiles();
        if (fds == null) {
            Log.w(TAG, "Failed to list files under /dev/fd");
            return -1;
        }

        // 当前 FD 映射
        Map<String, Integer> fdTypeCount = new HashMap<>();

        for (File fd : fds) {
            String name = fd.getName();
            String canonicalPath;
            try {
                canonicalPath = fd.getCanonicalPath();
            } catch (IOException e) {
                canonicalPath = "unknown";
            }

            String fdType = parseFdName(name, canonicalPath);
            fdTypeCount.merge(fdType, 1, Integer::sum);
        }

        Map<String, Integer> diff = new HashMap<>(fdTypeCount);

        String prefix ="";
        if (onlyInc) {
            prefix = "INC ";
            //从fdTypeCount 中减去 lastFdTypeCount中的数值
            HashSet<String> removeList = new HashSet<>();
            for(String fdType : fdTypeCount.keySet()){
                int v = diff.get(fdType) - lastFdTypeCount.getOrDefault(fdType, 0);
                if(v <= 0 ){
                    removeList.add(fdType);
                }
                diff.put(fdType,  v);
            }
            for(String fdType : removeList){
                diff.remove(fdType);
            }
        }
        //  输出结果
        StringBuilder sb = new StringBuilder();
        for (String fdName : diff.keySet()) {
            //Log.d(TAG, "FD: " + fdName + " -> " + currentFdMap.get(fdName));
            sb.append(prefix).append("FD: ").append(fdName).append(" -> ").append(diff.get(fdName)).append("\n");
        }
        Log.d(TAG, sb.toString());

        lastFdTypeCount = fdTypeCount;

        //Log.i("FdUtils","FDList Ret "+getFdListOutput());
        Log.i("FDUtils", fds.length+"");
        return fds.length;
    }
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
    /**
     * 执行 ls -l /dev/fd 并返回完整输出（字符串形式）
     */
    private static String getFdListOutput() {
        try {
            Process process = Runtime.getRuntime().exec("ls -l /dev/fd/");
            process.waitFor();
            String output = readAll(process.getInputStream(), "utf-8");
            String err = readAll(process.getErrorStream(), "utf-8");

            return output + err;
        } catch (IOException e) {
            Log.e(TAG, "执行 ls -l /dev/fd 失败", e);
            return "Failed to execute ls -l /dev/fd";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将各种类型的 FD 名称统一归类处理
     */
    private static String parseFdName(String name, String canonicalPath) {
        switch (name) {
            case "0": return "stdin";
            case "1": return "stdout";
            case "2": return "stderr";
        }

        if (name.startsWith("socket:") || name.startsWith("pipe:") || name.startsWith("anon_inode:")) {
            return name;
        }

        if (canonicalPath != null && canonicalPath.matches("/proc/\\d+/fd/\\d+")) {
            return "<fd:file>";
        }

        if ("/dev/null".equals(canonicalPath)) return "/dev/null";
        if ("/dev/zero".equals(canonicalPath)) return "/dev/zero";
        if ("/dev/__properties__".equals(canonicalPath)) return "/dev/__properties__";

        return canonicalPath;
    }

    /**
     * 强制重置记录的历史 FD 集合
     */
    public static void resetLastFdSet() {
        lastFdTypeCount.clear();
    }
}