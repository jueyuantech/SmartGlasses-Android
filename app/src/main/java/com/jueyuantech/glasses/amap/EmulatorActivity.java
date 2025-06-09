package com.jueyuantech.glasses.amap;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.ParallelRoadListener;
import com.amap.api.navi.enums.AMapNaviParallelRoadStatus;
import com.amap.api.navi.enums.NaviType;
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
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.NextTurnTipView;
import com.google.gson.Gson;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.VenusAppBaseActivity;
import com.jueyuantech.glasses.util.BitmapUtil;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNHealthInfo;
import com.jueyuantech.venussdk.bean.VNNavInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmulatorActivity extends VenusAppBaseActivity implements AMapNaviListener, AMapNaviViewListener, ParallelRoadListener {

    private Gson gson = new Gson();
    private Random random = new Random();
    private int mLastIconType = -1;
    private ImageView mIconNttvCloud;
    private NextTurnTipView mIconNttvLocal;

    protected AMapNaviView mAMapNaviView;
    protected AMapNavi mAMapNavi;
    protected TTSController mTtsManager;
    protected NaviLatLng mEndLatlng = new NaviLatLng(40.084894,116.603039);
    protected NaviLatLng mStartLatlng = new NaviLatLng(39.825934,116.342972);
    NaviLatLng p1 = new NaviLatLng(39.831135,116.36056);//北京国际文化城
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    protected List<NaviLatLng> mWayPointList = new ArrayList<NaviLatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_basic_navi);

        // 这一步操作就设置高德地图中的隐私合规，不然可能会出现地图无法正确加载的问题
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        try {
            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
            mAMapNavi.addAMapNaviListener(this);
            mAMapNavi.addParallelRoadListener(this);
            mAMapNavi.setUseInnerVoice(true, true);

            //设置模拟导航的行车速度
            mAMapNavi.setEmulatorNaviSpeed(75);
            sList.add(mStartLatlng);
            eList.add(mEndLatlng);
        } catch (AMapException e) {
            e.printStackTrace();
        }

        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        mIconNttvCloud = findViewById(R.id.nttv_icon_cloud);
        mIconNttvLocal = findViewById(R.id.nttv_icon_local);

        boolean isUseInnerVoice = getIntent().getBooleanExtra("useInnerVoice", false);

        if (isUseInnerVoice) {
            /**
             * 设置使用内部语音播报，
             * 使用内部语音播报，用户注册的AMapNaviListener中的onGetNavigationText 方法将不再回调
             */
            if (mAMapNavi != null) {
                mAMapNavi.setUseInnerVoice(isUseInnerVoice);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAMapNaviView.onResume();

        //printPath();
        startWork();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAMapNaviView.onPause();

        stopWork();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAMapNaviView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        if (mAMapNavi!=null){
            mAMapNavi.stopNavi();
            mAMapNavi.destroy();
        }
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.MAP, null);
    }

    @Override
    protected void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, null);
    }

    @Override
    protected void notifyExitFromVenus() {
        // do nothing
    }

    @Override
    public void onInitNaviSuccess() {
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAMapNavi.calculateDriveRoute(sList, eList, mWayPointList, strategy);

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {
        mAMapNavi.startNavi(NaviType.EMULATOR);
    }

    @Override
    public void onGetNavigationText(int type, String text) {
        //播报类型和播报文字回调
        LogUtil.i("TYPE : " + type + " text : " + text);
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
        LogUtil.i(naviinfo.getCurStepRetainDistance() + "m : 图标（" + naviinfo.getIconType() + "） " + naviinfo.getNextRoadName() + " " + naviinfo.getIconBitmap());

        mIconNttvCloud.setImageBitmap(naviinfo.getIconBitmap());
        mIconNttvLocal.setIconType(naviinfo.getIconType());

        VNNavInfo navInfo = new VNNavInfo();
        navInfo.setIconType(naviinfo.getIconType());
        // 如果导航图标发生变化，再传输新的图标
        if (mLastIconType != navInfo.getIconType()) {
            Bitmap bmp = naviinfo.getIconBitmap();
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
        navInfo.setCurStepRetainDistance(formatDistance(naviinfo.getCurStepRetainDistance()));
        navInfo.setNextRoadName(naviinfo.getNextRoadName());
        navInfo.setSpeed((75.0 + random.nextInt(5)) + " km/h");

        updateNavInfo(navInfo);
        updateHealthInfo();

        mLastIconType = navInfo.getIconType();
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

    private String base64Bitmap(Bitmap bitmap) {
        if (null != bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        return null;
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

    private void printPath() {
        File filesDir = getFilesDir();
        LogUtil.i("filesDir " + filesDir.getAbsolutePath());

        File cacheDir = getCacheDir();
        LogUtil.i("cacheDir " + cacheDir.getAbsolutePath());

        File externalFilesDir = getExternalFilesDir(null); // 返回的是外部存储的公共目录下的私有目录
        LogUtil.i("externalFilesDir " + externalFilesDir.getAbsolutePath());

        File[] externalFilesDirs = getExternalFilesDirs(null);
        for (File file : externalFilesDirs) {
            LogUtil.i("externalFilesDirs " + file.getAbsolutePath());
        }

        File externalCacheDir = getExternalCacheDir();
        LogUtil.i("externalCacheDir " + externalCacheDir.getAbsolutePath());
    }

    public void writeBytesToBinaryFile(byte[] bytes, int iconId) {
        File file = new File(getExternalFilesDir(null), String.valueOf(iconId));
        file.deleteOnExit();

        FileOutputStream fileOutputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭文件输出流
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* ============================================================== */

    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartNavi(int type) {
        //开始导航回调
    }

    @Override
    public void onTrafficStatusUpdate() {
        //
    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {
        //当前位置回调
        //location.getSpeed();
    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {
        //结束模拟导航
    }

    @Override
    public void onArriveDestination() {
        //到达目的地
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {

    }

    @Override
    public void onReCalculateRouteForYaw() {
        //偏航后重新计算路线回调
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        //拥堵后重新计算路线回调
    }

    @Override
    public void onArrivedWayPoint(int wayID) {
        //到达途径点
    }

    @Override
    public void onGpsOpenStatus(boolean enabled) {
        //GPS开关状态回调
    }

    @Override
    public void onNaviSetting() {
        //底部导航设置点击回调
    }

    @Override
    public void onNaviMapMode(int naviMode) {
        //导航态车头模式，0:车头朝上状态；1:正北朝上模式。
    }

    @Override
    public void onNaviCancel() {
        finish();
    }


    @Override
    public void onNaviTurnClick() {
        //转弯view的点击回调
    }

    @Override
    public void onNextRoadClick() {
        //下一个道路View点击回调
    }


    @Override
    public void onScanViewButtonClick() {
        //全览按钮点击回调
    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] amapServiceAreaInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        //已过时
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        //显示放大图回调
    }

    @Override
    public void hideCross() {
        //隐藏放大图回调
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        //显示车道信息

    }

    @Override
    public void hideLaneInfo() {
        //隐藏车道信息
    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        //多路径算路成功回调
    }

    @Override
    public void notifyParallelRoad(int i) {
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        //更新交通设施信息
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        //更新巡航模式的统计信息
    }


    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        //更新巡航模式的拥堵信息
    }

    @Override
    public void onPlayRing(int i) {

    }


    @Override
    public void onLockMap(boolean isLock) {
        //锁地图状态发生变化时回调
    }

    @Override
    public void onNaviViewLoaded() {
        Log.d("wlx", "导航页面加载成功");
        Log.d("wlx", "请不要使用AMapNaviView.getMap().setOnMapLoadedListener();会overwrite导航SDK内部画线逻辑");
    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }


    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {
    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult result) {
        //路线计算失败
        Log.e("dm", "--------------------------------------------");
        Log.i("dm", "路线计算失败：错误码=" + result.getErrorCode() + ",Error Message= " + result.getErrorDescription());
        Log.i("dm", "错误码详细链接见：http://lbs.amap.com/api/android-navi-sdk/guide/tools/errorcode/");
        Log.e("dm", "--------------------------------------------");
        Toast.makeText(this, "errorInfo：" + result.getErrorDetail() + ", Message：" + result.getErrorDescription(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }

    @Override
    public void notifyParallelRoad(AMapNaviParallelRoadStatus aMapNaviParallelRoadStatus) {
        if (aMapNaviParallelRoadStatus.getmElevatedRoadStatusFlag() == 1) {
            Toast.makeText(this, "当前在高架上", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在高架上");
        } else if (aMapNaviParallelRoadStatus.getmElevatedRoadStatusFlag() == 2) {
            Toast.makeText(this, "当前在高架下", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在高架下");
        }

        if (aMapNaviParallelRoadStatus.getmParallelRoadStatusFlag() == 1) {
            Toast.makeText(this, "当前在主路", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在主路");
        } else if (aMapNaviParallelRoadStatus.getmParallelRoadStatusFlag() == 2) {
            Toast.makeText(this, "当前在辅路", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在辅路");
        }
    }
}
