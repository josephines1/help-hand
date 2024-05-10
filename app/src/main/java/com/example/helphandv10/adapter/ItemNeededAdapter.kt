package com.example.helphandv10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.R

class ItemNeededAdapter(private val items: List<String>) : RecyclerView.Adapter<ItemNeededAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemTextView: TextView = itemView.findViewById(R.id.tv_item_needed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_needed_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTextView.text = items[position]
    }

    override fun getItemCount() = items.size
}