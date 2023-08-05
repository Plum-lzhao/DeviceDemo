package com.icbc.demo.utils;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 日志保存类
 * Created by codemaster on 17/7/19.
 */

public class LogWriteUtil {

    private static final int MAX_CACHE_SIZE = 10;
    // 单个日志文件最大长度 单位是Byte
    private static final long MAX_FILE_SIZE = 500 * 1204;
    private static final String SDPATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "DEBUG";
    private static String ROOT_PATH;

    private String mNormalFileName;
    private Vector<String> mNormalLogList;

    private String mNetLogFileName;
    private Vector<String> mNetLogList;

    private LogHandler mHandler;
    private boolean mInit = false;

    private static final class Holder {
        public static LogWriteUtil INSTANCE = new LogWriteUtil();
    }

    private LogWriteUtil() {
        HandlerThread thread = new HandlerThread("app log write thread");
        thread.start();
        mHandler = new LogHandler(thread.getLooper());
    }

    public static LogWriteUtil getInstance() {
        return Holder.INSTANCE;
    }

    public void init() {
        if (!Logger.DEBUG || mInit) {
            return;
        }
        ROOT_PATH = SDPATH + File.separator + "log";
        File file = new File(ROOT_PATH);
        if (!file.exists()) {
            boolean flag = file.mkdirs();
            if (!flag) {
                Log.e("app", "createRootPath error: " + ROOT_PATH);
            }
        }
        mNormalFileName = "normallog_" + getFileTime() + ".txt";
        mNetLogFileName = "netlog_" + getFileTime() + ".txt";
        mInit = true;
    }

    public void unInit() {
        if (!Logger.DEBUG || !mInit) {
            return;
        }
        mInit = false;

        // flush conmmon日志
        if (mNormalLogList != null) {
            mNormalLogList.add("end time:" + getCurrentTime());
            sendMessage(mNormalLogList, mNormalFileName);
            mNormalLogList.clear();
        }
        // flush 网络日志
        if (mNetLogList != null) {
            mNetLogList.add("end time:" + getCurrentTime());
            sendMessage(mNetLogList, mNetLogFileName);
            mNetLogList.clear();
        }
    }

    public void logNormal(String log) {
        if (!Logger.DEBUG || !mInit) {
            return;
        }

        if (mNormalLogList == null) {
            mNormalLogList = new Vector<String>(MAX_CACHE_SIZE);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getCurrentTime()).append(" ")
                .append(log);

        mNormalLogList.add(builder.toString());

        if (mNormalLogList.size() >= MAX_CACHE_SIZE) {
            sendMessage(mNormalLogList, mNormalFileName);
            mNormalLogList.clear();
        }
    }

    public void logNetInfo(String log) {
        if (!Logger.DEBUG || !mInit) {
            return;
        }

        if (mNetLogList == null) {
            mNetLogList = new Vector<String>(MAX_CACHE_SIZE);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getCurrentTime()).append(" ")
                .append(System.currentTimeMillis()).append(" ")
                .append(log);

        mNetLogList.add(builder.toString());

        if (mNetLogList.size() >= MAX_CACHE_SIZE) {
            sendMessage(mNetLogList, mNetLogFileName);
            mNetLogList.clear();
        }
    }

    private void sendMessage(List<String> list, String path) {
        Message msg = mHandler.obtainMessage();
        msg.obj = new LogMessage(new ArrayList<String>(list), path);
        mHandler.sendMessage(msg);
    }

    @SuppressLint ("SimpleDateFormat")
    private String getCurrentTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");
        return sDateFormat.format(new java.util.Date());
    }

    @SuppressLint ("SimpleDateFormat")
    private String getFileTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return sDateFormat.format(new java.util.Date());
    }

    private class LogHandler extends Handler {

        public LogHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null || !(msg.obj instanceof LogMessage)) {
                return;
            }
            LogMessage logs = (LogMessage) msg.obj;
            if (logs.list != null && logs.list.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String loc : logs.list) {
                    sb.append(loc);
                    sb.append("\n");
                }
                writeLog(logs.path, sb.toString());
            }
        }

        private void writeLog(String fileName, String log) {
            if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(log)) {
                return;
            }

            String path = createRootPath();

            String filePath = path + File.separator + fileName;

            File file = new File(filePath);
            FileWriter writer = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                if(file.length() > MAX_FILE_SIZE){
                    mNormalFileName = "normallog_" + getFileTime() + ".txt";
                    filePath = path + File.separator + mNormalFileName;
                    file = new File(filePath);
                    file.createNewFile();
                }
                writer = new FileWriter(file, true);
                writer.write(log + "\n");
                writer.flush();
            } catch (IOException e) {
                Logger.e("writeLog IOException message: " + e.getMessage());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Logger.e("writeLog finally IOException message: " + e.getMessage());
                    }
                }
            }
        }

        private String createRootPath() {
            File file = new File(ROOT_PATH);
            if (!file.exists()) {
                boolean flag = file.mkdirs();
                if (!flag) {
                    Logger.e("createRootPath error ROOT_PATH = " + ROOT_PATH);
                }
            }
            return file.getAbsolutePath();
        }
    }

    private class LogMessage {
        private List<String> list;
        private String path;

        public LogMessage(List<String> list, String path) {
            this.list = list;
            this.path = path;
        }
    }
}
