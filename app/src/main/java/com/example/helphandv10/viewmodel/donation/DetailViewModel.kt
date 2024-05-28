package com.example.helphandv10.viewmodel.donation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DetailViewModel(private val donationRepository: DonationRepository) : ViewModel() {
    private val _donationDetailState = MutableLiveData<Resource<Donations>>()
    val donationDetailState: LiveData<Resource<Donations>> = _donationDetailState

    constructor() : this(DonationRepository(FirebaseFirestore.getInstance()))

    fun getDonationById(donationId: String) {
        viewModelScope.launch {
            _donationDetailState.value = Resource.Loading
            try {
                donationRepository.getDonationById(donationId)
                    .collect { resource ->
                        _donationDetailState.value = resource
                    }
            } catch (e: Exception) {
                _donationDetailState.value = Resource.Error("Error fetching donation")
            }
        }
    }
}