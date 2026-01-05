package com.jtkj.library.infrastructure.base;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.jtkj.library.commom.cache.PreferencesManager;
import com.jtkj.library.commom.logger.CLog;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.multidex.MultiDex;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class BaseApplication<T extends BaseApplication> extends Application {
    private static final String TAG = "BaseApplication";

    /**
     * default value is base on 480*800 4.0inch screen
     */
    public static int densityDpi = 240;
    public static float density = 1.5f;//scale
    public static float scaledDensity = 1.5f;//font scale
    public static int widthPixels = 480;
    public static int heightPixels = 800;
    public static float xDpi = 240.0f;
    public static float yDpi = 240.0f;

    public static String versionName;
    public static int versionCode;
    public static String channel = "";

    private static int mMainThreadId;
    private static Handler mMainThreadHandler;

    private ExecutorService mExecutorService;

    public ExecutorService getExecutorService() {
        if (null == mExecutorService) {
            mExecutorService = Executors.newCachedThreadPool();
        }
        return mExecutorService;
    }

    private static volatile BaseApplication mInstance;

    public static BaseApplication getInstance() {
        if (null == mInstance) {
            synchronized (BaseApplication.class) {
                if (null == mInstance) {
                    mInstance = new BaseApplication();
                }
            }
        }
        return mInstance;
    }

    public void getVersion() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            CLog.e(TAG, "!@#$#$%^$%&%*^&(&)" + pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Configuration config = getResources().getConfiguration();
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mMainThreadId = android.os.Process.myTid();
        //noinspection deprecation
        CLog.e(TAG, "onCreate: uid=" + android.os.Process.myUid() +
                ", supportsProcesses=" + android.os.Process.supportsProcesses()
                +", pid=" + android.os.Process.myPid()
                +", tid=" + mMainThreadId);
        mMainThreadHandler = new Handler(Looper.myLooper());
        getDisplayMetrics();
        getPackageInfo();
//
//        NetworkReceiver nr = new NetworkReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(nr, filter);

        getPref();
    }

    public static boolean isRunInMainThread() {
        return android.os.Process.myTid() == mMainThreadId;
    }

    private void getDisplayMetrics() {
        DisplayMetrics dm = CUtility.getDisplayMetrics(this);
        densityDpi = dm.densityDpi;
        density = dm.density;
        scaledDensity = dm.scaledDensity;
        widthPixels = dm.widthPixels;
        heightPixels = dm.heightPixels;
        xDpi = dm.xdpi;
        yDpi = dm.ydpi;

        String desc = "densityDpi = " + densityDpi + ", scale = " + density + "; fontScale = " + scaledDensity + "; " + widthPixels + "*" + heightPixels + "; " + xDpi + "*" + yDpi;
        CLog.i(TAG, desc);
    }

    private void getPackageInfo() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                versionName = "1.0.0";
            }

            versionCode = pi.versionCode;
            if (versionCode == -1) {
                versionCode = 0;
            }
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            channel = appInfo.metaData.getString("UMENG_CHANNEL");
            CLog.i(TAG, "获取channel=" + channel + ";versionCode=" + versionCode + "'versionName=" + versionName);
        } catch (Exception e) {
            versionName = "1.0.0";
            versionCode = 0;
            channel = "";
        }
    }

    /**
     * 返回的是dp为单位的值
     */
    public static int fromXHDPItoDeviceScale(int dp) {
        return ((int) ((dp / 3) * density));
    }

    private PreferencesManager mPreManager;

    public PreferencesManager getPref() {
        mPreManager = PreferencesManager.getInstance(this);
        return mPreManager;
    }

    public void savePreference(String key, Object value) {
        if (value == null) return;
        if (value instanceof String) {
            mPreManager.put(key, (String) value);
        } else if (value instanceof Long) {
            mPreManager.put(key, (Long) value);
        } else if (value instanceof Integer) {
            mPreManager.put(key, (Integer) value);
        } else if (value instanceof Boolean) {
            mPreManager.put(key, (Boolean) value);
        } else if (value instanceof Float) {
            mPreManager.put(key, (Float) value);
        } else {
            mPreManager.put(key, value.toString());
        }
    }

    public String readString(String key, String defaultValue) {
        return mPreManager.get(key, defaultValue);
    }

    public long readLong(String key, Long defaultValue) {
        return mPreManager.get(key, defaultValue);
    }

    public int readInt(String key, int defaultValue) {
        return mPreManager.get(key, defaultValue);
    }

    public boolean readBoolean(String key, boolean defaultValue) {
        return mPreManager.get(key, defaultValue);
    }

    public float readFloat(String key, float defaultValue) {
        return mPreManager.get(key, defaultValue);
    }

    public void cancelRequest(String tag) {

    }

    public String formatString(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    /**
     * use handler post runnable on main thread
     */
    public static boolean post(Runnable runnable) {
        return mMainThreadHandler.post(runnable);
    }

    public static boolean postDelay(Runnable runnable,int delayTime) {
        return mMainThreadHandler.postDelayed(runnable,delayTime);
    }

    public static void removePost() {
        mMainThreadHandler.removeCallbacksAndMessages(null);
    }

    public int color(int resId) {
        return getResources().getColor(resId);
    }

    public String string(int id) {
        return getResources().getString(id);
    }

    public int dimen(int resId) {
        return (int) getResources().getDimension(resId);
    }

    public static void toast(String message) {
        Toast.makeText(getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * show toast in main thread
     */
    public static void toastSafe(final String message) {
        if (isRunInMainThread()) {
            toast(message);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    toast(message);
                }
            });
        }
    }

    public void onClickEvent(String eventId) {
//        MobclickAgent.onEvent(this, eventId);
    }

}
