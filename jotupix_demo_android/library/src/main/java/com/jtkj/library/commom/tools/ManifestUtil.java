package com.jtkj.library.commom.tools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import com.jtkj.library.commom.logger.CLog;

/**
 * Created by yfxiong on 2016/12/27.
 * Copyright © yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class ManifestUtil {
	private static final String TAG = "ManifestUtil";

	/**
	 * 读取application节点meta-data信息
	 */
	public static String readMetaDataFromApplication(Context ctx, String tagName) {
		String tag;
		try {
			ApplicationInfo appInfo = ctx.getPackageManager().getApplicationInfo(
					ctx.getPackageName(), PackageManager.GET_META_DATA);
			tag = appInfo.metaData.getString(tagName);
			CLog.i(TAG, "packageName = " + ctx.getPackageName() + ", " + tagName +" = " + tag);
		} catch (PackageManager.NameNotFoundException e) {
			tag = "";
		}
		return tag;
	}

	/**
	 * 读取BroadcastReceiver节点meta-data信息
	 */
	public static String readMetaDataFromBroadCast(Context ctx, Class receiverClass, String tagName) {
		String tag;
		try {
			ComponentName cn = new ComponentName(ctx, receiverClass);
			ActivityInfo info = ctx.getPackageManager().getReceiverInfo(cn, PackageManager.GET_META_DATA);
			tag = info.metaData.getString(tagName);
			CLog.i(TAG, "packageName = " + ctx.getPackageName() + ", tag = " + tag);
		} catch (PackageManager.NameNotFoundException e) {
			tag = "";
		}
		return tag;
	}

	/**
	 * 读取Service节点meta-data信息
	 */
	public static String readMetaDataFromService(Context ctx, Class serviceClass, String tagName) {
		String tag;
		try {
			ComponentName cn = new ComponentName(ctx, serviceClass);
			ServiceInfo info = ctx.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
			tag = info.metaData.getString(tagName);
			CLog.i(TAG, "serviceName = " + serviceClass.getSimpleName() + ", tag = " + tag);
		} catch (PackageManager.NameNotFoundException e) {
			tag = "";
		}
		return tag;
	}

	/**
	 * 读取Activity节点meta-data信息
	 */
	public static String readMetaDataFromActivity(Activity aty, String tagName) {
		String tag;
		try {
			ActivityInfo info = aty.getPackageManager().getActivityInfo(
					aty.getComponentName(), PackageManager.GET_META_DATA);
			tag = info.metaData.getString(tagName);
			CLog.i(TAG, "activityName = " + aty.getComponentName() + ", tag = " + tag);
		} catch (PackageManager.NameNotFoundException e) {
			tag = "";
		}
		return tag;
	}
}
