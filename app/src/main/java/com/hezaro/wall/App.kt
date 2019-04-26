package com.hezaro.wall

import android.app.Application
import com.hezaro.wall.utils.module
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(module))
        if (BuildConfig.DEBUG) {
            Logger.addLogAdapter(AndroidLogAdapter())
            Timber.plant(Timber.DebugTree())
            //            Timber.plant(object : Timber.DebugTree() {
//                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//                    Logger.log(priority, tag, message, t)
//                }
//            })
        }
    }
}