package com.jueyuantech.glasses;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jueyuantech.glasses.bean.UserInfo;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;
    private TextView mUserNameTv;
    private TextView mNickNameTv;
    private TextView mPhoneNumberTv;
    private TextView mEmailTv;
    private RelativeLayout mDeleteAccountRl;
    private Button mLogoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mUserNameTv = findViewById(R.id.tv_user_name);
        mNickNameTv = findViewById(R.id.tv_nick_name);
        mPhoneNumberTv = findViewById(R.id.tv_phone_number);
        mEmailTv = findViewById(R.id.tv_email);

        mDeleteAccountRl = findViewById(R.id.rl_container_delete_account);
        mDeleteAccountRl.setOnClickListener(this);

        mLogoutBtn = findViewById(R.id.btn_logout);
        mLogoutBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        UserInfo userInfo = UserManager.getInstance().loadUserInfo();
        if (null != userInfo) {
            mNickNameTv.setText(userInfo.getNickName());
            mUserNameTv.setText(userInfo.getUserName());
            mPhoneNumberTv.setText(userInfo.getPhonenumber());
            mEmailTv.setText(userInfo.getEmail());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.rl_container_delete_account:
                showDeleteAccountDialog();
                break;
            case R.id.btn_logout:
                logout();
                finish();
                break;
        }
    }

    private void logout() {
        UserManager.getInstance().removeUserInfo();
        UserManager.getInstance().removeToken();
    }

    private void deleteAccount() {
        // TODO backend service
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_delete_account_title);
        builder.setMessage(R.string.dialog_delete_account_message);
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.setPositiveButton(R.string.dialog_delete_account_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccount();
            }
        });

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 9;
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }
}