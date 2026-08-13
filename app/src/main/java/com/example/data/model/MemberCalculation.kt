package com.example.data.model

import java.util.Calendar

enum class PaymentStatus {
    PAID_UP_TO_DATE, // পরিশোধিত
    OVERDUE,          // বকেয়া আছে (MUST BE HIGHLIGHTED IN RED)
    ADVANCE           // অগ্রিম জমা
}

data class MemberCalculation(
    val member: Member,
    val payments: List<Payment>,
    val targetYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val targetMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed (1-12)
) {
    companion object {
        const val MONTHLY_INSTALLMENT_AMOUNT = 2000.0
        const val START_YEAR = 2025
        const val START_MONTH = 1 // January 2025

        /**
         * Calculate total elapsed months from 01/01/2025 to target year and month
         */
        fun calculateTotalExpectedMonths(year: Int, month: Int): Int {
            if (year < START_YEAR) return 0
            val yearDiff = year - START_YEAR
            val monthDiff = month - START_MONTH
            val total = yearDiff * 12 + monthDiff + 1
            return total.coerceAtLeast(1)
        }

        fun toBengaliDigits(input: String): String {
            val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
            val sb = StringBuilder()
            for (ch in input) {
                if (ch in '0'..'9') {
                    sb.append(banglaDigits[ch - '0'])
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        fun formatCurrency(amount: Double): String {
            val formattedStr = String.format("%,.0f", amount)
            return "৳" + toBengaliDigits(formattedStr)
        }

        fun formatNumber(num: Int): String {
            return toBengaliDigits(num.toString())
        }
    }

    val totalExpectedMonths: Int = calculateTotalExpectedMonths(targetYear, targetMonth)
    val totalExpectedAmount: Double = totalExpectedMonths * MONTHLY_INSTALLMENT_AMOUNT

    val totalPaidAmount: Double = payments.sumOf { it.amount }
    val totalPaidInstallments: Int = (totalPaidAmount / MONTHLY_INSTALLMENT_AMOUNT).toInt()

    val dueInstallments: Int = (totalExpectedMonths - totalPaidInstallments).coerceAtLeast(0)
    val dueAmount: Double = dueInstallments * MONTHLY_INSTALLMENT_AMOUNT

    val advanceInstallments: Int = (totalPaidInstallments - totalExpectedMonths).coerceAtLeast(0)
    val advanceAmount: Double = advanceInstallments * MONTHLY_INSTALLMENT_AMOUNT

    val status: PaymentStatus = when {
        dueAmount > 0 -> PaymentStatus.OVERDUE
        advanceAmount > 0 -> PaymentStatus.ADVANCE
        else -> PaymentStatus.PAID_UP_TO_DATE
    }
}
