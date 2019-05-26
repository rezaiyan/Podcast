package com.hezaro.wall

import android.app.Application
import com.facebook.stetho.Stetho
import com.hezaro.wall.utils.module
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(module))
        }
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            Logger.addLogAdapter(AndroidLogAdapter())
            Timber.plant(Timber.DebugTree())
        }
    }
}