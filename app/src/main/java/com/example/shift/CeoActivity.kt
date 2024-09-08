package com.example.shift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.Intent
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import android.app.DatePickerDialog
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import android.os.Environment

class CeoActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerOptions: Spinner
    private lateinit var spinnerPracownicy: Spinner
    private lateinit var acceptButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var tvFromDate: TextView
    private lateinit var tvToDate: TextView
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ceo)

        sessionManager = SessionManager(this)
        databaseHelper = DatabaseHelper(this)

        // Check if the user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
        }

        spinnerOptions = findViewById(R.id.idSpinnerOptions)
        spinnerPracownicy = findViewById(R.id.idSpinnerPracownicy)
        acceptButton = findViewById(R.id.idButtonAccept)

        tvFromDate = findViewById(R.id.tvFromDate)
        tvToDate = findViewById(R.id.tvToDate)

        tvFromDate.setOnClickListener { showDatePickerDialog(true) }
        tvToDate.setOnClickListener { showDatePickerDialog(false) }

        acceptButton.setOnClickListener {
            handleAction()
        }
    }



    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish this activity to prevent the user from coming back to it
    }

    private fun handleAction() {
        // Check if dates are selected
        val startDate = tvFromDate.text.toString()
        val endDate = tvToDate.text.toString()
        val selectedUser = spinnerPracownicy.selectedItem.toString()
        val selectedLocation = spinnerOptions.selectedItem.toString()

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Wybierz daty!", Toast.LENGTH_SHORT).show()
            return
        }


        // Fetch results based on the selected criteria
        val results: List<Result> = when {
            // User selected both location and pracownik
            selectedLocation != "-Lokalizacja-" && selectedUser != "-Pracownik-" -> {
                databaseHelper.getResultsForUserAndLocationBetweenDates(selectedUser, selectedLocation, startDate, endDate)
            }
            // User selected only location
            selectedLocation != "-Lokalizacja-" -> {
                databaseHelper.getResultsForLocationBetweenDates(selectedLocation, startDate, endDate)
            }
            // User selected only pracownik
            selectedUser != "-Pracownik-" -> {
                databaseHelper.getResultsForUserBetweenDates(selectedUser, startDate, endDate)
            }
            // Neither location nor pracownik is selected
            else -> {
                databaseHelper.getResultsBetweenDates(startDate, endDate)
            }
        }



        // Define the file path to the Downloads folder
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "results_${System.currentTimeMillis()}.xlsx"
        val filePath = File(downloadsDir, fileName).absolutePath

        // Export results to Excel
        exportResultsToExcel(results, filePath)

        // Notify user
        Toast.makeText(this, "Exported to $filePath", Toast.LENGTH_LONG).show()
    }


    private fun showDatePickerDialog(isFromDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time

                val formattedDate = dateFormat.format(selectedDate)
                if (isFromDate) {
                    tvFromDate.text = formattedDate
                } else {
                    tvToDate.text = formattedDate
                }
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

}
