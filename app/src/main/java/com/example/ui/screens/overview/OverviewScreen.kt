package com.example.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberCalculation
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    calculations: List<MemberCalculation>,
    onMemberClick: (Long) -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMembers = calculations.size
    val totalCollected = calculations.sumOf { it.totalPaidAmount }
    val totalDue = calculations.sumOf { it.dueAmount }
    val totalExpected = calculations.sumOf { it.totalExpectedAmount }
    val overdueMembers = calculations.filter { it.dueAmount > 0 }

    val collectionPercentage = if (totalExpected > 0) {
        ((totalCollected / totalExpected) * 100).coerceAtMost(100.0)
    } else 0.0

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "হিসাব ও ওভারভিউ (Overview)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "শিংলাব চরপোতন সমিতি",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rule Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "হিসাবের ভিত্তি নীতি:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "• প্রতি সদস্যের মাসিক কিস্তি: ৳২,০০০\n• হিসাব শুরু: 01/01/2025 তারিখ হতে",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Summary KPI Matrix Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "মোট সদস্য",
                            value = "${MemberCalculation.formatNumber(totalMembers)} জন",
                            icon = Icons.Default.Groups,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        KpiCard(
                            title = "মোট জমা অর্থ",
                            value = MemberCalculation.formatCurrency(totalCollected),
                            icon = Icons.Default.AccountBalanceWallet,
                            containerColor = GreenPaidContainer,
                            contentColor = GreenPaid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "মোট বকেয়া অর্থ",
                            value = MemberCalculation.formatCurrency(totalDue),
                            icon = Icons.Default.Warning,
                            containerColor = RedDueContainer,
                            contentColor = RedDue,
                            modifier = Modifier.weight(1f)
                        )

                        KpiCard(
                            title = "প্রাপ্য মোট তহবিল",
                            value = MemberCalculation.formatCurrency(totalExpected),
                            icon = Icons.Default.Savings,
                            containerColor = AmberWarningContainer,
                            contentColor = AmberWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Collection Progress Progress Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "তহবিল আদায়ের অগ্রগতি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = String.format("%.1f%%", collectionPercentage),
                                fontWeight = FontWeight.Bold,
                                color = GreenPaid,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (collectionPercentage / 100.0).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = GreenPaid,
                            trackColor = Color.LightGray.copy(alpha = 0.4f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "মোট প্রাপ্য ৳${MemberCalculation.toBengaliDigits(String.format("%,.0f", totalExpected))} এর মধ্যে ৳${MemberCalculation.toBengaliDigits(String.format("%,.0f", totalCollected))} আদায় হয়েছে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Overdue Members Warning List
            item {
                Text(
                    text = "বকেয়া সদস্যবৃন্দ (${MemberCalculation.formatNumber(overdueMembers.size)} জন)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RedDue
                )
            }

            if (overdueMembers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GreenPaidContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPaid)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "কোনো সদস্যের বকেয়া নেই! সকল সদস্যের কিস্তি পরিশোধিত।",
                                color = GreenPaid,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(overdueMembers, key = { "overdue_${it.member.id}" }) { calc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMemberClick(calc.member.id) },
                        colors = CardDefaults.cardColors(containerColor = RedDueContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedDue)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(RedDue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = MemberCalculation.toBengaliDigits(calc.member.memberNo),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = calc.member.name,
                                    fontWeight = FontWeight.Bold,
                                    color = RedDue,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "বকেয়া: ${MemberCalculation.formatNumber(calc.dueInstallments)} টি কিস্তি (${MemberCalculation.formatCurrency(calc.dueAmount)})",
                                    fontSize = 12.sp,
                                    color = RedDue
                                )
                            }

                            Button(
                                onClick = { onMemberClick(calc.member.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = RedDue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("জমা দিন", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
