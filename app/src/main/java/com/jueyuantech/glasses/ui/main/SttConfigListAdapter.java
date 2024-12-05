package com.jueyuantech.glasses.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jueyuantech.glasses.R;

import java.util.List;

public class SttConfigListAdapter extends BaseAdapter {

    private Context context;
    private List<ConfigItem> items;
    private int selectedPosition = -1;
    private int selectedValue = -1;

    public SttConfigListAdapter(Context context, List<ConfigItem> items) {
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
        ConfigItem item = items.get(position);
        holder.titleTv.setText(item.getText());
        holder.stateIv.setVisibility(View.VISIBLE);

        if (item.getValue() == selectedValue) {
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

    public void setSelectedValue(int value) {
        selectedValue = value;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView titleTv;
        ImageView stateIv;
    }
}