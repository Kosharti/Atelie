package com.example.atelie.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.atelie.data.AppDatabase
import com.example.atelie.data.entity.User
import com.example.atelie.databinding.FragmentProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: AppDatabase
    private var isLoginMode = true // По умолчанию показываем форму входа

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        db = AppDatabase.getDatabase(requireContext())

        checkAuthState() // Проверяем, авторизован ли пользователь

        // Кнопки переключения между режимами
        binding.btnSwitchToLogin.setOnClickListener {
            switchToLoginMode()
        }

        binding.btnSwitchToRegister.setOnClickListener {
            switchToRegisterMode()
        }

        // Кнопка входа
        binding.btnLogin.setOnClickListener {
            val username = binding.etLoginUsername.text.toString()
            val password = binding.etLoginPassword.text.toString()
            if (validateInput(username, password)) {
                loginUser(username, password)
            }
        }

        // Кнопка регистрации
        binding.btnRegister.setOnClickListener {
            val username = binding.etRegUsername.text.toString()
            val password = binding.etRegPassword.text.toString()
            if (validateInput(username, password)) {
                registerUser(username, password)
            }
        }

        // Кнопка выхода
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        // По умолчанию показываем форму входа
        switchToLoginMode()

        return binding.root
    }

    private fun switchToLoginMode() {
        isLoginMode = true
        binding.loginLayout.visibility = View.VISIBLE
        binding.registerLayout.visibility = View.GONE
        binding.btnSwitchToLogin.isEnabled = false
        binding.btnSwitchToRegister.isEnabled = true
    }

    private fun switchToRegisterMode() {
        isLoginMode = false
        binding.loginLayout.visibility = View.GONE
        binding.registerLayout.visibility = View.VISIBLE
        binding.btnSwitchToLogin.isEnabled = true
        binding.btnSwitchToRegister.isEnabled = false
    }


    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun loginUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = db.atelierDao().getUser(username, password)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    saveSession(user.id, username)
                    showAuthLayout(username)
                    Toast.makeText(requireContext(), "Добро пожаловать, $username!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerUser(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val userExists = db.atelierDao().getUser(username, password) != null
            withContext(Dispatchers.Main) {
                if (userExists) {
                    Toast.makeText(requireContext(), "Пользователь уже существует", Toast.LENGTH_SHORT).show()
                } else {
                    val newUser = User(username = username, password = password)
                    db.atelierDao().insertUser(newUser)
                    Toast.makeText(requireContext(), "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                    binding.etRegUsername.text.clear()
                    binding.etRegPassword.text.clear()
                }
            }
        }
    }

    private fun saveSession(userId: Int, username: String) {
        sharedPreferences.edit()
            .putInt("user_id", userId)
            .putString("username", username)
            .putBoolean("is_auth", true)
            .apply()
    }

    private fun checkAuthState() {
        val isAuth = sharedPreferences.getBoolean("is_auth", false)
        if (isAuth) {
            val username = sharedPreferences.getString("username", "")
            showAuthLayout(username ?: "")
        }
    }

    private fun showAuthLayout(username: String) {
        binding.authLayout.visibility = View.VISIBLE
        binding.tvWelcome.text = "Привет, $username!"
        // Скрываем формы входа/регистрации
        binding.etLoginUsername.visibility = View.GONE
        binding.etLoginPassword.visibility = View.GONE
        binding.btnLogin.visibility = View.GONE
        binding.etRegUsername.visibility = View.GONE
        binding.etRegPassword.visibility = View.GONE
        binding.btnRegister.visibility = View.GONE
    }

    private fun logoutUser() {
        sharedPreferences.edit().clear().apply()
        binding.authLayout.visibility = View.GONE
        // Показываем формы входа/регистрации
        binding.etLoginUsername.visibility = View.VISIBLE
        binding.etLoginPassword.visibility = View.VISIBLE
        binding.btnLogin.visibility = View.VISIBLE
        binding.etRegUsername.visibility = View.VISIBLE
        binding.etRegPassword.visibility = View.VISIBLE
        binding.btnRegister.visibility = View.VISIBLE
        // Очищаем поля
        binding.etLoginUsername.text.clear()
        binding.etLoginPassword.text.clear()
    }
}