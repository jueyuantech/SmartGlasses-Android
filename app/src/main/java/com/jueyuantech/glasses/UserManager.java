package com.jueyuantech.glasses;

import android.content.Context;

import com.google.gson.Gson;
import com.jueyuantech.glasses.bean.UserInfo;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;

public class UserManager {

    private static volatile UserManager singleton;
    private Context mContext;

    private Gson gson = new Gson();
    private static final String MMKV_KEY_USER_TOKEN = "userToken";
    private static final String MMKV_KEY_USER_INFO = "userInfo";

    private boolean isLogin;

    private UserManager(Context context) {
        mContext = context.getApplicationContext();

        initUser();
    }

    public static UserManager getInstance() {
        if (singleton == null) {
            throw new IllegalStateException("UserManager is not initialized. Call init() before getInstance().");
        }
        return singleton;
    }

    /**
     * 初始化方法，应该在Application中调用
     *
     * @param context
     */
    public static void init(Context context) {
        if (singleton == null) {
            synchronized (UserManager.class) {
                if (singleton == null) {
                    singleton = new UserManager(context);
                }
            }
        }
    }

    public void initUser() {
        LogUtil.mark();
        if (hasToken()) {
            //CommonRequest.setAuthorization(loadToken());
        }
    }

    public boolean isLoggedIn() {
        return hasUserInfo();
    }

    public void login(String token) {
        // Step 1: 持久化
        saveToken(token);
        // Step 2: 初始化net
        //CommonRequest.setAuthorization(token);

        // 从后台获取UserInfo
        //fetchUserInfo();
    }

    public void logout() {
        removeToken();
        //CommonRequest.setAuthorization("");
        removeUserInfo();
    }

    public void fetchUserInfo() {
        // TODO backend service
    }

    /* Token start */
    public Boolean hasToken() {
        return MmkvUtil.getInstance().have(MMKV_KEY_USER_TOKEN);
    }

    public void saveToken(String token) {
        MmkvUtil.encode(MMKV_KEY_USER_TOKEN, token);
    }

    public String loadToken() {
        return MmkvUtil.decodeString(MMKV_KEY_USER_TOKEN, "");
    }

    public void removeToken() {
        MmkvUtil.getInstance().removeKey(MMKV_KEY_USER_TOKEN);
    }
    /* Token end */

    /* UserInfo start */
    public Boolean hasUserInfo() {
        return MmkvUtil.getInstance().have(MMKV_KEY_USER_INFO);
    }

    public void saveUserInfo(UserInfo userInfo) {
        MmkvUtil.getInstance().encodeParcelable(MMKV_KEY_USER_INFO, userInfo);
    }

    public UserInfo loadUserInfo() {
        return (UserInfo) MmkvUtil.getInstance().decodeParcelable(MMKV_KEY_USER_INFO, UserInfo.class);
    }

    public void removeUserInfo() {
        MmkvUtil.getInstance().removeKey(MMKV_KEY_USER_INFO);
    }
    /* UserInfo end */
}
