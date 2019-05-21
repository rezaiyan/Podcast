package com.hezaro.wall.feature.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.hezaro.wall.R
import com.hezaro.wall.feature.adapter.PagerAdapter
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.sdk.platform.BaseFragment
import com.hezaro.wall.sdk.platform.ext.load
import com.hezaro.wall.utils.CircleTransform
import com.hezaro.wall.utils.PROFILE
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
    override fun id() = PROFILE
    private val vm: ProfileViewModel by inject()

    companion object {
        fun getInstance() = ProfileFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userInfo = GoogleSignIn.getLastSignedInAccount(context!!)

        userInfo?.let {
            avatar.load(it.photoUrl.toString(), CircleTransform())
            username.text = it.displayName
            email.text = it.email
        }

        val menu = PopupMenu(requireContext(), moreProfile)
        menu.inflate(R.menu.profile)
        val switchItem = menu.menu.findItem(R.id.switchTheme)


        menu.setOnMenuItemClickListener {

            if (it.itemId == R.id.signout) {
                activity!!.onBackPressed()
                (activity as MainActivity).signOut()
            } else
                if (it.itemId == R.id.switchTheme) {
                    val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                    AppCompatDelegate.setDefaultNightMode(if (isNight) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES)
                    switchItem.isChecked = !isNight
                    vm.setThemeStatus(isNight)
                }
            true
        }

        moreProfile.setOnClickListener { menu.show() }

        viewpager.adapter =
            PagerAdapter(
                childFragmentManager,
                arrayOf(DownloadFragment.getInstance(), BookmarkFragment.getInstance()),
                arrayOf("دانلودها", "بوکمارک‌ها")
            )
        tabLayout.setupWithViewPager(viewpager)
    }
}