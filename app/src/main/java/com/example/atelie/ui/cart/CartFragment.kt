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
import com.example.atelie.data.entity.Order
import com.example.atelie.data.entity.OrderItem
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

            // Добавляем проверку на пустую корзину
            if (cartItems.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Корзина пуста", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val allAvailable = cartItems.all { cart ->
                val product = db.atelierDao().getProductById(cart.productId)
                product?.stock?.let { it >= cart.quantity } ?: false
            }

            withContext(Dispatchers.Main) {
                if (allAvailable) {
                    showCheckoutDialog(userId, cartItems)
                } else {
                    Toast.makeText(requireContext(), "Некоторые товары закончились", Toast.LENGTH_SHORT).show()
                    loadCartItems()
                }
            }
        }
    }

    private suspend fun showCheckoutDialog(userId: Int, cartItems: List<Cart>) {
        if (cartItems.isEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Корзина пуста", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val totalPrice = cartItems.sumOf { cart ->
            val product = db.atelierDao().getProductById(cart.productId)
            product?.price?.times(cart.quantity) ?: 0.0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Оформление заказа")
            .setMessage("Заказ на сумму ${totalPrice} ₽. Подтвердить?")
            .setPositiveButton("Подтвердить") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Создаем заказ
                        val orderId = db.atelierDao().insertOrder(
                            Order(
                                userId = userId,
                                totalPrice = totalPrice
                            )
                        ).toInt()

                        // Добавляем товары в заказ
                        val orderItems = cartItems.mapNotNull { cart ->
                            val product = db.atelierDao().getProductById(cart.productId)
                            product?.let {
                                OrderItem(
                                    orderId = orderId,
                                    productId = cart.productId,
                                    quantity = cart.quantity,
                                    priceAtOrder = product.price
                                )
                            }
                        }
                        db.atelierDao().insertOrderItems(orderItems)

                        // Обновляем количество товаров на складе
                        cartItems.forEach { cart ->
                            val product = db.atelierDao().getProductById(cart.productId)
                            product?.let {
                                // Вместо insertProduct используем update (нужно добавить метод в DAO)
                                db.atelierDao().updateProduct(
                                    it.copy(stock = it.stock - cart.quantity)
                                )
                            }
                        }

                        // Очищаем корзину
                        db.atelierDao().clearCart(userId)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Заказ оформлен!", Toast.LENGTH_SHORT).show()
                            loadCartItems()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Ошибка при оформлении заказа: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Отмена", null)
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