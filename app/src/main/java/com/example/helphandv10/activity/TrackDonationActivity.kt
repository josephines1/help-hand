package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.example.helphandv10.viewmodel.donation.TrackDonationViewModel
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TrackDonationActivity : AppCompatActivity() {
    private lateinit var donationSentContainer: RelativeLayout
    private lateinit var donationWaitContainer: RelativeLayout
    private lateinit var donationReceivedContainer: RelativeLayout
    private val trackDonationViewModel: TrackDonationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_donation)

        donationSentContainer = findViewById(R.id.donationSentContainer)
        donationWaitContainer = findViewById(R.id.donationWaitContainer)
        donationReceivedContainer = findViewById(R.id.donationReceivedContainer)

        // Set back button functionality
        findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.backToHomeButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Observe donation status and update UI accordingly
        trackDonationViewModel.donationStatus.observe(this) { status ->
            Log.e("STATUS", status)
            when (status) {
                "sent" -> {
                    donationWaitContainer.visibility = View.VISIBLE
                    donationReceivedContainer.visibility = View.GONE
                    // Load details for sent donation
                    loadDonationDetails()
                }
                "received" -> {
                    donationWaitContainer.visibility = View.GONE
                    donationReceivedContainer.visibility = View.VISIBLE
                    // Load details for received donation
                    loadDonationDetails()
                }
                else -> {
                    // Handle case where no relevant status is found
                    finish()
                }
            }
        }

        // Load donation status
        loadDonationStatus()
    }

    private fun loadDonationStatus() {
        // Retrieve donationId passed from the previous activity
        val donationId = intent.getStringExtra("donationId")
        if (donationId != null) {
            trackDonationViewModel.checkDonationStatus(donationId)
        } else {
            // Handle error: donationId not found
            finish()
        }
    }

    private fun loadDonationDetails() {
        trackDonationViewModel.donationDetails.observe(this) { details ->
            details?.let {
                val sentConfirmation = it["sentConfirmation"] as? Map<*, *>
                val receivedConfirmation = it["receivedConfirmation"] as? Map<*, *>

                // Set status text
                if (receivedConfirmation != null && receivedConfirmation.isNotEmpty() && receivedConfirmation.any { it.value != null }) {
                        findViewById<TextView>(R.id.statusText_received).text = "Donation Received"
                }
                findViewById<TextView>(R.id.statusText).text = "Donation Sent"

                // Set date
                if (receivedConfirmation != null && receivedConfirmation.isNotEmpty() && receivedConfirmation.any { it.value != null }) {
                    val receivedDate = receivedConfirmation["confirmationDate"]
                    findViewById<TextView>(R.id.receivedDate).text = "Received on ${formatTimestamp(receivedDate as Timestamp)}"
                }
                val estimatedDate = sentConfirmation?.get("expectedArrival")
                findViewById<TextView>(R.id.estimatedDate).text = "Estimated to arrive on ${formatTimestamp(estimatedDate as Timestamp)}"

                // load image
                if (receivedConfirmation != null && receivedConfirmation.isNotEmpty() && receivedConfirmation.any { it.value != null }) {
                    val imageReceived = receivedConfirmation.get("receivedProofImageUrl") as? String
                    Glide.with(this)
                        .load(imageReceived)
                        .centerCrop()
                        .into(findViewById(R.id.donationImage3))
                }
                val imageUrl = sentConfirmation.get("donationItemImageUrl") as? String
                Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(findViewById(R.id.donationImage1))

                // Setup see details button for sent donation
                findViewById<View>(R.id.seeDetails).setOnClickListener {
                    val intentToDetails = Intent(this, DonationSendDetailActivity::class.java)
                    intentToDetails.putExtra("donorId", trackDonationViewModel.documentId)
                    intentToDetails.putExtra("messageFromDonor", sentConfirmation["message"] as? String)
                    intentToDetails.putExtra("estimatedArrivalDate",
                        estimatedDate.let { formatTimestamp(it) })
                    intentToDetails.putExtra("donationImageUrl", imageUrl)
                    intentToDetails.putExtra("items", (sentConfirmation?.get("items") as? List<*>)?.joinToString(", "))
                    intentToDetails.putExtra("deliveryMethod", sentConfirmation["shippingMethod"] as? String)

                    startActivity(intentToDetails)
                }

                // Setup see details button for received donation
                findViewById<View>(R.id.seeDetails_received).setOnClickListener {
                    val intent = Intent(this, DonationReceivedDetailActivity::class.java)
                    intent.putExtra("confirmationDate", receivedConfirmation?.get("confirmationDate")?.let { formatTimestamp(it as Timestamp) })
                    intent.putExtra("confirmationMessage", receivedConfirmation?.get("message") as? String)
                    intent.putExtra("donationImageUrl", receivedConfirmation?.get("receivedProofImageUrl") as? String)
                    startActivity(intent)
                }
            }
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
