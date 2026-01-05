package com.jtkj.demo.base;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.device.ProgressDialog;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.infrastructure.base.AnalyticsFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public abstract class BaseFragment extends AnalyticsFragment {
    private static final String TAG = BaseFragment.class.getSimpleName();
    private Dialog mDialog;

    ProgressDialog mProgressDialog;

    public boolean hasMicPermission() {
        return hasPermissions(Manifest.permission.RECORD_AUDIO);
    }

    public boolean hasMusicPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasPermissions(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.RECORD_AUDIO);
        } else {
            return hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
        }
    }

    public boolean isLocationPermissionEnable() {
        return hasPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
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
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseDialog();
    }

    public void showTipsDialog(String tips) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), null);
        mDialog = builder.create();
        mDialog.show();
    }

    public void showTipsDialog(int tips) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(R.string.confirm), null);
        mDialog = builder.create();
        mDialog.show();
    }

    public void showTipsDialog(String tips, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), onClickListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(int tips, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(R.string.confirm), onClickListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(int tips, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(R.string.confirm), confirmListener);
        builder.setNegativeButton(getResources().getString(R.string.cancel), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(String tips, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(R.string.confirm), confirmListener);
        builder.setNegativeButton(getResources().getString(R.string.cancel), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(int tips, int confirmString, int cancelString, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(confirmString), confirmListener);
        builder.setNegativeButton(getResources().getString(cancelString), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(String tips, int confirmString, int cancelString, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(confirmString), confirmListener);
        builder.setNegativeButton(getResources().getString(cancelString), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public void showTipsDialog(int tips, boolean isCancelAble, int confirmString, int cancelString, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(getResources().getString(tips));
        builder.setPositiveButton(getResources().getString(confirmString), confirmListener);
        builder.setNegativeButton(getResources().getString(cancelString), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(isCancelAble);
        mDialog.show();
    }

    public void showTipsDialog(String tips, boolean isCancelAble, int confirmString, int cancelString, DialogInterface.OnClickListener cancelListener, DialogInterface.OnClickListener confirmListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.tip));
        builder.setMessage(tips);
        builder.setPositiveButton(getResources().getString(confirmString), confirmListener);
        builder.setNegativeButton(getResources().getString(cancelString), cancelListener);
        mDialog = builder.create();
        mDialog.setCancelable(isCancelAble);
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

    public boolean isProgressDialogCancelable;
    public boolean isProgressDialogCancelableOutside;

    protected void showProgressDialog(String messageOne, String messageTwo, boolean cancelable, boolean cancelableOutside) {
        CLog.e(TAG, "showLoading(): " + getClass().getSimpleName());
        isProgressDialogCancelable = cancelable;
        isProgressDialogCancelableOutside = cancelableOutside;
        try {
            getActivity().runOnUiThread(new Runnable() {
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
            CLog.e(TAG, "showLoading() exception>>>" + exception.getMessage());
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
            getActivity().runOnUiThread(new Runnable() {
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
            CLog.e(TAG, "sending exception>>>" + exception.getMessage());
        }
    }


    protected void sendSuccess() {
        CoolLED.removePost();
        try {
            getActivity().runOnUiThread(new Runnable() {
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
            CLog.e(TAG, "sendSuccess exception>>>" + exception.getMessage());
        }
    }

    protected void sendFailed() {
        CoolLED.removePost();
        try {
            getActivity().runOnUiThread(new Runnable() {
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
            CLog.e(TAG, "sendFailed exception>>>" + exception.getMessage());
        }
    }

    protected void dismissProgressDialog() {
        CLog.e(TAG, "dismissLoading(): " + getClass().getSimpleName());
        try {
            getActivity().runOnUiThread(new Runnable() {
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
            CLog.e(TAG, "dismissProgressDialog exception>>>" + exception.getMessage());
        }
    }

    protected void initProgressDialog(String messageOne, String messageTwo, boolean cancelable, boolean cancelableOutside) {
        CLog.e(TAG, "onFunctionalStart showLoading() cancelable: " + cancelable);
        try {
            Context ctx = getActivity();
            if (ctx != null) {
                mProgressDialog = ProgressDialog.showLoading(ctx, messageOne, messageTwo, cancelable, cancelableOutside);
                mProgressDialog.startSend();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.e(TAG, "initProgressDialog exception>>>" + exception.getMessage());
        }
    }



    public Fragment getRootFragment() {
        Fragment fragment = getParentFragment();
        while (fragment.getParentFragment() != null) {
            fragment = fragment.getParentFragment();
        }
        return fragment;

    }

}
