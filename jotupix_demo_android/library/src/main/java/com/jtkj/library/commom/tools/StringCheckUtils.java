package com.jtkj.library.commom.tools;

import java.util.Collection;
import java.util.List;

import android.text.TextUtils;

public class StringCheckUtils {
	/**
	 * 判断字符不能为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (TextUtils.isEmpty(str) || "".equals(str)
				|| "null".equalsIgnoreCase(str) || "".equals(str.trim())) {
			return true;
		}
		return false;
	}


	/**
	 * 判断数组是否为空
	 * 
	 * @param list
	 * @return
	 */
	public static <T> boolean isEmpty(List<T> list) {
		if (list == null || list.isEmpty()) {
			return true;
		}
		return false;
	}

	/** 判断List是否为空 */
	public static boolean listIsNullOrEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}

	/**
	 * 请选择
	 */
	final static String PLEASE_SELECT = "请选择...";

	public static boolean notEmpty(Object o) {
		return o != null && !"".equals(o.toString().trim())
				&& !"null".equalsIgnoreCase(o.toString().trim())
				&& !"undefined".equalsIgnoreCase(o.toString().trim())
				&& !PLEASE_SELECT.equals(o.toString().trim());
	}
}
