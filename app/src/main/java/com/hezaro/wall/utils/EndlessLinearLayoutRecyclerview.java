package com.hezaro.wall.utils;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EndlessLinearLayoutRecyclerview extends RecyclerView {

    public abstract class AdvancedEndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

        private int lastVisibleItem, totalItemCount;

        private RecyclerView.LayoutManager linearLayoutManager;

        private boolean loading = true;

        private int visibleThreshold = 3;

        public AdvancedEndlessRecyclerOnScrollListener(RecyclerView.LayoutManager linearLayoutManager) {
            this.linearLayoutManager = linearLayoutManager;
        }

        public abstract void onLoadMore();

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = ((LinearLayoutManager) linearLayoutManager).findLastVisibleItemPosition();
            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                onLoadMore();
                loading = true;
            }
        }

        public void setLoading(boolean enable) {
            this.loading = enable;
        }

    }

    public interface onLoadMoreListener {

        void onLoadMore();
    }

    AdvancedEndlessRecyclerOnScrollListener aeros;

    private onLoadMoreListener onLoadMoreListener;

    public EndlessLinearLayoutRecyclerview(Context context) {
        super(context);
    }

    public EndlessLinearLayoutRecyclerview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EndlessLinearLayoutRecyclerview(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {

        if (!(layout instanceof LinearLayoutManager)) {
            throw new RuntimeException();
        }

        aeros = new AdvancedEndlessRecyclerOnScrollListener(layout) {
            @Override
            public void onLoadMore() {
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore();
                }
            }
        };

        addOnScrollListener(aeros);
        super.setLayoutManager(layout);
    }

    public void setLoading(boolean enable) {
        this.aeros.setLoading(enable);
    }

    public void setOnLoadMoreListener(EndlessLinearLayoutRecyclerview.onLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

}
