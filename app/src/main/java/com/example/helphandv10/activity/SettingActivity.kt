package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.helphandv10.R

class SettingActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // Find the back button by its ID
        val backButton: View = findViewById(R.id.ic_back)

        // Set an onClickListener for the back button
        backButton.setOnClickListener {
            // Finish the current activity and go back to the previous activity
            finish()
        }

        // Set onClickListener for the edit profile button
        val editProfile: View = findViewById(R.id.btn_edit_profile)

        editProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListener for the change password button
        val changePassword: View = findViewById(R.id.btn_change_password)

        changePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListener for the delete account button
        val deleteAccountLayout: View = findViewById(R.id.btn_delete_acc)

        deleteAccountLayout.setOnClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
        }


    }
}