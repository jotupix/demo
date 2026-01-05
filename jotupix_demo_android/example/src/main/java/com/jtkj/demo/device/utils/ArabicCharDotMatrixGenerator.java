package com.jtkj.demo.device.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import com.jtkj.demo.CoolLED;
import com.jtkj.demo.device.TextFontTypeItem;
import com.jtkj.library.commom.logger.CLog;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ArabicCharDotMatrixGenerator {
    private static final String TAG = ArabicCharDotMatrixGenerator.class.getSimpleName();

    public static boolean isLocalTypeSupported(String langCode) {
        CLog.i(TAG, "isLocalTypeSupported>>>langCode>>>" + langCode);
        if (langCode.equalsIgnoreCase("ar")) {
            return false;
        }

        if (langCode.equalsIgnoreCase("iw")) {
            return false;
        }

        if (langCode.equalsIgnoreCase("th")) {
            return false;
        }

        if (langCode.equalsIgnoreCase("hi")) {
            return false;
        }
        return true; // 不需要处理
    }

    public static String getVisualText(String langCode, String input) {
        if (input == null || input.trim().isEmpty()) return "";
        CLog.i(TAG, "result>>>getVisualText>>>input>>>" + input);
        try {
            switch (langCode) {
                case "ar": {
                    CLog.i(TAG, "result>>>getVisualText>>>ar>>>");
                    ArabicShaping shaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
                    String shaped = shaping.shape(input);
                    Bidi bidi = new Bidi(shaped, Bidi.DIRECTION_RIGHT_TO_LEFT);
                    String result = bidi.writeReordered(Bidi.DO_MIRRORING);
                    CLog.i(TAG, "result>>>getVisualText>>>result>>>" + result);
                    return result;
                }
                case "iw": {
                    CLog.i(TAG, "result>>>getVisualText>>>iw>>>");
                    Bidi bidi = new Bidi(input, Bidi.DIRECTION_RIGHT_TO_LEFT);
                    String result = bidi.writeReordered(Bidi.DO_MIRRORING);
                    CLog.i(TAG, "result>>>getVisualText>>>result>>>" + result);
                    return result;
                }
                case "vi": {
                    CLog.i(TAG, "result>>>getVisualText>>>vi>>>");
                    String result = Normalizer.normalize(input, Normalizer.Form.NFC);
                    CLog.i(TAG, "result>>>getVisualText>>>result>>>" + result);
                    return result;
                }
                case "th": {
                    CLog.i(TAG, "result>>>getVisualText>>>th>>>");
                    String result = Normalizer.normalize(input, Normalizer.Form.NFC);
                    CLog.i(TAG, "result>>>getVisualText>>>result>>>" + result);
                    return result;
                }
                case "hi": {
                    CLog.i(TAG, "result>>>getVisualText>>>hi>>>");
                    String result = Normalizer.normalize(input, Normalizer.Form.NFC);
                    CLog.i(TAG, "result>>>getVisualText>>>result>>>" + result);
                    return result;
                }
                default:
                    return input; // 不需要处理
            }
        } catch (Exception e) {
            e.printStackTrace();
            return input;
        }
    }

    public static byte[] readFontDataFromDraw(String langCode, char char_num, int showRow, int fontSize, boolean isBold, int fontType) {
        byte[] data = generateDotMatrix(langCode, char_num, showRow, isBold, fontSize, 128, fontType);
        return data;
    }

    public static byte[] readFontDataFromDraw(String langCode, char char_num, int showRow, int fontSize, boolean isBold) {
        byte[] data = generateDotMatrix(langCode, char_num, showRow, isBold, fontSize, 128);
        return data;
    }

    public static byte[] readFontDataFromDraw(String langCode, String content, int showWidth, int showHeight, int fontSize, boolean isBold) {
        byte[] data = generateDotMatrix(langCode, content, showWidth, showHeight, isBold, fontSize, 128);
        return data;
    }

    public static Typeface getFontByInput(char c, boolean isBold) {
        if (isAr(c)) {
            return getArFont(isBold);
        }

        if (isIw(c)) {
            return getIwFont(isBold);
        }

        if (isHi(c)) {
            return getHiFont(isBold);
        }

        if (isVi(c)) {
            return getViFont(isBold);
        }

        if (isTh(c)) {
            return getThFont(isBold);
        }

        return null;
    }

    public static Typeface getFontByInput(char c, boolean isBold, int fontType) {
        return getChineseFont(isBold, fontType);
    }

    public static String getLanguageCodeByInput(char c) {
        if (isAr(c)) {
            return "ar";
        }

        if (isIw(c)) {
            return "iw";
        }

        if (isHi(c)) {
            return "hi";
        }

        if (isVi(c)) {
            return "vi";
        }

        if (isTh(c)) {
            return "th";
        }
        return null;
    }

    public static byte[] generateDotMatrix(String langCode, char c, int sizePx, boolean isBold, int fontSize, int thresholdGray, int fontType) {
        Bitmap bitmap = drawSingleChar(langCode, c, sizePx, getFontByInput(c, isBold, fontType), isBold, fontSize, fontType);
        byte[][] dotMatrix;
        if (isVi(langCode)) {
            dotMatrix = bitmapToDotMatrixVi(bitmap, thresholdGray);
        } else {
            dotMatrix = bitmapToDotMatrix(bitmap, thresholdGray);
        }
        return dotMatrixToBytes(dotMatrix);
    }

    public static byte[] generateDotMatrix(String langCode, char c, int sizePx, boolean isBold, int fontSize, int thresholdGray) {
        Bitmap bitmap = drawSingleChar(langCode, c, sizePx, getFontByInput(c, isBold), isBold, fontSize);
        byte[][] dotMatrix;
        if (isVi(langCode)) {
            dotMatrix = bitmapToDotMatrixVi(bitmap, thresholdGray);
        } else {
            dotMatrix = bitmapToDotMatrix(bitmap, thresholdGray);
        }
        return dotMatrixToBytes(dotMatrix);
    }

    public static byte[] generateDotMatrix(String langCode, String content, int showWidth, int showHeight, boolean isBold, int fontSize, int thresholdGray) {
        Bitmap bitmap = drawArabicContent(langCode, content, showWidth, showHeight, getFontByInput(content.charAt(0), isBold), isBold, fontSize);
        byte[][] dotMatrix = bitmapToDotMatrix(bitmap, thresholdGray);
        return dotMatrixToBytes(dotMatrix);
    }

    public static Bitmap drawSingleChar(String langCode, char c, int sizePx, Typeface typeface, boolean isBold, int fontSizePx, int fontType) {
        boolean isDigitAndLetter = ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
        boolean isLetter = ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
        boolean isBigLetter = ((c >= 'A' && c <= 'Z'));
        boolean isSmallLetter = ((c >= 'a' && c <= 'z'));
        boolean isDigital = ((c >= '0' && c <= '9'));
        boolean isChinese = isChineseChar(c);
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // 白底

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK); // 黑色字体
        paint.setTextSize(fontSizePx);
        paint.setTypeface(typeface);

        if (isBold) {
            paint.setStyle(Paint.Style.FILL);
            if (fontType == TextFontTypeItem.BLACK) {
                if (isDigitAndLetter) {
                    if (fontSizePx > 16) {
                        paint.setFlags(Paint.ANTI_ALIAS_FLAG); // 大字用抗锯齿
                    } else {
                        paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                    }

//                    if (isLetter) {
//                        paint.setTextSize(fontSizePx + 3f);
//                    }
//
//                    if (isDigital) {
//                        paint.setTextSize(fontSizePx + 2.5f);
//                    }

                } else {
                    if (fontSizePx > 16) {
                        paint.setFlags(Paint.ANTI_ALIAS_FLAG); // 大字用抗锯齿
                        paint.setFakeBoldText(true);
                        paint.setStrokeWidth(0.8f); // 设置描边宽度
                        paint.setStyle(Paint.Style.FILL_AND_STROKE); // 关键：同时填充和描边
                        paint.setSubpixelText(true); // 启用子像素渲染
                        paint.setLinearText(true);   // 启用线性文本
                    } else {
                        paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                        paint.setFakeBoldText(true);
                        paint.setSubpixelText(true); // 启用子像素渲染
                        paint.setLinearText(true);   // 启用线性文本
                    }
                }
            } else if (fontType == TextFontTypeItem.SONG) {
                if (isDigitAndLetter) {
                    if (fontSizePx > 16) {
                        paint.setFakeBoldText(true);
                        paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                    } else {
                        paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                        paint.setFakeBoldText(true);
                        if (isLetter) {
                            paint.setTextSize(fontSizePx + 3f);
                        }

                        if (isDigital) {
                            paint.setTextSize(fontSizePx + 2.5f);
                        }
                        paint.setSubpixelText(true); // 启用子像素渲染
                        paint.setLinearText(true);   // 启用线性文本
                    }
                } else {
                    if (fontSizePx > 16) {
                        paint.setFlags(Paint.ANTI_ALIAS_FLAG); // 大字用抗锯齿
                        paint.setFakeBoldText(true);
                        paint.setStrokeWidth(0.8f); // 设置描边宽度
                        paint.setStyle(Paint.Style.FILL_AND_STROKE); // 关键：同时填充和描边
//                        paint.setSubpixelText(true); // 启用子像素渲染
//                        paint.setLinearText(true);   // 启用线性文本
                    } else {
                        paint.setFakeBoldText(true);
                        paint.setStrokeWidth(0.8f); // 设置描边宽度
                    }
                }
            }
        } else {
            paint.setStyle(Paint.Style.FILL);
            if (fontType ==TextFontTypeItem.BLACK) {
                if (fontSizePx > 16) {
                    paint.setFlags(Paint.ANTI_ALIAS_FLAG); // 大字用抗锯齿
                } else {
                    paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                    paint.setSubpixelText(true); // 启用子像素渲染
                    paint.setLinearText(true);   // 启用线性文本
                }
            } else if (fontType ==TextFontTypeItem.SONG) {
                if (isDigitAndLetter) {
                    if (fontSizePx > 16) {
                        paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                        paint.setSubpixelText(true); // 启用子像素渲染
                        paint.setLinearText(true);   // 启用线性文本
                    } else {
                        paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                        if (isLetter) {
                            paint.setTextSize(fontSizePx + 3f);
                        }

                        if (isDigital) {
                            paint.setTextSize(fontSizePx + 2f);
                        }
                    }
                } else {
                    if (fontSizePx > 16) {
                        paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                        paint.setSubpixelText(true); // 启用子像素渲染
                        paint.setLinearText(true);   // 启用线性文本
                    } else {
                        if (isChinese || isDigitAndLetter) {

                        } else {
                            paint.setFlags(0); // 小字禁用抗锯齿，保持笔画清晰
                        }
                        paint.setSubpixelText(true); // 启用子像素渲染
                        paint.setLinearText(true);   // 启用线性文本
                    }
                }
            }
        }

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextLocale(new Locale(langCode));

        Paint.FontMetrics fm = paint.getFontMetrics();
        float x = sizePx / 2f;
        float y = (sizePx - (fm.ascent + fm.descent)) / 2f;

        if (fontType == TextFontTypeItem.BLACK) {
            if (isBold) {
                if (isDigitAndLetter) {
                    if (fontSizePx > 16) {
                        paint.setTextSize(fontSizePx - 1f);
                        if (isLetter) {
                            if (c == 'g') {
                                y = y - 4;
                            } else {
                                y = y - 2;
                            }
                        }

                        if (isDigital) {
                            y = y - 2;
                        }
                    } else {
                        if (isBigLetter) {
                            y = y - 1;
                        }

                        if (isSmallLetter) {
                            y = y - 2;
                        }

                        if (isDigital) {
                            y = y - 1;
                        }
                    }
                } else {
                    if (fontSizePx > 16) {
                        paint.setTextSize(fontSizePx - 2f);
                        y = y - 2;
                    } else {

                    }
                }
            } else {
                if (isDigitAndLetter) {
                    if (fontSizePx > 16) {
                        if (c == 'g' || c == 'j' || c == 'p' || c == 'q' || c == 'y') {
                            y = y - 2.5f;
                            paint.setTextSize(fontSizePx - 2.5f);
                        } else {
                            paint.setTextSize(fontSizePx + 3f);
                        }
                    } else {
                        if (isSmallLetter) {
                            if (fontSizePx == 16) {
                                if (c == 'g' || c == 'j' || c == 'p' || c == 'q' || c == 'y') {
                                    y = y - 2;
                                    paint.setTextSize(fontSizePx - 1f);
                                } else {
                                    paint.setTextSize(fontSizePx + 3f);
                                }
                            } else if (fontSizePx == 14) {
                                if (c == 'g' || c == 'j' || c == 'p' || c == 'q' || c == 'y') {
                                    y = y - 2;
//                                    paint.setTextSize(fontSizePx - 1);
                                } else {
                                    paint.setTextSize(fontSizePx + 3f);
                                }
                            }
                        }
                    }
                } else {
                    if (fontSizePx > 16) {
                        y = y - 2;
                    } else {

                    }
                }
            }
        } else if (fontType == TextFontTypeItem.SONG) {
            if (isBold) {
                if (isDigitAndLetter) {
                    if (fontSizePx > 16) {

                    } else {
                        if (c == 'g') {
                            y = y - 2;
                        } else {
                            y = y - 1;
                        }

                        if(fontSizePx==14){
                            y = y + 1;
                        }

                        if(fontSizePx==12){
                            y = y + 2;
                        }

                    }
                }
            } else {
                if (isDigitAndLetter) {
                    if (fontSizePx > 16) {

                    } else {
                        if (isLetter) {
                            y = y - 1;
                        }

                        if (isDigital) {
                            y = y;
                        }

                    }
                } else {
                    if (c == '_') {
                        y = y - 1;
                    }
                }
            }
        }


        canvas.drawText(String.valueOf(c), x, y, paint);

        return bitmap;
    }

    /**
     * 绘制单个阿拉伯字符到 Bitmap（白底黑字，便于点阵提取）
     */
    public static Bitmap drawSingleChar(String langCode, char c, int sizePx, Typeface typeface, boolean isBold, int fontSizePx) {
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        if (isVi(langCode)) {
//            boolean isDigit = (c >= '0' && c <= '9');
            boolean isDigit = ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));

            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.BLACK); // 白底

            Paint paint = new Paint();
            paint.setColor(Color.WHITE); // 黑色字体
            paint.setTextSize(fontSizePx);
            if (sizePx == fontSizePx && fontSizePx == 12) {
                isDigit = false;
            }
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(false);  // 启用抗锯齿
            paint.setTypeface(typeface);
            paint.setTextLocale(new Locale(langCode));

            //越南语数字效果1
//            if (isDigit) {
//                if (c == '4') {
//                    // 预先设置统一的Typeface
//                    if (isBold) {
//                        if (sizePx == fontSizePx) {
//                            paint.setTextSize(fontSizePx + 1f);
//                            Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
//                            paint.setTypeface(boldTypeface);
//                        }
//                    }
//
//                    paint.setFontFeatureSettings("tnum");
//                    // 统一的笔画设置
//                    if (isBold) {
//                        paint.setStrokeWidth(1.2f);
//                        paint.setStrokeJoin(Paint.Join.ROUND);
//                    } else {
//                        paint.setStrokeWidth(0f);
//                    }
//                } else {
//                    if (isBold) {
//                        paint.setStrokeWidth(3f);
//                        paint.setFakeBoldText(true);
//                    } else {
//                        paint.setStrokeWidth(0f);
//                        paint.setFakeBoldText(false);
//                    }
//                }
//            }
//
//
//            Paint.FontMetrics fm = paint.getFontMetrics();
//            float x = sizePx / 2f;
//            float y = (sizePx - (fm.ascent + fm.descent)) / 2f;
//
//            if (sizePx == fontSizePx) {
//                if (isDigit) {
//                    if (c == '4') {
//                        if (isBold) {
//
//                        } else {
//                            y = y + 1f;
//                        }
//                    } else {
//                        y = y + 1f;
//                    }
//                } else {
//                    if (isBold) {
//                        y = y - 1;
//                    } else {
//                        y = y - 0.5f;
//                    }
//                }
//            }


            if (isDigit) {
                paint.setTextSize(fontSizePx + 1f);
                paint.setFontFeatureSettings("tnum");
                // 统一的笔画设置
                if (isBold) {
                    Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
                    paint.setTypeface(boldTypeface);
                    paint.setStrokeWidth(1.2f);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                } else {
                    Typeface boldTypeface = Typeface.create(typeface, Typeface.NORMAL);
                    paint.setTypeface(boldTypeface);
                    paint.setStrokeWidth(0f);
                }
            } else {
                if (sizePx == fontSizePx && fontSizePx == 12) {
                    paint.setTextSize(fontSizePx + 1f);
                    paint.setFontFeatureSettings("tnum");
                    // 统一的笔画设置
                    Typeface boldTypeface = Typeface.create(typeface, Typeface.NORMAL);
                    paint.setTypeface(boldTypeface);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setStrokeWidth(0f);
                }

//                if (isBold) {
//                    paint.setStrokeWidth(3f);
//                    paint.setFakeBoldText(true);
//                } else {
//                    paint.setStrokeWidth(0f);
//                    paint.setFakeBoldText(false);
//                }
            }


            Paint.FontMetrics fm = paint.getFontMetrics();
            float x = sizePx / 2f;
            float y = (sizePx - (fm.ascent + fm.descent)) / 2f;

            if (sizePx == fontSizePx) {
                if (isDigit) {
                    if (isBold) {
                        y = y - 1;
                    } else {
                        y = y - 0.5f;
                    }
                } else {
                    if (fontSizePx == 12) {
                        y = y - 1;
                    } else {
                        if (isBold) {
                            y = y - 1;
                        } else {
                            y = y - 0.5f;
                        }
                    }
                }
            }

            canvas.drawText(String.valueOf(c), x, y, paint);
        } else {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE); // 白底

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK); // 黑色字体
            if (isAr(c)) {
                if (isBold) {
                    paint.setTextSize(fontSizePx);
                } else {
                    paint.setTextSize(fontSizePx * 0.9f); // 模拟细字体
                }
            } else {
                paint.setTextSize(fontSizePx);
            }

            paint.setTypeface(typeface);
            paint.setTextAlign(Paint.Align.CENTER);
//        paint.setFakeBoldText(isBold);  // 加粗模拟
            paint.setTextLocale(new Locale(getLanguageCodeByInput(c)));

            Paint.FontMetrics fm = paint.getFontMetrics();
            float x = sizePx / 2f;
            float y = (sizePx - (fm.ascent + fm.descent)) / 2f;

            canvas.drawText(String.valueOf(c), x, y, paint);
        }
        return bitmap;
    }

    public static Bitmap drawArabicContent(String langCode, String content, int showWidth, int showHeight, Typeface typeface, boolean isBold, int fontSizePx) {
        Bitmap bitmap = Bitmap.createBitmap(showWidth, showHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // 白底

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK); // 黑色字体

        if (isAr(content.charAt(0))) {
            if (isBold) {
                paint.setTextSize(fontSizePx);
            } else {
                paint.setTextSize(fontSizePx * 0.9f); // 模拟细字体
            }
        } else {
            paint.setTextSize(fontSizePx);
        }

        paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.RIGHT); // 注意：RTL 要靠右对齐
//        paint.setFakeBoldText(isBold);  // 加粗模拟
        paint.setTextLocale(new Locale(getLanguageCodeByInput(content.charAt(0))));

        Paint.FontMetrics fm = paint.getFontMetrics();
        float x = showWidth - 5; // 靠右边一点，防止被裁剪
        float y = (showHeight - (fm.ascent + fm.descent)) / 2f;
        String newContent;
        if (isAr(content.charAt(0)) || isIw(content.charAt(0))) {
            newContent = reverseIfArabic(content);
        } else {
            newContent = content;
        }
        canvas.drawText(newContent, x, y, paint);
        return bitmap;
    }

    public static Bitmap drawArabicContent(String content, int showWidth, int showHeight, Typeface typeface, int fontSizePx) {
        Bitmap bitmap = Bitmap.createBitmap(showWidth, showHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // 白底

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK); // 黑色字体
        paint.setTextSize(fontSizePx);
        paint.setTypeface(typeface);
        paint.setTextAlign(Paint.Align.RIGHT); // 注意：RTL 要靠右对齐

        paint.setTextLocale(new Locale(getLanguageCodeByInput(content.charAt(0))));

        Paint.FontMetrics fm = paint.getFontMetrics();
        float x = showWidth - 5; // 靠右边一点，防止被裁剪
        float y = (showHeight - (fm.ascent + fm.descent)) / 2f;
        String newContent;
        if (isAr(content.charAt(0)) || isIw(content.charAt(0))) {
            newContent = reverseIfArabic(content);
        } else {
            newContent = content;
        }
        canvas.drawText(newContent, x, y, paint);
        return bitmap;
    }

    public static String reverseIfArabic(String input) {
        // 简单判断是否为 Arabic 字符串（你已经有 isArabicLetter 判断）
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * 将 Bitmap 转为 0/1 点阵（基于灰度判断黑色区域）
     */
    public static byte[][] bitmapToDotMatrix(Bitmap bitmap, int thresholdGray) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        byte[][] matrix = new byte[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = bitmap.getPixel(x, y);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                matrix[y][x] = (byte) (gray < thresholdGray ? 1 : 0); // 黑色为亮点
            }
        }
        return matrix;
    }

    /**
     * 将 Bitmap 转为 0/1 点阵（基于灰度判断黑色区域）
     */
    public static byte[][] bitmapToDotMatrixVi(Bitmap bitmap, int thresholdGray) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        byte[][] matrix = new byte[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = bitmap.getPixel(x, y);
                int rgbcolor = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel));
                matrix[y][x] = (byte) (rgbcolor > 0 ? 1 : 0);
            }
        }
        return matrix;
    }

    public static byte[] dotMatrixToBytes(byte[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int bytesPerColumn = (rows + 7) / 8; // 每列需要的字节数
        byte[] result = new byte[cols * bytesPerColumn];

        for (int col = 0; col < cols; col++) {
            for (int byteIndex = 0; byteIndex < bytesPerColumn; byteIndex++) {
                byte b = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int row = byteIndex * 8 + bit;

                    // 处理行数不足的情况
                    if (row >= rows) {
                        break; // 剩余位保持为0
                    }

                    // 关键修改：反转位顺序
                    // bit0 -> 顶部像素（物理位置的最上方）
                    // 存储时：bit7=最顶部，bit0=最底部
                    if (matrix[row][col] == 1) {
                        // 将顶部像素放在最高位 (bit7)
                        b |= (1 << (7 - bit));
                    }
                }
                result[col * bytesPerColumn + byteIndex] = b;
            }
        }
        return result;
    }


    /**
     * 打印点阵图（# 表示点亮，. 表示未亮）
     */
    public static void printDotMatrix(byte[][] matrix) {
        int i = 0;
        for (byte[] row : matrix) {
            StringBuilder sb = new StringBuilder();
            for (byte b : row) {
                sb.append(b == 1 ? '*' : '.');
            }
            sb.append(String.valueOf(i) + row.length);
            i++;
            CLog.i("DotMatrix", sb.toString());
        }
    }

    public static boolean isAr(char c) {
        int codePoint = c;
        // Arabic
        if ((codePoint >= 0x0600 && codePoint <= 0x06FF) ||  // Arabic
                (codePoint >= 0x0750 && codePoint <= 0x077F) ||  // Arabic Supplement
                (codePoint >= 0x08A0 && codePoint <= 0x08FF) ||  // Arabic Extended-A
                (codePoint >= 0xFB50 && codePoint <= 0xFDFF) ||  // Arabic Presentation Forms-A
                (codePoint >= 0xFE70 && codePoint <= 0xFEFF) ||  // Arabic Presentation Forms-B
                (codePoint >= 0x1EE00 && codePoint <= 0x1EEFF)   // Arabic Mathematical Alphabetic Symbols
        ) {
            return true;
        }

        return false;
    }

    public static boolean isIw(char c) {
        int codePoint = c;
        // Hebrew
        if (codePoint >= 0x0590 && codePoint <= 0x05FF) {
            return true;
        }

        return false;
    }

    public static boolean isVi(char c) {
        int codePoint = c;
        // Vietnamese (Latin letters + diacritics)
        if (isVietnameseChar(codePoint)) {
            return true;
        }

        return false;
    }

    public static boolean isVi(String langCode) {
        return langCode.equalsIgnoreCase("vi");
    }

    public static boolean isChinese(String langCode) {
        return langCode.equalsIgnoreCase("zh-CN");
    }

    public static boolean isTh(char c) {
        int codePoint = c;
        // Thai
        if (codePoint >= 0x0E00 && codePoint <= 0x0E7F) {
            return true;
        }

        return false;
    }

    public static boolean isHi(char c) {
        int codePoint = c;
        // Hindi (Devanagari script)
        if (codePoint >= 0x0900 && codePoint <= 0x097F) {
            return true;
        }

        return false;
    }

    public static boolean isLanguageSupported(char c) {
        int codePoint = c;

        // Arabic
        if ((codePoint >= 0x0600 && codePoint <= 0x06FF) ||  // Arabic
                (codePoint >= 0x0750 && codePoint <= 0x077F) ||  // Arabic Supplement
                (codePoint >= 0x08A0 && codePoint <= 0x08FF) ||  // Arabic Extended-A
                (codePoint >= 0xFB50 && codePoint <= 0xFDFF) ||  // Arabic Presentation Forms-A
                (codePoint >= 0xFE70 && codePoint <= 0xFEFF) ||  // Arabic Presentation Forms-B
                (codePoint >= 0x1EE00 && codePoint <= 0x1EEFF)   // Arabic Mathematical Alphabetic Symbols
        ) {
            CLog.i(TAG, "isAr");
            return false;
        }


        // Hebrew
        if (codePoint >= 0x0590 && codePoint <= 0x05FF) {
            CLog.i(TAG, "isHe");
            return false;
        }

        // Hindi (Devanagari script)
        if (codePoint >= 0x0900 && codePoint <= 0x097F) {
            CLog.i(TAG, "isHi");
            return false;
        }

        // Thai
        if (codePoint >= 0x0E00 && codePoint <= 0x0E7F) {
            CLog.i(TAG, "isTh");
            return false;
        }

        // Vietnamese (Latin letters + diacritics)
        if (isVietnameseChar(codePoint)) {
            CLog.i(TAG, "isVi");
            return false;
        }

//        if (isChineseChar(codePoint)) {
//            CLog.i(TAG, "is Zh-CN");
//            return false;
//        }

        return true;
    }


    private static boolean isChineseChar(int codePoint) {
        // Simplified Chinese (简体中文)
        if ((codePoint >= 0x4E00 && codePoint <= 0x9FFF) ||  // CJK Unified Ideographs (基本汉字)
                (codePoint >= 0x3400 && codePoint <= 0x4DBF) ||  // CJK Extension A (包含部分简体)
                (codePoint >= 0x20000 && codePoint <= 0x2A6DF) || // CJK Extension B (包含部分简体)
                (codePoint >= 0xF900 && codePoint <= 0xFAFF) ||  // CJK Compatibility Ideographs
                (codePoint >= 0x2F800 && codePoint <= 0x2FA1F)   // CJK Compatibility Supplement
        ) {
            CLog.i(TAG, "isZh-CN");
            return true;
        }

        return false;
    }


    private static boolean isVietnameseChar(int codePoint) {
//        // 基础拉丁字符 A-Z a-z
//        if ((codePoint >= 0x0041 && codePoint <= 0x005A) ||  // A-Z
//                (codePoint >= 0x0061 && codePoint <= 0x007A)) {  // a-z
//            return true;
//        }

        // 越南语使用的组合附加符号（变音符号）
        if ((codePoint >= 0x00C0 && codePoint <= 0x00FF) ||     // À-ÿ 拉丁-1补充
                (codePoint >= 0x0102 && codePoint <= 0x01B0) ||     // 拉丁扩展-A
                (codePoint >= 0x1EA0 && codePoint <= 0x1EF9)) {     // 越南语专用字符 (如 ắ, ộ)
            return true;
        }

        return false;
    }

    static Map<String, Typeface> typefaceMap = new HashMap<>();

    public static Typeface getChineseFont(boolean isBold, int fontType) {
        if (null != typefaceMap.get(fontType + String.valueOf(isBold))) {
            return typefaceMap.get(fontType + String.valueOf(isBold));
        } else {
            if (fontType == TextFontTypeItem.SONG) {
                CLog.i(TAG, "chinese_song.ttf");
                Typeface typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "chinese_song.ttf");
                if (isBold) {
                    Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
                    typefaceMap.put(fontType + String.valueOf(isBold), boldTypeface);
                    return boldTypeface;
                } else {
                    Typeface normalTypeface = Typeface.create(typeface, Typeface.NORMAL);
                    typefaceMap.put(fontType + String.valueOf(isBold), normalTypeface);
                    return normalTypeface;
                }

            } else if (fontType == TextFontTypeItem.BLACK) {
                CLog.i(TAG, "chinese_black.ttf");
//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "chinese_black.ttf");
                Typeface typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "chinese_yahei.ttf");
                if (isBold) {
                    Typeface boldTypeface = Typeface.create(typeface, Typeface.BOLD);
                    typefaceMap.put(fontType + String.valueOf(isBold), boldTypeface);
                    return boldTypeface;
                } else {
                    Typeface normalTypeface = Typeface.create(typeface, Typeface.NORMAL);
                    typefaceMap.put(fontType + String.valueOf(isBold), normalTypeface);
                    return normalTypeface;
                }
            } else {
                return null;
            }
        }
    }


    public static Typeface getArFont(boolean isBold) {
        Typeface typeface;
        if (isBold) {
            CLog.i(TAG, "ar-bold.ttf");
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "Arabic_Bold.ttf");
//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "ar-bold.ttf");
        } else {
            CLog.i(TAG, "ar-thin.ttf");
//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "Arabic_Light.ttf");
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "Arabic_Bold.ttf");
//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "Amiri-Regular.ttf");
        }
        return typeface;
    }

    public static Typeface getThFont(boolean isBold) {
        Typeface typeface;
        if (isBold) {
            CLog.i(TAG, "getThFont>>>th-bold.ttf");
            typeface = Typeface.create("sans-serif-thai", Typeface.BOLD);
//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "th-bold.ttf");

//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "NotoSansThai-Bold.ttf");
        } else {
            CLog.i(TAG, "getThFont>>>th-thin.ttf");
            typeface = Typeface.create("sans-serif-thai", Typeface.NORMAL);
//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "th-thin.ttf");

//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "NotoSansThai-Thin.ttf");
        }
        return typeface;
    }

    public static Typeface getViFont(boolean isBold) {
        Typeface typeface;
        if (isBold) {
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "vi-bold1.ttf");
        } else {
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "vi-thin1.ttf");
//            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "vi-bold1.ttf");
        }
        return typeface;
    }

    public static Typeface getHiFont(boolean isBold) {
        Typeface typeface;
        if (isBold) {
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "hi-bold.ttf");
        } else {
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "hi-thin.ttf");
        }
        return typeface;
    }

    public static Typeface getIwFont(boolean isBold) {
        Typeface typeface;
        if (isBold) {
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "iw-bold.ttf");
        } else {
            typeface = Typeface.createFromAsset(CoolLED.getInstance().getResources().getAssets(), "iw-thin.ttf");
        }
        return typeface;
    }

}
