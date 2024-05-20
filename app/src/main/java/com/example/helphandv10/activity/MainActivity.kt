package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.helphandv10.R
import com.example.helphandv10.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import com.example.helphandv10.HistoryFragment
import com.example.helphandv10.HomeFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()

        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (intent.getBooleanExtra("showHistoryFragment", false)) {
            navigateToHistoryFragment()
        }

        val navBottom: BottomNavigationView = binding.navBottom
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navBottom.setupWithNavController(navController)
    }

    private fun navigateToHistoryFragment() {
        val historyFragment = HistoryFragment.newInstance("param1", "param2")
        val bundle = Bundle().apply {
            putBoolean("showAsOrganizer", true)
        }
        historyFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, historyFragment)
            .addToBackStack(null)
            .commit()

        // Mengubah item terpilih di BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_bottom)
        bottomNav.selectedItemId = R.id.nav_item_create
    }
}