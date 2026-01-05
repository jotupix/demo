package com.jtkj.library.commom.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;

import com.jtkj.library.commom.logger.CLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

/**
 * Created by yfxiong on 2016/12/6.
 * Copyright © yfxiong www.jotus-tech.com. All Rights Reserved.
 *
 * http://www.jianshu.com/p/178786f833b6
 */
public class Installation {
	private static final String TAG = "Installation";
	private static final String INSTALLATION = "INSTALLATION";
	private static final String DEFAULT = "550e8400-e29b-41d4-a716-4b6t5w480pAA";
	private static String sID = null;

	public synchronized static String id(Context ctx) {
		if (sID == null) {
			try {
				File installation = new File(ctx.getFilesDir(), INSTALLATION);
				if (!installation.exists())
					writeInstallationFile(installation);
				sID = readInstallationFile(installation);
			} catch (Exception e) {
				CLog.i(TAG, "Read or write file exception, going to hardware way");
				@SuppressLint("HardwareIds")
				final String androidId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
				try {
					UUID uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
					sID = uuid.toString();
				} catch (UnsupportedEncodingException e1) {
					CLog.i(TAG, "Get UUID from android ID: UnsupportedEncodingException");
				}
			} finally {
				if (sID == null || TextUtils.isEmpty(sID)) {
					CLog.i(TAG, "Use default UUID: " + DEFAULT);
					sID = DEFAULT;
				}
			}
		}
		return sID;
	}

	private static String readInstallationFile(File installation) throws IOException {
		RandomAccessFile f = new RandomAccessFile(installation, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	private static void writeInstallationFile(File installation) throws IOException {
		FileOutputStream out = new FileOutputStream(installation);
		String id = UUID.randomUUID().toString();
		out.write(id.getBytes());
		out.close();
	}

	public static boolean hadInstall(Context context, String packageName) {
		boolean exist = false;
		PackageManager pm = context.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);
		for (ResolveInfo resolveInfo : resolveInfoList) {
			if (resolveInfo.activityInfo.packageName.equals(packageName)) {
				exist = true;
			}
		}
		return exist;
	}
}
