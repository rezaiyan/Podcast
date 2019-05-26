package com.hezaro.wall.data.base

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import com.hezaro.wall.data.BuildConfig
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.extention.JWT
import com.hezaro.wall.sdk.base.extention.get
import com.hezaro.wall.sdk.base.extention.isJson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.orhanobut.logger.Logger
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS

fun provideRetrofit(context: Context): ApiService {
    val jwt = PreferenceManager.getDefaultSharedPreferences(context).get(JWT, "")
    return Retrofit.Builder()
        .baseUrl("http://wall.hezaro.com")
        .client(provideHttpClient(context, jwt))
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build().create(ApiService::class.java)
}

fun provideHttpClient(context: Context, jwt: String): OkHttpClient {
    val clientBuilder = OkHttpClient.Builder()
        .cache(provideCache(context))
        .addNetworkInterceptor(networkCacheProvider(context))
        .addInterceptor(offlineCacheProvider(context))
        .readTimeout(5, SECONDS)
        .connectTimeout(10, SECONDS)
        .addInterceptor(setHeader("Authorization", "Bearer $jwt"))
        .addInterceptor(setHeader("User-Agent", context.packageName))
        .addInterceptor(setHeader("X-App-Token", BuildConfig.API_KEY))

    val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
        if (message.isJson())
            Logger.json(message)
        else Logger.d(message)
    })
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    clientBuilder.addInterceptor(httpLoggingInterceptor)

    return clientBuilder.build()
}

fun provideCache(context: Context): Cache {
    val cacheSize = 10 * 1024 * 1024
    val fileCache = java.io.File(context.cacheDir, "responses")
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

fun offlineCacheProvider(context: Context): Interceptor {
    return Interceptor {
        var request = it.request()

        if (!isOnline(context)) {
            val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
            request = request.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .build()
        }
        it.proceed(request)
    }
}

fun networkCacheProvider(context: Context): Interceptor {
    return Interceptor {
        val originalResponse = it.proceed(it.request())
        val cacheControl = originalResponse.header("Cache-Control")
        if ((cacheControl == null || cacheControl.contains("no-store") ||
                    cacheControl.contains("no-cache") ||
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) &&
            !isOnline(context)
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
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val netInfo = cm!!.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}