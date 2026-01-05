package com.jtkj.library.infrastructure.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Toast;

import com.jtkj.library.Constants;
import com.jtkj.library.R;
import com.jtkj.library.commom.logger.CLog;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import rx.Subscriber;

public class PretreatmentSubscriber<T> extends Subscriber<T> {
    private static final String TAG = "PretreatmentSubscriber";

    private WeakReference<Context> mContext;

    /**
     * 是否需要弹框
     */
    private boolean mShowDialog = true;

    private ProgressDialog mDialog;

    /**
     * 回调接口
     */
    private OnNextListener mOnNextListener;

    public PretreatmentSubscriber(Context ctx, OnNextListener listener) {
        mOnNextListener = listener;
        mContext = new WeakReference<>(ctx);
        initProgressDialog(null, false);
    }

	/**
	 * @param ctx
	 * @param listener OnNextListener
	 * @param message 弹框消息内容
	 */
    public PretreatmentSubscriber(Context ctx, OnNextListener listener, String message) {
        this.mOnNextListener = listener;
        this.mContext = new WeakReference<>(ctx);
        initProgressDialog(message, false);
    }

    /**
     * @param ctx
     * @param listener OnNextListener
     * @param cancel         是否能cancel
     */
    public PretreatmentSubscriber(Context ctx, OnNextListener listener, boolean cancel) {
        mOnNextListener = listener;
        mContext = new WeakReference<>(ctx);
        initProgressDialog(null, cancel);
    }

    /**
     * @param ctx
     * @param listener OnNextListener
     * @param cancel 是否能cancel
	 * @param message 弹框消息内容
     */
    public PretreatmentSubscriber(Context ctx, OnNextListener listener, boolean cancel, String message) {
        mOnNextListener = listener;
        mContext = new WeakReference<>(ctx);
        initProgressDialog(message, cancel);
    }

    /**
     * @param ctx
     * @param listener OnNextListener
     * @param showDialog 是否需要进度框
     * @param cancel 是否能取消进度框
     */
    public PretreatmentSubscriber(Context ctx, OnNextListener listener, boolean showDialog, boolean cancel) {
        mOnNextListener = listener;
        mContext = new WeakReference<>(ctx);
        setShowDialog(showDialog);
        if (showDialog) {
            initProgressDialog(null, cancel);
        }
    }

    /**
     * @param ctx
     * @param listener OnNextListener
     * @param showDialog 是否需要进度框
     * @param cancel 是否能取消进度框
	 * @param message 弹框消息内容
     */
    public PretreatmentSubscriber(Context ctx, OnNextListener listener, boolean showDialog, boolean cancel, String message) {
        mOnNextListener = listener;
        mContext = new WeakReference<>(ctx);
        setShowDialog(showDialog);
        if (showDialog) {
            initProgressDialog(message, cancel);
        }
    }

    private void initProgressDialog(String message, boolean cancel) {
		Context context = mContext.get();
        if (mDialog == null && context != null) {
            mDialog = new ProgressDialog(context);
            if (!TextUtils.isEmpty(message)) {
                mDialog.setMessage(message);
            } else {
                mDialog.setMessage(mContext.get().getString(R.string.base_operating));
            }
            mDialog.setCanceledOnTouchOutside(cancel);
            mDialog.setCancelable(cancel);
            if (cancel) {
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        onCancelProgress();
                    }
                });
            }
        }
    }

	public void onCancelProgress() {
		if (!isUnsubscribed()) {
			unsubscribe();//取消对observable的订阅(也取消了请求)
		}
	}

	public boolean isShowDialog() {
		return mShowDialog;
	}

	public void setShowDialog(boolean showDialog) {
		mShowDialog = showDialog;
	}

    private void showProgressDialog() {
        if (!isShowDialog())
            return;
        Context context = mContext.get();
        if (mDialog == null || context == null)
            return;
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (!isShowDialog())
            return;
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        showProgressDialog();
    }

    @Override
    public void onCompleted() {
        dismissProgressDialog();
    }

    @Override
    public void onError(Throwable e) {
        Context ctx = mContext.get();
        if (ctx == null)
            return;
        if (e instanceof SocketTimeoutException) {
            Toast.makeText(ctx, R.string.check_network, Toast.LENGTH_SHORT).show();
            CLog.i(TAG, "onError(): SocketTimeoutException");
        } else if (e instanceof ConnectException) {
            Toast.makeText(ctx, R.string.check_network, Toast.LENGTH_SHORT).show();
            CLog.i(TAG, "onError(): ConnectException");
        } else {
            Toast.makeText(ctx, R.string.check_network, Toast.LENGTH_SHORT).show();
            CLog.i(TAG, "onError(): " + e.getMessage());
            if (Constants.DEBUG_TOGGLE) e.printStackTrace();
        }
        dismissProgressDialog();
        if (mOnNextListener != null) {
            mOnNextListener.onError(e);
        }
    }

    @Override
    public void onNext(T t) {
        if (mOnNextListener != null) {
            CLog.i(TAG, "onNext: " + t.toString());
            mOnNextListener.onNext(t);
        }
    }
}