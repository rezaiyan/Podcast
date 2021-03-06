object App {
    val id = "com.hezaro.wall"
    val compileSdk = 28
    val minSdk = 21
    val minSdkTv = 21
    val targetSdk = 28
    val versionCode = 1
    val versionName = "1.0.0-alpha05"
}

object GradleDir {
    val android = "../android-library.gradle"
}

object BuildPlugins {
    val androidGradle = "com.android.tools.build:gradle:${Versions.gradle}"
    val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val googleServices = "com.google.gms:google-services:4.2.0"
    val fabric = "io.fabric.tools:gradle:1.26.1"
}

object Repo {
    val fabric = "https://maven.fabric.io/public"
}

object Modules {
    val app = ":app"
    val domain = ":domain"
    val data = ":data"
    val sdkBase = ":sdk-base"
    val sdkPlatform = ":sdk-platform"
    val sdkTest = ":sdk-test"
}

object Versions {
    val gradle = "3.5.0"

    val appcompat = "1.1.0-alpha05"
    val archComponents_version = "2.0.0-rc01"

    val constraint = "2.0.0-alpha3"
    val design = "1.1.0-alpha05"
    val cardview = "1.0.0"
    val recyclerview = "1.0.0"
    val androidx = "1.0.0-rc01"
    val paging = "2.1.0-rc01"

    val ktx = "1.0.1"


    val kotlin = "1.3.30"
    val kotlinCoroutines = "1.0.1"
    val timber = "4.7.1"
    val retrofit = "2.5.0"
    val okHttp = "3.12.0"
    val loggingInterceptor = "3.12.0"
    val moshi = "1.8.0"
    val lifecycle = "2.0.0"
    val leakCanary = "1.6.2"
    val koin = "2.0.0"
    val gson = "2.8.5"
    val okio = "2.2.2"
    val coroutinesAdapter = "0.9.2"
    val logger = "2.2.0"

    val junit = "4.12"
    val mockito = "2.18.3"
    val assertjCore = "3.11.1"
    val mockitoKotlin = "2.0.0-RC1"
    val mockitoInline = "2.8.9"
    val robolectric_version = "3.8"
    val kluent_version = "1.14"
    val glide = "4.8.0"

    val javaxInject_version = "1"
    val javaxAnnotations_version = "1.0"

    val picasso = "2.71828"

    val firebase = "16.0.7"
    val firebaseMessaging = "17.6.0"
    val crashlytics = "2.9.9"

    val exoPlayer = "2.9.6"
    val playService = "16.0.1"

    val rxJava = "2.2.0"
    val rxAndroid = "2.1.0"

    val room = "2.1.0-alpha07"
    val lottie = "3.0.1"
    val persianDate = "0.1"
    val stetho = "1.5.1"
    val canary = "2.0-alpha-1"

}

object Libraries {
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"

    val media = "androidx.media:media:${Versions.androidx}"
    val ktx = "androidx.core:core-ktx:${Versions.ktx}"
    val paging = "androidx.paging:paging-runtime-ktx:${Versions.paging}"

    val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"
    val coroutinesAdapter = "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:${Versions.coroutinesAdapter}"

    val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    val roomRxjava = "androidx.room:room-rxjava2:${Versions.room}"
    val roomCoroutines = "androidx.room:room-ktx:${Versions.room}"
    val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    val roomCompiler = "androidx.room:room-compiler:${Versions.room}"


    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val okhttp = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val rxjavaAdapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.loggingInterceptor}"

    val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    val gsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"

    val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    val lifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"

    val leakCanaryAndroid = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    val leakCanaryAndroidNoOp = "com.squareup.leakcanary:leakcanary-android-no-op:${Versions.leakCanary}"
    val leakCanaryAndroidSupportFragment = "com.squareup.leakcanary:leakcanary-support-fragment:${Versions.leakCanary}"

    val koinViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
    val koinAndroid = "org.koin:koin-android:${Versions.koin}"
    val koinCore = "org.koin:koin-core:${Versions.koin}"
    val gson = "com.google.code.gson:gson:${Versions.gson}"
    val okio = "com.squareup.okio:okio:${Versions.okio}"
    val javaxInject = "javax.inject:javax.inject:${Versions.javaxInject_version}"
    val javaxAnnotation = "javax.annotation:jsr250-api:${Versions.javaxAnnotations_version}"
    val picasso = "com.squareup.picasso:picasso:${Versions.picasso}"

    val firebaseCore = "com.google.firebase:firebase-core:${Versions.firebase}"
    val firebaseMessaging = "com.google.firebase:firebase-messaging:${Versions.firebaseMessaging}"
    val crashlytics = "com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics}"

    val playServiceAuth = "com.google.android.gms:play-services-auth:${Versions.playService}"

    val exoPlayerCore = "com.google.android.exoplayer:exoplayer-core:${Versions.exoPlayer}"
    val exoPlayerDash = "com.google.android.exoplayer:exoplayer-dash:${Versions.exoPlayer}"
    val exoPlayerUi = "com.google.android.exoplayer:exoplayer-ui:${Versions.exoPlayer}"
    val exoPlayerHls = "com.google.android.exoplayer:exoplayer-hls:${Versions.exoPlayer}"
    val exoPlayerIma = "com.google.android.exoplayer:exoplayer-ima:${Versions.exoPlayer}"
    val exoPlayerExMediaSession = "com.google.android.exoplayer:extension-mediasession:${Versions.exoPlayer}"
    val exoPlayersmoothstreaming = "com.google.android.exoplayer:exoplayer-smoothstreaming:${Versions.exoPlayer}"

    val logger = "com.orhanobut:logger:${Versions.logger}"

    val lottie = "com.airbnb.android:lottie:${Versions.lottie}"
    val glide =  "com.github.bumptech.glide:glide:${Versions.glide}"
    val glideCompiler =  "com.github.bumptech.glide:compiler:${Versions.glide}"

    val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJava}"
    val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"

    val persianDate = "com.github.samanzamani.persiandate:PersianDate:${Versions.persianDate}"
    val stetho = "com.facebook.stetho:stetho:${Versions.stetho}"
    val canary = "com.squareup.leakcanary:leakcanary-android:${Versions.canary}"
}

object SupportLibraries {
    val archComponents = "androidx.lifecycle:lifecycle-extensions:${Versions.archComponents_version}"

    val leanback = "androidx.leanback:leanback:${Versions.appcompat}"
    val annotation = "androidx.annotation:annotation:${Versions.appcompat}"
    val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraint}"
    val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    val design = "com.google.android.material:material:${Versions.design}"
    val cardview = "androidx.cardview:cardview:${Versions.cardview}"
    val recyclerview = "androidx.recyclerview:recyclerview:${Versions.recyclerview}"
}

object TestLibraries {
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val kotlinTest = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
    val androidJUnitRunner = "androidx.test.runner.AndroidJUnitRunner"
    val junit = "junit:junit:${Versions.junit}"
    val jupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
    val jupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit}"
    val runner = "androidx.test:runner:1.1.0"
    val rules = "androidx.test:rules:1.1.0"
    val espressoCore = "androidx.test.espresso:espresso-core:3.1.0"
    val espressoIntent = "androidx.test.espresso:espresso-intents:3.1.0"
    val espressoContrib = "androidx.test.espresso:espresso-contrib:3.1.0"
    val xjunit = "androidx.test.ext:junit:1.0.0"
    val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    val assertjCore = "org.assertj:assertj-core:${Versions.assertjCore}"
    val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    val mockitoInline = "org.mockito:mockito-inline:${Versions.mockitoInline}"
    val lifecycleTesting = "androidx.arch.core:core-testing:${Versions.lifecycle}"
    val robolectric = "org.robolectric:robolectric:${Versions.robolectric_version}"
    val kluent = "org.amshove.kluent:kluent:${Versions.kluent_version}"
    val retrofitMock = "com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}"
    val okhttpMockServer = "com.squareup.okhttp3:mockwebserver:${Versions.okHttp}"

}