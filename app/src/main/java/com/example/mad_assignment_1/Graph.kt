package com.example.mad_assignment_1

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomnavigation.BottomNavigationView

class Graph : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graph)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        @Suppress("DEPRECATION")
        val transactions = intent.getParcelableArrayListExtra<Transaction>("transactions") ?: arrayListOf()

        val barChart = findViewById<BarChart>(R.id.barChart)
        val emptyText = findViewById<TextView>(R.id.chart_empty_text)

        if (transactions.isEmpty()) {
            barChart.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            barChart.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
            setupBarChart(barChart, transactions)
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav_bar)
        bottomNav.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item_icon_color)
        bottomNav.selectedItemId = R.id.nav_graph
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_graph -> true
                else -> false
            }
        }
    }

    private fun setupBarChart(barChart: BarChart, transactions: List<Transaction>) {
        val barEntries = transactions.mapIndexed { index, transaction ->
            val amt = transaction.amount.toDoubleOrNull() ?: 0.0
            BarEntry(index.toFloat(), Math.abs(amt).toFloat())
        }

        val colors = listOf(
            Color.parseColor("#1A237E"), Color.parseColor("#FF6202"),
            Color.parseColor("#0F9D58"), Color.parseColor("#D06BED"),
            Color.parseColor("#00BCD4"), Color.parseColor("#F44336"),
            Color.parseColor("#FF9800")
        )

        val dataSet = BarDataSet(barEntries, "Expenses").apply {
            valueTextSize = 11f
            valueTextColor = Color.DKGRAY
            val repeated = mutableListOf<Int>()
            repeat((transactions.size / colors.size) + 1) { repeated.addAll(colors) }
            this.colors = repeated.take(transactions.size)
        }

        barChart.data = BarData(dataSet).apply { barWidth = 0.5f }
        barChart.apply {
            description.isEnabled = false
            axisLeft.apply {
                textColor = Color.DKGRAY
                isInverted = true // Invert Y-axis to match the downward bars in the screenshot
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            extraBottomOffset = 30f // Increased space for category titles at the bottom

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.DKGRAY
                textSize = 12f
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false) // Cleaner look for category labels
                setDrawLabels(true)
                // Use the custom formatter to show transaction titles
                valueFormatter = XAxisValueFormatter(transactions)
                labelCount = transactions.size
            }
            animateY(600)
            invalidate()
        }
    }
}

class XAxisValueFormatter(private val transactions: List<Transaction>) :
    com.github.mikephil.charting.formatter.IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float, axis: AxisBase?): String? {
        val index = Math.round(value)
        return if (index >= 0 && index < transactions.size) {
            transactions[index].title
        } else {
            ""
        }
    }
}