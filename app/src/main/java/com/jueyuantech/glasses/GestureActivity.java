package com.jueyuantech.glasses;

import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;

public class GestureActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mBackIv;
    GesturePanelBg mGesturePanelBg;
    AiwaysGestureManager aiwaysGestureManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        aiwaysGestureManager = new AiwaysGestureManager(this, listener);

        mGesturePanelBg = findViewById(R.id.v_panel);
        mGesturePanelBg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                aiwaysGestureManager.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            default:
        }
    }

    private AiwaysGestureManager.AiwaysGestureListener listener = new AiwaysGestureManager.AiwaysGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            ToastUtil.toast(GestureActivity.this, R.string.title_touch_pad_event_single);
            VNCommon.sendTouchEvent(VNConstant.TouchEvent.SINGLE_TAP);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            ToastUtil.toast(GestureActivity.this, R.string.title_touch_pad_event_double);
            VNCommon.sendTouchEvent(VNConstant.TouchEvent.DOUBLE_TAP);
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            ToastUtil.toast(GestureActivity.this, R.string.title_touch_pad_event_long);
            VNCommon.sendTouchEvent(VNConstant.TouchEvent.LONG_PRESS);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }

        @Override
        public boolean singleFingeronSlipProcess(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return false;
        }

        @Override
        public boolean singleFingerSlipAction(AiwaysGestureManager.GestureEvent gestureEvent, MotionEvent startEvent, MotionEvent endEvent, float velocity) {
            switch (gestureEvent) {
                case SINGLE_GINGER_LEFT_SLIP:
                    ToastUtil.toast(GestureActivity.this, R.string.title_touch_pad_event_slip_left);
                    VNCommon.sendTouchEvent(VNConstant.TouchEvent.SLIP_LEFT);
                    break;
                case SINGLE_GINGER_RIGHT_SLIP:
                    ToastUtil.toast(GestureActivity.this, R.string.title_touch_pad_event_slip_right);
                    VNCommon.sendTouchEvent(VNConstant.TouchEvent.SLIP_RIGHT);
                    break;
                case SINGLE_GINGER_UP_SLIP:
                    ToastUtil.toast(GestureActivity.this, R.string.title_touch_pad_event_slip_up);
                    VNCommon.sendTouchEvent(VNConstant.TouchEvent.SLIP_UP);
                    break;
                case SINGLE_GINGER_DOWN_SLIP:
                    ToastUtil.toast(GestureActivity.this, R.string.title_touch_pad_event_slip_down);
                    VNCommon.sendTouchEvent(VNConstant.TouchEvent.SLIP_DOWN);
                    break;
                default:
            }
            return false;
        }

        @Override
        public boolean mutiFingerSlipProcess(AiwaysGestureManager.GestureEvent gestureEvent, float startX, float startY, float endX, float endY, float moveX, float moveY) {
            return false;
        }

        @Override
        public boolean mutiFingerSlipAction(AiwaysGestureManager.GestureEvent gestureEvent, float startX, float startY, float endX, float endY, float velocityX, float velocityY) {
            return false;
        }
    };
}