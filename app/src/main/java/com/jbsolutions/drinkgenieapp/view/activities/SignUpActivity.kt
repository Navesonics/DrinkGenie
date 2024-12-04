package com.jbsolutions.drinkgenieapp.view.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jbsolutions.drinkgenieapp.R
import com.jbsolutions.drinkgenieapp.viewmodel.AuthViewModel

class SignUpActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val firstNameInput = findViewById<EditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<EditText>(R.id.lastNameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        // Observe the registration result
        authViewModel.registerResult.observe(this) { result ->
            val (success, message) = result
            if (success) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                finish() // Close the SignUpActivity
            } else {
                Toast.makeText(this, "Registration failed: $message", Toast.LENGTH_LONG).show()
            }
        }

        // Sign-up button click listener
        signupButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Call the ViewModel's register method
                authViewModel.register(email, password, firstName, lastName)
            }
        }

        // Login link click listener
        loginLink.setOnClickListener {
            finish() // Close the SignUpActivity and navigate back to LoginActivity
        }
    }
}
