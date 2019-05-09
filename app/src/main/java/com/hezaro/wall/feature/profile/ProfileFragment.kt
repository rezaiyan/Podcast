package com.hezaro.wall.feature.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
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
import timber.log.Timber

class ProfileFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_profile
    override fun tag(): String = this::class.java.simpleName

    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = ProfileFragment()
    }

    override fun onAttach(context: Context?) {
        Timber.tag(tag()).i("onAttach")
        super.onAttach(context)
    }

    override fun onAttachFragment(childFragment: Fragment?) {
        Timber.tag(tag()).i("onAttachFragment")
        super.onAttachFragment(childFragment)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Timber.tag(tag()).i("onHiddenChanged")
        super.onHiddenChanged(hidden)
    }

    override fun onBackPressed() {
        (activity as MainActivity).finishFragment(tag(), downloadFragment.playlistCreated)
        Timber.tag(tag()).i("onBackPressed")
        super.onBackPressed()
    }

    override fun onResume() {
        Timber.tag(tag()).i("onResume")
        super.onResume()
    }

    lateinit var downloadFragment: ListFragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(vm) {
            observe(userInfo, ::onSuccess)
            failure(failure, ::onFailure)
        }
        downloadFragment = ListFragment.getInstance()
        viewpager.adapter = PagerAdapter(childFragmentManager, arrayOf(downloadFragment))
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