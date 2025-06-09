package com.jueyuantech.glasses.ui.main;

import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.stt.SttConfigManager;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FuncConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FuncConfigFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SttConfigManager mSttConfigManager;
    private String mEngine;
    private String mFunc;

    private ImageView mCloseBtn;
    private TextView mFuncSttBtn;
    private TextView mFuncTransBtn;

    private ListView mSourceConfigLv;
    private LanguageTagListAdapter mSourceLanTagAdapter;
    private ListView mTargetConfigLv;
    private LanguageTagListAdapter mTargetLanTagAdapter;

    public FuncConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FuncConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FuncConfigFragment newInstance(String param1, String param2) {
        FuncConfigFragment fragment = new FuncConfigFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setStyle(STYLE_NORMAL, R.style.FuncConfigDialog);

        mSttConfigManager = SttConfigManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_func_config, container, false);
        mCloseBtn = rootView.findViewById(R.id.iv_close);
        mCloseBtn.setOnClickListener(this);
        mFuncSttBtn = rootView.findViewById(R.id.tv_func_stt);
        mFuncSttBtn.setOnClickListener(this);
        mFuncTransBtn = rootView.findViewById(R.id.tv_func_trans);
        mFuncTransBtn.setOnClickListener(this);

        mSourceConfigLv = rootView.findViewById(R.id.lv_source_config);
        mTargetConfigLv = rootView.findViewById(R.id.lv_target_config);

        updateView();
        updateConfigList();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 当 BottomSheetDialogFragment 关闭时，调用回调方法
        if (mListener != null) {
            mListener.onDestroyView();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.tv_func_stt:
                setFunc(STT_FUNC_TRANSCRIBE);
                break;
            case R.id.tv_func_trans:
                setFunc(STT_FUNC_TRANSLATE);
                break;
            default:
        }
    }

    private void updateView() {
        mFunc = mSttConfigManager.getFunc();
        if (STT_FUNC_TRANSCRIBE.equals(mFunc)) {
            mFuncSttBtn.setBackgroundResource(R.drawable.bg_func_selected);
            mFuncTransBtn.setBackground(null);

            mSourceConfigLv.setVisibility(View.VISIBLE);
            mTargetConfigLv.setVisibility(View.GONE);
        } else if (STT_FUNC_TRANSLATE.equals(mFunc)) {
            mFuncTransBtn.setBackgroundResource(R.drawable.bg_func_selected);
            mFuncSttBtn.setBackground(null);

            mSourceConfigLv.setVisibility(View.VISIBLE);
            mTargetConfigLv.setVisibility(View.VISIBLE);
        }
    }

    private void updateConfigList() {
        mEngine = mSttConfigManager.getEngine();
        mFunc = mSttConfigManager.getFunc();

        if (STT_FUNC_TRANSCRIBE.equals(mFunc)) {
            List<LanguageTag> sourceTagList = mSttConfigManager.getSourceLanTags(mEngine, mFunc);
            LanguageTag sourceLanTag = mSttConfigManager.getSourceLanTag(mEngine, mFunc);

            mSourceLanTagAdapter = new LanguageTagListAdapter(getContext(), sourceTagList);
            mSourceLanTagAdapter.setSelectedValue(null == sourceLanTag ? "" : sourceLanTag.getTag());
            mSourceConfigLv.setAdapter(mSourceLanTagAdapter);

            mSourceConfigLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LanguageTag selectedItem = sourceTagList.get(position);
                    String selectedValue = selectedItem.getTag();
                    mSourceLanTagAdapter.setSelectedValue(selectedValue);

                    mSttConfigManager.setSourceLanTag(mEngine, mFunc, selectedValue);
                }
            });
        } else if (STT_FUNC_TRANSLATE.equals(mFunc)) {
            List<LanguageTag> sourceTagList = mSttConfigManager.getSourceLanTags(mEngine, mFunc);
            LanguageTag sourceLanTag = mSttConfigManager.getSourceLanTag(mEngine, mFunc);
            String sourceLanTagKey = "";
            if (null != sourceLanTag) {
                sourceLanTagKey = sourceLanTag.getTag();
            }

            mSourceLanTagAdapter = new LanguageTagListAdapter(getContext(), sourceTagList);
            mSourceLanTagAdapter.setSelectedValue(sourceLanTagKey);
            mSourceConfigLv.setAdapter(mSourceLanTagAdapter);

            mSourceConfigLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LanguageTag selectedItem = sourceTagList.get(position);
                    String selectedValue = selectedItem.getTag();
                    mSourceLanTagAdapter.setSelectedValue(selectedValue);

                    mSttConfigManager.setSourceLanTag(mEngine, mFunc, selectedValue);
                    updateConfigList();
                }
            });

            List<LanguageTag> targetTagList = mSttConfigManager.getTargetLanTags(mEngine, mFunc, sourceLanTagKey);
            LanguageTag targetLanTag = mSttConfigManager.getTargetLanTag(mEngine, mFunc, sourceLanTagKey);

            mTargetLanTagAdapter = new LanguageTagListAdapter(getContext(), targetTagList);
            mTargetLanTagAdapter.setSelectedValue(null == targetLanTag ? "" : targetLanTag.getTag());
            mTargetConfigLv.setAdapter(mTargetLanTagAdapter);

            String finalSourceLanTagKey = sourceLanTagKey;
            mTargetConfigLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LanguageTag selectedItem = targetTagList.get(position);
                    String selectedValue = selectedItem.getTag();
                    mTargetLanTagAdapter.setSelectedValue(selectedValue);

                    mSttConfigManager.setTargetLanTag(mEngine, mFunc, finalSourceLanTagKey, selectedValue);
                }
            });
        }
    }

    private void setFunc(String funcName) {
        mFunc = mSttConfigManager.getFunc();
        if (mFunc.equals(funcName)) {
            return;
        }

        if (STT_FUNC_TRANSCRIBE.equals(mFunc)) {
            mSttConfigManager.setFunc(STT_FUNC_TRANSLATE);
        } else if (STT_FUNC_TRANSLATE.equals(mFunc)) {
            mSttConfigManager.setFunc(STT_FUNC_TRANSCRIBE);
        }

        updateView();
        updateConfigList();
    }

    public interface Listener {
        void onDestroyView();
    }

    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }
}