package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.helphandv10.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var tv_doNotHaveAnAccount : TextView

    private fun initComponents() {
        tv_doNotHaveAnAccount = findViewById(R.id.tv_doNotHaveAnAccount)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
//        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        initComponents()
        tv_doNotHaveAnAccount.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        val message = intent.getStringExtra("message")
        if (!message.isNullOrEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        auth = Firebase.auth

        val loginEmail = findViewById<EditText>(R.id.et_loginEmail)
        val loginPassword = findViewById<EditText>(R.id.et_loginPassword)
        val loginBtn = findViewById<Button>(R.id.btn_login)

        loginBtn.setOnClickListener{
            val email = loginEmail.text.toString()
            if (email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email tidak sesuai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val password = loginPassword.text.toString()
            if (password.isEmpty() && password.length < 8) {
                Toast.makeText(this, "Password tidak sesuai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("Login", user?.email.toString())
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("EMAIL", user?.email.toString())
                        startActivity(intent)
                    } else {
                        Log.d("Login", it.exception.toString())
                        Toast.makeText(this, "Login Gagal", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}