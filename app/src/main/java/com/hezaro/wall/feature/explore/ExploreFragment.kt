package com.hezaro.wall.feature.explore

import android.os.Bundle
import android.util.Log
import android.view.View
import com.hezaro.wall.R
import com.hezaro.wall.data.model.DExplore
import com.hezaro.wall.feature.adapter.ExploreAdapter
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.utils.EXPLORE
import kotlinx.android.synthetic.main.fragment_explore.exploreRecyclerView
import org.koin.android.ext.android.inject

class ExploreFragment : BaseFragment() {

    override fun layoutId() = R.layout.fragment_explore

    override fun tag(): String = this::class.java.simpleName

    override fun id() = EXPLORE

    private val vm: ExploreViewModel by inject()

    companion object {
        fun getInstance() = ExploreFragment()
    }

    private fun onSuccess(it: DExplore) {
        Log.i("tag", "")
        exploreRecyclerView.adapter = ExploreAdapter(it)
    }

    private fun onFailure(it: Failure) {
        Log.i("tag", "")
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