package com.example.atelie.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,  // Например: "Платья", "Костюмы"
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "stock") val stock: Int,  // Количество на складе
    @ColumnInfo(name = "imageRes") val imageRes: Int  // Иконка из ресурсов (R.drawable.dress1)
)