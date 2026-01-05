package com.jtkj.library.commom.noscroll;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class NoScrollerListView extends ListView {

	public NoScrollerListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NoScrollerListView(Context context) {
		super(context);

	}

	public NoScrollerListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
