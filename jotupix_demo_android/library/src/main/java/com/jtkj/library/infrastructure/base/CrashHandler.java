package com.jtkj.library.infrastructure.base;

import android.text.TextUtils;

import com.jtkj.library.commom.logger.CLog;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private static final String TAG = CrashHandler.class.getSimpleName();

	private Thread.UncaughtExceptionHandler mDefaultHandler;

	public CrashHandler() {
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		if (!handlerException(throwable) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, throwable);//让系统默认的异常处理器处理
		}

//		Intent intent = new Intent(Application.this, MainActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		Application.this.startActivity(intent);

//		android.os.Process.killProcess(android.os.Process.myPid());
//		System.exit(1);
	}

	private boolean handlerException(Throwable throwable) {
		if (throwable == null) {
			return true;
		}

		final String msg = throwable.getLocalizedMessage();
		if (TextUtils.isEmpty(msg)) {
			return false;
		}
		CLog.e(TAG, msg);
		throwable.printStackTrace();
		return true;
	}
}
