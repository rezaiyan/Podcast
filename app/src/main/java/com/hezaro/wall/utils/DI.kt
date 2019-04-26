package com.hezaro.wall.utils

import android.preference.PreferenceManager
import com.hezaro.wall.data.utils.provideRetrofit
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.domain.LoginRepository
import com.hezaro.wall.domain.ProfileRepository
import com.hezaro.wall.feature.core.main.MainViewModel
import com.hezaro.wall.feature.explore.ExploreViewModel
import com.hezaro.wall.feature.profile.ProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

val module: Module = module {

    viewModel { ExploreViewModel(get()) }
    single { ExploreRepository.ExploreRepositoryImpl(get()) } bind ExploreRepository::class
    viewModel { MainViewModel(get()) }
    single { LoginRepository.LoginRepositoryImpl(get(), get()) } bind LoginRepository::class
    viewModel { ProfileViewModel(get()) }
    single { ProfileRepository.ProfileRepositoryImpl(get()) } bind ProfileRepository::class

    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    single { provideRetrofit() }
}
