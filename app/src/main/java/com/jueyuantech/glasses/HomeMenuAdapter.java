package com.jueyuantech.glasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jueyuantech.venussdk.bean.VNHomeMenu;

import java.util.List;

public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.HomeMenuViewHolder> {
    public static final int MENU_TYPE_SHOW = 1;
    public static final int MENU_TYPE_HIDE = 2;

    private Context mContext;
    private int mMenuType;
    private List<VNHomeMenu> mHomeMenus;

    public HomeMenuAdapter(Context context, List<VNHomeMenu> homeMenus, int menuType) {
        mContext = context;
        mHomeMenus = homeMenus;
        mMenuType = menuType;
    }

    @Override
    public HomeMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_home_menu, parent, false);
        return new HomeMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeMenuViewHolder holder, int position) {
        VNHomeMenu homeMenu = mHomeMenus.get(position);
        holder.name.setText(homeMenu.getPage());
        holder.icon.setImageResource(getMenuImage(homeMenu.getPage(), homeMenu.getDefaultFlag()));
        if (mMenuType == MENU_TYPE_SHOW) {
            if (1 == homeMenu.getDefaultFlag()) {
                holder.name.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                holder.name.setTextColor(mContext.getResources().getColor(android.R.color.black));
            }
            holder.remove.setVisibility(View.GONE);
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHomeMenus.remove(homeMenu);
                    if (null != onRemoveListener) {
                        onRemoveListener.onRemoved(homeMenu);
                    }
                    notifyDataSetChanged();
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDefault(homeMenu);
                }
            });
        } else if (mMenuType == MENU_TYPE_HIDE) {
            holder.remove.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHomeMenus.remove(homeMenu);
                    if (null != onRemoveListener) {
                        onRemoveListener.onRemoved(homeMenu);
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mHomeMenus.size();
    }

    private void setDefault(VNHomeMenu homeMenu) {
        for (VNHomeMenu menu : mHomeMenus) {
            if (menu.getPage().equals(homeMenu.getPage())) {
                menu.setDefaultFlag(1);
            } else {
                menu.setDefaultFlag(0);
            }
        }
        notifyDataSetChanged();
    }

    private int getMenuImage(String name, int defaultFlag) {
        int iconResourceId = R.mipmap.ic_venus_app_settings_normal;
        if ("Music".equals(name)) {
            if (1 == defaultFlag) {
                iconResourceId = R.mipmap.ic_venus_app_music_selected;
            } else {
                iconResourceId = R.mipmap.ic_venus_app_music_normal;
            }
        } else if ("Transcribe".equals(name)) {
            if (1 == defaultFlag) {
                iconResourceId = R.mipmap.ic_venus_app_transcribe_selected;
            } else {
                iconResourceId = R.mipmap.ic_venus_app_transcribe_normal;
            }
        } else if ("Translate".equals(name)) {
            if (1 == defaultFlag) {
                iconResourceId = R.mipmap.ic_venus_app_translate_selected;
            } else {
                iconResourceId = R.mipmap.ic_venus_app_translate_normal;
            }
        } else if ("Map".equals(name)) {
            if (1 == defaultFlag) {
                iconResourceId = R.mipmap.ic_venus_app_map_selected;
            } else {
                iconResourceId = R.mipmap.ic_venus_app_map_normal;
            }
        } else if ("Gallery".equals(name)) {
            if (1 == defaultFlag) {
                iconResourceId = R.mipmap.ic_venus_app_prompter_selected;
            } else {
                iconResourceId = R.mipmap.ic_venus_app_prompter_normal;
            }
        } else if ("Book".equals(name)) {
            if (1 == defaultFlag) {
                iconResourceId = R.mipmap.ic_venus_app_book_selected;
            } else {
                iconResourceId = R.mipmap.ic_venus_app_book_normal;
            }
        } else if ("Settings".equals(name)) {
            if (1 == defaultFlag) {
                iconResourceId = R.mipmap.ic_venus_app_settings_selected;
            } else {
                iconResourceId = R.mipmap.ic_venus_app_settings_normal;
            }
        }
        return iconResourceId;
    }

    private OnRemovedListener onRemoveListener;

    public void setOnRemoveListener(OnRemovedListener listener) {
        onRemoveListener = listener;
    }

    interface OnRemovedListener {
        void onRemoved(VNHomeMenu homeMenu);
    }

    class HomeMenuViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView name;
        public ImageView remove;

        public HomeMenuViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            remove = (ImageView) itemView.findViewById(R.id.iv_remove);
        }
    }
}