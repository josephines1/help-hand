package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.example.helphandv10.adapter.ItemNeededAdapter
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import com.example.helphandv10.viewmodel.donation.DetailViewModel
import com.example.helphandv10.viewmodel.donation.HistoryViewModel
import com.example.helphandv10.viewmodel.donation.HistoryViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DonationDetailActivity : AppCompatActivity() {
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var itemNeededAdapter: ItemNeededAdapter
    private lateinit var auth: FirebaseAuth

    private val viewModel: DetailViewModel by viewModels()

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_donation_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val donationDetail: Donations? = intent.getParcelableExtra("DONATION")
        val donationId = donationDetail?.id

        donationId?.let { id ->
            lifecycleScope.launch {
                viewModel.getDonationById(id)
                observeDonationDetail()
            }
        }
    }

    private fun observeDonationDetail() {
        viewModel.donationDetailState.observe(this, Observer { resource ->
            Log.d("DONATION DETAIL RESOURCE", resource.toString())
            when (resource) {
                is Resource.Success -> {
                    val donationDetail = resource.data
                    Log.d("DONATION DETAIL", donationDetail.toString())
                    donationDetail?.let {
                        setupUI(it)
                        setupDonationButton(it)
                    }
                }
                is Resource.Error -> {
                    handleErrorMessage(resource.error)
                    Log.d("DONATION DETAIL", resource.error)
                }
                is Resource.Loading -> {
                    // Tampilkan loading UI jika diperlukan
                }
            }
        })
    }

    private fun setupUI(donationDetail: Donations) {
        // Lakukan penyesuaian UI dengan detail donasi yang diterima
        val title = donationDetail.title
        val imageURL = donationDetail.donationImageUrl
        val location = donationDetail.location
        val deadline = donationDetail.deadline
        val organizerIdPath = donationDetail.organizerId

        findViewById<TextView>(R.id.tv_detail_title).text = title
        findViewById<TextView>(R.id.tv_location).text = location
        findViewById<TextView>(R.id.tv_date).text = "Deadline: ${deadline?.let { formatTimestamp(it) }}"

        val tv_organizer = findViewById<TextView>(R.id.tv_organizer)
        val tv_phone = findViewById<TextView>(R.id.tv_phone)

        // Kueri pengguna dengan ID organizer
        val userRef = organizerIdPath.let {
            FirebaseFirestore.getInstance().document(it) // Gunakan path yang valid
        }
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Dokumen pengguna ditemukan
                    val organizerName = document.getString("username")
                    val organizerPhone = document.getString("phoneNumber")
                    tv_organizer.text = organizerName
                    tv_phone.text = organizerPhone
                } else {
                    // Dokumen pengguna tidak ditemukan atau tidak ada
                    tv_organizer.text = "..."
                    tv_phone.text = "..."
                }
            }
            .addOnFailureListener { exception ->
                // Penanganan kesalahan saat mengambil dokumen pengguna
                Log.e(TAG, "Error getting organizer document", exception)
            }

        Glide.with(this)
            .load(imageURL)
            .centerCrop()
            .into(findViewById(R.id.iv_detail_image))

        itemsRecyclerView = findViewById(R.id.rv_items_needed)
        itemNeededAdapter = donationDetail.itemsNeeded.let { ItemNeededAdapter(it) }!!
        itemsRecyclerView.adapter = itemNeededAdapter
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.ic_back).setOnClickListener {
            finish()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setupDonationButton(donationDetail: Donations) {
        val btnDonate = findViewById<TextView>(R.id.btn_donate)
        val deadline = donationDetail.deadline

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid
        val organizerId = donationDetail.organizerId.substringAfterLast("/")

        val tvTrackDonation = findViewById<TextView>(R.id.tv_track_donation)
        tvTrackDonation.visibility = View.GONE

        val firestoreDb = FirebaseFirestore.getInstance()
        val donationRepository = DonationRepository(firestoreDb)
        val factory = HistoryViewModelFactory(donationRepository)
        val donationViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        lifecycleScope.launch {
            donationViewModel.getDonationsByDonor().collect { donations ->
                val isDonationAlreadyMade = donations.any { it.id == donationDetail.id }

                if (isDonationAlreadyMade) {
                    // Menonaktifkan tombol donasi
                    btnDonate.text = "You Already Donated"
                    btnDonate.setTextColor(R.color.text)
                    btnDonate.setBackgroundResource(R.drawable.button_neutral_rounded_corner)

                    // Menampilkan text view untuk melacak donasi
                    tvTrackDonation.visibility = View.VISIBLE

                } else if (deadline != null && deadline < Timestamp.now()) {
                    // Jika deadline sudah lewat, atur teks tombol menjadi "Closed" dan nonaktifkan tombol
                    btnDonate.text = "Closed"
                    btnDonate.setTextColor(R.color.text)
                    btnDonate.setBackgroundResource(R.drawable.button_neutral_rounded_corner)

                } else if (currentUserId != organizerId) {
                    btnDonate.setOnClickListener {
                        startActivity(Intent(this@DonationDetailActivity, DonationSendActivity::class.java).apply {
                            putExtra("DONATION", donationDetail)
                        })
                        finish()
                    }
                } else {
                    btnDonate.text = "Manage Donation"

                    btnDonate.setOnClickListener {
                        startActivity(Intent(this@DonationDetailActivity, ManageDonationActivity::class.java).apply {
                            putExtra("DONATION", donationDetail)
                        })
                        finish()
                    }
                }
            }
        }
    }

    private fun handleErrorMessage(message: String?) {
        // Penanganan kesalahan saat mengambil detail donasi
        Toast.makeText(this@DonationDetailActivity, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
        // Mengonversi Timestamp ke LocalDateTime
        val localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        // Menggunakan DateTimeFormatter untuk memformat LocalDateTime
        return localDateTime.format(formatter)
    }
}