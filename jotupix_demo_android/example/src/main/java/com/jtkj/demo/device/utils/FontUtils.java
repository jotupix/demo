package com.jtkj.demo.device.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.jtkj.demo.CoolLED;
import com.jtkj.demo.device.ModeAdapter;
import com.jtkj.demo.device.TextFontTypeItem;
import com.jtkj.demo.emoji.TextEmojiManager;
import com.jtkj.demo.emoji.TextEmojiManagerCoolLEDUX;
import com.jtkj.demo.widget.DrawView;
import com.jtkj.jotupix.program.JTextDiyColorContent;
import com.jtkj.jotupix.program.JTextFont;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.fastble.utils.HexUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FontUtils {
    private static final String TAG = FontUtils.class.getSimpleName();
    private final static String UNICODE12 = "UNICODE12";
    private final static String UNICODE16 = "UNICODE16";
    private final static String UNICODE16_BOLD = "UNICODE16_bold";
    private final static String UNICODE32_32 = "32_32_small";
    private final static String UNICODE32_32_BOLD = "32_32_large";
    private final static String UNICODE32_24 = "32_24_small";
    private final static String UNICODE32_24_BOLD = "32_24_large";
    private final static String UNICODE_20 = "20_small";
    private final static String UNICODE_20_BOLD = "20_large";
    private final static String UNICODE32_16 = "32_16_small";
    private final static String UNICODE32_16_BOLD = "32_16_large";
    private final static String UNICODE32_14 = "32_14_small";
    private final static String UNICODE32_14_BOLD = "32_14_large";
    private final static String UNICODE8 = "8_small";
    private final static String UNICODE8_BOLD = "8_large";

    public static volatile FontUtils mInstance;

    public static FontUtils getInstance(Context context) {
        if (null == mInstance) {
            synchronized (FontUtils.class) {
                if (null == mInstance) {
                    mInstance = new FontUtils();
                }
            }
        }
        return mInstance;
    }

    /***
     * 二位数组旋转
     * @param matrix
     * @return
     */
    private static String[][] rotate(String[][] matrix) {
        int n = matrix.length;
        String[][] m = new String[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                m[row][col] = matrix[n - 1 - col][row];
            }
        }
        //再赋值回matrix，注意java是形参是引用传递
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                matrix[row][col] = m[row][col];
            }
        }
        return matrix;
    }

    private static byte[] mirror(byte[] input) {
        byte[] newArray = new byte[input.length];

        for (int i = 0; i <= input.length - 2; i += 2) {
            newArray[i] = input[input.length - 2 - i];
            newArray[i + 1] = input[input.length - 2 + 1 - i];
        }

        return newArray;
    }


    private static byte[] mirror(byte[] input, int byteNumber) {
        byte[] newArray = new byte[input.length];
        if (byteNumber == 2) {
            for (int i = 0; i <= input.length - 2; i += 2) {
                newArray[i] = input[input.length - 2 - i];
                newArray[i + 1] = input[input.length - 2 + 1 - i];
            }
        } else if (byteNumber == 3) {
            for (int i = 0; i <= input.length - 3; i += 3) {
                newArray[i] = input[input.length - 3 - i];
                newArray[i + 1] = input[input.length - 3 + 1 - i];
                newArray[i + 2] = input[input.length - 3 + 2 - i];
            }
        } else if (byteNumber == 4) {
            for (int i = 0; i <= input.length - 4; i += 4) {
                newArray[i] = input[input.length - 4 - i];
                newArray[i + 1] = input[input.length - 4 + 1 - i];
                newArray[i + 2] = input[input.length - 4 + 2 - i];
                newArray[i + 3] = input[input.length - 4 + 3 - i];
            }
        }
        return newArray;
    }

    public static List<String> mirrorCoolledUTextColors(List<String> allTextColors) {
        List<String> result = new ArrayList<>();
        int count = allTextColors.size() / 2;
        for (int i = (count - 1); i >= 0; i--) {
            int index = i * 2;
            result.add(allTextColors.get(index));
            result.add(allTextColors.get(index + 1));
        }
        return result;
    }

    /***
     * 根据给定的角度(0 90 180 270 360度）对文字字节数据进行旋转
     * @param degree
     * @param input
     * @return
     */
    private static byte[] rotate(int degree, byte[] input) {
        byte[] output = null;
        if (degree == 0 || degree == 360) {
            output = input;
        } else if (degree == 90) {
            output = rotate90Degree(input);
        } else if (degree == 180) {
            output = rotate90Degree(rotate90Degree(input));
        } else if (degree == 270) {
            output = rotate90Degree(rotate90Degree(rotate90Degree(input)));
        }
        return output;
    }

    private List<String> deleteEmptyColorForImageData(List<String> imageData, int showRow) {
        List<String> output = null;
        return output;
    }

    public static List<DrawView.DrawItem> rotate(int degree, List<DrawView.DrawItem> input, int showRow) {
        List<DrawView.DrawItem> output = null;
        if (degree == 0 || degree == 360) {
            output = input;
        } else if (degree == 90) {
            output = rotate90Degree(input, showRow);
        } else if (degree == 180) {
            output = rotate90Degree(rotate90Degree(input, showRow), showRow);
        } else if (degree == 270) {
            output = rotate90Degree(rotate90Degree(rotate90Degree(input, showRow), showRow), showRow);
        }
        return output;
    }

    private static List<DrawView.DrawItem> rotate90Degree(List<DrawView.DrawItem> input, int showRow) {
        int mTempRow = showRow;
        int mTempColumn = showRow;
        int count = mTempRow * mTempColumn;
        List<DrawView.DrawItem> mTempData = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            mTempData.add(new DrawView.DrawItem());
        }
        List<DrawView.DrawItem> newSelectedDrawItems = new ArrayList<>();

        for (int i = (mTempColumn - 1); i >= 0; i--) {
            for (int j = 0; j < mTempRow; j++) {
                int index = j * mTempColumn + i;
                DrawView.DrawItem item = mTempData.get(index);
                newSelectedDrawItems.add(item);
            }
        }

        for (int i = 0; i < newSelectedDrawItems.size(); i++) {
            newSelectedDrawItems.get(i).color = input.get(i).color;
        }

        return mTempData;
    }

    private static byte[] rotate(int degree, byte[] input, int textSize) {
        byte[] output = null;
        if (degree == 0 || degree == 360) {
            output = input;
        } else if (degree == 90) {
            output = rotate90Degree(input, textSize);
        } else if (degree == 180) {
            output = rotate90Degree(rotate90Degree(input, textSize), textSize);
        } else if (degree == 270) {
            output = rotate90Degree(rotate90Degree(rotate90Degree(input, textSize), textSize), textSize);
        }
        return output;
    }

    private static byte[] rotate90Degree(byte[] input, int textSize) {
        int column = textSize;
        int row = textSize;
        List<String> showData = new ArrayList<>();
        showData.addAll(byte2hex(input));

        List<String> result = new ArrayList<>();
        for (int i = 0; i < column * row; i++) {
            result.add(String.valueOf(false));
        }

        for (int i = 0; i < showData.size(); i++) {
            int showValue = Integer.parseInt(showData.get(i), 16);
            int m = (row / 8 + ((row % 8) > 0 ? 1 : 0));
            int newColumn = i / m;
            int newRow = i % m;
            if (newRow < (m - 1)) {
                for (int k = 0; k < 8; k++) {
                    int index = (newRow * 8 + k) * column + newColumn;
                    int value = (showValue << k) & 0x80;
                    result.set(index, String.valueOf(value == 0x80 ? true : false));
                }
            } else {
                int temp;
                if (row % 8 == 0) {
                    temp = 8;
                } else {
                    temp = row % 8;
                }
                for (int k = 0; k < temp; k++) {
                    int index = (newRow * 8 + k) * column + newColumn;
                    int value = (showValue << k) & 0x80;
                    result.set(index, String.valueOf(value == 0x80 ? true : false));
                }
            }
        }

        String[][] result0Degrees = new String[column][row];
        for (int i = 0; i < result0Degrees.length; i++) {
            for (int j = 0; j < result0Degrees[i].length; j++) {
                result0Degrees[i][j] = result.get(i * column + j);
            }
        }
        String[][] resultsRotate90 = rotate(result0Degrees);
        List<String> listStringRotate90 = new ArrayList<>();
        for (int i = 0; i < resultsRotate90.length; i++) {
            for (int j = 0; j < resultsRotate90[i].length; j++) {
                listStringRotate90.add(result0Degrees[i][j]);
            }
        }

        List<String> listHexStringRotate90 = new ArrayList<>();
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
                    boolean flag = Boolean.valueOf(listStringRotate90.get(index));
                    int temp = flag ? 1 : 0;
                    int tempValue = (int) (temp * Math.pow(2, 7 - k));
                    resultInt += tempValue;
                }
                listHexStringRotate90.add(getHexStringForInt(resultInt));
            }
        }

        byte[] finalResult = null;
        for (String str : listHexStringRotate90) {
            byte[] tempData = HexUtil.hexStringToBytes(str);
            if (null == finalResult) {
                finalResult = tempData;
            } else {
                finalResult = concat(finalResult, tempData);
            }
        }

        return finalResult;
    }

    /***
     * 对某个文字得到的字节 旋转90度
     * @param input
     * @return
     */
    private static byte[] rotate90Degree(byte[] input) {
        int column = 16;
        int row = 16;
        List<String> showData = new ArrayList<>();
        showData.addAll(byte2hex(input));

        List<String> result = new ArrayList<>();
        for (int i = 0; i < column * row; i++) {
            result.add(String.valueOf(false));
        }

        for (int i = 0; i < showData.size(); i++) {
            int showValue = Integer.parseInt(showData.get(i), 16);
            int m = (row / 8 + ((row % 8) > 0 ? 1 : 0));
            int newColumn = i / m;
            int newRow = i % m;
            if (newRow < (m - 1)) {
                for (int k = 0; k < 8; k++) {
                    int index = (newRow * 8 + k) * column + newColumn;
                    int value = (showValue << k) & 0x80;
                    result.set(index, String.valueOf(value == 0x80 ? true : false));
                }
            } else {
                int temp;
                if (row % 8 == 0) {
                    temp = 8;
                } else {
                    temp = row % 8;
                }
                for (int k = 0; k < temp; k++) {
                    int index = (newRow * 8 + k) * column + newColumn;
                    int value = (showValue << k) & 0x80;
                    result.set(index, String.valueOf(value == 0x80 ? true : false));
                }
            }
        }

        String[][] result0Degrees = new String[column][row];
        for (int i = 0; i < result0Degrees.length; i++) {
            for (int j = 0; j < result0Degrees[i].length; j++) {
                result0Degrees[i][j] = result.get(i * column + j);
            }
        }
        String[][] resultsRotate90 = rotate(result0Degrees);
        List<String> listStringRotate90 = new ArrayList<>();
        for (int i = 0; i < resultsRotate90.length; i++) {
            for (int j = 0; j < resultsRotate90[i].length; j++) {
                listStringRotate90.add(result0Degrees[i][j]);
            }
        }

        List<String> listHexStringRotate90 = new ArrayList<>();
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
                    boolean flag = Boolean.valueOf(listStringRotate90.get(index));
                    int temp = flag ? 1 : 0;
                    int tempValue = (int) (temp * Math.pow(2, 7 - k));
                    resultInt += tempValue;
                }
                listHexStringRotate90.add(getHexStringForInt(resultInt));
            }
        }

        byte[] finalResult = null;
        for (String str : listHexStringRotate90) {
            byte[] tempData = HexUtil.hexStringToBytes(str);
            if (null == finalResult) {
                finalResult = tempData;
            } else {
                finalResult = concat(finalResult, tempData);
            }
        }

        return finalResult;
    }

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static String getHexStringForInt(int dir) {
        String resultStr = Integer.toHexString(dir);
        if (resultStr.length() == 1) {
            resultStr = "0" + resultStr;
        } else if (resultStr.length() == 2) {
        }
        return resultStr;
    }

    /***
     * 给表情数据做下处理，表情的数据在最右边增加一列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForEmoji(byte[] data) {
        byte[] tempData = new byte[2];
        tempData[0] = 0;
        tempData[1] = 0;
        data = concat(data, tempData);
        return data;
    }

    /***
     * 右边增加空列
     * @param data
     * @return
     */
    public static List<String> addEmptyColumnForImageDataToTheLeft1(List<String> data, int showRow) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < showRow; i++) {
            result.addAll(TextEmojiManagerCoolLEDUX.getColorDataWithColorWithRGB444Transfer(Color.BLACK));
        }
        if (data.size() > 0) {
            result.addAll(data);
        }
        return result;
    }

    public static List<String> addEmptyColumnForImageDataToTheLeft1(List<String> data, int spacingNumber, int showRow) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < spacingNumber; i++) {
            result = addEmptyColumnForImageDataToTheLeft1(result, showRow);
        }
        result.addAll(data);
        return result;
    }

    /***
     * 右边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData8ToTheRight(byte[] data) {
        byte[] tempData = new byte[1];
        tempData[0] = 0;
        data = concat(data, tempData);
        return data;
    }

    public static byte[] addEmptyColumnForData8ToTheRight(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[1];
        tempData[0] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(data, tempData);
        }
        return data;
    }

    /***
     * 左边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData8ToTheLeft(byte[] data) {
        byte[] tempData = new byte[1];
        tempData[0] = 0;
        data = concat(tempData, data);
        return data;

    }

    public static byte[] addEmptyColumnForData8ToTheLeft(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[1];
        tempData[0] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(tempData, data);
        }
        return data;
    }

    /***
     * 右边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData16ToTheRight(byte[] data) {
        byte[] tempData = new byte[2];
        tempData[0] = 0;
        tempData[1] = 0;
        data = concat(data, tempData);
        return data;
    }

    public static byte[] addEmptyColumnForData16ToTheRight(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[2];
        tempData[0] = 0;
        tempData[1] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(data, tempData);
        }
        return data;
    }

    /***
     * 左边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData16ToTheLeft(byte[] data) {
        byte[] tempData = new byte[2];
        tempData[0] = 0;
        tempData[1] = 0;
        data = concat(tempData, data);
        return data;

    }

    public static byte[] addEmptyColumnForData16ToTheLeft(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[2];
        tempData[0] = 0;
        tempData[1] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(tempData, data);
        }
        return data;
    }

    /***
     * 左边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData24ToTheLeft(byte[] data) {
        byte[] tempData = new byte[3];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        data = concat(tempData, data);
        return data;
    }

    public static byte[] addEmptyColumnForData24ToTheLeft(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[3];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(tempData, data);
        }
        return data;
    }

    /***
     * 右边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData24ToTheRight(byte[] data) {
        byte[] tempData = new byte[3];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        data = concat(data, tempData);
        return data;
    }

    public static byte[] addEmptyColumnForData24ToTheRight(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[3];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(data, tempData);
        }
        return data;
    }

    /***
     * 右边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData32ToTheRight(byte[] data) {
        byte[] tempData = new byte[4];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        data = concat(data, tempData);
        return data;
    }

    public static byte[] addEmptyColumnForData32ToTheRight(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[4];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(data, tempData);
        }
        return data;
    }


    /***
     * 左边增加空列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForData32ToTheLeft(byte[] data) {
        byte[] tempData = new byte[4];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        data = concat(tempData, data);
        return data;
    }

    public static byte[] addEmptyColumnForData32ToTheLeft(byte[] data, int spacingNumber) {
        byte[] tempData = new byte[4];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        for (int i = 0; i < spacingNumber; i++) {
            data = concat(tempData, data);
        }
        return data;
    }

    /***
     * 右边增加空列
     * @param data
     * @return
     */
    public static byte[] addTwoEmptyColumnForData32ToTheRight(byte[] data) {
        byte[] tempData = new byte[8];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        tempData[4] = 0;
        tempData[5] = 0;
        tempData[6] = 0;
        tempData[7] = 0;
        data = concat(data, tempData);
        return data;
    }

    /***
     * 右边增加空列
     * @param data
     * @return
     */
    public static byte[] addTwoEmptyColumnForData24ToTheRight(byte[] data) {
        byte[] tempData = new byte[6];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        tempData[4] = 0;
        tempData[5] = 0;
        data = concat(data, tempData);
        return data;
    }

    /***
     * 左边增加空列
     * @param data
     * @return
     */
    public static byte[] addTwoEmptyColumnForData32ToTheLeft(byte[] data) {
        byte[] tempData = new byte[8];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        tempData[4] = 0;
        tempData[5] = 0;
        tempData[6] = 0;
        tempData[7] = 0;
        data = concat(tempData, data);
        return data;
    }

    /***
     * 左边增加空列
     * @param data
     * @return
     */
    public static byte[] addTwoEmptyColumnForData24ToTheLeft(byte[] data) {
        byte[] tempData = new byte[6];
        tempData[0] = 0;
        tempData[1] = 0;
        tempData[2] = 0;
        tempData[3] = 0;
        tempData[4] = 0;
        tempData[5] = 0;
        data = concat(tempData, data);
        return data;
    }

    public static List<DrawView.DrawItem> addEmptyColumnForDrawItemsToTheLeft(List<DrawView.DrawItem> data, int showRow, int spacingNumber) {
        List<DrawView.DrawItem> result = new ArrayList<>();
        for (int i = 0; i < spacingNumber; i++) {
            for (int j = 0; j < showRow; j++) {
                result.add(new DrawView.DrawItem());
            }
        }
        result.addAll(data);
        return result;
    }

    public static List<DrawView.DrawItem> addEmptyColumnForDrawItemsToTheRight(List<DrawView.DrawItem> data, int showRow, int spacingNumber) {
        List<DrawView.DrawItem> result = new ArrayList<>();
        result.addAll(data);
        for (int i = 0; i < spacingNumber; i++) {
            for (int j = 0; j < showRow; j++) {
                result.add(new DrawView.DrawItem());
            }
        }
        return result;
    }

    public static List<String> addEmptyColumnForImageDataToTheRight(List<String> data, int showRow, int spacingNumber) {
        List<String> result = new ArrayList<>();
        result.addAll(data);
        for (int i = 0; i < spacingNumber; i++) {
            for (int j = 0; j < showRow; j++) {
                result.addAll(TextEmojiManagerCoolLEDUX.getColorDataWithColorWithRGB444Transfer(0));
            }
        }
        return result;
    }

    public static List<String> addEmptyColumnForImageDataToTheLeft(List<String> data, int showRow, int spacingNumber) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < spacingNumber; i++) {
            for (int j = 0; j < showRow; j++) {
                result.addAll(TextEmojiManagerCoolLEDUX.getColorDataWithColorWithRGB444Transfer(0));
            }
        }
        result.addAll(data);
        return result;
    }

    public static byte[] transfer24FontTo32(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 3) == 2) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 4); i += 4) {
            byte tempValue1 = (byte) ((0x0f & data[i]) << 4);
            byte tempValue2 = (byte) ((0x0f & data[i + 1]) << 4);
            byte tempValue3 = (byte) ((0x0f & data[i + 2]) << 4);
            data[i] = (byte) ((data[i] & 0xff) >>> 4);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 4);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 4);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
            data[i + 3] = (byte) ((data[i + 3] & 0xff) >>> 4);
            data[i + 3] = (byte) (data[i + 3] | tempValue3);
        }


        byte[] newTempData = new byte[4];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        newTempData[3] = 0;
        for (int i = 0; i < 4; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 4; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer20FontTo32(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 3) == 2) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 4); i += 4) {
            byte tempValue1 = (byte) ((0x3f & data[i]) << 2);
            byte tempValue2 = (byte) ((0x3f & data[i + 1]) << 2);
            byte tempValue3 = (byte) ((0x3f & data[i + 2]) << 2);
            data[i] = (byte) ((data[i] & 0xff) >>> 6);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 6);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 6);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
            data[i + 3] = (byte) ((data[i + 3] & 0xff) >>> 6);
            data[i + 3] = (byte) (data[i + 3] | tempValue3);
        }


        byte[] newTempData = new byte[4];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        newTempData[3] = 0;
        for (int i = 0; i < 6; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 6; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer16FontTo32(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[2];
        emptyData[0] = 0;
        emptyData[1] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 4); i += 4) {
            byte tempValue1 = data[i];
            byte tempValue2 = data[i + 1];
            data[i] = 0;
            data[i + 1] = tempValue1;
            data[i + 2] = tempValue2;
            data[i + 3] = 0;
        }


        byte[] newTempData = new byte[4];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        newTempData[3] = 0;
        for (int i = 0; i < 8; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 8; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer14FontTo32(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[2];
        emptyData[0] = 0;
        emptyData[1] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData2 = new byte[1];
                tempData2[0] = input[i];
                if (null == data) {
                    data = tempData2;
                } else {
                    data = concat(data, tempData2);
                }
            }
        }

        for (int i = 0; i <= (data.length - 4); i += 4) {
            byte tempValue1 = data[i];
            byte tempValue2 = data[i + 1];
            data[i] = 0;
            data[i + 1] = tempValue1;
            data[i + 2] = tempValue2;
            data[i + 3] = 0;
        }

        for (int i = 0; i <= (data.length - 4); i += 4) {
            byte tempValue = (byte) ((0x01 & data[i + 1]) << 7);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 1);
            data[i + 2] = (byte) (data[i + 2] | tempValue);
        }

        byte[] newTempData = new byte[4];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        newTempData[3] = 0;
        for (int i = 0; i < 9; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 9; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer12FontTo32(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[2];
        emptyData[0] = 0;
        emptyData[1] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 4); i += 4) {
            byte tempValue1 = data[i];
            byte tempValue2 = data[i + 1];
            data[i] = 0;
            data[i + 1] = tempValue1;
            data[i + 2] = tempValue2;
            data[i + 3] = 0;
        }

        for (int i = 0; i <= (data.length - 4); i += 4) {
            byte tempValue = (byte) ((0x03 & data[i + 1]) << 6);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 2);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 2);
            data[i + 2] = (byte) (data[i + 2] | tempValue);
        }


        byte[] newTempData = new byte[4];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        newTempData[3] = 0;
        for (int i = 0; i < 10; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 10; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer20FontTo24(byte[] input) {
        byte[] data = input;

        for (int i = 0; i <= (data.length - 3); i += 3) {
            byte tempValue1 = (byte) ((0x0f & data[i]) << 6);
            byte tempValue2 = (byte) ((0x0f & data[i + 1]) << 6);
            data[i] = (byte) ((data[i] & 0xff) >>> 2);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 2);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 2);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
        }


        byte[] newTempData = new byte[3];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        for (int i = 0; i < 2; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 2; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer16FontTo24(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 3); i += 3) {
            byte tempValue1 = (byte) ((0x0f & data[i]) << 4);
            byte tempValue2 = (byte) ((0x0f & data[i + 1]) << 4);
            data[i] = (byte) ((data[i] & 0xff) >>> 4);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 4);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 4);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
        }


        byte[] newTempData = new byte[3];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        for (int i = 0; i < 4; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 4; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer14FontTo24(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 3); i += 3) {
            byte tempValue1 = (byte) ((0x1f & data[i]) << 3);
            byte tempValue2 = (byte) ((0x1f & data[i + 1]) << 3);
            data[i] = (byte) ((data[i] & 0xff) >>> 5);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 5);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 5);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
        }


        byte[] newTempData = new byte[3];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        for (int i = 0; i < 5; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 5; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer12FontTo24(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 3); i += 3) {
            byte tempValue1 = (byte) ((0x3f & data[i]) << 2);
            byte tempValue2 = (byte) ((0x3f & data[i + 1]) << 2);
            data[i] = (byte) ((data[i] & 0xff) >>> 6);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 6);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 6);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
        }


        byte[] newTempData = new byte[3];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        for (int i = 0; i < 6; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 6; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer16FontTo20(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 3); i += 3) {
            byte tempValue1 = (byte) ((0x0f & data[i]) << 6);
            byte tempValue2 = (byte) ((0x0f & data[i + 1]) << 6);
            data[i] = (byte) ((data[i] & 0xff) >>> 2);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 2);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 2);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
        }


        byte[] newTempData = new byte[3];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        for (int i = 0; i < 2; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 2; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer14FontTo20(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 3); i += 3) {
            byte tempValue1 = (byte) ((0x1f & data[i]) << 5);
            byte tempValue2 = (byte) ((0x1f & data[i + 1]) << 5);
            data[i] = (byte) ((data[i] & 0xff) >>> 3);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 3);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 3);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
        }


        byte[] newTempData = new byte[3];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        for (int i = 0; i < 3; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 3; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer12FontTo20(byte[] input) {
        byte[] data = null;

        byte[] emptyData = new byte[1];
        emptyData[0] = 0;

        for (int i = 0; i < input.length; i++) {
            if ((i % 2) == 1) {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                data = concat(data, tempData);

                data = concat(data, emptyData);
            } else {
                byte[] tempData = new byte[1];
                tempData[0] = input[i];
                if (null == data) {
                    data = tempData;
                } else {
                    data = concat(data, tempData);
                }
            }
        }

        for (int i = 0; i <= (data.length - 3); i += 3) {
            byte tempValue1 = (byte) ((0x3f & data[i]) << 4);
            byte tempValue2 = (byte) ((0x3f & data[i + 1]) << 4);
            data[i] = (byte) ((data[i] & 0xff) >>> 4);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 4);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
            data[i + 2] = (byte) ((data[i + 2] & 0xff) >>> 4);
            data[i + 2] = (byte) (data[i + 2] | tempValue2);
        }


        byte[] newTempData = new byte[3];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        for (int i = 0; i < 4; i++) {
            data = concat(newTempData, data);
        }
        for (int i = 0; i < 4; i++) {
            data = concat(data, newTempData);
        }
        return data;
    }

    public static byte[] transfer14FontTo16(byte[] input) {
        byte[] data = input;

        for (int i = 0; i <= (data.length - 2); i += 2) {
            byte tempValue1 = (byte) ((0x01 & data[i]) << 7);
            data[i] = (byte) ((data[i] & 0xff) >>> 1);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 1);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
        }

        byte[] newTempData = new byte[2];
        newTempData[0] = 0;
        newTempData[1] = 0;
        data = concat(newTempData, data);
        data = concat(data, newTempData);
        return data;
    }

    public static byte[] transfer12FontTo16(byte[] input) {
        byte[] data = input;

        for (int i = 0; i <= (data.length - 2); i += 2) {
            byte tempValue1 = (byte) ((0x03 & data[i]) << 6);
            data[i] = (byte) ((data[i] & 0xff) >>> 2);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 2);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
        }

        byte[] newTempData = new byte[4];
        newTempData[0] = 0;
        newTempData[1] = 0;
        newTempData[2] = 0;
        newTempData[3] = 0;
        data = concat(newTempData, data);
        data = concat(data, newTempData);
        return data;
    }

    public static byte[] transfer12FontTo14(byte[] input) {
        byte[] data = input;

        for (int i = 0; i <= (data.length - 2); i += 2) {
            byte tempValue1 = (byte) ((0x01 & data[i]) << 7);
            data[i] = (byte) ((data[i] & 0xff) >>> 1);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 1);
            data[i + 1] = (byte) (data[i + 1] | tempValue1);
        }

        byte[] newTempData = new byte[2];
        newTempData[0] = 0;
        newTempData[1] = 0;
        data = concat(newTempData, data);
        data = concat(data, newTempData);
        return data;
    }


    /***
     * 给表情数据做下处理，表情的数据在最右边增加一列
     * @param data
     * @return
     */
    public static byte[] addEmptyColumnForEmojiFor1632A(byte[] data) {
        byte[] tempData = new byte[16];
        for (int i = 0; i < tempData.length; i++) {
            tempData[i] = 0;
        }
        data = concat(data, tempData);
        return data;
    }

    private static byte[] deleteEmptyColumnFor8(byte[] data) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 1;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 1; i += 1) {
            if (data[i] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < 4; i++) {
                byte[] temp = new byte[1];
                temp[0] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 1; i += 1) {
                if (data[i] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 1; i >= 0; i -= 1) {
                if (data[i] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 1) {
                    byte[] temp = new byte[1];
                    temp[0] = data[i];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[1];
                temp[0] = data[indexFromLeft];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < 4; i++) {
                    byte[] temp = new byte[1];
                    temp[0] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    private static byte[] deleteEmptyColumnFor8(byte[] data, int textSize) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 1;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 1; i += 1) {
            if (data[i] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < (textSize / 2); i++) {
                byte[] temp = new byte[1];
                temp[0] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 1; i += 1) {
                if (data[i] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 1; i >= 0; i -= 1) {
                if (data[i] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 1) {
                    byte[] temp = new byte[1];
                    temp[0] = data[i];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[1];
                temp[0] = data[indexFromLeft];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < (textSize / 2); i++) {
                    byte[] temp = new byte[1];
                    temp[0] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    /***
     * 去处左右两边的空列
     * @param data
     * @return
     */
    public static byte[] deleteEmptyColumnFor12(byte[] data) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 2;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 2; i += 2) {
            if (data[i] != 0 || data[i + 1] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < 6; i++) {
                byte[] temp = new byte[2];
                temp[0] = 0;
                temp[1] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 2; i += 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 2; i >= 0; i -= 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 2) {
                    byte[] temp = new byte[2];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[2];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < 6; i++) {
                    byte[] temp = new byte[2];
                    temp[0] = 0;
                    temp[1] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    public static byte[] deleteEmptyColumnFor12(byte[] data, int textSize) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 2;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 2; i += 2) {
            if (data[i] != 0 || data[i + 1] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < (textSize / 2); i++) {
                byte[] temp = new byte[2];
                temp[0] = 0;
                temp[1] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 2; i += 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 2; i >= 0; i -= 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 2) {
                    byte[] temp = new byte[2];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[2];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < (textSize / 2); i++) {
                    byte[] temp = new byte[2];
                    temp[0] = 0;
                    temp[1] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    /***
     * 去处左右两边的空列
     * @param data
     * @return
     */
    private static byte[] deleteEmptyColumnFor14(byte[] data) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 2;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 2; i += 2) {
            if (data[i] != 0 || data[i + 1] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < 7; i++) {
                byte[] temp = new byte[2];
                temp[0] = 0;
                temp[1] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 2; i += 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 2; i >= 0; i -= 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 2) {
                    byte[] temp = new byte[2];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[2];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < 7; i++) {
                    byte[] temp = new byte[2];
                    temp[0] = 0;
                    temp[1] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    private static byte[] deleteEmptyColumnFor14(byte[] data, int textSize) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 2;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 2; i += 2) {
            if (data[i] != 0 || data[i + 1] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < (textSize / 2); i++) {
                byte[] temp = new byte[2];
                temp[0] = 0;
                temp[1] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 2; i += 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 2; i >= 0; i -= 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 2) {
                    byte[] temp = new byte[2];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[2];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < (textSize / 2); i++) {
                    byte[] temp = new byte[2];
                    temp[0] = 0;
                    temp[1] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    /***
     * 去处左右两边的空列
     * @param data
     * @return
     */
    private static byte[] deleteEmptyColumnFor16(byte[] data) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 2;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 2; i += 2) {
            if (data[i] != 0 || data[i + 1] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < 8; i++) {
                byte[] temp = new byte[2];
                temp[0] = 0;
                temp[1] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 2; i += 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 2; i >= 0; i -= 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 2) {
                    byte[] temp = new byte[2];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[2];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < 8; i++) {
                    byte[] temp = new byte[2];
                    temp[0] = 0;
                    temp[1] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    private static byte[] deleteEmptyColumnFor16(byte[] data, int textSize) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 2;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 2; i += 2) {
            if (data[i] != 0 || data[i + 1] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < (textSize / 2); i++) {
                byte[] temp = new byte[2];
                temp[0] = 0;
                temp[1] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 2; i += 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 2; i >= 0; i -= 2) {
                if (data[i] != 0 || data[i + 1] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 2) {
                    byte[] temp = new byte[2];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[2];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < (textSize / 2); i++) {
                    byte[] temp = new byte[2];
                    temp[0] = 0;
                    temp[1] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    /***
     * 去处左右两边的空列
     * @param data
     * @return
     */
    private static byte[] deleteEmptyColumnFor20(byte[] data) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 3;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 3; i += 3) {
            if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < 10; i++) {
                byte[] temp = new byte[3];
                temp[0] = 0;
                temp[1] = 0;
                temp[2] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 3; i += 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 3; i >= 0; i -= 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 3) {
                    byte[] temp = new byte[3];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    temp[2] = data[i + 2];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[3];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                temp[2] = data[indexFromLeft + 2];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < 10; i++) {
                    byte[] temp = new byte[3];
                    temp[0] = 0;
                    temp[1] = 0;
                    temp[2] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    private static byte[] deleteEmptyColumnFor20(byte[] data, int textSize) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 3;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 3; i += 3) {
            if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < (textSize / 2); i++) {
                byte[] temp = new byte[3];
                temp[0] = 0;
                temp[1] = 0;
                temp[2] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 3; i += 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 3; i >= 0; i -= 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 3) {
                    byte[] temp = new byte[3];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    temp[2] = data[i + 2];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[3];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                temp[2] = data[indexFromLeft + 2];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < (textSize / 2); i++) {
                    byte[] temp = new byte[3];
                    temp[0] = 0;
                    temp[1] = 0;
                    temp[2] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    /***
     * 去处左右两边的空列
     * @param data
     * @return
     */
    private static byte[] deleteEmptyColumnFor24(byte[] data) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 3;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 3; i += 3) {
            if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < 12; i++) {
                byte[] temp = new byte[3];
                temp[0] = 0;
                temp[1] = 0;
                temp[2] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 3; i += 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 3; i >= 0; i -= 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 3) {
                    byte[] temp = new byte[3];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    temp[2] = data[i + 2];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[3];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                temp[2] = data[indexFromLeft + 2];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < 12; i++) {
                    byte[] temp = new byte[3];
                    temp[0] = 0;
                    temp[1] = 0;
                    temp[2] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    private static byte[] deleteEmptyColumnFor24(byte[] data, int textSize) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 3;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 3; i += 3) {
            if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < (textSize / 2); i++) {
                byte[] temp = new byte[3];
                temp[0] = 0;
                temp[1] = 0;
                temp[2] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 3; i += 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 3; i >= 0; i -= 3) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 3) {
                    byte[] temp = new byte[3];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    temp[2] = data[i + 2];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[3];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                temp[2] = data[indexFromLeft + 2];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < (textSize / 2); i++) {
                    byte[] temp = new byte[3];
                    temp[0] = 0;
                    temp[1] = 0;
                    temp[2] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    private static List<String> deleteEmptyColumnForImageData(List<String> input, int showRow) {
        List<String> output = new ArrayList<>();
        return output;
    }

    /***
     * 去处左右两边的空列
     * @param data
     * @return
     */
    private static byte[] deleteEmptyColumnFor32(byte[] data) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 4;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 4; i += 4) {
            if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0 || data[i + 3] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < 16; i++) {
                byte[] temp = new byte[4];
                temp[0] = 0;
                temp[1] = 0;
                temp[2] = 0;
                temp[3] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 4; i += 4) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0 || data[i + 3] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 4; i >= 0; i -= 4) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0 || data[i + 3] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 4) {
                    byte[] temp = new byte[4];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    temp[2] = data[i + 2];
                    temp[3] = data[i + 3];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[4];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                temp[2] = data[indexFromLeft + 2];
                temp[3] = data[indexFromLeft + 3];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < 16; i++) {
                    byte[] temp = new byte[4];
                    temp[0] = 0;
                    temp[1] = 0;
                    temp[2] = 0;
                    temp[3] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }

    private static byte[] deleteEmptyColumnFor32(byte[] data, int textSize) {
        byte[] result = null;
        int indexFromLeft = 0;
        int indexFromRight = data.length - 4;

        boolean isAllEmpty = true;

        for (int i = 0; i <= data.length - 4; i += 4) {
            if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0 || data[i + 3] != 0) {
                isAllEmpty = false;
                break;
            }
        }

        if (isAllEmpty) {
            for (int i = 0; i < (textSize / 2); i++) {
                byte[] temp = new byte[4];
                temp[0] = 0;
                temp[1] = 0;
                temp[2] = 0;
                temp[3] = 0;
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            }
        } else {
            for (int i = 0; i <= data.length - 4; i += 4) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0 || data[i + 3] != 0) {
                    indexFromLeft = i;
                    break;
                }
            }
            for (int i = data.length - 4; i >= 0; i -= 4) {
                if (data[i] != 0 || data[i + 1] != 0 || data[i + 2] != 0 || data[i + 3] != 0) {
                    indexFromRight = i;
                    break;
                }
            }

            if (indexFromLeft < indexFromRight) {
                for (int i = indexFromLeft; i <= indexFromRight; i += 4) {
                    byte[] temp = new byte[4];
                    temp[0] = data[i];
                    temp[1] = data[i + 1];
                    temp[2] = data[i + 2];
                    temp[3] = data[i + 3];
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            } else if (indexFromLeft == indexFromRight) {
                byte[] temp = new byte[4];
                temp[0] = data[indexFromLeft];
                temp[1] = data[indexFromLeft + 1];
                temp[2] = data[indexFromLeft + 2];
                temp[3] = data[indexFromLeft + 3];
                if (null == result) {
                    result = temp;
                } else {
                    result = concat(result, temp);
                }
            } else if (indexFromLeft > indexFromRight) {
                for (int i = 0; i < (textSize / 2); i++) {
                    byte[] temp = new byte[4];
                    temp[0] = 0;
                    temp[1] = 0;
                    temp[2] = 0;
                    temp[3] = 0;
                    if (null == result) {
                        result = temp;
                    } else {
                        result = concat(result, temp);
                    }
                }
            }
        }

        if (null == result) {
            return data;
        } else {
            return result;
        }
    }


    public static byte[] addStaticDataForCoolledux32(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result = new byte[0];
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 4;
            int tempCurrentColumn = (currentLength / 4) % column;
            if (tempCurrentColumn == 0) {
                result = inputData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData32ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData32ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }
        return result;
    }

    public static byte[] addStaticDataForCoolledux24(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result = new byte[0];
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 3;
            int tempCurrentColumn = (currentLength / 3) % column;
            if (tempCurrentColumn == 0) {
                result = inputData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData24ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData24ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }
        return result;
    }

    public static byte[] addStaticDataForCoolledux16(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result = new byte[0];
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = inputData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }
        return result;
    }


    public static byte[] addStaticDataForCoolledux14(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result = new byte[0];
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = inputData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }
        return result;
    }

    public static byte[] addStaticDataForCoolledux12(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result = new byte[0];
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = inputData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }
        return result;
    }

    public static byte[] addStaticDataForCoolledux8(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result = new byte[0];
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength;
            int tempCurrentColumn = (currentLength) % column;
            if (tempCurrentColumn == 0) {
                result = inputData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData8ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData8ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }
        return result;
    }

    public static List<String> addStaticDataForCoolledux12(List<String> imageData, byte[] currentData, byte[] inputData, int column, int spacingNumber, int showRow) {
        List<String> result = new ArrayList<>();
        if (null == currentData) {
            result = imageData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = imageData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, currentRestColumn);
                } else {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, spacingNumber);
                }
            }
        }
        return result;
    }

    public static List<String> addStaticDataForCoolledux14(List<String> imageData, byte[] currentData, byte[] inputData, int column, int spacingNumber, int showRow) {
        List<String> result = new ArrayList<>();
        if (null == currentData) {
            result = imageData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = imageData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, currentRestColumn);
                } else {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, spacingNumber);
                }
            }
        }
        return result;
    }

    public static List<String> addStaticDataForCoolledux16(List<String> imageData, byte[] currentData, byte[] inputData, int column, int spacingNumber, int showRow) {
        List<String> result = new ArrayList<>();
        if (null == currentData) {
            result = imageData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = imageData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, currentRestColumn);
                } else {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, spacingNumber);
                }
            }
        }
        return result;
    }

    public static List<String> addStaticDataForCoolledux24(List<String> imageData, byte[] currentData, byte[] inputData, int column, int spacingNumber, int showRow) {
        List<String> result = new ArrayList<>();
        if (null == currentData) {
            result = imageData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 3;
            int tempCurrentColumn = (currentLength / 3) % column;
            if (tempCurrentColumn == 0) {
                result = imageData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, currentRestColumn);
                } else {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, spacingNumber);
                }
            }
        }
        return result;
    }

    public static List<String> addStaticDataForCoolledux32(List<String> imageData, byte[] currentData, byte[] inputData, int column, int spacingNumber, int showRow) {
        List<String> result = new ArrayList<>();
        if (null == currentData) {
            result = imageData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 4;
            int tempCurrentColumn = (currentLength / 4) % column;
            if (tempCurrentColumn == 0) {
                result = imageData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, currentRestColumn);
                } else {
                    result = addEmptyColumnForImageDataToTheLeft(imageData, showRow, spacingNumber);
                }
            }
        }
        return result;
    }

    /***
     * 处理自动分段
     * @param currentData
     * @param inputData
     * @param column
     * @return
     */
    public static byte[] checkSegment16(byte[] currentData, byte[] inputData, int column) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + 1)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result);
                    result = concat(result, inputData);

                }
            }
        }

        return result;
    }

    public static byte[] checkSegment16(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);

                }
            }
        }

        return result;
    }

    public static byte[] checkSegment16(byte[] currentData, byte[] inputData, int column, int spacingNumber,
                                        TextEmojiManager.TextEmoji32Item textEmoji32Item,
                                        List<List<TextEmojiManager.TextEmoji32Item>> notLeftRightLists,
                                        List<List<byte[]>> staticData, int byteNumber) {
        byte[] result;
        List<TextEmojiManager.TextEmoji32Item> tempList;
        List<byte[]> tempStaticData;
        if (null == currentData) {
            result = inputData;
            int newIndex = 0;
            notLeftRightLists.add(new ArrayList<>());
            staticData.add(new ArrayList<>());
            tempList = notLeftRightLists.get(newIndex);
            tempStaticData = staticData.get(newIndex);

            tempList.add(textEmoji32Item);
            tempStaticData.add(mirror(inputData, byteNumber));

        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
                int size = notLeftRightLists.size();
                int newIndex = size;
                notLeftRightLists.add(new ArrayList<>());
                staticData.add(new ArrayList<>());
                tempList = notLeftRightLists.get(newIndex);
                tempStaticData = staticData.get(newIndex);
                tempList.add(textEmoji32Item);
                tempStaticData.add(mirror(inputData, byteNumber));
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);

                    int size = notLeftRightLists.size();
                    int newIndex = size;
                    notLeftRightLists.add(new ArrayList<>());
                    staticData.add(new ArrayList<>());
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                    int size = notLeftRightLists.size();
                    int newIndex = size - 1;
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                }
            }
        }

        return result;
    }

    /***
     * 处理自动分段
     * @param currentData
     * @param inputData
     * @param column
     * @return
     */
    public static byte[] checkSegment14(byte[] currentData, byte[] inputData, int column) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + 1)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result);
                    result = concat(result, inputData);

                }
            }
        }

        return result;
    }

    public static byte[] checkSegment8(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength;
            int tempCurrentColumn = (currentLength) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData8ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData8ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);

                }
            }
        }

        return result;
    }

    public static byte[] checkSegment14(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);

                }
            }
        }

        return result;
    }

    public static byte[] checkSegment14(byte[] currentData, byte[] inputData, int column, int spacingNumber,
                                        TextEmojiManager.TextEmoji32Item textEmoji32Item,
                                        List<List<TextEmojiManager.TextEmoji32Item>> notLeftRightLists,
                                        List<List<byte[]>> staticData, int byteNumber) {
        byte[] result;
        List<TextEmojiManager.TextEmoji32Item> tempList;
        List<byte[]> tempStaticData;
        if (null == currentData) {
            result = inputData;
            int newIndex = 0;
            notLeftRightLists.add(new ArrayList<>());
            staticData.add(new ArrayList<>());
            tempList = notLeftRightLists.get(newIndex);
            tempStaticData = staticData.get(newIndex);

            tempList.add(textEmoji32Item);
            tempStaticData.add(mirror(inputData, byteNumber));

        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
                int size = notLeftRightLists.size();
                int newIndex = size;
                notLeftRightLists.add(new ArrayList<>());
                staticData.add(new ArrayList<>());
                tempList = notLeftRightLists.get(newIndex);
                tempStaticData = staticData.get(newIndex);
                tempList.add(textEmoji32Item);
                tempStaticData.add(mirror(inputData, byteNumber));
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);

                    int size = notLeftRightLists.size();
                    int newIndex = size;
                    notLeftRightLists.add(new ArrayList<>());
                    staticData.add(new ArrayList<>());
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                    int size = notLeftRightLists.size();
                    int newIndex = size - 1;
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                }
            }
        }

        return result;
    }

    public static byte[] checkSegment12(byte[] currentData, byte[] inputData, int column) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + 1)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result);
                    result = concat(result, inputData);

                }
            }
        }

        return result;
    }

    public static byte[] checkSegment12(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);

                }
            }
        }

        return result;
    }

    public static byte[] checkSegment12(byte[] currentData, byte[] inputData, int column, int spacingNumber,
                                        TextEmojiManager.TextEmoji32Item textEmoji32Item,
                                        List<List<TextEmojiManager.TextEmoji32Item>> notLeftRightLists,
                                        List<List<byte[]>> staticData, int byteNumber) {
        byte[] result;
        List<TextEmojiManager.TextEmoji32Item> tempList;
        List<byte[]> tempStaticData;
        if (null == currentData) {
            result = inputData;
            int newIndex = 0;
            notLeftRightLists.add(new ArrayList<>());
            staticData.add(new ArrayList<>());
            tempList = notLeftRightLists.get(newIndex);
            tempStaticData = staticData.get(newIndex);

            tempList.add(textEmoji32Item);
            tempStaticData.add(mirror(inputData, byteNumber));

        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 2;
            int tempCurrentColumn = (currentLength / 2) % column;
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
                int size = notLeftRightLists.size();
                int newIndex = size;
                notLeftRightLists.add(new ArrayList<>());
                staticData.add(new ArrayList<>());
                tempList = notLeftRightLists.get(newIndex);
                tempStaticData = staticData.get(newIndex);
                tempList.add(textEmoji32Item);
                tempStaticData.add(mirror(inputData, byteNumber));
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData16ToTheRight(result);
                    }
                    result = concat(result, inputData);

                    int size = notLeftRightLists.size();
                    int newIndex = size;
                    notLeftRightLists.add(new ArrayList<>());
                    staticData.add(new ArrayList<>());
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                } else {
                    result = addEmptyColumnForData16ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                    int size = notLeftRightLists.size();
                    int newIndex = size - 1;
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                }
            }
        }

        return result;
    }

    /***
     * 处理自动分段
     * @param currentData
     * @param inputData
     * @param column
     * @return
     */
    public static byte[] checkSegment24(byte[] currentData, byte[] inputData, int column) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 3;
            int tempCurrentColumn = (currentLength / 3) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + 1)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData24ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData24ToTheRight(result);
                    result = concat(result, inputData);
                }
            }
        }

        return result;
    }

    public static byte[] checkSegment24(byte[] currentData, byte[] inputData, int column, int spacingNumber,
                                        TextEmojiManager.TextEmoji32Item textEmoji32Item,
                                        List<List<TextEmojiManager.TextEmoji32Item>> notLeftRightLists,
                                        List<List<byte[]>> staticData, int byteNumber) {
        byte[] result;
        List<TextEmojiManager.TextEmoji32Item> tempList;
        List<byte[]> tempStaticData;
        if (null == currentData) {
            result = inputData;
            int newIndex = 0;
            notLeftRightLists.add(new ArrayList<>());
            staticData.add(new ArrayList<>());
            tempList = notLeftRightLists.get(newIndex);
            tempStaticData = staticData.get(newIndex);

            tempList.add(textEmoji32Item);
            tempStaticData.add(mirror(inputData, byteNumber));

        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 3;
            int tempCurrentColumn = (currentLength / 3) % column;
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
                int size = notLeftRightLists.size();
                int newIndex = size;
                notLeftRightLists.add(new ArrayList<>());
                staticData.add(new ArrayList<>());
                tempList = notLeftRightLists.get(newIndex);
                tempStaticData = staticData.get(newIndex);
                tempList.add(textEmoji32Item);
                tempStaticData.add(mirror(inputData, byteNumber));
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData24ToTheRight(result);
                    }
                    result = concat(result, inputData);

                    int size = notLeftRightLists.size();
                    int newIndex = size;
                    notLeftRightLists.add(new ArrayList<>());
                    staticData.add(new ArrayList<>());
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                } else {
                    result = addEmptyColumnForData24ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                    int size = notLeftRightLists.size();
                    int newIndex = size - 1;
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                }
            }
        }

        return result;
    }

    public static byte[] checkSegment24(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 3;
            int tempCurrentColumn = (currentLength / 3) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData24ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData24ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }

        return result;
    }

    /***
     * 处理自动分段
     * @param currentData
     * @param inputData
     * @param column
     * @return
     */
    public static byte[] checkSegment32(byte[] currentData, byte[] inputData, int column) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 4;
            int tempCurrentColumn = (currentLength / 4) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + 1)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData32ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData32ToTheRight(result);
                    result = concat(result, inputData);
                }
            }
        }

        return result;
    }


    public static byte[] checkSegment32(byte[] currentData, byte[] inputData, int column, int spacingNumber,
                                        TextEmojiManager.TextEmoji32Item textEmoji32Item,
                                        List<List<TextEmojiManager.TextEmoji32Item>> notLeftRightLists,
                                        List<List<byte[]>> staticData, int byteNumber) {
        byte[] result;
        List<TextEmojiManager.TextEmoji32Item> tempList;
        List<byte[]> tempStaticData;
        if (null == currentData) {
            result = inputData;
            int newIndex = 0;
            notLeftRightLists.add(new ArrayList<>());
            staticData.add(new ArrayList<>());
            tempList = notLeftRightLists.get(newIndex);
            tempStaticData = staticData.get(newIndex);

            tempList.add(textEmoji32Item);
            tempStaticData.add(mirror(inputData, byteNumber));

        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / 4;
            int tempCurrentColumn = (currentLength / 4) % column;
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
                int size = notLeftRightLists.size();
                int newIndex = size;
                notLeftRightLists.add(new ArrayList<>());
                staticData.add(new ArrayList<>());
                tempList = notLeftRightLists.get(newIndex);
                tempStaticData = staticData.get(newIndex);
                tempList.add(textEmoji32Item);
                tempStaticData.add(mirror(inputData, byteNumber));
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData32ToTheRight(result);
                    }
                    result = concat(result, inputData);

                    int size = notLeftRightLists.size();
                    int newIndex = size;
                    notLeftRightLists.add(new ArrayList<>());
                    staticData.add(new ArrayList<>());
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                } else {
                    result = addEmptyColumnForData32ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                    int size = notLeftRightLists.size();
                    int newIndex = size - 1;
                    tempList = notLeftRightLists.get(newIndex);
                    tempStaticData = staticData.get(newIndex);
                    tempList.add(textEmoji32Item);
                    tempStaticData.add(mirror(inputData, byteNumber));
                }
            }
        }

        return result;
    }


    public static byte[] checkSegment32(byte[] currentData, byte[] inputData, int column, int spacingNumber) {
        byte[] result;
        if (null == currentData) {
            result = inputData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
//            CLog.i(TAG, "checkSegment16 currentLength>>>>" + currentLength);
//            CLog.i(TAG, "checkSegment16 inputLength>>>>" + inputLength);
            int inputColumn = inputLength / 4;
            int tempCurrentColumn = (currentLength / 4) % column;
//            CLog.i(TAG, "checkSegment16 tempCurrentColumn>>>>" + tempCurrentColumn);
            if (tempCurrentColumn == 0) {
                result = concat(currentData, inputData);
            } else {
                result = currentData;
                int currentRestColumn = column - tempCurrentColumn;
//                CLog.i(TAG, "checkSegment16 currentRestColumn>>>>" + currentRestColumn);
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    for (int i = 0; i < currentRestColumn; i++) {
                        result = addEmptyColumnForData32ToTheRight(result);
                    }
                    result = concat(result, inputData);
                } else {
                    result = addEmptyColumnForData32ToTheRight(result, spacingNumber);
                    result = concat(result, inputData);
                }
            }
        }

        return result;
    }


    public static List<byte[]> splitBytes(byte[] input, int column) {
        int count = 2 * column;
        List<byte[]> result = new ArrayList<>();

        // 检查输入是否合法
        if (input == null || column <= 0 || input.length % count != 0) {
            return result; // 返回空列表
        }

        // 计算分割后的数组数量
        int numberOfChunks = input.length / count;

        // 创建分割后的数组
        for (int i = 0; i < numberOfChunks; i++) {
            byte[] chunk = new byte[count];
            System.arraycopy(input, i * count, chunk, 0, count);
            result.add(chunk);
        }

        return result;
    }

    public static List<byte[]> splitBytes(byte[] input, int column, int byteNumber) {
        int count = byteNumber * column;
        List<byte[]> result = new ArrayList<>();
        if (input.length % count != 0) {
            int rest = count - (input.length % count);
            for (int i = 0; i < rest; i++) {
                byte[] tempData = new byte[1];
                tempData[0] = 0;
                input = concat(input, tempData);
            }
        }

        // 检查输入是否合法
        if (input == null || column <= 0) {
            return result; // 返回空列表
        }

        // 计算分割后的数组数量
        int numberOfChunks = input.length / count;
        if (numberOfChunks == 0) {
            result.add(input);
        } else {
            // 创建分割后的数组
            for (int i = 0; i < numberOfChunks; i++) {
                byte[] chunk = new byte[count];
                System.arraycopy(input, i * count, chunk, 0, count);
                result.add(chunk);
            }
        }

        return result;
    }

    private static byte[] addAllSpiltedBytes(List<byte[]> input) {
        byte[] result = new byte[0];
        for (byte[] temp : input) {
            result = concat(result, mirror(temp));
        }
        return result;
    }

    private static byte[] addAllSpiltedBytes(List<byte[]> input, int byteNumber) {
        byte[] result = new byte[0];
        for (byte[] temp : input) {
            result = concat(result, mirror(temp, byteNumber));
        }
        return result;
    }

    private static List<byte[]> getSingleTextEmojiData(List<byte[]> input, int byteNumber) {
        List<byte[]> result = new ArrayList<>();
        List<byte[]> mirroredInput = new ArrayList<>();
        for (byte[] temp : input) {
            mirroredInput.add(mirror(temp, byteNumber));
        }


        return result;
    }

    /***
     * 处理分段 多余不够一屏幕显示的居中显示
     * @param input
     * @param column
     * @return
     */
    private static byte[] dealSegmentResult16(byte[] input, int column) {
        byte[] result;
        int inputColumn = input.length / 2;
        int restCount = input.length % (2 * column);
        int count = input.length / (2 * column);
        if (restCount == 0) {
            result = input;
        } else {
            if (count == 0) {
                result = input;
                int restColumn = column - (inputColumn % column);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
            } else {
                int resultLength = 2 * column * count;
                result = new byte[resultLength];
                for (int i = 0; i < resultLength; i++) {
                    result[i] = input[i];
                }
                int restInputLength = input.length - resultLength;
                byte[] restInput = new byte[restInputLength];
                for (int i = 0; i < restInputLength; i++) {
                    restInput[i] = input[resultLength + i];
                }

                int restColumn = column - (restInputLength / 2);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData16ToTheLeft(restInput);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    restInput = addEmptyColumnForData16ToTheLeft(restInput);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData16ToTheRight(restInput);
                }
                result = concat(result, restInput);
            }
        }
        return result;
    }

    /***
     * 处理分段 多余不够一屏幕显示的居中显示
     * @param input
     * @param column
     * @return
     */
    private static byte[] dealSegmentResult14(byte[] input, int column) {
        byte[] result;
        int inputColumn = input.length / 2;
        int restCount = input.length % (2 * column);
        int count = input.length / (2 * column);
        if (restCount == 0) {
            result = input;
        } else {
            if (count == 0) {
                result = input;
                int restColumn = column - (inputColumn % column);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
            } else {
                int resultLength = 2 * column * count;
                result = new byte[resultLength];
                for (int i = 0; i < resultLength; i++) {
                    result[i] = input[i];
                }
                int restInputLength = input.length - resultLength;
                byte[] restInput = new byte[restInputLength];
                for (int i = 0; i < restInputLength; i++) {
                    restInput[i] = input[resultLength + i];
                }

                int restColumn = column - (restInputLength / 2);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData16ToTheLeft(restInput);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    restInput = addEmptyColumnForData16ToTheLeft(restInput);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData16ToTheRight(restInput);
                }
                result = concat(result, restInput);
            }
        }
        return result;
    }

    private static byte[] dealSegmentResult12(byte[] input, int column) {
        byte[] result;
        int inputColumn = input.length / 2;
        int restCount = input.length % (2 * column);
        int count = input.length / (2 * column);
        if (restCount == 0) {
            result = input;
        } else {
            if (count == 0) {
                result = input;
                int restColumn = column - (inputColumn % column);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
            } else {
                int resultLength = 2 * column * count;
                result = new byte[resultLength];
                for (int i = 0; i < resultLength; i++) {
                    result[i] = input[i];
                }
                int restInputLength = input.length - resultLength;
                byte[] restInput = new byte[restInputLength];
                for (int i = 0; i < restInputLength; i++) {
                    restInput[i] = input[resultLength + i];
                }

                int restColumn = column - (restInputLength / 2);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData16ToTheLeft(restInput);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    restInput = addEmptyColumnForData16ToTheLeft(restInput);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData16ToTheRight(restInput);
                }
                result = concat(result, restInput);
            }
        }
        return result;
    }

    private static byte[] dealSegmentResult8(byte[] input, int column) {
        byte[] result;
        int inputColumn = input.length;
        int restCount = input.length % (column);
        int count = input.length / (column);
        if (restCount == 0) {
            result = input;
        } else {
            if (count == 0) {
                result = input;
                int restColumn = column - (inputColumn % column);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData8ToTheLeft(result);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    result = addEmptyColumnForData8ToTheRight(result);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData8ToTheRight(result);
                }
            } else {
                int resultLength = column * count;
                result = new byte[resultLength];
                for (int i = 0; i < resultLength; i++) {
                    result[i] = input[i];
                }
                int restInputLength = input.length - resultLength;
                byte[] restInput = new byte[restInputLength];
                for (int i = 0; i < restInputLength; i++) {
                    restInput[i] = input[resultLength + i];
                }

                int restColumn = column - (restInputLength);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData8ToTheLeft(restInput);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    restInput = addEmptyColumnForData8ToTheLeft(restInput);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData8ToTheRight(restInput);
                }
                result = concat(result, restInput);
            }
        }
        return result;
    }

    /***
     * 处理分段 多余不够一屏幕显示的居中显示
     * @param input
     * @param column
     * @return
     */
    private static byte[] dealSegmentResult24(byte[] input, int column) {
        byte[] result;
        int inputColumn = input.length / 3;
        int restCount = input.length % (3 * column);
        int count = input.length / (3 * column);
        if (restCount == 0) {
            result = input;
        } else {
            if (count == 0) {
                result = input;
                int restColumn = column - (inputColumn % column);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData24ToTheLeft(result);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    result = addEmptyColumnForData24ToTheRight(result);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData24ToTheRight(result);
                }
            } else {
                int resultLength = 3 * column * count;
                result = new byte[resultLength];
                for (int i = 0; i < resultLength; i++) {
                    result[i] = input[i];
                }
                int restInputLength = input.length - resultLength;
                byte[] restInput = new byte[restInputLength];
                for (int i = 0; i < restInputLength; i++) {
                    restInput[i] = input[resultLength + i];
                }

                int restColumn = column - (restInputLength / 3);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData24ToTheLeft(restInput);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    restInput = addEmptyColumnForData24ToTheLeft(restInput);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData24ToTheRight(restInput);
                }
                result = concat(result, restInput);
            }
        }
        return result;
    }

    /***
     * 处理分段 多余不够一屏幕显示的居中显示
     * @param input
     * @param column
     * @return
     */
    private static byte[] dealSegmentResult32(byte[] input, int column) {
        byte[] result;
        int inputColumn = input.length / 4;
        int restCount = input.length % (4 * column);
        int count = input.length / (4 * column);
        if (restCount == 0) {
            result = input;
        } else {
            if (count == 0) {
                result = input;
                int restColumn = column - (inputColumn % column);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData32ToTheLeft(result);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    result = addEmptyColumnForData32ToTheRight(result);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    result = addEmptyColumnForData32ToTheRight(result);
                }
            } else {
                int resultLength = 4 * column * count;
                result = new byte[resultLength];
                for (int i = 0; i < resultLength; i++) {
                    result[i] = input[i];
                }
                int restInputLength = input.length - resultLength;
                byte[] restInput = new byte[restInputLength];
                for (int i = 0; i < restInputLength; i++) {
                    restInput[i] = input[resultLength + i];
                }

                int restColumn = column - (restInputLength / 4);
                int halfRestColumn = restColumn / 2;
                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData32ToTheLeft(restInput);
                }
                int isDouble = restColumn % 2;
                if (isDouble != 0) {
                    restInput = addEmptyColumnForData32ToTheLeft(restInput);
                }

                for (int i = 0; i < halfRestColumn; i++) {
                    restInput = addEmptyColumnForData32ToTheRight(restInput);
                }
                result = concat(result, restInput);
            }
        }
        return result;
    }

    private static byte[] processBytesCentered8(byte[] input) {
        byte[] result = input;

        int restColumn = 8 - input.length;
        if (restColumn > 0) {
            int halfNumber = restColumn / 2;
            int halfRestNumber = restColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData8ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData8ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData8ToTheRight(result);
            }
        }
        result = rotate(90, result, 8);
        result = deleteEmptyColumnFor8(result);

        int newRestColumn = 8 - result.length;
        if (newRestColumn > 0) {
            int halfNumber = newRestColumn / 2;
            int halfRestNumber = newRestColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData8ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData8ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData8ToTheRight(result);
            }
        }

        result = rotate(270, result, 8);
        result = deleteEmptyColumnFor8(result);
        return result;
    }

    /***
     * 处理居中显示
     * @param input
     * @return
     */
    private static byte[] processBytesCentered(byte[] input) {
        byte[] result = input;

        int restColumn = 16 - input.length / 2;
        if (restColumn > 0) {
            int halfNumber = restColumn / 2;
            int halfRestNumber = restColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData16ToTheRight(result);
            }
        }
        result = rotate(90, result);
        result = deleteEmptyColumnFor16(result);

        int newRestColumn = 16 - result.length / 2;
        if (newRestColumn > 0) {
            int halfNumber = newRestColumn / 2;
            int halfRestNumber = newRestColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData16ToTheRight(result);
            }
        }

        result = rotate(270, result);
        result = deleteEmptyColumnFor16(result);
        return result;
    }

    private static byte[] processBytesCentered12(byte[] input) {
        byte[] result = input;

        int restColumn = 12 - input.length / 2;
        if (restColumn > 0) {
            int halfNumber = restColumn / 2;
            int halfRestNumber = restColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData16ToTheRight(result);
            }
        }
        result = rotate(90, result, 12);
        result = deleteEmptyColumnFor14(result);

        int newRestColumn = 12 - result.length / 2;
        if (newRestColumn > 0) {
            int halfNumber = newRestColumn / 2;
            int halfRestNumber = newRestColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData16ToTheRight(result);
            }
        }

        result = rotate(270, result, 12);
        result = deleteEmptyColumnFor12(result);
        return result;
    }

    /***
     * 处理居中显示
     * @param input
     * @return
     */
    private static byte[] processBytesCentered14(byte[] input) {
        byte[] result = input;

        int restColumn = 14 - input.length / 2;
        if (restColumn > 0) {
            int halfNumber = restColumn / 2;
            int halfRestNumber = restColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData16ToTheRight(result);
            }
        }
        result = rotate(90, result, 14);
        result = deleteEmptyColumnFor14(result);

        int newRestColumn = 14 - result.length / 2;
        if (newRestColumn > 0) {
            int halfNumber = newRestColumn / 2;
            int halfRestNumber = newRestColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData16ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData16ToTheRight(result);
            }
        }

        result = rotate(270, result, 14);
        result = deleteEmptyColumnFor14(result);
        return result;
    }

    /***
     * 处理居中显示
     * @param input
     * @return
     */
    private static byte[] processBytesCentered20(byte[] input) {
        byte[] result = input;

        int restColumn = 20 - input.length / 3;
        if (restColumn > 0) {
            int halfNumber = restColumn / 2;
            int halfRestNumber = restColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData24ToTheRight(result);
            }
        }
        result = rotate(90, result, 20);
        result = deleteEmptyColumnFor24(result);

        int newRestColumn = 20 - result.length / 3;
        if (newRestColumn > 0) {
            int halfNumber = newRestColumn / 2;
            int halfRestNumber = newRestColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData24ToTheRight(result);
            }
        }

        result = rotate(270, result, 20);
        result = deleteEmptyColumnFor24(result);
        return result;
    }

    /***
     * 处理居中显示
     * @param input
     * @return
     */
    private static byte[] processBytesCentered24(byte[] input) {
        byte[] result = input;

        int restColumn = 24 - input.length / 3;
        if (restColumn > 0) {
            int halfNumber = restColumn / 2;
            int halfRestNumber = restColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData24ToTheRight(result);
            }
        }
        result = rotate(90, result, 24);
        result = deleteEmptyColumnFor24(result);

        int newRestColumn = 24 - result.length / 3;
        if (newRestColumn > 0) {
            int halfNumber = newRestColumn / 2;
            int halfRestNumber = newRestColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData24ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData24ToTheRight(result);
            }
        }

        result = rotate(270, result, 24);
        result = deleteEmptyColumnFor24(result);
        return result;
    }

    /***
     * 处理居中显示
     * @param input
     * @return
     */
    private static byte[] processBytesCentered32(byte[] input) {
        byte[] result = input;

        int restColumn = 32 - input.length / 4;
        if (restColumn > 0) {
            int halfNumber = restColumn / 2;
            int halfRestNumber = restColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData32ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData32ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData32ToTheRight(result);
            }
        }
        result = rotate(90, result, 32);
        result = deleteEmptyColumnFor32(result);

        int newRestColumn = 32 - result.length / 4;
        if (newRestColumn > 0) {
            int halfNumber = newRestColumn / 2;
            int halfRestNumber = newRestColumn % 2;
            if (halfNumber > 0) {
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData32ToTheRight(result);
                }
                for (int i = 0; i < halfNumber; i++) {
                    result = addEmptyColumnForData32ToTheLeft(result);
                }
            }

            if (halfRestNumber > 0) {
                result = addEmptyColumnForData32ToTheRight(result);
            }
        }

        result = rotate(270, result, 32);
        result = deleteEmptyColumnFor32(result);
        return result;
    }

    private static List<String> byte2hex(byte[] buffer) {
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

    private byte[] readUnicode1248(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE12);//打开中文字库的流
            int index = char_num;
            long offset = index * 24;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[24];//定义缓存区的大小
            in.read(data, 0, 24);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode1236(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE12);//打开中文字库的流
            int index = char_num;
            long offset = index * 24;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[24];//定义缓存区的大小
            in.read(data, 0, 24);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode8(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE8);//打开中文字库的流
            int index = char_num;
            long offset = index * 8;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[8];//定义缓存区的大小
            in.read(data, 0, 8);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode8Bold(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE8_BOLD);//打开中文字库的流
            int index = char_num;
            long offset = index * 8;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[8];//定义缓存区的大小
            in.read(data, 0, 8);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode16(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE16);//打开中文字库的流
            int index = char_num;
            long offset = index * 32;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[32];//定义缓存区的大小
            in.read(data, 0, 32);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode16Bold(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE16_BOLD);//打开中文字库的流
            int index = char_num;
            long offset = index * 32;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[32];//定义缓存区的大小
            in.read(data, 0, 32);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode3216Bold(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_16_BOLD);//打开中文字库的流
            int index = char_num;
            long offset = index * 32;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[32];//定义缓存区的大小
            in.read(data, 0, 32);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode3216(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_16);//打开中文字库的流
            int index = char_num;
            long offset = index * 32;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[32];//定义缓存区的大小
            in.read(data, 0, 32);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode3214(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_14);//打开中文字库的流
            int index = char_num;
            long offset = index * 28;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[28];//定义缓存区的大小
            in.read(data, 0, 28);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode3214Bold(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_14_BOLD);//打开中文字库的流
            int index = char_num;
            long offset = index * 28;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[28];//定义缓存区的大小
            in.read(data, 0, 28);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] checkData3214(byte[] input) {
        byte[] data = input;
        for (int i = 0; i <= (input.length - 2); i += 2) {
            byte tempValue = (byte) ((0x01 & data[i]) << 7);
            data[i] = (byte) ((data[i] & 0xff) >>> 1);
            data[i + 1] = (byte) ((data[i + 1] & 0xff) >>> 1);
            data[i + 1] = (byte) (data[i + 1] | tempValue);
        }
        return data;
    }

    private byte[] readUnicode3224Bold(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_24_BOLD);//打开中文字库的流
            int index = char_num;
            long offset = index * 72;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[72];//定义缓存区的大小
            in.read(data, 0, 72);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode3224(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_24);//打开中文字库的流
            int index = char_num;
            long offset = index * 72;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[72];//定义缓存区的大小
            in.read(data, 0, 72);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode3232Bold(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_32_BOLD);//打开中文字库的流
            int index = char_num;
            long offset = index * 128;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[128];//定义缓存区的大小
            in.read(data, 0, 128);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readUnicode3232(char char_num) {
        byte[] data = null;
        try {
            InputStream in = CoolLED.getInstance().getResources().getAssets().open(UNICODE32_32);//打开中文字库的流
            int index = char_num;
            long offset = index * 128;//汉字在字库里的偏移量
            in.skip(offset);//跳过偏移量
            data = new byte[128];//定义缓存区的大小
            in.read(data, 0, 128);//读取该汉字的点阵数据
            in.close();
        } catch (Exception ex) {
            CoolLED.reportError(ex);
        }
        return data;
    }

    private byte[] readFontData(char char_num, int fontSize) {
        int temp = (fontSize % 8);
        int rest = temp > 0 ? 1 : 0;
        int fontByteSize = ((fontSize / 8) + rest) * fontSize;
//        CLog.i(TAG, "readFontData>>>fontByteSize>>>" + fontByteSize);
        byte[] data = new byte[fontByteSize];

        if (mFontLibFileStream == null) {
            return data;
        }

        try {
            mFontLibFileStream.seek(char_num * fontByteSize);
            mFontLibFileStream.read(data, 0, fontByteSize);
        } catch (Exception e) {
            CoolLED.reportError(e);
        }
        return data;
    }


    RandomAccessFile mFontLibFileStream = null;

    //
    private boolean copyFontLibFileToCache(String fileName) {
        File file = new File(CoolLED.getInstance().getCacheDir(), fileName);

        if (file.exists()) {
            return true;
        }
        CLog.i(TAG, "readFont>>>font lib not exists in cache, copy!" + fileName);

        try {
            InputStream inputStream = CoolLED.getInstance().getResources().getAssets().open(fileName);
            File outputFile = new File(CoolLED.getInstance().getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return true;
        } catch (Exception e) {
            CLog.i(TAG, "copyFontLibFileToCache>>>" + e.getMessage());
            CoolLED.reportError(e);
        }
        return false;
    }

    private void startReadFontData(int fontSize, boolean isBold) {
        String fileName = getFontLibName(fontSize, isBold);

        if (fileName != null) {
            try {
                File file = new File(CoolLED.getInstance().getCacheDir(), fileName);

                if (!file.exists()) {
                    if (copyFontLibFileToCache(fileName)) {
                        file = new File(CoolLED.getInstance().getCacheDir(), fileName);
                    } else {
                        return;
                    }
                }

                mFontLibFileStream = new RandomAccessFile(file, "r");
            } catch (Exception e) {
                CLog.i(TAG, "startReadFontData>>>" + e.getMessage());
                CoolLED.reportError(e);
            }
        }
    }

    /**
     * 结束字库读取操作，这里最好在destroy中再调用一次，防止应用异常或者强制退出，资源得不到释放。
     */
    public void release() {
        if (mFontLibFileStream != null) {
            try {
                mFontLibFileStream.close();
            } catch (IOException e) {
                CoolLED.reportError(e);
            }
            mFontLibFileStream = null;
        }
    }

    //
//    //     finalize 方法也不会保证会被调用，不推荐
//    @Override
//    protected void finalize() throws Throwable {
//        try {
//            endReadFontData();
//        } catch (Exception e) {
//            super.finalize();
//        }
//
//    }
    private String getFontLibName(int fontSize, boolean isBold) {
        String fileName = null;

        if (fontSize == 24) {
            if (isBold) {
                fileName = UNICODE32_24_BOLD;
            } else {
                fileName = UNICODE32_24;
            }
        } else if (fontSize == 20) {
            if (isBold) {
                fileName = UNICODE_20_BOLD;
            } else {
                fileName = UNICODE_20;
            }
        } else if (fontSize == 16) {
            if (isBold) {
                fileName = UNICODE32_16_BOLD;
            } else {
                fileName = UNICODE32_16;
            }
        } else if (fontSize == 14) {
            if (isBold) {
                fileName = UNICODE32_14_BOLD;
            } else {
                fileName = UNICODE32_14;
            }
        } else if (fontSize == 32) {
            if (isBold) {
                fileName = UNICODE32_32_BOLD;
            } else {
                fileName = UNICODE32_32;
            }
        } else if (fontSize == 12) {
            fileName = UNICODE12;
        } else if (fontSize == 8) {
            if (isBold) {
                fileName = UNICODE8_BOLD;
            } else {
                fileName = UNICODE8;
            }
        }

        return fileName;
    }

    static class UXDataItem {
        public int textType;
        public byte[] textData;
        public List<String> imageData;
    }

    public List<JTextFont> getFontByteDataCoolleduxForEmojinew(String input, int width, int height, int textSize, int mode, int color, boolean isOneWordOneColor) {
        List<JTextFont> textFont = new ArrayList<>();
        List<String> resultData = new ArrayList<>();
        int allTextWidth = 0;
        List<String> allTextData = new ArrayList<>();
        int showRow = height;
        int textSpacing = 1;
        int fontType = TextFontTypeItem.SONG;
        String content = input;
        String language = "zh-CN";
        content = ArabicCharDotMatrixGenerator.getVisualText(language, content);
        List<TextEmojiManager.TextEmoji32Item> textEmojis = TextEmojiManagerCoolLEDUX.getInstance().getTextEmojiItems(language, content, TextEmojiManagerCoolLEDUX.PATTEN_STR);
        if (isOneWordOneColor) {
            for (int i = 0; i < textEmojis.size(); i++) {
                TextEmojiManager.TextEmoji32Item textEmoji32Item = textEmojis.get(i);
                textEmoji32Item.isBold = true;
                textEmoji32Item.rotate = 0;
                textEmoji32Item.color = JTextDiyColorContent.multicolorData[i%JTextDiyColorContent.multicolorData.length];
                textEmoji32Item.textSize = textSize;
            }
        } else {
            for (TextEmojiManager.TextEmoji32Item textEmoji32Item : textEmojis) {
                textEmoji32Item.isBold = true;
                textEmoji32Item.rotate = 0;
                textEmoji32Item.color = color;
                textEmoji32Item.textSize = textSize;
            }
        }


        boolean notLeftRightMode = (
                mode == ModeAdapter.STATIC ||
                        mode == ModeAdapter.UP_MODE ||
                        mode == ModeAdapter.DOWN_MODE ||
                        mode == ModeAdapter.ACCUMULATE_MODE ||
                        mode == ModeAdapter.PICTURE_MODE ||
                        mode == ModeAdapter.SHINING_MODE ||
                        mode == ModeAdapter.LEFT_PANNING_MODE ||
                        mode == ModeAdapter.RIGHT_PANNING_MODE ||
                        mode == ModeAdapter.LEFT_COVER_MODE ||
                        mode == ModeAdapter.RIGHT_COVER_MODE ||
                        mode == ModeAdapter.LEFT_RIGHT_MODE);
        byte[] data = new byte[0];
        List<byte[]> staticDatas = new ArrayList<>();
        List<UXDataItem> listUXDataItem = new ArrayList<>();
        List<TextEmojiManager.TextEmoji32Item> textEmojisList = new ArrayList<>();

//        for (TextEmojiManager.TextEmoji32Item item : textEmojis) {
//            if(item.isArbic){
//                Collections.reverse(textEmojis);
//                break;
//            }
//        }

        for (int i = 0; i < textEmojis.size(); i++) {
//            textFontItem.setTextType(JTextFont.TextType.SINGLECOLOR);
            TextEmojiManager.TextEmoji32Item textEmoji = textEmojis.get(i);
            byte[] temp = null;
            List<String> imageData = new ArrayList<>();
            List<DrawView.DrawItem> drawItems = new ArrayList<>();
            int textType = 0;
            boolean isOtherInput = false;
            int otherInputWidth = 0;
            int otherInputHeight = 0;
            if (textEmoji.isText) {
                textType = 0;
                if (((language.equalsIgnoreCase("zh-CN") && fontType == TextFontTypeItem.SONG) || language.equalsIgnoreCase("vi") || !ArabicCharDotMatrixGenerator.isLanguageSupported(textEmoji.text.charAt(0))) && (textEmoji.textSize != 8)) {
//                    temp = ArabicCharDotMatrixGenerator.readFontDataFromDraw(textEmoji.text.charAt(0), showRow, textEmoji.textSize, textEmoji.isBold);
                    if (textEmoji.text.length() == 1) {
                        if (language.equalsIgnoreCase("zh-CN")) {
                            CLog.i(TAG, "getgetFontByteDataCoolleduxForEmojinew>>> textEmoji.text>>>" + textEmoji.text);
                            temp = ArabicCharDotMatrixGenerator.readFontDataFromDraw(language, textEmoji.text.charAt(0), showRow, textEmoji.textSize, textEmoji.isBold, fontType);
                        } else {
                            temp = ArabicCharDotMatrixGenerator.readFontDataFromDraw(language, textEmoji.text.charAt(0), showRow, textEmoji.textSize, textEmoji.isBold);
                        }
                    } else {
                        isOtherInput = true;
                        otherInputWidth = showRow * textEmoji.text.length();
                        otherInputHeight = showRow;
                        temp = ArabicCharDotMatrixGenerator.readFontDataFromDraw(language, textEmoji.text, otherInputWidth, otherInputHeight, textEmoji.textSize, textEmoji.isBold);
                    }
                } else {
                    temp = readFontData(textEmoji.text.charAt(0), textEmoji.textSize);
                    if (showRow == 32) {
                        if (textEmoji.textSize == 32) {
                        } else if (textEmoji.textSize == 24) {
                            temp = transfer24FontTo32(temp);
                        } else if (textEmoji.textSize == 20) {
                            temp = transfer20FontTo32(temp);
                        } else if (textEmoji.textSize == 16) {
                            temp = transfer16FontTo32(temp);
                        } else if (textEmoji.textSize == 14) {
                            temp = transfer14FontTo32(temp);
                        } else if (textEmoji.textSize == 12) {
                            temp = transfer12FontTo32(temp);
                        }
                    } else if (showRow == 24) {
                        if (textEmoji.textSize == 24) {
                        } else if (textEmoji.textSize == 20) {
                            temp = transfer20FontTo24(temp);
                        } else if (textEmoji.textSize == 16) {
                            temp = transfer16FontTo24(temp);
                        } else if (textEmoji.textSize == 14) {
                            temp = transfer14FontTo24(temp);
                        } else if (textEmoji.textSize == 12) {
                            temp = transfer12FontTo24(temp);
                        }
                    } else if (showRow == 20) {
                        if (textEmoji.textSize == 20) {
                        } else if (textEmoji.textSize == 16) {
                            temp = transfer16FontTo20(temp);
                        } else if (textEmoji.textSize == 14) {
                            temp = transfer14FontTo20(temp);
                        } else if (textEmoji.textSize == 12) {
                            temp = transfer12FontTo20(temp);
                        }
                    } else if (showRow == 16) {
                        if (textEmoji.textSize == 16) {
                        } else if (textEmoji.textSize == 14) {
                            temp = transfer14FontTo16(temp);
                        } else if (textEmoji.textSize == 12) {
                            temp = transfer12FontTo16(temp);
                        }
                    } else if (showRow == 14) {
                        if (textEmoji.textSize == 14) {
                        } else if (textEmoji.textSize == 12) {
                            temp = transfer12FontTo14(temp);
                        }
                    } else if (showRow == 12) {
                        if (textEmoji.textSize == 12) {
                        }
                    } else if (showRow == 8) {
                        if (textEmoji.textSize == 8) {
                        }
                    }
                }
            } else {
                textType = 1;
                List<String> emojiData = new ArrayList<>();
                List<String> hexStrings = new ArrayList<>();

                if (showRow == 32) {
                    if (textEmoji.textSize == 32) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName32, 32, 32);
                    } else if (textEmoji.textSize == 24) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName24, 24, 32);
                    } else if (textEmoji.textSize == 20) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName20, 20, 32);
                    } else if (textEmoji.textSize == 16) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName16, 16, 32);
                    } else if (textEmoji.textSize == 14) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName14, 14, 32);
                    } else if (textEmoji.textSize == 12) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName12, 12, 32);
                    }
                    emojiData = TextEmojiManagerCoolLEDUX.getImageData(drawItems, 32, 32);
                    imageData = CoolledUXUtils.getDrawListDataFColor(drawItems, 32, 32);
                } else if (showRow == 24) {
                    if (textEmoji.textSize == 24) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName24, 24, 24);
                    } else if (textEmoji.textSize == 20) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName20, 20, 24);
                    } else if (textEmoji.textSize == 16) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName16, 16, 24);
                    } else if (textEmoji.textSize == 14) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName14, 14, 24);
                    } else if (textEmoji.textSize == 12) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName12, 12, 24);
                    }
                    emojiData = TextEmojiManagerCoolLEDUX.getImageData(drawItems, 24, 24);
                    imageData = CoolledUXUtils.getDrawListDataFColor(drawItems, 24, 24);
                } else if (showRow == 20) {
                    if (textEmoji.textSize == 20) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName20, 20, 20);
                    } else if (textEmoji.textSize == 16) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName16, 16, 20);
                    } else if (textEmoji.textSize == 14) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName14, 14, 20);
                    } else if (textEmoji.textSize == 12) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName12, 12, 20);
                    }
                    emojiData = TextEmojiManagerCoolLEDUX.getImageData(drawItems, 20, 20);
                    imageData = CoolledUXUtils.getDrawListDataFColor(drawItems, 20, 20);
                } else if (showRow == 16) {
                    if (textEmoji.textSize == 16) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName16, 16, 16);
                    } else if (textEmoji.textSize == 14) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName14, 14, 16);
                    } else if (textEmoji.textSize == 12) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName12, 12, 16);
                    }
                    emojiData = TextEmojiManagerCoolLEDUX.getImageData(drawItems, 16, 16);
                    imageData = CoolledUXUtils.getDrawListDataFColor(drawItems, 16, 16);
                } else if (showRow == 14) {
                    if (textEmoji.textSize == 14) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName14, 14, 14);
                    } else if (textEmoji.textSize == 12) {
                        drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName12, 12, 14);
                    }
                    emojiData = TextEmojiManagerCoolLEDUX.getImageData(drawItems, 14, 14);
                    imageData = CoolledUXUtils.getDrawListDataFColor(drawItems, 14, 14);
                } else if (showRow == 12) {
                    drawItems = TextEmojiManagerCoolLEDUX.getDrawItemsFromBitmap(textEmoji.imageName12, 12, 12);
                    emojiData = TextEmojiManagerCoolLEDUX.getImageData(drawItems, 12, 12);
                    imageData = CoolledUXUtils.getDrawListDataFColor(drawItems, 12, 12);
                }


                for (String str : emojiData) {
                    hexStrings.add(str);
                }
                for (String str : hexStrings) {
                    byte[] tempData = HexUtil.hexStringToBytes(str);
                    if (null == temp) {
                        temp = tempData;
                    } else {
                        temp = concat(temp, tempData);
                    }
                }
            }

            if (isOtherInput && (textEmoji.rotate == 180)) {
                temp = flip180(temp, otherInputWidth, otherInputHeight);
            } else {
                temp = rotate(textEmoji.rotate, temp, showRow);
            }
            if (textType == 1) {
                List<DrawView.DrawItem> tempDrawItems = rotate(textEmoji.rotate, drawItems, showRow);
                imageData = CoolledUXUtils.getDrawListDataColorAndDeleteEmptyColumn(tempDrawItems, showRow, showRow);
            }


            if (showRow == 8) {
                temp = deleteEmptyColumnFor8(temp, textEmoji.textSize);
            } else if (showRow == 12) {
                temp = deleteEmptyColumnFor12(temp, textEmoji.textSize);
            } else if (showRow == 14) {
                temp = deleteEmptyColumnFor14(temp, textEmoji.textSize);
            } else if (showRow == 16) {
                temp = deleteEmptyColumnFor16(temp, textEmoji.textSize);
            } else if (showRow == 20) {
                temp = deleteEmptyColumnFor20(temp, textEmoji.textSize);
            } else if (showRow == 24) {
                temp = deleteEmptyColumnFor24(temp, textEmoji.textSize);
            } else if (showRow == 32) {
                temp = deleteEmptyColumnFor32(temp, textEmoji.textSize);
            }

            if (textType == 0) {
                if (textEmoji.rotate == 90 || textEmoji.rotate == 270) {
                    if (showRow == 8) {
                        temp = processBytesCentered8(temp);
                        temp = deleteEmptyColumnFor8(temp, textEmoji.textSize);
                    } else if (showRow == 12) {
                        temp = processBytesCentered12(temp);
                        temp = deleteEmptyColumnFor12(temp, textEmoji.textSize);
                    } else if (showRow == 14) {
                        temp = processBytesCentered14(temp);
                        temp = deleteEmptyColumnFor14(temp, textEmoji.textSize);
                    } else if (showRow == 16) {
                        temp = processBytesCentered(temp);
                        temp = deleteEmptyColumnFor16(temp, textEmoji.textSize);
                    } else if (showRow == 20) {
                        temp = processBytesCentered20(temp);
                        temp = deleteEmptyColumnFor24(temp, textEmoji.textSize);
                    } else if (showRow == 24) {
                        temp = processBytesCentered24(temp);
                        temp = deleteEmptyColumnFor24(temp, textEmoji.textSize);
                    } else if (showRow == 32) {
                        temp = processBytesCentered32(temp);
                        temp = deleteEmptyColumnFor32(temp, textEmoji.textSize);
                    }
                }
            } else {

            }


            if (notLeftRightMode) {
                if (textEmoji.isText) {
                    if (" ".equalsIgnoreCase(textEmoji.text) || TextUtils.isEmpty(textEmoji.text)) {

                    } else {
                        staticDatas.add(temp);
                        textEmojisList.add(textEmoji);
                        UXDataItem uxDataItem = new UXDataItem();
                        uxDataItem.textType = textType;
                        uxDataItem.textData = temp;
                        if (textType == 1) {
                            uxDataItem.imageData = imageData;
                        }
                        listUXDataItem.add(uxDataItem);
                    }
                } else {
                    staticDatas.add(temp);
                    textEmojisList.add(textEmoji);
                    UXDataItem uxDataItem = new UXDataItem();
                    uxDataItem.textType = textType;
                    uxDataItem.textData = temp;
                    if (textType == 1) {
                        uxDataItem.imageData = imageData;
                    }
                    listUXDataItem.add(uxDataItem);
                }


                if (showRow == 8) {
                    data = checkSegment8(data, temp, width, textSpacing);
                } else if (showRow == 12) {
                    data = checkSegment12(data, temp, width, textSpacing);
                } else if (showRow == 14) {
                    data = checkSegment14(data, temp, width, textSpacing);
                } else if (showRow == 16) {
                    data = checkSegment16(data, temp, width, textSpacing);
                } else if (showRow == 20) {
                    data = checkSegment24(data, temp, width, textSpacing);
                } else if (showRow == 24) {
                    data = checkSegment24(data, temp, width, textSpacing);
                } else if (showRow == 32) {
                    data = checkSegment32(data, temp, width, textSpacing);
                }
            } else {
                if (i < (textEmojis.size() - 1)) {
                    if (showRow == 8) {
                        temp = addEmptyColumnForData8ToTheRight(temp, textSpacing);
                    } else if (showRow == 12) {
                        temp = addEmptyColumnForData16ToTheRight(temp, textSpacing);
                    } else if (showRow == 14) {
                        temp = addEmptyColumnForData16ToTheRight(temp, textSpacing);
                    } else if (showRow == 16) {
                        temp = addEmptyColumnForData16ToTheRight(temp, textSpacing);
                    } else if (showRow == 20) {
                        temp = addEmptyColumnForData24ToTheRight(temp, textSpacing);
                    } else if (showRow == 24) {
                        temp = addEmptyColumnForData24ToTheRight(temp, textSpacing);
                    } else if (showRow == 32) {
                        temp = addEmptyColumnForData32ToTheRight(temp, textSpacing);
                    }
                    if (textType == 0) {

                    } else {
                        imageData = addEmptyColumnForImageDataToTheRight(imageData, showRow, textSpacing);
                    }
                }
                if (showRow == 32) {
                    JTextFont textFontItem = new JTextFont();
                    textFontItem.textWidth = temp.length / 4;
                    textFontItem.textColor = textEmoji.color;
                    textFontItem.showData = temp;
                    textFont.add(textFontItem);

                    allTextWidth += temp.length / 4;
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(temp.length / 4));
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                    if (textType == 0) {
                        allTextData.addAll(byte2hex(temp));
                    } else {
                        allTextData.addAll(imageData);
                    }
                } else if (showRow == 24) {
                    JTextFont textFontItem = new JTextFont();
                    textFontItem.textWidth = temp.length / 3;
                    textFontItem.textColor = textEmoji.color;
                    textFontItem.showData = temp;
                    textFont.add(textFontItem);
                    allTextWidth += temp.length / 3;
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(temp.length / 3));
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                    if (textType == 0) {
                        allTextData.addAll(byte2hex(temp));
                    } else {
                        allTextData.addAll(imageData);
                    }
                } else if (showRow == 20) {
                    JTextFont textFontItem = new JTextFont();
                    textFontItem.textWidth = temp.length / 3;
                    textFontItem.textColor = textEmoji.color;
                    textFontItem.showData = temp;
                    textFont.add(textFontItem);
                    allTextWidth += temp.length / 3;
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(temp.length / 3));
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                    if (textType == 0) {
                        allTextData.addAll(byte2hex(temp));
                    } else {
                        allTextData.addAll(imageData);
                    }
                } else if (showRow == 16) {
                    JTextFont textFontItem = new JTextFont();
                    textFontItem.textWidth = temp.length / 2;
                    textFontItem.textColor = textEmoji.color;
                    textFontItem.showData = temp;
                    textFont.add(textFontItem);
                    allTextWidth += temp.length / 2;
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(temp.length / 2));
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                    if (textType == 0) {
                        allTextData.addAll(byte2hex(temp));
                    } else {
                        allTextData.addAll(imageData);
                    }
                } else if (showRow == 14) {
                    JTextFont textFontItem = new JTextFont();
                    textFontItem.textWidth = temp.length / 2;
                    textFontItem.textColor = textEmoji.color;
                    textFontItem.showData = temp;
                    textFont.add(textFontItem);
                    allTextWidth += temp.length / 2;
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(temp.length / 2));
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                    if (textType == 0) {
                        allTextData.addAll(byte2hex(temp));
                    } else {
                        allTextData.addAll(imageData);
                    }
                } else if (showRow == 12) {
                    JTextFont textFontItem = new JTextFont();
                    textFontItem.textWidth = temp.length / 2;
                    textFontItem.textColor = textEmoji.color;
                    textFontItem.showData = temp;
                    textFont.add(textFontItem);
                    allTextWidth += temp.length / 2;
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(temp.length / 2));
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                    if (textType == 0) {
                        allTextData.addAll(byte2hex(temp));
                    } else {
                        allTextData.addAll(imageData);
                    }
                } else if (showRow == 8) {
                    JTextFont textFontItem = new JTextFont();
                    textFontItem.textWidth = temp.length;
                    textFontItem.textColor = textEmoji.color;
                    textFontItem.showData = temp;
                    textFont.add(textFontItem);
                    allTextWidth += temp.length;
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(temp.length));
                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                    if (textType == 0) {
                        allTextData.addAll(byte2hex(temp));
                    } else {
                        allTextData.addAll(imageData);
                    }
                }
            }
        }

        if (notLeftRightMode) {
            if (showRow == 32) {
                data = getCenteredDataBytes(data, 32, width);
            } else if (showRow == 24) {
                data = getCenteredDataBytes(data, 24, width);
            } else if (showRow == 20) {
                data = getCenteredDataBytes(data, 20, width);
            } else if (showRow == 16) {
                data = getCenteredDataBytes(data, 16, width);
            } else if (showRow == 14) {
                data = getCenteredDataBytes(data, 14, width);
            } else if (showRow == 12) {
                data = getCenteredDataBytes(data, 12, width);
            } else if (showRow == 8) {
                data = getCenteredDataBytes(data, 8, width);
            }


            if (showRow == 32) {
                int lastStartIndex = 0;
                for (int i = 0; i < textEmojisList.size(); i++) {
                    TextEmojiManager.TextEmoji32Item textEmoji = textEmojisList.get(i);
                    String text = textEmoji.text;

                    UXDataItem uxDataItem = listUXDataItem.get(i);
                    int textType = uxDataItem.textType;
                    byte[] textData = uxDataItem.textData;
                    List<String> imageData = uxDataItem.imageData;
                    if (" ".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
//                        allTextWidth += 0 / 4;
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(0 / 4));
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(uxDataItem.textType));
////                        allTextData.addAll(null);
                    } else {
                        byte[] staticDataTemp = staticDatas.get(i);
                        int tempLength = staticDataTemp.length;
                        int startIndex = lastStartIndex;
                        for (int j = startIndex; j <= (data.length - 4); j += 4) {
                            boolean isTheSame = true;

                            for (int m = 0; m <= (staticDataTemp.length - 4); m += 4) {
                                if (staticDataTemp[m + 0] != data[j + m + 0] || staticDataTemp[m + 1] != data[j + m + 1] || staticDataTemp[m + 2] != data[j + m + 2] || staticDataTemp[m + 3] != data[j + m + 3]) {
                                    isTheSame = false;
                                    break;
                                }
                            }

                            if (isTheSame) {
                                int emptyColumn = (j - lastStartIndex) / 4;
                                if (emptyColumn > 0) {
                                    if (textType == 0) {
                                        textData = addEmptyColumnForData32ToTheLeft(textData, emptyColumn);
                                    } else if (textType == 1) {
                                        imageData = addEmptyColumnForImageDataToTheLeft(imageData, showRow, emptyColumn);
                                    }
                                }

                                lastStartIndex = (j + tempLength);
                                tempLength = (emptyColumn * 4 + tempLength);
                                for (int n = lastStartIndex; n <= (data.length - 4); n += 4) {
                                    if (data[n + 0] == 0 && data[n + 1] == 0 && data[n + 2] == 0 && data[n + 3] == 0) {
                                        tempLength += 4;
                                        lastStartIndex += 4;
                                        if (textType == 0) {
                                            textData = addEmptyColumnForData32ToTheRight(textData, 1);
                                        } else if (textType == 1) {
                                            imageData = addEmptyColumnForImageDataToTheRight(imageData, showRow, 1);
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                if (textType == 0) {
                                    allTextWidth += tempLength / 4;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 4));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(byte2hex(textData));

                                    JTextFont textFontItem = new JTextFont();
                                    textFontItem.textWidth = tempLength / 4;
                                    textFontItem.textColor = textEmoji.color;
                                    textFontItem.showData = textData;
                                    textFont.add(textFontItem);
                                } else if (textType == 1) {
                                    allTextWidth += tempLength / 4;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 4));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(imageData);
                                }
                                break;
                            } else {
//                                lastStartIndex += 4;
                            }
                        }
                    }
                }
            } else if (showRow == 24 || showRow == 20) {
                int lastStartIndex = 0;
                for (int i = 0; i < textEmojisList.size(); i++) {
                    TextEmojiManager.TextEmoji32Item textEmoji = textEmojisList.get(i);
                    String text = textEmoji.text;

                    UXDataItem uxDataItem = listUXDataItem.get(i);
                    int textType = uxDataItem.textType;
                    byte[] textData = uxDataItem.textData;
                    List<String> imageData = uxDataItem.imageData;
                    if (" ".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
//                        allTextWidth += 0 / 3;
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(0 / 3));
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(uxDataItem.textType));
////                        allTextData.addAll(null);
                    } else {
                        byte[] staticDataTemp = staticDatas.get(i);
                        int tempLength = staticDataTemp.length;
                        int startIndex = lastStartIndex;
                        for (int j = startIndex; j <= (data.length - 3); j += 3) {
                            boolean isTheSame = true;

                            for (int m = 0; m <= (staticDataTemp.length - 3); m += 3) {
                                if (staticDataTemp[m + 0] != data[j + m + 0] || staticDataTemp[m + 1] != data[j + m + 1] || staticDataTemp[m + 2] != data[j + m + 2]) {
                                    isTheSame = false;
                                    break;
                                }
                            }

                            if (isTheSame) {
                                int emptyColumn = (j - lastStartIndex) / 3;
                                if (emptyColumn > 0) {
                                    if (textType == 0) {
                                        textData = addEmptyColumnForData24ToTheLeft(textData, emptyColumn);
                                    } else if (textType == 1) {
                                        imageData = addEmptyColumnForImageDataToTheLeft(imageData, showRow, emptyColumn);
                                    }
                                }

                                lastStartIndex = (j + tempLength);
                                tempLength = (emptyColumn * 3 + tempLength);
                                for (int n = lastStartIndex; n <= (data.length - 3); n += 3) {
                                    if (data[n + 0] == 0 && data[n + 1] == 0 && data[n + 2] == 0) {
                                        tempLength += 3;
                                        lastStartIndex += 3;
                                        if (textType == 0) {
                                            textData = addEmptyColumnForData24ToTheRight(textData, 1);
                                        } else if (textType == 1) {
                                            imageData = addEmptyColumnForImageDataToTheRight(imageData, showRow, 1);
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                if (textType == 0) {
                                    allTextWidth += tempLength / 3;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 3));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(byte2hex(textData));

                                    JTextFont textFontItem = new JTextFont();
                                    textFontItem.textWidth = tempLength / 3;
                                    textFontItem.textColor = textEmoji.color;
                                    textFontItem.showData = textData;
                                    textFont.add(textFontItem);
                                } else if (textType == 1) {
                                    allTextWidth += tempLength / 3;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 3));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(imageData);
                                }
                                break;
                            } else {
//                                lastStartIndex += 4;
                            }
                        }
                    }
                }
            } else if (showRow == 8) {
                int lastStartIndex = 0;
                for (int i = 0; i < textEmojisList.size(); i++) {
                    TextEmojiManager.TextEmoji32Item textEmoji = textEmojisList.get(i);
                    String text = textEmoji.text;

                    UXDataItem uxDataItem = listUXDataItem.get(i);
                    int textType = uxDataItem.textType;
                    byte[] textData = uxDataItem.textData;
                    List<String> imageData = uxDataItem.imageData;
                    if (" ".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
//                        allTextWidth += 0;
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(0));
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(uxDataItem.textType));
////                        allTextData.addAll(null);
                    } else {
                        byte[] staticDataTemp = staticDatas.get(i);
                        int tempLength = staticDataTemp.length;
                        int startIndex = lastStartIndex;
                        for (int j = startIndex; j <= (data.length - 1); j += 1) {
                            boolean isTheSame = true;

                            for (int m = 0; m <= (staticDataTemp.length - 1); m += 1) {
                                if (staticDataTemp[m + 0] != data[j + m + 0]) {
                                    isTheSame = false;
                                    break;
                                }
                            }

                            if (isTheSame) {
                                int emptyColumn = (j - lastStartIndex) / 1;
                                if (emptyColumn > 0) {
                                    if (textType == 0) {
                                        textData = addEmptyColumnForData8ToTheLeft(textData, emptyColumn);
                                    } else if (textType == 1) {
                                        imageData = addEmptyColumnForImageDataToTheLeft(imageData, showRow, emptyColumn);
                                    }
                                }

                                lastStartIndex = (j + tempLength);
                                tempLength = (emptyColumn * 1 + tempLength);
                                for (int n = lastStartIndex; n <= (data.length - 1); n += 1) {
                                    if (data[n + 0] == 0) {
                                        tempLength += 1;
                                        lastStartIndex += 1;
                                        if (textType == 0) {
                                            textData = addEmptyColumnForData8ToTheRight(textData, 1);
                                        } else if (textType == 1) {
                                            imageData = addEmptyColumnForImageDataToTheRight(imageData, showRow, 1);
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                if (textType == 0) {
                                    allTextWidth += tempLength / 1;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 1));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(byte2hex(textData));

                                    JTextFont textFontItem = new JTextFont();
                                    textFontItem.textWidth = tempLength;
                                    textFontItem.textColor = textEmoji.color;
                                    textFontItem.showData = textData;
                                    textFont.add(textFontItem);
                                } else if (textType == 1) {
                                    allTextWidth += tempLength / 1;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 1));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(imageData);
                                }
                                break;
                            } else {
//                                lastStartIndex += 4;
                            }
                        }
                    }
                }
            } else if (showRow == 16 || showRow == 14 || showRow == 12) {
                int lastStartIndex = 0;
                for (int i = 0; i < textEmojisList.size(); i++) {
                    TextEmojiManager.TextEmoji32Item textEmoji = textEmojisList.get(i);
                    String text = textEmoji.text;

                    UXDataItem uxDataItem = listUXDataItem.get(i);
                    int textType = uxDataItem.textType;
                    byte[] textData = uxDataItem.textData;
                    List<String> imageData = uxDataItem.imageData;

                    if (" ".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
//                        allTextWidth += 0 / 2;
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(0 / 2));
//                        allTextData.addAll(Utils.getHexListStringForWithOneByte(uxDataItem.textType));
////                        allTextData.addAll(null);
                    } else {
                        byte[] staticDataTemp = staticDatas.get(i);
                        int tempLength = staticDataTemp.length;
                        for (int j = lastStartIndex; j <= (data.length - 2); j += 2) {
                            boolean isTheSame = true;

                            for (int m = 0; m <= (staticDataTemp.length - 2); m += 2) {
                                if (staticDataTemp[m + 0] != data[j + m + 0] || staticDataTemp[m + 1] != data[j + m + 1]) {
                                    isTheSame = false;
                                    break;
                                }
                            }

                            if (isTheSame) {
                                int emptyColumn = (j - lastStartIndex) / 2;
                                if (emptyColumn > 0) {
                                    if (textType == 0) {
                                        textData = addEmptyColumnForData16ToTheLeft(textData, emptyColumn);
                                    } else if (textType == 1) {
                                        imageData = addEmptyColumnForImageDataToTheLeft(imageData, showRow, emptyColumn);
                                    }
                                }
                                lastStartIndex = (j + tempLength);
                                tempLength = (emptyColumn * 2 + tempLength);
                                int emptyColumnNumberAdded = 0;
                                for (int n = (lastStartIndex); n <= (data.length - 2); n += 2) {
                                    if (data[n + 0] == 0 && data[n + 1] == 0) {
                                        emptyColumnNumberAdded++;
                                        tempLength += 2;
                                        lastStartIndex += 2;
                                        if (textType == 0) {
                                            textData = addEmptyColumnForData16ToTheRight(textData, 1);
                                        } else if (textType == 1) {
                                            imageData = addEmptyColumnForImageDataToTheRight(imageData, showRow, 1);
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                if (textType == 0) {
                                    allTextWidth += tempLength / 2;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 2));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(byte2hex(textData));

                                    JTextFont textFontItem = new JTextFont();
                                    textFontItem.textWidth = tempLength / 2;
                                    textFontItem.textColor = textEmoji.color;
                                    textFontItem.showData = textData;
                                    textFont.add(textFontItem);
                                } else if (textType == 1) {
                                    allTextWidth += tempLength / 2;
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(tempLength / 2));
                                    allTextData.addAll(Utils.getHexListStringForWithOneByte(textType));
                                    allTextData.addAll(imageData);
                                }
//                                lastStartIndex += 2;
                                break;
                            } else {
//                                lastStartIndex += 2;
                            }
                        }
                    }
                }
            }


        }

        if (notLeftRightMode) {
            resultData.addAll(Utils.getHexListStringForIntWithTwoByte(textEmojisList.size()));
        } else {
            resultData.addAll(Utils.getHexListStringForIntWithTwoByte(textEmojis.size()));
        }
        resultData.addAll(Utils.getHexListStringForIntWithFourByte(allTextWidth));
        resultData.addAll(allTextData);
        CLog.i(TAG, "textFontSize>>>" + textFont.size());
        return textFont;
    }


    public static byte[] flip180(byte[] original, int width, int height) {
        int bytesPerColumn = (height + 7) / 8;
        byte[] flipped = new byte[original.length];

        for (int col = 0; col < width; col++) {
            int flippedCol = width - 1 - col; // 水平翻转

            for (int byteIndex = 0; byteIndex < bytesPerColumn; byteIndex++) {
                int srcIndex = col * bytesPerColumn + byteIndex;
                int destByteIndex = bytesPerColumn - 1 - byteIndex; // 垂直翻转

                int destIndex = flippedCol * bytesPerColumn + destByteIndex;

                // 同时进行上下 & bit 内部翻转
                flipped[destIndex] = reverseByteBits(original[srcIndex]);
            }
        }

        return flipped;
    }

    /**
     * 翻转一个字节的 bit 顺序（如 0b10000000 → 0b00000001）
     */
    private static byte reverseByteBits(byte b) {
        int i = b & 0xFF;
        i = ((i & 0xF0) >> 4) | ((i & 0x0F) << 4);
        i = ((i & 0xCC) >> 2) | ((i & 0x33) << 2);
        i = ((i & 0xAA) >> 1) | ((i & 0x55) << 1);
        return (byte) i;
    }

    public static List<DrawView.DrawItem> addStaticDrawItemsForCoolledux(List<DrawView.DrawItem> imageData, byte[] currentData, byte[] inputData, int column, int spacingNumber, int showRow) {
        List<DrawView.DrawItem> result = new ArrayList<>();
        int byteNumber = (showRow / 8 + ((showRow % 8) > 0 ? 1 : 0));
        if (null == currentData) {
            result = imageData;
        } else {
            int currentLength = currentData.length;
            int inputLength = inputData.length;
            int inputColumn = inputLength / byteNumber;
            int tempCurrentColumn = (currentLength / byteNumber) % column;
            if (tempCurrentColumn == 0) {
                result = imageData;
            } else {
                int currentRestColumn = column - tempCurrentColumn;
                if (currentRestColumn < (inputColumn + spacingNumber)) {
                    result = addEmptyColumnForDrawItemsToTheLeft(imageData, showRow, currentRestColumn);
                } else {
                    result = addEmptyColumnForDrawItemsToTheLeft(imageData, showRow, spacingNumber);
                }
            }
        }
        return result;
    }

    public List<DrawView.DrawItem> getDrawItemsByTextByteData(byte[] input, int column, int row, int color) {
        List<DrawView.DrawItem> result = getDrawItemsByTextByteData(input, column, row);
        for (DrawView.DrawItem drawItem : result) {
            boolean flag = Boolean.valueOf(drawItem.data);
            if (flag) {
                drawItem.color = color;
            }
        }
        return result;
    }

    public List<DrawView.DrawItem> getDrawItemsByTextByteData(byte[] input, int column, int row) {
        List<String> hexStrings = new ArrayList<>();
        hexStrings.addAll(Utils.byte2hex(input));
        List<DrawView.DrawItem> result = getDrawItems(hexStrings, column, row);
//        return getDrawItemsByRow(result, column, row);
        return result;
    }

    public List<DrawView.DrawItem> getDrawItemsByRow(List<DrawView.DrawItem> inputDrawItems, int column, int row) {
        List<DrawView.DrawItem> result = new ArrayList<>();
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                int index = j * column + i;
                DrawView.DrawItem drawItem = inputDrawItems.get(index);
                result.add(drawItem);
            }
        }
        return result;
    }

    public List<DrawView.DrawItem> getDrawItems(List<String> inputString, int column, int row) {
        List<Integer> input = new ArrayList<>();
        for (String str : inputString) {
            input.add(Integer.valueOf(str, 16));
        }
        List<DrawView.DrawItem> animationDrawItems = new ArrayList<>();
        for (int m = 0; m < column * row; m++) {
            animationDrawItems.add(new DrawView.DrawItem(String.valueOf(false)));
        }

        for (int i = 0; i < input.size(); i++) {
            int showValue = input.get(i);
            int m = (row / 8 + ((row % 8) > 0 ? 1 : 0));
            int newColumn = i / m;
            int newRow = i % m;
            if (newRow < (m - 1)) {
                for (int k = 0; k < 8; k++) {
                    int index = (newRow * 8 + k) * column + newColumn;
                    int value = (showValue << k) & 0x80;
                    animationDrawItems.get(index).data = String.valueOf(value == 0x80 ? true : false);
                }
            } else {
                int temp;
                if (row % 8 == 0) {
                    temp = 8;
                } else {
                    temp = row % 8;
                }
                for (int k = 0; k < temp; k++) {
                    int index = (newRow * 8 + k) * column + newColumn;
                    int value = (showValue << k) & 0x80;
                    animationDrawItems.get(index).data = String.valueOf(value == 0x80 ? true : false);
                }
            }
        }
        return animationDrawItems;
    }


    public List<DrawView.DrawItem> getDrawItemsByTextSizeAndBold(String text, int fontSize, boolean isBold) {
        startReadFontData(fontSize, isBold);
        byte[] temp = readFontData(text.charAt(0), fontSize);
        List<String> hexStrings = new ArrayList<>();
        hexStrings.addAll(Utils.byte2hex(temp));
        List<DrawView.DrawItem> result = getDrawItems(hexStrings, fontSize, fontSize);
        return result;
    }

    private List<List<String>> splitArr(String[] array, int num) {
        int count = array.length % num == 0 ? array.length / num : array.length / num + 1;
        List<List<String>> arrayList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = i * num;
            List<String> list = new ArrayList<>();
            int j = 0;
            while (j < num && index < array.length) {
                list.add(array[index++]);
                j++;
            }
            arrayList.add(list);
        }
        return arrayList;
    }

    public static List<byte[]> splitArray(byte[] array, int num) {
        int count = array.length % num == 0 ? array.length / num : array.length / num + 1;
        List<byte[]> arrayList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = i * num;
            byte[] list = null;
            int j = 0;
            while (j < num && index < array.length) {
                byte value = array[index];
                index++;
                byte[] tempByte = new byte[1];
                tempByte[0] = value;
                if (null == list) {
                    list = tempByte;
                } else {
                    list = concat(list, tempByte);
                }
                j++;
            }
            arrayList.add(list);
        }
        return arrayList;
    }


    public static byte[] getCenteredDataBytes(byte[] input, int textSize, int column) {
        byte[] result = null;
        List<byte[]> dataBytes = new ArrayList<>();

        if (textSize == 32) {
            dataBytes = splitArray(input, column * 4);
        } else if (textSize == 24) {
            dataBytes = splitArray(input, column * 3);
        } else if (textSize == 20) {
            dataBytes = splitArray(input, column * 3);
        } else if (textSize == 16) {
            dataBytes = splitArray(input, column * 2);
        } else if (textSize == 14) {
            dataBytes = splitArray(input, column * 2);
        } else if (textSize == 12) {
            dataBytes = splitArray(input, column * 2);
        } else if (textSize == 8) {
            dataBytes = splitArray(input, column);
        }

        List<byte[]> newDataBytes = new ArrayList<>();
        for (byte[] tempByte : dataBytes) {
            if (textSize == 32) {
                newDataBytes.add(dealDataWithCentered32(tempByte, textSize, column));
            } else if (textSize == 24) {
                newDataBytes.add(dealDataWithCentered24(tempByte, textSize, column));
            } else if (textSize == 20) {
                newDataBytes.add(dealDataWithCentered24(tempByte, textSize, column));
            } else if (textSize == 16) {
                newDataBytes.add(dealDataWithCentered16(tempByte, textSize, column));
            } else if (textSize == 14) {
                newDataBytes.add(dealDataWithCentered14(tempByte, textSize, column));
            } else if (textSize == 12) {
                newDataBytes.add(dealDataWithCentered12(tempByte, textSize, column));
            } else if (textSize == 8) {
                newDataBytes.add(dealDataWithCentered8(tempByte, textSize, column));
            }
        }

        for (byte[] tempByte : newDataBytes) {
            if (null == result) {
                result = tempByte;
            } else {
                result = concat(result, tempByte);
            }
        }
        return result;
    }

    public static byte[] dealDataWithCentered32(byte[] input, int textSize, int column) {
        byte[] data = null;
        int inputColumn = input.length / 4;
        int leftNotEmptyIndex = 0;
        int rightNotEmptyIndex = (inputColumn - 1);

        for (int i = 0; i <= (inputColumn - 1); i++) {
            if (input[4 * i] != 0 || input[4 * i + 1] != 0 || input[4 * i + 2] != 0 || input[4 * i + 3] != 0) {
                leftNotEmptyIndex = i;
                break;
            }
        }

        for (int i = (inputColumn - 1); i >= 0; i--) {
            if (input[4 * i] != 0 || input[4 * i + 1] != 0 || input[4 * i + 2] != 0 || input[4 * i + 3] != 0) {
                rightNotEmptyIndex = i;
                break;
            }
        }

//        if ((leftNotEmptyIndex == 0) && (rightNotEmptyIndex == (inputColumn - 1))) {
//            return input;
//        }

        if (leftNotEmptyIndex <= rightNotEmptyIndex) {
            for (int i = leftNotEmptyIndex; i <= rightNotEmptyIndex; i++) {
                byte[] tempByte = new byte[4];
                tempByte[0] = input[4 * i];
                tempByte[1] = input[4 * i + 1];
                tempByte[2] = input[4 * i + 2];
                tempByte[3] = input[4 * i + 3];
                if (null == data) {
                    data = tempByte;
                } else {
                    data = concat(data, tempByte);
                }
            }
        }

        return dealSegmentResult32(data, column);
    }

    public static byte[] dealDataWithCentered24(byte[] input, int textSize, int column) {
        byte[] data = null;
        int inputColumn = input.length / 3;
        int leftNotEmptyIndex = 0;
        int rightNotEmptyIndex = (inputColumn - 1);

        for (int i = 0; i <= (inputColumn - 1); i++) {
            if (input[3 * i] != 0 || input[3 * i + 1] != 0 || input[3 * i + 2] != 0) {
                leftNotEmptyIndex = i;
                break;
            }
        }

        for (int i = (inputColumn - 1); i >= 0; i--) {
            if (input[3 * i] != 0 || input[3 * i + 1] != 0 || input[3 * i + 2] != 0) {
                rightNotEmptyIndex = i;
                break;
            }
        }

//        if ((leftNotEmptyIndex == 0) && (rightNotEmptyIndex == (inputColumn - 1))) {
//            return input;
//        }

        if (leftNotEmptyIndex <= rightNotEmptyIndex) {
            for (int i = leftNotEmptyIndex; i <= rightNotEmptyIndex; i++) {
                byte[] tempByte = new byte[3];
                tempByte[0] = input[3 * i];
                tempByte[1] = input[3 * i + 1];
                tempByte[2] = input[3 * i + 2];
                if (null == data) {
                    data = tempByte;
                } else {
                    data = concat(data, tempByte);
                }
            }
        }

        return dealSegmentResult24(data, column);
    }

    public static byte[] dealDataWithCentered16(byte[] input, int textSize, int column) {
        byte[] data = null;
        int inputColumn = input.length / 2;
        int leftNotEmptyIndex = 0;
        int rightNotEmptyIndex = (inputColumn - 1);

        for (int i = 0; i <= (inputColumn - 1); i++) {
            if (input[2 * i] != 0 || input[2 * i + 1] != 0) {
                leftNotEmptyIndex = i;
                break;
            }
        }

        for (int i = (inputColumn - 1); i >= 0; i--) {
            if (input[2 * i] != 0 || input[2 * i + 1] != 0) {
                rightNotEmptyIndex = i;
                break;
            }
        }

//        if ((leftNotEmptyIndex == 0) && (rightNotEmptyIndex == (inputColumn - 1))) {
//            return input;
//        }

        if (leftNotEmptyIndex <= rightNotEmptyIndex) {
            for (int i = leftNotEmptyIndex; i <= rightNotEmptyIndex; i++) {
                byte[] tempByte = new byte[2];
                tempByte[0] = input[2 * i];
                tempByte[1] = input[2 * i + 1];
                if (null == data) {
                    data = tempByte;
                } else {
                    data = concat(data, tempByte);
                }
            }
        }
        return dealSegmentResult16(data, column);
    }

    public static byte[] dealDataWithCentered14(byte[] input, int textSize, int column) {
        byte[] data = null;
        int inputColumn = input.length / 2;
        int leftNotEmptyIndex = 0;
        int rightNotEmptyIndex = (inputColumn - 1);

        for (int i = 0; i <= (inputColumn - 1); i++) {
            if (input[2 * i] != 0 || input[2 * i + 1] != 0) {
                leftNotEmptyIndex = i;
                break;
            }
        }

        for (int i = (inputColumn - 1); i >= 0; i--) {
            if (input[2 * i] != 0 || input[2 * i + 1] != 0) {
                rightNotEmptyIndex = i;
                break;
            }
        }

//        if ((leftNotEmptyIndex == 0) && (rightNotEmptyIndex == (inputColumn - 1))) {
//            return input;
//        }

        if (leftNotEmptyIndex <= rightNotEmptyIndex) {
            for (int i = leftNotEmptyIndex; i <= rightNotEmptyIndex; i++) {
                byte[] tempByte = new byte[2];
                tempByte[0] = input[2 * i];
                tempByte[1] = input[2 * i + 1];
                if (null == data) {
                    data = tempByte;
                } else {
                    data = concat(data, tempByte);
                }
            }
        }
        return dealSegmentResult14(data, column);
    }

    public static byte[] dealDataWithCentered12(byte[] input, int textSize, int column) {
        byte[] data = null;
        int inputColumn = input.length / 2;
        int leftNotEmptyIndex = 0;
        int rightNotEmptyIndex = (inputColumn - 1);

        for (int i = 0; i <= (inputColumn - 1); i++) {
            if (input[2 * i] != 0 || input[2 * i + 1] != 0) {
                leftNotEmptyIndex = i;
                break;
            }
        }

        for (int i = (inputColumn - 1); i >= 0; i--) {
            if (input[2 * i] != 0 || input[2 * i + 1] != 0) {
                rightNotEmptyIndex = i;
                break;
            }
        }

//        if ((leftNotEmptyIndex == 0) && (rightNotEmptyIndex == (inputColumn - 1))) {
//            return input;
//        }

        if (leftNotEmptyIndex <= rightNotEmptyIndex) {
            for (int i = leftNotEmptyIndex; i <= rightNotEmptyIndex; i++) {
                byte[] tempByte = new byte[2];
                tempByte[0] = input[2 * i];
                tempByte[1] = input[2 * i + 1];
                if (null == data) {
                    data = tempByte;
                } else {
                    data = concat(data, tempByte);
                }
            }
        }
        return dealSegmentResult12(data, column);
    }

    public static byte[] dealDataWithCentered8(byte[] input, int textSize, int column) {
        byte[] data = null;
        int inputColumn = input.length;
        int leftNotEmptyIndex = 0;
        int rightNotEmptyIndex = (inputColumn - 1);

        for (int i = 0; i <= (inputColumn - 1); i++) {
            if (input[i] != 0) {
                leftNotEmptyIndex = i;
                break;
            }
        }

        for (int i = (inputColumn - 1); i >= 0; i--) {
            if (input[i] != 0) {
                rightNotEmptyIndex = i;
                break;
            }
        }

//        if ((leftNotEmptyIndex == 0) && (rightNotEmptyIndex == (inputColumn - 1))) {
//            return input;
//        }

        if (leftNotEmptyIndex <= rightNotEmptyIndex) {
            for (int i = leftNotEmptyIndex; i <= rightNotEmptyIndex; i++) {
                byte[] tempByte = new byte[1];
                tempByte[0] = input[i];
                if (null == data) {
                    data = tempByte;
                } else {
                    data = concat(data, tempByte);
                }
            }
        }
        return dealSegmentResult8(data, column);
    }
}

