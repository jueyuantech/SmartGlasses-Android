package com.jueyuantech.glasses.amap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AmapRouteActivity;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.view.NextTurnTipView;
import com.google.gson.Gson;
import com.jueyuantech.glasses.util.BitmapUtil;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNHealthInfo;
import com.jueyuantech.venussdk.bean.VNNavInfo;

import java.util.Random;

public class NaviActivity extends AmapRouteActivity {
    private Gson gson = new Gson();
    private Random random = new Random();
    private int mLastIconType = -1;
    private ImageView mIconNttvCloud;
    private NextTurnTipView mIconNttvLocal;
    protected AMapNavi mAMapNavi;
    private static float mCurSpeed = 0;
    LatLng p1 = new LatLng(39.993266, 116.473193);//首开广场
    LatLng p2 = new LatLng(39.917337, 116.397056);//故宫博物院
    LatLng p3 = new LatLng(39.904556, 116.427231);//北京站
    LatLng p4 = new LatLng(39.773801, 116.368984);//新三余公园(南5环)
    LatLng p5 = new LatLng(40.041986, 116.414496);//立水桥(北5环)

    /* VenusAppBaseActivity START */
    private boolean ENTER_FROM_VENUS = false;
    private boolean EXIT_FROM_VENUS = false;
    /* VenusAppBaseActivity END */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.mark();

        /* VenusAppBaseActivity START */
        try {
            ENTER_FROM_VENUS = getIntent().getBooleanExtra("ENTER_FROM_VENUS", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter exitFromVenusIntent = new IntentFilter();
        exitFromVenusIntent.addAction("com.jueyuantech.glasses.ACTION_EXIT_FROM_VENUS");
        registerReceiver(exitFromVenusReceiver, exitFromVenusIntent);

        if (ENTER_FROM_VENUS) {
            // do nothing
        } else {
            notifyVenusEnter();
        }
        /* VenusAppBaseActivity END */

        try {
            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        } catch (AMapException e) {
            e.printStackTrace();
        }

        addAMapNaviListener();
    }

    @Override
    protected void onStart() {
        super.onStart();

        LogUtil.mark();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LogUtil.mark();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        LogUtil.mark();
    }

    @Override
    protected void onPause() {
        LogUtil.mark();

        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.mark();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogUtil.mark();

        /* VenusAppBaseActivity START */
        if (EXIT_FROM_VENUS) {
            notifyExitFromVenus();
        } else {
            notifyVenusExit();
        }
        unregisterReceiver(exitFromVenusReceiver);
        /* VenusAppBaseActivity END */

        removeAMapNaviListener();

        super.onDestroy();
    }

    /* VenusAppBaseActivity START */
    private void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.MAP, null);
    }

    private void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, null);
    }

    private void notifyExitFromVenus() {
        mAMapNavi.stopNavi();
    }

    private BroadcastReceiver exitFromVenusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EXIT_FROM_VENUS = true;
            finish();
        }
    };
    /* VenusAppBaseActivity END */

    private void addAMapNaviListener() {
        LogUtil.mark();

        if (null != mAMapNavi) {
            mAMapNavi.addAMapNaviListener(aMapNaviListener);
        }
    }

    private void removeAMapNaviListener() {
        LogUtil.mark();

        if (null != mAMapNavi) {
            mAMapNavi.removeAMapNaviListener(aMapNaviListener);
        }
    }

    private void startWork() {
        mLastIconType = -1;
    }

    private void stopWork() {

    }

    private void updateNavInfo(VNNavInfo navInfo) {
        LogUtil.i(gson.toJson(navInfo));
        VNCommon.updateNav(navInfo, null);
    }

    private void updateHealthInfo() {
        VNHealthInfo healthInfo = new VNHealthInfo();
        healthInfo.setBmp((70 + random.nextInt(10)) + " bmp");
        VNCommon.updateHealth(healthInfo, null);
    }

    private String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + "米";
        } else if (meters < 1000 * 100) {
            double kilometers = meters / 1000.0;
            return String.format("%.1f公里", kilometers);
        } else {
            int kilometers = meters / 1000;
            return kilometers + "公里";
        }
    }

    /**
     * 将秒数格式化为时间字符串
     * @param totalSeconds 总秒数
     * @return 格式化后的时间字符串，例如：1小时30分钟、45分钟、1分钟
     */
    private String formatTimeFromSeconds(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "0分钟";
        }
        
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        
        // 不到1分钟的，显示为1分钟
        if (hours == 0 && minutes == 0) {
            return "1分钟";
        }
        
        StringBuilder result = new StringBuilder();
        if (hours > 0) {
            result.append(hours).append("小时");
        }
        if (minutes > 0 || hours > 0) {
            result.append(minutes).append("分钟");
        }
        
        return result.toString();
    }

    private byte[] getVenusBmpBytes(Bitmap bitmap) {
        if (null == bitmap) {
            return new byte[0];
        }

        Bitmap aBmp = bitmap.copy(Bitmap.Config.ALPHA_8, true);
        Bitmap bmp = BitmapUtil.scaleImage(aBmp, 48, 48);

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        byte[] venusChannel = new byte[width * height];
        int index = 0;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int clr = bmp.getPixel(w, h);
                //LogUtil.i("r-" + Color.red(clr) + " g-" + Color.green(clr) + " b-" + Color.blue(clr) + " a-" + Color.alpha(clr));
                venusChannel[index++] = (byte) Color.alpha(clr);
            }
        }
        return venusChannel;
    }

    private AMapNaviListener aMapNaviListener = new AMapNaviListener() {
        @Override
        public void onInitNaviFailure() {
            LogUtil.mark();
        }

        @Override
        public void onInitNaviSuccess() {
            LogUtil.mark();
        }

        @Override
        public void onStartNavi(int i) {
            LogUtil.mark();
        }

        @Override
        public void onTrafficStatusUpdate() {
            LogUtil.mark();
        }

        @Override
        public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
            LogUtil.mark();
            mCurSpeed = aMapNaviLocation.getSpeed();
        }

        @Override
        public void onGetNavigationText(int i, String s) {
            LogUtil.mark();
        }

        @Override
        public void onGetNavigationText(String s) {
            LogUtil.mark();
        }

        @Override
        public void onEndEmulatorNavi() {
            LogUtil.mark();
        }

        @Override
        public void onArriveDestination() {
            LogUtil.mark();
        }

        @Override
        public void onCalculateRouteFailure(int i) {
            LogUtil.mark();
        }

        @Override
        public void onReCalculateRouteForYaw() {
            LogUtil.mark();
        }

        @Override
        public void onReCalculateRouteForTrafficJam() {
            LogUtil.mark();
        }

        @Override
        public void onArrivedWayPoint(int i) {
            LogUtil.mark();
        }

        @Override
        public void onGpsOpenStatus(boolean b) {
            LogUtil.mark();
        }

        @Override
        public void onNaviInfoUpdate(NaviInfo naviInfo) {
            LogUtil.i(naviInfo.getCurStepRetainDistance() + "m : 图标（" + naviInfo.getIconType() + "） " + naviInfo.getNextRoadName() + " " + naviInfo.getIconBitmap());

            VNNavInfo navInfo = new VNNavInfo();
            navInfo.setIconType(naviInfo.getIconType());
            // 如果导航图标发生变化，再传输新的图标
            if (mLastIconType != navInfo.getIconType()) {
                Bitmap bmp = naviInfo.getIconBitmap();
                if (null == bmp) { // 如果SDK回调中没有图像信息，则从本地组件中获取
                    if (mIconNttvLocal.getDrawable() instanceof BitmapDrawable) {
                        bmp = ((BitmapDrawable) mIconNttvLocal.getDrawable()).getBitmap();
                    }
                }

                if (null != bmp) {
                    byte[] bmpBytes = getVenusBmpBytes(bmp);
                    //writeBytesToBinaryFile(bmpBytes, naviinfo.getIconType());
                    String base64Bmp = Base64.encodeToString(bmpBytes, Base64.DEFAULT);
                    navInfo.setIconBitmap(base64Bmp);
                }
            }
            navInfo.setCurStepRetainDistance(formatDistance(naviInfo.getCurStepRetainDistance()));
            navInfo.setNextRoadName(naviInfo.getNextRoadName());
            navInfo.setSpeed(String.format("%.1f", mCurSpeed) + " km/h");
            navInfo.setRemainDistance(formatDistance(naviInfo.getPathRetainDistance()));
            navInfo.setRemainTime(formatTimeFromSeconds(naviInfo.getPathRetainTime()));
            navInfo.setNavMode(1);

            updateNavInfo(navInfo);
            //updateHealthInfo();

            mLastIconType = navInfo.getIconType();
        }

        @Override
        public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
            LogUtil.mark();
        }

        @Override
        public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {
            LogUtil.mark();
        }

        @Override
        public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {
            LogUtil.mark();
        }

        @Override
        public void showCross(AMapNaviCross aMapNaviCross) {
            LogUtil.mark();
        }

        @Override
        public void hideCross() {
            LogUtil.mark();
        }

        @Override
        public void showModeCross(AMapModelCross aMapModelCross) {
            LogUtil.mark();
        }

        @Override
        public void hideModeCross() {
            LogUtil.mark();
        }

        @Override
        public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
            LogUtil.mark();
        }

        @Override
        public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
            LogUtil.mark();
        }

        @Override
        public void hideLaneInfo() {
            LogUtil.mark();
        }

        @Override
        public void onCalculateRouteSuccess(int[] ints) {
            LogUtil.mark();
        }

        @Override
        public void notifyParallelRoad(int i) {
            LogUtil.mark();
        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
            LogUtil.mark();
        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
            LogUtil.mark();
        }

        @Override
        public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
            LogUtil.mark();
        }

        @Override
        public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
            LogUtil.mark();
        }

        @Override
        public void onPlayRing(int i) {
            LogUtil.mark();
        }

        @Override
        public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
            LogUtil.mark();
        }

        @Override
        public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {
            LogUtil.mark();
        }

        @Override
        public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {
            LogUtil.mark();
        }

        @Override
        public void onGpsSignalWeak(boolean b) {
            LogUtil.mark();
        }
    };

    /* INaviInfoCallback START */
    public static INaviInfoCallback iNaviInfoCallback = new INaviInfoCallback() {
        @Override
        public void onInitNaviFailure() {
            LogUtil.mark();
        }

        @Override
        public void onGetNavigationText(String s) {
            LogUtil.mark();
        }

        @Override
        public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
            LogUtil.mark();
            mCurSpeed = aMapNaviLocation.getSpeed();
        }

        @Override
        public void onArriveDestination(boolean b) {
            LogUtil.mark();
        }

        @Override
        public void onStartNavi(int i) {
            LogUtil.mark();
        }

        @Override
        public void onCalculateRouteSuccess(int[] ints) {
            LogUtil.mark();
        }

        @Override
        public void onCalculateRouteFailure(int i) {
            LogUtil.mark();
        }

        @Override
        public void onStopSpeaking() {
            LogUtil.mark();
        }

        @Override
        public void onReCalculateRoute(int i) {
            LogUtil.mark();
        }

        @Override
        public void onExitPage(int i) {
            LogUtil.mark();
        }

        @Override
        public void onStrategyChanged(int i) {
            LogUtil.mark();
        }

        @Override
        public void onArrivedWayPoint(int i) {
            LogUtil.mark();
        }

        @Override
        public void onMapTypeChanged(int i) {
            LogUtil.mark();
        }

        @Override
        public void onNaviDirectionChanged(int i) {
            LogUtil.mark();
        }

        @Override
        public void onDayAndNightModeChanged(int i) {
            LogUtil.mark();
        }

        @Override
        public void onBroadcastModeChanged(int i) {
            LogUtil.mark();
        }

        @Override
        public void onScaleAutoChanged(boolean b) {
            LogUtil.mark();
        }

        @Override
        public View getCustomMiddleView() {
            LogUtil.mark();
            return null;
        }

        @Override
        public View getCustomNaviView() {
            LogUtil.mark();
            return null;
        }

        @Override
        public View getCustomNaviBottomView() {
            LogUtil.mark();
            return null;
        }
    };
    /* INaviInfoCallback END */
}
