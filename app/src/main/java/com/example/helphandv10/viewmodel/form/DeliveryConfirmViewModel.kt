package com.example.helphandv10.viewmodel.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.DonationConfirmation
import com.example.helphandv10.utils.Resource
import kotlinx.coroutines.launch

class DeliveryConfirmViewModel(private val repository: DonationRepository) : ViewModel() {

    private val _confirmationStatus = MutableLiveData<Resource<Boolean>>()
    val confirmationStatus: LiveData<Resource<Boolean>> = _confirmationStatus

    fun submitConfirmation(confirmation: DonationConfirmation) {
        _confirmationStatus.value = Resource.Loading()
        viewModelScope.launch {
            try {
                repository.submitConfirmation(confirmation)
                _confirmationStatus.value = Resource.Success(true)
            } catch (e: Exception) {
                _confirmationStatus.value = Resource.Error(e.message ?: "An error occurred")
            }
        }
    }
}

class DeliveryConfirmViewModelFactory(private val repository: DonationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryConfirmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryConfirmViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
