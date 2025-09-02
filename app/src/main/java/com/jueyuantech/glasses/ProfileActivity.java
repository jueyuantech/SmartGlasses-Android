package com.jueyuantech.glasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jueyuantech.glasses.bean.UserInfo;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.login.PhoneNumberLoginActivity;
import com.jueyuantech.glasses.util.ToastUtil;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;
    private TextView mUserNameTv;
    private RelativeLayout mAccountContainerRl;
    private RelativeLayout mDevicesContainerRl;
    private RelativeLayout mPermissionContainerRl;
    private RelativeLayout mBackgroundPermissionContainerRl;
    private RelativeLayout mGuideContainerRl;
    private RelativeLayout mFeedbackContainerRl;
    private RelativeLayout mAboutContainerRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mUserNameTv = findViewById(R.id.tv_user_name);
        mUserNameTv.setOnClickListener(this);
        mAboutContainerRl = findViewById(R.id.rl_container_about);
        mAboutContainerRl.setOnClickListener(this);
        mDevicesContainerRl = findViewById(R.id.rl_container_devices);
        mDevicesContainerRl.setOnClickListener(this);
        mPermissionContainerRl = findViewById(R.id.rl_container_permission);
        mPermissionContainerRl.setOnClickListener(this);
        mBackgroundPermissionContainerRl = findViewById(R.id.rl_container_background_permission);
        mBackgroundPermissionContainerRl.setOnClickListener(this);
        mGuideContainerRl = findViewById(R.id.rl_container_guide);
        mGuideContainerRl.setOnClickListener(this);
        mFeedbackContainerRl = findViewById(R.id.rl_container_feedback);
        mFeedbackContainerRl.setOnClickListener(this);
        mAccountContainerRl = findViewById(R.id.rl_container_account);
        mAccountContainerRl.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (UserManager.getInstance().isLoggedIn()) {
            UserInfo userInfo = UserManager.getInstance().loadUserInfo();
            mUserNameTv.setText(userInfo.getPhonenumber());
        } else {
            mUserNameTv.setText(R.string.label_not_logged_in);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_user_name:
            case R.id.rl_container_account:
                if (UserManager.getInstance().isLoggedIn()) {
                    toAccountAct();
                } else {
                    toPhoneNumberLoginAct();
                }
                break;
            case R.id.rl_container_devices:
                toDevicesAct();
                break;
            case R.id.rl_container_permission:
                toPermissionAct();
                break;
            case R.id.rl_container_background_permission:
                toBackgroundPermissionAct();
                break;
            case R.id.rl_container_guide:
                toGuideAct();
                break;
            case R.id.rl_container_feedback:
                if (UserManager.getInstance().isLoggedIn()) {
                    toFeedbackAct();
                } else {
                    ToastUtil.toast(ProfileActivity.this, R.string.label_not_logged_in);
                }
                break;
            case R.id.rl_container_about:
                toAboutAct();
                break;
            default:
        }
    }

    private void toAccountAct() {
        Intent accountIntent = new Intent(this, AccountActivity.class);
        startActivity(accountIntent);
    }

    private void toDevicesAct() {
        if (DeviceManager.getInstance().isBound()) {
            Intent deviceInfoIntent = new Intent(this, DeviceInfoActivity.class);
            startActivity(deviceInfoIntent);
        } else {
            ToastUtil.toast(this, R.string.tips_no_device_bound);
        }
    }

    private void toPhoneNumberLoginAct() {
        Intent phoneNumberLoginIntent = new Intent(this, PhoneNumberLoginActivity.class);
        startActivity(phoneNumberLoginIntent);
    }

    private void toPermissionAct() {
        Intent permissionIntent = new Intent(this, NormalPermissionActivity.class);
        startActivity(permissionIntent);
    }

    private void toBackgroundPermissionAct() {
        Intent backgroundPermissionIntent = new Intent(this, BackgroundPermissionActivity.class);
        startActivity(backgroundPermissionIntent);
    }

    private void toGuideAct() {
        Intent guideIntent = new Intent(this, GuideActivity.class);
        startActivity(guideIntent);
    }

    private void toFeedbackAct() {
        Intent feedbackIntent = new Intent(this, FeedbackActivity.class);
        startActivity(feedbackIntent);
    }

    private void toAboutAct() {
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        startActivity(aboutIntent);
    }
}