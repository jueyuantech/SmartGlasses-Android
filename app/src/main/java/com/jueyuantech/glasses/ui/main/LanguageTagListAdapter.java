package com.jueyuantech.glasses.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.bean.LanguageTag;

import java.util.List;

public class LanguageTagListAdapter extends BaseAdapter {

    private Context context;
    private List<LanguageTag> items;
    private int selectedPosition = -1;
    private String selectedValue = "";

    public LanguageTagListAdapter(Context context, List<LanguageTag> items) {
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
        LanguageTag item = items.get(position);
        holder.titleTv.setText(item.getTitle());
        holder.stateIv.setVisibility(View.VISIBLE);

        if (selectedValue.equals(item.getTag())) {
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

    private static class ViewHolder {
        TextView titleTv;
        ImageView stateIv;
    }
}