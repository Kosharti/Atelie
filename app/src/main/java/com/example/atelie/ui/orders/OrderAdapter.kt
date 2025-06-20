package com.example.atelie.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.atelie.R
import com.example.atelie.data.entity.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (orderId: Int) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val date: TextView = itemView.findViewById(R.id.tvOrderDate)
        val total: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val status: TextView = itemView.findViewById(R.id.tvOrderStatus)

        init {
            itemView.setOnClickListener {
                onOrderClick(orders[adapterPosition].id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderId.text = "Заказ #${position + 1}"
        holder.date.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(order.timestamp))
        holder.total.text = "Сумма: ${order.totalPrice} ₽"
        holder.status.text = "Статус: ${order.status}"
    }

    override fun getItemCount(): Int = orders.size
}