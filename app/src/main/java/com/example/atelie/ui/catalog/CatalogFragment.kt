package com.example.atelie.ui.catalog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.atelie.data.AppDatabase
import com.example.atelie.data.entity.Cart
import com.example.atelie.databinding.FragmentCatalogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogFragment : Fragment() {
    private lateinit var binding: FragmentCatalogBinding
    private lateinit var db: AppDatabase
    private lateinit var productAdapter: ProductAdapter
    private var currentCategory = "Все"  // Текущая выбранная категория

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        loadProducts()
        setupSearch()
        setupCategories()

        return binding.root
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList()) { productId ->
            addToCart(productId)
        }
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = productAdapter
    }

    private fun loadProducts(category: String = "Все", query: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            val products = if (category == "Все") {
                if (query.isEmpty()) db.atelierDao().getAllProducts()
                else db.atelierDao().searchProducts(query)
            } else {
                if (query.isEmpty()) db.atelierDao().getProductsByCategory(category)
                else db.atelierDao().getProductsByCategory(category)
                    .filter { it.name.contains(query, ignoreCase = true) }
            }
            withContext(Dispatchers.Main) {
                productAdapter = ProductAdapter(products) { productId ->
                    addToCart(productId)
                }
                binding.rvProducts.adapter = productAdapter
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String): Boolean {
                loadProducts(currentCategory, newText)
                return true
            }
        })
    }

    private fun setupCategories() {
        val categories = listOf("Все", "Платья", "Костюмы", "Рубашки", "Аксессуары")
        val layout = binding.categoriesLayout

        categories.forEach { category ->
            val button = Button(requireContext()).apply {
                text = category
                setOnClickListener {
                    currentCategory = category
                    loadProducts(category)
                }
            }
            layout.addView(button)
        }
    }

    private fun addToCart(productId: Int) {
        val userId = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val existingItem = db.atelierDao().getCartItems(userId).find { it.productId == productId }
            if (existingItem != null) {
                // Увеличиваем количество, если товар уже в корзине
                val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
                db.atelierDao().addToCart(updatedItem)
            } else {
                // Добавляем новый товар
                db.atelierDao().addToCart(Cart(userId = userId, productId = productId, quantity = 1))
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Товар добавлен в корзину", Toast.LENGTH_SHORT).show()
            }
        }
    }
}