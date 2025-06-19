package com.example.atelie

import android.app.Application
import com.example.atelie.data.AppDatabase
import com.example.atelie.data.entity.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Заполняем БД при первом запуске
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@App)
            if (db.atelierDao().getAllProducts().isEmpty()) {
                val sampleProducts = listOf(
                    // Одежда
                    Product(name = "Платье вечернее", category = "Платья", price = 5000.0, stock = 5, imageRes = R.drawable.dress1),
                    Product(name = "Платье летнее", category = "Платья", price = 3500.0, stock = 7, imageRes = R.drawable.dress2),
                    Product(name = "Платье офисное", category = "Платья", price = 4200.0, stock = 4, imageRes = R.drawable.dress3),

                    // Костюмы
                    Product(name = "Костюм мужской", category = "Костюмы", price = 8000.0, stock = 3, imageRes = R.drawable.suit1),
                    Product(name = "Костюм женский", category = "Костюмы", price = 7500.0, stock = 2, imageRes = R.drawable.suit2),
                    Product(name = "Костюм детский", category = "Костюмы", price = 4500.0, stock = 6, imageRes = R.drawable.suit3),

                    // Рубашки
                    Product(name = "Рубашка мужская", category = "Рубашки", price = 2500.0, stock = 8, imageRes = R.drawable.shirt1),
                    Product(name = "Рубашка женская", category = "Рубашки", price = 2300.0, stock = 5, imageRes = R.drawable.shirt2),
                    Product(name = "Рубашка в клетку", category = "Рубашки", price = 2800.0, stock = 4, imageRes = R.drawable.shirt3),

                    // Аксессуары
                    Product(name = "Галстук шелковый", category = "Аксессуары", price = 1500.0, stock = 10, imageRes = R.drawable.accessory1),
                    Product(name = "Ремень кожаный", category = "Аксессуары", price = 2000.0, stock = 7, imageRes = R.drawable.accessory2),
                    Product(name = "Шарф кашемировый", category = "Аксессуары", price = 1800.0, stock = 5, imageRes = R.drawable.accessory3)
                )
                sampleProducts.forEach { db.atelierDao().insertProduct(it) }
            }
        }
    }
}