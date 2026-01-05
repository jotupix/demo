package com.jtkj.library.commom.dialog.effects;

import android.view.View;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.view.ViewHelper;

public abstract class BaseEffect {
	private static final int DURATION = 400;//default value is 700 in first version

	protected long mDuration = DURATION;

	private AnimatorSet mAnimatorSet;

	{
		mAnimatorSet = new AnimatorSet();
	}

	protected abstract void setupShowAnimation(View view);

	protected void setupDismissAnimation(View view, Callback cb) {
		cb.onEnd();
	}

	public void dismiss(View view, Callback cb) {
		reset(view);
		setupDismissAnimation(view, cb);
		mAnimatorSet.start();
	}

	public interface Callback {
		public void onEnd();
	}

	public void start(View view) {
		reset(view);
		setupShowAnimation(view);
		mAnimatorSet.start();
	}

	public void reset(View view) {
		ViewHelper.setPivotX(view, view.getMeasuredWidth() / 2.0f);
		ViewHelper.setPivotY(view, view.getMeasuredHeight() / 2.0f);
	}

	public AnimatorSet getAnimatorSet() {
		return mAnimatorSet;
	}

	public void setDuration(long duration) {
		this.mDuration = duration;
	}
}
