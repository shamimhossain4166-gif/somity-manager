package com.example.ui.screens.members

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Member

@Composable
fun EditMemberDialog(
    member: Member,
    onDismiss: () -> Unit,
    onConfirm: (updatedMember: Member) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var memberNo by remember { mutableStateOf(member.memberNo) }
    var phone by remember { mutableStateOf(member.phone) }
    var address by remember { mutableStateOf(member.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("সদস্য তথ্য এডিট করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = memberNo,
                    onValueChange = { memberNo = it },
                    label = { Text("সদস্য নম্বর") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_no_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("সদস্যের নাম") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_phone_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && memberNo.isNotBlank()) {
                        val updated = member.copy(
                            name = name.trim(),
                            memberNo = memberNo.trim(),
                            phone = phone.trim(),
                            address = address.trim()
                        )
                        onConfirm(updated)
                    }
                },
                modifier = Modifier.testTag("confirm_edit_member_button")
            ) {
                Text("হালনাগাদ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
