package com.jtkj.library.commom.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jtkj.library.R;
import com.jtkj.library.commom.dialog.effects.*;
import java.lang.ref.WeakReference;


/**
 * see https://github.com/sd6352051/NiftyDialogEffects
 * see http://tympanus.net/Development/ModalWindowEffects/
 * DialogBuilder builder = DialogBuilder.getInstance(getActivity());
 * builder
 * .setTarget(false)
 * .withTitle("Modal Dialog")
 * .withMessage("This is a modal Dialog.")
 * .withEffect(DialogBuilder.EffectTypes.SignTop)
 * .show();
 */
public class DialogBuilder extends Dialog implements DialogInterface {
	public enum EffectTypes {
		Fadein(FadeIn.class),
		SlideLeft(SlideLeft.class),
		SlideTop(SlideTop.class),
		SlideBottom(SlideBottom.class),
		SlideBottomWithoutAlpha(SlideBottomWithoutAlpha.class),
		SlideRight(SlideRight.class),
		Fall(Fall.class),
		NewsPager(NewsPaper.class),
		FlipH(FlipH.class),
		FlipV(FlipV.class),
		RotateBottom(RotateBottom.class),
		RotateLeft(RotateLeft.class),
		Slit(Slit.class),
		Shake(Shake.class),
		SideFill(SideFall.class),
		SignTop(SignTop.class);

		private Class<? extends BaseEffect> effectsClazz;

		private EffectTypes(Class<? extends BaseEffect> mClass) {
			effectsClazz = mClass;
		}

		public BaseEffect getAnimator() {
			BaseEffect bEffects = null;
			try {
				bEffects = effectsClazz.newInstance();
			} catch (Exception e) {
				throw new Error("Can not init animatorClazz instance");
			}
			return bEffects;
		}
	}

	private final String defTextColor = "#FFFFFFFF";
	private final String defDividerColor = "#11000000";
	private final String defMsgColor = "#FFFFFFFF";
	private final String defDialogColor = "#FFE74C3C";

	private static WeakReference<Context> tmpContext;

	private EffectTypes type = null;

	private LinearLayout mParentLinearLayout;
	private RelativeLayout mMainRelativeLayout;
	private LinearLayout mMsgLinearLayout;
	private LinearLayout mTopLinearLayout;
	private FrameLayout mCustomFrameLayout;
	private LinearLayout mButtonBarLinearLayout;

	private View mDialogView;
	private View mTitleDivider;

	private ImageView mIcon;
	private TextView mTitle;
	private TextView mMessage;

	private TextView mBtnLeft;
	private TextView mBtnRight;

	private View mBtnBarDivider;
	private View mBtnSplitDivider;

	private ImageView mBtnDismiss;

	private int mDuration = -1;

	private static int mOrientation = 1;

	private boolean isCancelable = true;
	private boolean mIsRootView = true;

	private static DialogBuilder instance;

	public DialogBuilder(Context context) {
		super(context);
		init(context);
	}

	public DialogBuilder(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.height = ViewGroup.LayoutParams.MATCH_PARENT;
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		getWindow().setAttributes((WindowManager.LayoutParams) params);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);//去掉系统黑色背景
	}

	public static DialogBuilder getInstance(Context context) {
//		if (instance == null) {
		synchronized (DialogBuilder.class) {
			Context ctx = null;
			if (tmpContext != null) {
				ctx = tmpContext.get();
			}

			if (instance == null || ctx == null || !ctx.equals(context)) {
				instance = new DialogBuilder(context, R.style.common_dialog_un_transparent);

				if (tmpContext != null)
					tmpContext.clear();
				tmpContext = new WeakReference<Context>(context);
			}
		}
//		}
		return instance;
	}

	private void init(Context context) {
		mDialogView = View.inflate(context, R.layout.base_common_dialog, null);
		setContentView(mDialogView);

		mMainRelativeLayout = (RelativeLayout) mDialogView.findViewById(R.id.common_dialog_main);
		mParentLinearLayout = (LinearLayout) mDialogView.findViewById(R.id.common_dialog_parent_panel);
		mTopLinearLayout = (LinearLayout) mDialogView.findViewById(R.id.common_dialog_top_panel);
		mMsgLinearLayout = (LinearLayout) mDialogView.findViewById(R.id.common_dialog_content_panel);
		mCustomFrameLayout = (FrameLayout) mDialogView.findViewById(R.id.common_dialog_custom_panel);
		mButtonBarLinearLayout = (LinearLayout) mDialogView.findViewById(R.id.common_dialog_btn_bar);

		mTitle = (TextView) mDialogView.findViewById(R.id.common_dialog_alert_title);
		mMessage = (TextView) mDialogView.findViewById(R.id.common_dialog_message);
		mIcon = (ImageView) mDialogView.findViewById(R.id.common_dialog_icon);
		mTitleDivider = mDialogView.findViewById(R.id.common_dialog_title_divider);
		mBtnLeft = (TextView) mDialogView.findViewById(R.id.common_dialog_btn_left);
		mBtnRight = (TextView) mDialogView.findViewById(R.id.common_dialog_btn_right);

		mBtnBarDivider = mDialogView.findViewById(R.id.common_dialog_btn_bar_divider);
		mBtnSplitDivider = mDialogView.findViewById(R.id.common_dialog_btn_split_divider);

		mBtnDismiss = (ImageView) mDialogView.findViewById(R.id.common_dialog_btn_dismiss);

		this.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				mParentLinearLayout.setVisibility(View.VISIBLE);
				if (type == null) {
					type = EffectTypes.Fadein;
				}
				playStartAnim(type);
			}
		});

		mMainRelativeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDialogBackgroundResId == -1) {
					if (isCancelable)
						dismiss();
				}
			}
		});
		mBtnDismiss.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isCancelable)
					dismiss();
			}
		});
	}

	public void toDefault() {
		mTitle.setTextColor(Color.parseColor(defTextColor));
		mTitleDivider.setBackgroundColor(Color.parseColor(defDividerColor));
		mMessage.setTextColor(Color.parseColor(defMsgColor));
		mParentLinearLayout.setBackgroundColor(Color.parseColor(defDialogColor));
	}

	public DialogBuilder withDismissButtonResource(int resId) {
		if (mBtnDismiss != null)
			mBtnDismiss.setVisibility(View.VISIBLE);
		mBtnDismiss.setImageResource(resId);
		return this;
	}

	public DialogBuilder withoutDismissButton() {
		if (mBtnDismiss != null)
			mBtnDismiss.setVisibility(View.GONE);
		return this;
	}

	public DialogBuilder withDismissButtonListener(View.OnClickListener listener) {
		if (mBtnDismiss != null && listener != null)
			mBtnDismiss.setOnClickListener(listener);
		return this;
	}

	private int mDialogBackgroundResId = -1;

	public DialogBuilder withDialogBackground(int resId) {
		mDialogBackgroundResId = resId;

		mParentLinearLayout.setBackgroundResource(resId);
		mTopLinearLayout.setBackgroundColor(Color.TRANSPARENT);
		mTitleDivider.setBackgroundColor(Color.TRANSPARENT);
		mMsgLinearLayout.setBackgroundColor(Color.TRANSPARENT);
		mCustomFrameLayout.setBackgroundColor(Color.TRANSPARENT);
		mButtonBarLinearLayout.setBackgroundColor(Color.TRANSPARENT);

		return this;
	}

	private int mContentBackgroundResId = -1;

	public DialogBuilder withContentBackground(int resId) {
		mContentBackgroundResId = resId;

		if (mMsgLinearLayout.getVisibility() != View.GONE)
			mMsgLinearLayout.setBackgroundResource(resId);
		else
			mCustomFrameLayout.setBackgroundResource(resId);

		mParentLinearLayout.setBackgroundColor(Color.TRANSPARENT);
		mTopLinearLayout.setBackgroundColor(Color.TRANSPARENT);
		mTitleDivider.setBackgroundColor(Color.TRANSPARENT);
		mButtonBarLinearLayout.setBackgroundColor(Color.TRANSPARENT);

		return this;
	}

	public DialogBuilder withTitleBarBackgroundColor(int color) {
		mTopLinearLayout.setBackgroundColor(color);
		return this;
	}

	public DialogBuilder withContentBackgroundColor(int color) {
		mCustomFrameLayout.setBackgroundColor(color);
		return this;
	}

	public DialogBuilder withButtonBarBackground(int resId) {
		mButtonBarLinearLayout.setBackgroundResource(resId);
		return this;
	}

	public DialogBuilder withDividerColor(String colorString) {
		mTitleDivider.setBackgroundColor(Color.parseColor(colorString));
		return this;
	}

	public DialogBuilder withDividerColor(int color) {
		mTitleDivider.setBackgroundColor(color);
		return this;
	}

	public DialogBuilder withButtonBarDivider(int resId) {
		mBtnBarDivider.setBackgroundColor(resId);
		return this;
	}

	public DialogBuilder withoutButtonBarDivider() {
		mBtnBarDivider.setBackgroundColor(Color.TRANSPARENT);
		return this;
	}

	public DialogBuilder withButtonSplitDivider(int resId) {
		mBtnSplitDivider.setBackgroundColor(resId);
		return this;
	}

	public DialogBuilder withoutButtonSplitDivider() {
		mBtnSplitDivider.setBackgroundColor(Color.TRANSPARENT);
		return this;
	}

	public DialogBuilder withTitle(CharSequence title) {
		toggleView(mTopLinearLayout, title);
		mTitle.setText(title);
		return this;
	}

	public DialogBuilder withTitleColor(String colorString) {
		mTitle.setTextColor(Color.parseColor(colorString));
		return this;
	}

	public DialogBuilder withTitleColor(int textColor) {
		mTitle.setTextColor(textColor);
		return this;
	}

	public DialogBuilder withTitleSize(float spValue) {
		mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, spValue);
		return this;
	}

	public DialogBuilder withMessage(int textResId) {
		mMsgLinearLayout.setVisibility(View.VISIBLE);
		toggleView(mMsgLinearLayout, textResId);
		mMessage.setText(textResId);
		return this;
	}

	public DialogBuilder withMessage(CharSequence msg) {
		mMsgLinearLayout.setVisibility(View.VISIBLE);
		toggleView(mMsgLinearLayout, msg);
		mMessage.setText(msg);
		return this;
	}

	public DialogBuilder withMessageColor(String colorString) {
		mMessage.setTextColor(Color.parseColor(colorString));
		return this;
	}

	public DialogBuilder withMessageColor(int color) {
		mMessage.setTextColor(color);
		return this;
	}

	public DialogBuilder withMessageSize(float spValue) {
		mMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, spValue);
		return this;
	}

	public DialogBuilder withIcon(int drawableResId) {
		mIcon.setImageResource(drawableResId);
		return this;
	}

	public DialogBuilder withIcon(Drawable icon) {
		mIcon.setImageDrawable(icon);
		return this;
	}

	public DialogBuilder withDuration(int duration) {
		this.mDuration = duration;
		return this;
	}

	public DialogBuilder withEffect(EffectTypes type) {
		this.type = type;
		return this;
	}

	public DialogBuilder withButtonResource(int resId) {
		mBtnLeft.setBackgroundResource(resId);
		mBtnRight.setBackgroundResource(resId);
		return this;
	}

	public DialogBuilder withLeftButton(CharSequence text, View.OnClickListener listener) {
		if (TextUtils.isEmpty(text)) {
			mBtnLeft.setVisibility(View.GONE);
		} else {
			mBtnLeft.setVisibility(View.VISIBLE);
			mBtnLeft.setText(text);
			mBtnLeft.setOnClickListener(listener);
		}
		return this;
	}

	public DialogBuilder withLeftButton(CharSequence text) {
		if (TextUtils.isEmpty(text)) {
			mBtnLeft.setVisibility(View.GONE);
		} else {
			mBtnLeft.setVisibility(View.VISIBLE);
			mBtnLeft.setText(text);
		}
		return this;
	}

	public void setLeftBtnListener(View.OnClickListener listener) {
		mBtnLeft.setVisibility(View.VISIBLE);
		mBtnLeft.setOnClickListener(listener);
	}

	public DialogBuilder withRightButton(CharSequence text) {
		if (TextUtils.isEmpty(text)) {
			mBtnRight.setVisibility(View.GONE);
		} else {
			withButtonSplitDivider(R.color.color_dialog_split);
			mBtnRight.setVisibility(View.VISIBLE);
			mBtnRight.setText(text);
		}
		return this;
	}

	public void setRightBtnListener(View.OnClickListener listener) {
		withButtonSplitDivider(R.color.color_dialog_split);
		mBtnRight.setVisibility(View.VISIBLE);
		mBtnRight.setOnClickListener(listener);
	}

	public DialogBuilder withRightButton(CharSequence text, View.OnClickListener listener) {
		if (TextUtils.isEmpty(text)) {
			mBtnRight.setVisibility(View.GONE);
		} else {
			mBtnRight.setVisibility(View.VISIBLE);
			mBtnRight.setText(text);
			mBtnRight.setOnClickListener(listener);
		}
		return this;
	}

	public DialogBuilder withButtonTextColor(String colorString) {
		mBtnLeft.setTextColor(Color.parseColor(colorString));
		mBtnRight.setTextColor(Color.parseColor(colorString));
		return this;
	}

	public DialogBuilder withButtonTextSize(float spValue) {
		mBtnLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, spValue);
		mBtnRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, spValue);
		return this;
	}

	public DialogBuilder setBtnLeftTag(Object tag) {
		mBtnLeft.setTag(tag);
		return this;
	}

	public DialogBuilder setBtnRightTag(Object tag) {
		mBtnRight.setTag(tag);
		return this;
	}

	public DialogBuilder setCustomView(Context ctx, int resId) {
		mMsgLinearLayout.setVisibility(View.GONE);
		if (mCustomFrameLayout.getChildCount() > 0) {
			mCustomFrameLayout.removeAllViews();
		}
		View customView = View.inflate(ctx, resId, null);
		mCustomFrameLayout.addView(customView);
		return this;
	}

	public DialogBuilder setCustomView(View view) {
		mMsgLinearLayout.setVisibility(View.GONE);
		if (mCustomFrameLayout.getChildCount() > 0) {
			mCustomFrameLayout.removeAllViews();
		}
		mCustomFrameLayout.addView(view);

		if (mContentBackgroundResId != -1)
			mCustomFrameLayout.setBackgroundResource(mContentBackgroundResId);

		if (mDialogBackgroundResId != -1)
			mMainRelativeLayout.setBackgroundResource(android.R.color.transparent);
		return this;
	}

	public DialogBuilder isCancelableOnTouchOutside(boolean cancelable) {
		this.isCancelable = cancelable;
		this.setCanceledOnTouchOutside(cancelable);
		return this;
	}

	public DialogBuilder isCancelable(boolean cancelable) {
		this.isCancelable = cancelable;
		this.setCancelable(cancelable);
		return this;
	}

	private void toggleView(View view, Object obj) {
		if (obj == null) {
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void show() {
		super.show();
//		Context ctx = getContext();
//		if (ctx instanceof Activity) {
//			Activity a = (Activity) ctx;
//			boolean isUiThread = Thread.currentThread() == Looper.getMainLooper().getThread();
//			if (isUiThread && !a.isFinishing()) {
//				super.show();
//			}
//		}
	}

	@Override
	public void dismiss() {
		if (type == null) {
			type = EffectTypes.SlideTop;
		}
		playDismissAnim(type, new BaseEffect.Callback() {
			@Override
			public void onEnd() {
				doDismiss();
				//mBtnLeft.setVisibility(View.GONE);
				//mBtnRight.setVisibility(View.GONE);
			}
		});
	}

	private void doDismiss() {
		super.dismiss();
	}

	/**
	 * define whether the animation should play to root view or just the visible part of dialog view
	 */
	public DialogBuilder setTarget(boolean isRootView) {
		mIsRootView = isRootView;
		return this;
	}

	private void playStartAnim(EffectTypes type) {
		BaseEffect animator = type.getAnimator();
		if (mDuration != -1) {
			animator.setDuration(Math.abs(mDuration));
		}
		if (mIsRootView) {
			animator.start(mMainRelativeLayout);
		} else {
			animator.start(mParentLinearLayout);
		}
	}

	private void playDismissAnim(EffectTypes type, BaseEffect.Callback cb) {
		BaseEffect animator = type.getAnimator();
		if (mDuration != -1) {
			animator.setDuration(Math.abs(mDuration));
		}
		if (mIsRootView) {
			animator.dismiss(mMainRelativeLayout, cb);
		} else {
			animator.dismiss(mParentLinearLayout, cb);
		}
	}
}
