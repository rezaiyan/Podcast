package com.hezaro.wall.feature.episode

import com.hezaro.wall.R
import com.hezaro.wall.sdk.platform.BaseFragment

class EpisodeFragment : BaseFragment() {

    override fun layoutId() = R.layout.fragment_episode

    override fun tag(): String = this::class.java.simpleName
}