package com.jtkj.library.commom.tools;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Other Method:
 * java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
 * byte[] b = str.getBytes("UTF8");
 * byte[] hash = md.digest(b);
 * String pwd = Base64.encodeToString( hash, true );
 * 
 * @author agemzhang
 */
public class MD5 {
	public static String getMD5(String value) {
		byte[] result = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(value.getBytes());
//			md5.digest(value.getBytes(), 0, 32);
			result = md5.digest();// 加密
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught : " + e.getMessage());
		}
		return getString(result);
	}

	private static String getString(byte[] b) {
		if(b == null) return "";

		StringBuffer sb = new StringBuffer();  
        for (int i = 0; i < b.length; i++) {              
            if (Integer.toHexString(0xFF & b[i]).length() == 1)  
            	sb.append("0").append(Integer.toHexString(0xFF & b[i]));  
            else  
            	sb.append(Integer.toHexString(0xFF & b[i]));  
        } 
		return sb.toString();
	}

	public static final int LEN_32 = 32;
	public static final int LEN_16 = 16;

	public static String getMD5(String sourceStr, int len) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(sourceStr.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			result = buf.toString();
			switch (len) {
				case LEN_32:
					return result;
				case LEN_16:
					return buf.toString().substring(8, 24);
			}
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught : " + e.getMessage());
		}
		return result;
	}

	public final static String getMD532Low(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			//使用MD5创建MessageDigest对象
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte b = md[i];
				//System.out.println((int)b);
				//将没个数(int)b进行双字节加密
				str[k++] = hexDigits[b >> 4 & 0xf];
				str[k++] = hexDigits[b & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	public final static String get32MD5Str(String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			return "";
		} catch (UnsupportedEncodingException e) {
			return "";
		}
		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}

//	public final static String MD5(String s) {
//		try {
//			byte[] btInput = s.getBytes();
//			MessageDigest md5 = MessageDigest.getInstance("MD5"); md5.update(btInput);
//			byte[] md = md5.digest();
//			StringBuffer sb = new StringBuffer();
//			for (int i = 0; i < md.length; i++) {
//				int val = md & 0xff;
//				if (val < 16) sb.append("0");
//				sb.append(Integer.toHexString(val));
//			} return sb.toString();
//		} catch (Exception e) {
//			return null;
//		}
//	}
}
