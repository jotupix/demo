package com.jtkj.library.commom.dialog.effects;

import android.view.View;
import com.nineoldandroids.animation.ObjectAnimator;

public class Shake extends BaseEffect {
	@Override
	protected void setupShowAnimation(View view) {
		getAnimatorSet().playTogether(
				ObjectAnimator.ofFloat(view, "translationX", 0, .10f, -25, .26f, 25, .42f, -25, .58f, 25, .74f, -25, .90f, 1, 0).setDuration(mDuration)
		);
	}
}
