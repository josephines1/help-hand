package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.helphandv10.adapter.NeedsAdapter
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import com.example.helphandv10.viewmodel.form.DonationSendViewModel
import com.example.helphandv10.viewmodel.form.DonationSendViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class DonationSendActivity : AppCompatActivity() {

    private val donationViewModel: DonationSendViewModel by viewModels {
        DonationSendViewModelFactory(DonationRepository(FirebaseFirestore.getInstance()))
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var needsAdapter: NeedsAdapter

    @SuppressLint("ResourceAsColor")
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
        val btnSubmitText: TextView = findViewById(R.id.btn_send_text)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val linearLayout_in = findViewById<LinearLayout>(R.id.linearLayout_in)

        needsAdapter = NeedsAdapter(mutableListOf())

        fun addNewInputField() {
            val newItemView = LayoutInflater.from(this).inflate(R.layout.item_need, linearLayout, false)

            newItemView.findViewById<ImageView>(R.id.btnDelNeed).setOnClickListener {
                linearLayout.removeView(newItemView)
            }

            linearLayout.addView(newItemView)
        }

        // Menambahkan onClickListener untuk tombol tambah Need
        val btnAddNeed = findViewById<ImageView>(R.id.btnAddNeed)
        btnAddNeed.setOnClickListener {
            // Check if any EditText is empty before adding a new field
            var allFieldsFilled = true
            for (i in 0 until linearLayout.childCount) {
                val view = linearLayout.getChildAt(i)
                if (view is LinearLayout) {
                    for (j in 0 until view.childCount) {
                        val innerView = view.getChildAt(j)
                        if (innerView is EditText && innerView.text.toString().isEmpty()) {
                            allFieldsFilled = false
                            break
                        }
                    }
                }
                if (view is EditText && view.text.toString().isEmpty()) {
                    allFieldsFilled = false
                    break
                }
            }

            if (!allFieldsFilled) {
                Toast.makeText(
                    this,
                    "Please fill in all items before adding a new one",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            addNewInputField()
        }

        etDeliveryDate.setOnClickListener {
            showDatePickerDialog(etDeliveryDate)
        }

        storageReference = FirebaseStorage.getInstance().reference
        cl_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        fun collectDataFromLinearLayout(container: LinearLayout): List<String> {
            val inputData = mutableListOf<String>()
            // Iterasi melalui setiap child dari LinearLayout container
            for (i in 0 until container.childCount) {
                val view = container.getChildAt(i)
                // Jika child merupakan LinearLayout, kita perlu memanggil fungsi ini secara rekursif
                if (view is LinearLayout) {
                    // Panggil fungsi rekursif untuk mengumpulkan data dari LinearLayout anak
                    val dataFromChildLayout = collectDataFromLinearLayout(view)
                    // Tambahkan semua data dari LinearLayout anak ke dalam inputData
                    inputData.addAll(dataFromChildLayout)
                }
                // Jika child merupakan EditText, tambahkan teksnya ke dalam inputData
                if (view is EditText) {
                    inputData.add(view.text.toString())
                }
            }
            return inputData
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
            val items = collectDataFromLinearLayout(linearLayout)

            if (message.isEmpty() || items.any { it.isEmpty() }) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (deliveryDate == null) {
                Toast.makeText(this, "Invalid deadline format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (Timestamp(deliveryDate) < Timestamp.now()) {
                Toast.makeText(this, "Delivery cannot be dated before today.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (donation != null) {
                if (Timestamp(deliveryDate) > donation.deadline!!) {
                    Toast.makeText(this, "No delivery after donation deadline: ${formatTimestamp(donation.deadline)}", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (::imageUri.isInitialized) {
                val imageRef = storageReference.child("send-donation/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val donationItemImageUrl = uri.toString()
                        Log.d("DonationSendActivity", "Image URL: $donationItemImageUrl")
                        saveDonation(message, Timestamp(deliveryDate), deliveryMethod, donationItemImageUrl, donation, items)
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
                    Toast.makeText(this, "Saving data...", Toast.LENGTH_SHORT).show()
                    btnSubmitText.text = "Saving..."
                    btnSubmitText.setTextColor(R.color.text)
                    btnSubmit.setBackgroundResource(R.drawable.button_neutral_rounded_corner)
                }
                is Resource.Success -> {
                    Toast.makeText(this, "Confirmation submitted successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SuccessRequestSendDonation::class.java)
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

    private fun saveDonation(message: String, deliveryDate: Timestamp, deliveryMethod: String, donationItemImageUrl: String, donation: Donations?, items: List<String>) {
        val donationId = donation?.id
        val userId = auth.currentUser?.uid

        Log.d("DonationSendActivity", "Donation ID: $donationId")
        Log.d("DonationSendActivity", "User ID: $userId")
        Log.d("DonationSendActivity", "Donation Image URL: $donationItemImageUrl")

        if (userId != null && donationId != null) {
            donationViewModel.saveDonationConfirmation(userId, donationId, message, deliveryDate, donationItemImageUrl, deliveryMethod, items)
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

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
        // Mengonversi Timestamp ke LocalDateTime
        val localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        // Menggunakan DateTimeFormatter untuk memformat LocalDateTime
        return localDateTime.format(formatter)
    }

    companion object {
        const val IMAGE_PICK_CODE = 1000
    }
}
