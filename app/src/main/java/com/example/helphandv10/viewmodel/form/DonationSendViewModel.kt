package com.example.helphandv10.viewmodel.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.utils.Resource
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class DonationSendViewModel(private val donationRepository: DonationRepository) : ViewModel() {

    private val _saveConfirmationStatus = MutableLiveData<Resource<Unit>>()
    val saveConfirmationStatus: LiveData<Resource<Unit>> get() = _saveConfirmationStatus

    fun saveDonationConfirmation(
        userId: String,
        donationId: String,
        message: String,
        expectedArrival: Timestamp,
        donationItemImageUrl: String,
        deliveryMethod: String,
        items: List<String>
    ) {
        viewModelScope.launch {
            donationRepository.saveDonationConfirmation(userId, donationId, message, expectedArrival, donationItemImageUrl, deliveryMethod, items)
                .collect {
                    _saveConfirmationStatus.postValue(it)
                }
        }
    }
}

class DonationSendViewModelFactory(
    private val donationRepository: DonationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonationSendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DonationSendViewModel(donationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}