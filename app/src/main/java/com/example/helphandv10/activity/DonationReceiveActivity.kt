package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.helphandv10.CreateFragment
import com.example.helphandv10.R
import com.example.helphandv10.databinding.ActivityDonationReceiveBinding
import com.example.helphandv10.model.Donor
import com.example.helphandv10.model.ReceivedConfirmation
import com.example.helphandv10.model.SentConfirmation
import com.example.helphandv10.utils.Resource
import com.example.helphandv10.viewmodel.form.DonationReceiveViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class DonationReceiveActivity : AppCompatActivity() {

    private val donationReceiveViewModel: DonationReceiveViewModel by viewModels()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_donation_receive)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvDonatur: TextView = findViewById(R.id.tv_donatur)
        val tvMessageFromDonatur: TextView = findViewById(R.id.tv_message_from_donatur)
        val tvItems: TextView = findViewById(R.id.tv_items)
        val tvArrivalDate: TextView = findViewById(R.id.tv_arrival_date)
        val tvDeliveryMethod: TextView = findViewById(R.id.tv_delivery_method)
        val ivDonationImage: ImageView = findViewById(R.id.iv_donation_image)

        val etMessage: EditText = findViewById(R.id.et_confirm_message)
        val cl_image: ConstraintLayout = findViewById(R.id.cl_image)
        val btnSubmit: ConstraintLayout = findViewById(R.id.cl_btn_submit)
        val btnSubmitText: TextView = findViewById(R.id.btn_send_text)

        val donationId = intent.getStringExtra("DONATION_ID") ?: return
        val donorId = intent.getStringExtra("DONOR_ID") ?: return
        val donorName = intent.getStringExtra("DONOR_NAME") ?: return
        Log.d("DONATION RECEIVE ACTIVITY", donationId)

        tvDonatur.text = donorName

        storageReference = FirebaseStorage.getInstance().reference

        cl_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Mengembalikan result berupa donor
        donationReceiveViewModel.fetchDonor(donationId, donorId)

        donationReceiveViewModel.donor.observe(this, Observer { donor ->
            Log.e("SENT CONFIRMATION", donor.toString())
            val sentConfirmation = donor["sentConfirmation"] as? Map<*, *>

            if (sentConfirmation != null) {
                val message = sentConfirmation["message"] as? String
                val imageURL = sentConfirmation["donationItemImageUrl"] as? String
                val expectedArrival = sentConfirmation["expectedArrival"] as? Timestamp
                val items = sentConfirmation["items"] as? List<*>
                val shippingMethod = sentConfirmation["shippingMethod"] as? String

                // Menampilkan nilai-nilai tersebut ke dalam layout
                tvMessageFromDonatur.text = message
                tvArrivalDate.text = expectedArrival?.let { formatTimestamp(it) }
                Glide.with(this)
                    .load(imageURL)
                    .centerCrop()
                    .into(ivDonationImage)
                tvItems.text = items?.joinToString(", ")
                tvDeliveryMethod.text = shippingMethod
            } else {
                Log.d("DonationReceiveActivity", "No sentConfirmation found for this donor")
            }
        })

        btnSubmit.setOnClickListener {
            val message = etMessage.text.toString().trim()
            val confirmationDate = Timestamp.now()

            if (message.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (::imageUri.isInitialized) {
                val imageRef = storageReference.child("receive-donation/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val receivedProofImageUrl = uri.toString()

                        donationReceiveViewModel.donor.observe(this, Observer { donor ->
                            Log.e("SENT CONFIRMATION", donor.toString())
                            val sentConfirmation = donor["sentConfirmation"] as? Map<*, *>

                            if (sentConfirmation != null) {
                                val updatedDonor = Donor(
                                    sentConfirmation = SentConfirmation(
                                        sentConfirmation["message"] as String,
                                        sentConfirmation["expectedArrival"] as Timestamp,
                                        sentConfirmation["donationItemImageUrl"] as String,
                                        sentConfirmation["shippingMethod"] as String,
                                        sentConfirmation["items"] as List<String>,
                                    ),
                                    receivedConfirmation = ReceivedConfirmation(
                                        confirmationDate = confirmationDate,
                                        message = message,
                                        receivedProofImageUrl = receivedProofImageUrl
                                    )
                                )

                                // update data
                                donationReceiveViewModel.updateDonation(donationId, donorId, updatedDonor)
                            } else {
                                Log.d("DonationReceiveActivity", "No sentConfirmation found for this donor")
                            }
                        })
                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                        Log.d("DonationSendActivity", "Failed to get image URL")
                    }

                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    Log.d("DonationSendActivity", "Failed to upload image")
                }

            } else {
                Toast.makeText(this, "Image Required", Toast.LENGTH_SHORT).show()
                Log.d("DonationSendActivity", "Image Required")
            }
        }

        donationReceiveViewModel.updateStatus.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading indicator
                    Toast.makeText(this, "Saving data...", Toast.LENGTH_SHORT).show()
                    btnSubmitText.text = "Saving..."
                    btnSubmitText.setTextColor(R.color.text)
                    btnSubmit.setBackgroundResource(R.drawable.button_neutral_rounded_corner)
                }
                is Resource.Success -> {
                    Toast.makeText(this, "Confirmation submitted successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ManageDonorsActivity::class.java)
                    intent.putExtra("DONATION_ID", donationId)
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(this, "Something gone wrong", Toast.LENGTH_SHORT).show()
                    Log.d("DonationSendActivity", "Error: ${resource.error}")
                }
            }
        })

        val iconBack = findViewById<ImageView>(R.id.ic_back)

        iconBack.setOnClickListener{
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CreateFragment.IMAGE_PICK_CODE) {
            data?.data?.let {
                imageUri = it
                val imageIv = findViewById<ImageView>(R.id.iv_preview)
                imageIv?.scaleType = ImageView.ScaleType.CENTER_CROP
                imageIv?.setImageURI(imageUri)

                val tv_upload_image = findViewById<TextView>(R.id.tv_upload_image)
                tv_upload_image?.visibility = View.GONE
            }
        }
    }

    companion object {
        const val IMAGE_PICK_CODE = 1000
    }

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
        // Mengonversi Timestamp ke LocalDateTime
        val localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        // Menggunakan DateTimeFormatter untuk memformat LocalDateTime
        return localDateTime.format(formatter)
    }
}