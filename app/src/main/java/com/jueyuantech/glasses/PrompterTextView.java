package com.jueyuantech.glasses;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Looper;
import android.text.BidiFormatter;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrompterTextView extends View {

    private static final boolean CENTER_HORIZONTAL = false;
    private static final String DEFAULT_CONTENT = "Empty";
    private List<String> mDataList = new ArrayList<>();
    private TextPaint mTextPaint;
    private String mDefaultContent;
    private int mStartLine, mEndLine, mPreLine, mNextLine;
    private float mOffset;
    private float mTextSize;
    private float mLineSpaceHeight;

    private OverScroller mOverScroller;

    public PrompterTextView(Context context) {
        this(context, null);
    }

    public PrompterTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrompterTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mTextSize = sp2px(context, 15);
        mLineSpaceHeight = dp2px(context, 20);

        setupConfigs(context);
    }

    public void updateInit(float textSize, float lineSpace) {
        mTextSize = textSize;
        mLineSpaceHeight = lineSpace;

        setupConfigs(getContext());
    }

    private void setupConfigs(Context context) {
        mOverScroller = new OverScroller(context, new DecelerateInterpolator());
        mOverScroller.setFriction(0.1f);
        //ViewConfiguration.getScrollFriction();  //默认摩擦力 0.015f

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mDefaultContent = DEFAULT_CONTENT;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private int getContainerWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getContainerHeight() {
        return getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isDataEmpty()) {
            drawEmptyText(canvas);
            return;
        }
        mTextPaint.setTextSize(mTextSize);
        if (CENTER_HORIZONTAL) {
            mTextPaint.setTextAlign(Paint.Align.CENTER);
        } else {
            mTextPaint.setTextAlign(Paint.Align.LEFT);
        }
        float y = getContainerHeight() / 2f;
        float x = getContainerWidth() / 2f + getPaddingLeft();
        for (int i = 0; i < getDataCount(); i++) {
            if (i > 0) {
                y += (getTextHeight(i - 1) + getTextHeight(i)) / 2f + mLineSpaceHeight;
            }

            if (i >= mStartLine && i <= mEndLine) {
                mTextPaint.setColor(getResources().getColor(R.color.venus_green));
                mTextPaint.setFakeBoldText(true);
            } else if (i == mPreLine || i == mNextLine) {
                mTextPaint.setColor(getResources().getColor(R.color.venus_green_66));
                mTextPaint.setFakeBoldText(false);
            } else {
                mTextPaint.setColor(getResources().getColor(android.R.color.darker_gray));
                mTextPaint.setFakeBoldText(false);
            }

            if (CENTER_HORIZONTAL) {
                drawData(canvas, x, y, i);
            } else {
                drawData(canvas, 0, y, i);
            }
        }
    }

    private HashMap<String, StaticLayout> mDataLayoutMap = new HashMap<>();

    private void drawData(Canvas canvas, float x, float y, int i) {
        String text = mDataList.get(i);
        BidiFormatter.Builder builder = new BidiFormatter.Builder();
        builder.stereoReset(true);
        BidiFormatter formatter = builder.build();
        String formattedText = formatter.unicodeWrap(text);

        StaticLayout staticLayout = mDataLayoutMap.get(formattedText);
        if (staticLayout == null) {
            mTextPaint.setTextSize(mTextSize);
            staticLayout = new StaticLayout(formattedText, mTextPaint, getContainerWidth(),
                    Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            mDataLayoutMap.put(formattedText, staticLayout);
        }
        canvas.save();
        canvas.translate(x, y - staticLayout.getHeight() / 2f - mOffset);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    //中间空文字
    private void drawEmptyText(Canvas canvas) {
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(getResources().getColor(android.R.color.darker_gray));
        mTextPaint.setTextSize(sp2px(getContext(), 15));
        canvas.save();
        StaticLayout staticLayout = new StaticLayout(mDefaultContent, mTextPaint,
                getContainerWidth(), Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        canvas.translate(getContainerWidth() / 2f + getPaddingLeft(), getContainerHeight() / 2f);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    public void updatePtvHighlight(int start, int end, int pre, int next) {
        if (isDataEmpty()) {
            return;
        }

        if (mStartLine != start) {

            mStartLine = start;
            mEndLine = end;
            mPreLine = pre;
            mNextLine = next;

            ViewCompat.postOnAnimation(PrompterTextView.this, mScrollRunnable);
        }
    }

    private Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            scrollToPosition(mStartLine);
        }
    };

    private void scrollToPosition(int linePosition) {
        float scrollY = getItemOffsetY(linePosition);
        final ValueAnimator animator = ValueAnimator.ofFloat(mOffset, scrollY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOffset = (float) animation.getAnimatedValue();
                invalidateView();
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    private boolean isDataEmpty() {
        return mDataList == null || getDataCount() == 0;
    }

    private int getDataCount() {
        return mDataList.size();
    }

    public void setData(List<String> data) {
        resetView(DEFAULT_CONTENT);
        mDataList.addAll(data);
        invalidate();
    }

    public void resetView(String defaultContent) {
        if (mDataList != null) {
            mDataList.clear();
        }
        mDataLayoutMap.clear();
        mStaticLayoutHashMap.clear();

        mPreLine = -1;
        mStartLine = -1;
        mEndLine = -1;
        mNextLine = -1;

        mOffset = 0;
        mDefaultContent = defaultContent;
        invalidate();
    }

    private float getItemOffsetY(int linePosition) {
        float tempY = 0;
        for (int i = 1; i <= linePosition; i++) {
            tempY += (getTextHeight(i - 1) + getTextHeight(i)) / 2 + mLineSpaceHeight;
        }
        return tempY;
    }

    private HashMap<String, StaticLayout> mStaticLayoutHashMap = new HashMap<>();
    private float getTextHeight(int linePosition) {
        String text = mDataList.get(linePosition);
        StaticLayout staticLayout = mStaticLayoutHashMap.get(text);
        if (staticLayout == null) {
            mTextPaint.setTextSize(mTextSize);
            staticLayout = new StaticLayout(text, mTextPaint,
                    getContainerWidth(), Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            mStaticLayoutHashMap.put(text, staticLayout);
        }
        return staticLayout.getHeight();
    }

    private void invalidateView() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }
}