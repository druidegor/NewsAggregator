package org.newsagg.project

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.newsagg.project.di.initKoin

class NewsApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            modules(androidDatabaseModule)
            androidLogger()
            androidContext(this@NewsApp)
        }
    }
}