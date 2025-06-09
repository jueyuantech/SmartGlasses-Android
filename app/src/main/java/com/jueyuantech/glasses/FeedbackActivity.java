package com.jueyuantech.glasses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.jueyuantech.glasses.util.ToastUtil;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mBackIv;

    private EditText mTitleEt;
    private EditText mContentEt;
    private Button mCommitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mTitleEt = findViewById(R.id.et_title);
        mContentEt = findViewById(R.id.et_content);
        mCommitBtn = findViewById(R.id.btn_commit);
        mCommitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_commit:
                commit();
                break;
        }
    }

    private void commit() {
        String title = mTitleEt.getEditableText().toString();
        if (TextUtils.isEmpty(title)) {
            ToastUtil.toast(FeedbackActivity.this, "Title is Empty");
            return;
        }

        String content = mContentEt.getEditableText().toString();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.toast(FeedbackActivity.this, "Content is Empty");
            return;
        }

        // TODO backend service
    }
}