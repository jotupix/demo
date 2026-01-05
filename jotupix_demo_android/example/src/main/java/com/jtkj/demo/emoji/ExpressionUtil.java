package com.jtkj.demo.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.library.commom.logger.CLog;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtil {
    private static final String TAG = ExpressionUtil.class.getSimpleName();

    public static void dealExpression(Context context, SpannableString spannableString, int textSize, Pattern patten, int start)
            throws Exception {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            if (matcher.start() < start) {
                continue;
            }
            Field field;
            if (key.length() == 15) {
                //emoji_fc_13_003
                String[] parts = key.split("_");
                int emojiType = Integer.valueOf(parts[2]);
                int emojiNumber = Integer.valueOf(parts[3]);
                CLog.i(TAG, "dealExpression>>>key>>>" + key + ">>>emojiType>>>" + emojiType + ">>>emojiNumber>>>" + emojiNumber);
                String imgeString = "emoji_fc_32x32_" + Integer.valueOf(emojiType) + "_" + Integer.valueOf(emojiNumber);
                field = R.drawable.class.getDeclaredField(imgeString);
            } else {
                field = R.drawable.class.getDeclaredField(key);
            }
            int resId = field.getInt(R.drawable.class);
            if (resId != 0) {
                Drawable d = context.getResources().getDrawable(resId);
                d.setBounds(0, 0, textSize, textSize);
                ImageSpan imageSpan = new ImageSpan(d);
                int end = matcher.start() + key.length();
                spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                if (end < spannableString.length()) {
                    dealExpression(context, spannableString, textSize, patten, end);
                }
                break;
            }
        }
    }

    public static SpannableString getExpressionString(Context context, String str, int textSize, String PATTEN_STR) {
        SpannableString spannableString = new SpannableString(str);
        Pattern sinaPatten = Pattern.compile(PATTEN_STR, Pattern.CASE_INSENSITIVE);
        try {
            dealExpression(context, spannableString, textSize, sinaPatten, 0);
        } catch (Exception e) {
            CoolLED.reportError(e);
            CLog.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }

    public static boolean matchEmotion(String str, String PATTEN_STR) {
        if (str == null || str.equals("")) {
            return false;
        } else {
            Pattern sinaPatten = Pattern.compile(PATTEN_STR, Pattern.CASE_INSENSITIVE);
            Matcher matcher = sinaPatten.matcher(str);
            return matcher.matches();
        }
    }
}