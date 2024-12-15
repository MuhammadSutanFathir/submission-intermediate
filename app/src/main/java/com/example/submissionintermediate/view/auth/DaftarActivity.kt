package com.example.submissionintermediate.view.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.submissionintermediate.databinding.ActivityDaftarBinding
import com.example.submissionintermediate.viewmodel.MainViewModel
import com.example.submissionintermediate.viewmodel.ViewModelFactory

class DaftarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDaftarBinding

    private val registerViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getAuthInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
        setupView()
        setupAction()
        observeViewModel()
        playAnimation()

    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
    private fun setupAction() {
        binding.daftarButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                registerViewModel.register(name, email, password)
            } else {
                showToast("Please fill in all fields")
            }
        }
    }

    private fun observeViewModel() {
        registerViewModel.isRegisterSuccessful.observe(this) { isSuccessful ->
            if (isSuccessful) {
                showToast("Registration successful!")
                navigateToLogin()
            }
        }

        registerViewModel.errorMessage.observe(this) { error ->
            error?.let {
                showErrorDialog(it)
                registerViewModel.clearErrorMessage()
            }
        }

        registerViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun navigateToLogin() {
        Intent(this, LoginActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
        finish()
    }

    private fun showErrorDialog(message: String) {
        showLoading(false)
        AlertDialog.Builder(this).apply {
            setTitle("Error")
            setMessage(message)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.daftarLogo, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        ObjectAnimator.ofFloat(binding.roundedView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val nameText = ObjectAnimator.ofFloat(binding.fullNameEditText, View.ALPHA, 1f).setDuration(100)
        val emailText = ObjectAnimator.ofFloat(binding.emailEditText, View.ALPHA, 1f).setDuration(100)
        val passwordText =
            ObjectAnimator.ofFloat(binding.passwordEditText, View.ALPHA, 1f).setDuration(100)
        val name = ObjectAnimator.ofFloat(binding.edRegisterName, View.ALPHA, 1f).setDuration(100)
        val email = ObjectAnimator.ofFloat(binding.edRegisterEmail, View.ALPHA, 1f).setDuration(100)
        val password =
            ObjectAnimator.ofFloat(binding.edRegisterPassword, View.ALPHA, 1f).setDuration(100)
        val linear =
            ObjectAnimator.ofFloat(binding.linearLayoutRegister, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.daftarButton, View.ALPHA, 1f).setDuration(100)

        val together = AnimatorSet().apply {
            playTogether(name, nameText,email,emailText,passwordText,password)
        }
        AnimatorSet().apply {
            playSequentially(
                together,
                login,
                linear
            )

            startDelay = 100
        }.start()
    }
}