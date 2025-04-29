package com.example.entriqs.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.entriqs.databinding.ItemRoleBinding
import com.example.entriqs.model.ListItem

class RoleAdapter(
    private val isReadOnly: Boolean = false,
    private val showAllDetails: Boolean = false,
    private val onToggleClick: (position: Int, roleItem: ListItem.RoleItem, type: String) -> Unit = { _, _, _ -> },
    private val onEditClick: (position: Int, roleItem: ListItem.RoleItem, type: String) -> Unit = { _, _, _ -> },
    private val onDeleteClick: (position: Int, type: String) -> Unit = { _, _ -> }
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(ListItemDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemRoleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is RoleViewHolder) {
            when (item) {
                is ListItem.RoleItem -> holder.bindRole(item, position, isReadOnly, showAllDetails, onToggleClick, onEditClick, onDeleteClick)
                is ListItem.VisitorItem -> holder.bindVisitor(item)
                else -> {}
            }
        }
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return when (item) {
            is ListItem.RoleItem -> item.role.roleId.hashCode().toLong()
            is ListItem.VisitorItem -> item.visitor.name.hashCode().toLong()
            else -> position.toLong()
        }
    }

    inner class RoleViewHolder(private val binding: ItemRoleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindRole(
            item: ListItem.RoleItem,
            position: Int,
            isReadOnly: Boolean,
            showAllDetails: Boolean,
            onToggleClick: (position: Int, roleItem: ListItem.RoleItem, type: String) -> Unit,
            onEditClick: (position: Int, roleItem: ListItem.RoleItem, type: String) -> Unit,
            onDeleteClick: (position: Int, type: String) -> Unit
        ) {
            val role = item.role
            Log.d("RoleAdapter", "Binding: position=$position, role=$role, type=${item.type}, status=${role.status}, isChecked=${role.status == "Active"}")
            binding.root.tag = position

            binding.tvName.text = role.name ?: "Unknown"
            binding.tvPhone.text = role.phone ?: "N/A"
            binding.tvEmail.text = role.email ?: "N/A"
            binding.tvPassword.text = role.password ?: "N/A"
            binding.switchStatus.setOnCheckedChangeListener(null)
            binding.switchStatus.isChecked = role.status == "Active"
            binding.switchStatus.text = role.status
            binding.switchStatus.visibility = View.VISIBLE

            if (isReadOnly) {
                binding.switchStatus.isEnabled = false
                binding.btnEdit.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
                if (showAllDetails) {
                    binding.tvPhone.visibility = View.VISIBLE
                    binding.tvEmail.visibility = View.VISIBLE
                    binding.tvPassword.visibility = View.VISIBLE
                } else {
                    binding.tvPhone.visibility = View.GONE
                    binding.tvEmail.visibility = View.GONE
                    binding.tvPassword.visibility = View.GONE
                }
            } else {
                binding.tvPhone.visibility = View.VISIBLE
                binding.tvEmail.visibility = View.VISIBLE
                binding.tvPassword.visibility = View.VISIBLE
                binding.switchStatus.isEnabled = true
                binding.btnEdit.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE

                binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
                    Log.d("RoleAdapter", "Toggle clicked: position=$position, role=$role, type=${item.type}, isChecked=$isChecked")
                    onToggleClick(position, item, item.type)
                }
                binding.btnEdit.setOnClickListener { onEditClick(position, item, item.type) }
                binding.btnDelete.setOnClickListener { onDeleteClick(position, item.type) }
            }
        }

        fun bindVisitor(item: ListItem.VisitorItem) {
            val visitor = item.visitor
            binding.tvName.text = visitor.name
            binding.tvPhone.visibility = View.GONE
            binding.tvEmail.visibility = View.GONE
            binding.tvPassword.visibility = View.GONE
            binding.switchStatus.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
        }
    }
}

class ListItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return when {
            oldItem is ListItem.RoleItem && newItem is ListItem.RoleItem ->
                oldItem.role.roleId == newItem.role.roleId
            oldItem is ListItem.VisitorItem && newItem is ListItem.VisitorItem ->
                oldItem.visitor.visitorId == newItem.visitor.visitorId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}