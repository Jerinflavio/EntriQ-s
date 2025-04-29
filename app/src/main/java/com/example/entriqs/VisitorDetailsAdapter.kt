package com.example.entriqs

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.example.entriqs.databinding.ItemVisitorDetailBinding
import com.example.entriqs.model.Visitor

class VisitorDetailsAdapter(
    private val visitors: List<Visitor>,
    private val editVisitorLauncher: ActivityResultLauncher<Intent> // Receive launcher from activity
) : RecyclerView.Adapter<VisitorDetailsAdapter.VisitorViewHolder>() {

    inner class VisitorViewHolder(
        private val binding: ItemVisitorDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(visitor: Visitor) {
            binding.visitorId.text = visitor.visitorId.takeIf { it.isNotEmpty() } ?: binding.root.context.getString(R.string.na)
            binding.visitorName.text = visitor.name.takeIf { it.isNotEmpty() } ?: binding.root.context.getString(R.string.na)
            binding.checkInTime.text = binding.root.context.getString(
                R.string.check_in_label,
                visitor.checkInTime.takeIf { it.isNotEmpty() } ?: binding.root.context.getString(R.string.na)
            )
            binding.checkOutTime.text = binding.root.context.getString(
                R.string.check_out_label,
                visitor.checkOutTime ?: binding.root.context.getString(R.string.na)
            )
            binding.checkOutTime.visibility = if (visitor.checkOutTime != null) View.VISIBLE else View.GONE
            binding.visitorStatus.text = binding.root.context.getString(R.string.status_label, visitor.status)

            binding.visitorPhone.text = visitor.phoneNumber.takeIf { it.isNotEmpty() } ?: binding.root.context.getString(R.string.na)
            binding.visitorPhone.visibility = View.VISIBLE
            binding.phoneLabel.visibility = View.VISIBLE
            // Show "Edited" label only if the "phone" field was edited
            binding.phoneEditedLabel.visibility = if (visitor.editedFields.contains("phone")) View.VISIBLE else View.GONE

            binding.visitorPurpose.text = visitor.purpose.takeIf { it.isNotEmpty() } ?: binding.root.context.getString(R.string.na)
            binding.visitorPurpose.visibility = View.VISIBLE
            binding.purposeLabel.visibility = View.VISIBLE
            // Show "Edited" label only if the "purpose" field was edited
            binding.purposeEditedLabel.visibility = if (visitor.editedFields.contains("purpose")) View.VISIBLE else View.GONE

            binding.visitorDestination.text = visitor.destination.takeIf { it.isNotEmpty() } ?: binding.root.context.getString(R.string.na)
            binding.visitorDestination.visibility = View.VISIBLE
            binding.destinationLabel.visibility = View.VISIBLE
            // Show "Edited" label only if the "destination" field was edited
            binding.destinationEditedLabel.visibility = if (visitor.editedFields.contains("destination")) View.VISIBLE else View.GONE

            binding.visitorIdNumber.text = visitor.idNumber.takeIf { it.isNotEmpty() } ?: binding.root.context.getString(R.string.na)
            binding.visitorIdNumber.visibility = View.VISIBLE
            binding.idNumberLabel.visibility = View.VISIBLE
            // Show "Edited" label only if the "idNumber" field was edited
            binding.idNumberEditedLabel.visibility = if (visitor.editedFields.contains("idNumber")) View.VISIBLE else View.GONE

            if (visitor.photoUri != null) {
                binding.visitorPhoto.setImageURI(Uri.parse(visitor.photoUri))
            } else {
                binding.visitorPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Show "Edited" label only if the "name" field was edited
            binding.nameEditedLabel.visibility = if (visitor.editedFields.contains("name")) View.VISIBLE else View.GONE

            // Show Edit button only for "Checked In" visitors
            binding.editButton.visibility = if (visitor.status == "Checked In") View.VISIBLE else View.GONE
            binding.editButton.setOnClickListener {
                val intent = Intent(binding.root.context, EditVisitorActivity::class.java).apply {
                    putExtra("visitor", visitor)
                }
                editVisitorLauncher.launch(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val binding = ItemVisitorDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {
        holder.bind(visitors[position])
    }

    override fun getItemCount(): Int = visitors.size
}