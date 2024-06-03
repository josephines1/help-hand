package com.example.helphandv10.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.helphandv10.databinding.ActivityDonationSendDetailBinding

class DonationSendDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonationSendDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationSendDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val donorName = intent.getStringExtra("donorName")
        val messageFromDonor = intent.getStringExtra("messageFromDonor")
        val estimatedArrivalDate = intent.getStringExtra("estimatedArrivalDate")
        val donationImageUrl = intent.getStringExtra("donationImageUrl")
        val items = intent.getStringExtra("items")
        val deliveryMethod = intent.getStringExtra("deliveryMethod")

        binding.tvDonatur.text = donorName
        binding.tvMessageFromDonatur.text = messageFromDonor
        binding.tvArrivalDate.text = estimatedArrivalDate
        Glide.with(this).load(donationImageUrl).into(binding.ivDonationImage)
        binding.tvItems.text = items
        binding.tvDeliveryMethod.text = deliveryMethod

        binding.icBack.setOnClickListener {
            finish()
        }
    }
}
