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
    val limitUgx: Double
)

class SpendLimitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SpendLimitRepository(database)

    val limits: StateFlow<List<SpendingLimitEntity>> = repository.allLimits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<WithdrawalRuleEntity>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<UgandaAccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionRecordEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isAutomationActive: StateFlow<Boolean> = repository.isAutomationEngineActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // UI Dialog & Sheet States
    private val _withdrawalAlert = MutableStateFlow<AutoWithdrawalEvent?>(null)
    val withdrawalAlert: StateFlow<AutoWithdrawalEvent?> = _withdrawalAlert.asStateFlow()

    private val _showAddLimitDialog = MutableStateFlow(false)
    val showAddLimitDialog: StateFlow<Boolean> = _showAddLimitDialog.asStateFlow()

    private val _showAddRuleDialog = MutableStateFlow(false)
    val showAddRuleDialog: StateFlow<Boolean> = _showAddRuleDialog.asStateFlow()

    private val _showSimulateExpenseDialog = MutableStateFlow(false)
    val showSimulateExpenseDialog: StateFlow<Boolean> = _showSimulateExpenseDialog.asStateFlow()

    private val _selectedLimitForExpense = MutableStateFlow<SpendingLimitEntity?>(null)
    val selectedLimitForExpense: StateFlow<SpendingLimitEntity?> = _selectedLimitForExpense.asStateFlow()

    private val _selectedTransaction = MutableStateFlow<TransactionRecordEntity?>(null)
    val selectedTransaction: StateFlow<TransactionRecordEntity?> = _selectedTransaction.asStateFlow()

    private val _ussdPromptState = MutableStateFlow<String?>(null)
    val ussdPromptState: StateFlow<String?> = _ussdPromptState.asStateFlow()

    fun dismissWithdrawalAlert() {
        _withdrawalAlert.value = null
    }

    fun openAddLimitDialog() {
        _showAddLimitDialog.value = true
    }

    fun closeAddLimitDialog() {
        _showAddLimitDialog.value = false
    }

    fun openAddRuleDialog() {
        _showAddRuleDialog.value = true
    }

    fun closeAddRuleDialog() {
        _showAddRuleDialog.value = false
    }

    fun openExpenseDialog(limit: SpendingLimitEntity? = null) {
        _selectedLimitForExpense.value = limit
        _showSimulateExpenseDialog.value = true
    }

    fun closeExpenseDialog() {
        _selectedLimitForExpense.value = null
        _showSimulateExpenseDialog.value = false
    }

    fun selectTransaction(tx: TransactionRecordEntity?) {
        _selectedTransaction.value = tx
    }

    fun dismissUssdPrompt() {
        _ussdPromptState.value = null
    }

    fun toggleAutomationActive() {
        val current = isAutomationActive.value
        repository.setAutomationEngineActive(!current)
    }

    fun toggleRule(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setRuleEnabled(ruleId, isEnabled)
        }
    }

    fun recordExpense(limitId: Long, amountUgx: Double, title: String, sourceAccount: String) {
        viewModelScope.launch {
            val triggered = repository.recordExpense(limitId, amountUgx, title, sourceAccount)
            val limit = limits.value.find { it.id == limitId }
            if (triggered.isNotEmpty() && limit != null) {
                _withdrawalAlert.value = AutoWithdrawalEvent(
                    triggeredRules = triggered,
                    limitTitle = limit.title,
                    spentAmountUgx = amountUgx,
                    totalSpentUgx = limit.currentSpentUgx + amountUgx,
                    limitUgx = limit.limitAmountUgx
                )
            }
        }
    }

    fun executeRuleTest(rule: WithdrawalRuleEntity) {
        viewModelScope.launch {
            val tx = repository.executeWithdrawal(rule)
            _selectedTransaction.value = tx
        }
    }

    fun createLimit(
        title: String,
        category: String,
        period: String,
        amountUgx: Double,
        warningPercent: Int,
        autoWithdrawPercent: Int
    ) {
        viewModelScope.launch {
            repository.insertLimit(
                SpendingLimitEntity(
                    title = title,
                    category = category,
                    period = period,
                    limitAmountUgx = amountUgx,
                    currentSpentUgx = 0.0,
                    warningThresholdPercent = warningPercent,
                    autoWithdrawTriggerPercent = autoWithdrawPercent,
                    isActive = true
                )
            )
            closeAddLimitDialog()
        }
    }

    fun deleteLimit(limit: SpendingLimitEntity) {
        viewModelScope.launch {
            repository.deleteLimit(limit)
        }
    }

    fun createRule(
        ruleName: String,
        linkedLimitId: Long,
        limitTitle: String,
        triggerType: String,
        sourceType: String,
        sourceId: String,
        sourceName: String,
        destinationType: String,
        destinationName: String,
        withdrawalAmountType: String,
        amountUgx: Double,
        method: String
    ) {
        viewModelScope.launch {
            repository.insertRule(
                WithdrawalRuleEntity(
                    ruleName = ruleName,
                    linkedLimitId = linkedLimitId,
                    limitTitle = limitTitle,
                    triggerType = triggerType,
                    sourceType = sourceType,
                    sourceId = sourceId,
                    sourceName = sourceName,
                    destinationType = destinationType,
                    destinationName = destinationName,
                    withdrawalAmountType = withdrawalAmountType,
                    amountUgx = amountUgx,
                    method = method,
                    isEnabled = true
                )
            )
            closeAddRuleDialog()
        }
    }

    fun deleteRule(rule: WithdrawalRuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun resetAllLimits() {
        viewModelScope.launch {
            repository.resetAllSpending()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun dialUgandaUssd(context: Context, ussdCode: String, providerName: String) {
        try {
            val encodedUssd = Uri.encode(ussdCode)
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encodedUssd"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            _ussdPromptState.value = "Launched $providerName ($ussdCode) Mobile Money session"
            Toast.makeText(context, "Dialing $ussdCode on $providerName", Toast.LENGTH_SHORT).show()
        }
    }
}
