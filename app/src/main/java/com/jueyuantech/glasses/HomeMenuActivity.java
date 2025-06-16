package com.jueyuantech.glasses;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNHomeMenu;

import java.util.ArrayList;
import java.util.List;

public class HomeMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private GridLayoutManager mShownMenuGridLayoutManager, mHiddenMenuGridLayoutPanelManager;
    private RecyclerView mShownMenusRcv, mHiddenMenusRcv;
    private HomeMenuAdapter mShownMenuAdapter, mHiddenMenuAdapter;

    private List<VNHomeMenu> mShownMenus = new ArrayList<>();
    private List<VNHomeMenu> mHiddenMenus = new ArrayList<>();
    private ItemTouchHelper mItemTouchHelper;

    private Button mResetBtn;
    private Button mCommitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mShownMenusRcv = (RecyclerView) findViewById(R.id.rcv_menu_default);
        mHiddenMenusRcv = findViewById(R.id.rcv_menu_panel);

        mShownMenuGridLayoutManager = new GridLayoutManager(this, 3);
        mHiddenMenuGridLayoutPanelManager = new GridLayoutManager(this, 3);

        mShownMenusRcv.setLayoutManager(mShownMenuGridLayoutManager);
        mShownMenusRcv.setNestedScrollingEnabled(true);
        mShownMenuAdapter = new HomeMenuAdapter(this, mShownMenus, HomeMenuAdapter.MENU_TYPE_SHOW);
        mShownMenuAdapter.setOnRemoveListener(new HomeMenuAdapter.OnRemovedListener() {
            @Override
            public void onRemoved(VNHomeMenu homeMenu) {
                mHiddenMenus.add(homeMenu);
                mHiddenMenuAdapter.notifyDataSetChanged();
            }
        });
        mShownMenusRcv.setAdapter(mShownMenuAdapter);

        mHiddenMenusRcv.setLayoutManager(mHiddenMenuGridLayoutPanelManager);
        mHiddenMenusRcv.setNestedScrollingEnabled(true);
        mHiddenMenuAdapter = new HomeMenuAdapter(this, mHiddenMenus, HomeMenuAdapter.MENU_TYPE_HIDE);
        mHiddenMenuAdapter.setOnRemoveListener(new HomeMenuAdapter.OnRemovedListener() {
            @Override
            public void onRemoved(VNHomeMenu homeMenu) {
                mShownMenus.add(homeMenu);
                mShownMenuAdapter.notifyDataSetChanged();
            }
        });
        mHiddenMenusRcv.setAdapter(mHiddenMenuAdapter);

        //添加拖拽事件
        mItemTouchHelper = new ItemTouchHelper(new HomeMenuItemTouchHelper(mShownMenus, mShownMenuAdapter));
        mItemTouchHelper.attachToRecyclerView(mShownMenusRcv);

        mCommitBtn = findViewById(R.id.btn_commit);
        mCommitBtn.setOnClickListener(this);
        mResetBtn = findViewById(R.id.btn_reset);
        mResetBtn.setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FIXME Get from device via VNCommon
        genDefaultMenu();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                genDefaultMenu();
                break;
            case R.id.btn_commit:
                setMenus();
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    private void genDefaultMenu() {
        genShownMenus();
        mShownMenuAdapter.notifyDataSetChanged();
        genHiddenMenus();
        mHiddenMenuAdapter.notifyDataSetChanged();
    }

    private void setMenus() {
        for (int i = 0; i < mShownMenus.size(); i++) {
            mShownMenus.get(i).setOrderCustom(i + 1);
        }
        VNCommon.setHomeMenus(mShownMenus, null);
    }

    private List<VNHomeMenu> genShownMenus() {
        mShownMenus.clear();
        mShownMenus.add(new VNHomeMenu(1, 1, "Transcribe", 0));
        //mShownMenus.add(new VNHomeMenu(2, 2, "Book", 0));
        mShownMenus.add(new VNHomeMenu(2, 2, "Map", 0));
        mShownMenus.add(new VNHomeMenu(3, 3, "Music", 1));
        mShownMenus.add(new VNHomeMenu(4, 4, "Translate", 0));
        mShownMenus.add(new VNHomeMenu(5, 5, "Prompter", 0));
        mShownMenus.add(new VNHomeMenu(6, 6, "Settings", 0));
        return mShownMenus;
    }

    private List<VNHomeMenu> genHiddenMenus() {
        mHiddenMenus.clear();
        return mHiddenMenus;
    }
}