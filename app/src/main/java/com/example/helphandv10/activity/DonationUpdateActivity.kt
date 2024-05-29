package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.helphandv10.adapter.NeedsAdapter
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

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val updateViewModel: UpdateViewModel by viewModel()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var needsAdapter: NeedsAdapter
    private lateinit var webViewMap: WebView

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_donation_update)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize WebView
        webViewMap = findViewById(R.id.webview_map)
        configureWebView()

        // Load Leaflet map HTML file from assets
        webViewMap.loadUrl("file:///android_asset/leaflet_map.html")

        // Menambahkan listener untuk mengatur scroll
        webViewMap.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Menonaktifkan scroll ketika user menyentuh WebView
                    webViewMap.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    // Mengaktifkan kembali scroll setelah user melepaskan sentuhan
                    webViewMap.requestDisallowInterceptTouchEvent(false)
                    // Panggil performClick untuk menangani performClick yang diharapkan oleh WebView
                    webViewMap.performClick()
                }
            }
            false
        }

        webViewMap.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Set latitude dan longitude ke nilai dari input field
                webViewMap.loadUrl("javascript:showLocationOnMap();")
            }
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
        val btn_update = findViewById<ConstraintLayout>(R.id.cl_btn_update)
        val btn_update_text = findViewById<TextView>(R.id.btn_update_text)
        val cl_upload_image = findViewById<ConstraintLayout>(R.id.cl_upload_image)
        val iv_preview = findViewById<ImageView>(R.id.iv_preview)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)

        val margin = (12 * resources.displayMetrics.density + 0.5f).toInt()
        val params = btn_update.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = margin
        btn_update.layoutParams = params

        needsAdapter = NeedsAdapter(mutableListOf())

        fun addNewInputField(needText: String = "", tag: Int) {
            val newItemView = LayoutInflater.from(this).inflate(R.layout.item_need, linearLayout, false)
            val needEditText = newItemView.findViewById<EditText>(R.id.et_needs)
            needEditText.setText(needText)
            needEditText.setTag(tag)

            newItemView.findViewById<ImageButton>(R.id.btnDelNeed).setOnClickListener {
                linearLayout.removeView(newItemView)
            }

            linearLayout.addView(newItemView)
        }

        // Initial need field
        val etInitialNeed = findViewById<EditText>(R.id.et_needs)
        etInitialNeed.setTag(0)

        // Add initial need data if available
        donation?.itemsNeeded?.firstOrNull()?.let {
            etInitialNeed.setText(it)
        }

        // Add any additional needs
        donation?.itemsNeeded?.drop(1)?.forEachIndexed { index, item ->
            addNewInputField(item, index + 1)
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
                    "Please fill in all item needs before adding a new one",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val newTag = linearLayout.childCount
            addNewInputField(tag = newTag)
        }

        // Set up Firebase Cloud Storage
        storageReference = FirebaseStorage.getInstance().reference

        // Set up image picker
        cl_upload_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Mendapatkan referensi EditText untuk latitude dan longitude
        val etLatitude = findViewById<EditText>(R.id.et_latitude)
        val etLongitude = findViewById<EditText>(R.id.et_longitude)

        val coordinate = donation?.coordinate

        if(coordinate != null) {
            val parts = coordinate.split(",")
            latitude = parts[0].trim().toDouble()
            longitude = parts[1].trim().toDouble()
        }

        // Set data awal coordinate ke dalam EditText
        etLatitude.setText(latitude.toString())
        etLongitude.setText(longitude.toString())

        // Menambahkan pendengar acara (event listener) ke EditText untuk latitude
        etLatitude.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak perlu melakukan apa pun sebelum teks berubah
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tidak perlu melakukan apa pun saat teks berubah
            }

            override fun afterTextChanged(s: Editable?) {
                // Panggil setSelectedLocation() dengan nilai latitude yang baru
                val latitude = s?.toString()?.toDoubleOrNull() ?: return // Mengonversi teks ke Double
                setSelectedLocation(latitude, longitude)
            }
        })

        // Menambahkan pendengar acara (event listener) ke EditText untuk longitude
        etLongitude.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak perlu melakukan apa pun sebelum teks berubah
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Tidak perlu melakukan apa pun saat teks berubah
            }

            override fun afterTextChanged(s: Editable?) {
                // Panggil setSelectedLocation() dengan nilai longitude yang baru
                val longitude = s?.toString()?.toDoubleOrNull() ?: return // Mengonversi teks ke Double
                setSelectedLocation(latitude, longitude)
            }
        })

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

        Glide.with(this)
            .load(donation?.donationImageUrl)
            .centerCrop()
            .into(iv_preview)

        if(donation?.donationImageUrl == null) {
            iv_preview.setImageResource(R.drawable.button_primary_light_rounded_corner)
        }

        fun collectDataFromLinearLayout(container: LinearLayout): List<String> {
            val inputData = mutableListOf<String>()
            for (i in 0 until container.childCount) {
                val view = container.getChildAt(i)
                if (view is LinearLayout) {
                    for (j in 0 until view.childCount) {
                        val innerView = view.getChildAt(j)
                        if (innerView is EditText) {
                            inputData.add(innerView.text.toString())
                        }
                    }
                }
                if (view is EditText) {
                    inputData.add(view.text.toString())
                }
            }
            return inputData
        }

        btn_update.setOnClickListener {
            val currentDonation = donation
            val title = et_title.text.toString()
            val date = et_date.text.toString()
            val location = et_location.text.toString()
            val latitude = etLatitude.text.toString()
            val longitude = etLongitude.text.toString()
            val coordinate = "$latitude, $longitude"
            val items = collectDataFromLinearLayout(linearLayout)

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
                                coordinate = coordinate,
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
                        coordinate = coordinate,
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

    private fun configureWebView() {
        val webSettings: WebSettings = webViewMap.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess = true

        webViewMap.webChromeClient = WebChromeClient()
        webViewMap.webViewClient = WebViewClient()

        // Tambahkan antarmuka JavaScript baru ke WebView
        webViewMap.addJavascriptInterface(WebAppInterface(), "Android")
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

    private fun setSelectedLocation(lat: Double, lng: Double) {
        latitude = lat
        longitude = lng

        // Memanggil fungsi JavaScript untuk menampilkan lokasi baru di peta
        webViewMap.post {
            webViewMap.loadUrl("javascript:showLocationOnMap();")
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun getLatitude(): Double {
            return latitude
        }

        @JavascriptInterface
        fun getLongitude(): Double {
            return longitude
        }
    }
}