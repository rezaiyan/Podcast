package com.hezaro.wall.sdk.platform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Base Fragment class with helper methods for handling views and back button events.
 *
 * @see Fragment
 */
abstract class BaseFragment : Fragment() {

    abstract fun layoutId(): Int
    abstract fun tag(): String
    abstract fun id(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(layoutId(), container, false)

    override fun onStop() {
        hideProgress()
        super.onStop()
    }

    fun showMessage(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun showProgress() = progressStatus(View.VISIBLE)

    fun hideProgress() = progressStatus(View.GONE)

    private fun progressStatus(viewStatus: Int) {
        with(activity) { if (this is BaseActivity) progressBar().visibility = viewStatus }
    }
}
