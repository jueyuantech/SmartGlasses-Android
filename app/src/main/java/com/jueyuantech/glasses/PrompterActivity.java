package com.jueyuantech.glasses;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VenusConstant;
import com.jueyuantech.venussdk.VenusSDK;
import com.jueyuantech.venussdk.bean.PrompterInfo;
import com.jueyuantech.venussdk.bean.SystemConfig;
import com.jueyuantech.venussdk.cb.SystemConfigCallBack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PrompterActivity extends VenusAppBaseActivity implements View.OnClickListener {

    private SpannableString spannableStr;
    private int totalPages = 0;
    private int curPage = 0;
    private int linesPerPage = 3;
    private int totalLines = 0;
    private List<String> linesList = new ArrayList<>();
    private int[] PROMPTER_FONT_SIZE_CONFIG_KEY;

    private ImageView mBackIv;
    private TextView mContentTv;
    private PrompterTextView mContentPtv;
    private ViewTreeObserver mObserver;
    private Layout mContentTvLayout;
    private ImageView mNextBtn, mPreBtn;
    private ImageView mFileChooserBtn;

    private AiwaysGestureManager aiwaysGestureManager;
    private View mControlV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        PROMPTER_FONT_SIZE_CONFIG_KEY = getResources().getIntArray(R.array.prompter_font_size_config_key);

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

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
        mContentTv.setTextSize(PROMPTER_FONT_SIZE_CONFIG_KEY[1]);

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
    protected void onResume() {
        super.onResume();
        getVenusFontSize();
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
            default:
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                StringBuilder result = new StringBuilder();
                Uri uri = data.getData();
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                    result.append(System.getProperty("line.separator")); // TXT添加换行符
                }
                promptContentStr = result.toString();
                updatePromptContent();
                initPtvContent();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void notifyVenusEnter() {
        VenusSDK.setView(VenusConstant.View.PROMPTER, null);
    }

    @Override
    protected void notifyVenusExit() {
        VenusSDK.setView(VenusConstant.View.HOME, null);
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

    private void updatePromptContent() {
        // 去掉空白行
        //promptContentStr = promptContentStr.replaceAll("(?m)^[ \t]*\r?\n", "");

        spannableStr = new SpannableString(promptContentStr);
        mContentTv.setText(spannableStr);

        totalLines = mContentTvLayout.getLineCount();
        // 向上取整计算总页数
        totalPages = (totalLines + linesPerPage - 1) / linesPerPage;
        curPage = 0;

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
            linesList.add(promptContentStr.substring(start, end));
        }
        mContentPtv.setData(linesList);

        updateLines();
    }

    private void updatePtvHighlight(int start, int end, int pre, int next) {
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
        int startLine = curPage * linesPerPage;
        int endLine = startLine + linesPerPage - 1;
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

        updateHighlight(startLine, endLine, preLine, nextLine);
        updatePtvHighlight(startLine, endLine, preLine, nextLine);
        updateVenusPrompter(totalPages, curPage, startLine, endLine, preLine, nextLine);
    }

    private void updateHighlight(int startLine, int endLine, int preLine, int nextLine) {
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

    private void updateVenusPrompter(int total, int current, int startLine, int endLine, int preLine, int nextLine) {
        String curTextStr = "";
        for (int line = startLine; line < endLine + 1; line++) {
            String txt = getLineText(line);
            curTextStr += txt;
            if (line != endLine) {
                if (!txt.endsWith("\n")) {
                    curTextStr += "\n";
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

        PrompterInfo prompterInfo = new PrompterInfo();
        prompterInfo.setTotal(total);
        prompterInfo.setCurrent(current);
        prompterInfo.setPreText(preTextStr);
        prompterInfo.setCurText(curTextStr);
        prompterInfo.setNextText(nextTextStr);
        VenusSDK.updatePrompter(prompterInfo, null);

        LogUtil.i("preTextStr: " + preTextStr);
        LogUtil.i("curTextStr: " + curTextStr);
        LogUtil.i("nextTextStr: " + nextTextStr);
    }

    public String getLineText(int line) {
        int startOffset = mContentTvLayout.getLineStart(line);
        int endOffset = mContentTvLayout.getLineEnd(line);
        return mContentTv.getText().subSequence(startOffset, endOffset).toString();
    }

    private void setTextSize(float textSize) {
        LogUtil.i(textSize);
        mContentTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
    }

    private void reSizeByVenusFontSize(int venusFontSize) {
        int targetSize;
        switch (venusFontSize) {
            case VenusConstant.FontSize.SMALL:
                targetSize = PROMPTER_FONT_SIZE_CONFIG_KEY[0];
                break;
            case VenusConstant.FontSize.MEDIUM:
                targetSize = PROMPTER_FONT_SIZE_CONFIG_KEY[1];
                break;
            case VenusConstant.FontSize.LARGE:
                targetSize = PROMPTER_FONT_SIZE_CONFIG_KEY[2];
                break;
            default:
                targetSize = PROMPTER_FONT_SIZE_CONFIG_KEY[0];
        }
        float curSize = px2dp(mContentTv.getTextSize());
        if (curSize != targetSize) {
            setTextSize(targetSize);
        }
    }

    private float px2dp(float px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.densityDpi / 160f));
    }

    private void getVenusFontSize() {
        VenusSDK.getFontSize(new SystemConfigCallBack() {
            @Override
            public void onSuccess(SystemConfig systemConfig) {
                if (null != systemConfig.getFontSize()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reSizeByVenusFontSize(systemConfig.getFontSize());
                        }
                    });
                }
            }

            @Override
            public void onTimeOut() {

            }

            @Override
            public void onFailed() {

            }
        });
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

    private String promptContentStr = "提词器是一款实用的辅助工具，在您演讲、录制视频或直播过程中，为用户提供实时滚动显示的文稿内容，帮助用户更好地记忆和表达。\n" +
            "通过导入手机中存储的TXT格式文档，提词器能够展示预先准备好的题词内容，用户只需跟随屏幕上的文字进行朗读，即可避免忘词或重复，确保演出的流畅性和准确性，轻松实现高效演讲。";

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
}