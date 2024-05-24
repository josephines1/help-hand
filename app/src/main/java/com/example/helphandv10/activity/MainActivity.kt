package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.helphandv10.HistoryFragment
import com.example.helphandv10.R
import com.example.helphandv10.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
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

        val navBottom: BottomNavigationView = binding.navBottom
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navBottom.setupWithNavController(navController)

        var isShowHistory: Boolean = intent.getBooleanExtra("showHistoryFragment", false)
        if (isShowHistory) {
            navigateToHistoryFragment()
        }
    }

    private fun navigateToHistoryFragment() {
        val bundle = Bundle().apply {
            putBoolean("showAsOrganizer", true)
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navController.navigate(R.id.nav_item_history, bundle)

        // Tambahkan listener untuk BottomNavigationView
        val navBottom: BottomNavigationView = binding.navBottom
        navBottom.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_item_home -> {
                    // Navigasi kembali ke Fragment Home
                    navController.navigate(R.id.nav_item_home)
                    true
                }
                R.id.nav_item_search -> {
                    navController.navigate(R.id.nav_item_search)
                    true
                }
                R.id.nav_item_create -> {
                    navController.navigate(R.id.nav_item_create)
                    true
                }
                R.id.nav_item_history -> {
                    navController.navigate(R.id.nav_item_history)
                    true
                }
                R.id.nav_item_profile -> {
                    navController.navigate(R.id.nav_item_profile)
                    true
                }
                else -> false
            }
        }
    }
}