package com.hezaro.wall.sdk.platform


import androidx.lifecycle.MutableLiveData
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.test.AndroidTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test



class BaseViewModelTest : AndroidTest() {


    @Test
    fun `should handle failure by updating live data`() {
        val viewModel = MyViewModel()

        viewModel.handleError(Failure.NetworkConnection())

        val failure = viewModel.failure
        val error = viewModel.failure.value

        failure shouldBeInstanceOf MutableLiveData::class.java
        error shouldBeInstanceOf Failure.NetworkConnection::class.java
    }

    private class MyViewModel : BaseViewModel() {
        fun handleError(failure: Failure) = handleFailure(failure)
    }
}