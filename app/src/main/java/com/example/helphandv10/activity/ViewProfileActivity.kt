package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.example.helphandv10.databinding.ActivityViewProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityViewProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get current user from FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Find the back button by its ID
        val backButton: ImageView = findViewById(R.id.ic_back)

        // Set an onClickListener for the back button
        backButton.setOnClickListener {
            // Finish the current activity and go back to the previous activity
            finish()
        }

        // Set the user's display name, profile picture, email, and phone number
        currentUser?.let { user ->
            val uid = user.uid
            firestore.collection("users").document(uid)
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
                        getTotalDonations(uid)
                        getTotalOrganizedDonations(uid)

                    } else {
                        // User document does not exist, handle accordingly
                        binding.UserName.text = "Guest" // or any default value
                        binding.tvCreateEmail.text = "N/A"
                        binding.tvCreatePhone.text = "N/A"
                        binding.ivUserPhotoProfile.setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
                    }
                }
                .addOnFailureListener { exception ->
                    // Error occurred while retrieving user document, handle accordingly
                    binding.UserName.text = "Guest" // or any default value
                    binding.tvCreateEmail.text = "N/A"
                    binding.tvCreatePhone.text = "N/A"
                    binding.ivUserPhotoProfile.setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
                }
        } ?: run {
            // Current user is null, handle accordingly
            binding.UserName.text = "Guest" // or any default value
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
                // Handle any errors
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
                // Handle any errors
                binding.totalorganizer.text = "0"
            }
    }
}