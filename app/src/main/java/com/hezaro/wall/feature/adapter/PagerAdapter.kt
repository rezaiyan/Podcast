package com.hezaro.wall.feature.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter(
    fragmentManager: FragmentManager,
    private val fragments: Array<Fragment>,
    private val titles: Array<String>
) :
    FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int) = fragments[position]
    override fun getCount() = fragments.size
    override fun getPageTitle(position: Int) = titles[position]
}