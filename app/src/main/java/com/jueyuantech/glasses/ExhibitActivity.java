package com.jueyuantech.glasses;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNImageInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ExhibitActivity extends VenusAppBaseActivity implements View.OnClickListener {

    private RecyclerView mExhibitRcv;
    private ArrayList<File> mExhibitList;
    private ExhibitAdapter mExhibitAdapter;

    private ImageView mCurrentIv;
    private TextView mCurrentTv;
    private ImageView mBackIv;

    public static final String FOLDER_NAME = "VenusImage";
    private AssetManager assetManager;
    private File venusImageFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibit);

        assetManager = getAssets();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mCurrentIv = findViewById(R.id.iv_current);
        mCurrentTv = findViewById(R.id.tv_current);

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mExhibitRcv = (RecyclerView) findViewById(R.id.rcv_exhibit);
        mExhibitRcv.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mExhibitRcv.setLayoutManager(layoutManager);

        venusImageFolder = getExternalFilesDir(FOLDER_NAME);
        if (0 == venusImageFolder.list().length) {
            loadFromAsset();
        }

        mExhibitList = new ArrayList<>();
        for (File imgFile : venusImageFolder.listFiles()) {
            mExhibitList.add(imgFile);
        }
        mExhibitAdapter = new ExhibitAdapter(this, mExhibitList);
        mExhibitRcv.setAdapter(mExhibitAdapter);

        mExhibitAdapter.setOnItemClickListener(new ExhibitAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(File imageFile) {
                showImage(imageFile);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != mExhibitList && mExhibitList.size() > 0) {
            showImage(mExhibitList.get(0));
        }
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.GALLERY, null);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    private void showImage(File imageFile) {
        Glide.with(ExhibitActivity.this)
                .load(imageFile)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mCurrentIv);

        VNImageInfo imageInfo = new VNImageInfo();
        imageInfo.setTitle("");
        imageInfo.setBrief("");
        imageInfo.setFileName(imageFile.getName());
        VNCommon.showImage(imageInfo, null);
    }

    private void loadFromAsset() {
        try {
            String[] imagesName = assetManager.list(FOLDER_NAME);
            for (String imageName: imagesName) {
                File imageFile = new File(venusImageFolder, imageName);
                imageFile.createNewFile();

                InputStream inputStream = assetManager.open(FOLDER_NAME + File.separator + imageName);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}