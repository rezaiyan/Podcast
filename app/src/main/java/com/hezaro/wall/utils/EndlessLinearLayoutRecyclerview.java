package com.hezaro.wall.utils;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EndlessLinearLayoutRecyclerview extends RecyclerView {

    public abstract class AdvancedEndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

        private int lastVisibleItem, totalItemCount;

        private RecyclerView.LayoutManager linearLayoutManager;

        public MutableLiveData<Boolean> loading = new MutableLiveData();

        private int visibleThreshold = 3;

        public AdvancedEndlessRecyclerOnScrollListener(RecyclerView.LayoutManager linearLayoutManager) {
            this.linearLayoutManager = linearLayoutManager;
            loading.setValue(false);
        }

        public abstract void onLoadMore();

        public MutableLiveData<Boolean> getLoadingStatus() {
            return this.loading;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = ((LinearLayoutManager) linearLayoutManager).findLastVisibleItemPosition();
            if (!loading.getValue() && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                onLoadMore();
                page++;
                loading.postValue(true);
            }
        }

        public void setLoading(boolean enable) {
            this.loading.setValue(enable);
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

    public int page = 1;

    public void setLoading(boolean enable) {
        this.aeros.setLoading(enable);
    }

    public MutableLiveData<Boolean> getLoadingStatus() {
        return this.aeros.getLoadingStatus();
    }

    public void setOnLoadMoreListener(EndlessLinearLayoutRecyclerview.onLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

}
