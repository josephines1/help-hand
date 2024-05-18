package com.example.helphandv10.viewmodel.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.data.UsersRepository
import com.example.helphandv10.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManageDonorsViewModel(
    private val repository: DonationRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _donors = MutableStateFlow<Resource<List<DocumentSnapshot>>>(Resource.Loading)
    val donors: StateFlow<Resource<List<DocumentSnapshot>>> = _donors

    private val _user = MutableStateFlow<Resource<DocumentSnapshot>?>(null)
    val user: StateFlow<Resource<DocumentSnapshot>?> = _user

    fun fetchDonors(donationId: String) {
        viewModelScope.launch {
            repository.getDonorsForDonation(donationId).collect { result ->
                _donors.value = result
            }
        }
    }

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            usersRepository.getUserById(userId).collect { result ->
                _user.value = result
            }
        }
    }
}

class ManageDonorsViewModelFactory(
    private val donationRepository: DonationRepository,
    private val usersRepository: UsersRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageDonorsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageDonorsViewModel(donationRepository, usersRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}