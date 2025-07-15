package com.example.anychat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.anychat.databinding.ActivityRegisterBinding
import com.example.anychat.viewmodel.AuthViewModel
import com.example.anychat.data.AuthRepository
import com.example.anychat.ui.users.UsersListActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(AuthRepository()) as T
            }
        }
    }
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Session check: if already logged in, go to UsersListActivity
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, UsersListActivity::class.java))
            finish()
            return
        }
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textRegisterTitle.text = "Register"
        binding.textAppTitle.text = "AnyChat"

        // Password visibility toggle
        binding.imageTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.imageTogglePassword.setImageResource(com.example.anychat.R.drawable.ic_visibility)
            } else {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.imageTogglePassword.setImageResource(com.example.anychat.R.drawable.ic_visibility_off)
            }
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                showError("Please enter both email and password.")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showError("Password must be at least 6 characters.")
                return@setOnClickListener
            }
            binding.textRegisterError.visibility = View.GONE
            viewModel.register(email, password)
        }

        viewModel.authResult.observe(this) { result ->
            result.onSuccess {
                showSuccess("Registration successful! Please login.")
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }.onFailure {
                showError("Registration failed: ${it.message}")
            }
        }

        binding.btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            finish()
        }
    }

    private fun showError(message: String) {
        binding.textRegisterError.text = message
        binding.textRegisterError.visibility = View.VISIBLE
        fadeIn(binding.textRegisterError)
        binding.textRegisterSuccess.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        binding.textRegisterSuccess.text = message
        binding.textRegisterSuccess.visibility = View.VISIBLE
        fadeIn(binding.textRegisterSuccess)
        binding.textRegisterError.visibility = View.GONE
    }

    private fun fadeIn(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 400
        view.startAnimation(fadeIn)
    }
} 