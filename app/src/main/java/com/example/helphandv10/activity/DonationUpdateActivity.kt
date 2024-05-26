package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.helphandv10.CreateFragment.Companion.IMAGE_PICK_CODE
import com.example.helphandv10.R
import com.example.helphandv10.model.Donations
import com.example.helphandv10.viewmodel.donation.UpdateViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class DonationUpdateActivity : AppCompatActivity() {

    private val updateViewModel: UpdateViewModel by viewModel()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_donation_update)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val iconBack = findViewById<ImageView>(R.id.ic_back)

        iconBack.setOnClickListener{
            finish()
        }

        val donation: Donations? = intent.getParcelableExtra("data")
        if (donation != null) {
            donation.id?.let { Log.d("ID ID ID ID: Update", it) }
        }

        val et_title = findViewById<EditText>(R.id.et_update_title)
        val et_date = findViewById<EditText>(R.id.et_update_date)
        val et_location = findViewById<EditText>(R.id.et_update_location)
        val et_items = findViewById<EditText>(R.id.et_update_items)
        val btn_update = findViewById<ConstraintLayout>(R.id.cl_btn_update)
        val btn_update_text = findViewById<TextView>(R.id.btn_update_text)
        val cl_upload_image = findViewById<ConstraintLayout>(R.id.cl_upload_image)
        val iv_preview = findViewById<ImageView>(R.id.iv_preview)

        // Set up Firebase Cloud Storage
        storageReference = FirebaseStorage.getInstance().reference

        // Set up image picker
        cl_upload_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        et_date.setOnClickListener {
            showDatePickerDialog(et_date)
        }

        // Set data awal contact ke dalam EditText
        et_title.setText(donation?.title)
        et_location.setText(donation?.location)

        val deadline = donation?.deadline
        deadline?.let {
            et_date.setText(it.toFormattedDate())
        }

        // Tampilkan daftar item
        val itemsString = donation?.itemsNeeded?.joinToString(", ") // Menggabungkan item dengan koma
        et_items.setText(itemsString)

        Glide.with(this)
            .load(donation?.donationImageUrl)
            .centerCrop()
            .into(iv_preview)

        if(donation?.donationImageUrl == null) {
            iv_preview.setImageResource(R.drawable.button_primary_light_rounded_corner)
        }

        btn_update.setOnClickListener {
            val currentDonation = donation
            val title = et_title.text.toString()
            val date = et_date.text.toString()
            val location = et_location.text.toString()
            val items = et_items.text.toString().split(",").map { it.trim() }

            if (title.isEmpty() || date.isEmpty() || items.any { it.isEmpty() } || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deadline = try {
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

            if (deadline == null) {
                Toast.makeText(this, "Invalid deadline format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (Timestamp(deadline) <= Timestamp.now()) {
                Toast.makeText(this, "Deadline cannot be dated before today.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // if image changed
            if (currentDonation != null) {
                btn_update_text.text = "Saving..."
                btn_update_text.setTextColor(R.color.text)
                btn_update.setBackgroundResource(R.drawable.button_neutral_rounded_corner)

                if (::imageUri.isInitialized) {

                    if (currentDonation.donationImageUrl != null) {
                        val oldImagePath = getFileNameFromUrl(currentDonation.donationImageUrl)
                        val oldImageRef = storageReference.child(oldImagePath)

                        Log.d("UpdateActivity", "Attempting to delete old image at path: $oldImagePath")

                        oldImageRef.delete().addOnSuccessListener {
                            Log.i("UpdateActivity", "Success deleting old image")
                        }.addOnFailureListener { e ->
                            // Handle any errors
                            Log.e("UpdateActivity", "Error deleting old image", e)
                        }
                    }

                    val imageRef = storageReference.child("donations/${UUID.randomUUID()}")
                    imageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                        // Get the download URL of the uploaded image
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()

                            // Save the contact data to Firestore
                            val donationInput = Donations(
                                id = donation.id,
                                title = title,
                                donationImageUrl = imageUrl,
                                location = location,
                                organizerId = "users/${FirebaseAuth.getInstance().currentUser?.uid ?: ""}",
                                deadline = Timestamp(deadline),
                                itemsNeeded = items,
                            )
                            updateViewModel.updateDonation(donationInput, imageUrl)

                            updateViewModel.donationUpdated.observe(this) {
                                if (it) {
                                    Toast.makeText(this, "Successfully saved data", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, ManageDonationActivity::class.java)
                                    intent.putExtra("DONATION", donationInput)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    }
                } else {
                    // Save the contact data to Firestore without an image
                    val donationInput = Donations(
                        id = donation.id,
                        title = title,
                        donationImageUrl = donation.donationImageUrl,
                        location = location,
                        organizerId = "users/${FirebaseAuth.getInstance().currentUser?.uid ?: ""}",
                        deadline = Timestamp(deadline),
                        itemsNeeded = items,
                    )
                    updateViewModel.updateDonation(donationInput, donation.donationImageUrl)

                    updateViewModel.donationUpdated.observe(this) {
                        if (it) {
                            Toast.makeText(this, "Successfully saved data", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ManageDonationActivity::class.java)
                            intent.putExtra("DONATION", donationInput)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            val imageIv = findViewById<ImageView>(R.id.iv_preview)
            imageIv?.scaleType = ImageView.ScaleType.CENTER_CROP
            imageIv.setImageURI(imageUri)

            val tv_upload_image = findViewById<TextView>(R.id.tv_upload_image)
            tv_upload_image?.setTextColor(255)
        }
    }

    fun getFileNameFromUrl(url: String): String {
        val decodedUrl = URLDecoder.decode(url, "UTF-8")
        val regex = Regex("""/o/(.*)\?alt=media""")
        val matchResult = regex.find(decodedUrl)
        return matchResult?.groupValues?.get(1) ?: throw IllegalArgumentException("Invalid URL: $url")
    }

    private fun showDatePickerDialog(et_date: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                et_date.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    fun Timestamp.toFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(this.toDate())
    }

    companion object {
        const val IMAGE_PICK_CODE = 1000
    }
}