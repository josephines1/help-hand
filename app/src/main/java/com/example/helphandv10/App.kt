package com.example.helphandv10

import android.app.Application
import com.example.helphandv10.di.repositoryModule
import com.example.helphandv10.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf(
                viewModelModule,
                repositoryModule,
            ))
        }
    }
}