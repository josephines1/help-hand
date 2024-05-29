package com.example.helphandv10.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.helphandv10.R
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.databinding.ActivityTrackDonationBinding
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TrackDonationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackDonationBinding
    private val viewModel: TrackDonationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.icBack.setOnClickListener {
            finish()
        }

        binding.backToHomeButton.setOnClickListener {
            // Handle back to home action
            finish()
        }

        // Observe the ViewModel
        lifecycleScope.launch {
            viewModel.donations.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator
                    }
                    is Resource.Success -> {
                        // Update UI with the donations
                        resource.data?.let { donations ->
                            if (donations.isNotEmpty()) {
                                val donation = donations[0] // For example, we only show the first donation
                                updateUI(donation)
                            }
                        }
                    }
                    is Resource.Error -> {
                        // Show error message
                    }
                }
            }
        }

        // Fetch donations
        viewModel.getDonationsByDonor()
    }

    private fun updateUI(donation: Donations) {
        // Load image using Picasso
        Picasso.get().load(donation.donationImageUrl).into(binding.donationImage1)
        binding.statusText.text = "Donation Sent"
        binding.estimatedDate.text = "Estimated Arrival Date: ${donation.deadline.toDate()}"
        binding.seeDetails.setOnClickListener {
            // Handle see details action
        }
    }
}
