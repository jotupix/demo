package com.jtkj.library.commom.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class VersionUtil {

	/**
	 * 获取版本名
	 * 
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "0.0";
	}

	/**
	 * 获取版本号
	 * 
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * 获取版本号
	 * 
	 * @param context
	 * @return
	 */
	public static String getPackageName(Context context) {
		try {
			return context.getPackageName();
//			PackageManager packageManager = context.getPackageManager();
//			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
//			return packInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
