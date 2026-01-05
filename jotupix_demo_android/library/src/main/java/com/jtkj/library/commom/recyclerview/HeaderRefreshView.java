package com.jtkj.library.commom.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.jtkj.library.R;


public class HeaderRefreshView extends FrameLayout implements RefreshTrigger {
	public HeaderRefreshView(Context context) {
		this(context, null);
	}

	public HeaderRefreshView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HeaderRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.recycler_head_refresh_view, this);
	}

	@Override
	public void onStart(boolean automatic, int headerHeight, int finalHeight) {
	}

	@Override
	public void onMove(boolean finished, boolean automatic, int moved) {
	}

	@Override
	public void onRefresh() {
	}

	@Override
	public void onRelease() {
	}

	@Override
	public void onComplete() {
	}

	@Override
	public void onReset() {
	}
}