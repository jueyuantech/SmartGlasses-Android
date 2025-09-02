package com.jueyuantech.glasses.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PrompterLegacyConfig implements Parcelable {

    public static final Parcelable.Creator<PrompterLegacyConfig> CREATOR = new Parcelable.Creator<PrompterLegacyConfig>() {
        public PrompterLegacyConfig createFromParcel(Parcel var1) {
            return new PrompterLegacyConfig(var1);
        }

        public PrompterLegacyConfig[] newArray(int var1) {
            return new PrompterLegacyConfig[var1];
        }
    };

    private int displayRows;
    private boolean showPrevious;
    private int fontSize;
    private boolean autoLoadHistory;

    public PrompterLegacyConfig() {

    }

    public PrompterLegacyConfig(Parcel var1) {
        this.displayRows = var1.readInt();
        this.showPrevious = var1.readByte() != 0;
        this.fontSize = var1.readInt();
        this.autoLoadHistory = var1.readByte() != 0;
    }

    public int getDisplayRows() {
        return displayRows;
    }

    public void setDisplayRows(int displayRows) {
        this.displayRows = displayRows;
    }

    public boolean isShowPrevious() {
        return showPrevious;
    }

    public void setShowPrevious(boolean showPrevious) {
        this.showPrevious = showPrevious;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isAutoLoadHistory() {
        return autoLoadHistory;
    }

    public void setAutoLoadHistory(boolean autoLoadHistory) {
        this.autoLoadHistory = autoLoadHistory;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.displayRows);
        dest.writeByte((byte) (this.showPrevious ? 1 : 0));
        dest.writeInt(this.fontSize);
        dest.writeByte((byte) (this.autoLoadHistory ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
