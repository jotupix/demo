
package com.jtkj.demo.crash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;

import com.jtkj.library.commom.logger.CLog;
import com.jtkj.demo.CoolLED;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppCrashHandler implements UncaughtExceptionHandler {

    private final String tag = AppCrashHandler.class.getSimpleName();

    private Context mContext;
    private static AppCrashHandler instance;
    private UncaughtExceptionHandler mDefaultHandler;
    private APPOnCrashListener mOnCrashListener;
    private StringBuilder crashReport = new StringBuilder();

    private final String VERSION_NAME = "versionName";
    private final String VERSION_CODE = "versionCode";

    private final String PREFIX = "crash_";
    private final String PATTERN = "yyyy-MM-dd hh:mm:ss";
    private final String SUFFIX = ".txt";
    private String path;

    public static AppCrashHandler getInstance(Context context) {
        if (instance == null) {
            instance = new AppCrashHandler(context);
        }
        return instance;
    }

    private AppCrashHandler(Context context) {
        mContext = context;
//        if (checkSDCard()) {
//            path = Environment.getExternalStorageDirectory().getPath() + File.separator + mContext.getPackageName();
//        } else {
//            path = mContext.getFilesDir().getParent();
//        }
        path= CoolLED.getInstance().getExternalCacheDir().getAbsolutePath();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        CoolLED.reportError(ex);
        if (!handlerException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
//			try {
//				Thread.sleep(5000);
//            MobclickAgent.onKillProcess(mContext);
////				int pid = android.os.Process.myPid();
////				android.os.Process.killProcess(pid);
////				ActivityPageManager.getInstance().exit(mContext);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private boolean handlerException(Throwable ex) {
        if (ex == null) {
            return true;
        }

        final String msg = ex.getLocalizedMessage();
        if (TextUtils.isEmpty(msg)) {
            return false;
        }

        new Thread() {

            @Override
            public void run() {
                Looper.prepare();
                if (mOnCrashListener != null) {
                    mOnCrashListener.onCrashDialog(mContext);
                }
                Looper.loop();
            }

        }.start();

        collectCrashDeviceInfo(mContext);

        saveCrashInfo(ex);

        sendCrashReport();

        return true;
    }

    private void collectCrashDeviceInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                crashReport.append("\n\t");
                crashReport.append(VERSION_NAME + " = " + pi.versionName).append("\n\t");
                crashReport.append(VERSION_CODE + " = " + pi.versionCode).append("\n\t");
            }

            Field[] fieldList = Build.class.getDeclaredFields();
            if (fieldList != null) {
                for (Field device : fieldList) {
                    device.setAccessible(true);
                    crashReport.append(device.getName() + " = " + device.get(null)).append("\n\t");
                }
            }
        } catch (NameNotFoundException e) {
            CoolLED.reportError(e);
        } catch (IllegalArgumentException e) {
            CoolLED.reportError(e);
        } catch (IllegalAccessException e) {
            CoolLED.reportError(e);
        }
    }

    private void saveCrashInfo(Throwable ex) {
        try {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);

            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }

            String result = writer.toString();
            printWriter.close();

            crashReport.append("\n\t");
            crashReport.append(ex.getMessage()).append("\n\t");
            crashReport.append(result).append("\n\t");

            String fileName = getCrashFileName();
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(path, fileName);
            CLog.e(tag, file.getPath());

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(crashReport.toString().getBytes());
            fos.flush();
            fos.close();

        } catch (Exception e) {
            CoolLED.reportError(e);
        }
    }

    public boolean checkSDCard() {
        String flag = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(flag)) {
            return true;
        }
        return false;
    }

    @SuppressLint("SimpleDateFormat")
    public String getCrashFileName() {
        StringBuilder fileName = new StringBuilder(PREFIX);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
        fileName.append(sdf.format(date));

        fileName.append(SUFFIX);
        return fileName.toString();
    }

    public void sendCrashReport() {
        // File filesDir = mContext.getFilesDir();
        // FilenameFilter filter = new FilenameFilter() {
        // @Override
        // public boolean accept(File dir, String filename) {
        // return filename.endsWith(SUFFIX);
        // }
        // };
        //
        // String[] list = filesDir.list(filter);
        // if(list != null && list.length > 0){
        // for(String fileName : list){
        // File file = new File(path, fileName);
        // if(file.exists()){
        // if(onCrashListener != null){
        // onCrashListener.onCrashPost(String.valueOf(crashReport), file);
        // }
        // }
        // }
        // }
    }

    public APPOnCrashListener getOnCrashListener() {
        return mOnCrashListener;
    }

    public void setOnCrashListener(APPOnCrashListener onCrashListener) {
        mOnCrashListener = onCrashListener;
    }
}
