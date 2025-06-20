package com.example.atelie.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.atelie.data.AppDatabase
import com.example.atelie.data.entity.Product
import com.example.atelie.databinding.FragmentOrderDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDetailsFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var db: AppDatabase
    private var orderId: Int = -1

    companion object {
        fun newInstance(orderId: Int): OrderDetailsFragment {
            return OrderDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt("order_id", orderId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        orderId = arguments?.getInt("order_id") ?: -1
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadOrderDetails()
    }

    private fun loadOrderDetails() {
        if (orderId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            val order = db.atelierDao().getOrderById(orderId)
            val items = db.atelierDao().getOrderItems(orderId)
            val products = db.atelierDao().getProductsByIds(items.map { it.productId })

            // Получаем все заказы пользователя для определения номера текущего заказа
            val userOrders = order?.userId?.let { db.atelierDao().getUserOrders(it).sortedBy { it.timestamp }  } ?: emptyList()
            val orderNumber = userOrders.indexOfFirst { it.id == orderId } + 1

            withContext(Dispatchers.Main) {
                order?.let {
                    binding.tvOrderTitle.text = "Заказ #$orderNumber"  // Используем вычисленный номер
                    binding.tvOrderDate.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        .format(Date(it.timestamp))
                    binding.tvOrderStatus.text = "Статус: ${it.status}"
                    binding.tvOrderTotal.text = "Итого: ${it.totalPrice} ₽"

                    val productsMap: Map<Int, Product> = products.associateBy { it.id }
                    val adapter = OrderItemAdapter(items, productsMap)
                    binding.rvOrderItems.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvOrderItems.adapter = adapter
                }
            }
        }
    }
}