package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.TransactionRecordEntity
import com.example.data.model.UgandaAccountEntity
import com.example.data.model.WithdrawalRuleEntity
import com.example.data.repository.SpendLimitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AutoWithdrawalEvent(
    val triggeredRules: List<WithdrawalRuleEntity>,
    val limitTitle: String,
    val spentAmountUgx: Double,
    val totalSpentUgx: Double,
    val triggeredAt: Long = System.currentTimeMillis()
)

class SpendLimitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SpendLimitRepository(
        db.spendingLimitDao(),
        db.withdrawalRuleDao(),
        db.transactionDao(),
        db.ugandaAccountDao()
    )

    private val _limits = MutableStateFlow<List<SpendingLimitEntity>>(emptyList())
    val limits: StateFlow<List<SpendingLimitEntity>> = _limits.asStateFlow()

    val allLimits: StateFlow<List<SpendingLimitEntity>> = repository.allLimits.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _autoWithdrawalEvent = MutableStateFlow<AutoWithdrawalEvent?>(null)
    val autoWithdrawalEvent = _autoWithdrawalEvent.asStateFlow()

    fun loadLimits() {
        viewModelScope.launch {
            _limits.value = repository.getAllLimitsNow()
        }
    }

    // FIXED: line 141 and others - wrapped in launch
    fun addLimit(limit: SpendingLimitEntity) {
        viewModelScope.launch {
            repository.insertLimit(limit)
            loadLimits()
        }
    }

    fun removeLimit(limit: SpendingLimitEntity) {
        viewModelScope.launch {
            repository.deleteLimit(limit)
            loadLimits()
        }
    }

    fun addRule(rule: WithdrawalRuleEntity) {
        viewModelScope.launch {
            repository.insertRule(rule)
        }
    }

    fun addTransaction(record: TransactionRecordEntity) {
        viewModelScope.launch {
            repository.insertTransaction(record)
        }
    }

    fun addUgandaAccount(account: UgandaAccountEntity) {
        viewModelScope.launch {
            repository.insertUgandaAccount(account)
        }
    }

    fun clearEvent() {
        _autoWithdrawalEvent.value = null
    }
}