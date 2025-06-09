package com.jueyuantech.glasses.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.jueyuantech.glasses.PolicyActivity;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.TermsActivity;

public class PhoneNumberLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private PhoneNumberUtil mPhoneNumberUtil;

    private ImageView mBackIv;
    private TextView mCountryCodeTv;
    private EditText mPhoneNumberEt;
    private ImageView mAgreementCb;
    private TextView mAgreementTv;
    private Button mSendCodeBtn;

    private boolean accept = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mPhoneNumberUtil = PhoneNumberUtil.getInstance();

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mAgreementCb = findViewById(R.id.cb_agreement);
        mAgreementCb.setOnClickListener(this);
        mAgreementTv = findViewById(R.id.tv_agreement);
        initAgreementTipLink(mAgreementTv);

        mSendCodeBtn = findViewById(R.id.btn_send_code);
        mSendCodeBtn.setOnClickListener(this);
        mSendCodeBtn.setEnabled(false);

        mCountryCodeTv = findViewById(R.id.tv_country_code);
        mPhoneNumberEt = findViewById(R.id.et_phone_number);
        mPhoneNumberEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSendCodeBtn.setEnabled(isPhoneNumberValid());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        setAccept(accept);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.cb_agreement:
                setAccept(!accept);
                break;
            case R.id.btn_send_code:
                sendCode();
                break;
        }
    }

    private void setAccept(boolean accept) {
        this.accept = accept;
        if (accept) {
            mAgreementCb.setImageResource(R.drawable.baseline_check_circle_24);
        } else {
            mAgreementCb.setImageResource(R.drawable.baseline_radio_button_unchecked_24);
        }
    }

    private void sendCode() {
        if (!accept) {
            showAgreementDialog();
            return;
        }

        toVerifyAct();
        finish();
    }

    private boolean isPhoneNumberValid() {
        boolean isValid = false;
        try {
            String countryCode = mCountryCodeTv.getText().toString();
            String phoneNumber = mPhoneNumberEt.getEditableText().toString();
            String regionCode = mPhoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode.replace("+", "")));

            Phonenumber.PhoneNumber phone = mPhoneNumberUtil.parse(countryCode + phoneNumber, regionCode);
            isValid = mPhoneNumberUtil.isValidNumber(phone);
        } catch (NumberParseException e) {
            e.printStackTrace();
            isValid = false;
        }
        return isValid;
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
                ds.setColor(ContextCompat.getColor(PhoneNumberLoginActivity.this, R.color.venus_green)); // 设置链接颜色
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
                ds.setColor(ContextCompat.getColor(PhoneNumberLoginActivity.this, R.color.venus_green)); // 设置链接颜色
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

    private void showAgreementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.login_agreement_dialog_title);
        builder.setMessage(R.string.login_agreement_dialog_content);
        builder.setPositiveButton(R.string.btn_sure, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (null != window) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 9;
            lp.gravity = Gravity.CENTER;
            window.setAttributes(lp);
        }
    }

    private void showTerms() {
        Intent termsIntent = new Intent(this, TermsActivity.class);
        startActivity(termsIntent);
    }

    private void showPolicy() {
        Intent policyIntent = new Intent(this, PolicyActivity.class);
        startActivity(policyIntent);
    }

    private void toVerifyAct() {
        String countryCode = mCountryCodeTv.getText().toString();
        String phoneNumber = mPhoneNumberEt.getEditableText().toString();
        Intent verifyIntent = new Intent(this, PhoneNumberVerifyActivity.class);
        verifyIntent.putExtra("country_code", countryCode);
        verifyIntent.putExtra("phone_number", phoneNumber);
        startActivity(verifyIntent);
    }
}