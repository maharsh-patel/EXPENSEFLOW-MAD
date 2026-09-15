package com.example.mad_assignment_1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.GradientDrawable

class TransactionAdapter(
    private val transactionList: MutableList<Transaction>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView           = view.findViewById(R.id.icon_1)
        val title: TextView           = view.findViewById(R.id.icon_text_1)
        val amount: TextView          = view.findViewById(R.id.icon_text_2)
        val date: TextView            = view.findViewById(R.id.icon_text_3)
        val deleteButton: ImageButton = view.findViewById(R.id.delete)
    }

    override fun getItemCount(): Int = transactionList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        val iconColor = if (transaction.iconColor != 0) transaction.iconColor
                        else Color.parseColor("#D06BED")

        holder.icon.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(iconColor)
        }
        holder.icon.setImageResource(transaction.iconResId)
        holder.title.text = transaction.title

        val amountValue = transaction.amount.toDoubleOrNull() ?: 0.0
        holder.amount.text = if (amountValue < 0) {
            "-$${String.format("%,.2f", -amountValue)}"
        } else {
            "$${String.format("%,.2f", amountValue)}"
        }
        holder.amount.setTextColor(
            if (amountValue < 0) Color.parseColor("#F44336")
            else Color.parseColor("#0F9D58")
        )
        holder.date.text = transaction.date

        holder.deleteButton.setOnClickListener { onDeleteClick(transaction.id) }
    }
}
