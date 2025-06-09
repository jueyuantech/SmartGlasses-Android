package com.jueyuantech.glasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Author : lixuyang
 * @Time : On 2022/10/10 9:51
 * @Description : SwitchButton
 */
public class SwitchButton extends View {
    private final Paint mPaint = new Paint();
    private static final double MBTNHEIGHT = 0.55;
    private static final int OFFSET = 5;
    private int mHeight;
    private float mAnimate = 0L;
    private boolean touchable = false;
    private boolean checked = false;
    private float mScale;
    private int mSelectColor;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        /*
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);
        mSelectColor = typedArray.getColor(R.styleable.SwitchButton_buttonColor, Color.parseColor("#254AA1"));
        typedArray.recycle();
        */
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec 高度是是宽度的0.55倍
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = (int) (MBTNHEIGHT * width);
        setMeasuredDimension(width, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        //mPaint.setColor(Color.parseColor("#254AA1"));
        mPaint.setColor(getResources().getColor(R.color.venus_green));
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        RectF rectf = new RectF(rect);
        //绘制圆角矩形
        canvas.drawRoundRect(rectf, mHeight / 2, mHeight / 2, mPaint);

        //以下save和restore很重要，确保动画在中间一层
        canvas.save();
        //mPaint.setColor(Color.parseColor("#ADB2CB"));
        mPaint.setColor(getResources().getColor(R.color.bg_main_gradient_start));
        // 动画标示重绘10次
        mAnimate = mAnimate - 0.1f > 0 ? mAnimate - 0.1f : 0;
        mScale = (!checked ? 1 - mAnimate : mAnimate);
        canvas.scale(mScale, mScale, getWidth() - getHeight() / 2, rect.centerY());
        //绘制缩放的灰色圆角矩形
        canvas.drawRoundRect(rectf, mHeight / 2, mHeight / 2, mPaint);

        //mPaint.setColor(Color.parseColor("#ADB2CB"));
        mPaint.setColor(getResources().getColor(R.color.bg_main_gradient_end));
        Rect mRectInner = new Rect(OFFSET, OFFSET, getWidth() - OFFSET, getHeight() - OFFSET);
        RectF mRectoInaner = new RectF(mRectInner);
        //绘制缩放的白色圆角矩形，和上边的重叠实现灰色边框效果
        canvas.drawRoundRect(mRectoInaner, (mHeight - 8) / 2, (mHeight - 8) / 2, mPaint);
        canvas.restore();

        //中间圆形平移
        int sWidth = getWidth();
        int bTranslateX = sWidth - getHeight();
        final float translate = bTranslateX * (!checked ? mAnimate : 1 - mAnimate);
        canvas.translate(translate, 0);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getHeight() / 2, getHeight() / 2, getHeight() / 2 - OFFSET, mPaint);

        if (mScale > 0) {
            mPaint.reset();
            invalidate();
        }
    }

    /**
     * 事件分发
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mAnimate = 1;
                    checked = !checked;

                    if (mOnCheckedChangeListener != null) {
                        mOnCheckedChangeListener.onCheckedChanged(checked);
                    }
                    invalidate();
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    public boolean isTouchable() {
        return touchable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    /**
     * 状态构造函数
     *
     * @return
     */
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    /**
     * 构造函数
     *
     * @return
     */
    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return mOnCheckedChangeListener;
    }

    /**
     * 调用方法
     *
     * @param listener
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener {
        /**
         * 滑动接口
         *
         * @param isChecked 是否
         */
        void onCheckedChanged(boolean isChecked);
    }

}
