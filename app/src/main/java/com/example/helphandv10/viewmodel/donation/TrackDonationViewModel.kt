package com.example.helphandv10.viewmodel.donation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.helphandv10.data.DonationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrackDonationViewModel(private val donationRepository: DonationRepository) : ViewModel() {
    constructor() : this(DonationRepository(FirebaseFirestore.getInstance()))
    private val _donationStatus = MutableLiveData<String>()
    val donationStatus: LiveData<String> get() = _donationStatus

    private val _donationDetails = MutableLiveData<Map<String, Any>>()
    val donationDetails: LiveData<Map<String, Any>> get() = _donationDetails

    fun checkDonationStatus(donationId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        donationRepository.getDonorConfirmation(donationId, userId)
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val sentConfirmation = documentSnapshot.get("sentConfirmation") as? Map<*, *>
                    val receivedConfirmation = documentSnapshot.get("receivedConfirmation") as? Map<*, *>

                    _donationDetails.value = mapOf(
                        "sentConfirmation" to (sentConfirmation ?: emptyMap<String, Any>()),
                        "receivedConfirmation" to (receivedConfirmation ?: emptyMap<String, Any>())
                    )

                    when {
                        sentConfirmation != null && receivedConfirmation != null -> {
                            _donationStatus.value = "received"
                        }
                        sentConfirmation != null -> {
                            _donationStatus.value = "sent"
                        }
                        else -> {
                            _donationStatus.value = "waiting"
                        }
                    }
                } else {
                    _donationStatus.value = "undefined"
                }
            }
            .addOnFailureListener {
                _donationStatus.value = "undefined"
            }
    }
}
