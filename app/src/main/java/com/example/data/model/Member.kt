package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberNo: String,           // e.g. "০০১" or "001"
    val name: String,               // Member name
    val phone: String,              // Mobile number
    val address: String = "",       // Address
    val joinDate: String = "01/01/2025", // Join date / start date
    val note: String = "",
    val isActive: Boolean = true
)
