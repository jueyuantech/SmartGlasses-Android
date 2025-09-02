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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jueyuantech.glasses.util.BitmapUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNNotificationInfo;

import java.util.ArrayList;
import java.util.Random;
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

        initNotiTestView();
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
            case R.id.btn_generate_id:
                generateNewNotificationId();
                break;
            case R.id.btn_send_test_notification:
                sendConfiguredTestNotification();
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

    /** 通知测试 */

    // 测试通知ID
    private int mCurrentNotificationId = 1234;
    // 通知测试配置控件
    private TextView mNotificationIdTv;
    private Button mGenerateIdBtn;
    private RadioGroup mNotificationTypeRg;
    private RadioGroup mNotificationActionRg;
    private RadioGroup mNotificationDurationRg;
    private Button mSendTestNotificationBtn;

    private void initNotiTestView() {
        mNotificationIdTv = findViewById(R.id.tv_notification_id);
        mGenerateIdBtn = findViewById(R.id.btn_generate_id);
        mGenerateIdBtn.setOnClickListener(this);

        mNotificationTypeRg = findViewById(R.id.rg_notification_type);
        mNotificationActionRg = findViewById(R.id.rg_notification_action);
        mNotificationDurationRg = findViewById(R.id.rg_notification_duration);
        mSendTestNotificationBtn = findViewById(R.id.btn_send_test_notification);
        mSendTestNotificationBtn.setOnClickListener(this);

        // 设置duration选择监听器，实现联动逻辑
        mNotificationDurationRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_duration_auto_close) {
                    // 当选择自动关闭时，自动选择"显示"并禁用action选择
                    mNotificationActionRg.check(R.id.rb_action_show);
                    setActionRadioGroupEnabled(false);
                } else {
                    // 当选择常驻等待关闭时，启用action选择
                    setActionRadioGroupEnabled(true);
                }
            }
        });

        // 初始化ID显示
        updateNotificationIdDisplay();
    }

    /**
     * 生成新的通知ID
     */
    private void generateNewNotificationId() {
        Random random = new Random();
        mCurrentNotificationId = random.nextInt(9000) + 1000; // 生成1000-9999的随机数
        updateNotificationIdDisplay();
    }

    /**
     * 更新通知ID显示
     */
    private void updateNotificationIdDisplay() {
        mNotificationIdTv.setText(String.valueOf(mCurrentNotificationId));
    }

    /**
     * 设置Action RadioGroup的启用状态
     */
    private void setActionRadioGroupEnabled(boolean enabled) {
        for (int i = 0; i < mNotificationActionRg.getChildCount(); i++) {
            mNotificationActionRg.getChildAt(i).setEnabled(enabled);
        }
    }

    /**
     * 获取选中的通知类型
     *
     * @return 0-用户，1-系统
     */
    private int getSelectedNotificationType() {
        int checkedId = mNotificationTypeRg.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_type_system) {
            return 1;
        }
        return 0; // 默认用户
    }

    /**
     * 获取选中的通知操作
     *
     * @return 0-关闭，1-显示，2-更新
     */
    private int getSelectedNotificationAction() {
        int checkedId = mNotificationActionRg.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_action_close) {
            return 0;
        } else if (checkedId == R.id.rb_action_update) {
            return 2;
        }
        return 1; // 默认显示
    }

    /**
     * 获取选中的通知持续时间
     *
     * @return 0-常驻等待关闭，3-自动关闭
     */
    private int getSelectedNotificationDuration() {
        int checkedId = mNotificationDurationRg.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_duration_auto_close) {
            return 3;
        }
        return 0; // 默认常驻等待关闭
    }

    /**
     * 发送配置化的测试通知
     */
    private void sendConfiguredTestNotification() {
        if (!VNCommon.isConnected()) {
            ToastUtil.toast(this, "设备未连接");
            return;
        }

        int notificationType = getSelectedNotificationType();
        int notificationAction = getSelectedNotificationAction();
        int notificationDuration = getSelectedNotificationDuration();

        VNNotificationInfo notificationInfo = new VNNotificationInfo();
        notificationInfo.setId(mCurrentNotificationId);

        // 根据类型设置标题和内容
        String typeStr = notificationType == 0 ? "用户" : "系统";
        notificationInfo.setType(notificationType);

        String actionStr = "";
        switch (notificationAction) {
            case 0:
                actionStr = "关闭";
                break;
            case 1:
                actionStr = "显示";
                break;
            case 2:
                actionStr = "更新";
                break;
        }
        notificationInfo.setAction(notificationAction);

        String durationStr = notificationDuration == 0 ? "常驻等待关闭" : "自动关闭";
        notificationInfo.setDuration(notificationDuration);

        notificationInfo.setTitle(String.format("测试通知 [%s]", typeStr));
        notificationInfo.setMsg(String.format("ID: %d\n操作: %s\n持续: %s",
                mCurrentNotificationId, actionStr, durationStr));
        notificationInfo.setPostTime(System.currentTimeMillis() / 1000);

        // 设置应用图标
        try {
            Drawable appIcon = getApplicationContext().getDrawable(R.mipmap.ic_launcher);
            if (appIcon != null) {
                Bitmap iconBitmap = BitmapUtil.drawableToBitmap(appIcon);
                String bmpStr = VNCommon.getVenusBmpRawData(iconBitmap, 32, 32, false);
                if (bmpStr != null && !bmpStr.isEmpty()) {
                    notificationInfo.setIconBitmap(bmpStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 根据操作类型执行相应的通知操作
        switch (notificationAction) {
            case 0: // 关闭
                VNCommon.removeNotification(notificationInfo, null);
                ToastUtil.toast(this, "通知已关闭");
                break;
            case 1: // 显示
                VNCommon.addNotification(notificationInfo, null);
                ToastUtil.toast(this, String.format("通知已发送 (ID: %d, 类型: %s, 持续: %s)",
                        mCurrentNotificationId, typeStr, durationStr));
                break;
            case 2: // 更新
                VNCommon.updateNotification(notificationInfo, null);
                ToastUtil.toast(this, String.format("通知已更新 (ID: %d, 类型: %s, 持续: %s)",
                        mCurrentNotificationId, typeStr, durationStr));
                break;
        }
    }
}