package com.example.myapp004objednavka

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// SQLite databaze s objednavkami (soubor bike_orders.db v datech aplikace)
class OrderDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_ORDERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BIKE INTEGER NOT NULL,
                $COL_FORK INTEGER NOT NULL,
                $COL_SADDLE INTEGER NOT NULL,
                $COL_HANDLEBAR INTEGER NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        onCreate(db)
    }

    fun insertOrder(order: Order): Long =
        writableDatabase.insert(TABLE_ORDERS, null, order.toContentValues())

    // nejnovejsi zaznamy nahore
    fun getAllOrders(): List<Order> {
        val orders = mutableListOf<Order>()
        readableDatabase.query(TABLE_ORDERS, null, null, null, null, null, "$COL_ID DESC").use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COL_ID)
            val bikeIndex = cursor.getColumnIndexOrThrow(COL_BIKE)
            val forkIndex = cursor.getColumnIndexOrThrow(COL_FORK)
            val saddleIndex = cursor.getColumnIndexOrThrow(COL_SADDLE)
            val handlebarIndex = cursor.getColumnIndexOrThrow(COL_HANDLEBAR)
            val createdAtIndex = cursor.getColumnIndexOrThrow(COL_CREATED_AT)
            while (cursor.moveToNext()) {
                orders.add(
                    Order(
                        id = cursor.getLong(idIndex),
                        bike = cursor.getInt(bikeIndex),
                        fork = cursor.getInt(forkIndex) == 1,
                        saddle = cursor.getInt(saddleIndex) == 1,
                        handlebar = cursor.getInt(handlebarIndex) == 1,
                        createdAt = cursor.getLong(createdAtIndex)
                    )
                )
            }
        }
        return orders
    }

    fun updateOrder(order: Order): Int =
        writableDatabase.update(TABLE_ORDERS, order.toContentValues(), "$COL_ID = ?", arrayOf(order.id.toString()))

    fun deleteOrder(id: Long): Int =
        writableDatabase.delete(TABLE_ORDERS, "$COL_ID = ?", arrayOf(id.toString()))

    private fun Order.toContentValues() = ContentValues().apply {
        put(COL_BIKE, bike)
        put(COL_FORK, if (fork) 1 else 0)
        put(COL_SADDLE, if (saddle) 1 else 0)
        put(COL_HANDLEBAR, if (handlebar) 1 else 0)
        put(COL_CREATED_AT, createdAt)
    }

    companion object {
        private const val DATABASE_NAME = "bike_orders.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_ORDERS = "orders"
        private const val COL_ID = "_id"
        private const val COL_BIKE = "bike"
        private const val COL_FORK = "fork"
        private const val COL_SADDLE = "saddle"
        private const val COL_HANDLEBAR = "handlebar"
        private const val COL_CREATED_AT = "created_at"

        @Volatile
        private var instance: OrderDbHelper? = null

        // jedna instance pro celou aplikaci
        fun getInstance(context: Context): OrderDbHelper =
            instance ?: synchronized(this) {
                instance ?: OrderDbHelper(context.applicationContext).also { instance = it }
            }
    }
}
