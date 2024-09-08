package com.example.shift

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*
import android.database.Cursor

class DatabaseHelper(private val context: Context):
            SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1

        // User table
        private const val TABLE_NAME = "data"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_IS_SUPERUSER = "isSuperuser"

        //Results table
        private const val RESULTS_TABLE_NAME = "results"
        private const val COLUMN_RESULT_ID = "result_id"
        private const val COLUMN_RESULT_USERNAME = "username"
        private const val COLUMN_LOCALIZATION = "localization"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create user table
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USERNAME TEXT, " +
                "$COLUMN_PASSWORD TEXT, " +
                "$COLUMN_IS_SUPERUSER INTEGER)")

        // Create results table
        val createResultsTableQuery = ("CREATE TABLE $RESULTS_TABLE_NAME (" +
                "$COLUMN_RESULT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_RESULT_USERNAME TEXT, " +
                "$COLUMN_LOCALIZATION TEXT, " +
                "$COLUMN_TIMESTAMP TEXT)")

        db?.execSQL(createTableQuery)
        db?.execSQL(createResultsTableQuery)

        val superUserQuery = ("INSERT INTO $TABLE_NAME ($COLUMN_USERNAME, $COLUMN_PASSWORD, $COLUMN_IS_SUPERUSER) " +
                "VALUES ('Andrzej_Waszkowski', 'zurawina!@tarragona90', 1)")
        db?.execSQL(superUserQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        val dropResultsTableQuery = "DROP TABLE IF EXISTS $RESULTS_TABLE_NAME"
        db?.execSQL(dropTableQuery)
        db?.execSQL(dropResultsTableQuery)
        onCreate(db)
    }

    // Helper method to convert date string to desired format
    private fun convertDateString(dateString: String, inputFormat: String = "dd/MM/yyyy", outputFormat: String = "yyyy-MM-dd HH:mm:ss"): String {
        val inputDateFormat = SimpleDateFormat(inputFormat, Locale.getDefault())
        val outputDateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
        val date = inputDateFormat.parse(dateString)
        return outputDateFormat.format(date!!)
    }

    // user related methods
    fun insertUser(username:String, password:String, isSuperuser: Boolean = false): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_IS_SUPERUSER, if (isSuperuser) 1 else 0)
        }
        val db = writableDatabase
        return db.insert(TABLE_NAME, null, values)
    }

    fun readUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)

        val userExists = cursor.count > 0
        cursor.close()

        if (userExists) {
            // Save the username to SessionManager
            val sessionManager = SessionManager(context)
            sessionManager.createLoginSession(username)
        }

        return userExists
    }

    fun isSuperUser(username: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_IS_SUPERUSER), selection, selectionArgs, null, null, null)

        var isSuperUser = false
        if (cursor.moveToFirst()) {
            isSuperUser = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SUPERUSER)) == 1
        }
        cursor.close()

        return isSuperUser
    }

    // result related methods
    fun insertResult(username: String, localization: String, timestamp: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_RESULT_USERNAME, username)
            put(COLUMN_LOCALIZATION, localization)
            put(COLUMN_TIMESTAMP, timestamp)
        }
        val db = writableDatabase
        return db.insert(RESULTS_TABLE_NAME, null, values)
    }

    // The code inside the if (cursor.moveToFirst()) { ... }
    // block is responsible for iterating through the results of the SQL query,
    // extracting the relevant data from each row


    fun getResultsBetweenDates(startDate: String, endDate: String): List<Result> {
        val db = readableDatabase
        val convertedStartDate = convertDateString(startDate)
        val convertedEndDate = convertDateString(endDate)
        val selection = "$COLUMN_TIMESTAMP BETWEEN ? AND ?"
        val selectionArgs = arrayOf(convertedStartDate, convertedEndDate)
        val cursor = db.query(RESULTS_TABLE_NAME, null, selection, selectionArgs, null, null, null)
        return extractResultsFromCursor(cursor)
    }

    fun getResultsForUserBetweenDates(username: String, startDate: String, endDate: String): List<Result> {
        val db = readableDatabase
        val convertedStartDate = convertDateString(startDate)
        val convertedEndDate = convertDateString(endDate)
        val selection = "$COLUMN_RESULT_USERNAME = ? AND $COLUMN_TIMESTAMP BETWEEN ? AND ?"
        val selectionArgs = arrayOf(username, convertedStartDate, convertedEndDate)
        val cursor = db.query(RESULTS_TABLE_NAME, null, selection, selectionArgs, null, null, null)
        return extractResultsFromCursor(cursor)
    }

    fun getAllResults(): List<Result> {
        val db = readableDatabase
        val cursor = db.query(RESULTS_TABLE_NAME, null, null, null, null, null, null)
        return extractResultsFromCursor(cursor)
    }

    fun getResultsForUser(username: String): List<Result> {
        val db = readableDatabase
        val selection = "$COLUMN_RESULT_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(RESULTS_TABLE_NAME, null, selection, selectionArgs, null, null, null)
        return extractResultsFromCursor(cursor)
    }

    fun getResultsForLocationBetweenDates(location: String, startDate: String, endDate: String): List<Result> {
        val db = readableDatabase
        val convertedStartDate = convertDateString(startDate)
        val convertedEndDate = convertDateString(endDate)
        val selection = "$COLUMN_LOCALIZATION = ? AND $COLUMN_TIMESTAMP BETWEEN ? AND ?"
        val selectionArgs = arrayOf(location, convertedStartDate, convertedEndDate)
        val cursor = db.query(RESULTS_TABLE_NAME, null, selection, selectionArgs, null, null, null)
        return extractResultsFromCursor(cursor)
    }

    fun getResultsForLocation(location: String): List<Result> {
        val db = readableDatabase
        val selection = "$COLUMN_LOCALIZATION = ?"
        val selectionArgs = arrayOf(location)
        val cursor = db.query(RESULTS_TABLE_NAME, null, selection, selectionArgs, null, null, null)
        return extractResultsFromCursor(cursor)
    }

    private fun extractResultsFromCursor(cursor: Cursor): List<Result> {
        val resultsList = mutableListOf<Result>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RESULT_ID))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESULT_USERNAME))
                val option = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCALIZATION))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))

                val result = Result(id, username, option, timestamp)
                resultsList.add(result)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return resultsList
    }

    fun getLastResultIdForUser(username: String): Long? {
        val db = readableDatabase
        val cursor = db.query(
            RESULTS_TABLE_NAME,
            arrayOf(COLUMN_RESULT_ID),
            "$COLUMN_RESULT_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            "$COLUMN_RESULT_ID DESC",
            "1"
        )

        val resultId: Long?
        if (cursor.moveToFirst()) {
            resultId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RESULT_ID))
        } else {
            resultId = null
        }
        cursor.close()
        return resultId
    }

    fun deleteResultById(id: Long) : Int {
        val db = writableDatabase
        return db.delete(RESULTS_TABLE_NAME, "$COLUMN_RESULT_ID = ?", arrayOf(id.toString()))
    }

    fun deleteLastResult(username: String): Boolean {
        val lastResultId = getLastResultIdForUser(username)
        return if (lastResultId != null) {
            deleteResultById(lastResultId) > 0
        } else {
            false
        }
    }

    fun getResultsForUserAndLocationBetweenDates(username: String, location: String, startDate: String, endDate: String): List<Result> {
        val db = readableDatabase

        // Convert dates to the desired format
        val convertedStartDate = convertDateString(startDate)
        val convertedEndDate = convertDateString(endDate)

        // Build the SQL query with both username and localization filtering and date range
        val selection = "$COLUMN_RESULT_USERNAME = ? AND $COLUMN_LOCALIZATION = ? AND $COLUMN_TIMESTAMP BETWEEN ? AND ?"
        val selectionArgs = arrayOf(username, location, convertedStartDate, convertedEndDate)

        // Execute the query
        val cursor = db.query(
            RESULTS_TABLE_NAME,
            null,  // null selects all columns
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        // Extract results from the cursor
        return extractResultsFromCursor(cursor)
    }


}

data class Result(val id: Long, val username: String, val localization: String, val timestamp: String)