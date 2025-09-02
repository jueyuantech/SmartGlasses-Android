package com.jueyuantech.glasses;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jueyuantech.venussdk.VNCommon;

import android.app.Dialog;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LanguageConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LanguageConfigFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ImageView mCloseBtn;
    private ListView mConfigLv;
    private LanguageConfigListAdapter configListAdapter;

    private String[] STT_CONFIG_KEY = new String[]{};
    private String[] STT_CONFIG_TITLE = new String[]{};

    private String mDefaultSelectKey = "";

    public LanguageConfigFragment(String selectKey) {
        // Required empty public constructor
        mDefaultSelectKey = selectKey;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LanguageConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LanguageConfigFragment newInstance(String param1, String param2) {
        LanguageConfigFragment fragment = new LanguageConfigFragment(param1);
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
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                // 设置为展开状态
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                // 禁用拖拽关闭
                behavior.setDraggable(false);
                // 设置跳过折叠状态
                behavior.setSkipCollapsed(true);
            }
        });
        
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_language_config, container, false);
        mCloseBtn = rootView.findViewById(R.id.iv_close);
        mCloseBtn.setOnClickListener(this);

        mConfigLv = rootView.findViewById(R.id.lv_config);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateView();
        updateConfigList();
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
            default:
        }
    }

    private void updateView() {

    }

    private void updateConfigList() {
        List<LanguageConfigItem> itemList = new ArrayList<>();

        STT_CONFIG_TITLE = getResources().getStringArray(R.array.language_config_title);
        STT_CONFIG_KEY = getResources().getStringArray(R.array.language_config_key);

        for (int i = 0; i < STT_CONFIG_TITLE.length; i++) {
            itemList.add(new LanguageConfigItem(STT_CONFIG_TITLE[i], STT_CONFIG_KEY[i]));
        }

        configListAdapter = new LanguageConfigListAdapter(getContext(), itemList);
        mConfigLv.setAdapter(configListAdapter);

        configListAdapter.setSelectedKey(mDefaultSelectKey);

        mConfigLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LanguageConfigItem selectedItem = itemList.get(position);
                String selectedValue = selectedItem.getValue();

                configListAdapter.setSelectedKey(selectedValue);
                VNCommon.setLanguage(selectedValue, null);
            }
        });
    }

    public interface Listener {
        void onDestroyView();
    }

    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    class LanguageConfigListAdapter extends BaseAdapter {

        private Context context;
        private List<LanguageConfigItem> items;
        private int selectedPosition = -1;
        private String selectedKey = "";

        public LanguageConfigListAdapter(Context context, List<LanguageConfigItem> items) {
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
            LanguageConfigItem item = items.get(position);
            holder.titleTv.setText(item.getText());
            holder.stateIv.setVisibility(View.VISIBLE);

            if (selectedKey.equals(item.getValue())) {
                holder.stateIv.setVisibility(View.VISIBLE);
            } else {
                holder.stateIv.setVisibility(View.GONE);
            }

            return convertView;
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
            notifyDataSetChanged();
        }

        public void setSelectedKey(String key) {
            selectedKey = key;
            notifyDataSetChanged();
        }
    }

    class ViewHolder {
        TextView titleTv;
        ImageView stateIv;
    }
}