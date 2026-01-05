package com.jtkj.library.infrastructure.mvp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.jtkj.library.Constants;
import com.jtkj.library.R;
import com.jtkj.library.commom.logger.CLog;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import rx.Observable;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 * RxFragment会监听Fragment的销毁事件，通过使用compose()在流中加入拦截器，
 * 效果：在subscribe()之前拦截Observable并检测Fragment是否销毁，如果已销毁则中断Observable且取消订阅。
 */
public abstract class ViewFragment extends RxFragment implements IView, LoaderManager.LoaderCallbacks<Presenter> {
    private static final String TAG = ViewFragment.class.getSimpleName();

    public static final String ARGS_TIMEOUT = "args_timeout";
    public static final String ARGS_CONNECT = "args_connect";
    public static final String ARGS_ERROR = "args_error";
    public static final String ARGS_PROGRESS_MSG = "args_progress_msg";
    public static final String ARGS_CANCELABLE = "args_cancelable";

    private static final int DEFAULT_TIMEOUT = R.string.check_network;
    private static final int DEFAULT_CONNECT = R.string.check_network;
    private static final int DEFAULT_ERROR = R.string.check_network;
    private static final int DEFAULT_PROGRESS_MSG = R.string.base_operating;
    private static final boolean DEFAULT_CANCELABLE = true;

    private int mTimeout = DEFAULT_TIMEOUT, mConnect = DEFAULT_CONNECT, mError = DEFAULT_ERROR, mProgressMsgStrId = DEFAULT_PROGRESS_MSG;
    private boolean mCancelable = DEFAULT_CANCELABLE;

    protected ProgressDialog mLoadingDialog;

//	/**
//	 * @param id </br>
//	 * @param args Presenter creating arguments</br>
//	 * @param placeholder just use to override LoaderManager.Callback.onCreateLoader</br>
//	 * @return the Presenter instance
//	 */
//	public abstract Loader<IPresenter> onCreateLoader(int id, Bundle args, int placeholder);

    @Override
    public void onLoadFinished(Loader<Presenter> loader, Presenter data) {
        data.attachBaseView(this);
        onLoadFinished(loader, data, -1);
    }

    /**
     * @param loader      the PresenterLoader</br>
     * @param data        the Presenter instance just created</br>
     * @param placeholder just use to override LoaderManager.Callback.onCreateLoader</br>
     */
    public abstract void onLoadFinished(Loader<Presenter> loader, Presenter data, int placeholder);

    @Override
    public void onLoaderReset(Loader<Presenter> loader) {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTimeout = bundle.getInt(ARGS_TIMEOUT, DEFAULT_TIMEOUT);
            mConnect = bundle.getInt(ARGS_CONNECT, DEFAULT_CONNECT);
            mError = bundle.getInt(ARGS_ERROR, DEFAULT_ERROR);
            mProgressMsgStrId = bundle.getInt(ARGS_PROGRESS_MSG, DEFAULT_PROGRESS_MSG);
            mCancelable = bundle.getBoolean(ARGS_CANCELABLE, DEFAULT_CANCELABLE);
        }
    }

    @Override
    public Observable.Transformer composeLifecycle() {
        return bindToLifecycle();
    }

    public void onFunctionalStart(boolean autoTips) {
        if (autoTips) {
            CLog.e(TAG, "onFunctionalStart showLoading(): " + getClass().getSimpleName());
            showLoading(true);
        }
    }

    public void onFunctionalCompleted(boolean autoTips) {
        //if (autoTips) //调用页面在自己弹出loadingDialog的时候，在网络异常时也dissmiss掉
        dismissLoading();
    }

    public void onFunctionalError(Throwable e, boolean autoTips) {
        //if (autoTips)
        showErrorToast(e);
        onFunctionalCompleted(autoTips);
    }

    protected void showLoading(final boolean cancelable) {
        CLog.e(TAG, "showLoading(): " + getClass().getSimpleName());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoadingDialog == null) {
                    initProgressDialog(cancelable);
                } else {
                    if (!mLoadingDialog.isShowing()) {
                        CLog.e(TAG, "showLoading() reset loading cancelable: " + cancelable);
                        mLoadingDialog.setCanceledOnTouchOutside(cancelable);
                        mLoadingDialog.setCancelable(cancelable);
                    }
                }
                if (!mLoadingDialog.isShowing()) {
                    CLog.e(TAG, "showLoading() showDialog");
                    mLoadingDialog.show();
                }
            }
        });
    }

    protected void dismissLoading() {
        CLog.e(TAG, "dismissLoading(): " + getClass().getSimpleName());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
            }
        });
    }

    protected void showErrorToast(final Throwable e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CLog.d(TAG, "showErrorToast()");
                Context ctx = getContext();
                if (ctx != null) {
                    if (e instanceof SocketTimeoutException) {
                        Toast.makeText(ctx, mTimeout, Toast.LENGTH_SHORT).show();
                        CLog.i(TAG, "onError(): SocketTimeoutException");
                    } else if (e instanceof ConnectException) {
                        Toast.makeText(ctx, mConnect, Toast.LENGTH_SHORT).show();
                        CLog.i(TAG, "onError(): ConnectException");
                    } else {
                        Toast.makeText(ctx, mError, Toast.LENGTH_SHORT).show();
                        CLog.i(TAG, "onError(): " + (e != null ? e.getMessage() : ""));
                        if (e != null && Constants.DEBUG_TOGGLE) e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initProgressDialog(boolean cancelable) {
        Context ctx = getContext();
        if (ctx != null) {
            CLog.e(TAG, "onFunctionalStart showLoading() cancelable: " + cancelable);
            mLoadingDialog = new ProgressDialog(ctx);
            mLoadingDialog.setMessage(ctx.getString(mProgressMsgStrId));
            mLoadingDialog.setCanceledOnTouchOutside(cancelable);
            mLoadingDialog.setCancelable(cancelable);
            if (cancelable) {
                mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mLoadingDialog.dismiss();
                    }
                });
            }
        }
    }
}
