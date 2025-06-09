package com.jueyuantech.glasses.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.bean.ChatMessageBean;

import java.util.List;

public class ChatAdapter extends RvMultiAdapter<ChatMessageBean> {
    public ChatAdapter(Context context, List data) {
        super(context, data);

        init();
    }

    private void init() {
        addItemType(ChatMessageBean.TYPE_RECEIVED, R.layout.item_chat);
        addItemType(ChatMessageBean.TYPE_SEND, R.layout.item_chat_send);
        addItemType(ChatMessageBean.TYPE_SYSTEM, R.layout.item_chat_system);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void convert(RvViewHolder holder, ChatMessageBean bean, int position) {
        switch (getItemViewType(position)) {
            case ChatMessageBean.TYPE_SEND:
                TextView tv_send_content = holder.getView(R.id.tv_send_content);
                ImageView iv_send_picture = holder.getView(R.id.iv_send_picture);
                tv_send_content.setText(bean.content);
                /*
                Glide.with(context) //使得glide更容易使用，因为能接收context，activity，fragment对象
                        .load(bean.pic_url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter() //缩放图像，整个显示在控件，尽可能的填满
                        .into(iv_send_picture);
                */
                break;
            case ChatMessageBean.TYPE_RECEIVED:
                TextView tv_receive_content = holder.getView(R.id.tv_receive_content);
                ImageView iv_receive_picture = holder.getView(R.id.iv_receive_picture);
                TextView tv_receive_nick = holder.getView(R.id.tv_receive_nick);
                tv_receive_content.setText(bean.content);
                tv_receive_nick.setText(bean.nick);
                /*
                Glide.with(context) //使得glide更容易使用，因为能接收context，activity，fragment对象
                    .load(bean.pic_url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter() //缩放图像，整个显示在控件，尽可能的填满
                    .into(iv_receive_picture);
                */
                break;
            case ChatMessageBean.TYPE_SYSTEM:
                TextView tv_system_content = holder.getView(R.id.tv_system_content);
                tv_system_content.setText(bean.content);
                break;
        }
    }
}
