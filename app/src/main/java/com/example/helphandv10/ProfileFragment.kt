package com.example.helphandv10

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.helphandv10.activity.AboutActivity
import com.example.helphandv10.activity.HelpActivity
import com.example.helphandv10.activity.MainActivity
import com.example.helphandv10.activity.SettingActivity
import com.example.helphandv10.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get current user from FirebaseAuth
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
                        binding.UserName.text = username

                        if (photo != null && photo != "-") {
                            Glide.with(this)
                                .load(photo)
                                .centerCrop()
                                .into(binding.ivUserPhotoProfile)
                        }
                    } else {
                        // User document does not exist, handle accordingly
                        binding.UserName.text = "Guest" // or any default value
                    }
                }
                .addOnFailureListener { exception ->
                    // Error occurred while retrieving user document, handle accordingly
                    binding.UserName.text = "Guest" // or any default value
                }
        } ?: run {
            // Current user is null, handle accordingly
            binding.UserName.text = "Guest" // or any default value
        }

        // Set onClickListener for the settings button
        binding.btnSetting.setOnClickListener {
            val intent = Intent(activity, SettingActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListener for the help button
        binding.btnHelp.setOnClickListener {
            val intent = Intent(activity, HelpActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListener for the about button
        binding.btnAbout.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListener for the logout button
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // Set onClickListener for the back button
        binding.icBack.setOnClickListener {
            // Replace ProfileFragment with the HomeFragment
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_logout, null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog = builder.show()

        val btnCancel = dialogView.findViewById<ConstraintLayout>(R.id.cl_btn_negative)
        val btnLogout = dialogView.findViewById<ConstraintLayout>(R.id.cl_btn_positive)

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            alertDialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}