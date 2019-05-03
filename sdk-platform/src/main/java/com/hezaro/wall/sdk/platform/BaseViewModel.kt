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
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Base ViewModel class with default Failure handling.
 * @see ViewModel
 * @see Failure
 */
abstract class BaseViewModel : ViewModel(), CoroutineScope {

    var failure: MutableLiveData<Failure> = MutableLiveData()
    var isExecute = false
    var job = Job()

    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    public override fun onCleared() {
        super.onCleared()
        isExecute = false
        job.cancel()
        job = Job()
    }


    protected fun onFailure(it: Failure) {
        launch(Dispatchers.Main) {
            isExecute = false
            failure.value = it
        }
    }

    fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T) -> Unit) =
        liveData.observe(this, Observer(body))

    fun <L : LiveData<Failure>> LifecycleOwner.failure(liveData: L, body: (Failure) -> Unit) =
        liveData.observe(this, Observer(body))
}