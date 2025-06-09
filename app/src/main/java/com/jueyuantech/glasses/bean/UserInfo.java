package com.jueyuantech.glasses.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {
    private int userId;
    private String userName;
    private String nickName;
    private String email;
    private String phonenumber;
    private String sex;
    private String avatar;

    // Parcelable creator
    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    // Constructor
    public UserInfo(int userId, String userName, String nickName, String email, String phonenumber, String sex, String avatar) {
        this.userId = userId;
        this.userName = userName;
        this.nickName = nickName;
        this.email = email;
        this.phonenumber = phonenumber;
        this.sex = sex;
        this.avatar = avatar;
    }

    // Constructor for Parcelable
    protected UserInfo(Parcel in) {
        userId = in.readInt();
        userName = in.readString();
        nickName = in.readString();
        email = in.readString();
        phonenumber = in.readString();
        sex = in.readString();
        avatar = in.readString();
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    // Describe the kinds of special objects contained in this Parcelable's
    // marshalled representation.
    @Override
    public int describeContents() {
        return 0;
    }

    // Flatten this object in to a Parcel.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(userName);
        dest.writeString(nickName);
        dest.writeString(email);
        dest.writeString(phonenumber);
        dest.writeString(sex);
        dest.writeString(avatar);
    }
}
