package com.hezaro.wall.feature.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.hezaro.wall.R
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.normalize
import com.hezaro.wall.sdk.platform.ext.search
import kotlinx.android.synthetic.main.fragment_search.back
import kotlinx.android.synthetic.main.fragment_search.clear
import kotlinx.android.synthetic.main.fragment_search.inputSearch
import org.koin.android.ext.android.inject

class SearchFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_search
    override fun tag(): String = this::class.java.simpleName
    private val vm: SearchViewModel by inject()
    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(search, ::onSuccess)
            failure(failure, ::onFailure)
        }

        back.setOnClickListener { activity?.onBackPressed() }
        clear.setOnClickListener { inputSearch.setQuery("", false) }
        inputSearch.normalize()
        inputSearch.search { vm::doSearch }
    }

    private fun onSuccess(it: String) {
    }

    private fun onFailure(failure: Failure) {
    }
}