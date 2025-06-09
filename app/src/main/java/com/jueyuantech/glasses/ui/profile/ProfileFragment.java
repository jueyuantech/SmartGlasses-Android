package com.jueyuantech.glasses.ui.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jueyuantech.glasses.AboutActivity;
import com.jueyuantech.glasses.AccountActivity;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.UserManager;
import com.jueyuantech.glasses.bean.UserInfo;
import com.jueyuantech.glasses.databinding.FragmentProfileBinding;
import com.jueyuantech.glasses.login.PhoneNumberLoginActivity;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private FragmentProfileBinding binding;
    private TextView mUserNameTv;
    private RelativeLayout mAccountContainerRl;
    private RelativeLayout mAboutContainerRl;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mUserNameTv = binding.tvUserName;
        mUserNameTv.setOnClickListener(this);
        mAboutContainerRl = binding.rlContainerAbout;
        mAboutContainerRl.setOnClickListener(this);
        mAccountContainerRl = binding.rlContainerAccount;
        mAccountContainerRl.setOnClickListener(this);

        return root;
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_container_about:
                toAboutAct();
                break;
            case R.id.tv_user_name:
            case R.id.rl_container_account:
                if (UserManager.getInstance().isLoggedIn()) {
                    toAccountAct();
                } else {
                    toPhoneNumberLoginAct();
                }
                break;
            default:
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void toAccountAct() {
        Intent accountIntent = new Intent(getActivity(), AccountActivity.class);
        getActivity().startActivity(accountIntent);
    }

    private void toPhoneNumberLoginAct() {
        Intent phoneNumberLoginIntent = new Intent(getActivity(), PhoneNumberLoginActivity.class);
        getActivity().startActivity(phoneNumberLoginIntent);
    }

    private void toAboutAct() {
        Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
        getActivity().startActivity(aboutIntent);
    }
}