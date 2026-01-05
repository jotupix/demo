package com.jtkj.library.commom.tabbar;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.jtkj.library.commom.logger.CLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yfxiong on 2016/12/7.
 * Copyright © yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class TabBar extends LinearLayout {
	private static final String TAG = "TabBar";

	public interface OnTabChangedListener {
		void onTabChanged(int idx);
	}

	private boolean IS_INIT;

	private int mTabCounts;
	private int mCurrentTabIdx = 0;

	private ViewPager mViewPager;
	private List<TabView> mTabViewList;
	private OnTabChangedListener mListener;

	public TabBar(Context ctx) {
		this(ctx, null);
	}

	public TabBar(Context ctx, AttributeSet attrs) {
		this(ctx, attrs, 0);
	}

	public TabBar(Context ctx, AttributeSet attrs, int defStyleAttr) {
		super(ctx, attrs, defStyleAttr);
	}

	public void setViewPager(ViewPager mViewPager) {
		this.mViewPager = mViewPager;
		init();
	}

	public void setOnTabChangedListener(OnTabChangedListener listener) {
		this.mListener = listener;
		isInit();
	}

	public TabView getCurrentItemView() {
		isInit();
		return mTabViewList.get(mCurrentTabIdx);
	}

	public TabView getTabView(int idx) {
		isInit();
		return mTabViewList.get(idx);
	}

	public void removeAllBadge() {
		isInit();
		for (TabView tab : mTabViewList) {
			tab.removeShow();
		}
	}

	private void isInit() {
		if (!IS_INIT) {
			init();
		}
	}

	private void init() {
		IS_INIT = true;
		mTabViewList = new ArrayList<>();
		mTabCounts = getChildCount();

		if (null != mViewPager) {
			if (null == mViewPager.getAdapter()) {
				throw new NullPointerException("ViewPager的adapter为null");
			}
			if (mViewPager.getAdapter().getCount() != mTabCounts) {
				throw new IllegalArgumentException("LinearLayout的子View数量必须和ViewPager条目数量一致");
			}
			mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
		}

		View tab;
		for (int i = 0; i < mTabCounts; i++) {
			tab = getChildAt(i);
			if (tab instanceof TabView) {
				mTabViewList.add((TabView) tab);
				tab.setOnClickListener(new MyOnClickListener(i));
			} else {
				throw new IllegalArgumentException("TabBar的子View必须是TabView");
			}
		}

		mTabViewList.get(mCurrentTabIdx).setIconAlpha(1.0f);
	}

	private class MyOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			//滑动时的透明度渐变动画
			if (positionOffset > 0) {
				mTabViewList.get(position).setIconAlpha(1 - positionOffset);
				mTabViewList.get(position + 1).setIconAlpha(positionOffset);
			}
			mCurrentTabIdx = position;
		}

		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);
			changeTab(position);
		}
	}

	private class MyOnClickListener implements OnClickListener {
		private int currentIndex;

		public MyOnClickListener(int i) {
			currentIndex = i;
		}

		@Override
		public void onClick(View v) {
			//点击前先重置所有按钮的状态
			resetState();
			mTabViewList.get(currentIndex).setIconAlpha(1.0f);
			if (null != mListener) {
				mListener.onTabChanged(currentIndex);
			}
			if (null != mViewPager) {
				//不能使用平滑滚动，否者颜色改变会乱
				mViewPager.setCurrentItem(currentIndex, false);
			}
			//点击是保存当前按钮索引
			mCurrentTabIdx = currentIndex;
		}
	}

	/**
	 * 重置所有按钮的状态
	 */
	private void resetState() {
		for (int i = 0; i < mTabCounts; i++) {
			mTabViewList.get(i).setIconAlpha(0);
		}
	}

	public void changeTab(int idx) {
		CLog.i(TAG, "MainActivity changeTab--idx=" + idx);
		resetState();
		mTabViewList.get(idx).setIconAlpha(1.0f);
		mCurrentTabIdx = idx;
	}

	/*private static final String STATE_INSTANCE = "state_instance";
	private static final String STATE_CURRENT_TAB_IDX = "state_current_tab_idx";

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
		bundle.putInt(STATE_CURRENT_TAB_IDX, mCurrentTabIdx);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mCurrentTabIdx = bundle.getInt(STATE_CURRENT_TAB_IDX);
			resetState();
			mTabViewList.get(mCurrentTabIdx).setIconAlpha(1.0f);
			super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
		} else {
			super.onRestoreInstanceState(state);
		}
	}*/
}
