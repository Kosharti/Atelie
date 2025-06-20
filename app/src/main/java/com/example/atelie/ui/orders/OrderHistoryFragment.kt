package com.example.atelie.ui.orders

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.atelie.data.AppDatabase
import com.example.atelie.databinding.FragmentOrderHistoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.example.atelie.R


class OrderHistoryFragment : Fragment() {
    private lateinit var binding: FragmentOrderHistoryBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        setupRecyclerView()
        loadOrders()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(emptyList()) { orderId ->
            findNavController().navigate(
                R.id.action_orderHistoryFragment_to_orderDetailsFragment,
                Bundle().apply { putInt("order_id", orderId) }
            )
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }


    private fun loadOrders() {
        val userId = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getInt("user_id", -1)

        if (userId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            val orders = db.atelierDao().getUserOrders(userId).sortedBy { it.timestamp }
            withContext(Dispatchers.Main) {
                if (orders.isEmpty()) {
                    binding.tvEmptyOrders.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyOrders.visibility = View.GONE
                    adapter = OrderAdapter(orders) { orderId ->
                        findNavController().navigate(
                            R.id.action_orderHistoryFragment_to_orderDetailsFragment,
                            Bundle().apply { putInt("order_id", orderId) }
                        )
                    }
                    binding.rvOrders.adapter = adapter
                }
            }
        }
    }
}