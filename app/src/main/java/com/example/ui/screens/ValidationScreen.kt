package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.*
import com.example.engine.ValidationEngine
import com.example.ui.ExportViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationScreen(
    shipmentId: String,
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAi: (() -> Unit)? = null
) {
    val shipment by viewModel.currentShipment.collectAsState()
    val validationSummary by viewModel.validationSummary.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showOverrideDialog by remember { mutableStateOf(false) }
    var overrideReason by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val isAuthorizedForOverride = currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.EXPORT_MANAGER
    val allItems = validationSummary?.items ?: emptyList()
    val filteredItems = when (selectedCategory) {
        "ALL" -> allItems
        ValidationEngine.CATEGORY_INV_VS_PL -> allItems.filter { it.category == ValidationEngine.CATEGORY_INV_VS_PL }
        ValidationEngine.CATEGORY_PL_VS_ISF -> allItems.filter { it.category == ValidationEngine.CATEGORY_PL_VS_ISF }
        ValidationEngine.CATEGORY_PL_VS_BL -> allItems.filter { it.category == ValidationEngine.CATEGORY_PL_VS_BL }
        ValidationEngine.CATEGORY_ISF_VS_BL -> allItems.filter { it.category == ValidationEngine.CATEGORY_ISF_VS_BL }
        else -> allItems
    }

    val hasDiscrepancies = (validationSummary?.totalErrors ?: 0) > 0 || (validationSummary?.totalWarnings ?: 0) > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cross-Document Validation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(shipmentId, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.sendAiMessage("Explain why these documents don't match.")
                        onNavigateToAi?.invoke()
                    }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask Export AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }

                    TextButton(onClick = { viewModel.resetPreviewDiscrepancies() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val canComplete = validationSummary?.canMarkComplete == true

                    if (!canComplete) {
                        val hasErrors = (validationSummary?.totalErrors ?: 0) > 0
                        val hasWarnings = (validationSummary?.totalWarnings ?: 0) > 0

                        if (hasErrors) {
                            Text(
                                text = "⛔ Cannot mark complete: ${validationSummary?.totalErrors} Discrepancies must be resolved.",
                                color = if (isSystemInDarkTheme()) Color(0xFFF87171) else Color(0xFFB91C1C),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (hasWarnings && shipment?.isOverrideApproved != true) {
                            Text(
                                text = "⚠️ ${validationSummary?.totalWarnings} Warnings exist. Requires Manager Override Authorization to proceed.",
                                color = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFB45309),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetPreviewDiscrepancies() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("reset_preview_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-validate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if ((validationSummary?.totalWarnings ?: 0) > 0 && shipment?.isOverrideApproved != true) {
                            OutlinedButton(
                                onClick = { showOverrideDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("override_warning_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFB45309)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Override", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (canComplete && shipment != null) {
                                    viewModel.updateShipment(shipment!!.copy(status = ShipmentStatus.ISF_FILED))
                                }
                            },
                            enabled = canComplete,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("mark_documents_complete_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canComplete) StatusGreen else LightTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COMPLETE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("validation_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AUTOMATIC VALIDATION ENGINE",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Three-Way Cross Compliance",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusScoreBox(
                                count = validationSummary?.totalMatches ?: 0,
                                label = "Matches",
                                color = StatusGreen,
                                bg = Color(0xFF14532D)
                            )
                            StatusScoreBox(
                                count = validationSummary?.totalWarnings ?: 0,
                                label = "Warnings",
                                color = Color(0xFFFDE68A),
                                bg = Color(0xFF78350F)
                            )
                            StatusScoreBox(
                                count = validationSummary?.totalErrors ?: 0,
                                label = "Errors",
                                color = Color(0xFFFECACA),
                                bg = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "ALL",
                        onClick = { selectedCategory = "ALL" },
                        label = { Text("All (${allItems.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == ValidationEngine.CATEGORY_INV_VS_PL,
                        onClick = { selectedCategory = ValidationEngine.CATEGORY_INV_VS_PL },
                        label = { Text("Invoice vs PL", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == ValidationEngine.CATEGORY_PL_VS_ISF,
                        onClick = { selectedCategory = ValidationEngine.CATEGORY_PL_VS_ISF },
                        label = { Text("PL vs ISF", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == ValidationEngine.CATEGORY_PL_VS_BL,
                        onClick = { selectedCategory = ValidationEngine.CATEGORY_PL_VS_BL },
                        label = { Text("PL vs B/L", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == ValidationEngine.CATEGORY_ISF_VS_BL,
                        onClick = { selectedCategory = ValidationEngine.CATEGORY_ISF_VS_BL },
                        label = { Text("ISF vs B/L", fontSize = 11.sp) }
                    )
                }
            }

            // Compliance Policy Notice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Gavel,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "STRICT DATA GOVERNANCE POLICY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cross-document validation engine never automatically alters conflicting trade data. To ensure regulatory compliance and audit readiness, discrepancies must be verified and resolved manually in the respective source documents.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Exact Requested Format Box:
            // DOCUMENT VALIDATION
            // ────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DOCUMENT VALIDATION",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            OutlinedButton(
                                onClick = { viewModel.resetPreviewDiscrepancies() },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("RE-CHECK", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            thickness = 1.dp
                        )

                        // Display formatted items
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredItems.forEach { item ->
                                DocumentValidationCheckRow(item)
                            }
                        }
                    }
                }
            }

            // Override Approval Card if already approved
            if (shipment?.isOverrideApproved == true) {
                item {
                    val isDark = isSystemInDarkTheme()
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else StatusGreenContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = if (isDark) Color(0xFF86EFAC) else StatusGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Manager Override Granted",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF86EFAC) else Color(0xFF14532D),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Authorized by: ${shipment?.overrideApprovedBy}\nReason: ${shipment?.overrideNotes ?: "Standard operating exception"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Manager Override Authorization Dialog
    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("Manager Override Authorization", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current User: ${currentUser.fullName} (${currentUser.role.label})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!isAuthorizedForOverride) {
                        Text(
                            text = "⚠️ Your current role (${currentUser.role.label}) does not have permission to override validation warnings. Please switch to Admin or Export Manager role.",
                            color = if (isSystemInDarkTheme()) Color(0xFFF87171) else Color(0xFFB91C1C),
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Please enter an official justification for overriding active document validation warnings. This action will be permanently recorded in the immutable audit log.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = overrideReason,
                            onValueChange = { overrideReason = it },
                            label = { Text("Override Justification / Reason") },
                            placeholder = { Text("e.g. Verified with consignee via email...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                if (isAuthorizedForOverride) {
                    Button(
                        onClick = {
                            if (overrideReason.isNotBlank()) {
                                viewModel.approveOverride(overrideReason)
                                showOverrideDialog = false
                            }
                        },
                        enabled = overrideReason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryAmber)
                    ) {
                        Text("Authorize Override", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
fun StatusScoreBox(count: Int, label: String, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun DocumentValidationCheckRow(item: ValidationItem) {
    val isDark = isSystemInDarkTheme()
    val (statusSymbol, symbolColor, cardBg, textColor) = when (item.severity) {
        ValidationSeverity.MATCH -> if (isDark) {
            Quadruple("✅", Color(0xFF4ADE80), Color(0xFF064E3B).copy(alpha = 0.35f), Color(0xFF86EFAC))
        } else {
            Quadruple("✅", StatusGreen, Color(0xFFF0FDF4), Color(0xFF15803D))
        }
        ValidationSeverity.WARNING -> if (isDark) {
            Quadruple("⚠️", Color(0xFFFBBF24), Color(0xFF78350F).copy(alpha = 0.35f), Color(0xFFFDE68A))
        } else {
            Quadruple("⚠️", StatusAmber, Color(0xFFFFFBEB), Color(0xFF92400E))
        }
        ValidationSeverity.ERROR -> if (isDark) {
            Quadruple("❌", Color(0xFFF87171), Color(0xFF7F1D1D).copy(alpha = 0.35f), Color(0xFFFECACA))
        } else {
            Quadruple("❌", StatusRed, Color(0xFFFEF2F2), Color(0xFFB91C1C))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = statusSymbol, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.fieldName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.category.isNotBlank()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = item.category,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (item.severity == ValidationSeverity.MATCH) {
                Column(modifier = Modifier.padding(start = 22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Matching",
                            fontSize = 12.sp,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (item.valueA.isNotBlank() && item.valueA != "Matching") {
                            Text(
                                text = " • ${item.valueA}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${item.documentA} ↔ ${item.documentB}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(start = 22.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Document A & Value A
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Document A: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.documentA}  •  ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Value A: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = item.valueA,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }

                    // Document B & Value B
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Document B: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.documentB}  •  ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Value B: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = item.valueB,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }

                    // Severity Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Severity: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(symbolColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.severity.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = symbolColor
                            )
                        }
                    }

                    if (item.recommendedAction.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .border(0.8.dp, symbolColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = "RECOMMENDED ACTION:",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = symbolColor,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.recommendedAction,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
