package com.example.entriqs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.entriqs.databinding.ItemStatusCardBinding

class StatusCategoryAdapter(
    private val categories: List<StatusCategory>
) : RecyclerView.Adapter<StatusCategoryAdapter.StatusCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusCategoryViewHolder {
        val binding = ItemStatusCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatusCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusCategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    class StatusCategoryViewHolder(
        private val binding: ItemStatusCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: StatusCategory) {
            binding.categoryIcon.setImageResource(category.iconRes)
            binding.categoryName.text = category.name
            binding.categoryCount.text = "Count: ${category.count}"
            binding.categoryProgress.progress = category.percentage.toInt()
            binding.categoryProgress.progressTintList = ContextCompat.getColorStateList(
                binding.root.context,
                category.progressColor
            )
            binding.categoryPercentage.text = "${category.percentage.toInt()}%"
        }
    }
}