package com.example.ui.screens.memberdetail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemberCalculation
import com.example.data.model.Payment
import com.example.data.model.PaymentStatus
import com.example.ui.theme.*
import com.example.util.SomityPdfExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    calculation: MemberCalculation?,
    onBackClick: () -> Unit,
    onAddPayment: (memberId: Long, count: Int, date: String, monthYear: String, receiptNo: String, remark: String) -> Unit,
    onDeletePayment: (Payment) -> Unit,
    onUpdateMember: (com.example.data.model.Member) -> Unit,
    onDeleteMember: (com.example.data.model.Member) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditMemberDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (calculation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val member = calculation.member
    val isOverdue = calculation.status == PaymentStatus.OVERDUE

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "সদস্য নং: ${MemberCalculation.toBengaliDigits(member.memberNo)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEditMemberDialog = true },
                        modifier = Modifier.testTag("edit_member_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "সদস্য তথ্য এডিট", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("delete_member_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "সদস্য মুছে ফেলুন", tint = MaterialTheme.colorScheme.error)
                    }

                    IconButton(
                        onClick = {
                            val pdfFile = SomityPdfExporter.generateMemberPdf(context, calculation)
                            if (pdfFile != null) {
                                SomityPdfExporter.sharePdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "পিডিএফ তৈরি করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("download_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "পিডিএফ ডাউনলোড", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            // Member Contact & Info Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isOverdue) RedDue else MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = MemberCalculation.toBengaliDigits(member.memberNo),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("ঠিকানা: ${member.address.ifEmpty { "শিংলাব, চরপোতন" }}", fontSize = 12.sp, color = Color.Gray)
                            Text("হিসাব শুরুর তারিখ: ${MemberCalculation.toBengaliDigits(member.joinDate)}", fontSize = 12.sp, color = Color.Gray)
                        }

                        // Call Action Button
                        if (member.phone.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GreenPaidContainer)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "ফোন করুন", tint = GreenPaid)
                            }
                        }
                    }
                }
            }

            // Calculation Matrix Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailKpiCard(
                            label = "মোট দেওয়া কিস্তি",
                            value = "${MemberCalculation.formatNumber(calculation.totalPaidInstallments)} টি",
                            bgColor = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )

                        DetailKpiCard(
                            label = "মোট জমা টাকা",
                            value = MemberCalculation.formatCurrency(calculation.totalPaidAmount),
                            bgColor = GreenPaidContainer,
                            textColor = GreenPaid,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailKpiCard(
                            label = "কতটি কিস্তি বাকি",
                            value = "${MemberCalculation.formatNumber(calculation.dueInstallments)} টি",
                            bgColor = if (isOverdue) RedDueContainer else MaterialTheme.colorScheme.surfaceVariant,
                            textColor = if (isOverdue) RedDue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )

                        DetailKpiCard(
                            label = "কত টাকা বাকি",
                            value = MemberCalculation.formatCurrency(calculation.dueAmount),
                            bgColor = if (isOverdue) RedDueContainer else GreenPaidContainer,
                            textColor = if (isOverdue) RedDue else GreenPaid,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Action Buttons (Add Payment & Share PDF)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_payment_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("জমা যোগ করুন", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val pdfFile = SomityPdfExporter.generateMemberPdf(context, calculation)
                            if (pdfFile != null) {
                                SomityPdfExporter.sharePdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "পিডিএফ ফাইল প্রসেস করা যায়নি", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF শেয়ার করুন")
                    }
                }
            }

            // History Records Table Title
            item {
                Text(
                    text = "জমার ইতিহাস ও রশিদ (${MemberCalculation.formatNumber(calculation.payments.size)} টি)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (calculation.payments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("এখনো কোনো জমা রেকর্ড নেই", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(calculation.payments, key = { it.id }) { payment ->
                    PaymentRowCard(payment = payment, onDelete = { onDeletePayment(payment) })
                }
            }
        }
    }

    // Add Payment Dialog
    if (showAddDialog) {
        AddPaymentDialog(
            member = member,
            onDismiss = { showAddDialog = false },
            onConfirm = { count, date, monthYear, receiptNo, remark ->
                onAddPayment(member.id, count, date, monthYear, receiptNo, remark)
                showAddDialog = false
            }
        )
    }

    if (showEditMemberDialog) {
        com.example.ui.screens.members.EditMemberDialog(
            member = member,
            onDismiss = { showEditMemberDialog = false },
            onConfirm = { updatedMember ->
                onUpdateMember(updatedMember)
                showEditMemberDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("সদস্য ডিলিট নিশ্চিতকরণ") },
            text = { Text("আপনি কি নিশ্চিত যে '${member.name}' (সদস্য নং ${member.memberNo}) সমবায় সমিতির তালিকা হতে সম্পূর্ণ মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMember(member)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("হ্যাঁ, ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun DetailKpiCard(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 12.sp, color = textColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun PaymentRowCard(
    payment: Payment,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = GreenPaid,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${MemberCalculation.formatNumber(payment.installmentCount)} টি কিস্তি (${payment.monthYear})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "তারিখ: ${MemberCalculation.toBengaliDigits(payment.paymentDate)} | রশিদ: ${payment.receiptNo.ifEmpty { "N/A" }}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = MemberCalculation.formatCurrency(payment.amount),
                fontWeight = FontWeight.Bold,
                color = GreenPaid,
                fontSize = 15.sp
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "মুছুন", tint = RedDue)
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    member: com.example.data.model.Member,
    onDismiss: () -> Unit,
    onConfirm: (count: Int, date: String, monthYear: String, receiptNo: String, remark: String) -> Unit
) {
    var countText by remember { mutableStateOf("1") }
    var receiptNo by remember { mutableStateOf("REC-${(100..999).random()}") }
    var monthYear by remember { mutableStateOf("আগস্ট ২০২৬") }
    val todayDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    var paymentDate by remember { mutableStateOf(todayDateStr) }
    var remarks by remember { mutableStateOf("") }

    val count = countText.toIntOrNull() ?: 1
    val calculatedAmount = count * MemberCalculation.MONTHLY_INSTALLMENT_AMOUNT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন কিস্তি জমা গ্রহণ (${member.name})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it },
                    label = { Text("কিস্তির সংখ্যা") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("installment_count_input")
                )

                Surface(
                    color = GreenPaidContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "মোট জমার পরিমাণ: ${MemberCalculation.formatCurrency(calculatedAmount)}",
                        color = GreenPaid,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("জমার তারিখ (DD/MM/YYYY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("মাস ও বছর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = receiptNo,
                    onValueChange = { receiptNo = it },
                    label = { Text("রশিদ নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(count, paymentDate, monthYear, receiptNo, remarks) },
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("জমা নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
