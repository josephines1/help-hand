package com.example.helphandv10.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.helphandv10.R

class NeedsAdapter(private val needs: MutableList<String>) :
    RecyclerView.Adapter<NeedsAdapter.NeedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_need, parent, false)
        return NeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NeedViewHolder, position: Int) {
        val need = needs[position]
        holder.bind(need)

        holder.itemView.findViewById<ImageView>(R.id.btnDelNeed).setOnClickListener {
            removeNeed(position)
        }
    }

    override fun getItemCount(): Int = needs.size

    inner class NeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val needEditText: EditText = itemView.findViewById(R.id.et_needs)

        fun bind(need: String) {
            needEditText.setText(need)
        }
    }

    fun removeNeed(position: Int) {
        needs.removeAt(position)
        notifyDataSetChanged()
    }
}