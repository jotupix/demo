package com.jtkj.library.infrastructure.network.exception;


import com.jtkj.library.commom.logger.CLog;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * 获取网络连接状态：
 * https://lorentzos.com/improving-ux-with-rxjava-4440a13b157f
 */
public class RetryException implements Func1<Observable<? extends Throwable>, Observable<?>> {
	private final static String TAG = "RetryException";

	/**次数*/
	private int count = 0;
	/**延迟*/
	private long delay = 3000;
	/**叠加延迟*/
	private long increaseDelay = 3000;

	public RetryException() {
	}

	public RetryException(int count, long delay) {
		this.count = count;
		this.delay = delay;
	}

	@Override
	public Observable<?> call(Observable<? extends Throwable> observable) {
		return observable
				.zipWith(Observable.range(1, count + 1), new Func2<Throwable, Integer, Wrapper>() {
					@Override
					public Wrapper call(Throwable throwable, Integer integer) {
						return new Wrapper(throwable, integer);
					}
				})
				.flatMap(new Func1<Wrapper, Observable<?>>() {
					@Override
					public Observable<?> call(Wrapper wrapper) {
						if (wrapper.throwable instanceof UnknownHostException) {//没有联网
							return Observable.error(wrapper.throwable);
						} else if (
							(wrapper.throwable instanceof ConnectException//网络异常
							|| wrapper.throwable instanceof SocketTimeoutException//超时
							|| wrapper.throwable instanceof TimeoutException)//超时
							&& wrapper.index < count + 1//重试次数(不然会回调onCompleted)
						) {
							CLog.e(TAG, "this is the times of retry call:" + wrapper.index);
							long time = delay + (wrapper.index - 1) * increaseDelay;
							return Observable.timer(time, TimeUnit.MILLISECONDS);//创建Observable，延迟发送一次
						}
						return Observable.error(wrapper.throwable);//重试次数上限，放弃继续重试
					}
				});
	}

	private class Wrapper {
		/**重试次数*/
		private int index;
		private Throwable throwable;

		public Wrapper(Throwable throwable, int index) {
			this.index = index;
			this.throwable = throwable;
		}
	}
}
