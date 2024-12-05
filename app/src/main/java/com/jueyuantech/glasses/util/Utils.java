package com.jueyuantech.glasses.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");

    public static String getCurrentTimeStr() {
        return sdf.format(new Date());
    }
    public static String getTimeStr(long timestamp) {
        return sdf.format(new Date(timestamp));
    }
}
