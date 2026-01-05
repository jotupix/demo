package com.jtkj.library.commom.tools;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by yfxiong on 2017/1/5.
 * Copyright © 2017 www.yyfax.com. All Rights Reserved.
 */
public class Toaster {
	public static void showShortToast(Context ctx, int stringId) {
		Toast.makeText(ctx, stringId, Toast.LENGTH_SHORT).show();
	}

	public static void showShortToast(Context ctx, String string) {
		Toast.makeText(ctx, string, Toast.LENGTH_SHORT).show();
	}

	public static void showLongToast(Context ctx, int stringId) {
		Toast.makeText(ctx, stringId, Toast.LENGTH_LONG).show();
	}

	public static void showLongToast(Context ctx, String string) {
		Toast.makeText(ctx, string, Toast.LENGTH_LONG).show();
	}
}
