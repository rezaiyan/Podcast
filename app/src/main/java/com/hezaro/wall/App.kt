package com.hezaro.wall

import android.app.Application
import com.hezaro.wall.utils.module
import org.koin.android.ext.android.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(module))
    }
}