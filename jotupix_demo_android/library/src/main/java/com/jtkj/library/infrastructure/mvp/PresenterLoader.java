package com.jtkj.library.infrastructure.mvp;

import android.content.Context;
import androidx.loader.content.Loader;

import com.jtkj.library.commom.logger.CLog;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 * 特点：
 * 1.在手机状态改变时不会被销毁
 * 2.会在Activity/Fragment不再被使用后由系统回收
 * 3.与Activity/Fragment的生命周期绑定，所以事件会自己分发
 * 4.每一个Activity/Fragment持有自己的Loader对象的引用，所以可以同时存在多个Presenter-Activity/Fragment组合，比如说在ViewPager中
 * 5.可以同步运行，自己确定什么时候数据准备好了可以被传递
 */
public class PresenterLoader<P extends Presenter> extends Loader<P> {
	private static final String TAG = "PresenterLoader";

	private final PresenterFactory<P> factory;
	private P presenter;

	/**
	 * Stores away the application context associated with context. Since Loaders can be used
	 * across multiple activities it's dangerous to store the context directly.
	 *
	 * @param context used to retrieve the application context.
	 * @param factory
	 */
	public PresenterLoader(Context context, PresenterFactory factory) {
		super(context);
		this.factory = factory;
		CLog.i(TAG, "created " + context.getClass().getSimpleName() + "'s Loader with " + factory.getClass().getSimpleName());
	}

	@Override
	protected void onStartLoading() {
		CLog.i(TAG, "onStartLoading()");
		if (presenter != null) {
			deliverResult(presenter);
			return;
		}
		forceLoad();
	}

	@Override
	protected void onForceLoad() {
		CLog.i(TAG, "onForceLoad()");
		presenter = factory.create();//通过工厂来实例化Presenter
		deliverResult(presenter);
	}

	/**
	 * 只会在Activity/Fragment被销毁或主动调用destroyLoader()时被调用
	 */
	@Override
	protected void onReset() {
		CLog.i(TAG, "onReset()");
//		presenter.onDestroyed();
		if (null != presenter) {
			CLog.i(TAG, "Reset " + presenter.getClass().getSimpleName());
			presenter.detach();
			presenter = null;
		}
	}
}

