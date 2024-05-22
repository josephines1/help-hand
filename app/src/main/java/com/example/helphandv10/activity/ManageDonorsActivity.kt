package com.example.helphandv10.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.R
import com.example.helphandv10.adapter.DonorsAdapter
import com.example.helphandv10.data.DonationRepository
import com.example.helphandv10.data.UsersRepository
import com.example.helphandv10.model.Donations
import com.example.helphandv10.utils.Resource
import com.example.helphandv10.viewmodel.form.ManageDonorsViewModel
import com.example.helphandv10.viewmodel.form.ManageDonorsViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collect

class ManageDonorsActivity : AppCompatActivity() {

    private lateinit var viewModel: ManageDonorsViewModel
    private lateinit var adapter: DonorsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_donors)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val donationRepository = DonationRepository(FirebaseFirestore.getInstance())
        val usersRepository = UsersRepository(FirebaseFirestore.getInstance())
        val viewModelFactory = ManageDonorsViewModelFactory(donationRepository, usersRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ManageDonorsViewModel::class.java)

        setupRecyclerView()

        val donation: Donations? = intent.getParcelableExtra("DONATION")
        val donationId = donation?.id ?: intent.getStringExtra("DONATION_ID")

        if(donationId != null) {
            viewModel.fetchDonors(donationId)
        } else {
            Toast.makeText(this, "Donation is Invalid", Toast.LENGTH_SHORT).show()
            finish()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.donors.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading indicator
                    }
                    is Resource.Success -> {
                        resource.data.let { donorDocuments ->
                            if (donorDocuments.isEmpty()) {
                                findViewById<TextView>(R.id.tvNoData).visibility = View.VISIBLE
                            } else {
                                findViewById<TextView>(R.id.tvNoData).visibility = View.GONE
                                adapter = DonorsAdapter(this@ManageDonorsActivity, donorDocuments, viewModel, lifecycleScope)
                                findViewById<RecyclerView>(R.id.rv_donors).adapter = adapter
                            }
                        }
                    }
                    is Resource.Error -> {
                        Log.d("ManageDonorsActivity", "Error: ${resource.error}")
                    }
                }
            }
        }

        val iconBack = findViewById<ImageView>(R.id.ic_back)

        iconBack.setOnClickListener{
            finish()
        }
    }

    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.rv_donors).apply {
            layoutManager = LinearLayoutManager(context)
        }
    }
}