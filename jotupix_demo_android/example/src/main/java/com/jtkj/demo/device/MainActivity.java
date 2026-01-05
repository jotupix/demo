package com.jtkj.demo.device;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.base.BaseActivity;
import com.jtkj.demo.databinding.MainActivityBinding;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.commom.tools.ServiceUtils;
import com.jtkj.library.infrastructure.eventbus.EventAgent;
import com.jtkj.demo.rxpermission.Permission;
import com.jtkj.demo.rxpermission.RxPermissions;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.rxjava3.functions.Consumer;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private long mFirstClickTime = 0;
    MainService mService;
    MainActivityBinding mBinding;


    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CLog.i(TAG, "mServiceConnection>>>onServiceConnected");
            mService = ((MainService.ServiceBinder) service).getService();
            showDeviceFragment();
            if (!mBinding.drawerLayout.isDrawerOpen(mBinding.leftContentLayout)) {
                mBinding.drawerLayout.openDrawer(GravityCompat.START);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CLog.i(TAG, "mServiceConnection>>>onServiceDisconnected");
            mService = null;
        }
    };

    public static void start(Activity aty) {
        Intent intent = new Intent(aty, MainActivity.class);
        aty.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        EventAgent.register(this);
        showMainFragment();
        initBleAndLocation();
    }


    @Override
    public void initBleLocationSuccess() {
        super.initBleLocationSuccess();
        CLog.i(TAG, "initBleLocationSuccess");
        startMainService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CLog.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        CLog.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CLog.i(TAG, "onDestroy");
        EventAgent.unregister(this);
        stopMainService();
        if (null != DeviceManager.mInstance) {
            DeviceManager.getInstance().release();
        }
    }

    public void showDeviceFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment deviceFragment = fm.findFragmentByTag(DeviceFragment.class.getSimpleName());
        if (null == deviceFragment) {
            deviceFragment = new DeviceFragment();
        }
        if (!deviceFragment.isAdded()) {
            ft.add(R.id.left_content_layout, deviceFragment, DeviceFragment.class.getSimpleName());
        } else {
            ft.show(deviceFragment);
        }
        ft.commitAllowingStateLoss();
    }

    public void showMainFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment mainfragment = fm.findFragmentByTag(MainFragment.class.getSimpleName());
        if (null == mainfragment) {
            mainfragment = new MainFragment();
        }
        if (!mainfragment.isAdded()) {
            ft.add(R.id.container_layout, mainfragment, MainFragment.class.getSimpleName());
        } else {
            ft.show(mainfragment);
        }
        ft.commitAllowingStateLoss();
    }

    public void switchDeviceFragment() {
        if (mBinding.drawerLayout.isDrawerOpen(mBinding.leftContentLayout)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mBinding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mBinding.drawerLayout.isDrawerOpen(mBinding.leftContentLayout)) {
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            long currentTime = System.currentTimeMillis();
            if ((currentTime - mFirstClickTime) > 2000) {
                mFirstClickTime = currentTime;
                CoolLED.toastSafe(CoolLED.getInstance().string(R.string.click_one_more_time_and_exit));
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startMainService() {
        CLog.i(TAG, "startMainService");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= 34) {
                if (ActivityCompat.checkSelfPermission(this, "android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE") == PackageManager.PERMISSION_GRANTED) {
                    CLog.i(TAG, "startMainService has FOREGROUND_SERVICE_CONNECTED_DEVICE ");
                    startForegroundService(new Intent(this, MainService.class));
                } else {
                    CLog.i(TAG, "startMainService  not has FOREGROUND_SERVICE_CONNECTED_DEVICE ");
                    getForegroundService();
                }
            } else {
                startForegroundService(new Intent(this, MainService.class));
            }
        } else {
            startService(new Intent(this, MainService.class));
        }
    }


    private void stopMainService() {
        if (ServiceUtils.isServiceRunning(getApplicationContext(), MainService.class.getName())) {
            CLog.i(TAG, "stopMainService");
            if (mService != null) {
                unbindService(mServiceConnection);
            }
            stopService(new Intent(this, MainService.class));
        }
    }

    private void getForegroundService() {
        if (Build.VERSION.SDK_INT >= 34) {
            RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
            rxPermissions.requestEachCombined("android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE").subscribe(new Consumer<Permission>() {
                @Override
                public void accept(Permission permission) {
                    if (permission.granted) { // 在android 6.0之前会默认返回true
                        CLog.i(TAG, "startMainService get  FOREGROUND_SERVICE_CONNECTED_DEVICE success ");
                        startForegroundService(new Intent(MainActivity.this, MainService.class));
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        CLog.i(TAG, "startMainService get  FOREGROUND_SERVICE_CONNECTED_DEVICE failure ");
                    } else {

                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MainService.ServiceStartedEvent event) {
        if (ServiceUtils.isServiceRunning(this, MainService.class.getName())) {
            CLog.i(TAG, "onEvent(MainService.ServiceStartedEvent event)");
            if (null == mService) {
                CLog.i(TAG, "null == mService need to BindService");
                bindService(new Intent(this, MainService.class), mServiceConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
            } else {
                CLog.i(TAG, "null /= mService need to BindService");
                if (!mBinding.drawerLayout.isDrawerOpen(mBinding.leftContentLayout)) {
                    mBinding.drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        } else {
            CLog.i(TAG, "onEvent(MainService.ServiceStartedEvent event)>>>mService is not running");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MainService.ServiceUnBindEvent event) {
        CLog.i(TAG, "onEvent(MainService.ServiceUnBindEvent event)");
        mService = null;
    }
}