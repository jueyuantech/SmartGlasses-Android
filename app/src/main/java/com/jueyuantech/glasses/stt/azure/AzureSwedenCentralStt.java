package com.jueyuantech.glasses.stt.azure;

import android.content.Context;

public class AzureSwedenCentralStt extends AzureStt {

    public AzureSwedenCentralStt(Context context, String func) {
        super(context, func);
    }

    @Override
    public String getName() {
        return "AzureSwedenCentral";
    }

    @Override
    public String getServiceId() {
        return "90DF6BA2D9BD454B89E7E9B59F4AE1EC";
    }

    @Override
    public String getLocalParam() {
        return "";
    }
}
