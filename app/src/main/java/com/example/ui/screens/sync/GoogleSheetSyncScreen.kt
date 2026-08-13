package com.example.ui.screens.sync

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.GoogleSheetSyncManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSheetSyncScreen(
    isSyncing: Boolean,
    syncMessage: String?,
    onSyncClick: () -> Unit,
    onRestoreFromJson: (String) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var sheetUrl by remember { mutableStateOf(GoogleSheetSyncManager.getSheetUrl(context)) }
    var restoreJsonText by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }

    val lastSyncTime = GoogleSheetSyncManager.getLastSyncTime(context)
    val lastSyncStr = if (lastSyncTime > 0) {
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(lastSyncTime))
    } else "এখনো সিঙ্ক করা হয়নি"

    LaunchedEffect(syncMessage) {
        if (!syncMessage.isNull_Empty()) {
            Toast.makeText(context, syncMessage, Toast.LENGTH_LONG).show()
            onClearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("গুগল শিট সিঙ্ক ও ব্যাকআপ", fontWeight = FontWeight.Bold) },
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
            // Explanatory Banner
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
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "গুগল ড্রাইভ ও শিট অনলাইন ব্যাকআপ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "অ্যাপ আনইনস্টল করে পুনরায় ইনস্টল করলেও আপনার কোনো তথ্য হারিয়ে যাবে না। গুগল শিট ওয়েব অ্যাপ ইউআরএল এর সাহায্যে যেকোনো সময় সিঙ্ক বা রিস্টোর করতে পারবেন।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Sheet URL Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("গুগল শিট ওয়েব অ্যাপ লিংক (Apps Script Web App URL):", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = sheetUrl,
                            onValueChange = { sheetUrl = it },
                            placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sheet_url_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                GoogleSheetSyncManager.saveSheetUrl(context, sheetUrl)
                                Toast.makeText(context, "গুগল শিট ইউআরএল সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("লিংক সেভ করুন")
                        }
                    }
                }
            }

            // Trigger Sync Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("সর্বশেষ ব্যাকআপ সময়: $lastSyncStr", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onSyncClick,
                            enabled = !isSyncing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("trigger_sync_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("সিঙ্ক হচ্ছে...")
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("গুগল শিটে এখনই সিঙ্ক করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Restore from JSON Backup
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ডাটা রিস্টোর / ব্যাকআপ ফাইল হতে ইম্পোর্ট:", fontWeight = FontWeight.Bold)
                        Text("পূর্বে ব্যাকআপ নেওয়া JSON কোড দিয়ে ডাটা রিস্টোর করুন।", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestorePage, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JSON ডাটা রিস্টোর করুন")
                        }
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("ব্যাকআপ কোড পেস্ট করুন") },
            text = {
                OutlinedTextField(
                    value = restoreJsonText,
                    onValueChange = { restoreJsonText = it },
                    placeholder = { Text("এখানে ব্যাকআপ JSON কোড দিন...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotBlank()) {
                            onRestoreFromJson(restoreJsonText)
                            showRestoreDialog = false
                        }
                    }
                ) {
                    Text("রিস্টোর নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

private fun String?.isNull_Empty(): Boolean = this == null || this.isEmpty()
