package com.jtkj.library.infrastructure.mvp;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public interface PresenterFactory<P extends Presenter> {
	P create();
}
