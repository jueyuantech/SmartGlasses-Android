package com.jueyuantech.glasses;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.Gson;
import com.jueyuantech.glasses.bean.PrompterConfig;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.IVNFileUploadTask;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.bean.FontConfig;
import com.jueyuantech.venussdk.bean.VNPrompterPage;
import com.jueyuantech.venussdk.cb.VNCommonCallBack;
import com.jueyuantech.venussdk.listener.VNFileUploadListener;
import com.jueyuantech.venussdk.listener.VNPrompterListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.Layout;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import androidx.core.content.ContextCompat;

public class PrompterActivity extends VenusAppBaseActivity implements View.OnClickListener {

    private static long FILE_SIZE_MAX = 1024 * 50;

    private ImageView mBackIv;
    private ImageView mHelpIv;
    private TextView mContentTv;
    private ImageView mNextBtn, mPreBtn;
    private ImageView mFileChooserBtn;

    private AiwaysGestureManager aiwaysGestureManager;
    private View mControlV;

    private File mPrompterDir;
    private File mLocalFile;
    private IVNFileUploadTask vnFileUploadTask;
    
    // 定时器相关
    private Handler mLogHandler;
    private Runnable mLogRunnable;
    private static final int LOG_INTERVAL = 30 * 1000; // 30秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mPrompterDir = getExternalFilesDir("VenusPrompter");
        if (!mPrompterDir.exists()) {
            mPrompterDir.mkdirs();
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mHelpIv = findViewById(R.id.iv_help);
        mHelpIv.setOnClickListener(this);

        mContentTv = findViewById(R.id.tv_content);
        mNextBtn = findViewById(R.id.iv_next);
        mNextBtn.setOnClickListener(this);
        mPreBtn = findViewById(R.id.iv_pre);
        mPreBtn.setOnClickListener(this);
        mFileChooserBtn = findViewById(R.id.iv_text);
        mFileChooserBtn.setOnClickListener(this);

        aiwaysGestureManager = new AiwaysGestureManager(this, aiwaysGestureListener);
        mControlV = findViewById(R.id.v_control);
        mControlV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                aiwaysGestureManager.onTouchEvent(event);
                return true;
            }
        });

        loadPrompterConfig();
        
        // 初始化定时器
        initLogTimer();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        VNCommon.setPrompterListener(vnPrompterListener);
        
        // 启动定时器
        startLogTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // 停止定时器
        stopLogTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 清理定时器资源
        if (mLogHandler != null) {
            mLogHandler.removeCallbacksAndMessages(null);
            mLogHandler = null;
        }
        mLogRunnable = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_pre:
                prePage();
                break;
            case R.id.iv_next:
                nextPage();
                break;
            case R.id.iv_text:
                chooseFile();
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_help:
                showConfigDialog();
                break;
            default:
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_SCROLL:
                if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
                    prePage();
                } else {
                    nextPage();
                }
                return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            long fileSize = getFileSize(uri);

            if (fileSize > FILE_SIZE_MAX) {
                ToastUtil.toast(PrompterActivity.this, getString(R.string.prompter_file_size_oom));
            } else {
                ToastUtil.toast(PrompterActivity.this, getString(R.string.prompter_file_loading));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLocalFile = saveFileFromUri(uri);
                        setPrompterFile(mLocalFile);
                    }
                }, 1000);
            }
        }
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.PROMPTER, new VNCommonCallBack() {
            @Override
            public void onSuccess() {
                LogUtil.mark();
            }

            @Override
            public void onTimeOut() {
                LogUtil.mark();
            }

            @Override
            public void onFailed() {
                LogUtil.mark();
            }
        });
    }

    @Override
    protected void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, new VNCommonCallBack() {
            @Override
            public void onSuccess() {
                LogUtil.mark();
            }

            @Override
            public void onTimeOut() {
                LogUtil.mark();
            }

            @Override
            public void onFailed() {
                LogUtil.mark();
            }
        });
    }

    @Override
    protected void notifyExitFromVenus() {
        // do nothing
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");//筛选器
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "选择一个文件"), 1);
    }

    private String loadContentFromFile(File file) {
        try {
            StringBuilder result = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (!firstLine) {
                    result.append("\n"); // 使用Unix风格换行符，与saveFileFromUri保持一致
                }
                result.append(line);
                firstLine = false;
            }
            br.close();
            return result.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex);
                    }
                }
            }
        }
        if (null == fileName) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    private long getFileSize(Uri uri) {
        long fileSize = 0;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex);
                    }
                }
                cursor.close();
            }
        }
        if (fileSize == 0) {
            File file = new File(uri.getPath());
            if (file.exists()) {
                fileSize = file.length();
            }
        }
        return fileSize;
    }

    private File saveFileFromUri(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        File file = new File(mPrompterDir, fileName);
        InputStream is = null;
        BufferedReader reader = null;
        FileWriter writer = null;

        try {
            is = getContentResolver().openInputStream(uri);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            writer = new FileWriter(file);

            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    writer.write("\n"); // 使用Unix风格换行符
                }
                writer.write(line);
                firstLine = false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    private void setPrompterConfig() {
        int contextLines = prompterConfig.isShowContext() ? 1 : 0;
        VNCommon.setPrompterContextLine(contextLines, null);

        FontConfig fontConfig = new FontConfig();
        fontConfig.setWeight(prompterConfig.getFontSize());
        fontConfig.setRowSpace(8);
        fontConfig.setWordSpace(1);
        VNCommon.setFontConfig(VNConstant.View.PROMPTER, fontConfig, null);
    }

    private void setPrompterFile(File file) {

        setPrompterConfig();
        VNCommon.setPrompterFile(file, new VNCommonCallBack() {
            @Override
            public void onSuccess() {
                setPrompterPage();
                mContentTv.setText(loadContentFromFile(mLocalFile));
            }

            @Override
            public void onTimeOut() {
                ToastUtil.toast(PrompterActivity.this, "Timeout");
                showUploadDialog(mLocalFile);
            }

            @Override
            public void onFailed() {
                ToastUtil.toast(PrompterActivity.this, "onFailed");
                showUploadDialog(mLocalFile);
            }
        });
    }

    private void setPrompterPage() {
        VNPrompterPage vnPrompterPage = new VNPrompterPage();
        VNPrompterPage.PreTextBean preTextBean = new VNPrompterPage.PreTextBean();
        preTextBean.setStart(0);
        preTextBean.setEnd(0);
        preTextBean.setContent("");
        VNPrompterPage.CurTextBean curTextBean = new VNPrompterPage.CurTextBean();
        curTextBean.setStart(0);
        curTextBean.setEnd(0);
        curTextBean.setContent("");
        VNPrompterPage.NextTextBean nextTextBean = new VNPrompterPage.NextTextBean();
        nextTextBean.setStart(0);
        nextTextBean.setEnd(0);
        nextTextBean.setContent("");
        vnPrompterPage.setPreText(preTextBean);
        vnPrompterPage.setCurText(curTextBean);
        vnPrompterPage.setNextText(nextTextBean);
        vnPrompterPage.setCurrent(1);
        VNCommon.setPrompterPage(vnPrompterPage, new VNCommonCallBack() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onTimeOut() {

            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void uploadFile() {
        vnFileUploadTask = VNCommon.createFileTransferTask();
        vnFileUploadTask.init(mLocalFile, VNConstant.VNFile.Type.PROMPTER, new VNFileUploadListener() {
            @Override
            public void onError(int i, String s) {
                LogUtil.i("i " + i + " s " + s);
                showRetryDialog();
            }

            @Override
            public void onComplete() {
                dismissProgressDialog();
                ToastUtil.toast(PrompterActivity.this, "Finish");
                setPrompterFile(mLocalFile);
            }

            @Override
            public void onProgress(int cur, int total) {
                LogUtil.i(cur + "/" + total);
                showProgressDialog(cur + "/" + total);
            }
        });
        vnFileUploadTask.start();
    }


    private ProgressDialog mProgressDialog;

    private void showProgressDialog(String message) {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        mProgressDialog.setMessage(message);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();

            WindowManager.LayoutParams lp = mProgressDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mProgressDialog.getWindow().setAttributes(lp);
        }
    }

    private void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void showRetryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Failed, retry?");
        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissProgressDialog();
            }
        });
        builder.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (null != vnFileUploadTask) {
                    vnFileUploadTask.retry();
                }
            }
        });

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    private void showUploadDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload");
        builder.setMessage(file.getName());
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadFile();
            }
        });

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    private void nextPage() {
        VNCommon.setPrompterPageDown(null);
    }

    private void prePage() {
        VNCommon.setPrompterPageUp(null);
    }

    private Gson gson = new Gson();
    private VNPrompterListener vnPrompterListener = new VNPrompterListener() {
        @Override
        public void onPageChanged(VNPrompterPage vnPrompterPage) {
            LogUtil.i(" -- " + gson.toJson(vnPrompterPage));
            LogUtil.i(" -- PRE " + vnPrompterPage.getPreText().getContent());
            LogUtil.i(" -- CUR " + vnPrompterPage.getCurText().getContent());
            LogUtil.i(" -- NEXT " + vnPrompterPage.getNextText().getContent());

            int curTextStartIdx = vnPrompterPage.getCurText().getStart();
            int curTextEndIdx = vnPrompterPage.getCurText().getEnd();
            int preTextStartIdx = vnPrompterPage.getPreText().getStart();
            int preTextEndIdx = vnPrompterPage.getPreText().getEnd();
            int nextTextStartIdx = vnPrompterPage.getNextText().getStart();
            int nextTextEndIdx = vnPrompterPage.getNextText().getEnd();
            
            // 高亮显示当前文本区间以及前后文本
            highlightAllTexts(preTextStartIdx, preTextEndIdx, curTextStartIdx, curTextEndIdx, nextTextStartIdx, nextTextEndIdx);
        }

        @Override
        public void onLoadStart() {
            LogUtil.mark();
            showProgressDialog("Loading...");
        }

        @Override
        public void onLoadEnd() {
            LogUtil.mark();
            dismissProgressDialog();
        }
    };

    private AiwaysGestureManager.AiwaysGestureListener aiwaysGestureListener = new AiwaysGestureManager.AiwaysGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }

        @Override
        public boolean singleFingeronSlipProcess(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            return false;
        }

        @Override
        public boolean singleFingerSlipAction(AiwaysGestureManager.GestureEvent gestureEvent, MotionEvent startEvent, MotionEvent endEvent, float velocity) {
            switch (gestureEvent) {
                case SINGLE_GINGER_LEFT_SLIP:
                    break;
                case SINGLE_GINGER_RIGHT_SLIP:
                    break;
                case SINGLE_GINGER_UP_SLIP:
                    nextPage();
                    break;
                case SINGLE_GINGER_DOWN_SLIP:
                    prePage();
                    break;
                default:
            }
            return false;
        }

        @Override
        public boolean mutiFingerSlipProcess(AiwaysGestureManager.GestureEvent gestureEvent, float startX, float startY, float endX, float endY, float moveX, float moveY) {
            return false;
        }

        @Override
        public boolean mutiFingerSlipAction(AiwaysGestureManager.GestureEvent gestureEvent, float startX, float startY, float endX, float endY, float velocityX, float velocityY) {
            return false;
        }
    };

    private static final String MMKV_KEY_PROMPTER_CONFIG = "prompterConfig";
    private PrompterConfig prompterConfig;

    private void loadPrompterConfig() {
        PrompterConfig config = (PrompterConfig) MmkvUtil.getInstance().decodeParcelable(MMKV_KEY_PROMPTER_CONFIG, PrompterConfig.class);
        if (null == config) {
            prompterConfig = new PrompterConfig();
            prompterConfig.setCustomFontSize(false);
            prompterConfig.setFontSize(24);
            prompterConfig.setShowContext(true);
        } else {
            prompterConfig = config;
        }
    }

    private void savePrompterConfig() {
        MmkvUtil.getInstance().encodeParcelable(MMKV_KEY_PROMPTER_CONFIG, prompterConfig);
    }

    private AlertDialog.Builder configDialogBuilder;
    private Dialog configDialog;

    private void showConfigDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_prompter_config, null);

        SwitchCompat showContextSwitch = dialogView.findViewById(R.id.switch_show_context);
        showContextSwitch.setChecked(prompterConfig.isShowContext());

        ImageView fontSizeIncrease = dialogView.findViewById(R.id.iv_font_size_increase);
        ImageView fontSizeDecrease = dialogView.findViewById(R.id.iv_font_size_decrease);
        TextView fontSizeTv = dialogView.findViewById(R.id.tv_font_size);
        fontSizeTv.setText(String.valueOf(prompterConfig.getFontSize()));

        Button closeBtn = dialogView.findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissConfigDialog();
                finish();
            }
        });

        showContextSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prompterConfig.setShowContext(isChecked);
                savePrompterConfig();
            }
        });

        fontSizeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompterConfig.setFontSize(prompterConfig.getFontSize() + 1);
                savePrompterConfig();
                fontSizeTv.setText(String.valueOf(prompterConfig.getFontSize()));
            }
        });

        fontSizeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompterConfig.setFontSize(prompterConfig.getFontSize() - 1);
                savePrompterConfig();
                fontSizeTv.setText(String.valueOf(prompterConfig.getFontSize()));
            }
        });

        configDialogBuilder = new AlertDialog.Builder(this);
        configDialogBuilder.setView(dialogView);
        configDialog = configDialogBuilder.create();
        configDialog.show();

        Window window = configDialog.getWindow();
        if (null != window) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 10;
            lp.gravity = Gravity.BOTTOM;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.3f);
        }
    }

    private void dismissConfigDialog() {
        if (null != configDialog) {
            configDialog.dismiss();
        }
    }

    /**
     * 高亮显示所有文本区间（前文、当前文本、后文）
     * @param preStartByteIndex 前文字节起始位置
     * @param preEndByteIndex 前文字节结束位置
     * @param curStartByteIndex 当前文本字节起始位置
     * @param curEndByteIndex 当前文本字节结束位置
     * @param nextStartByteIndex 后文字节起始位置
     * @param nextEndByteIndex 后文字节结束位置
     */
    private void highlightAllTexts(int preStartByteIndex, int preEndByteIndex, 
                                  int curStartByteIndex, int curEndByteIndex,
                                  int nextStartByteIndex, int nextEndByteIndex) {
        if (mLocalFile == null || !mLocalFile.exists()) {
            return;
        }
        
        try {
            // 读取文件内容
            String content = loadContentFromFile(mLocalFile);
            if (content == null || content.isEmpty()) {
                return;
            }
            
            byte[] contentBytes = content.getBytes("UTF-8");
            SpannableString spannableString = new SpannableString(content);
            
            // 定义颜色
            int currentHighlightColor = ContextCompat.getColor(this, R.color.bg_main_gradient_start); // 当前文本高亮色
            int contextHighlightColor = ContextCompat.getColor(this, R.color.bg_main_gradient_end); // 前后文本高亮色（更浅）
            
            // 高亮前文
            if (preStartByteIndex >= 0 && preEndByteIndex > preStartByteIndex && preEndByteIndex <= contentBytes.length) {
                int preCharStart = convertByteIndexToCharIndex(content, preStartByteIndex);
                int preCharEnd = convertByteIndexToCharIndex(content, preEndByteIndex);
                if (preCharStart >= 0 && preCharEnd <= content.length() && preCharStart < preCharEnd) {
                    BackgroundColorSpan preSpan = new BackgroundColorSpan(contextHighlightColor);
                    spannableString.setSpan(preSpan, preCharStart, preCharEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    LogUtil.i("Highlighted preText from char " + preCharStart + " to " + preCharEnd);
                }
            }
            
            // 高亮当前文本
            if (curStartByteIndex >= 0 && curEndByteIndex > curStartByteIndex && curEndByteIndex <= contentBytes.length) {
                int curCharStart = convertByteIndexToCharIndex(content, curStartByteIndex);
                int curCharEnd = convertByteIndexToCharIndex(content, curEndByteIndex);
                if (curCharStart >= 0 && curCharEnd <= content.length() && curCharStart < curCharEnd) {
                    BackgroundColorSpan curSpan = new BackgroundColorSpan(currentHighlightColor);
                    spannableString.setSpan(curSpan, curCharStart, curCharEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    LogUtil.i("Highlighted curText from char " + curCharStart + " to " + curCharEnd);
                }
            }
            
            // 高亮后文
            if (nextStartByteIndex >= 0 && nextEndByteIndex > nextStartByteIndex && nextEndByteIndex <= contentBytes.length) {
                int nextCharStart = convertByteIndexToCharIndex(content, nextStartByteIndex);
                int nextCharEnd = convertByteIndexToCharIndex(content, nextEndByteIndex);
                if (nextCharStart >= 0 && nextCharEnd <= content.length() && nextCharStart < nextCharEnd) {
                    BackgroundColorSpan nextSpan = new BackgroundColorSpan(contextHighlightColor);
                    spannableString.setSpan(nextSpan, nextCharStart, nextCharEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    LogUtil.i("Highlighted nextText from char " + nextCharStart + " to " + nextCharEnd);
                }
            }
            
            // 设置到TextView
            mContentTv.setText(spannableString);
            
            // 滚动到当前文本位置
            if (curStartByteIndex >= 0 && curEndByteIndex > curStartByteIndex && curEndByteIndex <= contentBytes.length) {
                int curCharStart = convertByteIndexToCharIndex(content, curStartByteIndex);
                if (curCharStart >= 0 && curCharStart < content.length()) {
                    scrollToCharPosition(curCharStart);
                }
            }
            
        } catch (Exception e) {
            LogUtil.i("Error highlighting all texts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 将字节索引转换为字符索引
     * @param content 文本内容
     * @param byteIndex 字节索引
     * @return 字符索引
     */
    private int convertByteIndexToCharIndex(String content, int byteIndex) {
        try {
            byte[] contentBytes = content.getBytes("UTF-8");
            
            if (byteIndex <= 0) {
                return 0;
            }
            
            if (byteIndex >= contentBytes.length) {
                return content.length();
            }
            
            // 逐字符检查，找到对应的字符位置
            int currentByteIndex = 0;
            for (int charIndex = 0; charIndex < content.length(); charIndex++) {
                String currentChar = content.substring(charIndex, charIndex + 1);
                byte[] charBytes = currentChar.getBytes("UTF-8");
                
                if (currentByteIndex + charBytes.length > byteIndex) {
                    return charIndex;
                }
                
                currentByteIndex += charBytes.length;
                
                if (currentByteIndex == byteIndex) {
                    return charIndex + 1;
                }
            }
            
            return content.length();
        } catch (Exception e) {
            LogUtil.i("Error converting byte index to char index: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 滚动到指定字符位置，使其显示在控件中间
     * @param charPosition 字符位置
     */
    private void scrollToCharPosition(int charPosition) {
        if (mContentTv == null || mContentTv.getLayout() == null) {
            LogUtil.i("TextView or Layout is null, cannot scroll");
            return;
        }
        
        try {
            Layout layout = mContentTv.getLayout();
            
            // 获取字符位置对应的行号
            int line = layout.getLineForOffset(charPosition);
            
            // 获取该行的顶部位置
            int lineTop = layout.getLineTop(line);
            
            // 计算TextView的高度
            int textViewHeight = mContentTv.getHeight();
            
            // 计算滚动位置，使目标行显示在控件中间
            int scrollY = lineTop - (textViewHeight / 2);
            
            // 确保滚动位置不超出边界
            int maxScrollY = Math.max(0, mContentTv.getLayout().getHeight() - textViewHeight);
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            
            LogUtil.i("Scrolling to char position " + charPosition + ", line " + line + ", scrollY " + scrollY);
            
            // 使用平滑滚动
            smoothScrollTo(scrollY);
            
        } catch (Exception e) {
            LogUtil.i("Error scrolling to char position: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 平滑滚动到指定Y位置
     * @param targetY 目标Y位置
     */
    private void smoothScrollTo(int targetY) {
        if (mContentTv == null) {
            return;
        }
        
        int startY = mContentTv.getScrollY();
        
        ValueAnimator animator = ValueAnimator.ofInt(startY, targetY);
        animator.setDuration(500); // 500毫秒动画时长
        animator.setInterpolator(new DecelerateInterpolator());
        
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                mContentTv.scrollTo(0, animatedValue);
            }
        });
        
        animator.start();
        LogUtil.i("Started smooth scroll from " + startY + " to " + targetY);
    }
    
    /**
     * 初始化定时器
     */
    private void initLogTimer() {
        mLogHandler = new Handler();
        mLogRunnable = new Runnable() {
            @Override
            public void run() {
                // 打印log
                LogUtil.i("PrompterActivity is running - " + System.currentTimeMillis());
                VNCommon.sendKeepAlive();
                
                // 继续下一次定时任务
                if (mLogHandler != null) {
                    mLogHandler.postDelayed(this, LOG_INTERVAL);
                }
            }
        };
    }
    
    /**
     * 启动定时器
     */
    private void startLogTimer() {
        if (mLogHandler != null && mLogRunnable != null) {
            // 先停止之前的定时器，避免重复启动
            mLogHandler.removeCallbacks(mLogRunnable);
            // 启动新的定时器
            mLogHandler.postDelayed(mLogRunnable, LOG_INTERVAL);
            LogUtil.i("PrompterActivity log timer started");
        }
    }
    
    /**
     * 停止定时器
     */
    private void stopLogTimer() {
        if (mLogHandler != null && mLogRunnable != null) {
            mLogHandler.removeCallbacks(mLogRunnable);
            LogUtil.i("PrompterActivity log timer stopped");
        }
    }
}