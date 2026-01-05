package com.jtkj.library.commom.tools;

import android.graphics.Bitmap;

import java.util.List;

/**
 * yfxiong
 * 时间    7/1/24 17:23
 * 文件    cooled1248
 * 描述
 */
public class BitmapUtil {

    public static void releaseLisBitmap(List<Bitmap> bitmapList) {
        for (Bitmap bitmap : bitmapList) {
            releaseBitmap(bitmap);
        }
    }

    public static void releaseBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
