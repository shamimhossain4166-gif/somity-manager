package com.example.data.repository

import com.example.data.local.SomityDao
import com.example.data.model.CommitteeMember
import com.example.data.model.Member
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

class SomityRepository(private val dao: SomityDao) {

    val allMembers: Flow<List<Member>> = dao.getAllMembers()
    val allPayments: Flow<List<Payment>> = dao.getAllPayments()
    val committeeMembers: Flow<List<CommitteeMember>> = dao.getCommitteeMembers()

    fun getMemberById(id: Long): Flow<Member?> = dao.getMemberById(id)
    fun getPaymentsForMember(memberId: Long): Flow<List<Payment>> = dao.getPaymentsForMember(memberId)

    suspend fun getMembersDirect(): List<Member> = dao.getAllMembers().let {
        // Direct list fetching helper
        dao.getAllPaymentsDirect().map { p -> p.memberId }.distinct()
        // We can query directly
        val membersList = mutableListOf<Member>()
        dao.getAllMembers().collect { membersList.addAll(it); return@collect }
        membersList
    }

    suspend fun getAllPaymentsDirect(): List<Payment> = dao.getAllPaymentsDirect()

    suspend fun insertMember(member: Member): Long = dao.insertMember(member)
    suspend fun updateMember(member: Member) = dao.updateMember(member)
    suspend fun deleteMember(member: Member) = dao.deleteMember(member)

    suspend fun insertPayment(payment: Payment): Long = dao.insertPayment(payment)
    suspend fun deletePayment(payment: Payment) = dao.deletePayment(payment)

    suspend fun restoreData(members: List<Member>, payments: List<Payment>) {
        dao.deleteAllMembers()
        dao.deleteAllPayments()
        dao.insertMembers(members)
        dao.insertPayments(payments)
    }

    suspend fun seedInitialDataIfEmpty() {
        // Seed Committee if empty
        val committee = listOf(
            CommitteeMember(1, "মোঃ আব্দুল রহিম", "সভাপতি", "01711000001", 1, "শিংলাব, চরপোতন"),
            CommitteeMember(2, "আলহাজ্ব কবির হোসেন", "সহ-সভাপতি", "01811000002", 2, "শিংলাব, চরপোতন"),
            CommitteeMember(3, "মোঃ শফিকুল ইসলাম", "সাধারণ সম্পাদক", "01911000003", 3, "শিংলাব, চরপোতন"),
            CommitteeMember(4, "মোঃ বিল্লাল হোসেন", "ক্যাশিয়ার / কোষাধ্যক্ষ", "01722000004", 4, "শিংলাব, চরপোতন"),
            CommitteeMember(5, "মোঃ রফিকুল ইসলাম", "সাংগঠনিক সম্পাদক", "01822000005", 5, "শিংলাব, চরপোতন"),
            CommitteeMember(6, "মোঃ জহিরুল হক", "প্রচার সম্পাদক", "01922000006", 6, "শিংলাব, চরপোতন"),
            CommitteeMember(7, "মোসাঃ ফাতেমা বেগম", "কার্যকরী সদস্য", "01733000007", 7, "শিংলাব, চরপোতন")
        )
        dao.insertCommitteeMembers(committee)

        // Seed Members & Payments if empty
        val existingMember = dao.getMemberByIdDirect(1L)
        if (existingMember == null) {
            val initialMembers = listOf(
                Member(1, "০০১", "মোঃ আব্দুল রহিম", "01711000001", "শিংলাব, চরপোতন", "01/01/2025", "প্রতিষ্ঠাতা সদস্য"),
                Member(2, "০০২", "মোঃ শফিকুল ইসলাম", "01911000003", "শিংলাব, চরপোতন", "01/01/2025", ""),
                Member(3, "০০৩", "আলহাজ্ব কবির হোসেন", "01811000002", "শিংলাব, চরপোতন", "01/01/2025", ""),
                Member(4, "০০৪", "মোঃ রফিকুল ইসলাম", "01822000005", "শিংলাব, চরপোতন", "01/01/2025", ""),
                Member(5, "০০৫", "মোসাঃ ফাতেমা বেগম", "01733000007", "শিংলাব, চরপোতন", "01/01/2025", ""),
                Member(6, "০০৬", "মোঃ জহিরুল হক", "01922000006", "শিংলাব, চরপোতন", "01/01/2025", ""),
                Member(7, "০০৭", "মোঃ বিল্লাল হোসেন", "01722000004", "শিংলাব, চরপোতন", "01/01/2025", ""),
                Member(8, "০০৮", "মোঃ মিজানুর রহমান", "01833000008", "শিংলাব, চরপোতন", "01/01/2025", "")
            )
            dao.insertMembers(initialMembers)

            // Seed sample payments (Calculation base: 2025 to 2026 ~ 20 expected months = 40,000 BDT target)
            val initialPayments = mutableListOf<Payment>()

            // Member 1 (Up to date - 20 installments = 40,000)
            for (i in 1..20) {
                val monthStr = if (i <= 12) "মাস $i/২০২৫" else "মাস ${i-12}/২০২৬"
                initialPayments.add(
                    Payment(memberId = 1, paymentDate = "05/${if (i<=12) String.format("%02d", i) else String.format("%02d", i-12)}/${if (i<=12) "2025" else "2026"}", monthYear = monthStr, installmentCount = 1, amount = 2000.0, receiptNo = "REC-${100+i}")
                )
            }

            // Member 2 (16 installments = 32,000 paid, 4 installments due = 8,000 OVERDUE)
            for (i in 1..16) {
                val monthStr = if (i <= 12) "মাস $i/২০২৫" else "মাস ${i-12}/২০২৬"
                initialPayments.add(
                    Payment(memberId = 2, paymentDate = "10/${if (i<=12) String.format("%02d", i) else String.format("%02d", i-12)}/${if (i<=12) "2025" else "2026"}", monthYear = monthStr, installmentCount = 1, amount = 2000.0, receiptNo = "REC-${200+i}")
                )
            }

            // Member 3 (20 installments = 40,000 paid - Up to date)
            for (i in 1..20) {
                val monthStr = if (i <= 12) "মাস $i/২০২৫" else "মাস ${i-12}/২০২৬"
                initialPayments.add(
                    Payment(memberId = 3, paymentDate = "02/${if (i<=12) String.format("%02d", i) else String.format("%02d", i-12)}/${if (i<=12) "2025" else "2026"}", monthYear = monthStr, installmentCount = 1, amount = 2000.0, receiptNo = "REC-${300+i}")
                )
            }

            // Member 4 (12 installments = 24,000 paid, 8 installments due = 16,000 OVERDUE)
            for (i in 1..12) {
                initialPayments.add(
                    Payment(memberId = 4, paymentDate = "12/${String.format("%02d", i)}/2025", monthYear = "মাস $i/২০২৫", installmentCount = 1, amount = 2000.0, receiptNo = "REC-${400+i}")
                )
            }

            // Member 5 (18 installments = 36,000 paid, 2 installments due = 4,000 OVERDUE)
            for (i in 1..18) {
                val monthStr = if (i <= 12) "মাস $i/২০২৫" else "মাস ${i-12}/২০২৬"
                initialPayments.add(
                    Payment(memberId = 5, paymentDate = "15/${if (i<=12) String.format("%02d", i) else String.format("%02d", i-12)}/${if (i<=12) "2025" else "2026"}", monthYear = monthStr, installmentCount = 1, amount = 2000.0, receiptNo = "REC-${500+i}")
                )
            }

            // Member 6 (22 installments = 44,000 paid, 2 ADVANCE)
            for (i in 1..22) {
                val monthStr = if (i <= 12) "মাস $i/২০২৫" else "মাস ${i-12}/২০২৬"
                initialPayments.add(
                    Payment(memberId = 6, paymentDate = "01/${if (i<=12) String.format("%02d", i) else String.format("%02d", i-12)}/${if (i<=12) "2025" else "2026"}", monthYear = monthStr, installmentCount = 1, amount = 2000.0, receiptNo = "REC-${600+i}")
                )
            }

            // Member 7 (20 installments = 40,000 paid - Up to date)
            for (i in 1..20) {
                val monthStr = if (i <= 12) "মাস $i/২০২৫" else "মাস ${i-12}/২০২৬"
                initialPayments.add(
                    Payment(memberId = 7, paymentDate = "08/${if (i<=12) String.format("%02d", i) else String.format("%02d", i-12)}/${if (i<=12) "2025" else "2026"}", monthYear = monthStr, installmentCount = 1, amount = 2000.0, receiptNo = "REC-${700+i}")
                )
            }

            // Member 8 (14 installments = 28,000 paid, 6 installments due = 12,000 OVERDUE)
            for (i in 1..14) {
                val monthStr = if (i <= 12) "মাস $i/২০২৫" else "মাস ${i-12}/২০২৬"
                initialPayments.add(
                    Payment(memberId = 8, paymentDate = "20/${if (i<=12) String.format("%02d", i) else String.format("%02d", i-12)}/${if (i<=12) "2025" else "2026"}", monthYear = monthStr, installmentCount = 1, amount = 2000.0, receiptNo = "REC-${800+i}")
                )
            }

            dao.insertPayments(initialPayments)
        }
    }
}
