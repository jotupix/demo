package com.jtkj.library.commom.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by yfxiong on 2017/3/17.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class Divider extends RecyclerView.ItemDecoration {
	private Drawable mDivider;
	private boolean mIsHorizontalList;
	@Px
	private int mSpace;
	@Px
	private int mLeftOffset;
	@Px
	private int mTopOffset;
	@Px
	private int mRightOffset;
	@Px
	private int mBottomOffset;
	private int mTrimSpacePosition = -1;

	/**
	 *
	 * @param space
	 * @param color
	 * @param isHorizontalList
	 * @param leftOffset
	 * @param topOffset
	 * @param rightOffset
	 * @param bottomOffset
	 * @param trimSpacePosition 不设置某个item底部的mSpace,0是第一个item,如果有添加refreshHeader,那么refreshHeader的position是0,解决recycleview顶部设置有space的问题.
	 */
	public Divider(@Px int space, @ColorInt int color, boolean isHorizontalList
			, @Px int leftOffset, @Px int topOffset, @Px int rightOffset, @Px int bottomOffset, int trimSpacePosition) {
		mSpace = space;
		mDivider = new ColorDrawable(color);
		mIsHorizontalList = isHorizontalList;
		mLeftOffset = leftOffset;
		mTopOffset = topOffset;
		mRightOffset = rightOffset;
		mBottomOffset = bottomOffset;
		mTrimSpacePosition = trimSpacePosition;
	}

	public Divider(@Px int space, @ColorInt int color, boolean isHorizontalList
			, @Px int leftOffset, @Px int topOffset, @Px int rightOffset, @Px int bottomOffset) {
		mSpace = space;
		mDivider = new ColorDrawable(color);
		mIsHorizontalList = isHorizontalList;
		mLeftOffset = leftOffset;
		mTopOffset = topOffset;
		mRightOffset = rightOffset;
		mBottomOffset = bottomOffset;
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (mIsHorizontalList) {
			drawHorizontal(c, parent);
		} else {
			drawVertical(c, parent);
		}
	}

	private void drawVertical(Canvas c, RecyclerView parent) {
		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();
		int childCount = parent.getChildCount() - 1;
		for (int i = 1; i < childCount - 1; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
			final int top = child.getBottom() + params.bottomMargin;
			final int bottom = top + mSpace;
//			CLog.i("Divider", "drawVertical(): top=" + top + ", mTopOffset=" + mTopOffset + ", bottom=" + bottom + ", mBottomOffset=" + mBottomOffset);
			mDivider.setBounds(left + mLeftOffset, top + mTopOffset, right + mRightOffset, bottom + mBottomOffset);
			mDivider.draw(c);
		}
	}

	private void drawHorizontal(Canvas c, RecyclerView parent) {
		final int top = parent.getPaddingTop();
		final int bottom = parent.getHeight() - parent.getPaddingBottom();
		int childCount = parent.getChildCount() - 1;
		for (int i = 1; i < childCount - 1; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
			final int left = child.getRight() + params.rightMargin;
			final int right = left + mSpace;
			mDivider.setBounds(left + mLeftOffset, top + mTopOffset, right + mRightOffset, bottom + mBottomOffset);
			mDivider.draw(c);
		}
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (mIsHorizontalList) {
			outRect.set(0, 0, mSpace, 0);
		} else {
			int position = parent.getChildAdapterPosition(view);
			// 第一个item不设置底部间距
			if (position != mTrimSpacePosition ) {
				outRect.bottom = mSpace;
			}
//			outRect.set(0, 0, 0, mSpace);
		}
	}

	public void setTrimSpacePosition(int position){
		mTrimSpacePosition = position;
	}

	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
