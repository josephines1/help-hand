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

class DeleteViewModel(private val repository: DonationRepository): ViewModel() {
    private var _donations = MutableLiveData<List<Donations>>()
    val donations: LiveData<List<Donations>>
        get() = _donations

    fun delete(donation: Donations) {
        viewModelScope.launch {
            repository.delete(donation).collect {
                when (it) {
                    is Resource.Error -> {}
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        Log.d("DeleteViewModel: delete", "Success")
                    }
                }
            }
        }
    }
}