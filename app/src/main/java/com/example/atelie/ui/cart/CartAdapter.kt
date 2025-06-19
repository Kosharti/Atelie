package com.example.atelie.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.atelie.data.entity.Cart
import com.example.atelie.data.entity.CartWithProduct
import com.example.atelie.databinding.ItemCartBinding

class CartAdapter(
    private val items: List<CartWithProduct>,
    private val onQuantityChanged: (cartItem: Cart, newQuantity: Int) -> Unit,
    private val onItemRemoved: (cartItem: Cart) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val rootView: CardView = binding.root

        fun bind(item: CartWithProduct) {
            binding.ivCartItemImage.setImageResource(item.product.imageRes)
            binding.tvCartItemName.text = item.product.name
            binding.tvCartItemPrice.text = "Цена: ${item.product.price * item.cart.quantity} ₽ (${item.product.price} ₽/шт.)"
            binding.tvQuantity.text = item.cart.quantity.toString()

            binding.btnIncrease.setOnClickListener {
                val newQuantity = item.cart.quantity + 1
                if (newQuantity <= item.product.stock) {
                    onQuantityChanged(item.cart, newQuantity)
                } else {
                    Toast.makeText(rootView.context, "Недостаточно товара на складе", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnDecrease.setOnClickListener {
                val newQuantity = item.cart.quantity - 1
                if (newQuantity >= 1) {
                    onQuantityChanged(item.cart, newQuantity)
                }
            }

            binding.btnRemove.setOnClickListener {
                onItemRemoved(item.cart)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}