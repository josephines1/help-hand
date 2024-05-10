package com.example.helphandv10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.helphandv10.Donations
import com.example.helphandv10.R
import java.sql.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ListAdapter(val donation: List<Donations>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_item_image)
        val title: TextView = itemView.findViewById(R.id.tv_item_title)
        val date: TextView = itemView.findViewById(R.id.tv_item_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)

        return ViewHolder(view);
    }

    override fun getItemCount() = donation.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = donation[position]

        holder.title.text = item.title
        holder.date.text = "Deadline: ${formatTimestamp(item.deadline)}"

        Glide.with(holder.itemView.context)
            .load(item.donationImageUrl)
            .centerCrop()
            .into(holder.imageView)
    }

    private fun formatTimestamp(timestamp: Timestamp): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
        // Mengonversi Timestamp ke LocalDateTime
        val localDateTime = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        // Menggunakan DateTimeFormatter untuk memformat LocalDateTime
        return localDateTime.format(formatter)
    }

}