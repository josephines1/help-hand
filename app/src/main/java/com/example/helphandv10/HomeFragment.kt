package com.example.helphandv10

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

        initDummyData()

        recyclerView = view.findViewById(R.id.rv_donations)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.setNestedScrollingEnabled(false);
        adapter = ListAdapter(donationsList)
        recyclerView.adapter = adapter
    }

    private fun initDummyData() {
        val donation1 = Donations(
            title = "Bantuan Korban Banjir",
            donationImageUrl = "https://www.redcross.ca/getmedia/8098bc8d-af28-4703-9155-0323e360a1b2/flooding460.jpg.aspx;.pdf;?width=350&height=237",
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

        donationsList = listOf(donation1, donation2)
    }

    fun convertLocalDateToTimestamp(year: Int, month: Int, day: Int): Timestamp {
        // Buat LocalDate dari input
        val localDate = LocalDate.of(year, month, day)
        // Konversi LocalDate ke LocalDateTime di awal hari
        val startOfDay = localDate.atTime(LocalTime.MIN)
        // Konversi LocalDateTime ke java.util.Date
        val date = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant())
        // Konversi Date ke Timestamp
        return Timestamp(date.time)
    }
}