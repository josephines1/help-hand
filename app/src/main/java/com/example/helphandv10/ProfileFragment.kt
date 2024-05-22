package com.example.helphandv10

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.helphandv10.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get current user from FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Set the user's display name to the TextView
        currentUser?.let { user ->
            val uid = user.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        binding.textViewUserName.text = username
                    } else {
                        // User document does not exist, handle accordingly
                        binding.textViewUserName.text = "Guest" // or any default value
                    }
                }
                .addOnFailureListener { exception ->
                    // Error occurred while retrieving user document, handle accordingly
                    binding.textViewUserName.text = "Guest" // or any default value
                }
        } ?: run {
            // Current user is null, handle accordingly
            binding.textViewUserName.text = "Guest" // or any default value
        }

        return binding.root
    }
}