package com.jueyuantech.glasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private AlertDialog.Builder agreementDialogBuilder;
    private Dialog agreementDialog;

    private static final int MSG_TO_MAIN = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_TO_MAIN:
                    MyApplication.initApp();
                    toHomeAct();
                    finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        if (MyApplication.isAgreementAccepted()) {
            handler.sendEmptyMessageDelayed(MSG_TO_MAIN, 500);
        } else {
            showAgreementDialog();
        }
    }

    private void showAgreementDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_agreement, null);
        TextView contentTv = dialogView.findViewById(R.id.tv_content);
        initAgreementTipLink(contentTv);
        Button positiveBtn = dialogView.findViewById(R.id.btn_positive);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });
        Button negativeBtn = dialogView.findViewById(R.id.btn_negative);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reject();
            }
        });

        agreementDialogBuilder = new AlertDialog.Builder(this);
        agreementDialogBuilder.setView(dialogView);
        agreementDialog = agreementDialogBuilder.create();

        agreementDialog.setCancelable(false);
        agreementDialog.setCanceledOnTouchOutside(false);
        agreementDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    //TODO
                    return true;
                }
                return false;
            }
        });

        agreementDialog.show();

        Window window = agreementDialog.getWindow();
        if (null != window) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 10;
            lp.gravity = Gravity.BOTTOM;
            window.setAttributes(lp);
            //设置自身的底板透明
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //设置dialog周围activity背景的透明度，[0f,1f]，0全透明，1不透明黑
            window.setDimAmount(0.1f);
        }
    }

    private void dismissAgreementDialog() {
        if (null != agreementDialog) {
            agreementDialog.dismiss();
        }
    }

    private void initAgreementTipLink(TextView textView) {
        String text = textView.getText().toString();
        final SpannableString textWithLink = new SpannableString(text);

        int termsStartIdx = text.indexOf("《");
        int termsEndIdx = text.indexOf("》");
        int policyStartIdx = text.indexOf("《", termsStartIdx + 1);
        int policyEndIdx = text.indexOf("》", termsEndIdx + 1);

        if (termsStartIdx == -1 || termsEndIdx == -1 || policyStartIdx == -1 || policyEndIdx == -1) {
            return;
        }

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showTerms();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(SplashActivity.this, R.color.venus_green)); // 设置链接颜色
                ds.setUnderlineText(true);
            }
        };

        ClickableSpan policySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showPolicy();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(SplashActivity.this, R.color.venus_green)); // 设置链接颜色
                ds.setUnderlineText(true);
            }
        };

        textWithLink.setSpan(termsSpan, termsStartIdx, termsEndIdx + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textWithLink.setSpan(policySpan, policyStartIdx, policyEndIdx + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(textWithLink);
        textView.setMovementMethod(new LinkMovementMethod() {
            @Override
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Selection.removeSelection(buffer);
                }
                return super.onTouchEvent(widget, buffer, event);
            }
        });
    }

    private void showTerms() {
        Intent termsIntent = new Intent(this, TermsActivity.class);
        startActivity(termsIntent);
    }

    private void showPolicy() {
        Intent policyIntent = new Intent(this, PolicyActivity.class);
        startActivity(policyIntent);
    }

    private void accept() {
        MyApplication.acceptAgreement();
        dismissAgreementDialog();
        handler.sendEmptyMessage(MSG_TO_MAIN);
    }

    private void reject() {
        dismissAgreementDialog();
        finish();
    }

    private void toMainAct() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    private void toHomeAct() {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
    }
}