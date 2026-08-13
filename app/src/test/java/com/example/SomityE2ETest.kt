package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SomityDatabase
import com.example.data.model.Member
import com.example.data.model.MemberCalculation
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.data.repository.SomityRepository
import com.example.util.GoogleSheetSyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SomityE2ETest {

    private lateinit var database: SomityDatabase
    private lateinit var repository: SomityRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SomityDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SomityRepository(database.somityDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test 1 and 2 - add and edit test member`() = runBlocking {
        val testMember = Member(
            memberNo = "001",
            name = "করিম রহমান",
            phone = "01711223344",
            address = "চরপোতন",
            joinDate = "01/01/2025"
        )
        val id = repository.insertMember(testMember)
        assertTrue(id > 0)

        val members = repository.allMembers.first()
        assertEquals(1, members.size)
        assertEquals("করিম রহমান", members[0].name)

        // Edit member
        val updatedMember = members[0].copy(name = "করিম আহমেদ", phone = "01800000000")
        repository.updateMember(updatedMember)

        val updatedMembers = repository.allMembers.first()
        assertEquals("করিম আহমেদ", updatedMembers[0].name)
        assertEquals("01800000000", updatedMembers[0].phone)
    }

    @Test
    fun `test 3 and 4 - add test payment and verify in room database`() = runBlocking {
        val member = Member(memberNo = "002", name = "রহিম শেখ", phone = "01911223344")
        val memberId = repository.insertMember(member)

        val payment = Payment(
            memberId = memberId,
            paymentDate = "15/01/2025",
            monthYear = "জানুয়ারি ২০২৫",
            installmentCount = 1,
            amount = 2000.0,
            receiptNo = "REC-1001",
            remarks = "প্রথম কিস্তি"
        )
        val paymentId = repository.insertPayment(payment)
        assertTrue(paymentId > 0)

        val payments = repository.getPaymentsForMember(memberId).first()
        assertEquals(1, payments.size)
        assertEquals(2000.0, payments[0].amount, 0.01)
        assertEquals("REC-1001", payments[0].receiptNo)
    }

    @Test
    fun `test 13 and 14 - verify installment calculation and color coding`() {
        // Target: January 2025 (1 month expected)
        val expectedMonthsJan2025 = MemberCalculation.calculateTotalExpectedMonths(2025, 1)
        assertEquals(1, expectedMonthsJan2025)

        // Target: February 2025 (2 months expected)
        val expectedMonthsFeb2025 = MemberCalculation.calculateTotalExpectedMonths(2025, 2)
        assertEquals(2, expectedMonthsFeb2025)

        val dummyMember = Member(id = 1, memberNo = "001", name = "টেস্ট", phone = "01700000000")
        
        // Scenario 1: Paid 0 BDT -> OVERDUE
        val calcOverdue = MemberCalculation(
            member = dummyMember,
            payments = emptyList(),
            targetYear = 2025,
            targetMonth = 1
        )
        assertEquals(1, calcOverdue.dueInstallments)
        assertEquals(2000.0, calcOverdue.dueAmount, 0.01)
        assertEquals(PaymentStatus.OVERDUE, calcOverdue.status)

        // Scenario 2: Paid 2000 BDT -> PAID_UP_TO_DATE
        val p1 = Payment(id = 1, memberId = 1, paymentDate = "05/01/2025", monthYear = "01/2025", amount = 2000.0)
        val calcPaid = MemberCalculation(
            member = dummyMember,
            payments = listOf(p1),
            targetYear = 2025,
            targetMonth = 1
        )
        assertEquals(0, calcPaid.dueInstallments)
        assertEquals(0.0, calcPaid.dueAmount, 0.01)
        assertEquals(PaymentStatus.PAID_UP_TO_DATE, calcPaid.status)
    }

    @Test
    fun `test 5 and 6 and 7 - build sync json for google sheets`() = runBlocking {
        val m1 = Member(id = 1, memberNo = "001", name = "সদস্য ১", phone = "01700")
        repository.insertMember(m1)
        val p1 = Payment(id = 10, memberId = 1, paymentDate = "10/01/2025", monthYear = "01/2025", amount = 2000.0)
        repository.insertPayment(p1)

        val json = GoogleSheetSyncManager.buildSyncJson(repository)
        assertTrue(json.contains("\"members\":"))
        assertTrue(json.contains("\"payments\":"))
        assertTrue(json.contains("সদস্য ১"))
        assertTrue(json.contains("2000"))
    }
}
