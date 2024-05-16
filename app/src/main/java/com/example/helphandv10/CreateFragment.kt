package com.example.helphandv10

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.helphandv10.activity.MainActivity
import com.example.helphandv10.activity.SuccessCreateDonation
import com.example.helphandv10.databinding.FragmentHomeBinding
import com.example.helphandv10.model.Donations
import com.example.helphandv10.viewmodel.donation.AddViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.firebase.storage.StorageReference
import java.util.Calendar
import java.util.UUID

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val addViewModel: AddViewModel by viewModel()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val IMAGE_PICK_CODE = 1000
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val et_title = view.findViewById<EditText>(R.id.et_create_title)
        val et_date = view.findViewById<EditText>(R.id.et_create_date)
        val et_location = view.findViewById<EditText>(R.id.et_create_location)
        val et_needs = view.findViewById<EditText>(R.id.et_create_needs)
        val cl_image = view.findViewById<ConstraintLayout>(R.id.cl_create_image)
        val btn_create = view.findViewById<ConstraintLayout>(R.id.cl_btn_create)

        val initialMarginBottom = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
        val additionalMargin = (48 * resources.displayMetrics.density + 0.5f).toInt()
        val newMarginBottom = initialMarginBottom + additionalMargin

        val params = btn_create.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = newMarginBottom
        btn_create.layoutParams = params

        storageReference = FirebaseStorage.getInstance().reference

        cl_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btn_create.setOnClickListener {
            val title = et_title.text.toString()
            val date = et_date.text.toString()
            val location = et_location.text.toString()
            val needs = et_needs.text.toString().split(",").map { it.trim() }

            if (title.isEmpty() || date.isEmpty() || needs.isEmpty() || location.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val deadline = try {
                val dateParts = date.split("-")
                val year = dateParts[0].toInt()
                val month = dateParts[1].toInt()
                val day = dateParts[2].toInt()
                Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }.time
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (deadline == null) {
                Toast.makeText(context, "Invalid deadline format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (::imageUri.isInitialized) {
                val imageRef = storageReference.child("donations/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val donationId = UUID.randomUUID().toString()
                        val donation = Donations(
                            id = donationId,
                            title = title,
                            donationImageUrl = imageUrl,
                            location = location,
                            organizerId = "users/${FirebaseAuth.getInstance().currentUser?.uid ?: ""}",
                            deadline = Timestamp(deadline),
                            itemsNeeded = needs,
                            donors = mapOf()
                        )
                        addViewModel.addDonation(donation)

                        addViewModel.donationAdded.observe(viewLifecycleOwner) {
                            if (it) {
                                val intent = Intent(requireContext(), SuccessCreateDonation::class.java)
                                intent.putExtra("DONATION", donation)
                                if (donation != null) {
                                    donation.id?.let { Log.d("ID ID ID ID: FROM CREATE", it) }
                                }
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Image Required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }

        val iconBack = view.findViewById<ImageView>(R.id.ic_back)

        iconBack.setOnClickListener{
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let {
                imageUri = it
                val imageIv = view?.findViewById<ImageView>(R.id.iv_preview)
                imageIv?.scaleType = ImageView.ScaleType.CENTER_CROP
                imageIv?.setImageURI(imageUri)

                val tv_upload_image = view?.findViewById<TextView>(R.id.tv_upload_image)
                tv_upload_image?.visibility = View.GONE
            }
        }
    }
}