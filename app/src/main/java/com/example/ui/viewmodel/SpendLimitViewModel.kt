package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.TransactionRecordEntity
import com.example.data.model.UgandaAccountEntity
import com.example.data.model.WithdrawalRuleEntity
import com.example.data.repository.SpendLimitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AutoWithdrawalEvent(
    val triggeredRules: List<WithdrawalRuleEntity> = emptyList(),
    val limitTitle: String = "",
    val spentAmountUgx: Double = 0.0,
    val totalSpentUgx: Double = 0.0,
    val triggeredAt: Long = System.currentTimeMillis()
)

class SpendLimitViewModel(application: Application) : AndroidViewModel(application) {

    private val _limits = MutableStateFlow<List<SpendingLimitEntity>>(emptyList())
    val limits: StateFlow<List<SpendingLimitEntity>> = _limits.asStateFlow()
    val allLimits: StateFlow<List<SpendingLimitEntity>> = _limits.asStateFlow()

    private val _autoWithdrawalEvent = MutableStateFlow<AutoWithdrawalEvent?>(null)
    val autoWithdrawalEvent = _autoWithdrawalEvent.asStateFlow()

    fun loadLimits() {}
    fun addLimit(limit: SpendingLimitEntity) { viewModelScope.launch {} }
    fun removeLimit(limit: SpendingLimitEntity) { viewModelScope.launch {} }
    fun addRule(rule: WithdrawalRuleEntity) { viewModelScope.launch {} }
    fun addTransaction(record: TransactionRecordEntity) { viewModelScope.launch {} }
    fun addUgandaAccount(account: UgandaAccountEntity) { viewModelScope.launch {} }
    fun clearEvent() { _autoWithdrawalEvent.value = null }
}