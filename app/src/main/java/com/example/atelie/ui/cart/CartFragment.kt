package com.example.atelie.ui.cart

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.atelie.data.AppDatabase
import com.example.atelie.data.entity.Cart
import com.example.atelie.data.entity.CartWithProduct
import com.example.atelie.databinding.FragmentCartBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        loadCartItems()

        // Кнопка оформления заказа
        binding.btnCheckout.setOnClickListener {
            checkout()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            emptyList(),
            onQuantityChanged = { cart, newQuantity ->
                updateCartItem(cart, newQuantity)
            },
            onItemRemoved = { cart ->
                removeFromCart(cart)
            }
        )
        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter
    }

    private fun loadCartItems() {
        val userId = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val cartItems = db.atelierDao().getCartItems(userId)
            val itemsWithProducts = cartItems.mapNotNull { cart ->
                val product = db.atelierDao().getProductById(cart.productId)
                product?.let { CartWithProduct(cart, it) }
            }
            val totalPrice = itemsWithProducts.sumOf { it.product.price * it.cart.quantity }

            withContext(Dispatchers.Main) {
                adapter = CartAdapter(
                    itemsWithProducts,
                    onQuantityChanged = { cart, newQuantity ->
                        updateCartItem(cart, newQuantity)
                    },
                    onItemRemoved = { cart ->
                        removeFromCart(cart)
                    }
                )
                binding.rvCartItems.adapter = adapter
                binding.tvTotalPrice.text = "Итого: ${totalPrice} ₽"
            }
        }
    }

    private fun updateCartItem(cart: Cart, newQuantity: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            db.atelierDao().addToCart(cart.copy(quantity = newQuantity))
            loadCartItems()  // Перезагружаем данные
        }
    }

    private fun removeFromCart(cart: Cart) {
        CoroutineScope(Dispatchers.IO).launch {
            db.atelierDao().removeFromCart(cart)
            withContext(Dispatchers.Main) {
                loadCartItems()
                Toast.makeText(requireContext(), "Товар удалён", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkout() {
        val userId = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getInt("user_id", -1)

        CoroutineScope(Dispatchers.IO).launch {
            val cartItems = db.atelierDao().getCartItems(userId)
            val allAvailable = cartItems.all { cart ->
                val product = db.atelierDao().getProductById(cart.productId)
                product?.stock?.let { it >= cart.quantity } ?: false
            }

            withContext(Dispatchers.Main) {
                if (allAvailable) {
                    showCheckoutDialog()
                } else {
                    Toast.makeText(requireContext(), "Некоторые товары закончились", Toast.LENGTH_SHORT).show()
                    loadCartItems()  // Обновляем список
                }
            }
        }
    }

    private fun showCheckoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Оформление заказа")
            .setMessage("Заказ оформлен! Спасибо за покупку.")
            .setPositiveButton("OK") { _, _ ->
                clearCart()
            }
            .show()
    }

    private fun clearCart() {
        val userId = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getInt("user_id", -1)

        CoroutineScope(Dispatchers.IO).launch {
            db.atelierDao().clearCart(userId)
            withContext(Dispatchers.Main) {
                loadCartItems()
            }
        }
    }
}