package com.jtkj.library.commom.waterwave;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.jtkj.library.R;

public class WaveProgress extends View {

	private Paint mPaint;
	private Paint mWavePaint;
	private int mWaveColor;
	private int mBelowColor;
	private int mCirColor;
	private int mCirWidth;

	private final int DEFAULT_CIR_COLOR = Color.parseColor("#ff0000");
	private final int DEFAULT_WAVE_COLOR = Color.RED;
	private final int DEFAULT_CIR_WIDTH = dp2px(3);

	private final float DEFAULT_RADIAN = 1.0F;
	private final float DEFAULT_AMPlLITUDE = 0.05F;
	private final float DEFAULT_LEVEL = 0.5F;
	private final float DEFAULT_ALPHA = 0.3F;

	private float mAmpilitude = DEFAULT_AMPlLITUDE;
	private float mLevel;

	private Bitmap mShaderBitmap;
	private BitmapShader mShader;
	private int progress = 0;
	private Matrix mMatrix;

	private float translation;
	private AnimatorSet mAnimatorSet;

	private final int MAX_FLAG = 1;
	private final int PROGRESS_FLAG = 0;

	private float waveLevel = 1f;

	public float getWaveLevel() {
		return waveLevel;
	}

	public void setWaveLevel(float waveLevel) {
		this.waveLevel = waveLevel;
		invalidate();
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MAX_FLAG:
					break;
				case PROGRESS_FLAG:
					initLevelAnimation();
					break;

			}
		}
	};
	private Paint paint;
	private Canvas canvas;

	public int getmCirColor() {
		return mCirColor;
	}

	public void setmCirColor(int mCirColor) {
		this.mCirColor = mCirColor;
	}

	public int getmCirWidth() {
		return mCirWidth;
	}

	public void setmCirWidth(int mCirWidth) {
		this.mCirWidth = mCirWidth;
		mPaint.setStrokeWidth(mCirWidth);
		invalidate();
	}

	public int getmWaveColor() {
		return mWaveColor;
	}

	public void setmWaveColor(int mWaveColor) {
		this.mWaveColor = mWaveColor;
		onSizeChanged(getWidth(), getHeight(), 0, 0);
		invalidate();
	}

	public float getmAmpilitude() {
		return mAmpilitude;
	}

	public void setmAmpilitude(float mAmpilitude) {
		mAmpilitude = mAmpilitude / 1000;
		this.mAmpilitude = mAmpilitude;
		onSizeChanged(getWidth(), getHeight(), 0, 0);
		invalidate();
	}

	public float getTranslation() {
		invalidate();
		return translation;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		mHandler.sendEmptyMessage(PROGRESS_FLAG);
	}

	public void setTranslation(float translation) {
		this.translation = translation;
		invalidate();
	}

	public WaveProgress(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		obtain(attrs);
		init();
	}

	public int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
	}

	private void obtain(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.waveprogress);
		mCirColor = a.getColor(R.styleable.waveprogress_cirColor, DEFAULT_CIR_COLOR);
		mWaveColor = a.getColor(R.styleable.waveprogress_waveColor, DEFAULT_WAVE_COLOR);
		mBelowColor = a.getColor(R.styleable.waveprogress_belowColor, DEFAULT_WAVE_COLOR);
		mCirWidth = (int) a.getDimension(R.styleable.waveprogress_cirwidth, DEFAULT_CIR_WIDTH);
		a.recycle();
	}

	private void init() {
		initPaint();
		initAnimation();
		mMatrix = new Matrix();
	}

	private void initAnimation() {
		ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(this, "translation", 0f, 1f);
		waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
		waveShiftAnim.setDuration(1000);
		waveShiftAnim.setInterpolator(new LinearInterpolator());
		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.play(waveShiftAnim);
	}

	private void initLevelAnimation() {
		ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(this, "waveLevel", progress / 100f, waveLevel);
		waveShiftAnim.setDuration(1000);
		waveShiftAnim.setInterpolator(new LinearInterpolator());
		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.play(waveShiftAnim);
		mAnimatorSet.start();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mAnimatorSet != null)
			mAnimatorSet.start();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAnimatorSet != null)
			mAnimatorSet.cancel();
	}

	private void initPaint() {
		mPaint = new Paint();
		mPaint.setStrokeWidth(mCirWidth);
		mPaint.setAntiAlias(true);
		mPaint.setPathEffect(new CornerPathEffect(3));
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(mCirColor);
		mWavePaint = new Paint();
		mWavePaint.setAntiAlias(true);
		mWavePaint.setPathEffect(new CornerPathEffect(3));
	}

	public WaveProgress(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaveProgress(Context context) {
		this(context, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int finalWh = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(finalWh, finalWh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mMatrix.setTranslate(getWidth() * translation, (DEFAULT_LEVEL - waveLevel) * getHeight());
		mShader.setLocalMatrix(mMatrix);
		int width = getWidth() / 2;
		int height = getHeight() / 2;
		int radio = width - mCirWidth;
		canvas.drawCircle(width, height, radio, mWavePaint);
		canvas.drawCircle(width, height, radio, mPaint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int width = w;
		int height = h;
		double radian = 2.0f * Math.PI / DEFAULT_RADIAN / width;
		float amplitude = height * mAmpilitude;
		mLevel = height * DEFAULT_LEVEL;
		if (mShaderBitmap != null) {
			mShaderBitmap.recycle();
		}
		if (paint == null)
			paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2f);
		mShaderBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		if (canvas == null)
			canvas = new Canvas();
		canvas.setBitmap(mShaderBitmap);
		// draw wave
		paint.setColor(mWaveColor);
		int endX = width + 1;
		int endY = height + 1;
		float pointY[] = new float[endY];
		for (int beginX = 0; beginX < endX; beginX++) {
			double currentRadian = beginX * radian;
			float currentY = (float) (mLevel + amplitude * Math.sin(currentRadian));
			canvas.drawLine(beginX, currentY, beginX, endY, paint);
			pointY[beginX] = currentY;
		}
		paint.setColor(adjustAlpha(mBelowColor, 1f));
		final int wave2Shift = (int) (width / 3);
		for (int beginX = 0; beginX < endX; beginX++) {
			canvas.drawLine(beginX, pointY[(beginX + wave2Shift) % endX], beginX, endY, paint);
		}
		paint.setColor(adjustAlpha(mWaveColor, 1f));
		int wave3Shift = (int) (width / 7);
		for (int beginX = 0; beginX < endX; beginX++) {
			canvas.drawLine(beginX, pointY[(beginX + wave3Shift) % endX], beginX, endY, paint);
		}
		mShader = new BitmapShader(mShaderBitmap, TileMode.REPEAT, TileMode.CLAMP);
		mWavePaint.setShader(mShader);
	}

	private int adjustAlpha(int color, float factor) {
		int alpha = Math.round(Color.alpha(color) * factor);
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}
}
