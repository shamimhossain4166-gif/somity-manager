package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.screens.committee.CommitteeScreen
import com.example.ui.screens.memberdetail.MemberDetailScreen
import com.example.ui.screens.members.AddMemberDialog
import com.example.ui.screens.members.MembersScreen
import com.example.ui.screens.overview.OverviewScreen
import com.example.ui.screens.sync.GoogleSheetSyncScreen
import com.example.ui.theme.SomityTheme

sealed class BottomTab(val route: String, val label: String, val icon: ImageVector, val tag: String) {
    object Members : BottomTab("members", "সদস্যবৃন্দ", Icons.Default.People, "tab_members")
    // STRICT REQUIREMENT: Overview MUST BE POSITION #2 IN THE MENU!
    object Overview : BottomTab("overview", "ওভারভিউ", Icons.Default.Analytics, "tab_overview")
    object Committee : BottomTab("committee", "কমিটি", Icons.Default.Badge, "tab_committee")
    object Sync : BottomTab("sync", "শিট সিঙ্ক", Icons.Default.CloudSync, "tab_sync")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SomityTheme {
                MainAppShell(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppShell(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf<BottomTab>(BottomTab.Members) }
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    val memberCalculations by viewModel.filteredCalculations.collectAsStateWithLifecycle()
    val allCalculations by viewModel.memberCalculations.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val committeeMembers by viewModel.committeeMembers.collectAsStateWithLifecycle()
    val selectedCalculation by viewModel.selectedMemberCalculation.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncStateMessage.collectAsStateWithLifecycle()

    val navTabs = listOf(
        BottomTab.Members,
        BottomTab.Overview, // POSITION #2 IN MENU
        BottomTab.Committee,
        BottomTab.Sync
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (selectedMemberId == null) {
                NavigationBar {
                    navTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedMemberId != null) {
                // Member Detail Screen View
                MemberDetailScreen(
                    calculation = selectedCalculation,
                    onBackClick = {
                        selectedMemberId = null
                        viewModel.selectMember(null)
                    },
                    onAddPayment = { memberId, count, date, monthYear, receiptNo, remark ->
                        viewModel.addPayment(memberId, count, date, monthYear, receiptNo, remark)
                    },
                    onDeletePayment = { payment ->
                        viewModel.deletePayment(payment)
                    },
                    onUpdateMember = { member ->
                        viewModel.updateMember(member)
                    },
                    onDeleteMember = { member ->
                        viewModel.deleteMember(member)
                        selectedMemberId = null
                    }
                )
            } else {
                when (currentTab) {
                    BottomTab.Members -> {
                        MembersScreen(
                            calculations = memberCalculations,
                            searchQuery = searchQuery,
                            filterStatus = filterStatus,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onFilterStatusChange = { viewModel.setFilterStatus(it) },
                            onMemberClick = { id ->
                                selectedMemberId = id
                                viewModel.selectMember(id)
                            },
                            onAddMemberClick = { showAddMemberDialog = true }
                        )
                    }

                    BottomTab.Overview -> { // MENU ITEM #2
                        OverviewScreen(
                            calculations = allCalculations,
                            onMemberClick = { id ->
                                selectedMemberId = id
                                viewModel.selectMember(id)
                            },
                            onSyncClick = { viewModel.triggerGoogleSheetSync() }
                        )
                    }

                    BottomTab.Committee -> {
                        CommitteeScreen(committeeList = committeeMembers)
                    }

                    BottomTab.Sync -> {
                        GoogleSheetSyncScreen(
                            isSyncing = isSyncing,
                            syncMessage = syncMessage,
                            onSyncClick = { viewModel.triggerGoogleSheetSync() },
                            onRestoreFromJson = { jsonStr -> viewModel.restoreFromJson(jsonStr) },
                            onClearMessage = { viewModel.clearSyncMessage() }
                        )
                    }
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name, memberNo, phone, address ->
                viewModel.addNewMember(name, memberNo, phone, address)
                showAddMemberDialog = false
            }
        )
    }
}
