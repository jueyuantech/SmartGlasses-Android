package com.jueyuantech.glasses.bean;

import com.jueyuantech.glasses.adapter.MultiItemEntity;
import com.jueyuantech.venussdk.bean.RawData;

public class RawDataLog extends RawData implements MultiItemEntity {
    public RawDataLog(long timestamp, int type, int state, byte[] data) {
        super(timestamp, type, state, data);
    }
}
