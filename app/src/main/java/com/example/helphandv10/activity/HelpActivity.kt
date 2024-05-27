package com.example.helphandv10.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.helphandv10.R

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // Find the back button by its ID
        val backButton: ImageView = findViewById(R.id.ic_back)

        // Set an onClickListener for the back button
        backButton.setOnClickListener {
            // Finish the current activity and go back to the previous activity
            finish()
        }
    }
}