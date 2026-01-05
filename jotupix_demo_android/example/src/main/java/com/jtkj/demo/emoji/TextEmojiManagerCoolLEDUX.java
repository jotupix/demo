package com.jtkj.demo.emoji;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.widget.EditText;
import android.widget.TextView;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.R;
import com.jtkj.demo.device.utils.ArabicCharDotMatrixGenerator;
import com.jtkj.demo.device.utils.MultiLangTextEmojiParser;
import com.jtkj.demo.device.utils.Utils;
import com.jtkj.demo.widget.DrawView;
import com.jtkj.library.commom.logger.CLog;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEmojiManagerCoolLEDUX extends TextEmojiManager {
    private static final String TAG = TextEmojiManagerCoolLEDUX.class.getSimpleName();

    public static final String EMOJIS_FILE_NAME = "emotions_coolledux.xml";
    //    public static final String PATTEN_STR = "emoji_fc_[0-9]{3}|emoji_fc_[0-9]{2}_[0-9]{3}";
    public static final String PATTEN_STR = "emoji_fc_\\d{2}_\\d{3}|emoji_fc_\\d{3}";

    private static volatile TextEmojiManagerCoolLEDUX mInstance;

    public static TextEmojiManagerCoolLEDUX getInstance() {
        if (null == mInstance) {
            synchronized (TextEmojiManagerCoolLEDUX.class) {
                if (null == mInstance) {
                    mInstance = new TextEmojiManagerCoolLEDUX();
                }
            }
        }
        return mInstance;
    }

    List<Emotion> mEmotions;

    public TextEmojiManagerCoolLEDUX() {
        initEmoji();
    }

    public void initEmoji() {
        CoolLED.getInstance().getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = CoolLED.getInstance().getResources().getAssets().open(EMOJIS_FILE_NAME);
                    mEmotions = XmlUtil.getEmotions(inputStream);
                    for (Emotion emotion : mEmotions) {
                        CLog.i(TAG, "emojis>>>name>>>" + emotion.toString());
                        Field f = R.drawable.class.getDeclaredField(emotion.name);
                        int imageId = f.getInt(R.drawable.class);
                        emotion.setImageId(imageId);
                    }
                } catch (Exception e) {
                    CLog.i(TAG, e.getMessage());
                }
            }
        });
    }

    public List<Emotion> getEmotions() {
        return mEmotions;
    }

    public static void setTextWithEmotion(int position, EditText editText, Emotion emotion) {
        CLog.i(TAG, "setTextWithEmotion>>>" + emotion.toString());
        int cursor = editText.getSelectionStart();
        String code = emotion.getCode();
        try {
            if (code.startsWith("![CDATA[[em:")) {
                Field f = R.drawable.class.getDeclaredField(emotion.getName());
                int j = f.getInt(R.drawable.class);
                Drawable d = CoolLED.getInstance().getResources().getDrawable(j);
                int textSize = (int) editText.getTextSize();
                d.setBounds(0, 0, textSize, textSize);

                String str;
                int pos = position + 1;
                if (pos < 10) {
                    str = "emoji_fc_00" + pos;
                } else if (pos < 100) {
                    str = "emoji_fc_0" + pos;
                } else {
                    str = "emoji_fc_" + pos;
                }
                SpannableString ss = new SpannableString(str);
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                ss.setSpan(span, 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                editText.getText().insert(cursor, ss);
            } else {
                //emoji_fc_13_003
//                String emojiType = code.substring(6, 8);
//                String emojiNumber = code.substring(9);

                String[] parts = code.split("_");
                int emojiType = Integer.valueOf(parts[2]);
                int emojiNumber = Integer.valueOf(parts[3]);
                CLog.i(TAG, "setTextWithEmotion>>>code>>>" + code+">>>emojiType>>>"+emojiType+">>>emojiNumber>>>"+emojiNumber);
                String imgeString = "emoji_fc_32x32_" + Integer.valueOf(emojiType) + "_" + Integer.valueOf(emojiNumber);

                Field f = R.drawable.class.getDeclaredField(imgeString);
                int j = f.getInt(R.drawable.class);
                Drawable d = CoolLED.getInstance().getResources().getDrawable(j);
                int textSize = (int) editText.getTextSize();
                d.setBounds(0, 0, textSize, textSize);

                SpannableString ss = new SpannableString(code);
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                ss.setSpan(span, 0, code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                editText.getText().insert(cursor, ss);
            }
        } catch (Exception e) {
            CLog.i(TAG, e.getMessage());
        }
    }

    public static List<TextEmojiManager.TextEmoji32Item> getTextEmojiItems(String languageCode, String inputString, String PATTEN_STR) {
        return MultiLangTextEmojiParser.getTextEmojiItems(languageCode, inputString, PATTEN_STR);
    }

    public List<TextEmoji32Item> getTextEmojiItems(String inputString, String PATTEN_STR) {
        Pattern pattern = Pattern.compile(PATTEN_STR, Pattern.CASE_INSENSITIVE);
        List<TextEmoji32Item> listTextEmojiInfos = new ArrayList<>();
        CLog.i(TAG, "getTextEmojiItems>>>" + inputString);

        if (inputString.length() >= 12) {
            for (int i = 0; i < inputString.length(); i++) {
                // 检查剩余长度是否足够匹配最长的表情格式 (emoji_fc_12_345 共15字符)
                if (i <= inputString.length() - 15) {
                    // 先尝试匹配长格式: emoji_fc_12_345 (15字符)
                    String longFormatString = inputString.substring(i, i + 15);
                    CLog.i(TAG, "getTextEmojiItems>>longFormatString>>>" + longFormatString);
                    Matcher longMatcher = pattern.matcher(longFormatString);

                    if (longMatcher.find() && longFormatString.equals(longMatcher.group())) {
                        // 匹配到长格式: emoji_fc_12_345
                        String[] parts = longFormatString.split("_");
                        int emojiType = Integer.valueOf(parts[2]);
                        int emojiNumber = Integer.valueOf(parts[3]);
                        String imageName = longFormatString;
                        CLog.i(TAG, "textEmojiInfo is long format emoji>>>emojiType>>>" + emojiType+">>>emojiNumber>>>"+emojiNumber);
                        TextEmoji32Item textEmojiInfo = createEmojiItem(imageName, emojiType, emojiNumber);
                        CLog.i(TAG, "textEmojiInfo is long format emoji>>>" + textEmojiInfo);

                        i += 14; // 跳过15个字符，因为i会在循环结束时+1
                        listTextEmojiInfos.add(textEmojiInfo);
                        continue; // 继续下一次循环
                    }
                }

                // 检查剩余长度是否足够匹配短格式 (emoji_fc_123 共12字符)
                if (i <= inputString.length() - 12) {
                    String shortFormatString = inputString.substring(i, i + 12);
                    CLog.i(TAG, "getTextEmojiItems>>shortFormatString>>>" + shortFormatString);
                    Matcher shortMatcher = pattern.matcher(shortFormatString);

                    if (shortMatcher.find() && shortFormatString.equals(shortMatcher.group())) {
                        // 匹配到短格式: emoji_fc_123
                        String emojiIdStr = shortFormatString.substring(9); // 提取"123"
                        int emojiId = Integer.valueOf(emojiIdStr);
                        String imageName = shortFormatString;

                        TextEmoji32Item textEmojiInfo = createEmojiItem(imageName, emojiId);
                        CLog.i(TAG, "textEmojiInfo is short format emoji>>>" + textEmojiInfo);

                        i += 11; // 跳过12个字符，因为i会在循环结束时+1
                        listTextEmojiInfos.add(textEmojiInfo);
                        continue; // 继续下一次循环
                    }
                }

                // 如果没有匹配到任何表情格式，按普通文本处理
                String tempString = String.valueOf(inputString.charAt(i));
                TextEmoji32Item textEmojiInfo = createTextItem(tempString);
                CLog.i(TAG, "textEmojiInfo is text>>>" + textEmojiInfo);
                listTextEmojiInfos.add(textEmojiInfo);
            }
        } else {
            // 字符串长度不足，全部按文本处理
            for (int i = 0; i < inputString.length(); i++) {
                String tempString = String.valueOf(inputString.charAt(i));
                TextEmoji32Item textEmojiInfo = createTextItem(tempString);
                CLog.i(TAG, "textEmojiInfo is text>>>" + textEmojiInfo);
                listTextEmojiInfos.add(textEmojiInfo);
            }
        }
        return listTextEmojiInfos;
    }

    // 创建表情项目的辅助方法
    private TextEmoji32Item createEmojiItem(String imageName, int emojiId) {
        TextEmoji32Item textEmojiInfo = new TextEmoji32Item();
        textEmojiInfo.isText = false;
        textEmojiInfo.text = imageName;
        textEmojiInfo.imageName = imageName;

        textEmojiInfo.imageName12 = "emoji_fc_12x12_" + emojiId;
        textEmojiInfo.imageName14 = "emoji_fc_14x14_" + emojiId;
        textEmojiInfo.imageName16 = "emoji_fc_16x16_" + emojiId;
        textEmojiInfo.imageName20 = "emoji_fc_20x20_" + emojiId;
        textEmojiInfo.imageName24 = "emoji_fc_24x24_" + emojiId;
        textEmojiInfo.imageName32 = "emoji_fc_32x32_" + emojiId;
        textEmojiInfo.color = Color.RED;

        return textEmojiInfo;
    }

    // 创建表情项目的辅助方法
    private TextEmoji32Item createEmojiItem(String imageName, int emojiType, int emojiId) {
        TextEmoji32Item textEmojiInfo = new TextEmoji32Item();
        textEmojiInfo.isText = false;
        textEmojiInfo.text = imageName;
        textEmojiInfo.imageName = imageName;

        textEmojiInfo.imageName12 = "emoji_fc_12x12_" + emojiType + "_" + emojiId+ "_data";
        textEmojiInfo.imageName14 = "emoji_fc_14x14_" + emojiType + "_" + emojiId+ "_data";
        textEmojiInfo.imageName16 = "emoji_fc_16x16_" + emojiType + "_" + emojiId+ "_data";
        textEmojiInfo.imageName20 = "emoji_fc_20x20_" + emojiType + "_" + emojiId+ "_data";
        textEmojiInfo.imageName24 = "emoji_fc_24x24_" + emojiType + "_" + emojiId+ "_data";
        textEmojiInfo.imageName32 = "emoji_fc_32x32_" + emojiType + "_" + emojiId+ "_data";
        textEmojiInfo.color = Color.RED;

        return textEmojiInfo;
    }

    // 创建文本项目的辅助方法
    private TextEmoji32Item createTextItem(String text) {
        TextEmoji32Item textEmojiInfo = new TextEmoji32Item();
        textEmojiInfo.isText = true;
        textEmojiInfo.text = text;
        textEmojiInfo.color = Color.RED;

        return textEmojiInfo;
    }

    public static void setTextViewWithColorTextAndEmojis(TextView textView, List<TextEmoji32Item> listTextEmoji32Items, String PATTEN_STR) {
        List<Integer> textColors = new ArrayList<>();
        int colorIndex = -1;
        String inputString = " ";
        for (TextEmoji32Item textEmojiItem : listTextEmoji32Items) {
            inputString += textEmojiItem.text;
            if (textEmojiItem.isText) {
                textColors.add(textEmojiItem.color);
            }
        }
        inputString = inputString.trim();
        CLog.i(TAG, "setTextViewWithColorTextAndEmojis>>>" + inputString);
        SpannableString spannableString = new SpannableString(inputString);

        int i = 0;
        while (i < inputString.length()) {
            boolean matched = false;

            // 先尝试匹配长格式表情: emoji_fc_12_345 (15字符)
            if (i <= inputString.length() - 15) {
                String longFormatString = inputString.substring(i, i + 15);
                CLog.i(TAG, "setTextViewWithColorTextAndEmojis>>longFormatString>>>" + longFormatString);

                if (ExpressionUtil.matchEmotion(longFormatString, PATTEN_STR)) {
                    String emojiName = longFormatString;
                    Field f;
                    try {
                        f = R.drawable.class.getDeclaredField(emojiName);
                        int j = f.getInt(R.drawable.class);
                        Drawable d = CoolLED.getInstance().getResources().getDrawable(j);
                        int textSize = (int) textView.getTextSize();
                        d.setBounds(0, 0, textSize, textSize);
                        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                        spannableString.setSpan(span, i, i + 15, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        CLog.i(TAG, "Matched long format emoji: " + emojiName);
                    } catch (Exception e) {
                        CoolLED.reportError(e);
                    }
                    i += 15; // 跳过15个字符
                    matched = true;
                }
            }

            // 如果没有匹配长格式，尝试匹配短格式表情: emoji_fc_123 (12字符)
            if (!matched && i <= inputString.length() - 12) {
                String shortFormatString = inputString.substring(i, i + 12);
                CLog.i(TAG, "setTextViewWithColorTextAndEmojis>>shortFormatString>>>" + shortFormatString);

                if (ExpressionUtil.matchEmotion(shortFormatString, PATTEN_STR)) {
                    String emojiName = shortFormatString;
                    Field f;
                    try {
                        f = R.drawable.class.getDeclaredField(emojiName);
                        int j = f.getInt(R.drawable.class);
                        Drawable d = CoolLED.getInstance().getResources().getDrawable(j);
                        int textSize = (int) textView.getTextSize();
                        d.setBounds(0, 0, textSize, textSize);
                        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                        spannableString.setSpan(span, i, i + 12, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        CLog.i(TAG, "Matched short format emoji: " + emojiName);
                    } catch (Exception e) {
                        CoolLED.reportError(e);
                    }
                    i += 12; // 跳过12个字符
                    matched = true;
                }
            }

            // 如果没有匹配任何表情，按普通文本处理
            if (!matched) {
                colorIndex++;
                if (colorIndex < textColors.size()) {
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(textColors.get(colorIndex));
                    if (i < (inputString.length() - 1)) {
                        spannableString.setSpan(colorSpan, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    } else {
                        spannableString.setSpan(colorSpan, i, inputString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                    CLog.i(TAG, "Set text color at position: " + i);
                }
                i++; // 移动到下一个字符
            }
        }

        textView.setText(spannableString);
    }

    public static void setTextViewWithEmojis(TextView textView, String input, String PATTEN_STR) {
        List<TextEmoji32Item> listTextEmojiItems = TextEmojiManagerCoolLEDUX.getInstance().getTextEmojiItems(input, PATTEN_STR);
        String inputString = " ";
        for (TextEmoji32Item textEmojiItem : listTextEmojiItems) {
            inputString += textEmojiItem.text;
        }
        inputString = inputString.trim();
        CLog.i(TAG, "setTextViewWithEmojis>>>" + inputString);
        SpannableString spannableString = ExpressionUtil.getExpressionString(textView.getContext(), inputString, (int) textView.getTextSize(), PATTEN_STR);
        textView.setText(spannableString);
    }

    public static List<TextEmoji32Item> reverseEmojiSequenceOnly(List<TextEmoji32Item> inputList) {
        List<TextEmoji32Item> result = new ArrayList<>();
        List<TextEmoji32Item> emojiBuffer = new ArrayList<>();

        for (TextEmoji32Item item : inputList) {
            if (!item.isText && item.text.startsWith("emoji_fc_")) {
                emojiBuffer.add(item);
            } else {
                // 遇到文字，先将之前积累的 emoji 倒序输出
                for (int i = emojiBuffer.size() - 1; i >= 0; i--) {
                    result.add(emojiBuffer.get(i));
                }
                emojiBuffer.clear();
                result.add(item);
            }
        }

        // 最后还有 emoji 没清空（句子以 emoji 结尾）
        for (int i = emojiBuffer.size() - 1; i >= 0; i--) {
            result.add(emojiBuffer.get(i));
        }

        return result;
    }


    public static void setTextViewWithEmojis(String languageCode, TextView textView, String input, String PATTEN_STR) {
        List<TextEmoji32Item> listTextEmojiItems = TextEmojiManagerCoolLEDUX.getInstance().getTextEmojiItems(languageCode, input, PATTEN_STR);
//        String inputString = " ";
//        for (TextEmoji32Item textEmojiItem : listTextEmojiItems) {
//            inputString += textEmojiItem.text;
//        }
//        inputString = inputString.trim();
////        inputString = ArabicCharDotMatrixGenerator.getVisualText(languageCode, inputString);
//        CLog.i(TAG, "setTextViewWithEmojis>>>" + inputString);
//        SpannableString spannableString = ExpressionUtil.getExpressionString(textView.getContext(), inputString, (int) textView.getTextSize(), PATTEN_STR);
//        textView.setText(spannableString);

        if ("ar".equals(languageCode) || "iw".equals(languageCode)) {
            listTextEmojiItems = reverseEmojiSequenceOnly(listTextEmojiItems);
        }

        StringBuilder inputString = new StringBuilder();
        for (TextEmoji32Item textEmojiItem : listTextEmojiItems) {
            inputString.append(textEmojiItem.text);
        }

        String finalText = inputString.toString().trim();

        CLog.i(TAG, "setTextViewWithEmojis>>> " + finalText);
        SpannableString spannableString = ExpressionUtil.getExpressionString(
                textView.getContext(), finalText, (int) textView.getTextSize(), PATTEN_STR
        );
        textView.setText(spannableString);

    }

    public static void setTextViewWithEmojis(String languageCode, TextView textView, TextEmoji32Items textEmoji32Items, String PATTEN_STR) {
        List<TextEmoji32Item> listTextEmojiItems = textEmoji32Items.textEmojiItems;
        String inputString = " ";
        for (TextEmoji32Item textEmojiItem : listTextEmojiItems) {
            inputString += textEmojiItem.text;
        }
        inputString = inputString.trim();
        inputString = ArabicCharDotMatrixGenerator.getVisualText(languageCode, inputString);
        CLog.i(TAG, "setTextViewWithEmojis>>>" + inputString);
        SpannableString spannableString = ExpressionUtil.getExpressionString(textView.getContext(), inputString, (int) textView.getTextSize(), PATTEN_STR);
        textView.setText(spannableString);
    }

    public static void setTextViewWithEmojis(TextView textView, TextEmoji32Items textEmoji32Items, String PATTEN_STR) {
        List<TextEmoji32Item> listTextEmojiItems = textEmoji32Items.textEmojiItems;
        String inputString = " ";
        for (TextEmoji32Item textEmojiItem : listTextEmojiItems) {
            inputString += textEmojiItem.text;
        }
        inputString = inputString.trim();
        CLog.i(TAG, "setTextViewWithEmojis>>>" + inputString);
        SpannableString spannableString = ExpressionUtil.getExpressionString(textView.getContext(), inputString, (int) textView.getTextSize(), PATTEN_STR);
        textView.setText(spannableString);
    }

    public static void setTextViewWithColorValue(TextView tv, int colorValue) {
        tv.setTextColor(colorValue);
    }

    public static int getColorWithColorData(List<String> tempListColors) {
        int red = 16 * Integer.valueOf(tempListColors.get(1), 16);
        int green = 16 * Integer.valueOf(tempListColors.get(2), 16);
        int blue = 16 * Integer.valueOf(tempListColors.get(2), 16);
        return Color.rgb(red, green, blue);
    }

    public static List<String> getColorDataWithColor(int colorValue) {
        List<String> result = new ArrayList<>();
        int red = Color.red(colorValue) / 16;
        int green = Color.green(colorValue) / 16;
        int blue = Color.blue(colorValue) / 16;
        result.add(Integer.toHexString(0) + Integer.toHexString(red));
        result.add(Integer.toHexString(green) + Integer.toHexString(blue));
        return result;
    }

    public static List<String> getColorDataWithColorWithRGB444Transfer(int colorValue) {
        List<String> result = new ArrayList<>();
        int red = rgb444Transfer(Color.red(colorValue));
        int green = rgb444Transfer(Color.green(colorValue));
        int blue = rgb444Transfer(Color.blue(colorValue));
        result.add(Integer.toHexString(0) + Integer.toHexString(red));
        result.add(Integer.toHexString(green) + Integer.toHexString(blue));
        return result;
    }

    /***
     * 全彩显示屏只支持4位的颜色深度，即显示范围为0~15。实际图片为8位深度，显示范围0~255。所以需要对颜色进行量化处理。
     * @param value
     * @return
     */
    public static int rgb444Transfer(int value) {
        int result = 0;
        if (value >= 238) {
            result = 15;
        } else if (value <= 47) {
            result = 0;
        } else {
            result = ((value - 47) / 14) + 1;
        }
        return result;
    }

    public static List<TextEmoji32Item> getTextEmojiItems(String inputString) {
        List<TextEmoji32Item> textEmojiItemList = TextEmojiManagerCoolLEDUX.getInstance().getTextEmojiItems(inputString, TextEmojiManagerCoolLEDUX.PATTEN_STR);
        return textEmojiItemList;
    }

    public static List<DrawView.DrawItem> getDrawItemsFromBitmap(int resourceId) {
        List<DrawView.DrawItem> drawItems = new ArrayList<>();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(CoolLED.getInstance().getResources(), resourceId, options);
        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 0; j < bitmap.getWidth(); j++) {
                int color = bitmap.getPixel(j, i);
                DrawView.DrawItem drawItem = new DrawView.DrawItem();
                drawItem.color = color;
                drawItem.data = "true";
                drawItems.add(drawItem);
            }
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return drawItems;
    }

    public static List<DrawView.DrawItem> getDrawItemsFromBitmap(String imageName) {
        List<DrawView.DrawItem> drawItems = new ArrayList<>();
        try {
            Field f = R.drawable.class.getDeclaredField(imageName);
            int imageId = f.getInt(R.drawable.class);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap bitmap = BitmapFactory.decodeResource(CoolLED.getInstance().getResources(), imageId, options);
            for (int i = 0; i < bitmap.getHeight(); i++) {
                for (int j = 0; j < bitmap.getWidth(); j++) {
                    int color = bitmap.getPixel(j, i);
                    DrawView.DrawItem drawItem = new DrawView.DrawItem();
                    drawItem.color = color;
                    drawItem.data = "true";
                    drawItems.add(drawItem);
                }
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        } catch (Exception e) {
            CLog.i(TAG, "getDrawItemsFromBitmap>>>exception>>>" + e.getMessage());
        }
        return drawItems;
    }

    public static List<DrawView.DrawItem> getDrawItemsFromBitmap(String imageName, int textSize, int showRow) {
        if (textSize != showRow) {
            List<DrawView.DrawItem> drawItems = new ArrayList<>();
            try {
                Field f = R.drawable.class.getDeclaredField(imageName);
                int imageId = f.getInt(R.drawable.class);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap bitmap = BitmapFactory.decodeResource(CoolLED.getInstance().getResources(), imageId, options);

                Bitmap newBitmap = Bitmap.createBitmap(showRow, showRow, Bitmap.Config.ARGB_8888);
                for (int i = 0; i < newBitmap.getHeight(); i++) {
                    for (int j = 0; j < newBitmap.getWidth(); j++) {
                        newBitmap.setPixel(j, i, Color.BLACK);
                    }
                }

                int startRow = (showRow - textSize) / 2;
                int startColumn = startRow;

                for (int i = startRow; i < (newBitmap.getHeight() - startRow); i++) {
                    for (int j = startColumn; j < (newBitmap.getWidth() - startColumn); j++) {
                        int color = bitmap.getPixel(j - startColumn, i - startRow);
                        newBitmap.setPixel(j, i, color);
                    }
                }


                for (int i = 0; i < newBitmap.getHeight(); i++) {
                    for (int j = 0; j < newBitmap.getWidth(); j++) {
                        int color = newBitmap.getPixel(j, i);
                        DrawView.DrawItem drawItem = new DrawView.DrawItem();
                        drawItem.color = color;
                        drawItem.data = "true";
                        drawItems.add(drawItem);
                    }
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }

                if (newBitmap != null && !newBitmap.isRecycled()) {
                    newBitmap.recycle();
                    newBitmap = null;
                }
            } catch (Exception e) {
                CLog.i(TAG, "getDrawItemsFromBitmap>>>exception>>>" + e.getMessage());
            }
            return drawItems;
        } else {
            return getDrawItemsFromBitmap(imageName);
        }
    }

    public static List<String> getImageData(List<DrawView.DrawItem> drawItems, int column, int row) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < column; i++) {
            int newRow = (row / 8 + ((row % 8) > 0 ? 1 : 0));
            for (int j = 0; j < newRow; j++) {
                int resultInt = 0;
                int target = 0;
                if (j < (newRow - 1)) {
                    target = 8;
                } else if (j == (newRow - 1)) {
                    if ((row % 8) > 0) {
                        target = row % 8;
                    } else {
                        target = 8;
                    }
                }
                for (int k = 0; k < target; k++) {
                    int index = (j * 8 + k) * column + i;
                    int color = drawItems.get(index).color;
                    int red = Color.red(color);
                    int green = Color.green(color);
                    int blue = Color.blue(color);
                    int temp;
                    if (red == 0 && green == 0 && blue == 0) {
                        temp = 0;
                    } else {
                        temp = 1;
                    }
                    int tempValue = (int) (temp * Math.pow(2, 7 - k));
                    resultInt += tempValue;
                }
                result.add(Utils.getHexStringForInt(resultInt));
            }
        }
        return result;
    }

    public static List<DrawView.DrawItem> getDrawItemsFromBitmap(Bitmap bitmap) {
        List<DrawView.DrawItem> drawItems = new ArrayList<>();
        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 0; j < bitmap.getWidth(); j++) {
                int color = bitmap.getPixel(j, i);
                DrawView.DrawItem drawItem = new DrawView.DrawItem();
                drawItem.color = color;
                drawItem.data = "true";
                drawItems.add(drawItem);
            }
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return drawItems;
    }


    public static Bitmap getScaledBitmapFromTargetBitmap(int width, int height, Bitmap input) {
//        CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>width>>>" + width + ">>>height>>>" + height);
//
//        Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        for (int i = 0; i < targetBitmap.getHeight(); i++) {
//            for (int j = 0; j < targetBitmap.getWidth(); j++) {
//                targetBitmap.setPixel(j, i, Color.TRANSPARENT);
//            }
//        }
//
//        int inputWidth = input.getWidth();
//        int inputHeight = input.getHeight();
//        CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>inputwidth>>>" + inputWidth + ">>>inputheight>>>" + inputHeight);
//        int startColumn = (width - inputWidth) / 2;
//        int startRow = (height - inputHeight) / 2;
//        CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>startColumn>>>" + startColumn + ">>>startRow>>>" + startRow);
//        for (int i = startRow; i < (startRow + inputHeight); i++) {
//            for (int j = startColumn; j < (startColumn + inputWidth); j++) {
//                int color = input.getPixel(j - startColumn, i - startRow);
//                CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>j>>>" + j + ">>>i>>>" + i);
//                targetBitmap.setPixel(j, i, color);
//            }
//        }
//
//        return targetBitmap;

        Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int left = (width - input.getWidth()) / 2;
        int top = (height - input.getHeight()) / 2;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(input, left, top, paint);

        return targetBitmap;
    }

    public static Bitmap getScaledBitmapFromTargetBitmap(int width, int height, Bitmap input, int index, int maxSteps) {
        CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>width>>>" + width + ">>>height>>>" + height);

        Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < targetBitmap.getHeight(); i++) {
            for (int j = 0; j < targetBitmap.getWidth(); j++) {
                targetBitmap.setPixel(j, i, Color.TRANSPARENT);
            }
        }

        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>inputwidth>>>" + inputWidth + ">>>inputheight>>>" + inputHeight);
        int tempHeight = (height - inputHeight) / 2 / maxSteps;
        int startColumn = (width - inputWidth) / 2;

        if (index == 0) {
            int startRow = inputHeight / 2;
            CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>startColumn>>>" + startColumn + ">>>startRow>>>" + startRow);
            for (int i = startRow; i < (startRow + inputHeight); i++) {
                for (int j = startColumn; j < (startColumn + inputWidth); j++) {
                    int color = input.getPixel(j - startColumn, i - startRow);
                    CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>j>>>" + j + ">>>i>>>" + i);
                    targetBitmap.setPixel(j, i, color);
                }
            }
        } else {
            int startRow = index * tempHeight;
            CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>startColumn>>>" + startColumn + ">>>startRow>>>" + startRow);
            for (int i = startRow; i < (startRow + inputHeight); i++) {
                for (int j = startColumn; j < (startColumn + inputWidth); j++) {
                    int color = input.getPixel(j - startColumn, i - startRow);
                    CLog.i(TAG, "getScaledBitmapFromTargetBitmap>>>j>>>" + j + ">>>i>>>" + i);
                    targetBitmap.setPixel(j, i, color);
                }
            }
        }

        return targetBitmap;
    }


}

