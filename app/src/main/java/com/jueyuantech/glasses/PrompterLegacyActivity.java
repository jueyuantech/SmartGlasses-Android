package com.jueyuantech.glasses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.jueyuantech.glasses.bean.PrompterLegacyCache;
import com.jueyuantech.glasses.bean.PrompterLegacyConfig;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNPrompterLegacyInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PrompterLegacyActivity extends VenusAppBaseActivity implements View.OnClickListener {

    private static long FILE_SIZE_MAX = 1024 * 50;

    private SpannableString spannableStr;
    private int totalPages = 0;
    private int curPage = 0;
    private int totalLines = 0;
    private List<String> linesList = new ArrayList<>();

    private ImageView mBackIv;
    private ImageView mHelpIv;
    private TextView mContentTv;
    private PrompterTextView mContentPtv;
    private ViewTreeObserver mObserver;
    private Layout mContentTvLayout;
    private ImageView mNextBtn, mPreBtn;
    private ImageView mFileChooserBtn;

    private AiwaysGestureManager aiwaysGestureManager;
    private View mControlV;

    private String venusPreTextStr = "";
    private String venusCurTextStr = "";
    private String venusNextTextStr = "";

    private File prompterLegacyFilesDir;
    private String prompterInitTipStr;
    private String promptContentStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompter_legacy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        prompterInitTipStr = getString(R.string.prompter_init_tip);

        prompterLegacyFilesDir = getExternalFilesDir("VenusPrompterLegacyTemp");
        if (!prompterLegacyFilesDir.exists()) {
            prompterLegacyFilesDir.mkdirs();
        }
        loadPrompterLegacyConfig();
        loadPrompterLegacyCache();

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mHelpIv = findViewById(R.id.iv_help);
        mHelpIv.setOnClickListener(this);

        mContentPtv = findViewById(R.id.ptv_content);

        mContentTv = findViewById(R.id.tv_content);
        mContentTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        mObserver = mContentTv.getViewTreeObserver();
        mObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContentTvLayout = mContentTv.getLayout();
                updatePromptContent();
                initPtvConfig();
                initPtvContent();
            }
        });
        mContentTv.setTextSize(prompterLegacyConfig.getFontSize());

        mNextBtn = findViewById(R.id.iv_next);
        mNextBtn.setOnClickListener(this);
        mPreBtn = findViewById(R.id.iv_pre);
        mPreBtn.setOnClickListener(this);
        mFileChooserBtn = findViewById(R.id.iv_text);
        mFileChooserBtn.setOnClickListener(this);

        aiwaysGestureManager = new AiwaysGestureManager(this, listener);
        mControlV = findViewById(R.id.v_control);
        mControlV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                aiwaysGestureManager.onTouchEvent(event);
                return true;
            }
        });

        //testHanLp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadContent(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                ToastUtil.toast(PrompterLegacyActivity.this, getString(R.string.prompter_file_size_oom));
            } else {
                ToastUtil.toast(PrompterLegacyActivity.this, getString(R.string.prompter_file_loading));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadFromUri(uri);
                    }
                }, 1000);
            }
        }
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.PROMPTER, null);
    }

    @Override
    protected void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, null);
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

    private String getFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex);
                    }
                }
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
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

    private void loadFromUri(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        File targetFile = new File(prompterLegacyFilesDir, fileName);
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            is = getContentResolver().openInputStream(uri);
            fos = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            savePrompterLegacyCache(fileName, 0);
            loadContent(true);

            updatePromptContent();
            initPtvContent();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadContent(boolean formActivityResult) {
        if (prompterLegacyConfig.isAutoLoadHistory() || formActivityResult) { // 自动加载缓存开启 或 首次从外部加载
            if (null == prompterLegacyCache) { // 无缓存
                promptContentStr = prompterInitTipStr;
                curPage = 0;
            } else { // 有缓存
                String content = loadContentFromCache();
                if (TextUtils.isEmpty(content)) {
                    promptContentStr = getString(R.string.prompter_cache_load_err_1);
                    curPage = 0;
                } else {
                    promptContentStr = loadContentFromCache();
                    curPage = prompterLegacyCache.getCurPage();
                }
            }
        } else { // 未开启自动加载缓存
            promptContentStr = prompterInitTipStr;
            curPage = 0;
        }
    }

    private String loadContentFromCache() {
        try {
            StringBuilder result = new StringBuilder();
            File prompterFile = new File(prompterLegacyFilesDir, prompterLegacyCache.getFileName());
            BufferedReader br = new BufferedReader(new FileReader(prompterFile));
            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
                result.append(System.getProperty("line.separator")); // TXT添加换行符
            }
            return result.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            removePrompterLegacyCache();
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
            removePrompterLegacyCache();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            removePrompterLegacyCache();
            return null;
        }
    }

    private void updatePromptContent() {
        // 去掉空白行
        //promptContentStr = promptContentStr.replaceAll("(?m)^[ \t]*\r?\n", "");

        spannableStr = new SpannableString(promptContentStr);
        mContentTv.setText(spannableStr);

        totalLines = mContentTvLayout.getLineCount();
        // 向上取整计算总页数
        //totalPages = (totalLines + prompterConfig.getDisplayRows() - 1) / prompterConfig.getDisplayRows();
        try {
            totalPages = calculateTotalPages(totalLines, prompterLegacyConfig.getDisplayRows(), prompterLegacyConfig.isShowPrevious() ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.toast(this, R.string.prompter_file_load_err_1);
            return;
        }

        updateLines();
    }

    private void initPtvConfig() {
        float fontSize = mContentTv.getPaint().getTextSize();
        // 获取行距
        float lineSpacing = mContentTvLayout.getSpacingAdd();
        // 获取行间距倍数
        float lineSpacingMultiplier = mContentTvLayout.getSpacingMultiplier();
        // 行距是行间距倍数乘以字体大小加上额外的行间距
        float totalLineSpacing = lineSpacingMultiplier * fontSize + lineSpacing;

        mContentPtv.updateInit(fontSize, totalLineSpacing);
    }

    private void initPtvContent() {
        linesList.clear();
        for (int i = 0; i < totalLines; i++) {
            int start = mContentTvLayout.getLineStart(i);
            int end = mContentTvLayout.getLineEnd(i);
            try {
                linesList.add(promptContentStr.substring(start, end));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mContentPtv.setData(linesList);

        updateLines();
    }

    private void updatePtvHighlight(int start, int end, int pre, int next) throws Exception {
        mContentPtv.updatePtvHighlight(start, end, pre, next);
    }

    private void nextPage() {
        if (curPage >= (totalPages - 1)) {
            ToastUtil.toast(this, R.string.prompter_slide_bottom_reached);
            return;
        }
        curPage++;
        updateLines();
    }

    private void prePage() {
        if (curPage <= 0) {
            ToastUtil.toast(this, R.string.prompter_slide_top_reached);
            return;
        }
        curPage--;
        updateLines();
    }

    private void updateLines() {
        savePrompterLegacyCache(null, curPage);

        //int startLine = curPage * prompterConfig.getDisplayRows();
        //int endLine = startLine + prompterConfig.getDisplayRows() - 1;
        int startLine;
        int endLine;
        try {
            int[] range = getPageRange(totalLines, prompterLegacyConfig.getDisplayRows(), prompterLegacyConfig.isShowPrevious() ? 1 : 0, curPage);
            startLine = range[0];
            endLine = range[1];
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.i(getString(R.string.prompter_file_load_err_2));
            //ToastUtil.toast(this, R.string.prompter_file_load_err_2);
            return;
        }

        LogUtil.i("PageIdx[" + curPage + "] " + startLine + "-" + endLine);
        if (endLine > (totalLines - 1)) {
            endLine = totalLines - 1;
        }

        int preLine = -1;
        if (startLine > 0) {
            preLine = startLine - 1;
        }
        int nextLine = -1;
        if (endLine < totalLines - 1) {
            nextLine = endLine + 1;
        }

        try {
            updateHighlight(startLine, endLine, preLine, nextLine);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            updatePtvHighlight(startLine, endLine, preLine, nextLine);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            updateVenusPrompter(totalPages, curPage, startLine, endLine, preLine, nextLine);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHighlight(int startLine, int endLine, int preLine, int nextLine) throws Exception {
        // 清除之前的span
        spannableStr.setSpan(new ForegroundColorSpan(getColor(R.color.gray_700)), 0, spannableStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //spannableString.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        for (int line = startLine; line < endLine + 1; line++) {
            int start = mContentTvLayout.getLineStart(line);
            int end = mContentTvLayout.getLineEnd(line);
            spannableStr.setSpan(new ForegroundColorSpan(getColor(R.color.venus_green)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (preLine >= 0) {
            int preStart = mContentTvLayout.getLineStart(preLine);
            int preEnd = mContentTvLayout.getLineEnd(preLine);
            spannableStr.setSpan(new ForegroundColorSpan(getColor(R.color.venus_green_66)), preStart, preEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (nextLine >= 0) {
            int nextStart = mContentTvLayout.getLineStart(nextLine);
            int nextEnd = mContentTvLayout.getLineEnd(nextLine);
            spannableStr.setSpan(new ForegroundColorSpan(getColor(R.color.venus_green_66)), nextStart, nextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mContentTv.setText(spannableStr);
    }

    private void updateVenusPrompter(int total, int current, int startLine, int endLine, int preLine, int nextLine) throws Exception {
        String curTextStr = "";
        for (int line = startLine; line < endLine + 1; line++) {
            String txt = getLineText(line);
            curTextStr += txt;
            if (line != endLine) {
                if (!txt.endsWith("\n")) {
                    //curTextStr += "\n";
                }
            }
        }

        String preTextStr = "";
        if (preLine >= 0) {
            preTextStr = getLineText(preLine);
        }

        String nextTextStr = "";
        if (nextLine >= 0) {
            nextTextStr = getLineText(nextLine);
        }

        if (venusPreTextStr.equals(preTextStr)
                && venusCurTextStr.equals(curTextStr)
                && venusNextTextStr.equals(nextTextStr)) {
            LogUtil.i("Prompter String not changed, ignore this update.");
            return;
        }

        venusPreTextStr = preTextStr;
        venusCurTextStr = curTextStr;
        venusNextTextStr = nextTextStr;

        VNPrompterLegacyInfo prompterLegacyInfo = new VNPrompterLegacyInfo();
        prompterLegacyInfo.setTotal(total);
        prompterLegacyInfo.setCurrent(current);
        //prompterInfo.setPreText(venusPreTextStr);
        prompterLegacyInfo.setCurText(venusCurTextStr);
        //prompterInfo.setNextText(venusNextTextStr);
        VNCommon.updatePrompterLegacy(prompterLegacyInfo, null);

        LogUtil.i("venusPreTextStr: " + venusPreTextStr);
        LogUtil.i("venusCurTextStr: " + venusCurTextStr);
        LogUtil.i("venusNextTextStr: " + venusNextTextStr);
    }

    public String getLineText(int line) {
        int startOffset = mContentTvLayout.getLineStart(line);
        int endOffset = mContentTvLayout.getLineEnd(line);
        return mContentTv.getText().subSequence(startOffset, endOffset).toString();
    }

    private void setTextSizeDp(float textSize) {
        LogUtil.i(textSize);
        mContentTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
    }

    private float getTextSizeDp() {
        return px2dp(mContentTv.getTextSize());
    }

    private static final String MMKV_KEY_PROMPTER_LEGACY_CACHE = "prompterLegacyCache";
    private PrompterLegacyCache prompterLegacyCache = null;

    private void loadPrompterLegacyCache() {
        prompterLegacyCache = (PrompterLegacyCache) MmkvUtil.getInstance().decodeParcelable(MMKV_KEY_PROMPTER_LEGACY_CACHE, PrompterLegacyCache.class);
    }

    private void savePrompterLegacyCache(String name, int page) {
        if (null == prompterLegacyCache) {
            prompterLegacyCache = new PrompterLegacyCache();
        }
        if (!TextUtils.isEmpty(name)) {
            prompterLegacyCache.setFileName(name);
        }
        prompterLegacyCache.setCurPage(page);
        MmkvUtil.getInstance().encodeParcelable(MMKV_KEY_PROMPTER_LEGACY_CACHE, prompterLegacyCache);
    }

    private void removePrompterLegacyCache() {
        MmkvUtil.getInstance().removeKey(MMKV_KEY_PROMPTER_LEGACY_CACHE);
        prompterLegacyCache = null;
    }

    private static final String MMKV_KEY_PROMPTER_LEGACY_CONFIG = "prompterLegacyConfig";
    private PrompterLegacyConfig prompterLegacyConfig;

    private void loadPrompterLegacyConfig() {
        PrompterLegacyConfig config = (PrompterLegacyConfig) MmkvUtil.getInstance().decodeParcelable(MMKV_KEY_PROMPTER_LEGACY_CONFIG, PrompterLegacyConfig.class);
        if (null == config) {
            prompterLegacyConfig = new PrompterLegacyConfig();
            prompterLegacyConfig.setDisplayRows(3);
            prompterLegacyConfig.setShowPrevious(false);
            prompterLegacyConfig.setFontSize(22);
            prompterLegacyConfig.setAutoLoadHistory(false);
        } else {
            prompterLegacyConfig = config;
        }
    }

    private void savePrompterLegacyConfig() {
        MmkvUtil.getInstance().encodeParcelable(MMKV_KEY_PROMPTER_LEGACY_CONFIG, prompterLegacyConfig);
    }

    private float px2dp(float px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.densityDpi / 160f));
    }

    private AlertDialog.Builder configDialogBuilder;
    private Dialog configDialog;

    private void showConfigDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_prompter_legacy_config, null);

        ImageView rowsIncreaseIv = dialogView.findViewById(R.id.iv_display_rows_increase);
        ImageView rowsDecreaseIv = dialogView.findViewById(R.id.iv_display_rows_decrease);
        TextView rowsTv = dialogView.findViewById(R.id.tv_display_rows);
        rowsTv.setText(String.valueOf(prompterLegacyConfig.getDisplayRows()));

        SwitchCompat showPreviousSwitch = dialogView.findViewById(R.id.switch_show_previous);
        showPreviousSwitch.setChecked(prompterLegacyConfig.isShowPrevious());

        ImageView fontSizeIncrease = dialogView.findViewById(R.id.iv_font_size_increase);
        ImageView fontSizeDecrease = dialogView.findViewById(R.id.iv_font_size_decrease);
        TextView fontSizeTv = dialogView.findViewById(R.id.tv_font_size);
        fontSizeTv.setText(String.valueOf(prompterLegacyConfig.getFontSize()));

        SwitchCompat autoLoadSwitch = dialogView.findViewById(R.id.switch_auto_load);
        autoLoadSwitch.setChecked(prompterLegacyConfig.isAutoLoadHistory());

        Button closeBtn = dialogView.findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissConfigDialog();
                finish();
            }
        });

        rowsIncreaseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prompterLegacyConfig.getDisplayRows() >= 6) {
                    return;
                }
                prompterLegacyConfig.setDisplayRows(prompterLegacyConfig.getDisplayRows() + 1);
                savePrompterLegacyConfig();
                rowsTv.setText(String.valueOf(prompterLegacyConfig.getDisplayRows()));

                savePrompterLegacyCache(null, 0);
            }
        });

        rowsDecreaseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prompterLegacyConfig.getDisplayRows() <= 2) {
                    return;
                }
                prompterLegacyConfig.setDisplayRows(prompterLegacyConfig.getDisplayRows() - 1);
                savePrompterLegacyConfig();
                rowsTv.setText(String.valueOf(prompterLegacyConfig.getDisplayRows()));

                savePrompterLegacyCache(null,0);
            }
        });

        showPreviousSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prompterLegacyConfig.setShowPrevious(isChecked);
                savePrompterLegacyConfig();

                savePrompterLegacyCache(null, 0);
            }
        });

        fontSizeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompterLegacyConfig.setFontSize(prompterLegacyConfig.getFontSize() + 1);
                savePrompterLegacyConfig();
                setTextSizeDp(prompterLegacyConfig.getFontSize());
                fontSizeTv.setText(String.valueOf(prompterLegacyConfig.getFontSize()));

                savePrompterLegacyCache(null,0);
            }
        });

        fontSizeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompterLegacyConfig.setFontSize(prompterLegacyConfig.getFontSize() - 1);
                savePrompterLegacyConfig();
                setTextSizeDp(prompterLegacyConfig.getFontSize());
                fontSizeTv.setText(String.valueOf(prompterLegacyConfig.getFontSize()));

                savePrompterLegacyCache(null,0);
            }
        });

        autoLoadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prompterLegacyConfig.setAutoLoadHistory(isChecked);
                savePrompterLegacyConfig();
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
            //设置自身的底板透明
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //设置dialog周围activity背景的透明度，[0f,1f]，0全透明，1不透明黑
            window.setDimAmount(0.3f);
        }
    }

    private void dismissConfigDialog() {
        if (null != configDialog) {
            configDialog.dismiss();
        }
    }

    private void testHanLp() {
        LogUtil.i("start " + System.currentTimeMillis());
        List<Term> terms = StandardTokenizer.segment(promptContentStr);
        LogUtil.i("end " + System.currentTimeMillis());

        for (Term term : terms) {
            String word = term.word;
            String pos = term.nature.toString();
            String posInfo = getPosInfo(pos); // 判断词性属性
            LogUtil.i("Word: " + word + ", POS: " + pos + ", Attribute: " + posInfo);
        }
    }

    /**
     * 判断词性属性
     *
     * @param pos
     * @return 属性
     */
    static String getPosInfo(String pos) {
        // 这里你可以根据需要添加更多的判断逻辑来确定词性属性
        if (pos.equals("n")) {
            return "名词";
        } else if (pos.equals("v")) {
            return "动词";
        } else if (pos.equals("ns")) {
            return "地名";
        } else if (pos.equals("t")) {
            return "时间";
        } else {
            return "其他";
        }
    }

    private AiwaysGestureManager.AiwaysGestureListener listener = new AiwaysGestureManager.AiwaysGestureListener() {
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

    public static int calculateTotalPages(int totalRows, int rowsPerPage, int overlapRows) {
        if (totalRows <= 0 || rowsPerPage <= 0 || overlapRows < 0 || overlapRows >= rowsPerPage) {
            throw new IllegalArgumentException("参数无效: totalRows > 0, rowsPerPage > 0, 0 <= overlapRows < rowsPerPage");
        }
        // 每次取 rowsPerPage 行，重叠 overlapRows 行，所以每次新增的行数为 (rowsPerPage - overlapRows)
        // 总页数 = ceil((totalRows - overlapRows) / (rowsPerPage - overlapRows))
        // 使用 Math.max(1, ...) 确保至少返回 1 页
        return Math.max(1, (int) Math.ceil((double) (totalRows - overlapRows) / (rowsPerPage - overlapRows)));
    }

    public static int[] getPageRange(int totalRows, int rowsPerPage, int overlapRows, int pageIdx) {
        if (pageIdx < 0 || pageIdx > calculateTotalPages(totalRows, rowsPerPage, overlapRows) - 1) {
            throw new IllegalArgumentException("页码无效");
        }
        // 开始行 = pageIdx * (rowsPerPage - overlapRows)
        int start = pageIdx * (rowsPerPage - overlapRows);
        // 结束行 = Math.min(start + (rowsPerPage - 1), totalRows)
        int end = Math.min(start + (rowsPerPage - 1), totalRows - 1);
        return new int[]{start, end};
    }
}