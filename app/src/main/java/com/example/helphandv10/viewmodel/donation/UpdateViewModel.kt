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

class UpdateViewModel(private val repository: DonationRepository) : ViewModel() {

    private val _donationUpdated = MutableLiveData(false)
    val donationUpdated: LiveData<Boolean>
        get() = _donationUpdated

    fun updateDonation(donation: Donations, imageUrl: String?) {
        viewModelScope.launch {
            repository.update(donation, imageUrl).collect {
                when (it) {
                    is Resource.Error -> {
                        _donationUpdated.value = false
                        Log.e("UpdateViewModel: Update", it.error)
                    }
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        _donationUpdated.value = true
                    }
                }
            }
        }
    }
}