package com.example.mad_assignment_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mad_assignment_1.data.AppDatabase
import com.example.mad_assignment_1.ui.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.mad_assignment_1.repository.TransactionRepository
import com.example.mad_assignment_1.ui.TransactionViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var emptyTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private val transactionList = mutableListOf<Transaction>()
    private lateinit var addTransactionLauncher: ActivityResultLauncher<Intent>
    private lateinit var balanceTextView: TextView
    private lateinit var totalExpenseTextView: TextView
    private lateinit var incomeTextView: TextView
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bind views
        emptyTextView    = findViewById(R.id.empty_text)
        recyclerView     = findViewById(R.id.recyclerview)
        balanceTextView  = findViewById(R.id.input_text_1)
        totalExpenseTextView = findViewById(R.id.card_6_text_2)
        incomeTextView   = findViewById(R.id.card_5_text_2)

        // Show stored balance
        val storedBalance = getTotalBalance()
        balanceTextView.text = formatCurrency(storedBalance)

        // Set up RecyclerView adapter
        transactionAdapter = TransactionAdapter(transactionList) { transactionId ->
            confirmAndDelete(transactionId)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter

        // Set up ViewModel
        val dao = AppDatabase.getDatabase(this).transactionDao()
        val repository = TransactionRepository(dao)
        val factory = TransactionViewModelFactory(repository)
        transactionViewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        // Register launcher for AddExpenses result
        addTransactionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                @Suppress("DEPRECATION")
                val transaction = result.data?.getParcelableExtra<Transaction>("transaction")
                if (transaction != null) {
                    addTransaction(transaction)
                } else {
                    Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // FAB – open AddExpenses
        val addIcon: ImageButton = findViewById(R.id.add_icon)
        addIcon.setOnClickListener {
            val intent = Intent(this, AddExpenses::class.java)
            addTransactionLauncher.launch(intent)
        }

        // "New+" button – set total balance
        val newEntryButton = findViewById<Button>(R.id.btnNewEntry)
        newEntryButton.setOnClickListener { showBalanceInputDialog() }

        // Bottom navigation
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav_bar)
        bottomNav.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item_icon_color)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_graph -> {
                    val intent = Intent(this, Graph::class.java)
                    intent.putParcelableArrayListExtra("transactions", ArrayList(transactionList))
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Load persisted transactions
        loadTransactionsFromDatabase()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun updateEmptyState() {
        if (transactionList.isEmpty()) {
            emptyTextView.visibility = TextView.VISIBLE
            recyclerView.visibility  = RecyclerView.GONE
            totalExpenseTextView.text = formatCurrency(0.0)
            incomeTextView.text = formatCurrency(getTotalBalance())
        } else {
            emptyTextView.visibility = TextView.GONE
            recyclerView.visibility  = RecyclerView.VISIBLE
            updateSummaryCards()
        }
    }

    private fun updateSummaryCards() {
        val totalExpense = transactionList.sumOf {
            it.amount.toDoubleOrNull() ?: 0.0
        }
        // totalExpense is negative (all expenses stored as negative amounts)
        totalExpenseTextView.text = formatCurrency(totalExpense)

        val balance = getTotalBalance()
        val remaining = balance + totalExpense  // balance minus expenses
        incomeTextView.text = formatCurrency(remaining)
    }

    private fun formatCurrency(amount: Double): String {
        return if (amount < 0) {
            "-$${String.format("%,.2f", -amount)}"
        } else {
            "$${String.format("%,.2f", amount)}"
        }
    }

    private fun addTransaction(transaction: Transaction) {
        transactionList.add(0, transaction) // newest first
        transactionAdapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
        transactionViewModel.insertTransaction(transaction)
        updateEmptyState()
    }

    private fun confirmAndDelete(transactionId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ -> deleteTransaction(transactionId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTransaction(transactionId: Int) {
        val index = transactionList.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            transactionList.removeAt(index)
            transactionAdapter.notifyItemRemoved(index)
            transactionViewModel.deleteTransactionById(transactionId)
            updateEmptyState()
            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTransactionsFromDatabase() {
        transactionViewModel.getAllTransactions { transactions ->
            // callback already dispatched to Main thread by ViewModel
            transactionList.clear()
            transactionList.addAll(transactions)
            transactionAdapter.notifyDataSetChanged()
            updateEmptyState()
        }
    }

    private fun showBalanceInputDialog() {
        val input = EditText(this)
        input.hint = "e.g. 20000"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        AlertDialog.Builder(this)
            .setTitle("Set Total Balance")
            .setMessage("Enter your total balance:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().toDoubleOrNull()
                if (value != null && value >= 0) {
                    saveTotalBalance(value)
                    balanceTextView.text = formatCurrency(value)
                    updateSummaryCards()
                } else {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveTotalBalance(balance: Double) {
        getSharedPreferences("BalancePrefs", MODE_PRIVATE)
            .edit().putFloat("totalBalance", balance.toFloat()).apply()
    }

    private fun getTotalBalance(): Double {
        return getSharedPreferences("BalancePrefs", MODE_PRIVATE)
            .getFloat("totalBalance", 0f).toDouble()
    }
}
