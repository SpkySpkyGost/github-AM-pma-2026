package com.example.myapp004objednavka

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp004objednavka.databinding.ItemOrderBinding
import java.text.DateFormat
import java.util.Date

// seznam zaznamu z databaze; kazdy radek ma tlacitko se tremi teckami (upravit / smazat)
class OrderAdapter(
    private val onEdit: (Order) -> Unit,
    private val onDelete: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            val context = binding.root.context
            binding.tvRecordTitle.text = context.getString(R.string.record_title, order.id, order.bikeName(context))
            binding.tvRecordExtras.text = order.extrasText(context)
            binding.tvRecordDate.text =
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(order.createdAt))

            binding.btnMore.setOnClickListener { anchor ->
                PopupMenu(context, anchor).apply {
                    menuInflater.inflate(R.menu.record_menu, menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> {
                                onEdit(order)
                                true
                            }
                            R.id.action_delete -> {
                                onDelete(order)
                                true
                            }
                            else -> false
                        }
                    }
                    show()
                }
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
        }
    }
}
