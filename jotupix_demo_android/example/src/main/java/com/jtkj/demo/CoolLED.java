package com.jtkj.demo;

import com.jtkj.demo.base.NoticeToast;
import com.jtkj.demo.device.DeviceManager;
import com.jtkj.library.commom.cache.CacheManager;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.fastble.BleManager;
import com.jtkj.library.infrastructure.base.BaseApplication;


public class CoolLED extends BaseApplication {

    private static final String TAG = CoolLED.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        CLog.setIsDebug(BuildConfig.DEBUG);
        BleManager.getInstance().init(this);
        CacheManager.setSysCachePath(getFilesDir().getPath());
        DeviceManager.getInstance();
        //init the ticker for the timeout use  when send data
        DeviceManager.getInstance().getJotuPix().tick(System.currentTimeMillis());
        updateTicker();
    }

    /***
     * update the ticker every 20 ms
     */
    public void updateTicker() {
        postDelay(new Runnable() {
            @Override
            public void run() {
                DeviceManager.getInstance().getJotuPix().tick(System.currentTimeMillis());
                updateTicker();
            }
        }, 20);
    }


    public static void reportError(String error) {

    }

    public static void reportError(Throwable e) {

    }


    public static void toast(String message) {
        NoticeToast.showCustomToast(CoolLED.getInstance(), message);
    }

    public static void toast(int message) {
        NoticeToast.showCustomToast(CoolLED.getInstance(), message);
    }

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

    public static void toastSafe(final int message) {
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
}