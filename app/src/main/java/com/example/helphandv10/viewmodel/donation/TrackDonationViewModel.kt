package com.example.helphandv10.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackDonationViewModel(
    private val repository: DonationRepository
) : ViewModel() {

    private val _donations = MutableStateFlow<Resource<List<Donations>>>(Resource.Loading)
    val donations: StateFlow<Resource<List<Donations>>> get() = _donations

    fun getDonationsByDonor() {
        viewModelScope.launch {
            repository.getDonationsByDonor().collect { result ->
                _donations.value = Resource.Success(result)
            }
        }
    }
}

class TrackDonationViewModelFactory(
    private val repository: DonationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(TrackDonationViewModel::class.java)) {
            TrackDonationViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
