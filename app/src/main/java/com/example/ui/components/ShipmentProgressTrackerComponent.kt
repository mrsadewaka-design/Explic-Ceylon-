package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * Milestone status definition for the visual progress tracker:
 * Specifically displays the 4 core export logistics milestones:
 * 1. Packing
 * 2. Invoiced
 * 3. Validated
 * 4. Dispatched
 */
enum class ShipmentMilestone(
    val title: String,
    val subtitle: String,
    val targetStatus: ShipmentStatus,
    val icon: ImageVector,
    val testTagId: String
) {
    PACKING(
        title = "Packing",
        subtitle = "Cartons, Pallets & CFS Stuffing",
        targetStatus = ShipmentStatus.PACKED,
        icon = Icons.Default.Inventory2,
        testTagId = "milestone_packing"
    ),
    INVOICED(
        title = "Invoiced",
        subtitle = "Commercial Invoice & Billing",
        targetStatus = ShipmentStatus.DOCUMENTATION,
        icon = Icons.Default.ReceiptLong,
        testTagId = "milestone_invoiced"
    ),
    VALIDATED(
        title = "Validated",
        subtitle = "Cross-Document Compliance",
        targetStatus = ShipmentStatus.ISF_FILED,
        icon = Icons.Default.VerifiedUser,
        testTagId = "milestone_validated"
    ),
    DISPATCHED(
        title = "Dispatched",
        subtitle = "Carrier Cargo Dispatch & Transit",
        targetStatus = ShipmentStatus.SHIPPED,
        icon = Icons.Default.LocalShipping,
        testTagId = "milestone_dispatched"
    );

    companion object {
        fun getMilestoneIndex(status: ShipmentStatus): Int {
            return when (status) {
                ShipmentStatus.SHIPPED, ShipmentStatus.DELIVERED -> 3
                ShipmentStatus.ISF_FILED, ShipmentStatus.BOOKING_CONFIRMED,
                ShipmentStatus.LOADED, ShipmentStatus.BL_RELEASED -> 2
                ShipmentStatus.DOCUMENTATION -> 1
                ShipmentStatus.PACKED -> 0
                else -> -1 // NEW, ORDER_CONFIRMED, PRODUCTION
            }
        }
    }
}

enum class MilestoneState {
    COMPLETED,
    IN_PROGRESS,
    PENDING
}

data class MilestoneUpdateInfo(
    val milestone: ShipmentMilestone,
    val state: MilestoneState,
    val updateHeadline: String,
    val updateDetails: String,
    val timestamp: String,
    val isVerified: Boolean
)

/**
 * Visual Progress Tracker Component for the Shipment view.
 * Displays milestone status updates: 'Packing', 'Invoiced', 'Validated', 'Dispatched'.
 */
@Composable
fun ShipmentProgressTrackerComponent(
    shipment: Shipment,
    products: List<ShipmentProduct> = emptyList(),
    pallets: List<Pallet> = emptyList(),
    invoice: CommercialInvoice? = null,
    validationSummary: ShipmentValidationSummary? = null,
    canEdit: Boolean = true,
    onStatusUpdate: (ShipmentStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMilestoneIdx = ShipmentMilestone.getMilestoneIndex(shipment.status)
    var selectedMilestone by remember(shipment.id, shipment.status) {
        val initial = if (currentMilestoneIdx >= 0) {
            ShipmentMilestone.entries[currentMilestoneIdx.coerceIn(0, 3)]
        } else {
            ShipmentMilestone.PACKING
        }
        mutableStateOf(initial)
    }
    var showUpdateHistory by remember { mutableStateOf(false) }

    val totalCartons = products.sumOf { it.cartons }
    val totalGrossWeight = products.sumOf { it.totalGrossWeightKg }
    val totalInvoiceAmount = invoice?.totalAmount ?: products.sumOf { it.totalAmountUsd }
    val validationErrors = validationSummary?.totalErrors ?: 0
    val isValidated = (validationErrors == 0 && ((validationSummary?.totalMatches ?: 0) > 0 || validationSummary?.canMarkComplete == true)) || shipment.isOverrideApproved

    // Compute update information for all 4 milestones
    val milestoneUpdates = remember(shipment, products, pallets, invoice, validationSummary) {
        ShipmentMilestone.entries.map { milestone ->
            val state = when {
                milestone.ordinal < currentMilestoneIdx -> MilestoneState.COMPLETED
                milestone.ordinal == currentMilestoneIdx -> MilestoneState.IN_PROGRESS
                currentMilestoneIdx == -1 && milestone == ShipmentMilestone.PACKING -> MilestoneState.IN_PROGRESS
                else -> MilestoneState.PENDING
            }

            when (milestone) {
                ShipmentMilestone.PACKING -> {
                    val headline = if (state == MilestoneState.COMPLETED || pallets.isNotEmpty()) {
                        "Packed $totalCartons Cartons • ${pallets.size} Pallets"
                    } else if (state == MilestoneState.IN_PROGRESS) {
                        "Packing in Progress • $totalCartons Cartons Scheduled"
                    } else {
                        "Warehouse packing scheduled"
                    }
                    val details = "Gross Weight: ${"%.1f".format(totalGrossWeight)} kg • CFS Stuffing: ${shipment.containerNumber.ifBlank { "Assigned" }}"
                    val ts = if (shipment.createdAt > 0) "Recorded" else "Pending"
                    MilestoneUpdateInfo(milestone, state, headline, details, ts, state == MilestoneState.COMPLETED || pallets.isNotEmpty())
                }
                ShipmentMilestone.INVOICED -> {
                    val headline = if (invoice != null || state == MilestoneState.COMPLETED) {
                        "Invoice #${invoice?.invoiceNumber ?: shipment.invoiceNumber} Issued"
                    } else if (state == MilestoneState.IN_PROGRESS) {
                        "Commercial Invoice Draft Prepared"
                    } else {
                        "Awaiting final packing quantities"
                    }
                    val details = "Total: ${shipment.currency} ${"%.2f".format(totalInvoiceAmount)} • Date: ${invoice?.invoiceDate ?: shipment.invoiceDate}"
                    val ts = invoice?.invoiceDate ?: shipment.invoiceDate
                    MilestoneUpdateInfo(milestone, state, headline, details, ts, invoice != null)
                }
                ShipmentMilestone.VALIDATED -> {
                    val headline = if (isValidated) {
                        "Compliance Verified (0 Discrepancies)"
                    } else if (validationErrors > 0) {
                        "$validationErrors Validation Errors Detected"
                    } else {
                        "Pending Cross-Document Audit"
                    }
                    val details = if (isValidated) {
                        "Invoice, Packing List, ISF 10+2 & B/L match perfectly"
                    } else {
                        "Requires verification of HS Codes, weights & quantities"
                    }
                    val ts = if (isValidated) "Audit Verified" else "Pending Audit"
                    MilestoneUpdateInfo(milestone, state, headline, details, ts, isValidated)
                }
                ShipmentMilestone.DISPATCHED -> {
                    val headline = if (state == MilestoneState.COMPLETED) {
                        "Cargo Dispatched • In Transit"
                    } else if (state == MilestoneState.IN_PROGRESS) {
                        "Booking Confirmed • Loading at Port"
                    } else {
                        "Scheduled ETD: ${shipment.etd}"
                    }
                    val details = "Carrier: ${shipment.vessel.ifBlank { "Ocean Liner" }} • POL: ${shipment.portOfLoading} → POD: ${shipment.portOfDischarge}"
                    val ts = "ETD: ${shipment.etd}"
                    MilestoneUpdateInfo(milestone, state, headline, details, ts, state == MilestoneState.COMPLETED)
                }
            }
        }
    }

    val completionPercent = when {
        currentMilestoneIdx >= 3 -> 100
        currentMilestoneIdx == 2 -> 75
        currentMilestoneIdx == 1 -> 50
        currentMilestoneIdx == 0 -> 25
        else -> 10
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_progress_tracker"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Visual Progress Tracker Label & Overall Progress Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StatusBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VISUAL PROGRESS TRACKER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Milestones: Packing → Invoiced → Validated → Dispatched",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Completion percentage pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (completionPercent == 100) StatusGreenContainer else StatusBlueContainer
                ) {
                    Text(
                        text = "$completionPercent% COMPLETE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completionPercent == 100) StatusGreen else PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Horizontal Progress Stepper with 4 core milestones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShipmentMilestone.entries.forEachIndexed { index, milestone ->
                    val updateInfo = milestoneUpdates[index]
                    val isSelected = selectedMilestone == milestone
                    val isCompleted = updateInfo.state == MilestoneState.COMPLETED
                    val isInProgress = updateInfo.state == MilestoneState.IN_PROGRESS

                    val nodeColor = when {
                        isCompleted -> StatusGreen
                        isInProgress -> PrimaryBlue
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val iconColor = when {
                        isCompleted || isInProgress -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    // Milestone Node
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMilestone = milestone }
                            .testTag(milestone.testTagId)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(nodeColor)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = milestone.icon,
                                    contentDescription = milestone.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = milestone.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected || isInProgress) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isCompleted -> StatusGreen
                                isInProgress -> PrimaryBlue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = when (updateInfo.state) {
                                MilestoneState.COMPLETED -> "Done"
                                MilestoneState.IN_PROGRESS -> "Active"
                                MilestoneState.PENDING -> "Next"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (updateInfo.state) {
                                MilestoneState.COMPLETED -> StatusGreen
                                MilestoneState.IN_PROGRESS -> PrimaryBlue
                                MilestoneState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }
                        )
                    }

                    // Connecting Line between nodes
                    if (index < ShipmentMilestone.entries.size - 1) {
                        val nextIsCompletedOrActive = milestoneUpdates[index + 1].state != MilestoneState.PENDING
                        val connectorColor = if (isCompleted && nextIsCompletedOrActive) StatusGreen else if (isCompleted) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(connectorColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Milestone Status Update Card
            val activeUpdateInfo = milestoneUpdates[selectedMilestone.ordinal]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = when (activeUpdateInfo.state) {
                    MilestoneState.COMPLETED -> StatusGreenContainer.copy(alpha = 0.45f)
                    MilestoneState.IN_PROGRESS -> StatusBlueContainer.copy(alpha = 0.45f)
                    MilestoneState.PENDING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = selectedMilestone.icon,
                                contentDescription = null,
                                tint = when (activeUpdateInfo.state) {
                                    MilestoneState.COMPLETED -> StatusGreen
                                    MilestoneState.IN_PROGRESS -> PrimaryBlue
                                    MilestoneState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Status Update: ${selectedMilestone.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // State Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (activeUpdateInfo.state) {
                                MilestoneState.COMPLETED -> StatusGreen
                                MilestoneState.IN_PROGRESS -> PrimaryBlue
                                MilestoneState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = when (activeUpdateInfo.state) {
                                    MilestoneState.COMPLETED -> "COMPLETED"
                                    MilestoneState.IN_PROGRESS -> "IN PROGRESS"
                                    MilestoneState.PENDING -> "PENDING"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (activeUpdateInfo.state) {
                                    MilestoneState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> Color.White
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = activeUpdateInfo.updateHeadline,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = activeUpdateInfo.updateDetails,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Actions
                    if (canEdit) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Quick Action to update shipment to this milestone
                            Button(
                                onClick = { onStatusUpdate(selectedMilestone.targetStatus) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_set_status_${selectedMilestone.name.lowercase()}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (selectedMilestone) {
                                        ShipmentMilestone.PACKING -> SecondaryTeal
                                        ShipmentMilestone.INVOICED -> PrimaryBlue
                                        ShipmentMilestone.VALIDATED -> if (isValidated) StatusGreen else StatusAmber
                                        ShipmentMilestone.DISPATCHED -> PrimaryBlueVariant
                                    }
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Update Status: ${selectedMilestone.title}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable all-milestones status timeline toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showUpdateHistory = !showUpdateHistory }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showUpdateHistory) "Hide Status History" else "View All Status Updates (4 Stages)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )
                Icon(
                    imageVector = if (showUpdateHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expanded All Milestones Detailed Timeline
            AnimatedVisibility(visible = showUpdateHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    milestoneUpdates.forEach { info ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (info.state) {
                                                MilestoneState.COMPLETED -> StatusGreen
                                                MilestoneState.IN_PROGRESS -> PrimaryBlue
                                                MilestoneState.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = info.milestone.icon,
                                        contentDescription = null,
                                        tint = if (info.state == MilestoneState.PENDING) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = info.milestone.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = info.timestamp,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = info.updateHeadline,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact mini-stepper to display the 4 status updates directly inside shipment list cards.
 */
@Composable
fun ShipmentMiniMilestoneTracker(
    status: ShipmentStatus,
    modifier: Modifier = Modifier
) {
    val currentIdx = ShipmentMilestone.getMilestoneIndex(status)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShipmentMilestone.entries.forEachIndexed { index, milestone ->
            val isPassed = index < currentIdx
            val isCurrent = index == currentIdx

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPassed -> StatusGreen
                                isCurrent -> PrimaryBlue
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    } else if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = milestone.title,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isPassed -> StatusGreen
                        isCurrent -> PrimaryBlue
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (index < ShipmentMilestone.entries.size - 1) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
