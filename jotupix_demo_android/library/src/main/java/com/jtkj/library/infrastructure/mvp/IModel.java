package com.jtkj.library.infrastructure.mvp;

import rx.Observable;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public interface IModel<T> {
	/**构建Service对应的请求*/
	Observable getObservable(Object params, int functionalCode);

	void onNext(T entity);
}
