package com.example.helphandv10.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.helphandv10.CreateFragment
import com.example.helphandv10.R
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.DonationConfirmation
import com.example.helphandv10.model.Donations
import com.example.helphandv10.model.DonorConfirmation
import com.example.helphandv10.utils.Resource
import com.example.helphandv10.viewmodel.form.DonationSendViewModel
import com.example.helphandv10.viewmodel.form.DonationSendViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar
import java.util.UUID

class DonationSendActivity : AppCompatActivity() {

    private val donationViewModel: DonationSendViewModel by viewModels {
        DonationSendViewModelFactory(DonationRepository(FirebaseFirestore.getInstance()))
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_donation_send)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val donation: Donations? = intent.getParcelableExtra("DONATION")

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            Log.d("DonationSendActivity", "User not logged in")
        } else {
            Log.d("DonationSendActivity", "User logged in: ${user.uid}")
        }

        val etMessage: EditText = findViewById(R.id.et_send_message)
        val etDeliveryDate: EditText = findViewById(R.id.et_send_date)
        val ivPreview: ImageView = findViewById(R.id.iv_preview)
        val cl_image: ConstraintLayout = findViewById(R.id.cl_image)
        val rgDeliveryMethod: RadioGroup = findViewById(R.id.rg_send_delivery_method)
        val rbDropOff: RadioButton = findViewById(R.id.rb_send_drop_off)
        val rbCourier: RadioButton = findViewById(R.id.rb_send_courier)
        val btnSubmit: ConstraintLayout = findViewById(R.id.cl_btn_submit)

        storageReference = FirebaseStorage.getInstance().reference

        cl_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnSubmit.setOnClickListener {
            val message = etMessage.text.toString().trim()
            val date = etDeliveryDate.text.toString()
            val deliveryDate = try {
                val dateParts = date.split("-")
                val year = dateParts[0].toInt()
                val month = dateParts[1].toInt()
                val day = dateParts[2].toInt()
                Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }.time
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val deliveryMethod = if (rbDropOff.isChecked) "Drop Off" else "Courier"

            if (message.isEmpty() || deliveryDate == null) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (::imageUri.isInitialized) {
                val imageRef = storageReference.child("send-donation/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val donationItemImageUrl = uri.toString()
                        Log.d("DonationSendActivity", "Image URL: $donationItemImageUrl")
                        saveDonation(message, Timestamp(deliveryDate), deliveryMethod, donationItemImageUrl, donation)
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

        donationViewModel.saveConfirmationStatus.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading indicator
                }
                is Resource.Success -> {
                    Toast.makeText(this, "Confirmation saved successfully", Toast.LENGTH_SHORT).show()
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

    private fun saveDonation(message: String, deliveryDate: Timestamp, deliveryMethod: String, donationItemImageUrl: String, donation: Donations?) {
        val donationId = donation?.id
        val userId = auth.currentUser?.uid

        Log.d("DonationSendActivity", "Donation ID: $donationId")
        Log.d("DonationSendActivity", "User ID: $userId")
        Log.d("DonationSendActivity", "Donation Image URL: $donationItemImageUrl")

        if (userId != null && donationId != null) {
            donationViewModel.saveDonationConfirmation(userId, donationId, message, deliveryDate, donationItemImageUrl, deliveryMethod)
        } else {
            Log.d("DonationSendActivity", "Failed to save confirmation: userId or donationId is null")
            Toast.makeText(this, "Failed to save confirmation", Toast.LENGTH_SHORT).show()
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
}
