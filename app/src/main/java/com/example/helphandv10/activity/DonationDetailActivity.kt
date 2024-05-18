package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.example.helphandv10.adapter.ItemNeededAdapter
import com.example.helphandv10.model.Donations
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DonationDetailActivity : AppCompatActivity() {
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var itemNeededAdapter: ItemNeededAdapter

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

        val title = donationDetail?.title
        val imageURL = donationDetail?.donationImageUrl
        val itemNeeded = donationDetail?.itemsNeeded
        val organizerIdPath = donationDetail?.organizerId
        val location = donationDetail?.location

        val tv_title = findViewById<TextView>(R.id.tv_detail_title)
        val iv_image = findViewById<ImageView>(R.id.iv_detail_image)
        val tv_organizer = findViewById<TextView>(R.id.tv_organizer)
        val tv_location = findViewById<TextView>(R.id.tv_location)
        val tv_phone = findViewById<TextView>(R.id.tv_phone)
        val tv_date = findViewById<TextView>(R.id.tv_date)

        tv_title.text = title
        tv_location.text = location
        tv_date.text = "Deadline: ${donationDetail?.deadline?.let { formatTimestamp(it) }}"

        // Log the organizerIdPath for debugging
        Log.d(TAG, "Organizer ID Path: $organizerIdPath")

        // Kueri pengguna dengan ID pengatur acara
        val userRef = organizerIdPath?.let {
            FirebaseFirestore.getInstance().document(it) // Gunakan path yang valid
        }
        userRef?.get()
            ?.addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Dokumen pengguna ditemukan
                    val organizerName = document.getString("username")
                    val organizerPhone = document.getString("phoneNumber")
                    tv_organizer.text = organizerName
                    tv_phone.text = organizerPhone
                } else {
                    // Dokumen pengguna tidak ditemukan atau tidak ada
                    tv_organizer.text = "Orang Baik"
                    tv_phone.text = "-"
                }
            }
            ?.addOnFailureListener { exception ->
                // Penanganan kesalahan saat mengambil dokumen pengguna
                Log.e(TAG, "Error getting organizer document", exception)
            }

        Glide.with(this)
            .load(imageURL)
            .centerCrop()
            .into(iv_image)

        itemsRecyclerView = findViewById(R.id.rv_items_needed)
        itemNeededAdapter = itemNeeded?.let { ItemNeededAdapter(it) }!!
        itemsRecyclerView.adapter = itemNeededAdapter
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)

        val iconBack = findViewById<ImageView>(R.id.ic_back)

        iconBack.setOnClickListener{
            finish()
        }

        val btn_donate = findViewById<TextView>(R.id.btn_donate)

        btn_donate.setOnClickListener{
            val intent = Intent(this, DonationSendActivity::class.java)
            intent.putExtra("DONATION", donationDetail)
            startActivity(intent)
            finish()
        }
    }

    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
        // Mengonversi Timestamp ke LocalDateTime
        val localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        // Menggunakan DateTimeFormatter untuk memformat LocalDateTime
        return localDateTime.format(formatter)
    }
}