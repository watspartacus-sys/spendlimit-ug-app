package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.TransactionRecordEntity
import com.example.data.model.UgandaAccountEntity
import com.example.data.model.WithdrawalRuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SpendingLimitEntity::class,
        WithdrawalRuleEntity::class,
        UgandaAccountEntity::class,
        TransactionRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spendingLimitDao(): SpendingLimitDao
    abstract fun withdrawalRuleDao(): WithdrawalRuleDao
    abstract fun ugandaAccountDao(): UgandaAccountDao
    abstract fun transactionRecordDao(): TransactionRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uganda_spend_limit_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val accountDao = database.ugandaAccountDao()
                val limitDao = database.spendingLimitDao()
                val ruleDao = database.withdrawalRuleDao()
                val txDao = database.transactionRecordDao()

                // 1. Initial Uganda Accounts (SIMs & Banks)
                val initialAccounts = listOf(
                    UgandaAccountEntity(
                        id = "sim_mtn",
                        accountType = "SIM_CARD",
                        providerName = "MTN Mobile Money",
                        accountNumber = "+256 772 459 812",
                        balanceUgx = 185000.0,
                        ussdCode = "*165#",
                        simSlot = 0,
                        isDirectDebitApproved = true,
                        carrierLogoColorHex = 0xFFFFCC00L
                    ),
                    UgandaAccountEntity(
                        id = "sim_airtel",
                        accountType = "SIM_CARD",
                        providerName = "Airtel Money",
                        accountNumber = "+256 701 334 908",
                        balanceUgx = 94000.0,
                        ussdCode = "*185#",
                        simSlot = 1,
                        isDirectDebitApproved = true,
                        carrierLogoColorHex = 0xFFE50914L
                    ),
                    UgandaAccountEntity(
                        id = "bank_stanbic",
                        accountType = "BANK_ACCOUNT",
                        providerName = "Stanbic Bank Uganda",
                        accountNumber = "9030018472910 (Forest Mall Br)",
                        balanceUgx = 2450000.0,
                        ussdCode = "*290#",
                        simSlot = -1,
                        isDirectDebitApproved = true,
                        carrierLogoColorHex = 0xFF0033A0L
                    ),
                    UgandaAccountEntity(
                        id = "bank_centenary",
                        accountType = "BANK_ACCOUNT",
                        providerName = "Centenary Bank",
                        accountNumber = "31004829104 (Kampala Br)",
                        balanceUgx = 1200000.0,
                        ussdCode = "*211#",
                        simSlot = -1,
                        isDirectDebitApproved = true,
                        carrierLogoColorHex = 0xFF006837L
                    ),
                    UgandaAccountEntity(
                        id = "vault_safe",
                        accountType = "VAULT",
                        providerName = "Locked Savings Vault",
                        accountNumber = "UGX-SAFE-VAULT-01",
                        balanceUgx = 420000.0,
                        ussdCode = "",
                        simSlot = -1,
                        isDirectDebitApproved = true,
                        carrierLogoColorHex = 0xFF673AB7L
                    )
                )
                accountDao.insertAccounts(initialAccounts)

                // 2. Initial Spending Limits
                val limit1 = SpendingLimitEntity(
                    id = 1,
                    title = "Daily Total Spend",
                    category = "General",
                    period = "DAILY",
                    limitAmountUgx = 60000.0,
                    currentSpentUgx = 45000.0,
                    warningThresholdPercent = 80,
                    autoWithdrawTriggerPercent = 100,
                    isActive = true
                )
                val limit2 = SpendingLimitEntity(
                    id = 2,
                    title = "Food & Groceries",
                    category = "Dining",
                    period = "WEEKLY",
                    limitAmountUgx = 150000.0,
                    currentSpentUgx = 110000.0,
                    warningThresholdPercent = 75,
                    autoWithdrawTriggerPercent = 100,
                    isActive = true
                )
                val limit3 = SpendingLimitEntity(
                    id = 3,
                    title = "Transport & Boda / Fuel",
                    category = "Transport",
                    period = "WEEKLY",
                    limitAmountUgx = 80000.0,
                    currentSpentUgx = 55000.0,
                    warningThresholdPercent = 80,
                    autoWithdrawTriggerPercent = 100,
                    isActive = true
                )
                val limit4 = SpendingLimitEntity(
                    id = 4,
                    title = "Airtime, Data & Yaka Utilities",
                    category = "Utilities",
                    period = "MONTHLY",
                    limitAmountUgx = 100000.0,
                    currentSpentUgx = 35000.0,
                    warningThresholdPercent = 80,
                    autoWithdrawTriggerPercent = 100,
                    isActive = true
                )
                limitDao.insertLimit(limit1)
                limitDao.insertLimit(limit2)
                limitDao.insertLimit(limit3)
                limitDao.insertLimit(limit4)

                // 3. Initial Auto-Withdrawal Rules
                val rule1 = WithdrawalRuleEntity(
                    id = 1,
                    ruleName = "Stanbic Auto-Sweep on Daily Limit",
                    linkedLimitId = 1,
                    limitTitle = "Daily Total Spend",
                    triggerType = "ON_LIMIT_EXCEEDED",
                    sourceType = "BANK_ACCOUNT",
                    sourceId = "bank_stanbic",
                    sourceName = "Stanbic Bank UG (*2910)",
                    destinationType = "LOCKED_SAVINGS_VAULT",
                    destinationName = "Locked Savings Vault",
                    withdrawalAmountType = "FIXED_AMOUNT",
                    amountUgx = 20000.0,
                    method = "BANK_DIRECT_DEBIT",
                    isEnabled = true,
                    lastTriggeredAt = System.currentTimeMillis() - 86400000L
                )
                val rule2 = WithdrawalRuleEntity(
                    id = 2,
                    ruleName = "MTN MoMo Auto-Save to Vault",
                    linkedLimitId = 2,
                    limitTitle = "Food & Groceries",
                    triggerType = "ON_LIMIT_EXCEEDED",
                    sourceType = "SIM_CARD",
                    sourceId = "sim_mtn",
                    sourceName = "MTN MoMo (SIM 1)",
                    destinationType = "LOCKED_SAVINGS_VAULT",
                    destinationName = "Locked Savings Vault",
                    withdrawalAmountType = "FIXED_AMOUNT",
                    amountUgx = 15000.0,
                    method = "STK_PUSH_MOMO",
                    isEnabled = true,
                    lastTriggeredAt = 0L
                )
                val rule3 = WithdrawalRuleEntity(
                    id = 3,
                    ruleName = "Airtel SIM Auto-Withdraw to Bank",
                    linkedLimitId = 3,
                    limitTitle = "Transport & Boda / Fuel",
                    triggerType = "ON_LIMIT_EXCEEDED",
                    sourceType = "SIM_CARD",
                    sourceId = "sim_airtel",
                    sourceName = "Airtel Money (SIM 2)",
                    destinationType = "BANK_ACCOUNT",
                    destinationName = "Centenary Bank",
                    withdrawalAmountType = "FIXED_AMOUNT",
                    amountUgx = 25000.0,
                    method = "USSD_PROMPT",
                    isEnabled = true,
                    lastTriggeredAt = 0L
                )
                ruleDao.insertRule(rule1)
                ruleDao.insertRule(rule2)
                ruleDao.insertRule(rule3)

                // 4. Initial Transaction Records
                txDao.insertTransaction(
                    TransactionRecordEntity(
                        reference = "UGX-WTH-8821",
                        ruleName = "Stanbic Auto-Sweep on Daily Limit",
                        type = "AUTO_WITHDRAWAL",
                        sourceName = "Stanbic Bank UG",
                        destinationName = "Locked Savings Vault",
                        amountUgx = 20000.0,
                        status = "COMPLETED",
                        reason = "Daily Limit reached 100% threshold",
                        timestamp = System.currentTimeMillis() - 86400000L
                    )
                )
            }
        }
    }
}
