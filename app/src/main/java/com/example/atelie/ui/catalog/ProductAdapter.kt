package com.example.atelie.ui.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.atelie.R
import com.example.atelie.data.entity.Product

class ProductAdapter(
    private val products: List<Product>,
    private val onAddToCart: (productId: Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.ivProductImage)
        val name: TextView = itemView.findViewById(R.id.tvProductName)
        val price: TextView = itemView.findViewById(R.id.tvProductPrice)
        val stock: TextView = itemView.findViewById(R.id.tvProductStock)
        val addToCartBtn: Button = itemView.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.image.setImageResource(product.imageRes)
        holder.name.text = product.name
        holder.price.text = "Цена: ${product.price} ₽"
        holder.stock.text = "Осталось: ${product.stock} шт."

        holder.addToCartBtn.setOnClickListener {
            if (product.stock > 0) {
                onAddToCart(product.id)
            } else {
                Toast.makeText(holder.itemView.context, "Товар закончился", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = products.size
}