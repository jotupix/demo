package com.jtkj.library.infrastructure.base;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public abstract class LazyFragment extends Fragment {
	private boolean isVisible;

	private boolean isPrepared;

	private boolean isFirstLoad = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		isFirstLoad = true;
		View view = initViews(inflater, container, savedInstanceState);
		isPrepared = true;
		lazyLoad();
		return view;
	}

//	@Override
//	public void setUserVisibleHint(boolean isVisibleToUser) {
//		super.setUserVisibleHint(isVisibleToUser);
//		if (getUserVisibleHint()) {
//			isVisible = true;
//			onVisible();
//		} else {
//			isVisible = false;
//			onInvisible();
//		}
//	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			isVisible = true;
			onVisible();
		} else {
			isVisible = false;
			onInvisible();
		}
	}

	protected void onVisible() {
		lazyLoad();
	}

	protected void onInvisible() {}

	protected void lazyLoad() {
//		if (!isPrepared || !isVisible || !isFirstLoad) {
		if (!isPrepared || !isFirstLoad) {
			return;
		}
		isFirstLoad = false;
		initializeData();
	}

	protected abstract View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	protected abstract void initializeData();
}
