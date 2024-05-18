package com.example.helphandv10.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.example.helphandv10.activity.DonationDetailActivity
import com.example.helphandv10.utils.Resource
import com.example.helphandv10.viewmodel.form.ManageDonorsViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DonorsAdapter(
    private val context: Context?,
    private var donors: List<DocumentSnapshot>,
    private val viewModel: ManageDonorsViewModel,
    private val lifecycleScope: LifecycleCoroutineScope
) : RecyclerView.Adapter<DonorsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_photo: ImageView = itemView.findViewById(R.id.iv_img)
        val tv_username: TextView = itemView.findViewById(R.id.tv_item_name)
        val tv_request: TextView = itemView.findViewById(R.id.tv_item_request)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donors, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = donors.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val donorSnapshot = donors[position]
        val userId = donorSnapshot.id

        viewModel.fetchUser(userId)

        lifecycleScope.launch {
            viewModel.user.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val userSnapshot = resource.data
                        val username = userSnapshot.getString("username") ?: "Unknown"
                        val profileImageUrl = userSnapshot.getString("photoProfileURL")

                        holder.tv_username.text = username
                        Glide.with(holder.itemView.context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.icon_placeholder_photo_profile_secondary)
                            .centerCrop()
                            .into(holder.iv_photo)
                    }
                    is Resource.Error -> {
                        // Handle error
                    }
                    is Resource.Loading -> {
                        // Show loading state if necessary
                    }
                    null -> {
                        // No action needed
                    }
                }
            }
        }

        val data = donorSnapshot.data ?: return
        val confirmation = data["confirmation"] as? Map<*, *>
        val shippingConfirmation = data["shippingConfirmation"] as? Map<*, *>

        val requestText = if (confirmation != null && shippingConfirmation != null) {
            val expectedArrival = shippingConfirmation["expectedArrival"] as? Timestamp
            "Verify Arrival on ${expectedArrival?.let { formatTimestamp(it) } ?: ""}"
        } else {
            val plannedShippingDate = confirmation?.get("plannedShippingDate") as? Timestamp
            "Confirm Delivery on ${plannedShippingDate?.let { formatTimestamp(it) } ?: ""}"
        }

        holder.tv_request.text = requestText
    }

    private fun formatTimestamp(timestamp: Timestamp): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
        val localDateTime = timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        return localDateTime.format(formatter)
    }

    fun submitList(newDonors: List<DocumentSnapshot>) {
        donors = newDonors
        notifyDataSetChanged()
    }
}