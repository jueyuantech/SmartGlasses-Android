package com.jueyuantech.glasses.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MainViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is main fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}