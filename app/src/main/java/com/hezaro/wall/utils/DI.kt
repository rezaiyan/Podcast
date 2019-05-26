package com.hezaro.wall.utils

import android.preference.PreferenceManager
import com.hezaro.wall.data.base.provideRetrofit
import com.hezaro.wall.domain.EpisodeRepository
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.domain.MainRepository
import com.hezaro.wall.domain.MessagingRepository
import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.domain.PodcastRepository
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.domain.SearchRepository
import com.hezaro.wall.domain.SplashRepository
import com.hezaro.wall.feature.episode.EpisodeViewModel
import com.hezaro.wall.feature.explore.ExploreViewModel
import com.hezaro.wall.feature.main.MainViewModel
import com.hezaro.wall.feature.main.SharedViewModel
import com.hezaro.wall.feature.player.PlayerViewModel
import com.hezaro.wall.feature.podcast.PodcastViewModel
import com.hezaro.wall.feature.profile.ProfileViewModel
import com.hezaro.wall.feature.search.SearchViewModel
import com.hezaro.wall.feature.splash.SplashViewModel
import com.hezaro.wall.notification.MessagingViewModel
import com.hezaro.wall.notification.player.MediaSessionHelper
import com.hezaro.wall.notification.player.PlayerNotificationHelper
import com.hezaro.wall.sdk.platform.player.LocalMediaPlayer
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import com.hezaro.wall.sdk.platform.player.download.PlayerDownloadHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val module: Module = module {

    viewModel { SharedViewModel() }

    viewModel { SplashViewModel(get()) }
    single { SplashRepository.SplashRepositoryImpl(get(), get()) } bind SplashRepository::class

    viewModel { MessagingViewModel(get()) }
    single { MessagingRepository.MessagingRepositoryImpl(get(), get()) } bind MessagingRepository::class

    viewModel { EpisodeViewModel(get()) }
    single { EpisodeRepository.EpisodeRepositoryImpl(get(), get()) } bind EpisodeRepository::class

    viewModel { ExploreViewModel(get()) }
    single { ExploreRepository.ExploreRepositoryImpl(get(), get(), get()) } bind ExploreRepository::class

    viewModel { PodcastViewModel(get()) }
    single { PodcastRepository.PodcastRepositoryImpl(get()) } bind PodcastRepository::class

    viewModel { MainViewModel(get(), get()) }
    single { MainRepository.MainRepositoryImpl(get(), get(), get()) } bind MainRepository::class

    viewModel { ProfileViewModel(get()) }
    single { ProfileRepository.ProfileRepositoryImpl(get(), get(), get()) } bind ProfileRepository::class

    single { PlayerRepository.PlayerRepositoryImpl(get(), get(), get()) } bind PlayerRepository::class
    viewModel { PlayerViewModel(get()) }
    single { SearchRepository.SearchRepositoryImpl(get(), get(), get()) } bind SearchRepository::class
    viewModel { SearchViewModel(get()) }

    single { PlayerDownloadHelper(androidApplication()) }
    single { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }

    single { provideRetrofit(androidApplication()) }

    single { AppDatabase.getInstance(androidApplication()) }
    single { getEpisodeDAO(get()) }

    single { MediaSessionHelper(androidApplication(), get()) }
    single { PlayerNotificationHelper(androidApplication(), it[0], get()) }
    single { LocalMediaPlayer(androidApplication()) } bind MediaPlayer::class

}

fun getEpisodeDAO(appDatabase: AppDatabase) = appDatabase.episodeDao()
