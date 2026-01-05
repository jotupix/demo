package com.jtkj.library.infrastructure.mvp;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public interface IPresenter<V extends IView, E extends IEvent> {
	void attachBaseView(IView baseView);
	void attach(V view);
	void detach();
	void onEvent(E e);
}

