package com.jtkj.library.commom.dialog.effects;

import android.view.View;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlipH extends BaseEffect {
	@Override
	protected void setupShowAnimation(View view) {
		getAnimatorSet().playTogether(
				ObjectAnimator.ofFloat(view, "rotationY", -90, 0).setDuration(mDuration)
		);
	}
}
