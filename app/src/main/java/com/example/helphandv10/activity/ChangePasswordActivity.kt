package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.helphandv10.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var tvCurrentPassword: EditText
    private lateinit var tvNewPassword: EditText
    private lateinit var tvConfirmNewPassword: EditText
    private lateinit var btnChangePassword: ConstraintLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        // Initialize UI elements
        tvCurrentPassword = findViewById(R.id.tv_currentPass)
        tvNewPassword = findViewById(R.id.tv_newPass)
        tvConfirmNewPassword = findViewById(R.id.tv_confirmNewPass)
        btnChangePassword = findViewById(R.id.cl_btn_change_password)

        // Set transformation method to hide password characters with asterisks
        tvCurrentPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        tvNewPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        tvConfirmNewPassword.transformationMethod = AsteriskPasswordTransformationMethod()

        // Set click listener for Change Password button
        btnChangePassword.setOnClickListener {
            handleChangePassword()
        }

        // Find the back button by its ID
        val backButton: View = findViewById(R.id.ic_back)

        // Set an onClickListener for the back button
        backButton.setOnClickListener {
            // Finish the current activity and go back to the previous activity
            finish()
        }
    }

    private fun handleChangePassword() {
        val currentPassword = tvCurrentPassword.text.toString()
        val newPassword = tvNewPassword.text.toString()
        val confirmNewPassword = tvConfirmNewPassword.text.toString()

        if (newPassword.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmNewPassword) {
            Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show()
            return
        }


        // Reauthenticate user to verify current password
        val credential = currentUser.email?.let { EmailAuthProvider.getCredential(it, currentPassword) }
        credential?.let {
            currentUser.reauthenticate(it)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Update password
                        currentUser.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful) {
                                    // Password updated successfully
                                    val intent = Intent(this, LoginActivity::class.java)
                                    intent.putExtra("message", "Your password has been updated successfully. Please login again.")
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Failed to update password
                                    Toast.makeText(this, "Failed to update password: ${updatePasswordTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Failed to reauthenticate user
                        Toast.makeText(this, "Failed to authenticate user: ${reauthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    // Custom transformation method to show last character temporarily before hiding it
    private class AsteriskPasswordTransformationMethod : PasswordTransformationMethod() {
        private val handler = Handler()

        override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
            val isVisible = true // Set initial visibility
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({  }, 1000)
            return PasswordCharSequence(source!!, isVisible)
        }

        private class PasswordCharSequence(val source: CharSequence, val isVisible: Boolean) : CharSequence {
            override val length: Int
                get() = source.length

            override fun get(index: Int): Char {
                return if (index == source.length - 1 && !isVisible) {
                    // Show the last character as plain text
                    source[index]
                } else {
                    // Replace other characters with asterisks
                    '*'
                }
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return source.subSequence(startIndex, endIndex)
            }
        }
    }

}
