package com.example.helphandv10.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphandv10.R
import com.example.helphandv10.activity.DonationDetailActivity
import com.example.helphandv10.activity.ManageDonationActivity
import com.example.helphandv10.model.Donations
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DonationAdapter(private val isAsOrganizer: Boolean) : ListAdapter<Donations, DonationAdapter.DonationViewHolder>(DonationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return DonationViewHolder(view, isAsOrganizer)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DonationViewHolder(itemView: View, private val isAsOrganizer: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_title)
        private val deadlineTextView: TextView = itemView.findViewById(R.id.tv_item_date)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_item_image)

        fun bind(donation: Donations) {
            titleTextView.text = donation.title
            deadlineTextView.text = "Deadline: ${donation.deadline?.let { formatTimestamp(it) }}"
            Glide.with(itemView.context)
                .load(donation.donationImageUrl)
                .centerCrop()
                .into(imageView)

            itemView.setOnClickListener {
                val context = it.context
                val intent = if (isAsOrganizer) {
                    Intent(context, ManageDonationActivity::class.java)
                } else {
                    Intent(context, DonationDetailActivity::class.java)
                }
                intent.putExtra("DONATION", donation)
                context.startActivity(intent)
            }
        }

        private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            // Mengonversi Timestamp ke LocalDateTime
            val localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            // Menggunakan DateTimeFormatter untuk memformat LocalDateTime
            return localDateTime.format(formatter)
        }
    }

    class DonationDiffCallback : DiffUtil.ItemCallback<Donations>() {
        override fun areItemsTheSame(oldItem: Donations, newItem: Donations): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Donations, newItem: Donations): Boolean {
            return oldItem == newItem
        }
    }
}