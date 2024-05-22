package com.example.helphandv10.viewmodel.form

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.model.Donor
import com.example.helphandv10.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class DonationReceiveViewModel(private val repository: DonationRepository) : ViewModel() {

    constructor() : this(DonationRepository(FirebaseFirestore.getInstance()))

    private val _donation = MutableLiveData<Donations>()
    val donation: LiveData<Donations> get() = _donation

    private val _updateStatus = MutableLiveData<Resource<Unit>>()
    val updateStatus: LiveData<Resource<Unit>> = _updateStatus

    private val _donor = MutableLiveData<DocumentSnapshot>()
    val donor: LiveData<DocumentSnapshot> get() = _donor

    fun fetchDonor(donationId: String, donorId: String) {
        viewModelScope.launch {
            repository.getDonorById(donationId, donorId, { donor ->
                _donor.value = donor
            }, { exception ->
                Log.d("DONATION RECEIVE MODEL", exception.toString())
            })
        }
    }

    fun updateDonation(donationId: String, donorId: String, updatedDonor: Donor) {
        viewModelScope.launch {
            repository.updateDonation(donationId, donorId, updatedDonor)
                .collect {
                    _updateStatus.postValue(it)
                }
        }
    }
}
