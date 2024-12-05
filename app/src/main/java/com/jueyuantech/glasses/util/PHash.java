package com.jueyuantech.glasses.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

import java.io.IOException;

public class PHash {
    /**
     * pHash算法流程
     * 1.缩小图片，最佳大小为32*32
     * 2.转化成灰度图
     * 3.转化为DCT图
     * 4.取dct图左上角8*8的范围
     * 5.计算所有点的平均值
     * 6.8*8的范围刚好64个点，计算出64位的图片指纹，如果小于平均值记为0，反之记为1，指纹顺序可以随机，但是每张图片的指纹的顺序应该保持一致
     * 7.最后比较两张图片指纹的汉明距离，越小表示越相识
     */

    /**
     * pHash用法
     *
     * val dctImageHash1 = pHash.dctImageHash(bitmap1, true)
     * val dctImageHash2 = pHash.dctImageHash(bitmap2, true)
     *
     * val distance = pHash.hammingDistance(dctImageHash1, dctImageHash2)
     * Log.d(TAG, "dis2 =  $distance")
     */

    //获取指纹，long刚好64位，方便存放
    public static long dctImageHash(Bitmap src, boolean recycle) throws IOException {
        //由于计算dct需要图片长宽相等，所以统一取32
        int length = 32;

        //缩放图片
        Bitmap bitmap = scaleBitmap(src, recycle, length);

        //获取灰度图
        int[] pixels = createGrayImage(bitmap, length);

        //先获得32*32的dct，再取dct左上角8*8的区域
        return computeHash(DCT8(pixels, length));
    }

    private static int[] createGrayImage(Bitmap src, int length) {
        int[] pixels = new int[length * length];
        src.getPixels(pixels, 0, length, 0, 0, length, length);
        src.recycle();
        for (int i = 0; i < pixels.length; i++) {
            int gray = computeGray(pixels[i]);
            pixels[i] = Color.rgb(gray, gray, gray);
        }
        return pixels;
    }

    //缩放成宽高一样的图片
    private static Bitmap scaleBitmap(Bitmap src, boolean recycle, float length) throws IOException {
        if (src == null) {
            throw new IOException("invalid image");
        }
        int width = src.getWidth();
        int height = src.getHeight();
        if (width == 0 || height == 0) {
            throw new IOException("invalid image");
        }
        Matrix matrix = new Matrix();
        matrix.postScale(length / width, length / height);
        Bitmap bitmap = Bitmap.createBitmap(src, 0, 0, width, height, matrix, false);
        if (recycle) {
            src.recycle();
        }
        return bitmap;
    }

    //计算hash值
    private static long computeHash(double[] pxs) {
        double t = 0;
        for (double i : pxs) {
            t += i;
        }
        double median = t / pxs.length;
        long one = 0x0000000000000001;
        long hash = 0x0000000000000000;
        for (double current : pxs) {
            if (current > median)
                hash |= one;
            one = one << 1;
        }
        return hash;
    }

    /**
     * 计算灰度值
     * 计算公式Gray = R*0.299 + G*0.587 + B*0.114
     * 由于浮点数运算性能较低，转换成位移运算
     * 向右每位移一位，相当于除以2
     */
    private static int computeGray(int pixel) {
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        return (red * 38 + green * 75 + blue * 15) >> 7;
    }

    //取dct图左上角8*8的区域
    private static double[] DCT8(int[] pix, int n) {
        double[][] iMatrix = DCT(pix, n);

        double px[] = new double[8 * 8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(iMatrix[i], 0, px, i * 8, 8);
        }
        return px;
    }

    /**
     * 离散余弦变换
     * <p>
     * 计算公式为：系数矩阵*图片矩阵*转置系数矩阵
     *
     * @param pix 原图像的数据矩阵
     * @param n   原图像(n*n)
     * @return 变换后的矩阵数组
     */
    private static double[][] DCT(int[] pix, int n) {
        double[][] iMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                iMatrix[i][j] = (double) (pix[i * n + j]);
            }
        }
        double[][] quotient = coefficient(n);   //求系数矩阵
        double[][] quotientT = transposingMatrix(quotient, n);  //转置系数矩阵

        double[][] temp;
        temp = matrixMultiply(quotient, iMatrix, n);
        iMatrix = matrixMultiply(temp, quotientT, n);
        return iMatrix;
    }

    /**
     * 矩阵转置
     *
     * @param matrix 原矩阵
     * @param n      矩阵(n*n)
     * @return 转置后的矩阵
     */
    private static double[][] transposingMatrix(double[][] matrix, int n) {
        double nMatrix[][] = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                nMatrix[i][j] = matrix[j][i];
            }
        }
        return nMatrix;
    }

    /**
     * 求离散余弦变换的系数矩阵
     *
     * @param n n*n矩阵的大小
     * @return 系数矩阵
     */
    private static double[][] coefficient(int n) {
        double[][] coeff = new double[n][n];
        double sqrt = Math.sqrt(1.0 / n);
        double sqrt1 = Math.sqrt(2.0 / n);
        for (int i = 0; i < n; i++) {
            coeff[0][i] = sqrt;
        }
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < n; j++) {
                coeff[i][j] = sqrt1 * Math.cos(i * Math.PI * (j + 0.5) / n);
            }
        }

        return coeff;
    }

    /**
     * 矩阵相乘
     *
     * @param A 矩阵A
     * @param B 矩阵B
     * @param n 矩阵的大小n*n
     * @return 结果矩阵
     */
    private static double[][] matrixMultiply(double[][] A, double[][] B, int n) {
        double nMatrix[][] = new double[n][n];
        double t;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                t = 0;
                for (int k = 0; k < n; k++) {
                    t += A[i][k] * B[k][j];
                }
                nMatrix[i][j] = t;
            }
        }
        return nMatrix;
    }

    /**
     * 计算两个图片指纹的汉明距离
     *
     * @param hash1 指纹1
     * @param hash2 指纹2
     * @return 返回汉明距离 也就是64位long型不相同的位的个数
     */
    public static int hammingDistance(long hash1, long hash2) {
        long x = hash1 ^ hash2;
        final long m1 = 0x5555555555555555L;
        final long m2 = 0x3333333333333333L;
        final long h01 = 0x0101010101010101L;
        final long m4 = 0x0f0f0f0f0f0f0f0fL;
        x -= (x >> 1) & m1;
        x = (x & m2) + ((x >> 2) & m2);
        x = (x + (x >> 4)) & m4;
        return (int) ((x * h01) >> 56);
    }
}
