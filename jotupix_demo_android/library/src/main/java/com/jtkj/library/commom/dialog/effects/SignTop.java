package com.jtkj.library.commom.dialog.effects;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.jtkj.library.R;


/*
 * Copyright 2014 litao
 * https://github.com/sd6352051/NiftyDialogEffects
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SignTop extends BaseEffect {
	@Override
	public void reset(View view) {
		View v = view.findViewById(R.id.common_dialog_parent_panel);
		if (v != null) {
			ViewHelper.setPivotX(v, v.getMeasuredWidth() / 2.0f);
			ViewHelper.setPivotY(v, 0);
		} else {
			ViewHelper.setPivotX(view, view.getMeasuredWidth() / 2.0f);
			ViewHelper.setPivotY(view, view.getMeasuredHeight() / 2.0f);
		}
	}

    @Override
    protected void setupShowAnimation(View view) {
        getAnimatorSet().playTogether(
                ObjectAnimator.ofFloat(view, "rotationX", -90, 0).setDuration(mDuration / 2),
//                ObjectAnimator.ofFloat(view, "translationY", 300, 0).setDuration(mDuration),
                ObjectAnimator.ofFloat(view, "alpha", 0, 1).setDuration(mDuration / 2)
        );
    }

	@Override
	protected void setupDismissAnimation(View view, final Callback cb) {
		AnimatorSet set = getAnimatorSet();
		set.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			public void onAnimationEnd(Animator animation) {
				cb.onEnd();
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});

		set.playTogether(
				ObjectAnimator.ofFloat(view, "rotationX", 0, -90).setDuration(mDuration / 2),
				ObjectAnimator.ofFloat(view, "alpha", 1, 0).setDuration(mDuration / 2)
		);
	}
}
