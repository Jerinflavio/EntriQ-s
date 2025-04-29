package com.example.entriqs.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.entriqs.databinding.ItemLogBinding
import com.example.entriqs.model.ListItem

class VisitorLogAdapter(
    private val onItemClick: (ListItem.VisitorItem) -> Unit = {}
) : ListAdapter<ListItem, VisitorLogAdapter.ViewHolder>(LogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        Log.d("VisitorLogAdapter", "Binding item at position $position: $item")
    }

    inner class ViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem) {
            when (item) {
                is ListItem.StaffItem -> {
                    binding.itemName.text = item.staff.name
                    binding.itemExtraInfo.text = item.staff.status
                    binding.itemPhone.visibility = View.GONE
                    binding.itemTimestamp.visibility = View.GONE
                }
                is ListItem.SecurityItem -> {
                    binding.itemName.text = item.security.name
                    binding.itemExtraInfo.text = item.security.status
                    binding.itemPhone.visibility = View.GONE
                    binding.itemTimestamp.visibility = View.GONE
                }
                is ListItem.VisitorItem -> {
                    binding.itemName.text = item.visitor.name
                    val timeText = if (item.visitor.checkOutTime != null) {
                        "Check-In: ${item.visitor.checkInTime}\nCheck-Out: ${item.visitor.checkOutTime}"
                    } else {
                        "Check-In: ${item.visitor.checkInTime}"
                    }
                    binding.itemTimestamp.text = timeText
                    binding.itemTimestamp.visibility = View.VISIBLE
                    binding.itemExtraInfo.text = "Status: ${item.visitor.status}"
                    binding.itemExtraInfo.visibility = View.VISIBLE
                    binding.itemPhone.visibility = View.GONE
                    binding.root.setOnClickListener { onItemClick(item) }
                }
                else -> {
                    binding.root.visibility = View.GONE
                    Log.w("VisitorLogAdapter", "Unexpected item type: $item")
                }
            }
        }
    }

    class LogDiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return when {
                oldItem is ListItem.StaffItem && newItem is ListItem.StaffItem -> oldItem.staff.name == newItem.staff.name
                oldItem is ListItem.SecurityItem && newItem is ListItem.SecurityItem -> oldItem.security.name == newItem.security.name
                oldItem is ListItem.VisitorItem && newItem is ListItem.VisitorItem -> oldItem.visitor.visitorId == newItem.visitor.visitorId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}