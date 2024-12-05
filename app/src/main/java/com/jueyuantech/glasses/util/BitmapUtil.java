package com.jueyuantech.glasses.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapUtil {


    private static double maxDistance = colourDistance(Color.BLACK, Color.WHITE);
    private static double maxVenus = 0xFF;

    public static Bitmap bitmap2GrayBitmap(Bitmap bmSrc) {
        // 得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }

    /**
     * 提高图片亮度
     *
     * @param bitmap
     * @return
     */
    public static Bitmap bitmap2LightBitmap(Bitmap bitmap) {
        //得到图像的宽度和长度
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //依次循环对图像的像素进行处理
        final float[] a = new float[20];
        a[0] = a[6] = a[12] = a[18] = 1;
        a[4] = a[9] = a[14] = 50;
        ColorMatrix cm = new ColorMatrix(a);
        Paint paint = new Paint();
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        Canvas c = new Canvas(outputBitmap);
        c.drawBitmap(bitmap, 0, 0, paint);
        return outputBitmap;
    }

    public static Bitmap grayBitmap2BinaryBitmap(Bitmap graymap, boolean isReverse) {
        //得到图形的宽度和长度
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        //创建二值化图像
        Bitmap binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到当前像素的值
                int col = binarymap.getPixel(i, j);
                //得到alpha通道的值
                int alpha = Color.alpha(col);
                //得到图像的像素RGB的值
                int red = Color.red(col);
                int green = Color.green(col);
                int blue = Color.blue(col);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                //int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                int gray = (red * 38 + green * 75 + blue * 15) >> 7;  //降低浮点运算

                //对图像进行二值化处理
                if (gray > 128) {
                    gray = isReverse ? 0xFF000000 : 0xFFFFFFFF;
                } else {
                    gray = isReverse ? 0xFFFFFFFF : 0xFF000000;
                }
                //设置新图像的当前像素值
                binarymap.setPixel(i, j, gray);
            }
        }
        return binarymap;
    }

    /**
     * 平均灰度算法获取二值图
     *
     * @param srcBitmap 图像像素数组地址（ ARGB 格式）
     * @return Bitmap
     */
    public static Bitmap grayAverageBitmap2BinaryBitmap(Bitmap srcBitmap) {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        double pixel_total = width * height; // 像素总数
        if (pixel_total == 0) return null;

        Bitmap bitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true);

        long sum = 0;  // 总灰度
        int threshold = 0;    // 阈值

        for (int i = 0; i < pixel_total; i++) {

            int x = i % width;
            int y = i / width;

            int pixel = bitmap.getPixel(x, y);

            // 分离三原色及透明度
            int alpha = Color.alpha(pixel);
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            int gray = (red * 38 + green * 75 + blue * 15) >> 7;
            if (alpha == 0 && gray == 0) {
                gray = 0xFF;
            }

            if (gray > 0xFF) {
                gray = 0xFF;
            }
            bitmap.setPixel(x, y, gray | 0xFFFFFF00);
            sum += gray;
        }
        // 计算平均灰度
        threshold = (int) (sum / pixel_total);

        for (int i = 0; i < pixel_total; i++) {

            int x = i % width;
            int y = i / width;
            int pixel = bitmap.getPixel(x, y) & 0x000000FF;
            int color = pixel <= threshold ? 0xFF000000 : 0xFFFFFFFF;
            bitmap.setPixel(x, y, color);
        }
        return bitmap;
    }

    /**
     * OTSU 算法获取二值图
     *
     * @param srcBitmap 图像像素数组地址（ ARGB 格式）
     * @return 二值图像素数组地址
     */
    public static Bitmap bitmap2OTSUBitmap(Bitmap srcBitmap) {

        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        double pixel_total = width * height; // 像素总数
        if (pixel_total == 0) return null;

        Bitmap bitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true);


        long sum1 = 0;  // 总灰度值
        long sumB = 0;  // 背景总灰度值
        double wB = 0.0;        // 背景像素点比例
        double wF = 0.0;        // 前景像素点比例
        double mB = 0.0;        // 背景平均灰度值
        double mF = 0.0;        // 前景平均灰度值
        double max_g = 0.0;     // 最大类间方差
        double g = 0.0;         // 类间方差
        int threshold = 0;    // 阈值
        double[] histogram = new double[256];// 灰度直方图，下标是灰度值，保存内容是灰度值对应的像素点总数

        // 获取灰度直方图和总灰度
        for (int i = 0; i < pixel_total; i++) {
            int x = i % width;
            int y = i / width;
            int pixel = bitmap.getPixel(x, y);
            // 分离三原色及透明度
            int alpha = Color.alpha(pixel);
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            int gray = (red * 38 + green * 75 + blue * 15) >> 7;
            if (alpha == 0 && gray == 0) {
                gray = 0xFF;
            }
            if (gray > 0xFF) {
                gray = 0xFF;
            }
            bitmap.setPixel(x, y, gray | 0xFFFFFF00);

            // 计算灰度直方图分布，Histogram 数组下标是灰度值，保存内容是灰度值对应像素点数
            histogram[gray]++;
            sum1 += gray;
        }

        // OTSU 算法
        for (int i = 0; i < 256; i++) {
            wB = wB + histogram[i]; // 这里不算比例，减少运算，不会影响求 T
            wF = pixel_total - wB;
            if (wB == 0 || wF == 0) {
                continue;
            }
            sumB = (long) (sumB + i * histogram[i]);
            mB = sumB / wB;
            mF = (sum1 - sumB) / wF;
            g = wB * wF * (mB - mF) * (mB - mF);
            if (g >= max_g) {
                threshold = i;
                max_g = g;
            }
        }

        for (int i = 0; i < pixel_total; i++) {
            int x = i % width;
            int y = i / width;
            int pixel = bitmap.getPixel(x, y) & 0x000000FF;
            int color = pixel <= threshold ? 0xFF000000 : 0xFFFFFFFF;
            bitmap.setPixel(x, y, color);
        }

        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap scaleImage(Bitmap bmp, int newWidth, int newHeight) {
        if (bmp == null) {
            return null;
        }
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        /*
        if (bmp != null & !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        */
        return newBmp;
    }

    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static Bitmap makeBlackPixelsTransparent(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = result.getPixel(x, y);
                // 检查像素是否为纯黑色（0, 0, 0）
                if (Color.red(pixel) == 0 && Color.green(pixel) == 0 && Color.blue(pixel) == 0) {
                    // 设置像素为透明
                    result.setPixel(x, y, Color.TRANSPARENT);
                }
            }
        }
        return result;
    }

    public static Bitmap greenGray(Bitmap bitmap) {
        if (bitmap == null) return null;
        Bitmap greenBmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int greenBmpW = greenBmp.getWidth();
        int greenBmpH = greenBmp.getHeight();
        LogUtil.i("greenBmpW = " + greenBmpW + " greenBmpH = " + greenBmpH);
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < greenBmpW; i++) {
            for (int j = 0; j < greenBmpH; j++) {
                //得到当前像素的值
                int col = greenBmp.getPixel(i, j);
                //得到alpha通道的值
                int alpha = Color.alpha(col);
                //得到图像的像素RGB的值
                int red = Color.red(col);
                int green = Color.green(col);
                int blue = Color.blue(col);

                //LogUtil.i("alpha = " + alpha + " red = " + red + " green = " + green + " blue = " + blue);

                greenBmp.setPixel(i, j, Color.argb(alpha, 0, (255 - (int) getVenusColor(colourDistance(col, Color.WHITE))), 0));
            }
        }
        return greenBmp;
    }

    public static double colourDistance(int e1, int e2) {
        long rmean = ((long) Color.red(e1) + (long) Color.red(e2)) / 2;
        long r = (long) Color.red(e1) - (long) Color.red(e2);
        long g = (long) Color.green(e1) - (long) Color.green(e2);
        long b = (long) Color.blue(e1) - (long) Color.blue(e2);
        return Math.sqrt(
                (((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8)
        );
    }

    public static double getVenusColor(double distance) {
        return distance / maxDistance * 0xFF;
    }

    public static Bitmap getVenusBmp(Bitmap srcBmp, int scaleW, int scaleH) {
        if (null == srcBmp) {
            return null;
        }

        Bitmap resultBmp = srcBmp.copy(Bitmap.Config.ARGB_8888, true);

        // STEP 1: scale
        if (scaleW > 0 && scaleH >0) {
            resultBmp = scaleImage(resultBmp, scaleW, scaleH);
        }

        // STEP 2: gray
        resultBmp = bitmap2GrayBitmap(resultBmp);

        // STEP 3: remove black
        resultBmp = makeBlackPixelsTransparent(resultBmp);

        // STEP 4: green
        resultBmp = greenGray(resultBmp);
        return resultBmp;
    }
}