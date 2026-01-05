package com.jtkj.demo.device.utils;

import android.graphics.Color;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static final String HEXES = "0123456789ABCDEF";

    public static byte[] fromListStringToByteArray(List<String> strings) {
        byte[] bytes = new byte[strings.size()];
        if (strings != null && strings.size() > 0) {
            for (int i = 0; i < strings.size(); i++) {
                String temp = strings.get(i);
                int intStr = Integer.parseInt(temp, 16);
                bytes[i] = (byte) intStr;
            }
        }
        return bytes;
    }

    public static String byteArrayToHexString(final byte[] array) {
        final StringBuilder sb = new StringBuilder();

        for (final byte b : array) {
            sb.append(HEXES.charAt((b & 0xF0) >> 4));
            sb.append(HEXES.charAt((b & 0x0F)));
        }

        return sb.toString();
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append("0");
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String[] byteArraysToStringArrays(byte[] src) {
        String[] result = new String[src.length];

        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            StringBuilder stringBuilder = new StringBuilder("");
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append("0");
            }
            stringBuilder.append(hv);
            result[i] = stringBuilder.toString();
        }
        return result;
    }

    public static String bytesToString(byte[] src) {
        String res = null;
        try {
            res = new String(src, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }

    public static List<String> getDataString(int[] data) {
        List<String> resultStrs = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            resultStrs.add(getHexStringForInt(data[i]));
        }
        return resultStrs;
    }

    public static List<String> byte2hex(byte[] buffer) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            data.add(temp);
        }
        return data;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static List<String> getHexStringsForInt(int dir) {
        String resultStr = Integer.toHexString(dir);
        List<String> strings = new ArrayList<>();
        if (resultStr.length() == 1) {
            strings.add("0" + resultStr);
        } else if (resultStr.length() == 2) {
            strings.add(resultStr);
        }
        return strings;
    }

    public static String getHexStringForInt(int dir) {
        String resultStr = Integer.toHexString(dir);
        if (resultStr.length() == 1) {
            resultStr = "0" + resultStr;
        } else if (resultStr.length() == 2) {
        }
        return resultStr;
    }

    public static List<String> getHexListStringForInt(int dir) {
        String resultStr = Integer.toHexString(dir);
        List<String> strs = new ArrayList<String>();
        if (resultStr.length() == 1) {
            strs.add("0" + resultStr);
        } else if (resultStr.length() == 2) {
            strs.add(resultStr);
        }
        return strs;
    }

    public static String getHexStringForIntWithOneByte(int dir) {
        String resultStr = Integer.toHexString(dir);
        if (resultStr.length() == 1) {
            resultStr = "0" + resultStr;
        } else if (resultStr.length() == 2) {
        }
        return resultStr;
    }

    public static List<String> getHexListStringForWithOneByte(int dir) {
        String resultStr = Integer.toHexString(dir);
        List<String> strs = new ArrayList<String>();
        if (resultStr.length() == 1) {
            strs.add("0" + resultStr);
        } else if (resultStr.length() == 2) {
            strs.add(resultStr);
        }
        return strs;
    }

    public static List<String> getHexListStringForIntWithTwoByte(int dir) {
        String resultStr = Integer.toHexString(dir);
        List<String> strs = new ArrayList<String>();
        if (resultStr.length() == 1) {
            strs.add("00");
            strs.add("0" + resultStr);
        } else if (resultStr.length() == 2) {
            strs.add("00");
            strs.add(resultStr);
        } else if (resultStr.length() == 3) {
            strs.add("0" + resultStr.substring(0, 1));
            strs.add(resultStr.substring(1));
        } else if (resultStr.length() == 4) {
            strs.add(resultStr.substring(0, 2));
            strs.add(resultStr.substring(2));
        }
        return strs;
    }

    public static List<String> getDataStringLength(List<String> data) {
        List<String> lengthHexStr = new ArrayList<String>();
        if (data == null) {
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            return lengthHexStr;
        }
        int length = data.size();
        String resultStr = Integer.toHexString(length);
        if (resultStr.length() == 1) {
            lengthHexStr.add("00");
            lengthHexStr.add("0" + resultStr);
        } else if (resultStr.length() == 2) {
            lengthHexStr.add("00");
            lengthHexStr.add(resultStr);

        } else if (resultStr.length() == 3) {
            lengthHexStr.add("0" + resultStr.substring(0, 1));
            lengthHexStr.add(resultStr.substring(1));

        } else if (resultStr.length() == 4) {
            lengthHexStr.add(resultStr.substring(0, 2));
            lengthHexStr.add(resultStr.substring(2));
        }
        return lengthHexStr;
    }

    public static List<String> getHexListStringForIntWithTwo(int length) {
        List<String> lengthHexStr = new ArrayList<String>();
        String resultStr = Integer.toHexString(length);
        if (resultStr.length() == 1) {
            lengthHexStr.add("00");
            lengthHexStr.add("0" + resultStr);
        } else if (resultStr.length() == 2) {
            lengthHexStr.add("00");
            lengthHexStr.add(resultStr);

        } else if (resultStr.length() == 3) {
            lengthHexStr.add("0" + resultStr.substring(0, 1));
            lengthHexStr.add(resultStr.substring(1));

        } else if (resultStr.length() == 4) {
            lengthHexStr.add(resultStr.substring(0, 2));
            lengthHexStr.add(resultStr.substring(2));
        }
        return lengthHexStr;
    }


    public static List<String> getHexListStringForIntWithFourByte(List<String> data) {
        List<String> lengthHexStr = new ArrayList<String>();
        if (data == null) {
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            return lengthHexStr;
        }
        int length = data.size();
        return getHexListStringForIntWithFourByte(length);
    }

    public static List<String> getHexListStringForIntWithFourByte(int length) {
        List<String> lengthHexStr = new ArrayList<String>();
        String resultStr = Integer.toHexString(length);
        if (resultStr.length() == 1) {
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add("0" + resultStr);
        } else if (resultStr.length() == 2) {
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add(resultStr);

        } else if (resultStr.length() == 3) {
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add("0" + resultStr.substring(0, 1));
            lengthHexStr.add(resultStr.substring(1));

        } else if (resultStr.length() == 4) {
            lengthHexStr.add("00");
            lengthHexStr.add("00");
            lengthHexStr.add(resultStr.substring(0, 2));
            lengthHexStr.add(resultStr.substring(2));
        } else if (resultStr.length() == 5) {
            lengthHexStr.add("00");
            lengthHexStr.add("0" + resultStr.substring(0, 1));
            lengthHexStr.add(resultStr.substring(1, 3));
            lengthHexStr.add(resultStr.substring(3));
        } else if (resultStr.length() == 6) {
            lengthHexStr.add("00");
            lengthHexStr.add(resultStr.substring(0, 2));
            lengthHexStr.add(resultStr.substring(2, 4));
            lengthHexStr.add(resultStr.substring(4));
        } else if (resultStr.length() == 7) {
            lengthHexStr.add("0" + resultStr.substring(0, 1));
            lengthHexStr.add(resultStr.substring(1, 3));
            lengthHexStr.add(resultStr.substring(3, 5));
            lengthHexStr.add(resultStr.substring(5));
        } else if (resultStr.length() == 8) {
            lengthHexStr.add(resultStr.substring(0, 2));
            lengthHexStr.add(resultStr.substring(2, 4));
            lengthHexStr.add(resultStr.substring(4, 6));
            lengthHexStr.add(resultStr.substring(6));
        }
        return lengthHexStr;
    }

    public static List<String> recoverData(List<String> data) {
        List<String> targetData = new ArrayList<>();
        if (data.size() > 2 && Integer.parseInt(data.get(0), 16) == Integer.parseInt("01", 16)
                && Integer.parseInt(data.get(data.size() - 1), 16) == Integer.parseInt("03", 16)) {
            targetData = data.subList(1, data.size() - 1);
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < targetData.size(); i++) {
            if (Integer.parseInt(targetData.get(i), 16) != Integer.parseInt("02", 16)) {
                result.add(targetData.get(i));
            } else {
                if ((i + 1) < targetData.size()) {
                    int tempValue = Integer.valueOf(targetData.get(i + 1), 16);
                    tempValue ^= Integer.valueOf("04", 16);
                    result.add(getHexStringForInt(tempValue));
                    i++;
                }
            }
        }
        List<String> finalResult = new ArrayList<>();
        for (int i = 2; i < result.size(); i++) {
            finalResult.add(result.get(i));
        }
        return finalResult;
    }

    public static List<List<String>> recoverDataList(List<String> data) {
        List<List<String>> resultList = new ArrayList<>();
        List<List<String>> tempListStrings = extractFrames(data);
        if (null != tempListStrings && tempListStrings.size() > 0) {
            for (List<String> temp : tempListStrings) {
                List<String> tempResult = recoverData(temp);
                if (null != tempResult && tempResult.size() > 0) {
                    resultList.add(tempResult);
                }
            }
        }
        return resultList;
    }


    public static List<List<String>> extractFrames(List<String> hexData) {
        List<List<String>> frames = new ArrayList<>();
        int i = 0;
        while (i < hexData.size()) {
            // 找起始符 0x01
            while (i < hexData.size() && !"01".equalsIgnoreCase(hexData.get(i))) {
                i++;
            }

            if (i >= hexData.size()) break;

            int startIndex = i;
            i++;

            // 找结束符 0x03
            while (i < hexData.size() && !"03".equalsIgnoreCase(hexData.get(i))) {
                i++;
            }

            if (i < hexData.size()) {
                int endIndex = i;
                List<String> frame = new ArrayList<>(hexData.subList(startIndex, endIndex + 1));
                frames.add(frame);
                i++; // 继续查找下一个
            } else {
                break; // 没有结束符，不再继续
            }
        }

        return frames;
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
}
