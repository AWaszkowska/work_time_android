package com.example.shift

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.shift.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.signupButton.setOnClickListener {
            val signupUsername = binding.signupUsername.text.toString()
            val signupPassword = binding.signupPassword.text.toString()
            if (isPasswordValid(signupPassword)) {
                signupDatabase(signupUsername, signupPassword)
            } else {
                Toast.makeText(this, "Hasło musi się składać z conajmniej 8 znaków, w tym jednej cyfry i jednego znaku specjalnego", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        // Regex for at least 8 characters, one digit, and one special character
        val passwordPattern = "^(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z0-9!@#\$%^&*(),.?\":{}|<>]{8,}\$"
        return password.matches(passwordPattern.toRegex())
    }

    private fun signupDatabase(username: String, password: String){
        val insertedRowId = databaseHelper.insertUser(username, password, false)
        if (insertedRowId != -1L){
            Toast.makeText(this, "Zarejestrowano", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else{
            Toast.makeText(this, "Rejestracja nie powiodła się", Toast.LENGTH_SHORT).show()
        }
    }
}
