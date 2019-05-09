package com.hezaro.wall.utils

import android.preference.PreferenceManager
import com.hezaro.wall.data.utils.provideRetrofit
import com.hezaro.wall.domain.EpisodeRepository
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.domain.MainRepository
import com.hezaro.wall.domain.MessagingRepository
import com.hezaro.wall.domain.PlayerRepository
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.domain.SearchRepository
import com.hezaro.wall.feature.core.main.MainViewModel
import com.hezaro.wall.feature.core.player.PlayerViewModel
import com.hezaro.wall.feature.episode.EpisodeViewModel
import com.hezaro.wall.feature.explore.ExploreViewModel
import com.hezaro.wall.feature.profile.ProfileViewModel
import com.hezaro.wall.feature.search.SearchViewModel
import com.hezaro.wall.notification.MessagingViewModel
import com.hezaro.wall.sdk.platform.player.download.PlayerDownloadHelper
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

val module: Module = module {

    viewModel { MessagingViewModel(get()) }
    single { MessagingRepository.ProfileRepositoryImpl(get()) } bind MessagingRepository::class

    viewModel { EpisodeViewModel(get()) }
    single { EpisodeRepository.EpisodeRepositoryImpl(get()) } bind EpisodeRepository::class

    viewModel { ExploreViewModel(get()) }
    single { ExploreRepository.ExploreRepositoryImpl(get(), get()) } bind ExploreRepository::class

    viewModel { MainViewModel(get(), get()) }
    single { MainRepository.MainRepositoryImpl(get(), get(), get()) } bind MainRepository::class

    viewModel { ProfileViewModel(get()) }
    single { ProfileRepository.ProfileRepositoryImpl(get(), get()) } bind ProfileRepository::class

    single { PlayerRepository.PlayerRepositoryImpl(get(), get(), get()) } bind PlayerRepository::class
    viewModel { PlayerViewModel(get()) }
    single { SearchRepository.SearchRepositoryImpl(get(), get()) } bind SearchRepository::class
    viewModel { SearchViewModel(get()) }

    single { PlayerDownloadHelper(androidContext()) }
    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    single { provideRetrofit() }


    single { AppDatabase.getInstance(androidContext()) }
    single { getEpisodeDAO(get()) }
    single { getPodcastDAO(get()) }

}

fun getEpisodeDAO(appDatabase: AppDatabase) = appDatabase.episodeDao()

fun getPodcastDAO(appDatabase: AppDatabase) = appDatabase.podcastDao()

