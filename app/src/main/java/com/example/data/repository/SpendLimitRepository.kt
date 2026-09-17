package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.TransactionRecordEntity
import com.example.data.model.UgandaAccountEntity
import com.example.data.model.WithdrawalRuleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

class SpendLimitRepository(private val database: AppDatabase) {

    private val limitDao = database.spendingLimitDao()
    private val ruleDao = database.withdrawalRuleDao()
    private val accountDao = database.ugandaAccountDao()
    private val txDao = database.transactionRecordDao()

    private val _isAutomationEngineActive = MutableStateFlow(true)
    val isAutomationEngineActive: Flow<Boolean> = _isAutomationEngineActive.asStateFlow()

    fun setAutomationEngineActive(active: Boolean) {
        _isAutomationEngineActive.value = active
    }

    val allLimits: Flow<List<SpendingLimitEntity>> = limitDao.getAllLimits()
    val allRules: Flow<List<WithdrawalRuleEntity>> = ruleDao.getAllRules()
    val allAccounts: Flow<List<UgandaAccountEntity>> = accountDao.getAllAccounts()
    val allTransactions: Flow<List<TransactionRecordEntity>> = txDao.getAllTransactions()
    val autoWithdrawals: Flow<List<TransactionRecordEntity>> = txDao.getAutoWithdrawals()

    suspend fun insertLimit(limit: SpendingLimitEntity): Long {
        return limitDao.insertLimit(limit)
    }

    suspend fun updateLimit(limit: SpendingLimitEntity) {
        limitDao.updateLimit(limit)
    }

    suspend fun deleteLimit(limit: SpendingLimitEntity) {
        limitDao.deleteLimit(limit)
    }

    suspend fun resetAllSpending() {
        limitDao.resetAllSpending()
    }

    suspend fun insertRule(rule: WithdrawalRuleEntity): Long {
        return ruleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: WithdrawalRuleEntity) {
        ruleDao.updateRule(rule)
    }

    suspend fun deleteRule(rule: WithdrawalRuleEntity) {
        ruleDao.deleteRule(rule)
    }

    suspend fun setRuleEnabled(ruleId: Long, isEnabled: Boolean) {
        ruleDao.setRuleEnabled(ruleId, isEnabled)
    }

    suspend fun updateAccount(account: UgandaAccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun clearHistory() {
        txDao.clearAll()
    }

    /**
     * Records a new expense against a spending limit and automatically checks
     * if the spending limit triggers an automated withdrawal rule from a bank or SIM card!
     */
    suspend fun recordExpense(
        limitId: Long,
        amountUgx: Double,
        expenseTitle: String,
        sourceAccountName: String
    ): List<WithdrawalRuleEntity> {
        val triggeredRules = mutableListOf<WithdrawalRuleEntity>()

        val limit = limitDao.getLimitById(limitId) ?: return emptyList()
        val newSpent = limit.currentSpentUgx + amountUgx
        val updatedLimit = limit.copy(currentSpentUgx = newSpent)
        limitDao.updateLimit(updatedLimit)

        // Log the expense transaction
        val expRef = "EXP-${Random.nextInt(10000, 99999)}"
        txDao.insertTransaction(
            TransactionRecordEntity(
                reference = expRef,
                ruleName = "Expense: ${limit.title}",
                type = "EXPENSE_LOG",
                sourceName = sourceAccountName,
                destinationName = limit.title,
                amountUgx = amountUgx,
                status = "COMPLETED",
                reason = expenseTitle
            )
        )

        // If automation engine is paused, do not auto-withdraw
        if (!_isAutomationEngineActive.value) {
            return emptyList()
        }

        // Check rules linked to this limit or global rules
        val candidateRules = ruleDao.getRulesForLimit(limitId)
        for (rule in candidateRules) {
            if (!rule.isEnabled) continue

            val shouldTrigger = when (rule.triggerType) {
                "ON_LIMIT_EXCEEDED" -> updatedLimit.isLimitExceeded
                "ON_WARNING_LEVEL" -> updatedLimit.isWarningReached
                else -> false
            }

            if (shouldTrigger) {
                executeWithdrawal(rule, updatedLimit)
                triggeredRules.add(rule)
            }
        }

        return triggeredRules
    }

    /**
     * Executes the actual withdrawal transaction from the Bank or SIM card
     * based on custom rule criteria.
     */
    suspend fun executeWithdrawal(
        rule: WithdrawalRuleEntity,
        triggeredLimit: SpendingLimitEntity? = null
    ): TransactionRecordEntity {
        val withdrawalAmount = when (rule.withdrawalAmountType) {
            "EXACT_OVERSPENT_AMOUNT" -> {
                if (triggeredLimit != null && triggeredLimit.currentSpentUgx > triggeredLimit.limitAmountUgx) {
                    triggeredLimit.currentSpentUgx - triggeredLimit.limitAmountUgx
                } else {
                    rule.amountUgx
                }
            }
            "PERCENTAGE_OF_LIMIT" -> {
                if (triggeredLimit != null) triggeredLimit.limitAmountUgx * 0.20 else rule.amountUgx
            }
            else -> rule.amountUgx
        }

        val ref = "UGX-WTH-${Random.nextInt(10000, 99999)}"
        val reasonText = if (triggeredLimit != null) {
            "Auto-withdrawn: ${triggeredLimit.title} reached ${triggeredLimit.progressPercent.toInt()}% of limit"
        } else {
            "Triggered manual execution of rule '${rule.ruleName}'"
        }

        // Deduct from source (SIM or Bank)
        accountDao.adjustBalance(rule.sourceId, -withdrawalAmount)

        // Credit to destination (Vault, Bank, or SIM)
        val destId = when (rule.destinationType) {
            "LOCKED_SAVINGS_VAULT" -> "vault_safe"
            "BANK_ACCOUNT" -> "bank_stanbic"
            "SIM_CARD" -> "sim_mtn"
            else -> "vault_safe"
        }
        accountDao.adjustBalance(destId, withdrawalAmount)

        // Update last triggered timestamp
        ruleDao.updateLastTriggered(rule.id, System.currentTimeMillis())

        val tx = TransactionRecordEntity(
            reference = ref,
            ruleName = rule.ruleName,
            type = "AUTO_WITHDRAWAL",
            sourceName = rule.sourceName,
            destinationName = rule.destinationName,
            amountUgx = withdrawalAmount,
            status = "COMPLETED",
            reason = reasonText
        )
        txDao.insertTransaction(tx)
        return tx
    }

    companion object {
        fun formatUgx(amount: Double): String {
            val format = NumberFormat.getNumberInstance(Locale.US)
            return "UGX " + format.format(amount.toLong())
        }

        fun formatCompactUgx(amount: Double): String {
            return when {
                amount >= 1_000_000 -> String.format(Locale.US, "UGX %.1fM", amount / 1_000_000)
                amount >= 1_000 -> String.format(Locale.US, "UGX %.0fK", amount / 1_000)
                else -> "UGX ${amount.toInt()}"
            }
        }
    }
}
