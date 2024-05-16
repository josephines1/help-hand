package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.helphandv10.R
import com.example.helphandv10.model.Donations

class SuccessCreateDonation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_success_create_donation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val iconBack = findViewById<ImageView>(R.id.ic_back)
        val tv_back_to_home = findViewById<TextView>(R.id.tv_back_to_home)
        val cl_btn_manage = findViewById<ConstraintLayout>(R.id.cl_btn_manage)

        iconBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        tv_back_to_home.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val donation: Donations? = intent.getParcelableExtra("DONATION")
        if (donation != null) {
            donation.id?.let { Log.d("ID ID ID ID: SUCCESS", it) }
        }

        // Gunakan data donasi yang diterima sesuai kebutuhan Anda
        if (donation != null) {
            // Misalnya, Anda dapat mencetak judul donasi ke log
            Log.d("SuccessCreateDonation", "Judul Donasi: ${donation.title}")
        }

        cl_btn_manage.setOnClickListener{
            val intent = Intent(this, ManageDonationActivity::class.java)
            intent.putExtra("DONATION", donation)
            startActivity(intent)
            finish()
        }
    }
}