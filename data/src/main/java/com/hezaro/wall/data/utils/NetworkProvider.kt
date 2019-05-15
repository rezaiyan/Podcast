package com.hezaro.wall.data.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.hezaro.wall.data.BuildConfig
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.extention.JWT
import com.hezaro.wall.sdk.base.extention.get
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.orhanobut.logger.Logger
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.context.ModuleDefinition
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS

fun provideRetrofit(okHttpClient: OkHttpClient): ApiService {
    return Retrofit.Builder()
        .baseUrl("http://wall.hezaro.com")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build().create(ApiService::class.java)
}

fun ModuleDefinition.provideHttpClient(storage: SharedPreferences): OkHttpClient {
    val jwt = storage.get(JWT, "")
    val clientBuilder = OkHttpClient.Builder()
        .cache(provideCache())
        .addNetworkInterceptor(networkCacheProvider())
        .addInterceptor(offlineCacheProvider())
        .readTimeout(5, SECONDS)
        .connectTimeout(10, SECONDS)
        .addInterceptor(setHeader("User-Agent", androidContext().packageName))
        .addInterceptor(setHeader("X-App-Token", BuildConfig.API_KEY))
        .addInterceptor(setHeader("Authorization", "Bearer $jwt"))

    val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
        if (message.isJson())
            Logger.json(message)
        else Logger.d(message)
    })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        clientBuilder.addInterceptor(httpLoggingInterceptor)

    return clientBuilder.build()
}

fun ModuleDefinition.provideCache(): Cache {
    val cacheSize = 10 * 1024 * 1024
    val fileCache = java.io.File(androidContext().cacheDir, "responses")
    return okhttp3.Cache(fileCache, cacheSize.toLong())
}

fun setHeader(key: String, value: String): Interceptor {
    return Interceptor {
        var request = it.request()

        request = request.newBuilder()
            .header(key, value)
            .build()
        it.proceed(request)
    }
}

fun ModuleDefinition.offlineCacheProvider(): Interceptor {
    return Interceptor {
        var request = it.request()

        if (!isOnline()) {
            val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
            request = request.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .build()
        }
        it.proceed(request)
    }
}

fun ModuleDefinition.networkCacheProvider(): Interceptor {
    return Interceptor {
        val originalResponse = it.proceed(it.request())
        val cacheControl = originalResponse.header("Cache-Control")
        if ((cacheControl == null || cacheControl.contains("no-store") ||
                    cacheControl.contains("no-cache") ||
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) &&
            !isOnline()
        ) {
            val maxAge = 60 // read from cache for 1 minute
            originalResponse.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, max-age=$maxAge")
                .build()
        } else {
            originalResponse
        }
    }
}

@SuppressLint("MissingPermission")
fun ModuleDefinition.isOnline(): Boolean {
    val cm = androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val netInfo = cm!!.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}