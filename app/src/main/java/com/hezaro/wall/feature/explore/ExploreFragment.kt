package com.hezaro.wall.feature.explore

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hezaro.wall.R.layout
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import kotlinx.android.synthetic.main.frg_explore.exploreList
import org.koin.android.ext.android.inject

class ExploreFragment : BaseFragment() {

    private val vm: ExploreViewModel by inject()
    override fun layoutId() = layout.frg_explore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.episodes()
        showProgress()
        with(vm) {
            observe(episodes, ::onSuccess)
            failure(failure, ::onFailure)
        }
    }

    private fun onSuccess(episodes: MutableList<Episode>) {
        hideProgress()
        exploreList.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = ExploreAdapter(episodes.toMutableList()) {
            }
        }
    }

    private fun onFailure(failure: Failure) {
        hideProgress()
    }
}
