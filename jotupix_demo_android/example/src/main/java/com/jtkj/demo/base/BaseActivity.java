package com.jtkj.demo.base;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.device.ProgressDialog;
import com.jtkj.demo.rxpermission.Permission;
import com.jtkj.demo.rxpermission.RxPermissions;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.fastble.BleManager;
import com.jtkj.library.infrastructure.base.AnalyticsActivity;

import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.rxjava3.functions.Consumer;
import qiu.niorgai.StatusBarCompat;

public abstract class BaseActivity extends AnalyticsActivity {
    public static final String TAG = BaseActivity.class.getSimpleName();

    private Dialog mDialog;

    private static final int REQUEST_ENABLE_BT = 100;
    private static final int REQUEST_ENABLE_GPS_LOCATION = 101;
    public RxPermissions rxPermissions = new RxPermissions(this);
    ProgressDialog mProgressDialog;

    RxPermissions mRxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, CoolLED.getInstance().color(R.color.color_212121));
        registerReceiver(mReceiver, makeBleStatusChangeFilter());
        mRxPermissions = new RxPermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseDialog();
        unregisterReceiver(mReceiver);
    }

    ActivityResultLauncher<Intent> openBLeIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (isBleEnable()) {
                        initBleAndLocation();
                    } else {
                        bleNotOpened();
                    }
                }
            });

    ActivityResultLauncher<Intent> openLocationIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (isBleEnable()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (isGPSOpen()) {
                                initBleLocationSuccess();
                            } else {
                                gpsNotOpened();
                            }
                        } else {
                            initBleLocationSuccess();
                        }
                    } else {
                        bleNotOpened();
                    }
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (isBleEnable()) {
                    initBleAndLocation();
                } else {
                    bleNotOpened();
                }
                break;
            case REQUEST_ENABLE_GPS_LOCATION:
                if (isBleEnable()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (isGPSOpen()) {
                            initBleLocationSuccess();
                        } else {
                            gpsNotOpened();
                        }
                    } else {
                        initBleLocationSuccess();
                    }
                } else {
                    bleNotOpened();
                }
                break;
        }
    }

    public void initBleLocationSuccess() {

    }

    public void bleStateOff() {
        initBleAndLocation();
    }

    public void initBleAndLocation() {
        if (!BleManager.getInstance().isSupportBle()) {
            showTipsDialog(getResources().getString(R.string.ble_not_supported),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    });
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                getBlePermissions();
                return;
            }

            if (!isBleEnable()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                openBLeIntent.launch(enableBtIntent);
                return;
            }
            initBleLocationSuccess();

        } else {
            if (!isBleEnable()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                openBLeIntent.launch(enableBtIntent);
                return;
            }
            if (!isLocationPermissionEnable()) {
                getLocationPermission();
                return;
            }
            checkGPS();
        }
    }

    public void checkGPS() {
        if (isBleEnable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isGPSOpen()) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.tip))
                        .setMessage(getResources().getString(R.string.gpsNotifyMsg))
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .setPositiveButton(getResources().getString(R.string.go_to_set),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        openLocationIntent.launch(intent);
//                                        startActivityForResult(intent, REQUEST_ENABLE_GPS_LOCATION);
                                    }
                                })

                        .setCancelable(false)
                        .show();
            } else {
                initBleLocationSuccess();
            }
        } else {
            bleNotOpened();
        }
    }

    public void bleNotOpened() {
        showTipsDialog(getResources().getString(R.string.bluetooth_not_opened), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initBleAndLocation();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }, false);
    }

    public void gpsNotOpened() {
        showTipsDialog(getResources().getString(R.string.gps_not_opened), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initBleAndLocation();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }, false);
    }

    private boolean isGPSOpen() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    private boolean isBleEnable() {
        return BleManager.getInstance().isBlueEnable();
    }

    public boolean hasMicPermission() {
        return hasPermissions(Manifest.permission.RECORD_AUDIO);
    }

    public boolean hasImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasPermissions(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            return hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }


    public boolean hasPermissions(@NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPermissions(@NonNull List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationPermissionEnable() {
        return hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void getLocationPermission() {
        showTipsDialog(R.string.location_permission_tips,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rxPermissions.requestEachCombined(Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) {
                                if (permission.granted) {
                                    initBleAndLocation();
                                } else if (permission.shouldShowRequestPermissionRationale) {
                                    getLocationPermissionDenied();
                                } else {
                                    CoolLED.toast(CoolLED.getInstance().string(R.string.open_location_permission_in_the_settings));
                                }
                            }
                        });
                    }
                }, false);
    }

    public void getLocationPermissionDenied() {
        showTipsDialog(R.string.location_permission_denied_tips,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rxPermissions.requestEachCombined(Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) {
                                if (permission.granted) {
                                    initBleAndLocation();
                                } else if (permission.shouldShowRequestPermissionRationale) {
                                    getLocationPermissionDenied();
                                } else {
                                    CoolLED.toast(CoolLED.getInstance().string(R.string.open_location_permission_in_the_settings));
                                }
                            }
                        });
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, false);
    }

    public void getBlePermissions() {
        showTipsDialog(R.string.ask_to_open_ble_permissions,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rxPermissions.requestEachCombined(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE,
                                Manifest.permission.BLUETOOTH_SCAN).subscribe(new io.reactivex.rxjava3.functions.Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) {
                                if (permission.granted) {
                                    initBleAndLocation();
                                } else if (permission.shouldShowRequestPermissionRationale) {
                                    getBlePermissionsDenied();
                                } else {
                                    CoolLED.toast(CoolLED.getInstance().string(R.string.please_open_ble_permissions_in_the_settings));
                                }
                            }
                        });
                    }
                }, false);
    }

    public void getBlePermissionsDenied() {
        showTipsDialog(R.string.ask_to_open_ble_permissions_denied,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rxPermissions.requestEachCombined(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE,
                                Manifest.permission.BLUETOOTH_SCAN).subscribe(new io.reactivex.rxjava3.functions.Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) {
                                if (permission.granted) {
                                    initBleAndLocation();
                                } else if (permission.shouldShowRequestPermissionRationale) {
                                    getBlePermissionsDenied();
                                } else {
                                    CoolLED.toast(CoolLED.getInstance().string(R.string.please_open_ble_permissions_in_the_settings));
                                }
                            }
                        });
                    }
                }, false);
    }


    public boolean checkAllRequiredPermission() {
        return hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.VIBRATE);
    }

    public void getAllRequiredPermission() {
        requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.VIBRATE);
    }

    private void requestPermission(String... permissions) {
    }


    public void showTipsDialog(String tips) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), null);
        mDialog = builder.create();
        mDialog.show();
    }

    public void showTipsDialog(int tips) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(R.string.confirm), null);
        mDialog = builder.create();
        mDialog.show();
    }

    public void showTipsDialog(String tips, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), onClickListener);
        builder.setCancelable(false);
        mDialog = builder.create();
        mDialog.show();
    }

    public void showTipsDialog(int tips, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(R.string.confirm), onClickListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(String title, String tips, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), confirmListener);
        builder.setNegativeButton(getResources().getString(R.string.cancel), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(int tips, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(R.string.confirm), confirmListener);
        builder.setNegativeButton(getResources().getString(R.string.cancel), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    public void showTipsDialog(String tips, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), confirmListener);
        builder.setNegativeButton(getResources().getString(R.string.cancel), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(cancelable);
        mDialog.show();

    }

    public void showTipsDialog(String tips, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), confirmListener);
        builder.setNegativeButton(getResources().getString(R.string.cancel), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private void releaseDialog() {
        if (null != mDialog) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            mDialog = null;
        }

        if (null != mProgressDialog) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mProgressDialog = null;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CLog.i(TAG, "onReceive---------");
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_OFF:
                            CLog.i(TAG, "onReceive---------STATE_OFF");
                            bleStateOff();
                            break;
                    }
                    break;
            }
        }
    };

    private IntentFilter makeBleStatusChangeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    public static class ChangeLanguageEvent {

    }

    public boolean isProgressDialogCancelable;
    public  boolean isProgressDialogCancelableOutside;

    protected void showProgressDialog(String messageOne, String messageTwo, boolean cancelable, boolean cancelableOutside) {
        CLog.e(TAG, "showLoading(): " + getClass().getSimpleName());
        isProgressDialogCancelable = cancelable;
        isProgressDialogCancelableOutside = cancelableOutside;
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog == null) {
                        initProgressDialog(messageOne, messageTwo, cancelable, cancelableOutside);
                    } else {
                        if (!mProgressDialog.isShowing()) {
                            CLog.e(TAG, "showLoading() reset loading cancelable: " + cancelable);
                        } else {
                            CLog.e(TAG, "showLoading()>>>messageOne>>> " + messageOne + ">>>messageTwo>>>" + messageTwo);
                            mProgressDialog.sending(messageOne, messageTwo);
                        }
                    }

                    if (!mProgressDialog.isShowing()) {
                        CLog.e(TAG, "showLoading() showDialog");
                        mProgressDialog.show();
                    }
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "showProgressDialog>>>exception>>" + exception.getMessage());
        }
    }

    protected void showProgressDialog(String messageOne, boolean cancelable, boolean cancelableOutside) {
        CLog.e(TAG, "showLoading(): " + getClass().getSimpleName());
        isProgressDialogCancelable = cancelable;
        isProgressDialogCancelableOutside = cancelableOutside;
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog == null) {
                        initProgressDialog(messageOne, cancelable, cancelableOutside);
                    } else {
                        if (!mProgressDialog.isShowing()) {
                            CLog.e(TAG, "showLoading() reset loading cancelable: " + cancelable);
                        } else {
                            CLog.e(TAG, "showLoading()>>>messageOne>>> " + messageOne);
                            mProgressDialog.showContent();
                        }
                    }

                    if (!mProgressDialog.isShowing()) {
                        CLog.e(TAG, "showLoading() showDialog");
                        mProgressDialog.show();
                    }
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "showProgressDialog>>>exception>>" + exception.getMessage());
        }
    }

    protected void hideProgressDialogOverTime() {
        CoolLED.postDelay(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
            }
        }, 6000);
    }

    protected void sending(String messageOne, String messageTwo) {
        CoolLED.removePost();
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog == null) {
                        initProgressDialog(messageOne, messageTwo, isProgressDialogCancelable, isProgressDialogCancelableOutside);
                    }

                    if (!mProgressDialog.isShowing()) {
                        CLog.e(TAG, "showLoading() showDialog");
                        mProgressDialog.show();
                    }
                    mProgressDialog.sending(messageOne, messageTwo);
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "sending>>>exception>>" + exception.getMessage());
        }
    }


    protected void sendSuccess() {
        CoolLED.removePost();
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog == null) {
                        initProgressDialog(null, null, isProgressDialogCancelable, isProgressDialogCancelableOutside);
                    }

                    if (!mProgressDialog.isShowing()) {
                        CLog.e(TAG, "showLoading() showDialog");
                        mProgressDialog.show();
                    }
                    mProgressDialog.sendSuccess();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "sendSuccess>>>exception>>" + exception.getMessage());
        }
    }

    protected void sendFailed() {
        CoolLED.removePost();
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog == null) {
                        initProgressDialog(null, null, isProgressDialogCancelable, isProgressDialogCancelableOutside);
                    }

                    if (!mProgressDialog.isShowing()) {
                        CLog.e(TAG, "showLoading() showDialog");
                        mProgressDialog.show();
                    }
                    mProgressDialog.sendFailed();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "sendFailed>>>exception>>" + exception.getMessage());
        }
    }

    protected void dismissProgressDialog() {
        CLog.e(TAG, "dismissLoading(): " + getClass().getSimpleName());
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null != mProgressDialog) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        mProgressDialog = null;
                    }
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "dismissProgressDialog>>>exception>>" + exception.getMessage());
        }
    }

    protected void initProgressDialog(String messageOne, String messageTwo, boolean cancelable, boolean cancelableOutside) {
        CLog.e(TAG, "onFunctionalStart showLoading() cancelable: " + cancelable);
        try {
            Context ctx = this;
            if (ctx != null) {
                mProgressDialog = ProgressDialog.showLoading(ctx, messageOne, messageTwo, cancelable, cancelableOutside);
                mProgressDialog.startSend();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "initProgressDialog>>>exception>>" + exception.getMessage());
        }
    }

    protected void initProgressDialog(String messageOne, boolean cancelable, boolean cancelableOutside) {
        CLog.e(TAG, "onFunctionalStart showLoading() cancelable: " + cancelable);
        try {
            Context ctx = this;
            if (ctx != null) {
                mProgressDialog = ProgressDialog.showLoading(ctx, messageOne, null, cancelable, cancelableOutside);
                mProgressDialog.showContent();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "initProgressDialog>>>exception>>" + exception.getMessage());
        }
    }
}
