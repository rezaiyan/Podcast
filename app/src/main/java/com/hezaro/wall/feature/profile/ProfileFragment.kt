package com.hezaro.wall.feature.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hezaro.wall.R
import com.hezaro.wall.R.string
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.feature.adapter.PagerAdapter
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.CircleTransform
import kotlinx.android.synthetic.main.fragment_profile.avatar
import kotlinx.android.synthetic.main.fragment_profile.email
import kotlinx.android.synthetic.main.fragment_profile.moreProfile
import kotlinx.android.synthetic.main.fragment_profile.tabLayout
import kotlinx.android.synthetic.main.fragment_profile.username
import kotlinx.android.synthetic.main.fragment_profile.viewpager
import org.koin.android.ext.android.inject

class ProfileFragment : BaseFragment() {
    override fun layoutId() = R.layout.fragment_profile
    override fun tag(): String = this::class.java.simpleName
    override fun id() = 103
    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = ProfileFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(string.server_client_id))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context!!, gso)

        val menu = PopupMenu(requireContext().applicationContext, moreProfile)
        menu.inflate(R.menu.profile)
        val switchItem = menu.menu.findItem(R.id.switchTheme)
        menu.setOnMenuItemClickListener {
            if (it.itemId == R.id.signout) {
                activity!!.onBackPressed()
                googleSignInClient.signOut()
            } else if (it.itemId == R.id.switchTheme) {
                val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                AppCompatDelegate.setDefaultNightMode(if (isNight) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES)
                switchItem.isChecked = !isNight
                vm.setThemeStatus(isNight)
            }
            true
        }

        moreProfile.setOnClickListener { menu.show() }
        with(vm) {
            observe(userInfo, ::onSuccess)
            failure(failure, ::onFailure)
            userInfo()
        }
        viewpager.adapter =
            PagerAdapter(childFragmentManager, arrayOf(DownloadFragment.getInstance()), arrayOf("دانلودها"))
        tabLayout.setupWithViewPager(viewpager)
    }

    private fun onSuccess(userInfo: UserInfo) {
        userInfo.let {
            avatar.load(it.avatar, CircleTransform())
            username.text = it.username
            email.text = it.email
        }
    }

    private fun onFailure(failure: Failure) {
        hideProgress()
    }
}