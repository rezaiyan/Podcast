package com.hezaro.wall

import com.hezaro.wall.feature.explore.ExploreFragment
import com.hezaro.wall.sdk.platform.BaseActivity

class MainActivity : BaseActivity() {
    override fun fragment() = ExploreFragment()
}
