package com.example.ui.screens.members

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberCalculation
import com.example.data.model.PaymentStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    calculations: List<MemberCalculation>,
    searchQuery: String,
    filterStatus: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterStatusChange: (String) -> Unit,
    onMemberClick: (Long) -> Unit,
    onAddMemberClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMemberClick,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "নতুন সদস্য যোগ করুন") },
                text = { Text("নতুন সদস্য", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_member_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Search & Filter Section
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "শিংলাব চরপোতন সমিতি",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "সদস্য তালিকা ও কিস্তি হিসাব (মাসিক ৳২,০০০ - শুরু 01/01/2025)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search TextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("member_search_input"),
                        placeholder = { Text("নাম, সদস্য নং বা মোবাইল খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterStatus == "ALL",
                            onClick = { onFilterStatusChange("ALL") },
                            label = { Text("সকল সদস্য (${calculations.size})") },
                            leadingIcon = if (filterStatus == "ALL") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.testTag("filter_chip_all")
                        )

                        val overdueCount = calculations.count { it.dueAmount > 0 }
                        FilterChip(
                            selected = filterStatus == "OVERDUE",
                            onClick = { onFilterStatusChange("OVERDUE") },
                            label = { Text("বকেয়া আছে ($overdueCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedDueContainer,
                                selectedLabelColor = RedDue
                            ),
                            leadingIcon = if (filterStatus == "OVERDUE") {
                                { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedDue) }
                            } else null,
                            modifier = Modifier.testTag("filter_chip_overdue")
                        )

                        FilterChip(
                            selected = filterStatus == "PAID",
                            onClick = { onFilterStatusChange("PAID") },
                            label = { Text("পরিশোধিত") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPaidContainer,
                                selectedLabelColor = GreenPaid
                            ),
                            modifier = Modifier.testTag("filter_chip_paid")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Member Cards List
            if (calculations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "কোনো সদস্যের তথ্য পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(calculations, key = { it.member.id }) { item ->
                        MemberCard(
                            calculation = item,
                            onClick = { onMemberClick(item.member.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    calculation: MemberCalculation,
    onClick: () -> Unit
) {
    val member = calculation.member
    val isOverdue = calculation.status == PaymentStatus.OVERDUE

    // Card background color and border highlighting for overdue members
    val cardBg = if (isOverdue) RedDueContainer else MaterialTheme.colorScheme.surface
    val borderStroke = if (isOverdue) RedDue else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("member_card_${member.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = if (isOverdue) androidx.compose.foundation.BorderStroke(1.5.dp, borderStroke) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Member No Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isOverdue) RedDue else MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = MemberCalculation.toBengaliDigits(member.memberNo),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) RedDue else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "মোবাইল: ${MemberCalculation.toBengaliDigits(member.phone)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Arrow Details Icon
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "বিস্তারিত দেখুন",
                    tint = if (isOverdue) RedDue else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge & Calculation Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paid Info
                Column {
                    Text(
                        text = "দেওয়া হয়েছে: ${MemberCalculation.formatNumber(calculation.totalPaidInstallments)} টি কিস্তি",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "মোট জমা: ${MemberCalculation.formatCurrency(calculation.totalPaidAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenPaid
                    )
                }

                // Due Info Highlight Box
                if (isOverdue) {
                    Surface(
                        color = RedDue,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "বকেয়া: ${MemberCalculation.formatNumber(calculation.dueInstallments)} টি (${MemberCalculation.formatCurrency(calculation.dueAmount)})",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (calculation.status == PaymentStatus.ADVANCE) {
                    Surface(
                        color = Color(0xFF1976D2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "অগ্রিম: ${MemberCalculation.formatNumber(calculation.advanceInstallments)} টি কিস্তি",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = GreenPaidContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenPaid,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "পরিশোধিত",
                                color = GreenPaid,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
