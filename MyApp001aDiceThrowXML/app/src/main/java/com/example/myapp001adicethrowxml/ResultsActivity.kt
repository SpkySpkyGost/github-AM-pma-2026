package com.example.myapp001adicethrowxml

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Results page: reads all saved rolls from SQLite and shows them in a list. */
class ResultsActivity : AppCompatActivity() {

    private val diceSymbols = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    private lateinit var dbHelper: ResultsDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_results)

        dbHelper = ResultsDbHelper(this)

        val llResults = findViewById<LinearLayout>(R.id.llResults)
        val lvResults = findViewById<ListView>(R.id.lvResults)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Keep the page clear of the system bars
        ViewCompat.setOnApplyWindowInsetsListener(llResults) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button returns to the dice screen
        btnBack.setOnClickListener { finish() }

        // Load rows on a background thread, then show them on the main thread
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) { dbHelper.getAllResults() }

            // Each row as text: "#id   ⚃ 4   sázka 2 ✗   07.10.2026 13:30:12"
            val rows = results.map { item ->
                getString(
                    R.string.result_row,
                    item.id,
                    diceSymbols[item.result - 1],
                    item.result,
                    item.bet,
                    if (item.isWin) "✓" else "✗",
                    dateFormat.format(Date(item.timestamp))
                )
            }
            lvResults.adapter = ArrayAdapter(this@ResultsActivity, android.R.layout.simple_list_item_1, rows)
            tvEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
