package com.example.helphandv10.viewmodel.donation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryViewModel(private val donationRepository: DonationRepository) : ViewModel() {

    fun getDonationsByDonor() = donationRepository.getDonationsByDonor()

    fun getDonationsByOrganizer() = donationRepository.getDonationsByOrganizer()

}

class HistoryViewModelFactory(private val donationRepository: DonationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(donationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}