package com.lovelybible.di

import com.lovelybible.core.display.MonitorManager
import com.lovelybible.core.resource.ResourceLoader
import com.lovelybible.data.local.parser.BibleJsonParser
import com.lovelybible.data.repository.BibleRepositoryImpl
import com.lovelybible.domain.repository.BibleRepository
import com.lovelybible.feature.navigation.NavigationViewModel
import com.lovelybible.feature.presentation.PresentationViewModel
import com.lovelybible.feature.search.SearchViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * LovelyBible 앱 Koin DI 모듈
 * Clean Architecture 레이어별 의존성 정의
 */
val appModule = module {
    // ============================================
    // Core Layer
    // ============================================
    // ResourceLoader는 플랫폼별로 제공 (expect/actual 패턴)
    
    // ============================================
    // Data Layer - Parsers & Repositories
    // ============================================
    singleOf(::BibleJsonParser)
    
    single<BibleRepository> {
        BibleRepositoryImpl(
            parser = get(),
            jsonLoader = { get<ResourceLoader>().loadBibleJson() }
        )
    }
    
    // ============================================
    // Domain Layer - UseCases
    // ============================================
    // TODO: GetBibleBooksUseCase, SearchBooksUseCase 등
    
    // ============================================
    // Presentation Layer - ViewModels
    // ============================================
    single { NavigationViewModel(repository = get()) }
    single { SearchViewModel(
        repository = get(), 
        navigationViewModel = get(),
        settingsViewModel = get(),
        presentationViewModel = get()
    ) }
    single { PresentationViewModel(monitorManager = get(), navigationViewModel = get(), settingsRepository = get()) }
    single { com.lovelybible.feature.settings.SettingsViewModel(repository = get()) }
}

/**
 * 모든 모듈을 포함하는 리스트
 */
val allModules = listOf(appModule)

