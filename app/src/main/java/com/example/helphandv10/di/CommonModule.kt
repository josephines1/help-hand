package com.example.helphandv10.di

import com.example.helphandv10.ListViewModel
import com.example.helphandv10.data.DonationRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { ListViewModel(get()) }
}

val repositoryModule = module {
    single<DonationRepository> { DonationRepository(get()) }
}