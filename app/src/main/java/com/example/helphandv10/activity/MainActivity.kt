package com.example.helphandv10.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.helphandv10.HomeFragment
import com.example.helphandv10.R
import com.example.helphandv10.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navBottom: BottomNavigationView
    private lateinit var content: View

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

        navBottom = binding.navBottom
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        content = findViewById(R.id.nav_host_fragment_activity_main)

        navBottom.setupWithNavController(navController)

        var isShowHistory: Boolean = intent.getBooleanExtra("showHistoryFragment", false)
        if (isShowHistory) {
            navigateToHistoryFragment()
        }

        content.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val isNavBarVisible = navBottom.visibility == View.VISIBLE
                adjustContentPadding(isNavBarVisible)
            }
        })

        KeyboardVisibilityEvent.setEventListener(
            this,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    Log.d(TAG, "onVisibilityChanged: Keyboard visibility changed")
                    if (isOpen) {
                        Log.d(TAG, "onVisibilityChanged: Keyboard is open")
                        navBottom.visibility = View.INVISIBLE
                        Log.d(TAG, "onVisibilityChanged: NavBar got Invisible")
                        adjustContentPadding(false)
                    } else {
                        Log.d(TAG, "onVisibilityChanged: Keyboard is closed")
                        navBottom.visibility = View.VISIBLE
                        Log.d(TAG, "onVisibilityChanged: NavBar got Visible")
                        adjustContentPadding(true)
                    }
                }
            }
        )

        // Menambahkan listener navigasi untuk memantau perubahan fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val currentFragment = destination.id
            Log.d(TAG, "Current Fragment: $currentFragment")
            // Update padding sesuai dengan fragment yang sedang ditampilkan
            if (currentFragment == R.id.nav_item_home) {
                adjustContentPadding(true)
            } else {
                adjustContentPadding(false)
            }
        }
    }

    private fun adjustContentPadding(isNavBarVisible: Boolean) {
        val currentFragment = getCurrentFragment()
        Log.d("CURRENT FRAGMENT", currentFragment.toString())

        var bottomPadding = if (isNavBarVisible) {
            resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
        } else {
            0
        }

        if (currentFragment is HomeFragment) {
            bottomPadding = 0
        }

        content.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = bottomPadding
        }
    }

    private fun getCurrentFragment(): Fragment? {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        return navHostFragment.childFragmentManager.primaryNavigationFragment
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