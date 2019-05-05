package com.hezaro.wall.feature.profile

import android.os.Bundle
import android.view.View
import com.hezaro.wall.R
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import kotlinx.android.synthetic.main.fragment_profile.avatar
import org.koin.android.ext.android.inject

class ProfileFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_profile
    override fun tag(): String = this::class.java.simpleName

    private val vm: ProfileViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(userInfo, ::onSuccess)
            failure(failure, ::onFailure)
        }
    }

    private fun onSuccess(userInfo: UserInfo) {
        avatar.load("")
    }

    private fun onFailure(failure: Failure) {
        hideProgress()
    }
}