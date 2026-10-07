package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.database.AppDatabase
import com.example.data.repository.ExportRepository
import com.example.ui.ExportViewModel
import com.example.ui.ExportViewModelFactory
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

sealed class AppDestination {
    object Dashboard : AppDestination()
    object ShipmentList : AppDestination()
    data class ShipmentDetail(val id: String) : AppDestination()
    object ShipmentCreate : AppDestination()
    object DocumentCenter : AppDestination()
    object CommercialInvoice : AppDestination()
    object PackingList : AppDestination()
    object IsfFiling : AppDestination()
    object BlInstructions : AppDestination()
    data class Validation(val id: String) : AppDestination()
    object WarehousePallets : AppDestination()
    object ProductMaster : AppDestination()
    object PartyMaster : AppDestination()
    object AuditLogs : AppDestination()
    object ExcelIo : AppDestination()
    object ExportAi : AppDestination()
    object Deliveries : AppDestination()
    object Settings : AppDestination()
    object MoreMenu : AppDestination()
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ExportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this, lifecycleScope)
        val repository = ExportRepository(database)
        viewModel = ViewModelProvider(this, ExportViewModelFactory(repository))[ExportViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: ExportViewModel) {
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Dashboard) }
    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showNotifsSheet by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadNotifsCount = notifications.count { !it.isRead }

    // Intercept hardware / gesture back navigation on sub-screens
    BackHandler(enabled = currentDestination !is AppDestination.Dashboard) {
        when (currentDestination) {
            is AppDestination.ShipmentDetail,
            is AppDestination.ShipmentCreate -> {
                currentDestination = AppDestination.ShipmentList
                selectedBottomTab = 1
            }
            is AppDestination.CommercialInvoice,
            is AppDestination.PackingList,
            is AppDestination.IsfFiling,
            is AppDestination.BlInstructions,
            is AppDestination.Validation -> {
                currentDestination = AppDestination.DocumentCenter
                selectedBottomTab = 2
            }
            is AppDestination.ProductMaster,
            is AppDestination.PartyMaster,
            is AppDestination.AuditLogs,
            is AppDestination.ExcelIo -> {
                currentDestination = AppDestination.MoreMenu
                selectedBottomTab = 4
            }
            is AppDestination.ExportAi,
            is AppDestination.Deliveries,
            is AppDestination.Settings -> {
                currentDestination = AppDestination.Dashboard
                selectedBottomTab = 0
            }
            else -> {
                currentDestination = AppDestination.Dashboard
                selectedBottomTab = 0
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EXPLIC EXPORT PRO",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = PrimaryBlue
                            )
                        }
                        Text(
                            text = "Explic Ceylon (Pvt) Ltd",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Sync Status Pill (Tappable to toggle/sync)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (syncState) {
                                    "OFFLINE" -> StatusAmberContainer
                                    "SYNCING" -> StatusBlueContainer
                                    else -> StatusGreenContainer
                                }
                            )
                            .clickable { viewModel.triggerSync() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (syncState) {
                                            "OFFLINE" -> StatusAmber
                                            "SYNCING" -> PrimaryBlue
                                            else -> StatusGreen
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = syncState,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (syncState) {
                                    "OFFLINE" -> StatusAmber
                                    "SYNCING" -> PrimaryBlue
                                    else -> StatusGreen
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Active User Role Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Navy900)
                            .clickable { showRoleDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("active_role_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser.role.label,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Ask Export AI Button
                    IconButton(
                        onClick = { currentDestination = AppDestination.ExportAi },
                        modifier = Modifier.testTag("topbar_ask_export_ai_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Ask Export AI", tint = PrimaryBlue)
                    }

                    // Reset Preview Action
                    IconButton(
                        onClick = { viewModel.resetPreviewDiscrepancies() },
                        modifier = Modifier.testTag("reset_preview_topbar_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Preview", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    // Notifications Action
                    IconButton(
                        onClick = { showNotifsSheet = true },
                        modifier = Modifier.testTag("notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(containerColor = StatusRed) {
                                        Text("$unreadNotifsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0 && currentDestination is AppDestination.Dashboard,
                    onClick = {
                        selectedBottomTab = 0
                        currentDestination = AppDestination.Dashboard
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("HOME", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = {
                        selectedBottomTab = 1
                        currentDestination = AppDestination.ShipmentList
                    },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Shipments") },
                    label = { Text("SHIPMENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = {
                        selectedBottomTab = 2
                        currentDestination = AppDestination.DocumentCenter
                    },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Documents") },
                    label = { Text("DOCS", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = {
                        selectedBottomTab = 3
                        currentDestination = AppDestination.WarehousePallets
                    },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Warehouse") },
                    label = { Text("WAREHOUSE", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 4,
                    onClick = {
                        selectedBottomTab = 4
                        currentDestination = AppDestination.MoreMenu
                    },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("MORE", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val dest = currentDestination) {
                is AppDestination.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToShipments = {
                            selectedBottomTab = 1
                            currentDestination = AppDestination.ShipmentList
                        },
                        onNavigateToShipmentDetail = { id ->
                            currentDestination = AppDestination.ShipmentDetail(id)
                        },
                        onNavigateToNewShipment = {
                            currentDestination = AppDestination.ShipmentCreate
                        },
                        onNavigateToDocuments = { docType ->
                            selectedBottomTab = 2
                            when (docType) {
                                "COMMERCIAL_INVOICE" -> currentDestination = AppDestination.CommercialInvoice
                                "PACKING_LIST" -> currentDestination = AppDestination.PackingList
                                "ISF" -> currentDestination = AppDestination.IsfFiling
                                "BL" -> currentDestination = AppDestination.BlInstructions
                                else -> currentDestination = AppDestination.DocumentCenter
                            }
                        },
                        onNavigateToValidation = { id ->
                            currentDestination = AppDestination.Validation(id)
                        },
                        onNavigateToInventory = {
                            currentDestination = AppDestination.ProductMaster
                        },
                        onNavigateToWarehouse = {
                            selectedBottomTab = 3
                            currentDestination = AppDestination.WarehousePallets
                        },
                        onNavigateToDeliveries = {
                            currentDestination = AppDestination.Deliveries
                        },
                        onNavigateToSettings = {
                            currentDestination = AppDestination.Settings
                        },
                        onNavigateToAi = {
                            currentDestination = AppDestination.ExportAi
                        }
                    )
                }

                is AppDestination.ShipmentList -> {
                    ShipmentListScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { id ->
                            currentDestination = AppDestination.ShipmentDetail(id)
                        },
                        onNavigateToCreate = {
                            currentDestination = AppDestination.ShipmentCreate
                        }
                    )
                }

                is AppDestination.ShipmentDetail -> {
                    ShipmentDetailScreen(
                        shipmentId = dest.id,
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.ShipmentList
                        },
                        onNavigateToDocuments = {
                            selectedBottomTab = 2
                            currentDestination = AppDestination.DocumentCenter
                        },
                        onNavigateToValidation = {
                            currentDestination = AppDestination.Validation(dest.id)
                        },
                        onNavigateToWarehouse = {
                            selectedBottomTab = 3
                            currentDestination = AppDestination.WarehousePallets
                        },
                        onNavigateToAi = {
                            currentDestination = AppDestination.ExportAi
                        }
                    )
                }

                is AppDestination.ShipmentCreate -> {
                    ShipmentEditScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.ShipmentList
                        },
                        onShipmentCreated = { newId ->
                            currentDestination = AppDestination.ShipmentDetail(newId)
                        }
                    )
                }

                is AppDestination.DocumentCenter -> {
                    DocumentCenterScreen(
                        viewModel = viewModel,
                        onNavigateToDocumentDetail = { docType ->
                            when (docType) {
                                "COMMERCIAL_INVOICE" -> currentDestination = AppDestination.CommercialInvoice
                                "PACKING_LIST" -> currentDestination = AppDestination.PackingList
                                "ISF" -> currentDestination = AppDestination.IsfFiling
                                "BL" -> currentDestination = AppDestination.BlInstructions
                            }
                        },
                        onNavigateToValidation = { id ->
                            currentDestination = AppDestination.Validation(id)
                        }
                    )
                }

                is AppDestination.CommercialInvoice -> {
                    CommercialInvoiceScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.DocumentCenter
                        }
                    )
                }

                is AppDestination.PackingList -> {
                    PackingListScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.DocumentCenter
                        }
                    )
                }

                is AppDestination.IsfFiling -> {
                    IsfScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.DocumentCenter
                        }
                    )
                }

                is AppDestination.BlInstructions -> {
                    BlScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.DocumentCenter
                        }
                    )
                }

                is AppDestination.Validation -> {
                    ValidationScreen(
                        shipmentId = dest.id,
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.DocumentCenter
                        },
                        onNavigateToAi = {
                            currentDestination = AppDestination.ExportAi
                        }
                    )
                }

                is AppDestination.WarehousePallets -> {
                    WarehouseScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            selectedBottomTab = 0
                            currentDestination = AppDestination.Dashboard
                        }
                    )
                }

                is AppDestination.ProductMaster -> {
                    ProductMasterScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.MoreMenu
                        }
                    )
                }

                is AppDestination.PartyMaster -> {
                    PartyMasterScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.MoreMenu
                        }
                    )
                }

                is AppDestination.AuditLogs -> {
                    AuditLogScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.MoreMenu
                        }
                    )
                }

                is AppDestination.ExcelIo -> {
                    ExcelImportExportScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.MoreMenu
                        }
                    )
                }

                is AppDestination.ExportAi -> {
                    ExportAssistantScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.Dashboard
                        }
                    )
                }

                is AppDestination.Deliveries -> {
                    DeliveriesScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.Dashboard
                        },
                        onSelectShipment = { id ->
                            currentDestination = AppDestination.ShipmentDetail(id)
                        }
                    )
                }

                is AppDestination.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentDestination = AppDestination.Dashboard
                        },
                        onOpenRoleSwitcher = {
                            showRoleDialog = true
                        },
                        onNavigateToAuditLogs = {
                            currentDestination = AppDestination.AuditLogs
                        }
                    )
                }

                is AppDestination.MoreMenu -> {
                    MoreMenuScreen(
                        viewModel = viewModel,
                        onNavigateToProducts = { currentDestination = AppDestination.ProductMaster },
                        onNavigateToParties = { currentDestination = AppDestination.PartyMaster },
                        onNavigateToAuditLogs = { currentDestination = AppDestination.AuditLogs },
                        onNavigateToExcel = { currentDestination = AppDestination.ExcelIo },
                        onNavigateToAi = { currentDestination = AppDestination.ExportAi },
                        onOpenRoleSwitcher = { showRoleDialog = true }
                    )
                }
            }
        }
    }

    // Role switcher dialog
    if (showRoleDialog) {
        RoleSwitcherDialog(
            viewModel = viewModel,
            onDismiss = { showRoleDialog = false }
        )
    }

    // Notifications sheet
    if (showNotifsSheet) {
        NotificationsSheet(
            viewModel = viewModel,
            onDismiss = { showNotifsSheet = false }
        )
    }
}
