package com.jueyuantech.glasses;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ExhibitViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    public TextView textView;

    public ExhibitViewHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.iv_image);
        textView = (TextView) itemView.findViewById(R.id.tv_title);
    }
}