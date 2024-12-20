package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.MMKV_TRANS_SHOW_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.TRANS_SHOW_MODE_DEFAULT;

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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jueyuantech.glasses.util.MmkvUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransShowModeConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransShowModeConfigFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ImageView mCloseBtn;
    private ListView mConfigLv;
    private TransShowModeConfigListAdapter configListAdapter;

    private int[] CONFIG_KEY = new int[]{};
    private String[] CONFIG_TITLE = new String[]{};

    private int mDefaultSelectKey = 0;

    public TransShowModeConfigFragment() {
        // Required empty public constructor
        mDefaultSelectKey = (int) MmkvUtil.decode(MMKV_TRANS_SHOW_MODE_KEY, TRANS_SHOW_MODE_DEFAULT);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransShowModeConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransShowModeConfigFragment newInstance(String param1, String param2) {
        TransShowModeConfigFragment fragment = new TransShowModeConfigFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_trans_show_mode_config, container, false);
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
        List<TransShowModeConfigItem> itemList = new ArrayList<>();

        CONFIG_TITLE = getResources().getStringArray(R.array.trans_show_mode_config_title);
        CONFIG_KEY = getResources().getIntArray(R.array.trans_show_mode_config_key);

        for (int i = 0; i < CONFIG_TITLE.length; i++) {
            itemList.add(new TransShowModeConfigItem(CONFIG_TITLE[i], CONFIG_KEY[i]));
        }

        configListAdapter = new TransShowModeConfigListAdapter(getContext(), itemList);
        mConfigLv.setAdapter(configListAdapter);

        configListAdapter.setSelectedKey(mDefaultSelectKey);

        mConfigLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TransShowModeConfigItem selectedItem = itemList.get(position);
                int selectedValue = selectedItem.getValue();

                configListAdapter.setSelectedKey(selectedValue);
                MmkvUtil.encode(MMKV_TRANS_SHOW_MODE_KEY, selectedValue);
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

    class TransShowModeConfigListAdapter extends BaseAdapter {

        private Context context;
        private List<TransShowModeConfigItem> items;
        private int selectedPosition = -1;
        private int selectedKey = 0;

        public TransShowModeConfigListAdapter(Context context, List<TransShowModeConfigItem> items) {
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
            TransShowModeConfigItem item = items.get(position);
            holder.titleTv.setText(item.getText());
            holder.stateIv.setVisibility(View.VISIBLE);

            if (selectedKey == item.getValue()) {
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

        public void setSelectedKey(int key) {
            selectedKey = key;
            notifyDataSetChanged();
        }
    }

    class ViewHolder {
        TextView titleTv;
        ImageView stateIv;
    }
}