package com.docsalert.reminder.family

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.ui.components.MemberProfileAvatar
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.FamilyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFamilyMemberScreen(
    familyViewModel: FamilyViewModel? = null,
    memberId: String? = null,
    onBackClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val membersState = familyViewModel?.members?.collectAsState()
    val memberToEdit = membersState?.value?.find { it.id == memberId }

    val isDefaultProfile = memberId == "default_user_profile"

    var fullName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var relationshipExpanded by remember { mutableStateOf(false) }
    var selectedRelationship by remember { mutableStateOf("Self") }
    val relationships = listOf("Self", "Father", "Mother", "Spouse", "Child", "Sibling", "Other")

    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(memberToEdit) {
        if (memberToEdit != null) {
            fullName = memberToEdit.fullName
            notes = memberToEdit.notes
            selectedRelationship = memberToEdit.relationship
            selectedPhotoUri = memberToEdit.profilePhotoUrl
        }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
            Toast.makeText(context, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    val inputShape = RoundedCornerShape(16.dp)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = palette.bodyText,
        unfocusedTextColor = palette.bodyText,
        focusedContainerColor = palette.inputBackground,
        unfocusedContainerColor = palette.inputBackground,
        focusedBorderColor = palette.focusedBorderColor,
        unfocusedBorderColor = palette.inputBorderColor,
        cursorColor = palette.primaryAccent,
        focusedLabelColor = palette.primaryAccent,
        unfocusedLabelColor = palette.secondaryText
    )

    Scaffold(
        containerColor = palette.screenBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            com.docsalert.reminder.ui.components.ScreenHeader(
                title = if (memberId != null) "Edit Member" else "Add Member",
                subtitle = "Manage profile details",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item(key = "top_ad_banner") {
                com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(bottom = 4.dp))
            }

            item {
                StaggeredEntrance(0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                MemberProfileAvatar(
                                    photoUrl = selectedPhotoUri,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                                    Text(
                                        if (selectedPhotoUri != null) "Change Profile Photo ✓" else "Add Profile Photo (Optional)",
                                        color = palette.oppositeAccent,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                StaggeredEntrance(1) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { if (it.length <= 30) fullName = it },
                        label = { Text("Full Name *") },
                        supportingText = {
                            Text(
                                text = "${fullName.length}/30",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = palette.secondaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            item {
                StaggeredEntrance(2) {
                    ExposedDropdownMenuBox(
                        expanded = relationshipExpanded && !isDefaultProfile,
                        onExpandedChange = { if (!isDefaultProfile) relationshipExpanded = !relationshipExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRelationship,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isDefaultProfile,
                            label = { Text("Relationship *") },
                            leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = palette.secondaryAccent) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = inputShape,
                            colors = textFieldColors
                        )
                        ExposedDropdownMenu(
                            expanded = relationshipExpanded && !isDefaultProfile,
                            onDismissRequest = { relationshipExpanded = false }
                        ) {
                            relationships.forEach { relation ->
                                DropdownMenuItem(
                                    text = { Text(relation) },
                                    onClick = {
                                        selectedRelationship = relation
                                        relationshipExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                StaggeredEntrance(3) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { if (it.length <= 200) notes = it },
                        label = { Text("Note (Optional)") },
                        supportingText = {
                            Text(
                                text = "${notes.length}/200",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Note, contentDescription = null, tint = palette.secondaryAccent) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 4,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            // Bottom Save / Update Button
            item {
                StaggeredEntrance(4) {
                    val saveInteraction = remember { MutableInteractionSource() }

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                Toast.makeText(context, "Please enter full name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newMember = FamilyMember(
                                id = memberId ?: java.util.UUID.randomUUID().toString(),
                                fullName = fullName.trim(),
                                relationship = selectedRelationship,
                                profilePhotoUrl = selectedPhotoUri,
                                notes = notes.trim()
                            )
                            familyViewModel?.addFamilyMember(newMember)
                            Toast.makeText(context, if (memberId != null) "Family member updated!" else "Family member saved!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .pressScale(saveInteraction),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.oppositeAccent,
                            contentColor = palette.onOppositeAccent
                        )
                    ) {
                        Icon(
                            imageVector = if (memberId != null) Icons.Default.Check else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (memberId != null) "Update Family Member" else "Save Family Member",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
