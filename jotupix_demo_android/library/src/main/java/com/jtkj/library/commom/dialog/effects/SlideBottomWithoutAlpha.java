package com.jtkj.library.commom.dialog.effects;

import android.view.View;
import com.nineoldandroids.animation.ObjectAnimator;

public class SlideBottomWithoutAlpha extends BaseEffect {
	@Override
	protected void setupShowAnimation(View view) {
		getAnimatorSet().playTogether(
				ObjectAnimator.ofFloat(view, "translationY", 1500, 0).setDuration(mDuration)
		);
	}
}
