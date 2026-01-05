package com.jtkj.library.commom.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by zhanghaoming on 2016/10/23.
 * Copyright © yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class DensityUtil {
	private static final String TAG = "DensityUtil";

	private static DensityUtil densityUtil = null;

	// 当前屏幕的densityDpi
	private static float dmDensityDpi = 0.0f;

	private static DisplayMetrics dm;

	private static float scale = 1.0f;

	private DensityUtil(Context context) {
		// 获取当前屏幕
		dm = new DisplayMetrics();
		// 返回当前资源对象的DispatchMetrics信息�?
		//dm = context.getApplicationContext().getResources().getDisplayMetrics();
		if (context instanceof Activity) {
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		} else {
			Log.e(TAG, "your context must is instance of Activity!");
			return;
		}
		// 设置DensityDpi
		setDmDensityDpi(dm.densityDpi);
		// 密度因子
		//scale = getDmDensityDpi() / 160;// 等于 scale=dm.density;
		scale = dm.density;
	}

	public static DensityUtil getInstance(Context context) {
		if (densityUtil == null) {
			densityUtil = new DensityUtil(context);
		}
		return densityUtil;
	}

	/**
	 * @return
	 * @Description: 屏幕分辨率宽
	 */
	public int getWindowWidth() {
		if (dm != null) {
			return dm.widthPixels;
		}
		return 0;
	}

	/**
	 * @return
	 * @Description: 屏幕分辩类高
	 */
	public int getWindowHeight() {
		if (dm != null) {
			return dm.heightPixels;
		}
		return 0;
	}

	public static int getStatusBarHeight(Context ctx) {
		int statusBarHeight = 0;
		Resources res = ctx.getResources();
		int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusBarHeight = res.getDimensionPixelSize(resourceId);
		}
		return statusBarHeight;
	}

	/**
	 * @Description: 屏幕的ppi
	 * @return：float
	 */
	public float getDmDensityDpi() {
		return dmDensityDpi;
	}

	public void setDmDensityDpi(float dmDensityDpi) {
		DensityUtil.dmDensityDpi = dmDensityDpi;
	}

	public static int dip2px(float dipValue) {
		return (int) (dipValue * scale + 0.5f);
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * @param context
	 * @param dip
	 * @Description: dip转像�?
	 * @return：int
	 */
	public static int dipToPixels(Context context, int dip) {
		final float SCALE = context.getResources().getDisplayMetrics().density;
		float valueDips = dip;
		int valuePixels = (int) (valueDips * SCALE + 0.5f);
		return valuePixels;
	}

	/**
	 * @param context
	 * @param Pixels
	 * @Description: 像素转dip
	 * @return：float
	 */
	public static float pixelsToDip(Context context, int Pixels) {
		final float SCALE = context.getResources().getDisplayMetrics().density;
		float dips = Pixels / SCALE;
		return dips;
	}

	/**
	 * sp转px
	 *
	 * @param spValue sp值
	 * @return px值
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * px转sp
	 *
	 * @param pxValue px值
	 * @return sp值
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}
}
