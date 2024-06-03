package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.helphandv10.databinding.ActivityDonationSendDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class DonationSendDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationSendDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationSendDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val donorId = intent.getStringExtra("donorId")
        val messageFromDonor = intent.getStringExtra("messageFromDonor")
        val estimatedArrivalDate = intent.getStringExtra("estimatedArrivalDate")
        val donationImageUrl = intent.getStringExtra("donationImageUrl")
        val items = intent.getStringExtra("items")
        val deliveryMethod = intent.getStringExtra("deliveryMethod")

        val firestore = FirebaseFirestore.getInstance()
        var donorName = "..."

        if (donorId != null) {
            firestore.collection("users").document(donorId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        donorName = document.getString("username").toString()
                        binding.tvDonatur.text = donorName
                    } else {
                        binding.tvDonatur.text = "..."
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DONATION SEND DETAIL ACTIVITY", e.toString())
                }
        }

        binding.tvMessageFromDonatur.text = messageFromDonor
        binding.tvArrivalDate.text = estimatedArrivalDate
        Glide.with(this).load(donationImageUrl).centerCrop().into(binding.ivDonationImage)
        binding.tvItems.text = items
        binding.tvDeliveryMethod.text = deliveryMethod

        binding.icBack.setOnClickListener {
            finish()
        }

        // Set OnClickListener to tv_donatur
        binding.tvDonatur.setOnClickListener {
            val intent = Intent(this, ViewProfileActivity::class.java)
            intent.putExtra("donorId", donorId)
            startActivity(intent)
        }
    }
}
