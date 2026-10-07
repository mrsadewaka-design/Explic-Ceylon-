package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.ExportViewModel
import com.example.ui.theme.*

@Composable
fun MoreMenuScreen(
    viewModel: ExportViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToParties: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToExcel: () -> Unit,
    onNavigateToAi: () -> Unit,
    onOpenRoleSwitcher: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("more_menu_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active User & Role Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy900),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = currentUser.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = currentUser.department, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(text = "Role: ${currentUser.role.label}", color = StatusBlueContainer, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenRoleSwitcher,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Switch", fontSize = 11.sp)
                    }
                }
            }
        }

        // Connectivity Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("OFFLINE-FIRST SYNCHRONIZATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (syncState == "OFFLINE") StatusAmber else StatusGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(syncState, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Row {
                        TextButton(onClick = { viewModel.toggleOffline() }) {
                            Text(if (syncState == "OFFLINE") "Go Online" else "Simulate Offline", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.triggerSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Sync Now", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("MASTER DATA & TOOLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            MoreNavigationTile(
                title = "Reset Preview (Demo Mode)",
                subtitle = "Reintroduce discrepancies (505 vs 608 ctns, 143 vs 333 ctns & missing B/L weight)",
                icon = Icons.Default.RestartAlt,
                onClick = { viewModel.resetPreviewDiscrepancies() }
            )
        }

        item {
            MoreNavigationTile(
                title = "Product Master Database",
                subtitle = "Manage standard goods, HS codes, weights & carton dimensions",
                icon = Icons.Default.Category,
                onClick = onNavigateToProducts
            )
        }

        item {
            MoreNavigationTile(
                title = "Reusable Parties & Entities",
                subtitle = "Consignees, Exporters, Manufacturers, Shipping Agents",
                icon = Icons.Default.CorporateFare,
                onClick = onNavigateToParties
            )
        }

        item {
            MoreNavigationTile(
                title = "Ask Export AI",
                subtitle = "Gemini-powered shipment inspection, ISF review & B/L instructions",
                icon = Icons.Default.AutoAwesome,
                onClick = onNavigateToAi
            )
        }

        item {
            MoreNavigationTile(
                title = "Excel Import & Multi-Sheet Export",
                subtitle = "Parse legacy spreadsheets or download comprehensive workbook",
                icon = Icons.Default.TableChart,
                onClick = onNavigateToExcel
            )
        }

        item {
            MoreNavigationTile(
                title = "Audit Trail & Compliance Log",
                subtitle = "Review all user modifications, carton updates & manager overrides",
                icon = Icons.Default.History,
                onClick = onNavigateToAuditLogs
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Explic Ceylon (Private) Limited",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350\nReg: PV0028491 • FDA: 19482019482\nExports • Logistics • Documentation Management System v1.0",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MoreNavigationTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightTextMuted)
        }
    }
}
