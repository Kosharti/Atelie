package com.example.atelie.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.atelie.data.entity.OrderItem
import com.example.atelie.R
import com.example.atelie.data.entity.Product

class OrderItemAdapter(
    private val items: List<OrderItem>,
    private val products: Map<Int, Product> // Явно указываем типы
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    inner class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.tvProductName)
        private val productPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val productQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        private val itemTotal: TextView = itemView.findViewById(R.id.tvItemTotal)
        private val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)

        fun bind(item: OrderItem) {
            val product = products[item.productId]

            productName.text = product?.name ?: "Товар #${item.productId}"
            productPrice.text = "Цена: ${item.priceAtOrder} ₽"
            productQuantity.text = "Количество: ${item.quantity}"
            itemTotal.text = "${item.priceAtOrder * item.quantity} ₽"

            // Установка изображения
            product?.imageRes?.let { resId ->
                productImage.setImageResource(resId)
            } ?: run {
                productImage.setImageResource(R.drawable.ic_product_placeholder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_product, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}