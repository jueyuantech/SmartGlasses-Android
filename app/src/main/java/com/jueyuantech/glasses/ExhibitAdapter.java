package com.jueyuantech.glasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

public class ExhibitAdapter extends RecyclerView.Adapter<ExhibitViewHolder> {

    public Context mContext;
    public ArrayList<File> mExhibitList;

    public ExhibitAdapter(Context context, ArrayList<File> exhibitList) {
        mContext = context;
        mExhibitList = exhibitList;
    }

    @Override
    public ExhibitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exhibit, parent, false);
        return new ExhibitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExhibitViewHolder holder, int position) {
        //holder.textView.setText(mExhibitList.get(position).getTitle());
        Glide.with(mContext)
                .load(mExhibitList.get(position))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onItemClick(mExhibitList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mExhibitList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(File imageInfo);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}