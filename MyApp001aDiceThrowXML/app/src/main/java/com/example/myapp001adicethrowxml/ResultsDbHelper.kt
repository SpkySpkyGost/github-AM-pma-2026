package com.example.myapp001adicethrowxml

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** One saved roll: database row id, rolled value (1-6), the number the player bet on and time in milliseconds. */
data class GameResult(val id: Long, val result: Int, val bet: Int, val timestamp: Long) {
    val isWin: Boolean get() = result == bet
}

/**
 * SQLite database with one table "results" (id, result, bet, timestamp).
 * SQLiteOpenHelper creates the database file on first use and opens it later.
 * Call its methods off the main thread (e.g. with Dispatchers.IO).
 */
class ResultsDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Called once, when the database file does not exist yet
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_RESULTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_RESULT INTEGER NOT NULL, " +
                "$COLUMN_BET INTEGER NOT NULL, " +
                "$COLUMN_TIMESTAMP INTEGER NOT NULL)"
        )
    }

    // Called when DATABASE_VERSION increases; here we simply recreate the table
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESULTS")
        onCreate(db)
    }

    /** Saves one roll (result and the player's bet) with the current time and returns the new row id. */
    fun insertResult(result: Int, bet: Int): Long {
        val values = ContentValues().apply {
            put(COLUMN_RESULT, result)
            put(COLUMN_BET, bet)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        return writableDatabase.insert(TABLE_RESULTS, null, values)
    }

    /** Returns all saved rolls, newest first. */
    fun getAllResults(): List<GameResult> {
        val results = mutableListOf<GameResult>()
        readableDatabase.query(
            TABLE_RESULTS,
            arrayOf(COLUMN_ID, COLUMN_RESULT, COLUMN_BET, COLUMN_TIMESTAMP),
            null, null, null, null,
            "$COLUMN_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                results.add(
                    GameResult(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        result = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESULT)),
                        bet = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BET)),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                )
            }
        }
        return results
    }

    companion object {
        private const val DATABASE_NAME = "dice_results.db"
        // Version 2 added the "bet" column (onUpgrade recreates the table)
        private const val DATABASE_VERSION = 2
        const val TABLE_RESULTS = "results"
        const val COLUMN_ID = "id"
        const val COLUMN_RESULT = "result"
        const val COLUMN_BET = "bet"
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}
