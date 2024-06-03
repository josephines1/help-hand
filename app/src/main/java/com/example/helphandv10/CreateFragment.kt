package com.example.helphandv10

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.helphandv10.activity.EditProfileActivity
import com.example.helphandv10.activity.MainActivity
import com.example.helphandv10.activity.SuccessCreateDonation
import com.example.helphandv10.adapter.NeedsAdapter
import com.example.helphandv10.model.Donations
import com.example.helphandv10.viewmodel.donation.AddViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar
import java.util.UUID


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val addViewModel: AddViewModel by viewModel()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var needsAdapter: NeedsAdapter
    private lateinit var webViewMap: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create, container, false)

        webViewMap = view.findViewById(R.id.webview_map)

        configureWebView()

        // Set nilai default latitude dan longitude ke peta
        webViewMap.loadUrl("javascript:setLocationOnMap(0, 0);")

        return view
    }

    interface PhoneNumberCheckCallback {
        fun onCheckComplete(isRegistered: Boolean)
    }

    private fun isPhoneNumberRegistered(callback: PhoneNumberCheckCallback) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        val userRef = userId?.let {
            FirebaseFirestore.getInstance().collection("users").document(it)
        }

        userRef?.get()
            ?.addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val phoneNumber = document.getString("phoneNumber")
                    val isValidPhoneNumber = !phoneNumber.isNullOrEmpty() && phoneNumber != "-"
                    callback.onCheckComplete(isValidPhoneNumber)
                } else {
                    callback.onCheckComplete(false)
                }
            }
            ?.addOnFailureListener { exception ->
                // Handle the error
                Log.e("TAG", "Error getting user document", exception)
                callback.onCheckComplete(false)
            }
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun configureWebView() {
        val webSettings: WebSettings = webViewMap.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess = true

        webViewMap.webChromeClient = WebChromeClient()
        webViewMap.webViewClient = WebViewClient()

        // Enable JavaScript to call Android functions
        webViewMap.addJavascriptInterface(WebAppInterface(), "Android")

        // Load the Leaflet map HTML file from assets
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

    private fun replaceFragment(fragment: Fragment, itemId: Int) {
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.f_create, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        // Mengubah item terpilih di BottomNavigationView
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_bottom)
        bottomNav.selectedItemId = itemId
    }

    private fun showPhoneNumberDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_complete_profile, null)

        val positiveButton = dialogView.findViewById<ConstraintLayout>(R.id.cl_btn_positive)
        val negativeButton = dialogView.findViewById<ConstraintLayout>(R.id.cl_btn_negative)

        builder.apply {
            setView(dialogView)
        }

        val dialog = builder.create()

        positiveButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if the phone number is registered
        isPhoneNumberRegistered(object : PhoneNumberCheckCallback {
            override fun onCheckComplete(isRegistered: Boolean) {
                if (!isRegistered) {
                    // Show the dialog and return to prevent further initialization
                    showPhoneNumberDialog()
                    return
                } else {
                    // Initialize your view components if the phone number is registered
                    initializeViewComponents(view)
                }
            }
        })
    }

    @SuppressLint("ResourceAsColor")
    private fun initializeViewComponents(view: View) {
        val et_title = view.findViewById<EditText>(R.id.et_create_title)
        val et_date = view.findViewById<EditText>(R.id.et_create_date)
        val et_location = view.findViewById<EditText>(R.id.et_create_address)
        val cl_image = view.findViewById<ConstraintLayout>(R.id.cl_create_image)
        val btn_create = view.findViewById<ConstraintLayout>(R.id.cl_btn_create)
        val btn_create_text = view.findViewById<TextView>(R.id.btn_create_text)
        val linearLayout = view.findViewById<LinearLayout>(R.id.linearLayout)
        val linearLayout_in = view.findViewById<LinearLayout>(R.id.linearLayout_in)

        val margin = (12 * resources.displayMetrics.density + 0.5f).toInt()

        val params = btn_create.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = margin
        btn_create.layoutParams = params

        needsAdapter = NeedsAdapter(mutableListOf())

        fun addNewInputField() {
            val newItemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_need, linearLayout, false)

            newItemView.findViewById<ImageView>(R.id.btnDelNeed).setOnClickListener {
                linearLayout.removeView(newItemView)
            }

            linearLayout.addView(newItemView)
        }

        // Menambahkan onClickListener untuk tombol tambah Need
        val btnAddNeed = view.findViewById<ImageView>(R.id.btnAddNeed)
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
                    context,
                    "Please fill in all item needs before adding a new one",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            addNewInputField()
        }

        // Mendapatkan referensi EditText untuk latitude dan longitude
        val etLatitude = view.findViewById<EditText>(R.id.et_latitude)
        val etLongitude = view.findViewById<EditText>(R.id.et_longitude)

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

        btn_create.setOnClickListener {
            val title = et_title.text.toString()
            val date = et_date.text.toString()
            val location = et_location.text.toString()
            val latitude = etLatitude.text.toString()
            val longitude = etLongitude.text.toString()
            val coordinate = "$latitude, $longitude"
            val needs = collectDataFromLinearLayout(linearLayout)
            Log.d("NEEDS INPUT", needs.toString())

            if (title.isEmpty() || date.isEmpty() || needs.any { it.isEmpty() } || location.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Invalid deadline format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (Timestamp(deadline) < Timestamp.now()) {
                Toast.makeText(context, "Deadline cannot be dated before today.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (::imageUri.isInitialized) {
                btn_create_text.text = "Saving..."
                btn_create_text.setTextColor(R.color.text)
                btn_create.setBackgroundResource(R.drawable.button_neutral_rounded_corner)

                val imageRef = storageReference.child("donations/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val donationId = UUID.randomUUID().toString()
                        val donation = Donations(
                            id = donationId,
                            title = title,
                            donationImageUrl = imageUrl,
                            location = location,
                            coordinate = coordinate,
                            organizerId = "users/${FirebaseAuth.getInstance().currentUser?.uid ?: ""}",
                            deadline = Timestamp(deadline),
                            itemsNeeded = needs,
                            donors = mapOf()
                        )
                        addViewModel.addDonation(donation)

                        addViewModel.donationAdded.observe(viewLifecycleOwner) {
                            if (it) {
                                Toast.makeText(context, "Successfully saved data", Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireContext(), SuccessCreateDonation::class.java)
                                intent.putExtra("DONATION", donation)
                                if (donation != null) {
                                    donation.id?.let { Log.d("ID ID ID ID: FROM CREATE", it) }
                                }
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Image Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        val iconBack = view.findViewById<ImageView>(R.id.ic_back)

        iconBack.setOnClickListener{
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showDatePickerDialog(et_date: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        context?.let {
            DatePickerDialog(
                it,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    et_date.setText(selectedDate)
                },
                year,
                month,
                day
            )
        }?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let {
                imageUri = it
                val imageIv = view?.findViewById<ImageView>(R.id.iv_preview)
                imageIv?.scaleType = ImageView.ScaleType.CENTER_CROP
                imageIv?.setImageURI(imageUri)

                val tv_upload_image = view?.findViewById<TextView>(R.id.tv_upload_image)
                tv_upload_image?.visibility = View.GONE
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val IMAGE_PICK_CODE = 1000
    }
}