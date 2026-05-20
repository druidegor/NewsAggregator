package org.newsagg.project.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}, additionalModules: List<Module> = emptyList()) {
    startKoin {
        appDeclaration()
        modules(
            databaseModule,
            repositoryModule,
            domainModule,
            screenModule,
            networkModule,
            *additionalModules.toTypedArray()
        )
    }
}
