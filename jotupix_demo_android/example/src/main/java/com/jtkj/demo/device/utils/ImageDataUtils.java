package com.jtkj.demo.device.utils;

import android.content.Context;
import android.content.res.Resources;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageDataUtils {

    public static byte[] getImageBytesFromResource(Context context, int resId) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;

        try {
            Resources resources = context.getResources();
            inputStream = resources.openRawResource(resId);
            outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] getImageBytesFromResource2(Context context, int resId) {
        Resources resources = context.getResources();

        try (InputStream inputStream = resources.openRawResource(resId);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}