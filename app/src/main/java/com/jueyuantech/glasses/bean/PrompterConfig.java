package com.jueyuantech.glasses.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PrompterConfig implements Parcelable {

    public static final Creator<PrompterConfig> CREATOR = new Creator<PrompterConfig>() {
        public PrompterConfig createFromParcel(Parcel var1) {
            return new PrompterConfig(var1);
        }

        public PrompterConfig[] newArray(int var1) {
            return new PrompterConfig[var1];
        }
    };

    private boolean customFontSize;
    private int fontSize;
    private boolean showContext;

    public PrompterConfig() {

    }

    public PrompterConfig(Parcel var1) {
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (this.customFontSize ? 1 : 0));
        dest.writeInt(this.fontSize);
        dest.writeByte((byte) (this.showContext ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
