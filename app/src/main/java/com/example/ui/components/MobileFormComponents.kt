package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.ExportViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mobile-friendly Date Picker Field using Material 3 DatePickerDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select date"
) {
    var showModal by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                IconButton(onClick = { showModal = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = PrimaryBlue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { showModal = true }
        )
    }

    if (showModal) {
        DatePickerDialog(
            onDismissRequest = { showModal = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            onDateSelected(sdf.format(Date(selectedMillis)))
                        }
                        showModal = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Mobile-friendly Number Input Field with touch-friendly step buttons (+ / -).
 */
@Composable
fun AppNumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
    min: Int = 0,
    max: Int = 999999,
    keyboardType: KeyboardType = KeyboardType.Number,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val currentInt = value.toIntOrNull() ?: 0

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (input.isEmpty() || input.all { it.isDigit() }) {
                    onValueChange(input)
                }
            },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            trailingIcon = {
                Row(
                    modifier = Modifier.padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrement button
                    FilledIconButton(
                        onClick = {
                            val nextVal = maxOf(min, currentInt - step)
                            onValueChange(nextVal.toString())
                        },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Increment button
                    FilledIconButton(
                        onClick = {
                            val nextVal = minOf(max, currentInt + step)
                            onValueChange(nextVal.toString())
                        },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = PrimaryBlueContainer,
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
    }
}

/**
 * Mobile-optimized Dropdown Menu with Material 3 ExposedDropdownMenuBox.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppDropdownField(
    label: String,
    options: List<T>,
    selectedOption: T?,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption?.let(optionLabel) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option), fontSize = 14.sp) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

/**
 * Interactive Single Source of Truth Export Workflow Stepper
 * Order → Shipment → Products → Packing → Invoice → ISF → B/L → Validation → Approval → PDF/Excel → Shipment tracking
 */
@Composable
fun SingleSourceOfTruthWorkflowCard(
    shipment: Shipment?,
    products: List<ShipmentProduct>,
    pallets: List<Pallet>,
    invoice: CommercialInvoice?,
    packingList: PackingList?,
    isf: IsfFiling?,
    bl: BlInstructions?,
    validationSummary: ShipmentValidationSummary?,
    onNavigateToStage: (stageName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCartons = products.sumOf { it.cartons }
    val totalGrossWeight = products.sumOf { it.totalGrossWeightKg }
    val isValidationPassed = validationSummary?.totalErrors == 0
    val isApproved = shipment?.isOverrideApproved == true || isValidationPassed

    val stages = listOf(
        WorkflowStepInfo(1, "Order", "PO: ${shipment?.poNumber ?: "Pending"}", Icons.Default.ShoppingCart, isCompleted = shipment?.poNumber?.isNotBlank() == true),
        WorkflowStepInfo(2, "Shipment", "Ref: ${shipment?.reference ?: "N/A"}", Icons.Default.LocalShipping, isCompleted = shipment != null),
        WorkflowStepInfo(3, "Products", "$totalCartons ctns / ${products.size} SKUs", Icons.Default.Inventory2, isCompleted = products.isNotEmpty()),
        WorkflowStepInfo(4, "Packing", "${pallets.size} Pallets / CFS", Icons.Default.ViewInAr, isCompleted = pallets.isNotEmpty()),
        WorkflowStepInfo(5, "Invoice", invoice?.let { "${it.currency} ${it.totalAmount.toInt()}" } ?: "Draft", Icons.Default.Receipt, isCompleted = invoice != null),
        WorkflowStepInfo(6, "ISF", isf?.let { "${it.cartonsCount} ctns" } ?: "10+2", Icons.Default.AssignmentLate, isCompleted = isf != null),
        WorkflowStepInfo(7, "B/L", bl?.let { "%.1f kg".format(it.grossWeightKg) } ?: "Instructions", Icons.Default.DirectionsBoat, isCompleted = bl != null),
        WorkflowStepInfo(8, "Validation", if (isValidationPassed) "MATCH (0)" else "${validationSummary?.totalErrors ?: 0} Errors", Icons.Default.Rule, isCompleted = isValidationPassed, isWarning = !isValidationPassed),
        WorkflowStepInfo(9, "Approval", if (isApproved) "APPROVED" else "Pending", Icons.Default.Verified, isCompleted = isApproved),
        WorkflowStepInfo(10, "PDF/Excel", "SOLAS VGM Dossier", Icons.Default.PictureAsPdf, isCompleted = invoice != null && packingList != null),
        WorkflowStepInfo(11, "Tracking", shipment?.status?.label ?: "Port CMB", Icons.Default.Navigation, isCompleted = shipment?.status == ShipmentStatus.LOADED || shipment?.status == ShipmentStatus.SHIPPED || shipment?.status == ShipmentStatus.DELIVERED)
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("single_source_workflow_stepper")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SINGLE SOURCE OF TRUTH WORKFLOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Order → Shipment → Products → Packing → Invoice → ISF → B/L → Validation → Approval → PDF/Excel → Tracking",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontally Scrollable Step Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stages.forEachIndexed { index, step ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            step.isWarning -> StatusAmberContainer
                            step.isCompleted -> StatusGreenContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .widthIn(min = 100.dp)
                            .clickable { onNavigateToStage(step.name) }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                step.isWarning -> StatusAmber
                                                step.isCompleted -> StatusGreen
                                                else -> Color.Gray
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${step.stepNumber}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = step.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        step.isWarning -> Color(0xFF78350F)
                                        step.isCompleted -> Color(0xFF065F46)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = step.detail,
                                fontSize = 10.sp,
                                color = when {
                                    step.isWarning -> Color(0xFF78350F)
                                    step.isCompleted -> Color(0xFF065F46)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1
                            )
                        }
                    }

                    if (index < stages.size - 1) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Integrity Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Central SQLite Database is Single Source of Truth • Total: $totalCartons ctns, %.1f kg, ${pallets.size} pallets across all documents.".format(totalGrossWeight),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class WorkflowStepInfo(
    val stepNumber: Int,
    val name: String,
    val detail: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isCompleted: Boolean,
    val isWarning: Boolean = false
)

/**
 * Offline Draft Banner displaying prompt to resume saved draft.
 */
@Composable
fun OfflineDraftBanner(
    draft: OfflineDraft,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StatusAmberContainer),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_draft_banner")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudOff, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Offline Draft Available: ${draft.title}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F)
                    )
                    Text(
                        text = "${draft.summary} • Saved locally",
                        fontSize = 10.sp,
                        color = Color(0xFF78350F).copy(alpha = 0.8f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = onDiscard,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Discard", fontSize = 11.sp, color = StatusRed)
                }
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAmber),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Resume", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

/**
 * Bottom Sheet displaying all active offline drafts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDraftsSheet(
    drafts: List<OfflineDraft>,
    onSelectDraft: (OfflineDraft) -> Unit,
    onDeleteDraft: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = StatusAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline Drafts & Form Backups", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                if (drafts.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Clear All", color = StatusRed, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Unsaved shipments, warehouse receipts, and dispatches are safely preserved offline on this Android phone.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (drafts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No pending offline drafts.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 360.dp)
                ) {
                    items(drafts) { draft ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDraft(draft) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(draft.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(draft.summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onDeleteDraft(draft.draftId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
