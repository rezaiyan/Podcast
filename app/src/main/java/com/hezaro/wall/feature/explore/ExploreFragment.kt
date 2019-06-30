package com.hezaro.wall.feature.explore

import android.os.Bundle
import android.view.View
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Explore
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.utils.EXPLORE
import org.koin.android.ext.android.inject

class ExploreFragment : BaseFragment() {

    override fun layoutId() = R.layout.fragment_explore

    override fun tag(): String = this::class.java.simpleName

    override fun id() = EXPLORE

    private val vm: ExploreViewModel by inject()

    companion object {
        fun getInstance() = ExploreFragment()
    }

    private fun onSuccess(it: Explore) {
//        exploreRecyclerView.adapter = ExploreAdapter(it.best, it.category, it.last, it.recommended)
    }

    private fun onFailure(it: Failure) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(explore, ::onSuccess)
            failure(failure, ::onFailure)
            explore()
        }
    }
}