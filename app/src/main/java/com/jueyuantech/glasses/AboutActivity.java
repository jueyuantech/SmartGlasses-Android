package com.jueyuantech.glasses;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNSDK;
import com.jueyuantech.venussdk.bean.VNVersionInfo;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mBackIv;
    private TextView mAppNameTv;
    private TextView mAppVersionTv;
    private RelativeLayout mUpgradeContainerRl;
    private RelativeLayout mTermsContainerRl;
    private RelativeLayout mPolicyContainerRl;
    private RelativeLayout mWithdrawContainerRl;
    private TextView mICPNumberTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mAppNameTv = findViewById(R.id.tv_app_name);
        mAppNameTv.setOnClickListener(this);
        mAppVersionTv = findViewById(R.id.tv_app_version);
        mAppVersionTv.setText(BuildConfig.VERSION_NAME);
        mAppVersionTv.setOnClickListener(this);

        mTermsContainerRl = findViewById(R.id.rl_container_terms);
        mTermsContainerRl.setOnClickListener(this);
        mPolicyContainerRl = findViewById(R.id.rl_container_policy);
        mPolicyContainerRl.setOnClickListener(this);
        mUpgradeContainerRl = findViewById(R.id.rl_container_upgrade);
        mUpgradeContainerRl.setOnClickListener(this);
        mWithdrawContainerRl = findViewById(R.id.rl_container_withdraw_policy);
        mWithdrawContainerRl.setOnClickListener(this);

        mICPNumberTv = findViewById(R.id.tv_icp_number);
        mICPNumberTv.setText(BuildConfig.PROP_ICP_NUMBER);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_app_name:
                break;
            case R.id.tv_app_version:
                showSdkVersion();
                break;
            case R.id.rl_container_upgrade:
                ToastUtil.toast(AboutActivity.this, R.string.ota_latest_already);
                break;
            case R.id.rl_container_terms:
                showTerms();
                break;
            case R.id.rl_container_policy:
                showPolicy();
                break;
            case R.id.rl_container_withdraw_policy:
                showWithdrawPolicyDialog();
                break;
        }
    }

    private void showSdkVersion() {
        VNVersionInfo versionInfo = VNSDK.getVersionInfo();
        String sdkVersion = versionInfo.getBuildType() + " "
                + versionInfo.getName() + " "
                + versionInfo.getCode() + " "
                + versionInfo.getCommit() + " "
                + versionInfo.getDate();
        ToastUtil.toast(this, sdkVersion);
    }

    private void showTerms() {
        Intent termsIntent = new Intent(this, TermsActivity.class);
        startActivity(termsIntent);
    }

    private void showPolicy() {
        Intent policyIntent = new Intent(this, PolicyActivity.class);
        startActivity(policyIntent);
    }

    private void showWithdrawPolicyDialog() {
        AlertDialog.Builder withdrawDialogBuilder = new AlertDialog.Builder(this);
        withdrawDialogBuilder.setTitle(R.string.withdraw_dialog_title);
        withdrawDialogBuilder.setMessage(R.string.withdraw_dialog_content);
        withdrawDialogBuilder.setNegativeButton(R.string.withdraw_dialog_cancel, null);
        withdrawDialogBuilder.setPositiveButton(R.string.withdraw_dialog_withdraw, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                withdrawPolicy();
            }
        });

        Dialog withdrawDialog = withdrawDialogBuilder.create();
        withdrawDialog.show();

        Window window = withdrawDialog.getWindow();
        if (null != window) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 9;
            //lp.gravity = Gravity.BOTTOM;
            window.setAttributes(lp);
            //设置自身的底板透明
            //window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //设置dialog周围activity背景的透明度，[0f,1f]，0全透明，1不透明黑
            //window.setDimAmount(0.1f);
        }
    }

    private void withdrawPolicy() {
        MyApplication.withdrawAgreement();

        // TODO
        //android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(0);
    }
}