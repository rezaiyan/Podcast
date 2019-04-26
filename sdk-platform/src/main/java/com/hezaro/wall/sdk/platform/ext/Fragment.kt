package com.hezaro.wall.sdk.platform.ext

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

private const val EPISODE_FRAGMENT = 1
private const val PLAYER_FRAGMENT = 1
private const val COUNT_OF_BASE_FRAGMENTS = EPISODE_FRAGMENT + PLAYER_FRAGMENT
inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
    beginTransaction()
//        .setCustomAnimations(R.anim.enter_from_right,0,0, R.anim.exit_to_right)
        .func().doAddToBackStack(fragments.size).commit()

fun FragmentTransaction.doAddToBackStack(backStackEntryCount: Int): FragmentTransaction {
    if (backStackEntryCount > COUNT_OF_BASE_FRAGMENTS)
        addToBackStack("")
    return this
}