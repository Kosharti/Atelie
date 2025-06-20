package com.example.atelie.ui.support

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.atelie.data.AppDatabase
import com.example.atelie.data.entity.Message
import com.example.atelie.databinding.FragmentSupportBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupportFragment : Fragment() {
    private lateinit var binding: FragmentSupportBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: MessageAdapter
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSupportBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        userId = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
            .getInt("user_id", -1)

        setupRecyclerView()
        loadMessages()

        // Отправка сообщения
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(emptyList())
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true  // Прокрутка к новым сообщениям
        }
        binding.rvMessages.adapter = adapter
    }

    private fun loadMessages() {
        if (userId == -1) {
            Toast.makeText(requireContext(), "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val messages = db.atelierDao().getMessagesWithUser(userId)
            withContext(Dispatchers.Main) {
                adapter = MessageAdapter(messages)
                binding.rvMessages.adapter = adapter
                if (messages.isNotEmpty()) {
                    scrollToBottom()
                }
            }
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString()
        if (text.isBlank()) {
            Toast.makeText(requireContext(), "Введите текст", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            db.atelierDao().insertMessage(
                Message(
                    userId = userId,
                    text = text
                )
            )
            withContext(Dispatchers.Main) {
                binding.etMessage.text.clear()
                loadMessages()
            }
        }
    }

    private fun scrollToBottom() {
        if (adapter.itemCount > 0) {
            binding.rvMessages.post {
                binding.rvMessages.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }
    }
}