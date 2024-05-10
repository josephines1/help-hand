package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.helphandv10.R

class SignUpActivity : AppCompatActivity() {
    lateinit var tv_alreadyRegistered : TextView

    private fun initComponents() {
        tv_alreadyRegistered = findViewById(R.id.tv_alreadyRegistered)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initComponents()

        tv_alreadyRegistered.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}