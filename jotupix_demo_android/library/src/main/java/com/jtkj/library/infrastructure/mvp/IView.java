package com.jtkj.library.infrastructure.mvp;

import rx.Observable;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public interface IView {
	void onFunctionalStart(boolean autoTips);
	void onFunctionalCompleted(boolean autoTips);
	void onFunctionalError(Throwable e, boolean autoTips);

	Observable.Transformer composeLifecycle();
}
