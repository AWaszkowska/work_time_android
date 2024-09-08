package com.example.shift

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Toast
import com.example.shift.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        binding.loginButton.setOnClickListener {
            val loginUsername = binding.loginUsername.text.toString()
            val loginPassword = binding.loginPassword.text.toString()
            loginDatabase(loginUsername, loginPassword)
        }

        binding.signupRedirect.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginDatabase(username: String, password: String) {
        val userExists = databaseHelper.readUser(username, password)
        if (userExists) {
            val isSuperuser = databaseHelper.isSuperUser(username)

            sessionManager.createLoginSession(username)
            Toast.makeText(this, "Zalogowano pomyslnie", Toast.LENGTH_SHORT).show()

            val intent = if (isSuperuser) {
                Intent(this, CeoActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Błąd logowania", Toast.LENGTH_SHORT).show()
        }
    }
}
