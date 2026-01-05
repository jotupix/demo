package com.jtkj.library.commom.tools;

import android.content.Context;
import android.content.res.AssetManager;

import com.jtkj.library.commom.logger.CLog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    protected static final String TAG = FileUtils.class.getSimpleName();

    /**
     * 从asset路径下读取对应文件转String输出
     *
     * @param mContext
     * @return
     */
    public static String getJson(Context mContext, String fileName) {
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(am.open(fileName)));
            String next = "";
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }

    /**
     * 从文件路径下读取对应文件转String输出
     *
     * @param path
     * @return
     */
    public static String getJson(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream mInputStream = new FileInputStream(new File(path));
            InputStreamReader inputReader = new InputStreamReader(mInputStream);
            BufferedReader br = new BufferedReader(inputReader);
            String next = "";
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            CLog.i("FileUtils", e.toString());
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }

    public static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception exception) {
            CLog.i(TAG, "deleteFile>>>>exception>>>" + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static byte[] readFileToBytes(String path) {
        byte[] data = null;
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            int readBytes = fis.read(data);
            fis.close();
        } catch (Exception exception) {
            CLog.i(TAG, "readFileToBytes>>>>exception>>>" + exception.getMessage());
            exception.printStackTrace();
        }
        return data;
    }


    public static byte[] readFileToBytes(File file) {
        byte[] data = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            int readBytes = fis.read(data);
            fis.close();
        } catch (Exception exception) {
            CLog.i(TAG, "readFileToBytes>>>>exception>>>" + exception.getMessage());
            exception.printStackTrace();
        }
        return data;
    }
    public static byte[] readRawImageBytes(Context context, int resId)  {
        try{
            InputStream inputStream = context.getResources().openRawResource(resId);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            inputStream.close();
            return buffer.toByteArray();
        }
        catch (Exception exception){
            return null;
        }
    }
}
