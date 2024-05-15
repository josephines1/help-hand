package com.example.helphandv10.di

import com.example.helphandv10.viewmodel.donation.ListViewModel
import com.example.helphandv10.viewmodel.donation.AddViewModel
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.viewmodel.donation.UpdateViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { ListViewModel(get()) }
    viewModel { AddViewModel(get()) }
    viewModel { UpdateViewModel(get()) }
}

val repositoryModule = module {
    single<DonationRepository> { DonationRepository(get()) }
}