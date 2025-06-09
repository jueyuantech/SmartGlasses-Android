package com.jueyuantech.glasses.stt.azure;

import android.content.Context;

public class AzureWestUSStt extends AzureStt {

    public AzureWestUSStt(Context context, String func) {
        super(context, func);
    }

    @Override
    public String getName() {
        return "AzureWestUS";
    }

    @Override
    public String getServiceId() {
        return "";
    }

    @Override
    public String getLocalParam() {
        return "";
    }
}
