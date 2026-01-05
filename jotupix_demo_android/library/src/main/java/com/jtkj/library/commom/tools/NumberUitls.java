package com.jtkj.library.commom.tools;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * yfxiong
 * 时间    2022/6/11 16:43
 * 文件    cooled1248
 * 描述
 */
public class NumberUitls {

    /**求百分比
     * ：使用java.text.DecimalFormat实现
     * @param x
     * @param y
     * @return
     */
    public static String getPercent(int x, int y) {
        double d1 = x * 1.0;
        double d2 = y * 1.0;
        // 设置保留几位小数， “.”后面几个零就保留几位小数，这里设置保留四位小数
//        DecimalFormat decimalFormat = new DecimalFormat("##.0000%");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat("##%",symbols);
        return decimalFormat.format(d1 / d2);
    }
}
