package com.example.helphandv10.viewmodel.donation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchViewModel(private val donationRepository: DonationRepository) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Donations>>()
    val searchResults: LiveData<List<Donations>> = _searchResults

    private var currentKeyword = ""

    fun searchDonations(keyword: String) {
        currentKeyword = keyword
        donationRepository.getDonation().onEach { donations ->
            val filteredDonations = if (keyword.isNotBlank()) {
                // Filter donations based on keyword
                donations.filter { donation ->
                    donation.title.contains(keyword, ignoreCase = true) || // Check if title contains keyword
                            donation.title.split(" ").any { it.startsWith(keyword, ignoreCase = true) } // Check if any word in title starts with keyword
                }
            } else {
                // Show all donations if keyword is empty
                donations
            }
            _searchResults.postValue(filteredDonations)
        }.launchIn(viewModelScope)
    }

    fun getAllDonations() {
        viewModelScope.launch {
            val donationsFlow = donationRepository.getDonation()
            val donationsList = mutableListOf<Donations>()
            donationsFlow.collect { donations ->
                donationsList.clear()
                donationsList.addAll(donations)
            }
            _searchResults.value = donationsList
        }
    }
}