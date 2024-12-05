package com.jueyuantech.glasses;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.jueyuantech.glasses.util.LogUtil;

public class AiwaysGestureManager {
    private static final boolean DEBUG = false;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private AiwaysGestureListener mListener;
    private AiwaysMultiFingerSplit mAiwaysMultiFingerSplit;
    private VelocityTracker mVelocityTracker;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private int minVelocity = 50;

    public interface AiwaysGestureListener {
        public boolean onSingleTapUp(MotionEvent motionEvent);

        public boolean onDoubleTap(MotionEvent motionEvent);

        public void onLongPress(MotionEvent e);

        public boolean onDown(MotionEvent e);

        public boolean onUp(MotionEvent motionEvent);

        public boolean onScale(ScaleGestureDetector detector);

        public boolean onScaleBegin(ScaleGestureDetector detector);

        public void onScaleEnd(ScaleGestureDetector detector);

        public boolean singleFingeronSlipProcess(MotionEvent e1, MotionEvent e2, float dx, float dy);

        public boolean singleFingerSlipAction(GestureEvent gestureEvent, MotionEvent startEvent, MotionEvent endEvent, float velocity);

        public boolean mutiFingerSlipProcess(GestureEvent gestureEvent, float startX, float startY, float endX, float endY, float moveX, float moveY);

        public boolean mutiFingerSlipAction(GestureEvent gestureEvent, float startX, float startY, float endX, float endY, float velocityX, float velocityY);
    }

    public enum GestureEvent {
        SINGLE_GINGER_LEFT_SLIP,
        SINGLE_GINGER_RIGHT_SLIP,
        SINGLE_GINGER_UP_SLIP,
        SINGLE_GINGER_DOWN_SLIP,
        DOUBLE_GINGER,
        DOUBLE_GINGER_LEFT_SLIP,
        DOUBLE_GINGER_RIGHT_SLIP,
        DOUBLE_GINGER_UP_SLIP,
        DOUBLE_GINGER_DOWN_SLIP,
        THREE_GINGER,
        THREE_GINGER_UP_SLIP,
        THREE_GINGER_DOWN_SLIP,
        SCALE_START,
        SCALE_MOVE,
        SCALE_END;
    }

    public AiwaysGestureManager(Context context, AiwaysGestureListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new SimpleGesture(), null, true /* ignoreMultitouch */);
        mGestureDetector.setOnDoubleTapListener(new AiwaysDoubleTapListener());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mAiwaysMultiFingerSplit = new AiwaysMultiFingerSplit();
        if (null == context) {
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
            mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        final boolean gestureProcessed = mGestureDetector.onTouchEvent(event);
        final boolean scaleProcessed = mScaleDetector.onTouchEvent(event);
        final boolean multiFingerProcessed = mAiwaysMultiFingerSplit.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mListener.onUp(event);
        }
        return (gestureProcessed | scaleProcessed | multiFingerProcessed);
    }

    private class SimpleGesture extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            mListener.onLongPress(e);
        }

        @Override
        public boolean onScroll(
                MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return mListener.singleFingeronSlipProcess(e1, e2, dx, dy);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (e1.getX() - e2.getX() > 0 && Math.abs((int) (e1.getX() - e2.getX())) > Math.abs((int) (e1.getY() - e2.getY()))
                    && Math.abs(velocityX) > minVelocity) {
                return mListener.singleFingerSlipAction(GestureEvent.SINGLE_GINGER_LEFT_SLIP, e1, e2, Math.abs(velocityX));
            }

            if (e1.getX() - e2.getX() < 0 && Math.abs((int) (e1.getX() - e2.getX())) > Math.abs((int) (e1.getY() - e2.getY()))
                    && Math.abs(velocityX) > minVelocity) {
                return mListener.singleFingerSlipAction(GestureEvent.SINGLE_GINGER_RIGHT_SLIP, e1, e2, Math.abs(velocityX));
            }

            if (e1.getY() - e2.getY() > 0 && Math.abs((int) (e1.getY() - e2.getY())) > Math.abs((int) (e1.getX() - e2.getX()))
                    && Math.abs(velocityY) > minVelocity) {
                return mListener.singleFingerSlipAction(GestureEvent.SINGLE_GINGER_UP_SLIP, e1, e2, Math.abs(velocityY));
            }

            if (e1.getY() - e2.getY() < 0 && Math.abs((int) (e1.getY() - e2.getY())) > Math.abs((int) (e1.getX() - e2.getX()))
                    && Math.abs(velocityY) > minVelocity) {
                return mListener.singleFingerSlipAction(GestureEvent.SINGLE_GINGER_DOWN_SLIP, e1, e2, Math.abs(velocityY));
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mListener.onDown(e);
            return super.onDown(e);
        }
    }

    private class AiwaysDoubleTapListener implements GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return mListener.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return mListener.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }
    }

    boolean notifyMutiFingerSlipProcess(MotionEvent startEvent, MotionEvent endEvent, float dX, float dY) {
        int pCount = startEvent.getPointerCount();
        int sumX = 0, sumY = 0;
        for (int i = 0; i < pCount; i++) {
            sumX += startEvent.getX(i);
            sumY += startEvent.getY(i);
        }

        int startX = sumX / pCount;
        int startY = sumY / pCount;

        sumX = sumY = 0;
        pCount = endEvent.getPointerCount();
        for (int i = 0; i < pCount; i++) {
            sumX += endEvent.getX(i);
            sumY += endEvent.getY(i);
        }

        int endX = sumX / pCount;
        int endY = sumY / pCount;
        if (pCount == 2) {
            return mListener.mutiFingerSlipProcess(GestureEvent.DOUBLE_GINGER, startX, startY, endX, endY, dX, dY);
        }

        if (pCount == 3) {
            return mListener.mutiFingerSlipProcess(GestureEvent.THREE_GINGER, startX, startY, endX, endY, dX, dY);
        }
        return false;
    }

    boolean notifyMutiFingerSlipAction(MotionEvent startEvent, MotionEvent endEvent, float velocityX, float velocityY) {
        int pCount = startEvent.getPointerCount();
        int sumX = 0, sumY = 0;
        for (int i = 0; i < pCount; i++) {
            sumX += startEvent.getX(i);
            sumY += startEvent.getY(i);
        }

        int startX = sumX / pCount;
        int startY = sumY / pCount;

        sumX = sumY = 0;
        pCount = endEvent.getPointerCount();
        for (int i = 0; i < pCount; i++) {
            sumX += endEvent.getX(i);
            sumY += endEvent.getY(i);
        }

        int endX = sumX / pCount;
        int endY = sumY / pCount;

        debug("startX:" + startX + " startY:" + startY + " endX: " + endX + " endY: " + endY + " velocityX:" + velocityX + " velocityY: " + velocityY);
        if (pCount == 2) {
            if (startX - endX > 0 && Math.abs(velocityX) > minVelocity
                    && Math.abs(startX - endX) > Math.abs(startY - endY)) {
                debug("DOUBLE_GINGER_LEFT_SLIP");
                return mListener.mutiFingerSlipAction(GestureEvent.DOUBLE_GINGER_LEFT_SLIP, startX, startY, endX, endY, velocityX, velocityY);
            }

            if (startX - endX < 0 && Math.abs(velocityX) > minVelocity
                    && Math.abs(startX - endX) > Math.abs(startY - endY)) {
                debug("DOUBLE_GINGER_RIGHT_SLIP");
                return mListener.mutiFingerSlipAction(GestureEvent.DOUBLE_GINGER_RIGHT_SLIP, startX, startY, endX, endY, velocityX, velocityY);
            }

            if (startY - endY > 0 && Math.abs(velocityY) > minVelocity
                    && Math.abs(startY - endY) > Math.abs(startX - endX)) {
                debug("DOUBLE_GINGER_UP_SLIP");
                return mListener.mutiFingerSlipAction(GestureEvent.DOUBLE_GINGER_UP_SLIP, startX, startY, endX, endY, velocityX, velocityY);
            }

            if (startY - endY < 0 && Math.abs(velocityY) > minVelocity
                    && Math.abs(startY - endY) > Math.abs(startX - endX)) {
                debug("DOUBLE_GINGER_DOWN_SLIP");
                return mListener.mutiFingerSlipAction(GestureEvent.DOUBLE_GINGER_DOWN_SLIP, startX, startY, endX, endY, velocityX, velocityY);
            }
        }
        if (pCount == 3) {
            if (startY - endY > 0 && Math.abs(velocityY) > minVelocity
                    && Math.abs(startY - endY) > Math.abs(startX - endX)) {
                debug("THREE_GINGER_UP_SLIP");
                return mListener.mutiFingerSlipAction(GestureEvent.THREE_GINGER_UP_SLIP, startX, startY, endX, endY, velocityX, velocityY);
            }

            if (startY - endY < 0 && Math.abs(velocityY) > minVelocity
                    && Math.abs(startY - endY) > Math.abs(startX - endX)) {
                debug("THREE_GINGER_DOWN_SLIP");
                return mListener.mutiFingerSlipAction(GestureEvent.THREE_GINGER_DOWN_SLIP, startX, startY, endX, endY, velocityX, velocityY);
            }
        }
        return false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            debug("onScaleBegin");
            return mListener.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            debug("onScaling");
            return mListener.onScale(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            debug("onScaleEnd");
            mListener.onScaleEnd(detector);
        }
    }

    private class AiwaysMultiFingerSplit {
        private MotionEvent mStartMutiEvent;
        private MotionEvent mLastEvent;
        private float mLastFocusX;
        private float mLastFocusY;
        private float mDownFocusX;
        private float mDownFocusY;

        public boolean onTouchEvent(MotionEvent ev) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);

            boolean handled = false;
            float sumX = 0, sumY = 0;
            float focusX = 0, focusY = 0;
            int pCount = ev.getPointerCount();
            if (pCount == 2 || pCount == 3) {
                for (int i = 0; i < pCount; i++) {
                    sumX += ev.getX(i);
                    sumY += ev.getY(i);
                }
                focusX = sumX / pCount;
                focusY = sumY / pCount;
            }

            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mDownFocusX = mLastFocusX = focusX;
                    mDownFocusY = mLastFocusY = focusY;
                    break;
                case MotionEvent.ACTION_UP:
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    if (mLastEvent != null) {
                        mLastEvent = null;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (pCount == 2 || pCount == 3) {
                        mDownFocusX = mLastFocusX = focusX;
                        mDownFocusY = mLastFocusY = focusY;
                        mStartMutiEvent = MotionEvent.obtain(ev);
                        mLastEvent = MotionEvent.obtain(ev);
                    }
                    handled = true;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    debug("ACTION_POINTER_UP_" + ev.getActionIndex() + " ,onTouchEvent pCount:" + ev.getPointerCount());
                    if ((pCount - 1) == 2 || (pCount - 1) == 3) {
                        mDownFocusX = mLastFocusX = focusX;
                        mDownFocusY = mLastFocusY = focusY;
                        mStartMutiEvent = MotionEvent.obtain(ev);
                    }

                    if (pCount == 2 || pCount == 3) {
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                        final int id1 = ev.getPointerId(ev.getActionIndex());
                        float sumVX = velocityTracker.getXVelocity(id1);
                        float sumVY = velocityTracker.getYVelocity(id1);
                        for (int i = 0; i < pCount; i++) {
                            if (i == ev.getActionIndex()) continue;

                            final int id2 = ev.getPointerId(i);
                            sumVX += velocityTracker.getXVelocity(id2);
                            sumVY += velocityTracker.getYVelocity(id2);
                        }
                        final float velocityX = sumVX / pCount;
                        final float velocityY = sumVY / pCount;
                        velocityTracker.clear();

                        handled |= notifyMutiFingerSlipAction(mLastEvent, ev, velocityX, velocityY);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (pCount == 2 || pCount == 3) {
                        final float scrollX = mLastFocusX - focusX;
                        final float scrollY = mLastFocusY - focusY;
                        if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                            handled |= notifyMutiFingerSlipProcess(mStartMutiEvent, ev, scrollX, scrollY);
                        }
                        mLastFocusX = focusX;
                        mLastFocusY = focusY;
                        handled = true;
                    }

                    break;
                default:
                    break;
            }
            return handled;
        }
    }

    private void debug(String msg) {
        if (DEBUG) LogUtil.d(msg);
    }
}
