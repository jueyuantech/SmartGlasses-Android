package com.jueyuantech.glasses.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jueyuantech.glasses.BuildConfig;
import com.jueyuantech.glasses.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private TextView mAppVersionTv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAppVersionTv = binding.tvAppVersion;
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        mAppVersionTv.setText(BuildConfig.VERSION_NAME);
    }
}