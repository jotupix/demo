package com.jtkj.library.commom.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import androidx.core.content.PermissionChecker;
import android.util.Log;

/**
 * Created by laixj on 2016/11/30.
 */

public class PermissionUtil {

	public static final String TAG = "PermissionUtil";

	public static boolean isSdkUnderM() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
	}

	public static boolean selfPermissionGranted(Context context,String permission) {
		// For Android < Android M, self permissions are always granted.
		boolean result = true;
		Log.i(TAG, "Build.VERSION.SDK_INT="+Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (getTargetSdkVersion(context) >= Build.VERSION_CODES.M) {
				// targetSdkVersion >= Android M, we can use Context#checkSelfPermission
				result = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
			} else {
				// targetSdkVersion < Android M, we have to use PermissionChecker
				result = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
			}
		}
		if (result) {
			Log.i(TAG, "有"+permission+"这个权限");
		}else {
			Log.i(TAG, "木有"+permission+"这个权限");
		}
		return result;
	}

	private static int getTargetSdkVersion(Context context) {
		int version = 0;
		try {
			final PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			version = info.applicationInfo.targetSdkVersion;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();

		}
		Log.i(TAG, "TargetSdkVersion="+version);
		return version;
	}

	public static boolean checkAppPermission(Context context, String permission) {
		boolean result;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			result = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
			if (result) {
				Log.i(TAG, "有"+permission+"这个权限");
			} else {
				Log.i(TAG, "木有"+permission+"这个权限");
			}
		} else {
			result = true;
		}
		return result;
	}

	public static boolean checkCameraPermission(){
		boolean result = true;
		Camera mCamera = null;
		try {
			mCamera = Camera.open();
			// setParameters 是针对魅族做的。MX5 通过Camera.open() 拿到的Camera
			// 对象不为null
			Camera.Parameters mParameters = mCamera.getParameters();
			mCamera.setParameters(mParameters);
		} catch (Exception e) {
			result = false;
			Log.i(TAG, "判断相机权限"+e.toString());
		} finally {
			if (mCamera != null){
				mCamera.release();
				mCamera = null;
			}
		}
		return result;
	}
}
