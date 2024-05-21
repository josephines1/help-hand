package com.example.helphandv10

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.activity.MainActivity
import com.example.helphandv10.adapter.ListAdapter
import com.example.helphandv10.databinding.FragmentSearchBinding
import com.example.helphandv10.viewmodel.donation.ListViewModel
import com.example.helphandv10.viewmodel.donation.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var et_keyword: EditText
    private lateinit var ic_back: ImageView
    private val searchViewModel: SearchViewModel by viewModel()
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvExplores.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ListAdapter(requireContext(), listOf())
        binding.rvExplores.adapter = adapter

        searchViewModel.getAllDonations()

        binding.icBack.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        val initialPaddingBottom = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
        val additionalPadding = (48 * resources.displayMetrics.density + 0.5f).toInt()
        val newPaddingBottom = initialPaddingBottom + additionalPadding
        binding.rvExplores.setPadding(binding.rvExplores.paddingLeft, binding.rvExplores.paddingTop, binding.rvExplores.paddingRight, newPaddingBottom)

        binding.etKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                if (keyword.isEmpty()) {
                    // Jika keyword kosong, tampilkan semua data donasi
                    searchViewModel.getAllDonations()
                    binding.tvFindPlace.text = "Find a place to share"
                    binding.tvFindPlace.visibility = View.VISIBLE
                } else {
                    // Jika ada keyword, lakukan pencarian
                    binding.tvFindPlace.text = "Search Result:"
                    searchViewModel.searchDonations(keyword)
                }
            }
        })

        searchViewModel.searchResults.observe(viewLifecycleOwner, Observer { donations ->
            Log.d("SearchFragment", "Received ${donations.size} donations")
            if (donations.isEmpty()) {
                // Tampilkan TextView "No donation found" jika tidak ada hasil pencarian
                binding.tvNoData.visibility = View.VISIBLE
                binding.tvFindPlace.visibility = View.GONE
            } else {
                binding.tvNoData.visibility = View.GONE
            }
            adapter.updateData(donations)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}