package com.jtkj.demo.device;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.infrastructure.eventbus.EventAgent;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import androidx.annotation.Nullable;

public class MainService extends Service {
    private static final String TAG = MainService.class.getSimpleName();

    public class ServiceBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    ServiceBinder mBinder = new ServiceBinder();

    public final static int GRAY_SERVICE_ID = 1001;

    public static volatile MainService mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        CLog.i(TAG, "onCreate");
        mInstance = this;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        CLog.i(TAG, "onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CLog.i(TAG, "onStartCommand");
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                EventAgent.post(new ServiceStartedEvent());
                startForeground(GRAY_SERVICE_ID, new Notification());//API < 18
            } else {
                EventAgent.post(new ServiceStartedEvent());
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                    Intent innerIntent = new Intent(this, HelpService.class);
                    startService(innerIntent);
                } else {
                    startForeground(GRAY_SERVICE_ID, getNotification());
                }
            }
        } catch (Exception e) {

        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        CLog.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        CLog.i(TAG, "onUnbind");
        EventAgent.post(new ServiceUnBindEvent());
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        CLog.i(TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        CLog.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        CLog.i(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        CLog.i(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        CLog.i(TAG, "onTrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        CLog.i(TAG, "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        CLog.i(TAG, "dump");
        super.dump(fd, writer, args);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        CLog.i(TAG, "attachBaseContext");
    }

    private Notification getNotification() {
        Notification n = null;
        try {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel chan1 = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_DEFAULT);
                chan1.setLightColor(Color.GREEN);
                chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                manager.createNotificationChannel(chan1);

                n = new Notification.Builder(getApplicationContext(), chan1.getId())
                        .setContentTitle(CoolLED.getInstance().string(R.string.app_name))
                        .setContentText(CoolLED.getInstance().string(R.string.app_is_running))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .build();

            } else {
                n = new Notification.Builder(getApplicationContext())
                        .setContentTitle(CoolLED.getInstance().string(R.string.app_name))
                        .setContentText(CoolLED.getInstance().string(R.string.app_is_running))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .build();
            }
        } catch (Exception e) {

        }
        return n;
    }

    public static class HelpService extends Service {
        private static final String TAG = HelpService.class.getSimpleName();

        private Notification getNotification() {
            Notification n = null;
            try {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel chan1 = new NotificationChannel("default", "default", NotificationManager.IMPORTANCE_DEFAULT);
                    chan1.setLightColor(Color.GREEN);
                    chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    manager.createNotificationChannel(chan1);

                    n = new Notification.Builder(getApplicationContext(), chan1.getId())
                            .setContentTitle(CoolLED.getInstance().string(R.string.app_name))
                            .setContentText(CoolLED.getInstance().string(R.string.app_is_running))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pendingIntent)
                            .build();

                } else {
                    n = new Notification.Builder(getApplicationContext())
                            .setContentTitle(CoolLED.getInstance().string(R.string.app_name))
                            .setContentText(CoolLED.getInstance().string(R.string.app_is_running))
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentIntent(pendingIntent)
                            .build();
                }
            } catch (Exception e) {

            }
            return n;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            CLog.i(TAG, "GrayInnerService onStartCommand");
            try {
                startForeground(MainService.GRAY_SERVICE_ID, getNotification());
                MainService.mInstance.startForeground(MainService.GRAY_SERVICE_ID, getNotification());
                stopForeground(true);
                stopSelf();
            } catch (Exception e) {

            }
            return Service.START_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            CLog.i(TAG, "GrayInnerService onBind");
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            CLog.i(TAG, "GrayInnerService onDestroy");
        }
    }

    public static class ServiceStartedEvent {

    }

    public static class ServiceUnBindEvent {

    }

}
