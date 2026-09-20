package com.spendlimit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
data class VillageGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val meetingType: String,
    val interestRate: Double = 10.0,
    val chairperson: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val phone: String,
    val totalSavings: Double = 0.0
)

@Entity
data class Saving(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val groupId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)

@Entity
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val groupId: Long,
    val amount: Double,
    val interest: Double,
    val totalToPay: Double,
    val balance: Double,
    val status: String = "ACTIVE"
)

@Dao
interface VillageDao {
    @Query("SELECT * FROM VillageGroup ORDER BY createdAt DESC")
    fun getGroups(): Flow<List<VillageGroup>>
    @Insert
    suspend fun insertGroup(group: VillageGroup): Long
    @Query("SELECT * FROM Member WHERE groupId = :groupId")
    fun getMembers(groupId: Long): Flow<List<Member>>
    @Insert
    suspend fun insertMember(member: Member): Long
    @Insert
    suspend fun insertSaving(saving: Saving)
    @Insert
    suspend fun insertLoan(loan: Loan)
    @Query("SELECT * FROM Loan WHERE groupId = :groupId AND status = 'ACTIVE'")
    fun getActiveLoans(groupId: Long): Flow<List<Loan>>
}