package com.example.helphandv10

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.activity.MainActivity
import com.example.helphandv10.activity.SuccessCreateDonation
import com.example.helphandv10.adapter.NeedsAdapter
import com.example.helphandv10.model.Donations
import com.example.helphandv10.viewmodel.donation.AddViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar
import java.util.UUID

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val addViewModel: AddViewModel by viewModel()
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri

    private lateinit var needsAdapter: NeedsAdapter

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

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val et_title = view.findViewById<EditText>(R.id.et_create_title)
        val et_date = view.findViewById<EditText>(R.id.et_create_date)
        val et_location = view.findViewById<EditText>(R.id.et_create_location)
        val cl_image = view.findViewById<ConstraintLayout>(R.id.cl_create_image)
        val btn_create = view.findViewById<ConstraintLayout>(R.id.cl_btn_create)
        val btn_create_text = view.findViewById<TextView>(R.id.btn_create_text)

        val initialMarginBottom = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
        val additionalMargin = (32 * resources.displayMetrics.density + 0.5f).toInt()
        val newMarginBottom = initialMarginBottom + additionalMargin
        val linearLayout = view.findViewById<LinearLayout>(R.id.linearLayout)
        val linearLayout_in = view.findViewById<LinearLayout>(R.id.linearLayout_in)

        val params = btn_create.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = newMarginBottom
        btn_create.layoutParams = params

        needsAdapter = NeedsAdapter(mutableListOf())

        fun addNewInputField() {
            val newItemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_need, linearLayout, false)
            val needEditText = linearLayout_in.findViewById<EditText>(R.id.et_needs)

            newItemView.findViewById<ImageView>(R.id.btnDelNeed).setOnClickListener {
                linearLayout.removeView(newItemView)
            }

            // Validasi jika edit text kosong
            if(needEditText.text.toString().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in the item needs",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            linearLayout.addView(newItemView)
        }

        // Menambahkan onClickListener untuk tombol tambah Need
        val btnAddNeed = view.findViewById<ImageView>(R.id.btnAddNeed)
        btnAddNeed.setOnClickListener {
            addNewInputField()
        }

        et_date.setOnClickListener {
            showDatePickerDialog(et_date)
        }

        storageReference = FirebaseStorage.getInstance().reference
        cl_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        fun collectDataFromLinearLayout(container: LinearLayout): List<String> {
            val inputData = mutableListOf<String>()
            // Iterasi melalui setiap child dari LinearLayout container
            for (i in 0 until container.childCount) {
                val view = container.getChildAt(i)
                // Jika child merupakan LinearLayout, kita perlu memanggil fungsi ini secara rekursif
                if (view is LinearLayout) {
                    // Panggil fungsi rekursif untuk mengumpulkan data dari LinearLayout anak
                    val dataFromChildLayout = collectDataFromLinearLayout(view)
                    // Tambahkan semua data dari LinearLayout anak ke dalam inputData
                    inputData.addAll(dataFromChildLayout)
                }
                // Jika child merupakan EditText, tambahkan teksnya ke dalam inputData
                if (view is EditText) {
                    inputData.add(view.text.toString())
                }
            }
            return inputData
        }

        btn_create.setOnClickListener {
            val title = et_title.text.toString()
            val date = et_date.text.toString()
            val location = et_location.text.toString()
            val needs = collectDataFromLinearLayout(linearLayout)
            Log.d("NEEDS INPUT", needs.toString())

            if (title.isEmpty() || date.isEmpty() || needs.any { it.isEmpty() } || location.isEmpty()) {
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
            } else if (Timestamp(deadline) < Timestamp.now()) {
                Toast.makeText(context, "Deadline cannot be dated before today.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (::imageUri.isInitialized) {
                btn_create_text.text = "Saving..."
                btn_create_text.setTextColor(R.color.text)
                btn_create.setBackgroundResource(R.drawable.button_neutral_rounded_corner)

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
                                Toast.makeText(context, "Successfully saved data", Toast.LENGTH_SHORT).show()
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

    private fun showDatePickerDialog(et_date: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        context?.let {
            DatePickerDialog(
                it,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    et_date.setText(selectedDate)
                },
                year,
                month,
                day
            )
        }?.show()
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