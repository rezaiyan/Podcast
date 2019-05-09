package com.hezaro.wall.feature.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter(fragmentManager: FragmentManager, private val fragments: Array<Fragment>) :
    FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int) = fragments[position]
    override fun getCount() = fragments.size
    override fun getPageTitle(position: Int) = "دانلود شده ها"
}