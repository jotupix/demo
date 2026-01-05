package com.jtkj.library.commom.tools;

import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pengrf on 2016/10/31 17:51.
 *
 * @describe ${TODO}正则表达式判断工具类
 * @更新者 $Author$
 * @更新时间 2016/10/31
 * @更新描述 ${TODO} 判断手机正则表达工具
 */

public class RegularExpress {
    private RegularExpress() {
    }

    /**
     * 验证手机格式, 验证手机格式,
     *
     */
    public static boolean isMobileNumber(String mobile_number) {
        /**
         * 移动:134,135,136,137,138,139,150,151,157(TD),158,159,187,188
         * 联通:130,131,132,152,155,156,185,186
         * 电信:133,153,170,180,189
         * 总结起来: 第一位一定是1,第二位是3,4,5,7,8,其他位置0~9
         */
        String telRegex = "[1][1234567890]\\d{9}";
        if (TextUtils.isEmpty(mobile_number))
            return false;
        else
            return mobile_number.matches(telRegex);
    }

    /**
     * 银行帐号Pattern
     */
    public static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern
            .compile("\\d{16,21}");

    /**
     * 判断是否有表情
     *
     * @param str
     * @return
     */
    public static boolean existExpression(String str) {
        Pattern pattern = Pattern.compile("^[\u0000-\ud7a3\uff01-\uffee]*$");
        Matcher matcher = pattern.matcher(str);
        return !matcher.find();
    }

    /**
     *
     * 判断edittext是否为空
     *
     */
    public static boolean isEmpty(EditText edt) {
        if (edt==null) return true;
        Editable e = edt.getText();
        if (e == null || e.length() == 0) return true;
        String str = e.toString().trim();
        return TextUtils.isEmpty(str);
    }

    /**
     *
     * 判断edittext是否为空
     *
     */
    public static boolean isEmpty(TextView tv) {
        if (tv==null) return true;
        CharSequence str = tv.getText();
        return TextUtils.isEmpty(str);
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
     * 判断是否有数字
     *
     * @param s
     * @return
     */
    public static boolean containsNumber(String s) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(s).matches();
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

}
