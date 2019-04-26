package com.hezaro.wall.feature.search

import com.hezaro.wall.R
import com.hezaro.wall.sdk.platform.BaseFragment

class SearchFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_search
    override fun tag(): String = this::class.java.simpleName
}