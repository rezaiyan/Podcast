package com.hezaro.wall.feature.profile

import android.os.Bundle
import android.view.View
import com.hezaro.wall.R
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.feature.core.main.MainActivity
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import kotlinx.android.synthetic.main.fragment_profile.avatar
import kotlinx.android.synthetic.main.fragment_profile.email
import kotlinx.android.synthetic.main.fragment_profile.tabLayout
import kotlinx.android.synthetic.main.fragment_profile.username
import kotlinx.android.synthetic.main.fragment_profile.viewpager
import org.koin.android.ext.android.inject

class ProfileFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_profile
    override fun tag(): String = this::class.java.simpleName

    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = ProfileFragment()
    }

    override fun onBackPressed() {
        (activity as MainActivity).resetPlaylist.value = downloadFragment!!.playlistCreated
        super.onBackPressed()
    }

    private var downloadFragment: DownloadFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(userInfo, ::onSuccess)
            failure(failure, ::onFailure)
        }
        downloadFragment = DownloadFragment.getInstance()
        viewpager.adapter = PagerAdapter(childFragmentManager, arrayOf(downloadFragment!!))
        tabLayout.setupWithViewPager(viewpager)
    }

    override fun onDestroyView() {
        downloadFragment = null
        super.onDestroyView()
    }

    private fun onSuccess(userInfo: UserInfo) {
        userInfo.let {
            avatar.load(it.avatar)
            username.text = it.username
            email.text = it.email
        }
    }

    private fun onFailure(failure: Failure) {
        hideProgress()
    }
}