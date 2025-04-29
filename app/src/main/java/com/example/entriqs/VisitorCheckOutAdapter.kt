package com.example.entriqs

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.entriqs.databinding.ItemVisitorCheckOutBinding
import com.example.entriqs.model.Visitor

class VisitorCheckOutAdapter(
    private var visitors: List<Visitor>,
    private val onCheckOutClick: (Visitor) -> Unit
) : RecyclerView.Adapter<VisitorCheckOutAdapter.VisitorViewHolder>() {

    inner class VisitorViewHolder(private val binding: ItemVisitorCheckOutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(visitor: Visitor) {
            binding.visitorName.text = visitor.name
            binding.checkInTime.text = binding.root.context.getString(R.string.checked_in_label, visitor.checkInTime)
            binding.visitorStatus.text = binding.root.context.getString(R.string.status_label, visitor.status)

            if (visitor.checkOutTime != null) {
                binding.checkOutTime.text = binding.root.context.getString(R.string.checked_out_label, visitor.checkOutTime)
                binding.checkOutTime.visibility = View.VISIBLE
            } else {
                binding.checkOutTime.visibility = View.GONE
            }

            if (visitor.photoUri != null) {
                binding.visitorPhoto.setImageURI(Uri.parse(visitor.photoUri))
            } else {
                binding.visitorPhoto.setImageResource(R.drawable.ic_profile)
            }

            binding.checkOutButton.visibility = if (visitor.status == "Checked In") View.VISIBLE else View.GONE
            binding.checkOutButton.setOnClickListener { onCheckOutClick(visitor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val binding = ItemVisitorCheckOutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {
        holder.bind(visitors[position])
    }

    override fun getItemCount(): Int = visitors.size

    fun updateVisitors(newVisitors: List<Visitor>) {
        visitors = newVisitors
        notifyDataSetChanged()
    }
}