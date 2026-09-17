package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.TransactionRecordEntity
import com.example.data.model.UgandaAccountEntity
import com.example.data.model.WithdrawalRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendingLimitDao {
    @Query("SELECT * FROM spending_limits ORDER BY id ASC")
    fun getAllLimits(): Flow<List<SpendingLimitEntity>>

    @Query("SELECT * FROM spending_limits WHERE id = :id LIMIT 1")
    suspend fun getLimitById(id: Long): SpendingLimitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: SpendingLimitEntity): Long

    @Update
    suspend fun updateLimit(limit: SpendingLimitEntity)

    @Delete
    suspend fun deleteLimit(limit: SpendingLimitEntity)

    @Query("UPDATE spending_limits SET currentSpentUgx = currentSpentUgx + :amount WHERE id = :id")
    suspend fun addSpending(id: Long, amount: Double)

    @Query("UPDATE spending_limits SET currentSpentUgx = 0")
    suspend fun resetAllSpending()
}

@Dao
interface WithdrawalRuleDao {
    @Query("SELECT * FROM withdrawal_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<WithdrawalRuleEntity>>

    @Query("SELECT * FROM withdrawal_rules WHERE isEnabled = 1")
    suspend fun getEnabledRules(): List<WithdrawalRuleEntity>

    @Query("SELECT * FROM withdrawal_rules WHERE linkedLimitId = :limitId AND isEnabled = 1")
    suspend fun getRulesForLimit(limitId: Long): List<WithdrawalRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: WithdrawalRuleEntity): Long

    @Update
    suspend fun updateRule(rule: WithdrawalRuleEntity)

    @Delete
    suspend fun deleteRule(rule: WithdrawalRuleEntity)

    @Query("UPDATE withdrawal_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE withdrawal_rules SET lastTriggeredAt = :timestamp WHERE id = :id")
    suspend fun updateLastTriggered(id: Long, timestamp: Long)
}

@Dao
interface UgandaAccountDao {
    @Query("SELECT * FROM uganda_accounts ORDER BY accountType DESC, id ASC")
    fun getAllAccounts(): Flow<List<UgandaAccountEntity>>

    @Query("SELECT * FROM uganda_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): UgandaAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UgandaAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<UgandaAccountEntity>)

    @Update
    suspend fun updateAccount(account: UgandaAccountEntity)

    @Query("UPDATE uganda_accounts SET balanceUgx = balanceUgx + :amount WHERE id = :id")
    suspend fun adjustBalance(id: String, amount: Double)
}

@Dao
interface TransactionRecordDao {
    @Query("SELECT * FROM transaction_records ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionRecordEntity>>

    @Query("SELECT * FROM transaction_records WHERE type = 'AUTO_WITHDRAWAL' ORDER BY timestamp DESC")
    fun getAutoWithdrawals(): Flow<List<TransactionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecordEntity): Long

    @Query("DELETE FROM transaction_records")
    suspend fun clearAll()
}
