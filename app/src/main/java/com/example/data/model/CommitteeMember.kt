package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "committee_members")
data class CommitteeMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val designation: String,        // e.g. "সভাপতি", "সাধারণ সম্পাদক", "ক্যাশিয়ার"
    val phone: String,
    val rankOrder: Int = 1,
    val address: String = "চরপোতন, শিংলাব"
)
