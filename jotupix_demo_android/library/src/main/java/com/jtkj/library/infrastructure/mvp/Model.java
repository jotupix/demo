package com.jtkj.library.infrastructure.mvp;

import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.infrastructure.eventbus.EventAgent;
import com.jtkj.library.infrastructure.network.entity.BaseResultEntity;
import com.jtkj.library.infrastructure.network.exception.RetryException;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public abstract class Model<T> implements IModel<T> {//}, Func1<BaseResultEntity<T>, BaseResultEntity> {
	public Observable execute(Object params, int functionalCode, final Object extraParam, final String requestPage) {
		Observable observable = prepareExecute(getObservable(params, functionalCode));
		return resultFilter(observable, functionalCode, extraParam, requestPage);
	}

	private Observable prepareExecute(Observable observable) {
		return observable
				.subscribeOn(Schedulers.io())//http请求线程
				.observeOn(Schedulers.io())//AndroidSchedulers.mainThread())//回调所在线程
				.unsubscribeOn(Schedulers.io())//http请求线程
				.retryWhen(new RetryException())//失败后retry
				;
	}

	private Observable resultFilter(Observable observable, final int functionalCode, final Object extraParam, final String requestPage) {
		return observable
//				.onErrorResumeNext(Observable.<String>empty())
//				.map(this)//结果判断
				.map(new Func1<BaseResultEntity<T>, BaseResultEntity>() {
					@Override
					public BaseResultEntity call(BaseResultEntity<T> networkResult) {
						CLog.e("NetworkResult.call", "BaseResultEntity = " + networkResult);
						networkResult.requestCode = functionalCode;
						networkResult.extraParam = extraParam;
						networkResult.requestPage = requestPage;
						if (networkResult.getCode() == 10102) {
							EventAgent.postSticky(new IEvent.UnLoginEvent(IEvent.CODE_UNLOGIN));
						}
						/*if ("unlogin".equals(networkResult.getStatus())) {
							EventAgent.postSticky(new IEvent.UnLoginEvent(IEvent.CODE_UNLOGIN));
						}*/
						return networkResult;
					}
				});
	}

	public void execute(final Observable observable, Subscriber subscriber, final int functionalCode, final Object extraParam, final String requestPage) {
		if (observable == null || subscriber == null) return;

//		if (!NetworkUtils.isNetworkAvailable(request.getRxAppCompatActivity())) {//换成静态变量
//			Toast.makeText(request.getRxAppCompatActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();//给presenter决定怎么处理
//			return;
//		}
		observable
				.subscribeOn(Schedulers.io())//http请求线程
				.observeOn(Schedulers.io())//AndroidSchedulers.mainThread())//回调所在线程
				.unsubscribeOn(Schedulers.io())//http请求线程
				.retryWhen(new RetryException())//失败后的retry配置
//				.onErrorResumeNext(Observable.<String>empty())
//				.map(this)//结果判断
				.map(new Func1<BaseResultEntity<T>, BaseResultEntity>() {
					@Override
					public BaseResultEntity call(BaseResultEntity<T> networkResult) {
						CLog.e("NetworkResult.call", "BaseResultEntity = " + networkResult);
						networkResult.requestCode = functionalCode;
						networkResult.extraParam = extraParam;
						networkResult.requestPage = requestPage;
						if (networkResult.getCode() == 10102) {
							EventAgent.postSticky(new IEvent.UnLoginEvent(IEvent.CODE_UNLOGIN));
						}
						/*if ("unlogin".equals(networkResult.getStatus())) {
							EventAgent.postSticky(new IEvent.UnLoginEvent(IEvent.CODE_UNLOGIN));
						}*/
						return networkResult;
					}
				})
				.subscribe(subscriber);//数据回调
	}

//	@Override
//	public BaseResultEntity call(BaseResultEntity<T> networkResult) {
//		if ("unlogin".equals(networkResult.getStatus())) {
//			EventAgent.postSticky(new IEvent.UnLoginEvent(IEvent.CODE_UNLOGIN));
//		}
//		return networkResult;
//	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj) {
		return (T) obj;
	}
}