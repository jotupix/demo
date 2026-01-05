package com.jtkj.library.infrastructure.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class CUtility {
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			if (listItem instanceof ViewGroup) {
				listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			}
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	public static DisplayMetrics getDisplayMetrics(Context ctx) {
		return ctx.getResources().getDisplayMetrics();
	}

	/**获取版本号(内部识别号)*/
	public static int getVersionCode(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			return 1;
		}
	}

	/**获取版本号*/
	public static String getVersionName(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "v1";
		}
	}

}