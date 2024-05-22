package com.example.helphandv10.di

import com.example.helphandv10.viewmodel.donation.ListViewModel
import com.example.helphandv10.viewmodel.donation.AddViewModel
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.viewmodel.donation.HistoryViewModel
import com.example.helphandv10.viewmodel.donation.UpdateViewModel
import com.example.helphandv10.viewmodel.donation.DeleteViewModel
import com.example.helphandv10.viewmodel.donation.SearchViewModel
import com.example.helphandv10.viewmodel.form.DonationReceiveViewModel
import com.example.helphandv10.viewmodel.form.DonationSendViewModel
import com.example.helphandv10.viewmodel.form.ManageDonorsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { ListViewModel(get()) }
    viewModel { AddViewModel(get()) }
    viewModel { UpdateViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { DeleteViewModel(get()) }
    viewModel { DonationSendViewModel(get()) }
    viewModel { ManageDonorsViewModel(get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { DonationReceiveViewModel(get()) }
}

val repositoryModule = module {
    single<DonationRepository> { DonationRepository(get()) }
}