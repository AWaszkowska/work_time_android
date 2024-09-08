package com.example.shift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlertDialog
import android.content.Context
import android.content.Intent


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.shift.ui.theme.ShiftTheme
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast

class MainActivity : ComponentActivity() {
    private lateinit var currentTV: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerOptions: Spinner
    private lateinit var acceptButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
        }

        currentTV = findViewById(R.id.idTVCurrent)
        spinnerOptions = findViewById(R.id.idSpinnerOptions)
        acceptButton = findViewById(R.id.idButtonAccept)
        databaseHelper = DatabaseHelper(this)

        lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                val currentDateAndTime = dateFormat.format(Date())
                // Set the current date and time to our text view
                currentTV.text = currentDateAndTime
                // Wait for 1 second
                delay(1000)
            }
        }

        // button click listener
        acceptButton.setOnClickListener {
            insertResult()
        }
    }

    private fun insertResult() {
        // Retrieve the current timestamp from the TextView
        val currentTimestamp = currentTV.text.toString()
        // Retrieve the selected option from the Spinner
        val selectedOption = spinnerOptions.selectedItem.toString()

        // Assume you have a method to get the current username
        val currentUsername = getCurrentUsername() ?: return // Handle null case

        // Insert the result into the database
        val resultId = databaseHelper.insertResult(currentUsername, selectedOption, currentTimestamp)
        if (resultId != -1L) {
            // Successfully inserted
            showSuccessDialog(currentUsername)
        } else {
            showFailure()
        }
    }

    private fun showSuccessDialog(username: String) {
        AlertDialog.Builder(this)
            .setTitle("Powodzenie")
            .setMessage("Zapisano w bazie danych!")
            .setPositiveButton("Wyloguj") { dialog, _ ->
                // Handle log out
                logOut()
            }
            .setNegativeButton("Usuń dane") { dialog, _ ->
                // Handle delete last record
                val currentUsername = getCurrentUsername() ?: return@setNegativeButton
                val success = databaseHelper.deleteLastResult(currentUsername)
                if (success) {
                    Toast.makeText(this, "Ostatni zapis został usunięty.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nie udało się usunąć ostatniego zapisu.", Toast.LENGTH_SHORT).show()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun showFailure() {
        AlertDialog.Builder(this)
            .setTitle("Błąd")
            .setMessage("Błąd operacji danych. Spróbuj ponownie za kilka minut.")
            .setCancelable(true)
            .show()
    }

    private fun showSuccess() {
        AlertDialog.Builder(this)
            .setTitle("Usunięto")
            .setMessage("Pomyślnie usunięto dane")

            .setPositiveButton("Wyloguj") { dialog, _ ->
                // Handle log out
                logOut()
            }

            .setCancelable(false)
            .show()
    }

/*    private fun deleteLastRecord(username: String) {
        val lastResultId = databaseHelper.getLastResultIdForUser(username)
        if (lastResultId != null) {
            val rowsAffected = databaseHelper.deleteResultById(lastResultId)
            if (rowsAffected > 0) {
                // Successfully deleted
                // You might want to show a confirmation message or update the UI
            } else {
                showSuccess()
            }
        } else {
            showFailure()
        }
    }*/

    private fun getCurrentUsername(): String? {
        return sessionManager.getUserDetails()
    }

    private fun logOut() {
        sessionManager.logoutUser()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
