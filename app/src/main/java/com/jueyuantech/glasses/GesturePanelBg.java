package com.jueyuantech.glasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GesturePanelBg extends View {

    private int gridColor; // 网格颜色
    private int gridSize; // 网格大小
    private Paint paint; // 画笔

    public GesturePanelBg(Context context) {
        super(context);
        init();
    }

    public GesturePanelBg(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GesturePanelBg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        gridColor = 0x88000000; // 默认网格颜色（半透明黑色）
        gridSize = 50; // 默认网格大小
        paint = new Paint();
        paint.setColor(gridColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 绘制垂直线
        for (int i = 0; i < width; i += gridSize) {
            canvas.drawLine(i, 0, i, height, paint);
        }

        // 绘制水平线
        for (int i = 0; i < height; i += gridSize) {
            canvas.drawLine(0, i, width, i, paint);
        }
    }

    // 设置网格颜色
    public void setGridColor(int color) {
        gridColor = color;
        paint.setColor(gridColor);
        invalidate();
    }

    // 设置网格大小
    public void setGridSize(int size) {
        gridSize = size;
        invalidate();
    }
}
