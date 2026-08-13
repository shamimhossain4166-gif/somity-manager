package com.example.data.local

import androidx.room.*
import com.example.data.model.CommitteeMember
import com.example.data.model.Member
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface SomityDao {
    @Query("SELECT * FROM members ORDER BY memberNo ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Long): Flow<Member?>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberByIdDirect(id: Long): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<Member>)

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("SELECT * FROM payments WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getPaymentsForMember(memberId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE memberId = :memberId ORDER BY timestamp DESC")
    suspend fun getPaymentsForMemberDirect(memberId: Long): List<Payment>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    suspend fun getAllPaymentsDirect(): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<Payment>)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM committee_members ORDER BY rankOrder ASC")
    fun getCommitteeMembers(): Flow<List<CommitteeMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitteeMembers(members: List<CommitteeMember>)

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    @Query("DELETE FROM committee_members")
    suspend fun deleteAllCommitteeMembers()
}
