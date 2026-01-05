package com.jtkj.library.commom.noscroll;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoScrollerViewPager extends ViewPager {

	private boolean noScroll = true;

	public NoScrollerViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoScrollerViewPager(Context context) {
		super(context);
	}

	public void setNoScroll(boolean noScroll) {
		this.noScroll = noScroll;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {

		if (noScroll) {
			return false;
		} else {
			return super.onInterceptTouchEvent(arg0);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (noScroll) {
			return false;
		} else {
			return super.onTouchEvent(arg0);
		}

	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(item, smoothScroll);
	}

	@Override
	public void setCurrentItem(int item) {
		super.setCurrentItem(item);
	}
}
