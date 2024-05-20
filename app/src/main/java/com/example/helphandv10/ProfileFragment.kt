package com.example.helphandv10
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.example.helphandv10.databinding.FragmentProfileBinding
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseAuth.*
//
//////
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
/////**
//// * A simple [Fragment] subclass.
//// * Use the [ProfileFragment.newInstance] factory method to
//// * create an instance of this fragment.
//// */
//class ProfileFragment : Fragment() {
//    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////        // Inflate the layout for this fragment
////        return inflater.inflate(R.layout.fragment_profile, container, false)
////    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment using View Binding
//        val binding = FragmentProfileBinding.inflate(inflater, container, false)
//        val currentUser = getInstance().currentUser
//
//        // Set the user's display name to the TextView
//        currentUser?.let {
//            binding.textViewUserName.text = it.displayName
//        } ?: run {
//            binding.textViewUserName.text = "Guest" // or any default value
//        }
//
//        return binding.root
//    }
////
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment ProfileFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            ProfileFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
//}
//
//
//


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

