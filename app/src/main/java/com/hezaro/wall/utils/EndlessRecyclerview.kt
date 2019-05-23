package com.hezaro.wall.utils

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import timber.log.Timber

class EndlessLinearLayoutRecyclerview : RecyclerView {

    private lateinit var aeros: AdvancedEndlessRecyclerOnScrollListener

    private var loadMoreListener: OnLoadMoreListener? = null

    var page = 1

    fun onError() {
        if (page > 1)
            page--
    }

    abstract inner class AdvancedEndlessRecyclerOnScrollListener(private val linearLayoutManager: RecyclerView.LayoutManager) :
        RecyclerView.OnScrollListener() {

        private var lastVisibleItem: Int = 0
        private var totalItemCount: Int = 0

        var loadingStatus: MutableLiveData<Boolean> = MutableLiveData()

        private val visibleThreshold = 3

        init {
            loadingStatus.value = false
        }

        abstract fun onLoadMore()

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            totalItemCount = recyclerView.adapter!!.itemCount
            lastVisibleItem = (linearLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            if (!(loadingStatus.value)!! && totalItemCount <= lastVisibleItem + visibleThreshold) {
                onLoadMore()
                page++
                loadingStatus.postValue(true)
            }
        }

        fun setLoading(enable: Boolean) {
            this.loadingStatus.value = enable
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun setLayoutManager(layout: RecyclerView.LayoutManager?) {

        if (layout !is LinearLayoutManager) {
            throw RuntimeException()
        }

        aeros = object : AdvancedEndlessRecyclerOnScrollListener(layout) {
            override fun onLoadMore() {
                if (loadMoreListener != null) {
                    loadMoreListener!!.onLoadMore()
                }
            }
        }

        addOnScrollListener(aeros)
        super.setLayoutManager(layout)
    }

    fun setLoading(enable: Boolean) {
        this.aeros.setLoading(enable)
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener) {
        this.loadMoreListener = onLoadMoreListener
    }

    fun removeOnLoadMoreListener() {
        this.loadMoreListener = null
    }
}

interface OnLoadMoreListener {

    fun onLoadMore()
}

class EndlessLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    override fun onLayoutChildren(recycler: Recycler?, state: State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Timber.e("meet a IOOBE in RecyclerView")
        }
    }

    override fun supportsPredictiveItemAnimations() = false
}