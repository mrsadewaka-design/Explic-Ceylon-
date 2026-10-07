package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.Shipment
import com.example.data.model.ShipmentStatus
import com.example.ui.ExportViewModel
import com.example.ui.theme.*

data class DeliveryTrackingItem(
    val shipmentId: String,
    val vessel: String,
    val containerNumber: String,
    val destination: String,
    val origin: String = "Port of Colombo (CMB)",
    val etd: String,
    val eta: String,
    val carrier: String,
    val statusText: String,
    val currentMilestone: Int, // 1 to 5
    val isDelayed: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit,
    onSelectShipment: (String) -> Unit
) {
    val shipments by viewModel.allShipments.collectAsState()

    val deliveries = listOf(
        DeliveryTrackingItem(
            shipmentId = "ECGMIX_092826_016",
            vessel = "CMA CGM MEXICO / 1TUIUS1MA",
            containerNumber = "EMCU6409544/20",
            destination = "Deer Park, New York (USNYC)",
            origin = "Port of Colombo (LKCMB)",
            etd = "08-OCT-2026",
            eta = "28-OCT-2026",
            carrier = "CMA CGM Shipping Line",
            statusText = "Container Stuffing Completed at Colombo CFS",
            currentMilestone = 2
        ),
        DeliveryTrackingItem(
            shipmentId = "ECG_AIR_100226_017",
            vessel = "Qatar Airways Cargo QR8421",
            containerNumber = "PMC-49201-QR",
            destination = "Helsinki Vantaa (HEL), Finland",
            origin = "Colombo Bandaranaike (CMB)",
            etd = "03-OCT-2026",
            eta = "04-OCT-2026",
            carrier = "Qatar Airways Cargo",
            statusText = "Customs Export Clearance in Progress",
            currentMilestone = 2
        ),
        DeliveryTrackingItem(
            shipmentId = "ECGMIX_091526_015",
            vessel = "MSC OSCAR / 2408W",
            containerNumber = "MSCU9482104/40",
            destination = "Toronto, ON via Halifax, Canada",
            origin = "Port of Colombo (LKCMB)",
            etd = "20-SEP-2026",
            eta = "12-OCT-2026",
            carrier = "Mediterranean Shipping Co (MSC)",
            statusText = "In Transit (Indian Ocean → Suez Canal)",
            currentMilestone = 3
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Export Deliveries & Transit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Port of Colombo Departures & Vessels", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("deliveries_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "GLOBAL VESSEL TRACKING",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Live Cargo Shipments",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreenContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("3 IN TRANSIT", color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Origin: Explic Ceylon (Pvt) Ltd, Dehiwala → Port of Colombo Terminal",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(deliveries) { item ->
                DeliveryCard(
                    item = item,
                    onClick = {
                        viewModel.selectShipment(item.shipmentId)
                        onSelectShipment(item.shipmentId)
                    }
                )
            }
        }
    }
}

@Composable
fun DeliveryCard(
    item: DeliveryTrackingItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Column {
                    Text(
                        text = item.vessel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Ref: ${item.shipmentId} • ${item.carrier}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusBlueContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("TRACKING", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Routing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FROM", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = LightTextSecondary)
                    Text("Colombo, LK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("ETD: ${item.etd}", fontSize = 10.sp, color = PrimaryBlue)
                }

                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = LightTextMuted, modifier = Modifier.size(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text("TO", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = LightTextSecondary)
                    Text(item.destination.split(",").firstOrNull() ?: item.destination, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("ETA: ${item.eta}", fontSize = 10.sp, color = StatusGreen)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Milestones progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MilestoneDot(label = "Stuffing", isDone = item.currentMilestone >= 1, isCurrent = item.currentMilestone == 1)
                MilestoneDot(label = "Loaded", isDone = item.currentMilestone >= 2, isCurrent = item.currentMilestone == 2)
                MilestoneDot(label = "Departed", isDone = item.currentMilestone >= 3, isCurrent = item.currentMilestone == 3)
                MilestoneDot(label = "In Transit", isDone = item.currentMilestone >= 4, isCurrent = item.currentMilestone == 4)
                MilestoneDot(label = "Delivered", isDone = item.currentMilestone >= 5, isCurrent = item.currentMilestone == 5)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Container: ${item.containerNumber}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Navy800)
                Text(text = item.statusText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MilestoneDot(label: String, isDone: Boolean, isCurrent: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (isCurrent) PrimaryBlue else if (isDone) StatusGreen else LightBorder),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 8.sp, color = if (isCurrent || isDone) MaterialTheme.colorScheme.onSurface else LightTextMuted, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
    }
}
