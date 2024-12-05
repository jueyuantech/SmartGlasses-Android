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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jueyuantech.glasses.stt.AsrConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AsrEngineConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AsrEngineConfigFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ImageView mCloseBtn;
    private ListView mConfigLv;
    private EngineConfigListAdapter configListAdapter;

    private String[] STT_CONFIG_KEY = new String[]{};
    private String[] STT_CONFIG_TITLE = new String[]{};

    public AsrEngineConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AsrEngineConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AsrEngineConfigFragment newInstance(String param1, String param2) {
        AsrEngineConfigFragment fragment = new AsrEngineConfigFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_asr_engine_config, container, false);
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
        List<AsrEngineConfigItem> itemList = new ArrayList<>();

        STT_CONFIG_TITLE = getResources().getStringArray(R.array.asr_engine_config_title);
        STT_CONFIG_KEY = getResources().getStringArray(R.array.asr_engine_config_key);

        for (int i = 0; i < STT_CONFIG_TITLE.length; i++) {
            itemList.add(new AsrEngineConfigItem(STT_CONFIG_TITLE[i], STT_CONFIG_KEY[i]));
        }

        configListAdapter = new EngineConfigListAdapter(getContext(), itemList);
        mConfigLv.setAdapter(configListAdapter);

        String curAsrEngine = AsrConfigManager.getInstance().getEngine();
        configListAdapter.setSelectedValue(curAsrEngine);

        mConfigLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AsrEngineConfigItem selectedItem = itemList.get(position);
                String selectedValue = selectedItem.getValue();

                AsrConfigManager.getInstance().setEngine(selectedValue);
                configListAdapter.setSelectedValue(selectedValue);
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

    class EngineConfigListAdapter extends BaseAdapter {

        private Context context;
        private List<AsrEngineConfigItem> items;
        private int selectedPosition = -1;
        private String selectedValue = "";

        public EngineConfigListAdapter(Context context, List<AsrEngineConfigItem> items) {
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
            AsrEngineConfigItem item = items.get(position);
            holder.titleTv.setText(item.getText());
            holder.stateIv.setVisibility(View.VISIBLE);

            if (selectedValue.equals(item.getValue())) {
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

        public void setSelectedValue(String value) {
            selectedValue = value;
            notifyDataSetChanged();
        }
    }

    class ViewHolder {
        TextView titleTv;
        ImageView stateIv;
    }
}