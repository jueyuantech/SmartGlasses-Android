package com.jueyuantech.glasses.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static final SimpleDateFormat sdf_p = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    public static String getCurrentTimeStr() {
        return sdf.format(new Date());
    }

    public static String getTimeStr(long timestamp) {
        return sdf.format(new Date(timestamp));
    }
}
