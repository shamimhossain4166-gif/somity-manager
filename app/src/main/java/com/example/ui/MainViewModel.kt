package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SomityDatabase
import com.example.data.model.CommitteeMember
import com.example.data.model.Member
import com.example.data.model.MemberCalculation
import com.example.data.model.Payment
import com.example.data.repository.SomityRepository
import com.example.util.GoogleSheetSyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SomityDatabase.getDatabase(application)
    val repository = SomityRepository(db.somityDao())

    // Target Calculation Year & Month (Default to current)
    val currentCal = Calendar.getInstance()
    private val _targetYear = MutableStateFlow(currentCal.get(Calendar.YEAR))
    val targetYear: StateFlow<Int> = _targetYear.asStateFlow()

    private val _targetMonth = MutableStateFlow(currentCal.get(Calendar.MONTH) + 1)
    val targetMonth: StateFlow<Int> = _targetMonth.asStateFlow()

    // Search query & filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow("ALL") // "ALL", "OVERDUE", "PAID"
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    // Sync status message
    private val _syncStateMessage = MutableStateFlow<String?>(null)
    val syncStateMessage: StateFlow<String?> = _syncStateMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Selected Member for detail screen
    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    val selectedMemberId: StateFlow<Long?> = _selectedMemberId.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val members: StateFlow<List<Member>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val committeeMembers: StateFlow<List<CommitteeMember>> = repository.committeeMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined calculations list for all members
    val memberCalculations: StateFlow<List<MemberCalculation>> = combine(
        members,
        payments,
        targetYear,
        targetMonth
    ) { memberList, paymentList, yr, mo ->
        memberList.map { m ->
            val mPayments = paymentList.filter { p -> p.memberId == m.id }
            MemberCalculation(m, mPayments, yr, mo)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered member calculations based on search & filter
    val filteredCalculations: StateFlow<List<MemberCalculation>> = combine(
        memberCalculations,
        searchQuery,
        filterStatus
    ) { calculations, query, filter ->
        calculations.filter { calc ->
            val matchesQuery = query.isEmpty() ||
                    calc.member.name.contains(query, ignoreCase = true) ||
                    calc.member.memberNo.contains(query, ignoreCase = true) ||
                    calc.member.phone.contains(query)

            val matchesFilter = when (filter) {
                "OVERDUE" -> calc.dueAmount > 0
                "PAID" -> calc.dueAmount == 0.0
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected member calculation
    val selectedMemberCalculation: StateFlow<MemberCalculation?> = combine(
        selectedMemberId,
        memberCalculations
    ) { id, calcs ->
        if (id == null) null else calcs.find { it.member.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: String) {
        _filterStatus.value = status
    }

    fun selectMember(id: Long?) {
        _selectedMemberId.value = id
    }

    fun updateTargetPeriod(year: Int, month: Int) {
        _targetYear.value = year
        _targetMonth.value = month
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
            if (_selectedMemberId.value == member.id) {
                _selectedMemberId.value = null
            }
        }
    }

    fun addNewMember(name: String, memberNo: String, phone: String, address: String) {
        viewModelScope.launch {
            val member = Member(
                name = name.trim(),
                memberNo = memberNo.trim(),
                phone = phone.trim(),
                address = address.trim(),
                joinDate = "01/01/2025"
            )
            repository.insertMember(member)
        }
    }

    fun addPayment(
        memberId: Long,
        installmentCount: Int,
        paymentDate: String,
        monthYear: String,
        receiptNo: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val amount = installmentCount * MemberCalculation.MONTHLY_INSTALLMENT_AMOUNT
            val payment = Payment(
                memberId = memberId,
                paymentDate = paymentDate,
                monthYear = monthYear,
                installmentCount = installmentCount,
                amount = amount,
                receiptNo = receiptNo.trim(),
                remarks = remarks.trim()
            )
            repository.insertPayment(payment)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    fun triggerGoogleSheetSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStateMessage.value = "গুগল শিটে ডাটা সিঙ্ক করা হচ্ছে..."
            val result = GoogleSheetSyncManager.syncToGoogleSheet(getApplication(), repository)
            _isSyncing.value = false
            _syncStateMessage.value = result.getOrElse { it.message ?: "সিঙ্ক ব্যর্থ হয়েছে" }
        }
    }

    fun restoreFromJson(jsonString: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = GoogleSheetSyncManager.restoreFromJson(jsonString, repository)
            _isSyncing.value = false
            _syncStateMessage.value = result.getOrElse { it.message ?: "রিস্টোর ব্যর্থ হয়েছে" }
        }
    }

    fun clearSyncMessage() {
        _syncStateMessage.value = null
    }
}
