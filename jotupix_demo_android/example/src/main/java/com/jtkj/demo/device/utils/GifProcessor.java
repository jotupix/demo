package com.jtkj.demo.device.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.commom.tools.BitmapUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifIOException;

public class GifProcessor {
    private static final String TAG = "GifProcessor";

    public interface GifProcessCallback {
        void onSuccess(byte[] gifData);

        void onError(Exception e);
    }

    /**
     * 逐帧处理GIF缩放
     */
    public static void processGifFrameByFrame(Context context, int gifResId, int targetWidth, int targetHeight, GifProcessCallback callback) {
        try {
            byte[] gifData = processGifInternal(context, gifResId, targetWidth, targetHeight);
            callback.onSuccess(gifData);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private static byte[] processGifInternal(Context context, int gifResId,
                                             int targetWidth, int targetHeight) throws Exception {
        GifDrawable gifDrawable = createGifDrawable(context, gifResId);

        int frameCount = gifDrawable.getNumberOfFrames();
        List<Bitmap> frames = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();

        for (int i = 0; i < frameCount; i++) {
            gifDrawable.seekToFrame(i);

            Bitmap frameBitmap = gifDrawable.getCurrentFrame();

            Bitmap scaledBitmap = scaleBitmap(frameBitmap, targetWidth, targetHeight);

            frames.add(scaledBitmap);

            int delay = gifDrawable.getDuration() / frameCount;
            delays.add(delay);

            if (frameBitmap != scaledBitmap) {
                frameBitmap.recycle();
            }
        }

        byte[] gifData = encodeFramesToGif(frames, delays.get(0));

        for (Bitmap frame : frames) {
            frame.recycle();
        }
        gifDrawable.recycle();

        return gifData;
    }

    private static GifDrawable createGifDrawable(Context context, int gifResId) throws IOException {
        try {
            return new GifDrawable(context.getResources(), gifResId);
        } catch (GifIOException e) {
            InputStream is = context.getResources().openRawResource(gifResId);
            try {
                return new GifDrawable(is);
            } finally {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static Bitmap scaleBitmap(@NonNull Bitmap original, int targetWidth, int targetHeight) {
        Bitmap resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);

        Matrix matrix = new Matrix();

        float scaleX = (float) targetWidth / original.getWidth();
        float scaleY = (float) targetHeight / original.getHeight();

        float scale = Math.max(scaleX, scaleY);

        matrix.setScale(scale, scale);

        float dx = (targetWidth - original.getWidth() * scale) / 2;
        float dy = (targetHeight - original.getHeight() * scale) / 2;
        matrix.postTranslate(dx, dy);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);

        canvas.drawBitmap(original, matrix, paint);

        return resultBitmap;
    }

    private static byte[] encodeFramesToGif(List<Bitmap> bitmaps, int delay) {
        byte[] data = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
            gifEncoder.start(baos);
            gifEncoder.setRepeat(0); // 0 means loop indefinitely
            gifEncoder.setDelay(delay); // frame delay in milliseconds
            gifEncoder.setQuality(1);

            for (Bitmap bitmap : bitmaps) {
                gifEncoder.addFrame(bitmap);
            }

            gifEncoder.finish();
            data = baos.toByteArray();
            baos.flush();
            baos.close();
            BitmapUtil.releaseLisBitmap(bitmaps);
        } catch (Exception exception) {
            exception.printStackTrace();
            CLog.i(TAG, "generateGif>>>>exception>>>" + exception.getMessage());
        }
        return data;
    }
}