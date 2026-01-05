package com.jtkj.library.infrastructure.mvp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import android.widget.Toast;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.jtkj.library.Constants;
import com.jtkj.library.R;
import com.jtkj.library.commom.logger.CLog;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import rx.Observable;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 * RxAppCompatActivity会监听Activity的销毁事件，通过使用compose()在流中加入拦截器，
 * 效果：在subscribe()之前拦截Observable并检测Activity是否销毁，如果已销毁则中断Observable且取消订阅。
 */
public abstract class ViewActivity extends RxAppCompatActivity implements IView, LoaderManager.LoaderCallbacks<Presenter> {
	private static final String TAG = ViewActivity.class.getSimpleName();

	private final static int BASE_LOADER_ID = 999999;

	private int mTimeout = R.string.check_network, mConnect = R.string.check_network,
			mError = R.string.check_network, mProgressMsgStrId = R.string.base_operating;

	protected ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getSupportLoaderManager().initLoader(BASE_LOADER_ID, null, this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		getSupportLoaderManager().destroyLoader(BASE_LOADER_ID);
	}


    /**
	 * @param loader      the PresenterLoader</br>
	 * @param data        the Presenter instance just created</br>
	 * @param placeholder just use to override LoaderManager.Callback.onCreateLoader</br>
	 */
	public abstract void onLoadFinished(Loader<Presenter> loader, Presenter data, int placeholder);

	@Override
	public void onLoadFinished(Loader<Presenter> loader, Presenter data) {
		data.attachBaseView(this);
		switch (loader.getId()) {
			case BASE_LOADER_ID:
				break;
			default:
				onLoadFinished(loader, data, -1);
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Presenter> loader) {
	}

	@Override
	public Observable.Transformer composeLifecycle() {
		return bindToLifecycle();
	}

	@Override
	public void onFunctionalStart(boolean autoTips) {
		if (autoTips)
			showLoading(true);
	}

	@Override
	public void onFunctionalCompleted(boolean autoTips) {
		//if (autoTips)//调用页面在自己弹出loadingDialog的时候，在网络异常时也dissmiss掉
		dismissLoading();
	}

	@Override
	public void onFunctionalError(Throwable e, boolean autoTips) {
		//if (autoTips)
		showErrorToast(e);
		onFunctionalCompleted(autoTips);
	}

	protected void showLoading(final boolean cancelable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CLog.d(TAG, "showLoading().cancelable=" + cancelable);
				if (mProgressDialog == null) {
					initProgressDialog(cancelable);
				} else {
					if (!mProgressDialog.isShowing()) {
						mProgressDialog.setCanceledOnTouchOutside(cancelable);
						mProgressDialog.setCancelable(cancelable);
					}
				}
				if (!mProgressDialog.isShowing()) {
					mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {

						}
					});
					mProgressDialog.show();
				}
			}
		});
	}

	public void progressDialogCancel(DialogInterface dialog) {}

	protected void dismissLoading() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CLog.d(TAG, "dismissLoading()");
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		});
	}

	protected void showErrorToast(final Throwable e) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CLog.d(TAG, "showErrorToast()");
				if (e instanceof UnknownHostException) {
					//todo 添加无网络时的空白界面
				} else if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
					Toast.makeText(ViewActivity.this, mTimeout, Toast.LENGTH_SHORT).show();
					CLog.i(TAG, "onError(): SocketTimeoutException");
				} else if (e instanceof ConnectException) {
					Toast.makeText(ViewActivity.this, mConnect, Toast.LENGTH_SHORT).show();
					CLog.i(TAG, "onError(): ConnectException");
				} else {
					Toast.makeText(ViewActivity.this, mError, Toast.LENGTH_SHORT).show();
					CLog.i(TAG, "onError(): " + (e != null ? e.getMessage() : ""));
					if (e != null && Constants.DEBUG_TOGGLE)
						e.printStackTrace();
				}
			}
		});
	}

	private void initProgressDialog(boolean cancelable) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(mProgressMsgStrId));
		mProgressDialog.setCanceledOnTouchOutside(cancelable);
		mProgressDialog.setCancelable(cancelable);
		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mProgressDialog.dismiss();
			}
		});
	}
}
