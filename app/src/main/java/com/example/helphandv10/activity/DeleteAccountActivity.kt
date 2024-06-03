package com.example.helphandv10.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var checkBoxChecked = false // Declare checkBoxChecked as a member variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delete_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the back button by its ID
        val laterButton: ConstraintLayout = findViewById(R.id.cl_btn_later_delete)

        // Set an onClickListener for the back button
        laterButton.setOnClickListener {
            // Finish the current activity and go back to the previous activity
            finish()
        }

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Find the back button by its ID
        val backButton: ImageView = findViewById(R.id.ic_back)

        // Set an onClickListener for the back button
        backButton.setOnClickListener {
            // Finish the current activity and go back to the previous activity
            finish()
        }

        // Set up UI elements
        setupUI()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Set the user's display name and profile picture
        currentUser?.let { user ->
            val uid = user.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        val photo = document.getString("photoProfileURL")
                        // Assuming UserName and ivUserPhotoProfile are views in your layout
                        findViewById<TextView>(R.id.UserName).text = username ?: "Guest"
                        if (!photo.isNullOrEmpty() && photo != "-") {
                            // Check if the context is valid before loading the image
                            if (!isDestroyed) Glide.with(this@DeleteAccountActivity)
                                .load(photo)
                                .centerCrop()
                                .into(findViewById<ImageView>(R.id.iv_userPhotoProfile))
                        } else {
                            // If photoProfileURL is null or empty, set a placeholder image
                            findViewById<ImageView>(R.id.iv_userPhotoProfile).setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
                        }
                    } else {
                        // User document does not exist, handle accordingly
                        findViewById<TextView>(R.id.UserName).text = "Guest"
                    }
                }
                .addOnFailureListener { _ ->
                    // Error occurred while retrieving user document, handle accordingly
                    findViewById<TextView>(R.id.UserName).text = "Guest"
                }
        } ?: run {
            // Current user is null, handle accordingly
            findViewById<TextView>(R.id.UserName).text = "Guest"
        }

        // Find the delete button by its ID
        val btnDelete = findViewById<ConstraintLayout>(R.id.cl_btn_delete_account)

        // Find the checkbox by its ID
        val checkBox = findViewById<CheckBox>(R.id.checkBox)

        // Set onClickListener for the delete button
        btnDelete.setOnClickListener {
            // Check if checkbox is checked
            if (checkBox.isChecked) {
                // Show confirmation dialog before deleting account
                showDeleteConfirmationDialog()
            } else {
                Toast.makeText(this, "Please confirm deletion by checking the checkbox", Toast.LENGTH_SHORT).show()
            }
        }

        // Set onClickListener for the checkbox
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkBoxChecked = isChecked // Access checkBoxChecked here
        }
    }

    // Function to show delete confirmation dialog
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater

        val dialogView = inflater.inflate(R.layout.custom_dialog_delete_account, null)
        val positiveButton = dialogView.findViewById<ConstraintLayout>(R.id.cl_btn_positive)
        val negativeButton = dialogView.findViewById<ConstraintLayout>(R.id.cl_btn_negative)

        // Set up dialog content and buttons
        builder.setView(dialogView)
        val dialog = builder.create()

        // Set onClickListener for positive button
        positiveButton.setOnClickListener {
            deleteAccount()
            dialog.dismiss()
        }

        // Set onClickListener for negative button
        negativeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Function to delete account
    private fun deleteAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val uid = user.uid
            firestore.collection("users").document(uid)
                .delete()
                .addOnSuccessListener {
                    // Account deleted successfully
                    FirebaseAuth.getInstance().signOut() // Sign out the user
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Error occurred while deleting account
                    Log.e("DeleteAccActivity", "Error deleting account", e)
                    Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
