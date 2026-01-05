package com.jtkj.library.commom.recyclerview;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

	private int mSpace;

	public SpaceItemDecoration(int space) {
		mSpace = space;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//		if (parent.getChildPosition(view) != 0)
//			outRect.top = mSpace;
		//不是第一个的格子都设一个左边和底部的间距
		outRect.left = mSpace;
		outRect.right = mSpace;
		outRect.bottom = mSpace;
		//由于每行都只有2个，所以第一个都是3的倍数，把左边距设为0
		int position = parent.getChildLayoutPosition(view);
		if (position == 0) {
			outRect.bottom = 0;
		} else if (position == 1) {//position % 2 == 0 ||
			outRect.left = 0;
			outRect.right = 0;
		} else if (position > 1) {
			if (position % 2 == 0) {
				outRect.right = 0;
			}
		}
	}
}
