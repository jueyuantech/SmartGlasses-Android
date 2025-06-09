package com.jueyuantech.glasses;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.venussdk.bean.VNHomeMenu;

import java.util.Collections;
import java.util.List;

public class HomeMenuItemTouchHelper extends ItemTouchHelper.Callback {
    private List<VNHomeMenu> homeMenus;
    private HomeMenuAdapter homeMenuAdapter;

    public HomeMenuItemTouchHelper(List<VNHomeMenu> homeMenus, HomeMenuAdapter homeMenuAdapter) {
        LogUtil.mark();
        this.homeMenus = homeMenus;
        this.homeMenuAdapter = homeMenuAdapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        final int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
        //得到当拖拽的viewHolder的Position
        int fromPosition = viewHolder.getAdapterPosition();
        //拿到当前拖拽到的item的viewHolder
        int toPosition = target.getAdapterPosition();
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(homeMenus, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(homeMenus, i, i - 1);
            }
        }
        homeMenuAdapter.notifyItemMoved(fromPosition, toPosition);
        LogUtil.d("fromPosition = " + fromPosition + " toPosition" + toPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    /**
     * 长按选中Item时修改颜色
     *
     * @param viewHolder
     * @param actionState
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        //if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
        //viewHolder.itemView.setBackground(getDrawable(R.drawable.card_drag_selected));
        //}
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * 手指松开的时候还原颜色
     *
     * @param recyclerView
     * @param viewHolder
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        //viewHolder.itemView.setBackground(getDrawable(R.drawable.card));
    }

    /**
     * 重写拖拽不可用
     *
     * @return
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
}