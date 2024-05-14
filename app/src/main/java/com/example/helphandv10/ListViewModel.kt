package com.example.helphandv10

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import kotlinx.coroutines.launch

class ListViewModel(private val repository: DonationRepository) : ViewModel() {
    private var _donations = MutableLiveData<List<Donations>>()
    val donations: LiveData<List<Donations>>
        get() = _donations

    fun getDonations() {
        viewModelScope.launch {
            repository.getDonation().collect {
                _donations.value = it
            }
        }
    }
}