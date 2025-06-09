package com.jueyuantech.glasses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;

public class TermsActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mBackIv;
    private WebView mContentWv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mContentWv = findViewById(R.id.wv_content);

        // TODO
        //mContentWv.loadUrl("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }
}