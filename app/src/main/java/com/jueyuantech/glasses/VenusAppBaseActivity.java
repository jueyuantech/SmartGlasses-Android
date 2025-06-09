package com.jueyuantech.glasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class VenusAppBaseActivity extends AppCompatActivity {
    private boolean ENTER_FROM_VENUS = false;
    private boolean EXIT_FROM_VENUS = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ENTER_FROM_VENUS = getIntent().getBooleanExtra("ENTER_FROM_VENUS", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter exitFromVenusIntent = new IntentFilter();
        exitFromVenusIntent.addAction("com.jueyuantech.glasses.ACTION_EXIT_FROM_VENUS");
        registerReceiver(exitFromVenusReceiver, exitFromVenusIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ENTER_FROM_VENUS) {
            // do nothing
        } else {
            notifyVenusEnter();
        }
    }

    @Override
    protected void onDestroy() {
        if (EXIT_FROM_VENUS) {
            notifyExitFromVenus();
        } else {
            notifyVenusExit();
        }

        unregisterReceiver(exitFromVenusReceiver);
        super.onDestroy();
    }

    protected abstract void notifyVenusEnter();

    protected abstract void notifyVenusExit();

    protected abstract void notifyExitFromVenus();

    private BroadcastReceiver exitFromVenusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EXIT_FROM_VENUS = true;
            finish();
        }
    };
}
