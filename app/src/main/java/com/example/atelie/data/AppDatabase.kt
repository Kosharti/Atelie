package com.example.atelie.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.atelie.data.dao.AtelierDao
import com.example.atelie.data.entity.Cart
import com.example.atelie.data.entity.Message
import com.example.atelie.data.entity.Order
import com.example.atelie.data.entity.OrderItem
import com.example.atelie.data.entity.Product
import com.example.atelie.data.entity.User

@Database(
    entities = [User::class, Product::class, Cart::class, Message::class,  Order::class, OrderItem::class ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun atelierDao(): AtelierDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "atelier_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}