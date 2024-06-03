package com.example.helphandv10.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.example.helphandv10.databinding.ActivityViewProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityViewProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // Get donorId from the Intent
        val donorId = intent.getStringExtra("donorId")

        // Find the back button by its ID
        val backButton: ImageView = findViewById(R.id.ic_back)

        // Set an onClickListener for the back button
        backButton.setOnClickListener {
            finish()
        }

        // Load the donor's profile information using the donorId
        donorId?.let { id ->
            firestore.collection("users").document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        val email = document.getString("email")
                        val phone = document.getString("phoneNumber")
                        val photo = document.getString("photoProfileURL")

                        binding.UserName.text = username
                        binding.tvCreateEmail.text = email
                        binding.tvCreatePhone.text = phone

                        if (photo != null && photo != "-") {
                            Glide.with(this)
                                .load(photo)
                                .centerCrop()
                                .into(binding.ivUserPhotoProfile)
                        } else {
                            binding.ivUserPhotoProfile.setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
                        }

                        // Get total donations and total organized donations
                        getTotalDonations(id)
                        getTotalOrganizedDonations(id)

                    } else {
                        binding.UserName.text = "Guest"
                        binding.tvCreateEmail.text = "N/A"
                        binding.tvCreatePhone.text = "N/A"
                        binding.ivUserPhotoProfile.setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
                    }
                }
                .addOnFailureListener { exception ->
                    binding.UserName.text = "Guest"
                    binding.tvCreateEmail.text = "N/A"
                    binding.tvCreatePhone.text = "N/A"
                    binding.ivUserPhotoProfile.setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
                }
        } ?: run {
            binding.UserName.text = "Guest"
            binding.tvCreateEmail.text = "N/A"
            binding.tvCreatePhone.text = "N/A"
            binding.ivUserPhotoProfile.setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
        }
    }

    private fun getTotalDonations(userId: String) {
        firestore.collectionGroup("Donors")
            .whereEqualTo("UserID", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val totalDonations = querySnapshot.size()
                binding.totaldonor.text = totalDonations.toString()
            }
            .addOnFailureListener { exception ->
                binding.totaldonor.text = "0"
            }
    }

    private fun getTotalOrganizedDonations(userId: String) {
        firestore.collection("Donations")
            .whereEqualTo("organizerId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val totalOrganizedDonations = querySnapshot.size()
                binding.totalorganizer.text = totalOrganizedDonations.toString()
            }
            .addOnFailureListener { exception ->
                binding.totalorganizer.text = "0"
            }
    }
}