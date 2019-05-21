package com.hezaro.wall.sdk.platform

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.hezaro.wall.sdk.base.exception.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Base ViewModel class with default Failure handling.
 * @see ViewModel
 * @see Failure
 */
abstract class BaseViewModel : ViewModel(), CoroutineScope {

    var progress: MutableLiveData<Boolean> = MutableLiveData()
    var failure: MutableLiveData<Failure> = MutableLiveData()
    var job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    public override fun onCleared() {
        super.onCleared()
        progress.postValue(false)
        job.cancel()
        job = Job()
    }

    protected fun onFailure(it: Failure) {
        progress.postValue(false)
        failure.postValue(it)
    }

    fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T) -> Unit) =
        liveData.observe(this, Observer(body))

    fun <L : LiveData<Failure>> LifecycleOwner.failure(liveData: L, body: (Failure) -> Unit) =
        liveData.observe(this, Observer(body))
}