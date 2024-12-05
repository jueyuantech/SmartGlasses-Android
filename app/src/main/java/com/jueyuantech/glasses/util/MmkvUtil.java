package com.jueyuantech.glasses.util;

import android.os.Parcelable;

import com.tencent.mmkv.MMKV;

import java.util.Collections;
import java.util.Set;

public class MmkvUtil {

    private static MMKV mkv;

    private MmkvUtil() {
        mkv = MMKV.defaultMMKV();
    }

    public static MmkvUtil getInstance() {
        return SingletonHolder.sInstance;
    }

    //静态内部类
    private static class SingletonHolder {
        private static final MmkvUtil sInstance = new MmkvUtil();
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    public static void encode(String key, Object object) {
        if (object instanceof String) {
            mkv.encode(key, (String) object);
        } else if (object instanceof Integer) {
            mkv.encode(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mkv.encode(key, (Boolean) object);
        } else if (object instanceof Float) {
            mkv.encode(key, (Float) object);
        } else if (object instanceof Long) {
            mkv.encode(key, (Long) object);
        } else if (object instanceof Double) {
            mkv.encode(key, (Double) object);
        } else if (object instanceof byte[]) {
            mkv.encode(key, (byte[]) object);
        } else {
            mkv.encode(key, object.toString());
        }
    }

    public static void encodeSet(String key, Set<String> sets) {
        mkv.encode(key, sets);
    }

    public static void encodeParcelable(String key, Parcelable obj) {
        mkv.encode(key, obj);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object decode(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return mkv.decodeString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return mkv.decodeInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return mkv.decodeBool(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return mkv.decodeFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return mkv.decodeLong(key, (Long) defaultObject);
        } else if (defaultObject instanceof Double) {
            return mkv.decodeDouble(key, (Double) defaultObject);
        } else if (defaultObject instanceof byte[]) {
            return mkv.decodeBytes(key, (byte[]) defaultObject);
        }
        return defaultObject;
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    public static Integer decodeInt(String key) {
        return mkv.decodeInt(key, 0);
    }

    public static Double decodeDouble(String key) {
        return mkv.decodeDouble(key, 0.00);
    }

    public static Long decodeLong(String key) {
        return mkv.decodeLong(key, 0L);
    }

    public static Boolean decodeBoolean(String key) {
        return mkv.decodeBool(key, false);
    }

    public static Float decodeFloat(String key) {
        return mkv.decodeFloat(key, 0F);
    }

    public static byte[] decodeBytes(String key) {
        return mkv.decodeBytes(key);
    }

    public static String decodeString(String key) {
        return mkv.decodeString(key, "");
    }

    public static String decodeString(String key, String defaultValue) {
        return mkv.decodeString(key, defaultValue);
    }

    public static Set<String> decodeStringSet(String key) {
        return mkv.decodeStringSet(key, Collections.<String>emptySet());
    }

    public static Parcelable decodeParcelable(String key, Class clz) {
        return mkv.decodeParcelable(key, clz);
    }

    /**
     * 移除某个key对
     *
     * @param key
     */
    public static void removeKey(String key) {
        mkv.removeValueForKey(key);
    }

    /**
     * 移除多个key对
     *
     * @param key
     */
    public static void removeKeys(String[] key) {
        mkv.removeValuesForKeys(key);
    }

    /**
     * 获取全部key对
     */
    public static String[] getAllKeys() {
        return mkv.allKeys();
    }

    /**
     * 含有某个key
     *
     * @param key
     * @return
     */
    public static boolean hasKey(String key) {
        return mkv.containsKey(key);
    }

    /**
     * 含有某个key
     *
     * @param key
     * @return
     */
    public static boolean have(String key) {
        return mkv.contains(key);
    }

    /**
     * 清除所有key
     */
    public static void clearAll() {
        mkv.clearAll();
    }

    /**
     * 获取操作对象
     *
     * @return
     */
    public static MMKV getMkv() {
        return mkv;
    }
}