package com.jueyuantech.glasses.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class ReaderConfig implements Parcelable {

    public static final Creator<ReaderConfig> CREATOR = new Creator<ReaderConfig>() {
        public ReaderConfig createFromParcel(Parcel var1) {
            return new ReaderConfig(var1);
        }

        public ReaderConfig[] newArray(int var1) {
            return new ReaderConfig[var1];
        }
    };

    private boolean customFontSize;
    private int fontSize;
    private boolean showContext;

    public ReaderConfig() {

    }

    public ReaderConfig(Parcel var1) {
        this.customFontSize = var1.readByte() != 0;
        this.fontSize = var1.readInt();
        this.showContext = var1.readByte() != 0;
    }

    public boolean isCustomFontSize() {
        return customFontSize;
    }

    public void setCustomFontSize(boolean customFontSize) {
        this.customFontSize = customFontSize;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isShowContext() {
        return showContext;
    }

    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (customFontSize ? 1 : 0));
        dest.writeInt(fontSize);
        dest.writeByte((byte) (showContext ? 1 : 0));
    }
}