package com.example.helphandv10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.activity.LoginActivity
import com.example.helphandv10.adapter.ListAdapter
import com.example.helphandv10.model.DonationConfirmation
import com.example.helphandv10.model.Donations
import com.example.helphandv10.model.DonorConfirmation
import com.example.helphandv10.model.ShippingConfirmation
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: ListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var donationsList: List<Donations>

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val iv_userPhoto = view.findViewById<ImageView>(R.id.iv_userPhotoProfile)
        val tv_username = view.findViewById<TextView>(R.id.tv_username)

        val firestore = FirebaseFirestore.getInstance()

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        tv_username.text = username
                    } else {
                        tv_username.text = "Orang Baik!"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeFragment", e.toString())
                }
        }

        initDummyData()

        recyclerView = view.findViewById(R.id.rv_donations)

        val initialPaddingBottom = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
        val additionalPadding = (24 * resources.displayMetrics.density + 0.5f).toInt()
        val newPaddingBottom = initialPaddingBottom + additionalPadding
        recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, newPaddingBottom)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ListAdapter(context, donationsList)
        recyclerView.adapter = adapter
    }

    private fun initDummyData() {
        val donation1 = Donations(
            title = "Bantuan Korban Banjir",
            donationImageUrl = "https://www.redcross.ca/getmedia/8098bc8d-af28-4703-9155-0323e360a1b2/flooding460.jpg.aspx;.pdf;?width=350&height=237",
            location = "Jakarta Selatan",
            organizer = "Budi",
            deadline = convertLocalDateToTimestamp(2024, 8, 30),
            itemsNeeded = listOf("Pakaian", "Makanan Kaleng", "Selimut"),
            donors = mapOf(
                "donor1" to DonorConfirmation(
                    confirmation = DonationConfirmation(
                        message = "Siap mengirimkan selimut dan pakaian",
                        plannedShippingDate = convertLocalDateToTimestamp(2024, 8, 24),
                        donationItemImageUrl = "url_to_image.jpg",
                        shippingMethod = "Darat"
                    ),
                    shippingConfirmation = ShippingConfirmation(
                        message = "Paket telah dikirim",
                        expectedArrival = convertLocalDateToTimestamp(2024, 8, 24),
                        shippingProofImageUrl = "url_to_shipping_proof.jpg"
                    )
                ),
                "donor2" to DonorConfirmation(
                    confirmation = DonationConfirmation(
                        message = "Saya akan mengirim makanan kaleng",
                        plannedShippingDate = convertLocalDateToTimestamp(2024, 8, 24),
                        donationItemImageUrl = "url_to_food_image.jpg",
                        shippingMethod = "Udara"
                    ),
                    shippingConfirmation = ShippingConfirmation(
                        message = "Paket sedang dalam perjalanan",
                        expectedArrival = convertLocalDateToTimestamp(2024, 8, 24),
                        shippingProofImageUrl = "url_to_delivery_proof.jpg"
                    )
                )
            )
        )

        val donation2 = Donations(
            title = "Bantuan Korban Banjir 2",
            donationImageUrl = "https://www.redcross.ca/getmedia/8098bc8d-af28-4703-9155-0323e360a1b2/flooding460.jpg.aspx;.pdf;?width=350&height=237",
            location = "Tangerang Selatan",
            organizer = "Rahmat",
            deadline = convertLocalDateToTimestamp(2024, 8, 30),
            itemsNeeded = listOf("Pakaian", "Makanan Kaleng", "Selimut"),
            donors = mapOf(
                "donor1" to DonorConfirmation(
                    confirmation = DonationConfirmation(
                        message = "Siap mengirimkan selimut dan pakaian",
                        plannedShippingDate = convertLocalDateToTimestamp(2024, 8, 24),
                        donationItemImageUrl = "url_to_image.jpg",
                        shippingMethod = "Darat"
                    ),
                    shippingConfirmation = ShippingConfirmation(
                        message = "Paket telah dikirim",
                        expectedArrival = convertLocalDateToTimestamp(2024, 8, 24),
                        shippingProofImageUrl = "url_to_shipping_proof.jpg"
                    )
                ),
                "donor2" to DonorConfirmation(
                    confirmation = DonationConfirmation(
                        message = "Saya akan mengirim makanan kaleng",
                        plannedShippingDate = convertLocalDateToTimestamp(2024, 8, 24),
                        donationItemImageUrl = "url_to_food_image.jpg",
                        shippingMethod = "Udara"
                    ),
                    shippingConfirmation = ShippingConfirmation(
                        message = "Paket sedang dalam perjalanan",
                        expectedArrival = convertLocalDateToTimestamp(2024, 8, 24),
                        shippingProofImageUrl = "url_to_delivery_proof.jpg"
                    )
                )
            )
        )

        donationsList = listOf(donation1, donation2)
    }

    private fun convertLocalDateToTimestamp(year: Int, month: Int, day: Int): Timestamp {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day) // Bulan dimulai dari 0 (Januari) hingga 11 (Desember)
        }

        // Mendapatkan objek Date dari Calendar
        val date = calendar.time

        // Membuat objek Timestamp dari Date
        return Timestamp(date)
    }
}