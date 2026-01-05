package com.jtkj.library.infrastructure.network;

public abstract class OnNextListener<T> {
	/**
	 * 请求成功后回调
	 * @param rs Response result
	 */
	public abstract void onNext(T rs);

	/**
	 * 请求异常回调(主动调用)
	 * @param e Throwable error object
	 */
	public void onError(Throwable e) {
	}
}
