package com.example.atelie.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.atelie.data.entity.Cart
import com.example.atelie.data.entity.Message
import com.example.atelie.data.entity.MessageWithUser
import com.example.atelie.data.entity.Order
import com.example.atelie.data.entity.OrderItem
import com.example.atelie.data.entity.Product
import com.example.atelie.data.entity.User

@Dao
interface AtelierDao {
    // --- Пользователи ---
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun getUser(username: String, password: String): User?

    // --- Товары ---
    @Insert
    suspend fun insertProduct(product: Product)

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE category = :category")
    suspend fun getProductsByCategory(category: String): List<Product>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
    suspend fun searchProducts(query: String): List<Product>

    // --- Корзина ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cart: Cart)

    @Query("SELECT * FROM cart WHERE userId = :userId")
    suspend fun getCartItems(userId: Int): List<Cart>

    @Delete
    suspend fun removeFromCart(cart: Cart)

    // --- Сообщения ---
    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product?

    @Query("DELETE FROM cart WHERE userId = :userId")
    suspend fun clearCart(userId: Int)

    // Получение сообщений с именами пользователей
    @Query("""
    SELECT messages.*, users.username 
    FROM messages 
    INNER JOIN users ON messages.userId = users.id 
    WHERE messages.userId = :userId 
    ORDER BY timestamp
""")
    suspend fun getMessagesWithUser(userId: Int): List<MessageWithUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Insert
    suspend fun insertOrder(order: Order): Long

    @Insert
    suspend fun insertOrderItems(orderItems: List<OrderItem>)

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getUserOrders(userId: Int): List<Order>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Int): List<OrderItem>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): Order?

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM products WHERE id IN (:productIds)")
    suspend fun getProductsByIds(productIds: List<Int>): List<Product>
}