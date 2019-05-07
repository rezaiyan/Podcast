package com.hezaro.wall.feature.profile

import android.os.Bundle
import android.view.View
import com.hezaro.wall.R
import com.hezaro.wall.data.model.UserInfo
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(userInfo, ::onSuccess)
            failure(failure, ::onFailure)
        }

        viewpager.adapter = PagerAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewpager)
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