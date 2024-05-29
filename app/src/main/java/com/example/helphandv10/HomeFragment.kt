package com.example.helphandv10

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphandv10.adapter.ListAdapter
import com.example.helphandv10.databinding.FragmentHomeBinding
import com.example.helphandv10.model.Donations
import com.example.helphandv10.viewmodel.donation.ListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.ext.android.viewModel

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

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardLoading: CardView
    private val listViewModel: ListViewModel by viewModel()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private fun replaceFragment(fragment: Fragment, itemId: Int) {
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        // Mengubah item terpilih di BottomNavigationView
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_bottom)
        bottomNav.selectedItemId = itemId
    }

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
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
                        val photo = document.getString("photoProfileURL")
                        tv_username.text = username

                        if(photo != "-") {
                            Glide.with(this)
                                .load(photo)
                                .centerCrop()
                                .into(iv_userPhoto)
                        }
                    } else {
                        tv_username.text = "Orang Baik!"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeFragment", e.toString())
                }
        }

        // Mengatur click listener untuk tombol create
        val btn_to_create = view.findViewById<ConstraintLayout>(R.id.cl_btn_home_create)
        btn_to_create.setOnClickListener {
            replaceFragment(CreateFragment(), R.id.nav_item_create)
        }

        // Mengatur click listener untuk tombol search
        val btn_to_search = view.findViewById<ConstraintLayout>(R.id.cl_btn_home_search)
        btn_to_search.setOnClickListener {
            replaceFragment(SearchFragment(), R.id.nav_item_search)
        }

        // Mengatur click listener untuk text view username
        tv_username.setOnClickListener {
            replaceFragment(ProfileFragment(), R.id.nav_item_profile)
        }

        recyclerView = view.findViewById(R.id.rv_donations)
        cardLoading = view.findViewById(R.id.cardLoading)

        listViewModel.getDonations()

        // Jika data terdeteksi
        listViewModel.donations.observe(viewLifecycleOwner, Observer { donationsList ->
            cardLoading.visibility = View.GONE

            // Debugging
            Log.d("HomeFragment", donationsList.toString())

            // Atur adapter recyclerview nya
            val adapter = ListAdapter(requireContext(), donationsList.take(5))
            binding.rvDonations.adapter = adapter

            // Tampilkan recyclerview nya
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter

            // Atur height recyclerview nya
            val layoutParams = recyclerView.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            recyclerView.layoutParams = layoutParams

            // Tambahkan tombol jika jumlah item lebih dari 5
            if (donationsList.size > 5) {
                val btnViewAll = view.findViewById<ConstraintLayout>(R.id.cl_btn_more)
                btnViewAll.setOnClickListener {
                    replaceFragment(SearchFragment(), R.id.nav_item_search)
                }
                btnViewAll.visibility = View.VISIBLE

                // Atur margin nya
                val margin = (112 * resources.displayMetrics.density + 0.5f).toInt()
                val params = btnViewAll.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = margin
                btnViewAll.layoutParams = params
            } else {
                // Atur padding nya
                val initialPaddingBottom = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
                val additionalPadding = (24 * resources.displayMetrics.density + 0.5f).toInt()
                val newPaddingBottom = initialPaddingBottom + additionalPadding
                recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, newPaddingBottom)
            }
        })
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
}