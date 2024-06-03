package com.example.helphandv10.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DonationReceivedDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_received_detail)

        // Initialize views
        val tvConfirmationDate: TextView = findViewById(R.id.tv_confirmation_date)
        val tvConfirmationMessage: TextView = findViewById(R.id.tv_confirmation_message_for_donatur)
        val ivDonationImage: ImageView = findViewById(R.id.iv_donation_image)
        val iconBack: ImageView = findViewById(R.id.ic_back)

        // Get data from intent
        val confirmationDate = intent.getStringExtra("confirmationDate")
        val confirmationMessage = intent.getStringExtra("confirmationMessage")
        val donationImageUrl = intent.getStringExtra("donationImageUrl")

        // Set data to views
        tvConfirmationDate.text = confirmationDate
        tvConfirmationMessage.text = confirmationMessage
        Glide.with(this)
            .load(donationImageUrl)
            .centerCrop()
            .into(ivDonationImage)

        // Set back button action
        iconBack.setOnClickListener {
            finish()
        }
    }

    private fun formatTimestamp(timestamp: Timestamp): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
        // Mengonversi Timestamp ke LocalDateTime
        val localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        // Menggunakan DateTimeFormatter untuk memformat LocalDateTime
        return localDateTime.format(formatter)
    }
}