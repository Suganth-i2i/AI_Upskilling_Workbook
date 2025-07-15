package com.example.anychat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.anychat.databinding.ActivityLoginBinding
import com.example.anychat.viewmodel.AuthViewModel
import com.example.anychat.data.AuthRepository
import com.example.anychat.ui.users.UsersListActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textLoginTitle.text = "Login"
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

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                showError("Please enter both email and password.")
                return@setOnClickListener
            }
            binding.textLoginError.visibility = View.GONE
            viewModel.login(email, password)
        }

        viewModel.authResult.observe(this) { result ->
            result.onSuccess {
                showSuccess("Login successful!")
                startActivity(Intent(this, UsersListActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }.onFailure {
                showError("Login failed: ${it.message}")
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun showError(message: String) {
        binding.textLoginError.text = message
        binding.textLoginError.visibility = View.VISIBLE
        fadeIn(binding.textLoginError)
        binding.textLoginSuccess.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        binding.textLoginSuccess.text = message
        binding.textLoginSuccess.visibility = View.VISIBLE
        fadeIn(binding.textLoginSuccess)
        binding.textLoginError.visibility = View.GONE
    }

    private fun fadeIn(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 400
        view.startAnimation(fadeIn)
    }
} 