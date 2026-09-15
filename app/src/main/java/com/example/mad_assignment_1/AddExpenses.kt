package com.example.mad_assignment_1

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import yuku.ambilwarna.AmbilWarnaDialog
import android.app.DatePickerDialog
import android.widget.ImageButton
import android.widget.TextView
import java.util.Calendar

class AddExpenses : AppCompatActivity() {
    private lateinit var categorySpinner: Spinner
    private lateinit var categoryEditText: EditText
    private var currentColor: Int = Color.parseColor("#D06BED")
    private lateinit var dateCard: MaterialCardView
    private lateinit var dateTextView: TextView


    private val icons = arrayOf(
        R.drawable.agriculture,
        R.drawable.airplane,
        R.drawable.fast_food,
        R.drawable.home,
        R.drawable.healthcare,
        R.drawable.shopping,
        R.drawable.mortarboard
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expenses)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // category name
        categoryEditText = findViewById(R.id.enter_category_name)

        // spinner
        categorySpinner = findViewById(R.id.category_spinner)
        val adapter = IconSpinnerAdapter(this, icons)
        categorySpinner.adapter = adapter

        // color picker
        val colorCard = findViewById<MaterialCardView>(R.id.color_1)
        colorCard.setOnClickListener {
            openColorPicker(colorCard)
        }

        // date picker – default to today
        dateCard = findViewById(R.id.date)
        dateTextView = dateCard.findViewById(R.id.date_text)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        dateTextView.text = "$day/${month + 1}/$year"
        dateCard.setOnClickListener { openDatePicker() }

        // save
        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener { saveTransaction() }

        // back button – simply close this activity
        val backIcon: ImageButton = findViewById(R.id.back_icon)
        backIcon.setOnClickListener { finish() }
    }

    private fun openColorPicker(colorCard: MaterialCardView) {
        val colorPicker = AmbilWarnaDialog(this, currentColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {}
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                currentColor = color
                colorCard.setCardBackgroundColor(currentColor)
            }
        })
        colorPicker.show()
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            dateTextView.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day).show()
    }

    private fun saveTransaction() {
        val expenseEditText: EditText = findViewById(R.id.enter_expenses)
        val expenseText = expenseEditText.text.toString().trim()
        val enteredCategoryName = categoryEditText.text.toString().trim()
        val selectedIconPosition = categorySpinner.selectedItemPosition
        val selectedIcon = icons[selectedIconPosition]

        if (expenseText.isEmpty() || enteredCategoryName.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = expenseText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        val newTransaction = Transaction(
            id = 0,
            iconResId = selectedIcon,
            title = enteredCategoryName,
            amount = "-$expenseText",
            date = dateTextView.text.toString(),
            iconColor = currentColor
        )

        val resultIntent = Intent().apply {
            putExtra("transaction", newTransaction)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
        Log.d("AddExpenses", "Transaction saved: $newTransaction")
    }
}