package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val paymentDate: String,        // DD/MM/YYYY e.g. "15/01/2025"
    val monthYear: String,          // e.g. "জানুয়ারি ২০২৫" or "01/2025"
    val installmentCount: Int = 1,  // Number of installments paid in this receipt (e.g. 1, 2)
    val amount: Double = 2000.0,    // Amount in BDT
    val receiptNo: String = "",
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
