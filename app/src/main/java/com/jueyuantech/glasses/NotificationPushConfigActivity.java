package com.jueyuantech.glasses;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jueyuantech.glasses.util.BitmapUtil;

import java.util.ArrayList;
import java.util.Set;

public class NotificationPushConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout mNotificationPushContainerRl;
    private TextView mNotificationPushStateTv;
    private Button mPkgResetBtn;
    private Button mPkgSelectAllBtn;
    private Button mPkgUnselectAllBtn;

    private ImageView mBackIv;

    private RecyclerView mAppRcv;
    private ArrayList<PackageInfo> mPackageInfoList = new ArrayList<>();
    private AppInfoAdapter mAppAdapter;
    private PackageManager packageManager;

    private NotificationPushManager mNotificationPushManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_config);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        packageManager = getPackageManager();
        mNotificationPushManager = NotificationPushManager.getInstance();

        mNotificationPushContainerRl = findViewById(R.id.rl_container_notification_push);
        mNotificationPushContainerRl.setOnClickListener(this);
        mNotificationPushStateTv = findViewById(R.id.tv_state_notification_push);

        mPkgResetBtn = findViewById(R.id.btn_pkg_reset);
        mPkgResetBtn.setOnClickListener(this);
        mPkgSelectAllBtn = findViewById(R.id.btn_pkg_select_all);
        mPkgSelectAllBtn.setOnClickListener(this);
        mPkgUnselectAllBtn = findViewById(R.id.btn_pkg_unselect_all);
        mPkgUnselectAllBtn.setOnClickListener(this);

        mAppRcv = (RecyclerView) findViewById(R.id.rcv_app);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAppRcv.setLayoutManager(layoutManager);

        mAppAdapter = new AppInfoAdapter(this, mPackageInfoList);
        mAppRcv.setAdapter(mAppAdapter);
        mAppAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.rl_container_notification_push:
                switchNotificationPush();
                break;
            case R.id.btn_pkg_reset:
                mNotificationPushManager.resetAllowedPkgSet();
                mAppAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_pkg_select_all:
                mNotificationPushManager.selectAllPkg();
                mAppAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_pkg_unselect_all:
                mNotificationPushManager.unselectAllPkg();
                mAppAdapter.notifyDataSetChanged();
                break;
            default:
        }
    }

    private void updateUI() {
        if (mNotificationPushManager.isPushEnable()) {
            mNotificationPushStateTv.setText(R.string.state_enabled);
        } else {
            mNotificationPushStateTv.setText(R.string.state_disabled);
        }

        mPackageInfoList.clear();
        mPackageInfoList.addAll(mNotificationPushManager.getPkgList());
        mAppAdapter.notifyDataSetChanged();
    }

    private void switchNotificationPush() {
        if (mNotificationPushManager.isPushEnable()) {
            mNotificationPushManager.setPushEnable(false);
        } else {
            if (NotificationPushManager.getInstance().isNotificationListenerEnabled()) {
                mNotificationPushManager.setPushEnable(true);
            } else {
                showGrantPermissionDialog();
            }
        }

        updateUI();
    }

    private void showGrantPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_dialog_notification_title);
        builder.setMessage(R.string.permission_dialog_notification_content);
        builder.setNegativeButton(R.string.btn_cancel, null);

        builder.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openNotificationListenSettings();
            }
        });

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 9;
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    public void openNotificationListenSettings() {
        try {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class AppInfoAdapter extends RecyclerView.Adapter<AppInfoHolder> {

        public Context mContext;
        public ArrayList<PackageInfo> mPkgInfoList;

        public AppInfoAdapter(Context context, ArrayList<PackageInfo> pkgInfoList) {
            mContext = context;
            mPkgInfoList = pkgInfoList;
        }

        @Override
        public AppInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_info, parent, false);
            return new AppInfoHolder(view);
        }

        @Override
        public void onBindViewHolder(AppInfoHolder holder, int position) {
            PackageInfo packageInfo = mPkgInfoList.get(position);
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);

            Bitmap bitmap = BitmapUtil.drawableToBitmap(icon);
            Glide.with(mContext).load(bitmap).into(holder.icon);

            holder.name.setText(packageInfo.applicationInfo.loadLabel(packageManager).toString());
            holder.pkg.setText(packageInfo.packageName);
            if (mNotificationPushManager.isPkgAllowed(packageInfo.packageName)) {
                holder.allowed.setImageResource(R.drawable.baseline_check_24);
            } else {
                holder.allowed.setImageBitmap(null);
            }

            holder.allowed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNotificationPushManager.isPkgAllowed(packageInfo.packageName)) {
                        mNotificationPushManager.removePkgFromAllowed(packageInfo.packageName);
                    } else {
                        mNotificationPushManager.addPkgToAllowed(packageInfo.packageName);
                    }
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPkgInfoList.size();
        }
    }

    class AppInfoHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView pkg;
        public ImageView icon;
        public ImageView allowed;

        public AppInfoHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            pkg = (TextView) itemView.findViewById(R.id.tv_pkg);
            icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            allowed = (ImageView) itemView.findViewById(R.id.cb_allowed);
        }
    }
}