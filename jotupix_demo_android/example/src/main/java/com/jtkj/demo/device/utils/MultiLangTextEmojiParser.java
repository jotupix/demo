package com.jtkj.demo.device.utils;

import android.graphics.Color;
import android.os.Build;

import com.ibm.icu.text.BreakIterator;
import com.jtkj.demo.emoji.TextEmojiManagerCoolLEDUX;
import com.jtkj.demo.emoji.TextEmojiManager;
import com.jtkj.library.commom.logger.CLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiLangTextEmojiParser {
    private static final String TAG = MultiLangTextEmojiParser.class.getSimpleName();

    public static List<TextEmojiManager.TextEmoji32Item> getTextEmojiItems(String languageCode, String inputString, String PATTEN_STR) {
        if (languageCode.equalsIgnoreCase("ar")) {
            return getTextEmojiItemsByLanguage(languageCode, inputString, PATTEN_STR);
        }
        if (languageCode.equalsIgnoreCase("iw")) {
            return getTextEmojiItemsByLanguage(languageCode, inputString, PATTEN_STR);
        }
        if (languageCode.equalsIgnoreCase("hi")) {
            return getTextEmojiItemsByLanguage(languageCode, inputString, PATTEN_STR);
        }
        if (languageCode.equalsIgnoreCase("th")) {
            return getTextEmojiItemsByLanguage(languageCode, inputString, PATTEN_STR);
        }

        return TextEmojiManagerCoolLEDUX.getInstance().getTextEmojiItems(inputString, PATTEN_STR);

    }

    public static boolean isAllAsciiLetterOrDigit(String input) {
        return input.matches("[a-zA-Z0-9]+");
    }

    private enum ScriptType {
        ARABIC, IW, HI, TH, LATIN_DIGIT, WHITESPACE, PUNCTUATION, OTHER
    }

    private static ScriptType getScriptType(char c) {
        if (Character.isWhitespace(c)) return ScriptType.WHITESPACE;
        if (Character.isDigit(c) || Character.isLetter(c) && (c < 0x0600)) return ScriptType.LATIN_DIGIT;
        if (MultiLangTextEmojiParser.isAr(c)) return ScriptType.ARABIC;
        if (MultiLangTextEmojiParser.isIw(c)) return ScriptType.IW;
        if (MultiLangTextEmojiParser.isHi(c)) return ScriptType.HI;
        if (MultiLangTextEmojiParser.isTh(c)) return ScriptType.TH;
        if (isPunctuation(c)) return ScriptType.PUNCTUATION;
        return ScriptType.OTHER;
    }

    private static boolean isPunctuation(char c) {
        int type = Character.getType(c);
        return type == Character.CONNECTOR_PUNCTUATION
                || type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION
                || type == Character.END_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION;
    }

    private static List<String> splitByScriptType(String input) {
        List<String> parts = new ArrayList<>();
        if (input.isEmpty()) return parts;

        StringBuilder current = new StringBuilder();
        ScriptType prevType = getScriptType(input.charAt(0));
        current.append(input.charAt(0));

        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            ScriptType type = getScriptType(c);

            if (type == prevType) {
                current.append(c);
            } else {
                parts.add(current.toString());
                current.setLength(0);
                current.append(c);
                prevType = type;
            }
        }

        parts.add(current.toString());
        return parts;
    }


    public static List<TextEmojiManager.TextEmoji32Item> getTextEmojiItemsByLanguage(String languageCode, String inputString, String PATTEN_STR) {
        CLog.i(TAG, "inputString>>>" + inputString);
        Pattern pattern = Pattern.compile(PATTEN_STR, Pattern.CASE_INSENSITIVE);

        // —— 1. 用正则拆分 emoji_fc_xxx 段 ——
        Matcher em = pattern.matcher(inputString);
        List<String> segments = new ArrayList<>();
        int lastEnd = 0;
        while (em.find()) {
            if (em.start() > lastEnd) {
                segments.add(inputString.substring(lastEnd, em.start()));
            }
            segments.add(em.group());
            lastEnd = em.end();
        }
        if (lastEnd < inputString.length()) {
            segments.add(inputString.substring(lastEnd));
        }

        List<TextEmojiManager.TextEmoji32Item> result = new ArrayList<>();
        for (String seg : segments) {
            if (seg.startsWith("emoji_fc_")) {
                // —— 2. 这是一个 emoji 段 ——
                int emojiId = Integer.parseInt(seg.substring(9));
                TextEmojiManager.TextEmoji32Item ei = new TextEmojiManager.TextEmoji32Item();
                ei.isText = false;
                ei.text = seg;
                ei.imageName = seg;
                ei.imageName12 = "emoji_fc_12x12_" + emojiId;
                ei.imageName14 = "emoji_fc_14x14_" + emojiId;
                ei.imageName16 = "emoji_fc_16x16_" + emojiId;
                ei.imageName20 = "emoji_fc_20x20_" + emojiId;
                ei.imageName24 = "emoji_fc_24x24_" + emojiId;
                ei.imageName32 = "emoji_fc_32x32_" + emojiId;
                ei.color = Color.RED;
                result.add(ei);
                continue;
            }

            // —— 3. 其他文字段，先进一步分割为语言块 ——
            List<String> subSegments = splitByScriptType(seg);
            for (String sub : subSegments) {
                if ("ar".equals(languageCode)){
                    if ( isAr(sub.charAt(0))) {
                        // 使用阿拉伯语分词器
                        BreakIterator iter = BreakIterator.getWordInstance(new Locale("ar"));
                        iter.setText(sub);
                        int start = iter.first();
                        for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
                            String piece = sub.substring(start, end);
                            if (piece.trim().isEmpty()) {
                                for (char c : piece.toCharArray()) {
                                    addTextSegment(result, String.valueOf(c));
                                }
                            } else {
                                addTextSegment(result, piece);
                            }
                        }
                    } else {
                        // 默认逐字符处理
                        for (char c : sub.toCharArray()) {
                            addTextSegment(result, String.valueOf(c));
                        }
                    }
                }

                if ("iw".equals(languageCode)){
                    if ( isIw(sub.charAt(0))) {
                        // 使用阿拉伯语分词器
                        BreakIterator iter = BreakIterator.getWordInstance(new Locale("iw"));
                        iter.setText(sub);
                        int start = iter.first();
                        for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
                            String piece = sub.substring(start, end);
                            if (piece.trim().isEmpty()) {
                                for (char c : piece.toCharArray()) {
                                    addTextSegment(result, String.valueOf(c));
                                }
                            } else {
                                addTextSegment(result, piece);
                            }
                        }
                    } else {
                        // 默认逐字符处理
                        for (char c : sub.toCharArray()) {
                            addTextSegment(result, String.valueOf(c));
                        }
                    }
                }

                if ("hi".equals(languageCode)){
                    if ( isHi(sub.charAt(0))) {
                        // 使用阿拉伯语分词器
                        BreakIterator iter = BreakIterator.getWordInstance(new Locale("hi"));
                        iter.setText(sub);
                        int start = iter.first();
                        for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
                            String piece = sub.substring(start, end);
                            if (piece.trim().isEmpty()) {
                                for (char c : piece.toCharArray()) {
                                    addTextSegment(result, String.valueOf(c));
                                }
                            } else {
                                addTextSegment(result, piece);
                            }
                        }
                    } else {
                        // 默认逐字符处理
                        for (char c : sub.toCharArray()) {
                            addTextSegment(result, String.valueOf(c));
                        }
                    }
                }


                if ("th".equals(languageCode)){
                    if ( isTh(sub.charAt(0))) {
                        // 使用阿拉伯语分词器
                        BreakIterator iter = BreakIterator.getWordInstance(new Locale("th"));
                        iter.setText(sub);
                        int start = iter.first();
                        for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
                            String piece = sub.substring(start, end);
                            if (piece.trim().isEmpty()) {
                                for (char c : piece.toCharArray()) {
                                    addTextSegment(result, String.valueOf(c));
                                }
                            } else {
                                addTextSegment(result, piece);
                            }
                        }
                    } else {
                        // 默认逐字符处理
                        for (char c : sub.toCharArray()) {
                            addTextSegment(result, String.valueOf(c));
                        }
                    }
                }

            }
        }

        if ("ar".equals(languageCode)) {
            result = reverseConsecutiveEmojis(result);  // 如果你有处理 emoji 方向的函数
        }

        if ("iw".equals(languageCode)) {
            result = reverseConsecutiveEmojis(result);  // 如果你有处理 emoji 方向的函数
        }

        StringBuilder builder = new StringBuilder();
        for (TextEmojiManager.TextEmoji32Item item : result) {
            builder.append("|").append(item.text);
        }
        CLog.i(TAG, "inputString>>>" + inputString + ">>>result>>>" + builder);

        return result;
    }




//    public static List<TextEmojiManager.TextEmoji32Item> getTextEmojiItemsByLanguage(String languageCode, String inputString, String PATTEN_STR) {
//        CLog.i(TAG, "inputString>>>" + inputString);
//        Pattern pattern = Pattern.compile(PATTEN_STR, Pattern.CASE_INSENSITIVE);
//        // —— 1. 先正则拆分 emoji_fc_xxx 段 ——
//        Matcher em = pattern.matcher(inputString);
//        List<String> segments = new ArrayList<>();
//        int lastEnd = 0;
//        while (em.find()) {
//            if (em.start() > lastEnd) {
//                segments.add(inputString.substring(lastEnd, em.start()));
//            }
//            segments.add(em.group());
//            lastEnd = em.end();
//        }
//        if (lastEnd < inputString.length()) {
//            segments.add(inputString.substring(lastEnd));
//        }
//
//        List<TextEmojiManager.TextEmoji32Item> result = new ArrayList<>();
//        for (String seg : segments) {
//            CLog.i(TAG, "seg>>>" + seg);
//            // —— 2. 情况1：这是一个 emoji 段 ——
//            if (seg.startsWith("emoji_fc_")) {
//                int emojiId = Integer.parseInt(seg.substring(9));
//                CLog.i(TAG, "emojiId>>>" + emojiId);
//                TextEmojiManager.TextEmoji32Item ei = new TextEmojiManager.TextEmoji32Item();
//                ei.isText = false;
//                ei.text = seg;
//                ei.imageName = seg;
//                ei.imageName12 = "emoji_fc_12x12_" + emojiId;
//                ei.imageName14 = "emoji_fc_14x14_" + emojiId;
//                ei.imageName16 = "emoji_fc_16x16_" + emojiId;
//                ei.imageName20 = "emoji_fc_20x20_" + emojiId;
//                ei.imageName24 = "emoji_fc_24x24_" + emojiId;
//                ei.imageName32 = "emoji_fc_32x32_" + emojiId;
//                ei.color = Color.RED;
//                result.add(ei);
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("ar".equals(languageCode)) {
//                CLog.i(TAG, "isar>>>" + seg);
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("ar"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                        addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("iw".equals(languageCode) && isIw(seg.charAt(0))) {
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("iw"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                        addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("hi".equals(languageCode)) {
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("hi"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                        addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("th".equals(languageCode)) {
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("th"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                        addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 4. 其他语言／默认逐字符 ——
//            for (char c : seg.toCharArray()) {
//                addTextSegment(result, String.valueOf(c));
//            }
//        }
//
//
//        List<TextEmojiManager.TextEmoji32Item> newResult = new ArrayList<>();
//        for (TextEmojiManager.TextEmoji32Item item : result) {
//
//        }
//
//
//        if ("ar".equals(languageCode)) {
//            result = reverseConsecutiveEmojis(result);
//        }
//
//        if ("iw".equals(languageCode)) {
//            result = reverseConsecutiveEmojis(result);
//        }
//
//
//        StringBuilder builder = new StringBuilder();
//        for (TextEmojiManager.TextEmoji32Item item : result) {
//            builder.append("|");
//            builder.append(item.text);
//        }
//        CLog.i(TAG, "inputString>>>" + inputString + ">>>result>>>" + builder);
//        return result;
//    }


//    public static List<TextEmojiManager.TextEmoji32Item> getTextEmojiItemsByLanguage(String languageCode, String inputString, String PATTEN_STR) {
//        Pattern pattern = Pattern.compile(PATTEN_STR, Pattern.CASE_INSENSITIVE);
//        // —— 1. 先正则拆分 emoji_fc_xxx 段 ——
//        Matcher em = pattern.matcher(inputString);
//        List<String> segments = new ArrayList<>();
//        int lastEnd = 0;
//        while (em.find()) {
//            if (em.start() > lastEnd) {
//                segments.add(inputString.substring(lastEnd, em.start()));
//            }
//            segments.add(em.group());
//            lastEnd = em.end();
//        }
//        if (lastEnd < inputString.length()) {
//            segments.add(inputString.substring(lastEnd));
//        }
//
//        List<TextEmojiManager.TextEmoji32Item> result = new ArrayList<>();
//        for (String seg : segments) {
//            // —— 2. 情况1：这是一个 emoji 段 ——
//            if (seg.startsWith("emoji_fc_")) {
//                CLog.i(TAG, "seg>>>" + seg);
//                int emojiId = Integer.parseInt(seg.substring(9));
//                CLog.i(TAG, "emojiId>>>" + emojiId);
//                TextEmojiManager.TextEmoji32Item ei = new TextEmojiManager.TextEmoji32Item();
//                ei.isText = false;
//                ei.text = seg;
//                ei.imageName = seg;
//                ei.imageName12 = "emoji_fc_12x12_" + emojiId;
//                ei.imageName14 = "emoji_fc_14x14_" + emojiId;
//                ei.imageName16 = "emoji_fc_16x16_" + emojiId;
//                ei.imageName20 = "emoji_fc_20x20_" + emojiId;
//                ei.imageName24 = "emoji_fc_24x24_" + emojiId;
//                ei.imageName32 = "emoji_fc_32x32_" + emojiId;
//                ei.color = Color.RED;
//                result.add(ei);
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("ar".equals(languageCode)) {
//                CLog.i(TAG, "isar>>>" + seg);
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("ar"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                            addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("iw".equals(languageCode) && isIw(seg.charAt(0))) {
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("iw"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                        addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("hi".equals(languageCode)) {
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("hi"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                        addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 3. 情况2：这是文字段，按语言分支处理 ——
//            if ("th".equals(languageCode)) {
//                // 用一个新的 BreakIterator 专门处理这一小段
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("th"));
//                iter.setText(seg);
//                int start = iter.first();
//                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
//                    String piece = seg.substring(start, end);
//                    if (piece.trim().isEmpty()) {
//                        // 空白或标点
//                        for (char c : piece.toCharArray()) {
//                            addTextSegment(result, String.valueOf(c));
//                        }
//                    } else {
//                        addTextSegment(result, piece);
//                    }
//                }
//                continue;
//            }
//
//            // —— 4. 其他语言／默认逐字符 ——
//            for (char c : seg.toCharArray()) {
//                addTextSegment(result, String.valueOf(c));
//            }
//        }
//
//
//        List<TextEmojiManager.TextEmoji32Item> newResult = new ArrayList<>();
//        for (TextEmojiManager.TextEmoji32Item item : result) {
//
//        }
//
//
//        if ("ar".equals(languageCode)) {
//            result = reverseConsecutiveEmojis(result);
//        }
//
//        if ("iw".equals(languageCode)) {
//            result = reverseConsecutiveEmojis(result);
//        }
//
//
//        StringBuilder builder = new StringBuilder();
//        for (TextEmojiManager.TextEmoji32Item item : result) {
//            builder.append("|");
//            builder.append(item.text);
//        }
//        CLog.i(TAG, "inputString>>>" + inputString + ">>>result>>>" + builder);
//        return result;
//    }

    /**
     * 对一段混合的 TextEmoji32Item 列表：
     * - 连续 emoji 段 ①②③ → 翻转为 ③②①
     * - 文字段（isText==true）保持原序
     */
    public static List<TextEmojiManager.TextEmoji32Item> reverseConsecutiveEmojis(
            List<TextEmojiManager.TextEmoji32Item> arResult) {
        List<TextEmojiManager.TextEmoji32Item> output = new ArrayList<>();
        List<TextEmojiManager.TextEmoji32Item> emojiBuffer = new ArrayList<>();

        for (TextEmojiManager.TextEmoji32Item item : arResult) {
            if (!item.isText) {
                // 累积连续的 emoji
                emojiBuffer.add(item);
            } else {
                // 碰到文字，先把缓冲的 emoji 翻转输出
                if (!emojiBuffer.isEmpty()) {
                    Collections.reverse(emojiBuffer);
                    output.addAll(emojiBuffer);
                    emojiBuffer.clear();
                }
                // 然后输出当前文字段
                output.add(item);
            }
        }
        // 结尾如果还有残余 emoji，也翻转输出
        if (!emojiBuffer.isEmpty()) {
            Collections.reverse(emojiBuffer);
            output.addAll(emojiBuffer);
        }

        return output;
    }

//    public static List<TextEmojiManager.TextEmoji32Item> getTextEmojiItemsByLanguage(String languageCode, String inputString, String PATTEN_STR) {
//        CLog.i(TAG, "inputString>>>" + inputString);
//        for (int i = 0; i < inputString.length(); i++) {
//            char c = inputString.charAt(i);
//            CLog.i(TAG, "char " + c + " code: " + Integer.toHexString(c) + " isArabicLetter: " + isArabicLetter(c));
//        }
//
//        Pattern pattern = Pattern.compile(PATTEN_STR, Pattern.CASE_INSENSITIVE);
//        List<TextEmojiManager.TextEmoji32Item> result = new ArrayList<>();
//        int i = 0;
//        while (i < inputString.length()) {
//            if (i <= inputString.length() - 12) {
//                String firstString = inputString.substring(i, i + 9);
//                String second = String.valueOf(inputString.charAt(i + 9));
//                String third = String.valueOf(inputString.charAt(i + 10));
//                String fourth = String.valueOf(inputString.charAt(i + 11));
//                String wholeString = firstString.trim() + second + third + fourth;
//                Matcher matcher = pattern.matcher(wholeString);
//                if (matcher.find()) {
//                    int emojiId = Integer.parseInt(second + third + fourth);
//                    TextEmojiManager.TextEmoji32Item emojiItem = new TextEmojiManager.TextEmoji32Item();
//                    emojiItem.isText = false;
//                    emojiItem.text = wholeString;
//                    emojiItem.imageName = wholeString;
//                    emojiItem.imageName12 = "emoji_fc_12x12_" + emojiId;
//                    emojiItem.imageName14 = "emoji_fc_14x14_" + emojiId;
//                    emojiItem.imageName16 = "emoji_fc_16x16_" + emojiId;
//                    emojiItem.imageName20 = "emoji_fc_20x20_" + emojiId;
//                    emojiItem.imageName24 = "emoji_fc_24x24_" + emojiId;
//                    emojiItem.imageName32 = "emoji_fc_32x32_" + emojiId;
//                    emojiItem.color = Color.RED;
//                    result.add(emojiItem);
//                    i += 12;
//                    continue;
//                }
//            }
//
//
//            // 2. 其他语言或字符
//            if ("th".equals(languageCode)) {
//                // 泰语：使用 ICU BreakIterator 按词分割（相当音节）
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("th"));
//                iter.setText(inputString);
//                int start = iter.preceding(i + 1);
//                if (start < i) start = i;
//                int end = iter.next();
//                if (end == BreakIterator.DONE) end = i + 1;
//                // 截取当前分词
//                String piece = inputString.substring(i, end);
//                if (piece.trim().isEmpty()) {
//                    // 空白或标点，逐字符处理
//                    char c = inputString.charAt(i);
//                    addTextSegment(result, String.valueOf(c));
//                    i++;
//                } else {
//                    addTextSegment(result, piece);
//                    i += piece.length();
//                }
//                continue;
//            }
//
//            // 2. 其他语言或字符
//            if ("hi".equals(languageCode)) {
//                // 泰语：使用 ICU BreakIterator 按词分割（相当音节）
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("hi"));
//                iter.setText(inputString);
//                int start = iter.preceding(i + 1);
//                if (start < i) start = i;
//                int end = iter.next();
//                if (end == BreakIterator.DONE) end = i + 1;
//                // 截取当前分词
//                String piece = inputString.substring(i, end);
//                if (piece.trim().isEmpty()) {
//                    // 空白或标点，逐字符处理
//                    char c = inputString.charAt(i);
//                    addTextSegment(result, String.valueOf(c));
//                    i++;
//                } else {
//                    addTextSegment(result, piece);
//                    i += piece.length();
//                }
//                continue;
//            }
//
//            // 2. 其他语言或字符
//            if ("ar".equals(languageCode)) {
//                // 泰语：使用 ICU BreakIterator 按词分割（相当音节）
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("ar"));
//                iter.setText(inputString);
//                int start = iter.preceding(i + 1);
//                if (start < i) start = i;
//                int end = iter.next();
//                if (end == BreakIterator.DONE) end = i + 1;
//                // 截取当前分词
//                String piece = inputString.substring(i, end);
//                if (piece.trim().isEmpty()) {
//                    // 空白或标点，逐字符处理
//                    char c = inputString.charAt(i);
//                    addTextSegment(result, String.valueOf(c));
//                    i++;
//                } else {
//                    addTextSegment(result, piece);
//                    i += piece.length();
//                }
//                continue;
//            }
//
//            if ("iw".equals(languageCode)) {
//                // 泰语：使用 ICU BreakIterator 按词分割（相当音节）
//                BreakIterator iter = BreakIterator.getWordInstance(new Locale("iw"));
//                iter.setText(inputString);
//                int start = iter.preceding(i + 1);
//                if (start < i) start = i;
//                int end = iter.next();
//                if (end == BreakIterator.DONE) end = i + 1;
//                // 截取当前分词
//                String piece = inputString.substring(i, end);
//                if (piece.trim().isEmpty()) {
//                    // 空白或标点，逐字符处理
//                    char c = inputString.charAt(i);
//                    addTextSegment(result, String.valueOf(c));
//                    i++;
//                } else {
//                    addTextSegment(result, piece);
//                    i += piece.length();
//                }
//                continue;
//            }
//
////            if ("vi".equals(languageCode)) {
////                // 泰语：使用 ICU BreakIterator 按词分割（相当音节）
////                BreakIterator iter = BreakIterator.getWordInstance(new Locale("vi"));
////                iter.setText(inputString);
////                int start = iter.preceding(i + 1);
////                if (start < i) start = i;
////                int end = iter.next();
////                if (end == BreakIterator.DONE) end = i + 1;
////                // 截取当前分词
////                String piece = inputString.substring(i, end);
////                if (piece.trim().isEmpty()) {
////                    // 空白或标点，逐字符处理
////                    char c = inputString.charAt(i);
////                    addTextSegment(result, String.valueOf(c));
////                    i++;
////                } else {
////                    addTextSegment(result, piece);
////                    i += piece.length();
////                }
////                continue;
////            }
//
//            char current = inputString.charAt(i);
//
////            if (languageCode.equals("ar") && isArabicChar(current)) {
////                String segment = extractArabicLigatureSegment(inputString, i);
////                addTextSegment(result, segment);
////                i += segment.length();
////            }
////            else if (languageCode.equals("iw") && isHebrewChar(current)) {
////                String segment = extractHebrewSegment(inputString, i);
////                addTextSegment(result, segment);
////                i += segment.length();
////            }
//
////            else if (languageCode.equals("hi") && isDevanagariChar(current)) {
////                String segment = extractWhile(inputString, i, c -> isDevanagariChar(c) || isDevanagariMark(c));
////                addTextSegment(result, segment);
////                i += segment.length();
////            }
////            if (languageCode.equals("th") ) {
////                // 1) 先按“词”（词在泰语里近似音节）拆分
////                BreakIterator iter = BreakIterator.getWordInstance(new Locale("th"));
////                iter.setText(inputString);
////                int start = iter.first();
////                for (int end = iter.next(); end != BreakIterator.DONE; start = end, end = iter.next()) {
////                    String piece = inputString.substring(start, end);
////                    if (piece.trim().isEmpty()) {
////                        // 空白或标点，按字符再拆
////                        for (char c : piece.toCharArray()) {
////                            addTextSegment(result, String.valueOf(c));
////                        }
////                    } else {
////                        // 识别出的泰语“词”／音节
////                        addTextSegment(result, piece);
////                    }
////                }
////            }
////            else
//
//            addTextSegment(result, String.valueOf(current));
//        }
//
//                if ("ar".equals(languageCode)) {
//            result = reverseConsecutiveEmojis(result);
//        }
//
//        if ("iw".equals(languageCode)) {
//            result = reverseConsecutiveEmojis(result);
//        }
//
//        StringBuilder builder = new StringBuilder();
//        for (TextEmojiManager.TextEmoji32Item item : result) {
//            builder.append("|");
//            builder.append(item.text);
//        }
//        CLog.i(TAG, "inputString>>>" + inputString + ">>>result>>>" + builder);
//        return result;
//    }

    private static void addTextSegment(List<TextEmojiManager.TextEmoji32Item> list, String content) {
        TextEmojiManager.TextEmoji32Item item = new TextEmojiManager.TextEmoji32Item();
        item.isText = true;
        item.text = content;
        item.color = Color.RED;
        list.add(item);
    }

    private static String extractWhile(String input, int startIndex, Predicate<Character> condition) {
        StringBuilder builder = new StringBuilder();
        int i = startIndex;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            while (i < input.length() && condition.test(input.charAt(i))) {
                builder.append(input.charAt(i));
                i++;
            }
        }
        return builder.toString();
    }


//    //开始处理泰语
//    public static String extractThaiSyllable(String input, int startIndex) {
//        int len = input.length();
//        if (startIndex >= len) return "";
//
//        StringBuilder sb = new StringBuilder();
//        int i = startIndex;
//
//        // 1. 前置元音：เ แ โ ใ ไ
//        while (i < len && isThaiLeadingVowel(input.charAt(i))) {
//            sb.append(input.charAt(i));
//            i++;
//        }
//
//        // 2. 主辅音（必须存在）
//        if (i < len && isThaiConsonant(input.charAt(i))) {
//            sb.append(input.charAt(i));
//            i++;
//        } else {
//            // 不是泰语音节，返回单字符
//            return String.valueOf(input.charAt(startIndex));
//        }
//
//        // 3. 后续附加部分 —— tone mark、下标/上标元音、尾辅音
//        while (i < len) {
//            char c = input.charAt(i);
//            // 遇到新音节（前置元音或主辅音）则停止
//            if (isThaiLeadingVowel(c) || isThaiConsonant(c)) break;
//
//            if (isThaiCombiningChar(c)
//                    || isThaiTrailingVowel(c)
//                    || isThaiFinalConsonant(c)) {
//                sb.append(c);
//                i++;
//            } else {
//                break;
//            }
//        }
//
//        return sb.toString();
//    }
//
//    // ──────── 其余辅助判断函数请保持不变 ────────
//    public static boolean isThaiChar(char c) {
//        return c >= 0x0E00 && c <= 0x0E7F;
//    }
//    public static boolean isThaiConsonant(char c) {
//        return c >= 0x0E01 && c <= 0x0E2E;
//    }
//    private static boolean isThaiLeadingVowel(char c) {
//        return c >= 0x0E40 && c <= 0x0E44;
//    }
//    private static boolean isThaiCombiningChar(char c) {
//        int type = Character.getType(c);
//        return type == Character.NON_SPACING_MARK
//                || type == Character.COMBINING_SPACING_MARK
//                || type == Character.ENCLOSING_MARK;
//    }
//    private static boolean isThaiTrailingVowel(char c) {
//        return (c >= 0x0E30 && c <= 0x0E3A)
//                || (c >= 0x0E47 && c <= 0x0E4E);
//    }
//    public static boolean isThaiFinalConsonant(char c) {
//        return isThaiConsonant(c) && ("นมงยวลรฬ".indexOf(c) >= 0);
//    }
//
//    //处理泰语结束


    //开始处理希伯来语分割
    public static boolean isHebrewChar(char c) {
        return c >= 0x0590 && c <= 0x05FF;
    }

    private static String extractHebrewSegment(String input, int startIndex) {
        StringBuilder builder = new StringBuilder();
        int i = startIndex;
        if (i < input.length()) {
            char baseChar = input.charAt(i);
            builder.append(baseChar);
            i++;

            // 添加附加的元音符号和点符号（Niqqud）
            while (i < input.length()) {
                char next = input.charAt(i);
                int type = Character.getType(next);
                if (type == Character.NON_SPACING_MARK || next == '\u05BC' || next == '\u05C1' || next == '\u05C2') {
                    builder.append(next);
                    i++;
                } else {
                    break;
                }
            }
        }
        return builder.toString();
    }
    //处理希伯来语结束

    //开始处理阿拉伯语分割
    private static String extractArabicLigatureSegment(String input, int startIndex) {
        StringBuilder builder = new StringBuilder();
        int i = startIndex;
        char current = input.charAt(i);

        // Harakat 直接单独返回
        if (isHarakat(current)) return String.valueOf(current);

        builder.append(current);
        i++;

        while (i < input.length()) {
            char next = input.charAt(i);

            if (isHarakat(next)) {
                builder.append(next);
                i++;
                continue;
            }

            if (!shouldConnect(current, next)) break;

            builder.append(next);
            current = next;
            i++;
        }

        return builder.toString();
    }


    private static boolean shouldConnect(char current, char next) {
        if (!isArabicLetter(current) || !isArabicLetter(next)) return false;

        if (isIsolatedArabicChar(current) || isIsolatedArabicChar(next)) return false;

        if (current == 'ل' && (next == 'ا' || next == 'أ' || next == 'إ' || next == 'آ')) {
            CLog.i(TAG, "shouldConnect: Laam-Alef connected: " + current + " + " + next);
            return true;
        }

        if (current == 'أ' && next == 'ن') {
            CLog.i(TAG, "shouldConnect: Alef-Hamza + Noon connected: " + current + " + " + next);
            return true;
        }

        // 这里是新加的通用连接规则，保证多个阿拉伯字母间可以连接
        boolean connect = true;
        CLog.i(TAG, "shouldConnect: default connecting: " + current + " + " + next + " -> " + connect);
        return connect;
    }


    public static boolean isArabicChar(char c) {
        int code = c;
        return (code >= 0x0600 && code <= 0x06FF)
                || (code >= 0x0750 && code <= 0x077F)
                || (code >= 0x08A0 && code <= 0x08FF)
                || (code >= 0xFB50 && code <= 0xFDFF)
                || (code >= 0xFE70 && code <= 0xFEFF)
                || (code >= 0x1EE00 && code <= 0x1EEFF);
    }

    private static boolean isArabicLetter(char c) {
        int code = c;
        return (code >= 0x0621 && code <= 0x064A)
                || (code >= 0xFB50 && code <= 0xFDFF)
                || (code >= 0xFE70 && code <= 0xFEFF);
    }


    private static boolean isHarakat(char c) {
        return c >= 0x064B && c <= 0x065F;
    }

    private static boolean isIsolatedArabicChar(char c) {
        int[] isolatedChars = {
                0x0621, 0x0622, 0x0623, 0x0624, 0x0625,
                0x0627, 0x062F, 0x0630, 0x0631, 0x0632,
                0x0648, 0x0671, 0x0672, 0x0673, 0x0675
        };
        for (int code : isolatedChars) {
            if (c == code) return true;
        }
        return false;
    }
    //处理阿拉伯语结束


    public static boolean isDevanagariChar(char c) {
        return c >= 0x0900 && c <= 0x097F;
    }

    private static boolean isDevanagariMark(char c) {
        return c >= 0x093E && c <= 0x094D;
    }

    private static boolean isVietnameseLetter(char c) {
        // 基础字母 + 非间距附加符号（声调等）+ 修改符号
        int type = Character.getType(c);
        return Character.isLetter(c)
                || type == Character.NON_SPACING_MARK     // 非间距音符，例如 dấu sắc, dấu huyền
                || type == Character.MODIFIER_SYMBOL      // 修饰符，如 dấu mũ
                || type == Character.COMBINING_SPACING_MARK; // 少数越语输入法可能生成这种
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


    private static boolean isVietnameseChar(int codePoint) {
        // 基础拉丁字符 A-Z a-z
        if ((codePoint >= 0x0041 && codePoint <= 0x005A) ||  // A-Z
                (codePoint >= 0x0061 && codePoint <= 0x007A)) {  // a-z
            return true;
        }

        // 越南语使用的组合附加符号（变音符号）
        if ((codePoint >= 0x00C0 && codePoint <= 0x00FF) ||     // À-ÿ 拉丁-1补充
                (codePoint >= 0x0102 && codePoint <= 0x01B0) ||     // 拉丁扩展-A
                (codePoint >= 0x1EA0 && codePoint <= 0x1EF9)) {     // 越南语专用字符 (如 ắ, ộ)
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

}

