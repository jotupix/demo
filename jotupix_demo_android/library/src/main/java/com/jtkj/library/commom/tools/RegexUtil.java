package com.jtkj.library.commom.tools;

/**
 * @author 思恩
 * @time 2014.10.08
 * @description 验证正则表达式
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

	/**
	 * 车牌号码Pattern
	 */
	public static final Pattern PLATE_NUMBER_PATTERN = Pattern
			.compile("^[\u0391-\uFFE5]{1}[a-zA-Z0-9]{6}$");

	/**
	 * 证件号码Pattern
	 */
	public static final Pattern ID_CODE_PATTERN = Pattern
			.compile("^[a-zA-Z0-9]+$");

	/**
	 * 编码Pattern
	 */
	public static final Pattern CODE_PATTERN = Pattern
			.compile("^[a-zA-Z0-9]{6,20}[@#￥%&*+-=]+$");

	/**
	 * 固定电话编码Pattern
	 */
	public static final Pattern PHONE_NUMBER_PATTERN = Pattern
			.compile("0\\d{2,3}-[0-9]+");

	/**
	 * 固定电话编码 区号Pattern
	 */
	public static final Pattern PHONE_NUMBER_AREA_PATTERN = Pattern
			.compile("0\\d{2,3}");

	/**
	 * 固定电话编码 座机号Pattern
	 */
	public static final Pattern PHONE_NUMBER_PHONE_PATTERN = Pattern
			.compile("[0-9]+");

	/**
	 * 邮政编码Pattern
	 */
	public static final Pattern POST_CODE_PATTERN = Pattern.compile("\\d{6}");

	/**
	 * 面积Pattern
	 */
	public static final Pattern AREA_PATTERN = Pattern.compile("\\d*.?\\d*");

	/**
	 * 手机号码Pattern 判断手机格式是否正确
	 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
	 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通） 177
	 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
	 */
	public static final Pattern MOBILE_NUMBER_PATTERN = Pattern
			.compile("[1][34578]\\d{9}");

	// /**
	// * 手机号码Pattern
	// */
	// public static final Pattern MOBILE_NUMBER_PATTERN = Pattern
	// .compile("[1]\\d{10}");

	/**
	 * 银行帐号Pattern
	 */
	public static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern
			.compile("\\d{16,21}");

	/**
	 * 车牌号码是否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isPlateNumber(String s) {
		Matcher m = PLATE_NUMBER_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 证件号码是否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isIDCode(String s) {
		Matcher m = ID_CODE_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 编码是否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isCode(String s) {
		Matcher m = CODE_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 固定电话（包括区号）是否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isPhoneNumber(String s) {
		Matcher m = PHONE_NUMBER_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 固定电话（不包括区号）是否正确
	 *
	 * @param s
	 * @return
	 */
	public static boolean isPhoneNumberWithoutAreaCode(String s) {
		Matcher m = PHONE_NUMBER_PHONE_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 邮政编码是否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isPostCode(String s) {
		Matcher m = POST_CODE_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 面积是否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isArea(String s) {
		Matcher m = AREA_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 手机号码否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isMobileNumber(String s) {
		Matcher m = MOBILE_NUMBER_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 银行账号否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isAccountNumber(String s) {
		Matcher m = ACCOUNT_NUMBER_PATTERN.matcher(s);
		return m.matches();
	}

	/**
	 * 验证银行卡(12位到19位数字均可以)
	 *
	 * @param cardNumber 银行卡号
	 * @return boolean
	 */
	public static boolean checkBankCard(String cardNumber) {
		boolean flag;
		try {
			Pattern regex = Pattern.compile("^([0-9]{12,19})$");
			Matcher matcher = regex.matcher(cardNumber);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 邮箱校验
	 * */
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);

		return m.matches();
	}

}