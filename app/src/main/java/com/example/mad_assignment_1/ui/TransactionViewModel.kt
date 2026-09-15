package com.example.mad_assignment_1.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment_1.Transaction
import com.example.mad_assignment_1.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransaction(transaction)
        }
    }

    fun getAllTransactions(callback: (List<Transaction>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = repository.getAllTransactions()
            withContext(Dispatchers.Main) {
                callback(transactions)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransactionById(transactionId)
        }
    }
}