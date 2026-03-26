package com.ourspace.app.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ourspace.app.data.model.UserProfile
import com.ourspace.app.ui.components.AuraDatePickerField
import com.ourspace.app.ui.components.AuraTimePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    userViewModel: UserViewModel,
    onSetupComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Prefer not to say") }
    var nickname by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }
    
    val isSaving by userViewModel.isSavingProfile.collectAsState()
    val genders = listOf("Male", "Female", "Non-binary", "Prefer not to say")
    var genderExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome to Aura",
                fontSize = 32.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Let's get your profile ready.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name (Required)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genders.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                gender = selection
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = statusText,
                onValueChange = { statusText = it },
                label = { Text("Current Aura/Status (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            var birthday by remember { mutableStateOf("") }
            var anniversary by remember { mutableStateOf("") }

            AuraDatePickerField(
                value = birthday,
                onValueChange = { birthday = it },
                label = "Birthday (Optional)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuraDatePickerField(
                value = anniversary,
                onValueChange = { anniversary = it },
                label = "Anniversary (Optional)"
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val updates = mapOf(
                        "name" to name,
                        "gender" to gender,
                        "nickname" to nickname,
                        "statusText" to statusText,
                        "birthday" to birthday,
                        "anniversary" to anniversary,
                        "hasCompletedSetup" to true
                    )
                    userViewModel.updateExtendedProfile(updates)
                    onSetupComplete()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Start My Journey", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
