package com.example.helphandv10.viewmodel.donation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import kotlinx.coroutines.launch

class AddViewModel(private val repository: DonationRepository) : ViewModel() {

    private val _donationAdded = MutableLiveData(false)
    val donationAdded: LiveData<Boolean>
        get() = _donationAdded

    fun addDonation(donation: Donations) {
        viewModelScope.launch {
            repository.insert(donation).collect {
                when (it) {
                    is Resource.Error -> {
                        _donationAdded.value = false
                        Log.e("AddViewModel: Insert", it.error)
                    }
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        _donationAdded.value = true
                    }
                }
            }
        }
    }
}