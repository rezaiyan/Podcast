package com.hezaro.wall.feature.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.hezaro.wall.feature.profile.DownloadFragment

class PagerAdapter(
    fragmentManager: FragmentManager,
    private val fragments: Array<Fragment>,
    private val titles: Array<String>
) :
    FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int) = DownloadFragment.getInstance()
    override fun getCount() = fragments.size
    override fun getPageTitle(position: Int) = titles[position]
}