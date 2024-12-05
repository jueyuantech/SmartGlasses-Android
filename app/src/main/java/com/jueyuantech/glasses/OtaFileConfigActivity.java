package com.jueyuantech.glasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jueyuantech.glasses.util.LogUtil;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.ImageType;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OtaFileConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mFolderTipsTv;
    private List<String> mUserList = new ArrayList<>();
    private ListView mUserLv;
    private ListAdapter mUserAdapter;
    private List<String> mFileList = new ArrayList<>();
    private ListView mFileLv;
    private ListAdapter mFileAdapter;
    private Button mSureBtn;
    private ImageView mHelpIv;

    private LinearLayout mPdfContainerRl;
    private ImageView mCloseIv;
    //private ImageView mPdfIv;
    private File root;
    private AssetManager assetManager;
    private Bitmap pageImage;
    private WebView mPdfWv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_file_config);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mFolderTipsTv = findViewById(R.id.tv_folder_tips);
        mFolderTipsTv.setText(getString(R.string.ota_config_folder_tips, "/Android/data/com.jueyuantech.glasses/files/VenusOTA/"));

        mHelpIv = findViewById(R.id.iv_help);
        mHelpIv.setOnClickListener(this);

        mPdfContainerRl = findViewById(R.id.rl_container_pdf);
        mCloseIv = findViewById(R.id.iv_close);
        mCloseIv.setOnClickListener(this);
        //mPdfIv = findViewById(R.id.iv_pdf);
        mPdfWv = findViewById(R.id.wv_pdf);

        File otaFilesDir = getExternalFilesDir("VenusOTA");
        if (!otaFilesDir.exists()) {
            otaFilesDir.mkdir();
        }

        mUserList.clear();
        mUserList.add("firmware");
        mUserList.add("bth");
        mUserAdapter = new ListAdapter(this, mUserList);
        mUserLv = findViewById(R.id.lv_user);
        mUserLv.setAdapter(mUserAdapter);

        mUserLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = mUserList.get(position);
                mUserAdapter.setSelectedValue(selectedItem);
                setFile(selectedItem);
            }
        });

        mFileList.clear();
        mFileList.addAll(Arrays.asList(otaFilesDir.list()));
        mFileAdapter = new ListAdapter(this, mFileList);
        mFileLv = findViewById(R.id.lv_file);
        mFileLv.setAdapter(mFileAdapter);

        mSureBtn = findViewById(R.id.btn_sure);
        mSureBtn.setOnClickListener(this);

        PDFBoxResourceLoader.init(getApplicationContext());
        root = getApplicationContext().getCacheDir();
        assetManager = getAssets();
    }

    @Override
    protected void onPause() {
        hideHelpDialog();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sure:
                setResult();
                break;
            case R.id.iv_help:
                //showHelpDialog();
                break;
            case R.id.iv_close:
                hideHelpDialog();
                break;
        }
    }

    private void setFile(String user) {
        if ("firmware".equals(user)) {
            mFileAdapter.setSelectedValue("rtos_main.bin");
        } else if ("bth".equals(user)) {
            mFileAdapter.setSelectedValue("best1600_watch_bth.bin");
        }
    }

    private void setResult() {
        Intent resultIntent = new Intent();
        String otaUser = mUserAdapter.getSelectedValue();
        String fileName = mFileAdapter.getSelectedValue();
        String filePath = "";
        if (!TextUtils.isEmpty(fileName)) {
            File otaFile = new File(getExternalFilesDir("VenusOTA"), fileName);
            filePath = otaFile.getAbsolutePath();
        }
        resultIntent.putExtra("user", otaUser);
        resultIntent.putExtra("filePath", filePath);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void showHelpDialog() {
        if (mPdfContainerRl.getVisibility() != View.VISIBLE) {
            mPdfContainerRl.setVisibility(View.VISIBLE);
        }

        mPdfWv.getSettings().setJavaScriptEnabled(true); //支持javascript
        mPdfWv.getSettings().setSupportZoom(true); // 设置可以支持缩放
        //mPdfWv.getSettings().setBuiltInZoomControls(true); // 设置出现缩放工具
        mPdfWv.getSettings().setUseWideViewPort(true); //扩大比例的缩放
        mPdfWv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//自适应屏幕
        mPdfWv.getSettings().setLoadWithOverviewMode(true);

        mPdfWv.loadUrl("file:android_asset/local_ota_how_to.html");
        //renderFile(null);
    }

    private void hideHelpDialog() {
        if (mPdfContainerRl.getVisibility() != View.GONE) {
            mPdfContainerRl.setVisibility(View.GONE);
        }
    }

    public void renderFile(View view) {
        // Render the page and save it to an image file
        try {
            // Load in an already created PDF
            PDDocument document = PDDocument.load(assetManager.open("local_ota_how_to.pdf"));
            // Create a renderer for the document
            PDFRenderer renderer = new PDFRenderer(document);
            // Render the image to an RGB Bitmap
            pageImage = renderer.renderImage(0, 1, ImageType.RGB);

            // Save the render result to an image
            String path = root.getAbsolutePath() + "/render.jpg";
            File renderFile = new File(path);
            FileOutputStream fileOut = new FileOutputStream(renderFile);
            pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
            fileOut.close();
            LogUtil.i("Successfully rendered image to " + path);
            // Optional: display the render result on screen
            displayRenderedImage();
        } catch (IOException e) {
            LogUtil.e("PdfBox-Android-Sample, Exception thrown while rendering file");
            e.printStackTrace();
        }
    }

    /**
     * Helper method for drawing the result of renderFile() on screen
     */
    private void displayRenderedImage() {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mPdfIv.setImageBitmap(pageImage);
                    }
                });
            }
        }.start();
    }

    class ListAdapter extends BaseAdapter {
        private Context context;
        private List<String> items;
        private int selectedPosition = -1;
        private String selectedValue = "";

        public ListAdapter(Context context, List<String> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_func_config, parent, false);
                holder = new ViewHolder();
                holder.titleTv = convertView.findViewById(R.id.tv_title);
                holder.stateIv = convertView.findViewById(R.id.iv_state);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String item = items.get(position);
            holder.titleTv.setText(item);
            holder.stateIv.setVisibility(View.VISIBLE);

            if (selectedValue.equals(item)) {
                holder.stateIv.setVisibility(View.VISIBLE);
            } else {
                holder.stateIv.setVisibility(View.GONE);
            }

            return convertView;
        }

        public void setSelectedPosition(int position) {
            if (position < items.size()) {
                selectedValue = items.get(position);
                selectedPosition = position;
                notifyDataSetChanged();
            }
        }

        public void setSelectedValue(String value) {
            for (String item : items) {
                if (item.equals(value)) {
                    selectedValue = value;
                    notifyDataSetChanged();
                    break;
                }
            }
        }

        public String getSelectedValue() {
            return selectedValue;
        }
    }

    class ViewHolder {
        TextView titleTv;
        ImageView stateIv;
    }
}