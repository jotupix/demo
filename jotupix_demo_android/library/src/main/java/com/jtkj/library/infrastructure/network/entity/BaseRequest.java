package com.jtkj.library.infrastructure.network.entity;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.jtkj.library.infrastructure.network.OnNextListener;
import com.jtkj.library.infrastructure.network.PretreatmentSubscriber;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * 请求数据封装类
 */
public abstract class BaseRequest<T> implements Func1<BaseResultEntity<T>, BaseResultEntity> {
	private RxAppCompatActivity mAty;

	public BaseRequest() {}

	/**
	 * subscriber预处理类
	 */
	protected PretreatmentSubscriber mPretreatmentSubscriber;

	public BaseRequest(RxAppCompatActivity rxAty, OnNextListener listener) {
		mPretreatmentSubscriber = new PretreatmentSubscriber(rxAty, listener);
		mAty = rxAty;
	}

	public BaseRequest(RxAppCompatActivity rxAty, OnNextListener listener, String message) {
		mPretreatmentSubscriber = new PretreatmentSubscriber(rxAty, listener, message);
		mAty = rxAty;
	}

	public BaseRequest(RxAppCompatActivity rxAty, OnNextListener listener, boolean cancel) {
		mPretreatmentSubscriber = new PretreatmentSubscriber(rxAty, listener, cancel);
		mAty = rxAty;
	}

	public BaseRequest(RxAppCompatActivity rxAty, OnNextListener listener, String message, boolean cancel) {
		mPretreatmentSubscriber = new PretreatmentSubscriber(rxAty, listener, cancel, message);
		mAty = rxAty;
	}

	public BaseRequest(RxAppCompatActivity rxAty, OnNextListener listener, boolean showDialog, boolean cancel) {
		mPretreatmentSubscriber = new PretreatmentSubscriber(rxAty, listener, showDialog, true);
		mAty = rxAty;
	}

	/**
	 * 获取当前rx生命周期基类
	 */
	public RxAppCompatActivity getRxAppCompatActivity() {
		return mAty;
	}

	/**
	 * 回调subscriber
	 */
	public Subscriber getSubscriber() {
		return mPretreatmentSubscriber;
	}

	@Override
	public BaseResultEntity call(BaseResultEntity<T> networkResult) {
		/*if ("unlogin".equals(networkResult.getStatus())) {
			EventAgent.postSticky(new IEvent.UnLoginEvent(IEvent.CODE_UNLOGIN));
		}*/
		return networkResult;
	}

	/**
	 * 参数
	 */
//	public abstract Observable getObservable(Object methods);
	public abstract Observable getObservable();
}
