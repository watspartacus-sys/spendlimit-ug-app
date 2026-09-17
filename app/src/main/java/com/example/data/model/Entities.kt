package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spending_limits")
data class SpendingLimitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val period: String, // "DAILY", "WEEKLY", "MONTHLY"
    val limitAmountUgx: Double,
    val currentSpentUgx: Double = 0.0,
    val warningThresholdPercent: Int = 80,
    val autoWithdrawTriggerPercent: Int = 100,
    val isActive: Boolean = true
) {
    val progressPercent: Float
        get() = if (limitAmountUgx > 0) ((currentSpentUgx / limitAmountUgx) * 100).toFloat() else 0f

    val isWarningReached: Boolean
        get() = progressPercent >= warningThresholdPercent

    val isLimitExceeded: Boolean
        get() = progressPercent >= autoWithdrawTriggerPercent

    val remainingUgx: Double
        get() = (limitAmountUgx - currentSpentUgx).coerceAtLeast(0.0)
}

@Entity(tableName = "withdrawal_rules")
data class WithdrawalRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleName: String,
    val linkedLimitId: Long = 0,
    val limitTitle: String = "All Limits",
    val triggerType: String, // "ON_LIMIT_EXCEEDED", "ON_WARNING_LEVEL", "SCHEDULED_SWEEP"
    val sourceType: String, // "BANK_ACCOUNT", "SIM_CARD"
    val sourceId: String,
    val sourceName: String,
    val destinationType: String, // "LOCKED_SAVINGS_VAULT", "BANK_ACCOUNT", "SIM_CARD"
    val destinationName: String,
    val withdrawalAmountType: String, // "FIXED_AMOUNT", "EXACT_OVERSPENT_AMOUNT", "PERCENTAGE_OF_LIMIT"
    val amountUgx: Double,
    val method: String = "STK_PUSH_MOMO", // "STK_PUSH_MOMO", "USSD_PROMPT", "BANK_DIRECT_DEBIT"
    val isEnabled: Boolean = true,
    val lastTriggeredAt: Long = 0L
)

@Entity(tableName = "uganda_accounts")
data class UgandaAccountEntity(
    @PrimaryKey
    val id: String,
    val accountType: String, // "SIM_CARD", "BANK_ACCOUNT", "VAULT"
    val providerName: String, // "MTN Mobile Money", "Airtel Money", "Stanbic Bank", etc.
    val accountNumber: String, // "+256 772 123456", "9030018492"
    val balanceUgx: Double,
    val ussdCode: String = "", // "*165#", "*185#", "*290#"
    val simSlot: Int = -1, // 0 for SIM 1, 1 for SIM 2, -1 for Bank
    val isDirectDebitApproved: Boolean = true,
    val carrierLogoColorHex: Long = 0xFF006C4CL
)

@Entity(tableName = "transaction_records")
data class TransactionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reference: String,
    val ruleName: String,
    val type: String, // "AUTO_WITHDRAWAL", "EXPENSE_LOG", "MANUAL_SWEEP"
    val sourceName: String,
    val destinationName: String,
    val amountUgx: Double,
    val status: String, // "COMPLETED", "PENDING_CONFIRMATION", "FAILED"
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
