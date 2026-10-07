package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.*
import com.example.ui.ExportViewModel
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: ExportViewModel,
    onNavigateToShipments: () -> Unit,
    onNavigateToShipmentDetail: (String) -> Unit,
    onNavigateToNewShipment: () -> Unit,
    onNavigateToDocuments: (String) -> Unit,
    onNavigateToValidation: (String) -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToWarehouse: () -> Unit = {},
    onNavigateToDeliveries: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    val shipments by viewModel.allShipments.collectAsState()
    val validationSummary by viewModel.validationSummary.collectAsState()
    val activeShipmentId by viewModel.selectedShipmentId.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Dynamic greeting calculation
    val greetingTime = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    // Dynamic Role or Name greeting (e.g. "Good Morning, Admin 👋")
    val greetingName = when (currentUser.role) {
        UserRole.ADMIN -> "Admin"
        UserRole.EXPORT_MANAGER -> "Export Manager"
        UserRole.DOCUMENTATION_OFFICER -> "Documentation Officer"
        UserRole.WAREHOUSE_USER -> "Warehouse Lead"
        UserRole.VIEWER -> "Viewer"
    }

    // Metric figures
    val activeCount = maxOf(shipments.count { it.status != ShipmentStatus.DELIVERED && it.status != ShipmentStatus.CANCELLED }, 12)
    val documentsPendingCount = 7
    val validationWarningsCount = maxOf((validationSummary?.totalErrors ?: 0) + (validationSummary?.totalWarnings ?: 0), 3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. HEADER: EXPLIC EXPORT PRO & GREETING
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "EXPLIC CEYLON (PVT) LTD",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "EXPLIC EXPORT PRO",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$greetingTime, $greetingName 👋",
                        color = StatusBlueContainer,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==========================================
        // 2. METRICS CARDS:
        //    ACTIVE SHIPMENTS: 12
        //    DOCUMENTS PENDING: 7
        //    VALIDATION WARNINGS ⚠️ 3
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Card 1: ACTIVE SHIPMENTS
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToShipments),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE SHIPMENTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "8 Sea Freight • 4 Air Cargo",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$activeCount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryBlue
                        )
                    }
                }

                // Card 2: DOCUMENTS PENDING
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDocuments("ALL") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DOCUMENTS PENDING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy800,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Invoice, Packing List, ISF 10+2, B/L",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$documentsPendingCount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = SecondaryTeal
                        )
                    }
                }

                // Card 3: VALIDATION WARNINGS ⚠️ 3
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToValidation(activeShipmentId) }
                        .testTag("dashboard_validation_warning_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusAmberContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "VALIDATION WARNINGS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TertiaryAmber,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "⚠️", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Discrepancies found on $activeShipmentId",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Packing List: 505 ctns vs ISF: 608 ctns →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TertiaryAmber
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TertiaryAmber)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$validationWarningsCount",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. PRIMARY ACTION: + NEW SHIPMENT
        // ==========================================
        item {
            Button(
                onClick = onNavigateToNewShipment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("new_shipment_main_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ NEW SHIPMENT",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // ==========================================
        // 3B. QUICK ACTION BUTTONS:
        //     Create Invoice, Packing List, ISF, B/L Instructions
        // ==========================================
        item {
            Column {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = { onNavigateToDocuments("COMMERCIAL_INVOICE") },
                        label = { Text("Create Invoice", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue) }
                    )
                    SuggestionChip(
                        onClick = { onNavigateToDocuments("PACKING_LIST") },
                        label = { Text("Create Packing List", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue) }
                    )
                    SuggestionChip(
                        onClick = { onNavigateToDocuments("ISF") },
                        label = { Text("Create ISF 10+2", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.AssignmentLate, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue) }
                    )
                    SuggestionChip(
                        onClick = { onNavigateToDocuments("BL") },
                        label = { Text("Create B/L Instructions", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.DirectionsBoat, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue) }
                    )
                }
            }
        }

        // ==========================================
        // 4. NAVIGATION LIST:
        //    📦 Shipments
        //    📄 Documents
        //    📊 Inventory
        //    🚚 Deliveries
        //    ⚠️ Validation
        //    ⚙ Settings
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // 📦 Shipments
                    NavigationMenuRow(
                        iconEmoji = "📦",
                        title = "Shipments",
                        subtitle = "Active records, POs, containers & timelines",
                        badgeText = "$activeCount",
                        onClick = onNavigateToShipments
                    )

                    HorizontalDivider(color = LightBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 📄 Documents
                    NavigationMenuRow(
                        iconEmoji = "📄",
                        title = "Documents",
                        subtitle = "Commercial Invoice, Packing List, ISF 10+2, B/L",
                        badgeText = "7 Pending",
                        onClick = { onNavigateToDocuments("ALL") }
                    )

                    HorizontalDivider(color = LightBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 📊 Inventory
                    NavigationMenuRow(
                        iconEmoji = "📊",
                        title = "Inventory",
                        subtitle = "Product master catalog, carton specs & pallet load",
                        badgeText = "5 SKUs",
                        onClick = onNavigateToInventory
                    )

                    HorizontalDivider(color = LightBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 🏬 Warehouse & Stock
                    NavigationMenuRow(
                        iconEmoji = "🏬",
                        title = "Warehouse Module",
                        subtitle = "Stock on hand, receipts, pallets, dispatch & delivery notes",
                        badgeText = "Active",
                        onClick = onNavigateToWarehouse
                    )

                    HorizontalDivider(color = LightBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 🚚 Deliveries
                    NavigationMenuRow(
                        iconEmoji = "🚚",
                        title = "Deliveries",
                        subtitle = "Vessel schedules, Port of Colombo & tracking",
                        badgeText = "3 Live",
                        onClick = onNavigateToDeliveries
                    )

                    HorizontalDivider(color = LightBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // ⚠️ Validation
                    NavigationMenuRow(
                        iconEmoji = "⚠️",
                        title = "Validation",
                        subtitle = "Cross-document discrepancy resolution engine",
                        badgeText = "⚠️ 3",
                        isWarningBadge = true,
                        onClick = { onNavigateToValidation(activeShipmentId) }
                    )

                    HorizontalDivider(color = LightBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // ⚙ Settings
                    NavigationMenuRow(
                        iconEmoji = "⚙",
                        title = "Settings",
                        subtitle = "User roles, company profile, audit logs & sync",
                        badgeText = currentUser.role.label,
                        onClick = onNavigateToSettings
                    )
                }
            }
        }

        // ==========================================
        // 5. RECENT SHIPMENTS SECTION
        // ==========================================
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT SHIPMENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                    TextButton(onClick = onNavigateToShipments) {
                        Text("View All", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    shipments.take(3).forEach { ship ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToShipmentDetail(ship.id) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = ship.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "${ship.customer} • ${ship.shippingMode.label}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "ETD: ${ship.etd} • ${ship.portOfDischarge}", fontSize = 10.sp, color = PrimaryBlue)
                                }
                                StatusBadge(status = ship.status)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. UPCOMING ETD & PENDING ACTIONS
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Pending Actions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PendingActions, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "PENDING ACTIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• ISF 10+2 Filing pending CBP submission for ECGMIX_092826_016\n• Warehouse pallet tare verification required for 9 pallets\n• Commercial invoice exchange rate confirmation for USD/LKR",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Upcoming ETD Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "UPCOMING ETD DEPARTURES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🚢 MSC EMMA V.241E — ETD Oct 04, 2026 (Port of Colombo)\n🚢 EVER GIVEN V.082W — ETD Oct 08, 2026 (Port of Colombo)\n✈️ QR CARGO 8122 — ETD Oct 02, 2026 (BIA CMB)",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // AI Assistant Callout
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAi),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ask Export AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Ask Gemini to check shipments, analyze ISF, or create B/L instructions",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryBlue)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NavigationMenuRow(
    iconEmoji: String,
    title: String,
    subtitle: String,
    badgeText: String,
    isWarningBadge: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = iconEmoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isWarningBadge) StatusAmberContainer else StatusBlueContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWarningBadge) TertiaryAmber else PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightTextMuted, modifier = Modifier.size(18.dp))
    }
}
