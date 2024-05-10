package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.R
import com.example.helphandv10.adapter.ItemNeededAdapter

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

        itemsRecyclerView = findViewById(R.id.rv_items_needed)
        itemNeededAdapter = ItemNeededAdapter(listOf("Pakaian", "Makanan Kaleng", "Selimut"))
        itemsRecyclerView.adapter = itemNeededAdapter
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}