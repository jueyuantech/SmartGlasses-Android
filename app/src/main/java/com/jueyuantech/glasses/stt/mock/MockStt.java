package com.jueyuantech.glasses.stt.mock;

import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import android.content.Context;
import android.content.res.AssetManager;

import com.jueyuantech.glasses.stt.SttEngine;
import com.jueyuantech.glasses.util.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class MockStt extends SttEngine {
    private Context mContext;
    private AssetManager assetManager;
    private static int loopIdx = 0;

    private boolean isTrans = false;

    public MockStt(Context context, String func) {
        mContext = context;
        assetManager = mContext.getAssets();

        isTrans = STT_FUNC_TRANSLATE.equals(func);
    }

    @Override
    public String getName() {
        return "Mock";
    }

    @Override
    public String getServiceId() {
        return "";
    }

    @Override
    public String getLocalParam() {
        return "";
    }

    @Override
    public void initParam(String params) {

    }

    @Override
    public void connect() {
        loopIdx = 1;
        readerThread.start();
    }

    @Override
    public void disconnect() {
        readerThread.interrupt();
    }

    @Override
    public boolean shouldRetry(int errCode) {
        return true;
    }

    @Override
    public void send(byte[] data) {

    }

    private Thread readerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                File ftpFilesDir = mContext.getExternalFilesDir("VenusMock");
                File transMockFile = new File(ftpFilesDir, "TransMock");
                transMockFile.deleteOnExit();
                transMockFile.createNewFile();

                InputStream inputStream = assetManager.open("TransMock");
                FileOutputStream outputStream = new FileOutputStream(transMockFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                BufferedReader reader = new BufferedReader(new FileReader(transMockFile));
                String line;
                while (!Thread.currentThread().isInterrupted()) {
                    line = reader.readLine();
                    if (line == null) { // 如果读到文件末尾，则重新开始读取
                        reader.close();
                        reader = new BufferedReader(new FileReader(transMockFile));
                        loopIdx += 1000 * 60;
                        LogUtil.i(loopIdx);
                        continue;
                    }
                    // 发送消息到UI线程
                    if (null != mOnSttListener) {
                        String[] ret = line.split("-");
                        if (ret.length > 3) {
                            mOnSttListener.onMessage(
                                    Long.parseLong(ret[0]) + loopIdx,
                                    "",
                                    Integer.parseInt(ret[1]),
                                    ret[2],
                                    isTrans ? ret[3] : ""
                            );
                        }
                    }

                    // 产生一个100-200ms的随机间隔
                    int delay = 100 + (int) (Math.random() * 101);
                    Thread.sleep(delay);
                }
                reader.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    private BufferedWriter mockFileWriter;
    public void openFile() {
        File ftpFilesDir = mContext.getExternalFilesDir("VenusMock");
        if (!ftpFilesDir.exists()) {
            ftpFilesDir.mkdir();
        }

        File transMockFile = new File(ftpFilesDir, "TransMock");
        transMockFile.deleteOnExit();
        try {
            transMockFile.createNewFile();
            mockFileWriter = new BufferedWriter(new FileWriter(transMockFile, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeFileLine(String lineContent) {
        try {
            mockFileWriter.write(lineContent);
            mockFileWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            if (mockFileWriter != null) {
                mockFileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}