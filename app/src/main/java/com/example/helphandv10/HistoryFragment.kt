package com.example.helphandv10

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.activity.MainActivity
import com.example.helphandv10.adapter.DonationAdapter
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.viewmodel.donation.HistoryViewModel
import com.example.helphandv10.viewmodel.donation.HistoryViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var donationViewModel: HistoryViewModel
    private lateinit var donationAdapter: DonationAdapter
    private lateinit var recyclerView: RecyclerView

    private var showAsOrganizer: Boolean = false
    private lateinit var tvAsDonor: TextView
    private lateinit var tvAsOrganizer: TextView
    private lateinit var tvNoData: TextView
    private lateinit var tvLoading: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            showAsOrganizer = it.getBoolean("showAsOrganizer", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_history)
        tvAsDonor = view.findViewById(R.id.tv_as_donor)
        tvAsOrganizer = view.findViewById(R.id.tv_as_organizer)
        tvNoData = view.findViewById(R.id.tvNoData)
        tvLoading = view.findViewById(R.id.tvLoading)

        setupAdapter()

        val padding = (48 * resources.displayMetrics.density + 0.5f).toInt()
        recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, padding)

        val firestoreDb = FirebaseFirestore.getInstance()
        val donationRepository = DonationRepository(firestoreDb)
        val factory = HistoryViewModelFactory(donationRepository)
        donationViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        // Initial data load
        loadData()
        updateTextViewColors()

        // Set up the listener for the TextView tv_as_organizer
        tvAsOrganizer.setOnClickListener {
            showAsOrganizer = true
            updateTextViewColors()
            setupAdapter()
            loadData()
        }

        // Set up the listener for the TextView tv_as_donor
        tvAsDonor.setOnClickListener {
            showAsOrganizer = false
            updateTextViewColors()
            setupAdapter()
            loadData()
        }

        val iconBack = view.findViewById<ImageView>(R.id.ic_back)

        iconBack.setOnClickListener{
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupAdapter() {
        donationAdapter = DonationAdapter(showAsOrganizer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = donationAdapter
    }

    private fun loadData() {
        tvLoading.visibility = View.VISIBLE

        lifecycleScope.launch {
            if (showAsOrganizer) {
                donationViewModel.getDonationsByOrganizer().collect { donations ->
                    tvLoading.visibility = View.GONE
                    updateUI(donations)
                }
            } else {
                donationViewModel.getDonationsByDonor().collect { donations ->
                    tvLoading.visibility = View.GONE
                    updateUI(donations)
                }
            }
        }
    }

    private fun updateTextViewColors() {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.text)
        val secondaryColor = ContextCompat.getColor(requireContext(), R.color.secondary)

        if (showAsOrganizer) {
            tvAsOrganizer.setTextColor(secondaryColor)
            tvAsDonor.setTextColor(primaryColor)
        } else {
            tvAsOrganizer.setTextColor(primaryColor)
            tvAsDonor.setTextColor(secondaryColor)
        }
    }

    private fun updateUI(donations: List<Donations>) {
        if (donations.isEmpty()) {
            tvNoData.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoData.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            donationAdapter.submitList(donations)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putBoolean("showAsOrganizer", false)
                }
            }
    }
}