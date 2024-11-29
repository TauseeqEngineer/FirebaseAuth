package com.android.firebaseauth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.firebaseauth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
        binding.btnLogin.setOnClickListener {
            loginUser()
            it.isClickable= false
        }
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }

    private fun showProgress() {
        binding.btnRegister.visibility = View.GONE
        binding.btnProgress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.btnRegister.visibility = View.VISIBLE
        binding.btnProgress.visibility = View.GONE
    }

    private fun registerUser() {
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()

        if (email.isEmpty()) {
            showToast("Enter Email Address")
        } else if (password.isEmpty()) {
            showToast("Enter Password")
        } else {
            try {
                showProgress()

                lifecycleScope.launch(Dispatchers.IO)
                {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main)
                    {
                        hideProgress()
                        checkLoggedInState()
                    }
                }

            } catch (exception: Exception) {
                hideProgress()
                showToast(exception.message.toString())
            }
        }
    }

    private fun loginUser() {
        val email = binding.edtLoginEmail.text.toString()
        val password = binding.edtLoginPassword.text.toString()

        if (email.isEmpty()) {
            binding.btnLogin.isClickable = true
            showToast("Enter Email Address")
        } else if (password.isEmpty()) {
            binding.btnLogin.isClickable = true
            showToast("Enter Password")
        } else {
            try {
                binding.btnLogin.isClickable = false
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        firebaseAuth.signInWithEmailAndPassword(email, password).await()
                        withContext(Dispatchers.Main) {
                            binding.btnLogin.isClickable = true
                            checkLoggedInState()
                        }
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        // Handle specific case of incorrect credentials
                        withContext(Dispatchers.Main) {
                            binding.btnLogin.isClickable = true
                            showToast("Incorrect email or password.")
                        }
                    } catch (e: Exception) {
                        // Catch other exceptions (network issues, etc.)
                        withContext(Dispatchers.Main) {
                            binding.btnLogin.isClickable = true
                            showToast("Error: ${e.message}")
                        }
                    }
                }
            } catch (exception: Exception) {
                binding.btnLogin.isClickable = true
                showToast("An unexpected error occurred: ${exception.message}")
            }
        }
    }


    private fun checkLoggedInState() {
        if (firebaseAuth.currentUser != null) {
            binding.tvLoggedInStatus.text = getString(R.string.you_are_logged_in)

        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}