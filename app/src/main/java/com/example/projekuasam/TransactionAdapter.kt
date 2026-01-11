package com.example.projekuasam

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. Update Constructor to accept a click listener function
class TransactionAdapter(
    private var groupedList: List<ListItem>,
    private val onItemClicked: (Transaction) -> Unit // New parameter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ... keep existing code ...
        val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
        val tvMonthYear: TextView = itemView.findViewById(R.id.tvMonthYear)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ... keep existing code ...
        val title: TextView = itemView.findViewById(R.id.textTitle)
        val amount: TextView = itemView.findViewById(R.id.textAmount)
        val category: TextView = itemView.findViewById(R.id.textCategory)
    }

    // ... getItemViewType and onCreateViewHolder remain the same ...
    override fun getItemViewType(position: Int): Int {
        return when (groupedList[position]) {
            is ListItem.DateHeader -> TYPE_HEADER
            is ListItem.TransactionItem -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
            TransactionViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = groupedList[position]) {
            is ListItem.DateHeader -> {
                // ... keep existing header logic ...
                val headerHolder = holder as HeaderViewHolder
                val date = Date(item.dateTimestamp)
                headerHolder.tvDayNumber.text = SimpleDateFormat("dd", Locale.getDefault()).format(date)
                headerHolder.tvDayName.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                headerHolder.tvMonthYear.text = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
            }

            is ListItem.TransactionItem -> {
                val transactionHolder = holder as TransactionViewHolder
                val trans = item.transaction

                // 2. Set the click listener for the whole row
                holder.itemView.setOnClickListener {
                    onItemClicked(trans)
                }

                // ... keep existing view binding logic ...
                transactionHolder.title.text = trans.title
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                transactionHolder.category.text = "${trans.type.lowercase().capitalize()} • ${timeFormat.format(Date(trans.date))}"

                val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                if (trans.type == "INCOME") {
                    transactionHolder.amount.text = "+ ${formatRp.format(trans.amount)}"
                    transactionHolder.amount.setTextColor(Color.parseColor("#43A047"))
                } else {
                    transactionHolder.amount.text = "- ${formatRp.format(trans.amount)}"
                    transactionHolder.amount.setTextColor(Color.parseColor("#E53935"))
                }
            }
        }
    }

    override fun getItemCount(): Int = groupedList.size

    fun updateData(newList: List<ListItem>) {
        groupedList = newList
        notifyDataSetChanged()
    }
}