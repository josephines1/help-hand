package com.example.helphandv10.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var currentUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private var originalUsername: String = ""
    private var originalEmail: String = ""
    private var originalPhoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firestore and get current user
        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser!!

        // Initialize views
        editTextUsername = findViewById(R.id.tv_userName)
        editTextEmail = findViewById(R.id.tv_Email)
        editTextPhoneNumber = findViewById(R.id.tv_phoneNum) // Corrected phone number EditText
        val changePhotoButton = findViewById<TextView>(R.id.editphoto)
        val updateButton = findViewById<ConstraintLayout>(R.id.cl_btn_update_profile) // Corrected update button
        val backButton = findViewById<ImageView>(R.id.ic_back)

        // Fetch and set current user data
        fetchUserData()

        // Handle click on back button
        backButton.setOnClickListener {
            finish() // Finish the current activity and go back to the previous activity
        }

        // Handle click on change photo button
        changePhotoButton.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        // Handle click on update button
        updateButton.setOnClickListener {
            updateUserData()
        }
    }

    private fun fetchUserData() {
        // Fetch the current user's data from Firebase
        val userRef = firestore.collection("users").document(currentUser.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Retrieve username, email, phone number, and photo profile URL from Firestore document
                    val username = document.getString("username")
                    val email = document.getString("email")
                    val phoneNumber = document.getString("phoneNumber")
                    val photoProfileURL = document.getString("photoProfileURL")

                    // Set retrieved data to corresponding EditText fields
                    editTextUsername.setText(username)
                    editTextEmail.setText(email)
                    editTextPhoneNumber.setText(phoneNumber)

                    // Load the current profile photo into the ImageView
                    // You can use any image loading library like Glide or Picasso
                    // Here, I'm assuming you have a method loadProfilePhoto() to load the image into the ImageView
                    loadProfilePhoto(photoProfileURL)
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun updateUserData() {
        val newUsername = editTextUsername.text.toString()
        val newEmail = editTextEmail.text.toString()
        val newPhoneNumber = editTextPhoneNumber.text.toString()

        // Update user data in Firestore
        val userRef = firestore.collection("users").document(currentUser.uid)
        val updateMap = mutableMapOf<String, Any>()
        if (newUsername != originalUsername) {
            updateMap["username"] = newUsername
        }
        if (newEmail != originalEmail) {
            updateMap["email"] = newEmail
        }
        if (newPhoneNumber != originalPhoneNumber) {
            updateMap["phoneNumber"] = newPhoneNumber
        }

        userRef.update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {
            val selectedImageUri = data?.data
            val imageView = findViewById<ImageView>(R.id.ivUserPhotoProfile)
            imageView.setImageURI(selectedImageUri)

            // Upload the selected image to Firebase Storage
            selectedImageUri?.let { uri ->
                val storageRef = FirebaseStorage.getInstance().reference
                val photoRef = storageRef.child("profile_photos/${currentUser.uid}")

                photoRef.putFile(uri)
                    .addOnSuccessListener { _ ->
                        // Get the download URL of the uploaded image
                        photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Update the user's profile photo URL in Firestore
                            val userRef = firestore.collection("users").document(currentUser.uid)
                            userRef.update("photoProfileURL", downloadUri.toString())
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePhoto(photoUrl: String?) {
        if (!photoUrl.isNullOrEmpty() && photoUrl != "-") {
            Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .into(findViewById<ImageView>(R.id.ivUserPhotoProfile))
        } else {
            // If the photo URL is null or empty, you can set a default placeholder image here
            // For example:
             findViewById<ImageView>(R.id.ivUserPhotoProfile).setImageResource(R.drawable.icon_placeholder_photo_profile_secondary)
        }
    }
}
