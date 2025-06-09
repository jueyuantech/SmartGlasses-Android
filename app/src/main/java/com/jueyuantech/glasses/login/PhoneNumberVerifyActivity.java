package com.jueyuantech.glasses.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.util.ToastUtil;

public class PhoneNumberVerifyActivity extends AppCompatActivity implements View.OnClickListener {

    private Gson gson = new Gson();
    private ImageView mBackIv;
    private TextView mSentToTv;
    private EditText mCodeEt;
    private TextView mCountDownTv;
    private Button mLoginBtn;

    private String mCountryCodeStr;
    private String mPhoneNumberStr;
    private String mCodeUUID;
    private int mCurCountDown = 60;
    private static final int MSG_COUNT_DOWN = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_COUNT_DOWN:
                    if (mCurCountDown > 0) {
                        mCurCountDown--;
                        mHandler.sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);
                    }
                    updateCountDownView();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_verify);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mCountryCodeStr = getIntent().getStringExtra("country_code");
        mPhoneNumberStr = getIntent().getStringExtra("phone_number");

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mSentToTv = findViewById(R.id.tv_sent_to);
        mSentToTv.setText(getString(R.string.phone_number_verify_sent_to, mCountryCodeStr, mPhoneNumberStr));

        mCountDownTv = findViewById(R.id.tv_count_down);
        mCountDownTv.setOnClickListener(this);

        mLoginBtn = findViewById(R.id.btn_login);
        mLoginBtn.setOnClickListener(this);

        mCodeEt = findViewById(R.id.et_code);
        mCodeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLoginBtn.setEnabled(6 == s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendVerifyCode();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopCountDown();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_count_down:
                if (0 == mCurCountDown) {
                    sendVerifyCode();
                } else {
                    ToastUtil.toast(PhoneNumberVerifyActivity.this, getString(R.string.phone_number_verify_count_down, mCurCountDown));
                }
                break;
            case R.id.btn_login:
                login();
                break;
        }
    }

    private void startCountDown() {
        mCurCountDown = 60;
        mHandler.sendEmptyMessage(MSG_COUNT_DOWN);
    }

    private void stopCountDown() {
        mHandler.removeMessages(MSG_COUNT_DOWN);
        mCurCountDown = 0;
        mCountDownTv.setEnabled(true);
        mCountDownTv.setText(R.string.phone_number_verify_send_retry);
    }

    private void updateCountDownView() {
        if (mCurCountDown > 0) {
            mCountDownTv.setEnabled(false);
            mCountDownTv.setText(getString(R.string.phone_number_verify_count_down, mCurCountDown));
        } else {
            mCountDownTv.setEnabled(true);
            mCountDownTv.setText(R.string.phone_number_verify_send_retry);
        }
    }

    private void sendVerifyCode() {
        // TODO Image CAPTCHA

        // TODO backend service

        startCountDown();

        if (null != mCodeEt) {
            mCodeEt.requestFocus();
        }
    }

    private void login() {
        String codeStr = mCodeEt.getEditableText().toString();
        if (TextUtils.isEmpty(codeStr) || 6 != codeStr.length()) {
            ToastUtil.toast(PhoneNumberVerifyActivity.this, getString(R.string.phone_number_verify_code_invalid));
            return;
        }

        // TODO backend service
    }

    public void fetchUserInfo() {
        showProgressDialog(getString(R.string.phone_number_verify_syncing));

        // TODO backend service
    }

    private ProgressDialog mProgressDialog;

    private void showProgressDialog(String message) {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        mProgressDialog.setMessage(message);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();

            WindowManager.LayoutParams lp = mProgressDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mProgressDialog.getWindow().setAttributes(lp);
        }
    }

    private void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}