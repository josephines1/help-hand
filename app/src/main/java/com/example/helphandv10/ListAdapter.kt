package com.example.helphandv10

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(val contact: List<Donation>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_item_title)
        val date: TextView = itemView.findViewById(R.id.tv_item_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)

        return ViewHolder(view);
    }

    override fun getItemCount() = contact.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = contact[position]

        holder.title.text = item.title
        holder.date.text = item.dateUntil
    }

}