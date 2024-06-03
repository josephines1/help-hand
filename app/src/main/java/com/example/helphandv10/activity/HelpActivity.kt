package com.example.helphandv10.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.helphandv10.R

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the back button by its ID
        val backButton: ImageView = findViewById(R.id.ic_back)

        // Set an onClickListener for the back button
        backButton.setOnClickListener {
            // Finish the current activity and go back to the previous activity
            finish()
        }

        // Find the buttons by their IDs
        val btnHelpCenterWA: androidx.constraintlayout.widget.ConstraintLayout = findViewById(R.id.btn_helpcenterWA)
        val btnHelpCenterEmail: androidx.constraintlayout.widget.ConstraintLayout = findViewById(R.id.btn_helpcenterEmail)

        // Set onClickListener for WhatsApp button
        btnHelpCenterWA.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/62895383474182")
            startActivity(intent)
        }

        // Set onClickListener for Email button
        btnHelpCenterEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:helphandcenter@gmail.com")
            startActivity(intent)
        }
    }
}