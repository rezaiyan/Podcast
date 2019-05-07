package com.hezaro.wall.feature.profile

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int) = ListFragment.getInstance()
    override fun getCount() = 1
    override fun getPageTitle(position: Int) = "دانلود شده ها"
}