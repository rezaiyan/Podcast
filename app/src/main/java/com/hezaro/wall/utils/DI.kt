package com.hezaro.wall.utils

import com.hezaro.wall.BuildConfig
import com.hezaro.wall.data.utils.provideRetrofit
import com.hezaro.wall.domain.ExploreRepository
import com.hezaro.wall.feature.explore.ExploreViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

val module: Module = module {

    viewModel { ExploreViewModel(get()) }
    single { ExploreRepository.ExploreRepositoryImpl(get()) } bind ExploreRepository::class
    single { provideRetrofit(BuildConfig.DEBUG) }
}
