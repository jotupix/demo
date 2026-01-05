package com.jtkj.library.infrastructure.mvp;

import com.jtkj.library.Constants;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.infrastructure.network.entity.BaseResultEntity;

import java.net.ConnectException;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public abstract class Presenter<M extends Model, V extends IView> {
	private static final String TAG = Presenter.class.getSimpleName();

	//状态 success:成功，warning:警告，error:错误
	private final static String RESPONSE_STATUS_SUCCESS = "success";
	private final static String RESPONSE_STATUS_WARNING = "warning";
	private final static String RESPONSE_STATUS_ERROR = "error";

	protected static final int FUNCTIONAL_CODE = -1;

	public IView mBaseView;
	public M mModel;

	public static class BaseGenerator implements PresenterFactory<Presenter> {
		@Override
		public Presenter create() {
			return new Presenter() {
				@Override
				public void attach(IView view) {
					CLog.d(TAG, "attach");
					mBaseView = view;
				}

				@Override
				public void detach() {
					CLog.d(TAG, "detach");
					mBaseView = null;
				}
			};
		}
	}

	public abstract void attach(V view);

	public abstract void detach();

	public void attachBaseView(IView baseView) {
		CLog.d(TAG, "attachBaseView");
		mBaseView = baseView;
	}

	public void onStart(boolean autoTips) {
		CLog.d(TAG, "onStart");
		mBaseView.onFunctionalStart(autoTips);
	}

	@SuppressWarnings("unchecked")
	public void onNext(BaseResultEntity t) {
		CLog.d(TAG, "onNext");
		mModel.onNext(t);
	}

	public void onCompleted(boolean autoTips) {
		CLog.d(TAG, "onCompleted");
		mBaseView.onFunctionalCompleted(autoTips);
	}

	public void onError(Throwable e, boolean autoTips) {
		CLog.d(TAG, "onError:" + (e != null ? e.toString() : "Unknown Error"));
		mBaseView.onFunctionalError(e, autoTips);
	}

	/**
	 * 请求数据，数据源可以是网络、内存、硬盘或其他，调用者不需要关心<br>
	 * 当满足一下三个条件时使用：
	 * 1.要请求的数据不需要传参数，内部最终实例化{@link com.jtkj.library.infrastructure.network.entity.EmptyEntity}，会带上公共参数<br>
	 * 2.Presenter只包含一个请求，内部采用默认的FUNCTIONAL_CODE做标识<br>
	 * 3.View使用默认的请求过程提醒需求，内部在请求开始时显示LoadingDialog，结束时显示Toast<br>
	 */
	public void execute() {
		execute(null, FUNCTIONAL_CODE);
	}

	/**
	 * 请求数据，数据源可以是网络、内存、硬盘或其他，调用者不需要关心<br>
	 * 当满足一下两个条件时使用：
	 * 1.Presenter只包含一个请求，内部采用默认的FUNCTIONAL_CODE做标识<br>
	 * 2.View使用默认的请求过程提醒需求，内部在请求开始时显示LoadingDialog，结束时显示Toast<br>
	 *
	 * @param params 请求参数（公共参数除外）
	 */
	public void execute(Object params) {
		execute(params, FUNCTIONAL_CODE);
	}

	/**
	 * 请求数据，数据源可以是网络、内存、硬盘或其他，调用者不需要关心<br>
	 * 当满足一下两个条件时使用：
	 * 1.要请求的数据不需要传参数，内部最终实例化{@link com.jtkj.library.infrastructure.network.entity.EmptyEntity}，会带上公共参数<br>
	 * 2.View使用默认的请求过程提醒需求，内部在请求开始时显示LoadingDialog，结束时显示Toast<br>
	 */
	public void execute(final int functionalCode) {
		execute(null, functionalCode, true);
	}

	/**
	 * 请求数据，数据源可以是网络、内存、硬盘或其他，调用者不需要关心<br>
	 * 当满足一下两个条件时使用：
	 * 1.Presenter包含多个请求，需要显式指定过程标识<br>
	 * 2.View使用默认的请求过程提醒即可满足需求，内部在请求开始时显示LoadingDialog，结束时显示Toast<br>
	 *
	 * @param params         请求参数（公共参数除外）
	 * @param functionalCode 过程标识
	 */
	public void execute(Object params, final int functionalCode) {
		execute(params, functionalCode, true);
	}

	/**
	 * 请求数据，数据源可以是网络、内存、硬盘或其他，调用者不需要关心<br>
	 * 当满足一下三个条件时使用：
	 * 1.Presenter包含多个请求，需要显式指定过程标识<br>
	 * 2.View需要自定义的请求过程提醒才能满足需求，
	 * 3.View可以按需重写{@link IView}的onFunctionalStart(boolean autoTips)、onFunctionalCompleted(boolean autoTips)、onFunctionalError(Throwable e, boolean autoTips)方法<br>
	 *
	 * @param params         请求参数（公共参数除外）
	 * @param functionalCode 过程标识
	 * @param autoTips       是否使用默认的请求过程提醒
	 */
	@SuppressWarnings("unchecked")
	public void execute(Object params, final int functionalCode, final boolean autoTips) {
		execute(params, functionalCode, autoTips, null, null);
	}

	/**
	 * 请求数据，数据源可以是网络、内存、硬盘或其他，调用者不需要关心<br>
	 * 当满足以下五个条件时使用：
	 * 1.Presenter包含多个请求，需要显式指定过程标识<br>
	 * 2.View需要自定义的请求过程提醒才能满足需求，
	 * 3.View可以按需重写{@link IView}的onFunctionalStart(boolean autoTips)、onFunctionalCompleted(boolean autoTips)、onFunctionalError(Throwable e, boolean autoTips)方法<br>
	 * 4.Presenter需要根据某些参数区分请求
	 * 5.Presenter需要根据请求页面判断是否需要回调
	 *
	 * @param params         请求参数（公共参数除外）
	 * @param functionalCode 过程标识
	 * @param autoTips       是否使用默认的请求过程提醒
	 * @param extraParam     需要返回的参数
	 * @param requestPage    发送请求的页面
	 */
	@SuppressWarnings("unchecked")
	public void execute(Object params, final int functionalCode, final boolean autoTips, final Object extraParam, final String requestPage) {
		if (!Constants.isConnected){//如果没有网络
			Presenter.this.onError(new ConnectException(), autoTips);
			return;
		}
		mModel.execute(params, functionalCode, extraParam, requestPage)


				.doOnUnsubscribe(new Action0() {
					@Override
					public void call() {
						CLog.i(TAG, "Unsubscribe in " + getClass().getSimpleName());
					}
				})
				.compose(mBaseView.composeLifecycle())//在操作符之后绑定才能生效
				.subscribe(new Subscriber<BaseResultEntity>() {//数据回调
					@Override
					public void onStart() {
						CLog.e(TAG, "new Subscriber: onStart");
						Presenter.this.onStart(autoTips);
					}

					@Override
					public void onNext(final BaseResultEntity o) {
						CLog.e(TAG, "new Subscriber: onNext");
						Presenter.this.onNext(o);
					}

					@Override
					public void onCompleted() {
						CLog.e(TAG, "new Subscriber: onCompleted");
						Presenter.this.onCompleted(autoTips);
						unsubscribe();
					}

					@Override
					public void onError(Throwable e) {
						CLog.e(TAG, "new Subscriber: onError => " + e.getMessage());
						Presenter.this.onError(e, autoTips);
						unsubscribe();
					}
				});
	}

	@SuppressWarnings("unchecked")
	public void execute(Object params, final int functionalCode, final Subscriber subscriber) {
		mModel.execute(params, functionalCode, null, null)
				.doOnUnsubscribe(new Action0() {
					@Override
					public void call() {
						CLog.i(TAG, "Unsubscribe in " + getClass().getSimpleName());
					}
				})
				.compose(mBaseView.composeLifecycle())//在操作符之后绑定才能生效
				.subscribe(subscriber);
	}

	@SuppressWarnings("unchecked")
	public Observable compose(Object params) {
		return compose(params, FUNCTIONAL_CODE);
	}

	@SuppressWarnings("unchecked")
	public Observable compose(Object params, int functionalCode) {
		Observable.Transformer t = mBaseView.composeLifecycle();
		if (t != null) {
			Observable o = mModel.getObservable(params, functionalCode);
			if (o != null) {
				CLog.e(TAG, "compose: params = " + params + ", functionalCode = " + functionalCode);
				return o.compose(t);//绑定生命周期
			} else {
				CLog.e(TAG, "not compose: params = " + params + ", functionalCode = " + functionalCode);
			}
		} else {
			CLog.e(TAG, "Transformer null: params = " + params + ", functionalCode = " + functionalCode);
		}
		return mModel.getObservable(params, functionalCode);
	}

	/**
	 * 请求是否返回success
	 */
	protected static boolean isResponseSuccess(BaseResultEntity rs) {
		return null != rs && rs.getCode() == 0;
	}

	/**
	 * 请求是否返回warning
	 */
	/*protected static boolean isResponseWarning(BaseResultEntity rs) {
		return null != rs && rs.getCode() == 0;
	}*/

	/**
	 * 请求是否返回error
	 */
	/*protected static boolean isResponseError(BaseResultEntity rs) {
		return null != rs && rs.getCode() == 0;
	}*/
}
