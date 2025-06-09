package com.jueyuantech.glasses.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;

public class AppUtil {

    public static Bitmap getAppIconFromLayerDrawable(Context context, String packageName) {
        if (context == null) {
            return null;
        }

        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            //String packageName = context.getApplicationContext().getPackageName();
            Drawable drawable = packageManager.getApplicationIcon(packageName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable) drawable).getBitmap();
                } else if (drawable instanceof AdaptiveIconDrawable) {
                    Drawable[] drr = new Drawable[2];
                    drr[0] = ((AdaptiveIconDrawable) drawable).getBackground();
                    drr[1] = ((AdaptiveIconDrawable) drawable).getForeground();

                    LayerDrawable layerDrawable = new LayerDrawable(drr);
                    int width = layerDrawable.getIntrinsicWidth();
                    int height = layerDrawable.getIntrinsicHeight();

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    layerDrawable.draw(canvas);
                    return bitmap;
                }
            } else {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getAppIcon(Context context, String packageName) {
        if (context == null) {
            return null;
        }

        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            //String packageName = context.getApplicationContext().getPackageName();
            Drawable drawable = packageManager.getApplicationIcon(packageName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable) drawable).getBitmap();
                } else if (drawable instanceof AdaptiveIconDrawable) {
                    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                    return bitmap;
                }
            } else {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
