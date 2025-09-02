package com.jueyuantech.glasses.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PrompterLegacyCache implements Parcelable {

    public static final Creator<PrompterLegacyCache> CREATOR = new Creator<PrompterLegacyCache>() {
        public PrompterLegacyCache createFromParcel(Parcel var1) {
            return new PrompterLegacyCache(var1);
        }

        public PrompterLegacyCache[] newArray(int var1) {
            return new PrompterLegacyCache[var1];
        }
    };

    private String fileName;
    private long fileSize;
    private int curPage;

    public PrompterLegacyCache() {

    }

    public PrompterLegacyCache(Parcel var1) {
        this.fileName = var1.readString();
        this.fileSize = var1.readLong();
        this.curPage = var1.readInt();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileName);
        dest.writeLong(this.fileSize);
        dest.writeInt(this.curPage);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
