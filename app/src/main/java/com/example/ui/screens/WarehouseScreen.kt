package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.engine.DocumentGenerator
import com.example.ui.ExportViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val stockItems by viewModel.allWarehouseStock.collectAsState()
    val receipts by viewModel.allGoodsReceipts.collectAsState()
    val dispatches by viewModel.allWarehouseDispatches.collectAsState()
    val deliveryNotes by viewModel.allDeliveryNotes.collectAsState()
    val pallets by viewModel.currentPallets.collectAsState()
    val shipment by viewModel.currentShipment.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val allShipments by viewModel.allShipments.collectAsState()
    val masterProducts by viewModel.allProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showScannerDialog by remember { mutableStateOf(false) }
    var showReceiveDialog by remember { mutableStateOf(false) }
    var showPalletDialog by remember { mutableStateOf(false) }
    var palletToEdit by remember { mutableStateOf<Pallet?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var targetPalletForAssign by remember { mutableStateOf<Pallet?>(null) }
    var showMeasureDialog by remember { mutableStateOf(false) }
    var itemToMeasure by remember { mutableStateOf<WarehouseStockItem?>(null) }
    var showDispatchDialog by remember { mutableStateOf(false) }
    var showDeliveryNoteDialog by remember { mutableStateOf(false) }
    var noteToPreview by remember { mutableStateOf<DeliveryNote?>(null) }

    val canEdit = currentUser.role == UserRole.WAREHOUSE_USER ||
            currentUser.role == UserRole.ADMIN ||
            currentUser.role == UserRole.DOCUMENTATION_OFFICER ||
            currentUser.role == UserRole.EXPORT_MANAGER

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WAREHOUSE MODULE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SecondaryTealContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "HUB DEHIWALA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryTeal
                                )
                            }
                        }
                        Text(
                            text = "Stock Dashboard • Inward GRN • Pallets • Dispatch • Delivery Notes",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Barcode / QR Scanner button
                    FilledTonalButton(
                        onClick = { showScannerDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SecondaryTealContainer,
                            contentColor = SecondaryTeal
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("warehouse_barcode_scan_btn")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SCAN", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Primary Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryBlue,
                divider = { HorizontalDivider(color = LightBorder) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Stock Dashboard", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Receive Goods (${receipts.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.MoveToInbox, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Pallets & Cartons (${pallets.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Dispatch (${dispatches.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Delivery Notes (${deliveryNotes.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> StockDashboardTab(
                    stockItems = stockItems,
                    canEdit = canEdit,
                    onOpenScan = { showScannerDialog = true },
                    onReceiveItem = { item ->
                        itemToMeasure = item
                        showReceiveDialog = true
                    },
                    onMeasureItem = { item ->
                        itemToMeasure = item
                        showMeasureDialog = true
                    },
                    onPalletize = {
                        selectedTab = 2
                    }
                )
                1 -> ReceiveGoodsTab(
                    receipts = receipts,
                    canEdit = canEdit,
                    onOpenReceiveDialog = { showReceiveDialog = true },
                    onOpenScanner = { showScannerDialog = true }
                )
                2 -> PalletsTab(
                    pallets = pallets,
                    shipment = shipment,
                    products = products,
                    canEdit = canEdit,
                    onAddPallet = {
                        val nextNum = "Pallet ${pallets.size + 1}"
                        palletToEdit = Pallet(
                            shipmentId = shipment?.id ?: "ECGMIX_092826_016",
                            palletNumber = nextNum,
                            cartons = 0,
                            productsSummary = "General Cargo",
                            weightKg = 25.0,
                            cbm = 1.55
                        )
                        showPalletDialog = true
                    },
                    onEditPallet = { p ->
                        palletToEdit = p
                        showPalletDialog = true
                    },
                    onAssignCartons = { p ->
                        targetPalletForAssign = p
                        showAssignDialog = true
                    },
                    onDeletePallet = { p ->
                        viewModel.deletePallet(p)
                    }
                )
                3 -> DispatchTab(
                    dispatches = dispatches,
                    shipment = shipment,
                    canEdit = canEdit,
                    onRecordDispatch = { showDispatchDialog = true }
                )
                4 -> DeliveryNotesTab(
                    deliveryNotes = deliveryNotes,
                    shipment = shipment,
                    canEdit = canEdit,
                    onCreateNote = { showDeliveryNoteDialog = true },
                    onPreviewNote = { noteToPreview = it }
                )
            }
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    // 1. Barcode / QR Scanner Dialog
    if (showScannerDialog) {
        BarcodeScannerDialog(
            stockItems = stockItems,
            onDismiss = { showScannerDialog = false },
            onItemAction = { action, item ->
                showScannerDialog = false
                when (action) {
                    "RECEIVE" -> {
                        itemToMeasure = item
                        showReceiveDialog = true
                    }
                    "MEASURE" -> {
                        itemToMeasure = item
                        showMeasureDialog = true
                    }
                    "PALLETIZE" -> {
                        selectedTab = 2
                    }
                }
            }
        )
    }

    // 2. Receive Goods Dialog (GRN)
    if (showReceiveDialog) {
        ReceiveGoodsDialog(
            preselectedItem = itemToMeasure,
            masterProducts = masterProducts,
            allShipments = allShipments,
            currentShipment = shipment,
            currentUser = currentUser,
            onDismiss = {
                showReceiveDialog = false
                itemToMeasure = null
            },
            onSaveDraft = { receipt ->
                viewModel.saveOfflineDraft(
                    draftId = receipt.receiptNumber,
                    draftType = "GOODS_RECEIPT",
                    title = "GRN: ${receipt.productName}",
                    summary = "${receipt.cartonsReceived} ctns • ${receipt.supplierName} • Bay: ${receipt.locationBay}",
                    payloadJson = """{"product":"${receipt.productName}","cartons":${receipt.cartonsReceived}}"""
                )
                showReceiveDialog = false
                itemToMeasure = null
            },
            onConfirm = { receipt, autoUpdateShipment ->
                viewModel.receiveGoods(receipt, autoUpdateShipment)
                showReceiveDialog = false
                itemToMeasure = null
            }
        )
    }

    // 3. Pallet Edit / Create Dialog
    if (showPalletDialog && palletToEdit != null) {
        PalletEditDialog(
            pallet = palletToEdit!!,
            onDismiss = {
                showPalletDialog = false
                palletToEdit = null
            },
            onSave = { updated ->
                viewModel.savePallet(updated)
                showPalletDialog = false
                palletToEdit = null
            }
        )
    }

    // 4. Assign Cartons to Pallet Dialog
    if (showAssignDialog && targetPalletForAssign != null) {
        AssignCartonsToPalletDialog(
            pallet = targetPalletForAssign!!,
            stockItems = stockItems,
            products = products,
            onDismiss = {
                showAssignDialog = false
                targetPalletForAssign = null
            },
            onAssign = { palletId, shipmentId, stockItemId, productName, cartons, ctnGross, cbm ->
                viewModel.assignCartonsToPallet(
                    palletId = palletId,
                    shipmentId = shipmentId,
                    stockItemId = stockItemId,
                    productName = productName,
                    cartonsToAssign = cartons,
                    cartonGrossWeightKg = ctnGross,
                    cbmPerCarton = cbm
                )
                showAssignDialog = false
                targetPalletForAssign = null
            }
        )
    }

    // 5. Record Carton Measurements & Weights Dialog
    if (showMeasureDialog && itemToMeasure != null) {
        RecordDimensionsDialog(
            item = itemToMeasure!!,
            onDismiss = {
                showMeasureDialog = false
                itemToMeasure = null
            },
            onSave = { stockId, length, width, height, grossWeight, qty, bay ->
                viewModel.recordCartonMeasurements(
                    stockItemId = stockId,
                    lengthCm = length,
                    widthCm = width,
                    heightCm = height,
                    cartonGrossWeightKg = grossWeight,
                    cartonQuantity = qty,
                    locationBay = bay
                )
                showMeasureDialog = false
                itemToMeasure = null
            }
        )
    }

    // 6. Record Dispatch Dialog
    if (showDispatchDialog) {
        RecordDispatchDialog(
            currentShipment = shipment,
            allShipments = allShipments,
            pallets = pallets,
            currentUser = currentUser,
            onDismiss = { showDispatchDialog = false },
            onConfirm = { dispatch, createDeliveryNote ->
                viewModel.recordDispatch(dispatch, createDeliveryNote) { createdNote ->
                    if (createdNote != null) {
                        noteToPreview = createdNote
                    }
                }
                showDispatchDialog = false
            }
        )
    }

    // 7. Create Delivery Note Dialog
    if (showDeliveryNoteDialog) {
        CreateDeliveryNoteDialog(
            currentShipment = shipment,
            pallets = pallets,
            currentUser = currentUser,
            onDismiss = { showDeliveryNoteDialog = false },
            onConfirm = { note ->
                viewModel.createDeliveryNote(note)
                showDeliveryNoteDialog = false
                noteToPreview = note
            }
        )
    }

    // 8. Delivery Note PDF / A4 Preview Dialog
    if (noteToPreview != null) {
        DeliveryNotePreviewDialog(
            deliveryNote = noteToPreview!!,
            shipment = shipment,
            onDismiss = { noteToPreview = null }
        )
    }
}

// Backward compatible alias
@Composable
fun WarehousePalletScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    WarehouseScreen(viewModel = viewModel, onNavigateBack = onNavigateBack, initialTab = 2)
}

// =========================================================================
// TAB 0: WAREHOUSE STOCK DASHBOARD
// =========================================================================

@Composable
fun StockDashboardTab(
    stockItems: List<WarehouseStockItem>,
    canEdit: Boolean,
    onOpenScan: () -> Unit,
    onReceiveItem: (WarehouseStockItem) -> Unit,
    onMeasureItem: (WarehouseStockItem) -> Unit,
    onPalletize: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBayFilter by remember { mutableStateOf("ALL") }

    val totalCartonsOnHand = stockItems.sumOf { it.availableCartons + it.allocatedCartons }
    val totalAvailableCartons = stockItems.sumOf { it.availableCartons }
    val totalAllocatedCartons = stockItems.sumOf { it.allocatedCartons }
    val totalDispatchedCartons = stockItems.sumOf { it.dispatchedCartons }
    val totalStockWeightKg = stockItems.sumOf { (it.availableCartons + it.allocatedCartons) * it.cartonGrossWeightKg }
    val totalStockCbm = stockItems.sumOf { (it.availableCartons + it.allocatedCartons) * it.cbmPerCarton }
    val distinctBays = remember(stockItems) {
        stockItems.map { it.locationBay }.filter { it.isNotBlank() }.distinct()
    }

    val filteredItems = stockItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.productName.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true) ||
                item.batchLotNumber.contains(searchQuery, ignoreCase = true) ||
                item.locationBay.contains(searchQuery, ignoreCase = true)
        val matchesBay = selectedBayFilter == "ALL" || item.locationBay.startsWith(selectedBayFilter)
        matchesSearch && matchesBay
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("warehouse_stock_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Warehouse Overview KPI Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy900),
                shape = RoundedCornerShape(14.dp),
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
                                text = "WAREHOUSE STOCK INVENTORY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Dehiwala Logistics & Stuffing Hub",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Export Cargo Depot • CFS Staging & High-Rack Bays",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(
                            onClick = onOpenScan,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SecondaryTeal)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // KPI Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StockKpiMetric(label = "Cartons on Hand", value = "$totalCartonsOnHand ctns", highlightColor = Color.White)
                        StockKpiMetric(label = "Available Stock", value = "$totalAvailableCartons ctns", highlightColor = StatusGreen)
                        StockKpiMetric(label = "Allocated to Pallets", value = "$totalAllocatedCartons ctns", highlightColor = PrimaryBlue)
                        StockKpiMetric(label = "Dispatched", value = "$totalDispatchedCartons ctns", highlightColor = SecondaryTeal)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StockKpiMetric(label = "Gross Mass (SOLAS)", value = "%.1f kg".format(totalStockWeightKg), highlightColor = Color.White)
                        StockKpiMetric(label = "Total Volume", value = "%.3f CBM".format(totalStockCbm), highlightColor = Color.White)
                        StockKpiMetric(label = "Active Bays", value = "${distinctBays.size} Bays", highlightColor = Color.White)
                        StockKpiMetric(label = "SKUs Tracked", value = "${stockItems.size} SKUs", highlightColor = Color.White)
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by SKU, barcode, lot # or bay...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("warehouse_stock_search_input")
                )

                // Bay Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedBayFilter == "ALL",
                        onClick = { selectedBayFilter = "ALL" },
                        label = { Text("All Bays (${stockItems.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedBayFilter == "Bay A",
                        onClick = { selectedBayFilter = "Bay A" },
                        label = { Text("Bay A (Hockey/Lids)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedBayFilter == "Bay B",
                        onClick = { selectedBayFilter = "Bay B" },
                        label = { Text("Bay B (Plastics)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedBayFilter == "Bay C",
                        onClick = { selectedBayFilter = "Bay C" },
                        label = { Text("Bay C (Pet/Aqua)", fontSize = 11.sp) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORED INVENTORY (${filteredItems.size} SKUS)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap item to edit dimensions / weight",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of Stock Items
        items(filteredItems) { stockItem ->
            WarehouseStockCard(
                item = stockItem,
                canEdit = canEdit,
                onMeasure = { onMeasureItem(stockItem) },
                onReceive = { onReceiveItem(stockItem) },
                onPalletize = onPalletize
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun StockKpiMetric(label: String, value: String, highlightColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
        Text(text = value, color = highlightColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WarehouseStockCard(
    item: WarehouseStockItem,
    canEdit: Boolean,
    onMeasure: () -> Unit,
    onReceive: () -> Unit,
    onPalletize: () -> Unit
) {
    val totalCartons = item.availableCartons + item.allocatedCartons
    val allocatedPct = if (totalCartons > 0) item.allocatedCartons.toFloat() / totalCartons.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "HS: ${item.hsCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Lot: ${item.batchLotNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Bay Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryBlueContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = item.locationBay, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barcode representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryTeal)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.barcode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${item.unitsPerCarton} units/ctn",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantities breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Available", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${item.availableCartons} ctns", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                }
                Column {
                    Text(text = "Allocated Pallets", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${item.allocatedCartons} ctns", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
                Column {
                    Text(text = "Dispatched", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${item.dispatchedCartons} ctns", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text(text = "Total Received", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${item.totalReceivedCartons} ctns", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Allocation progress bar
            LinearProgressIndicator(
                progress = { allocatedPct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = StatusGreenContainer
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Measurements breakdown: Dimensions & Weights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dimensions: ${item.lengthCm.toInt()}x${item.widthCm.toInt()}x${item.heightCm.toInt()} cm",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Gross: ${item.cartonGrossWeightKg} kg • Vol: %.4f CBM/ctn".format(item.cbmPerCarton),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (canEdit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = onMeasure,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Measure", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onReceive,
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Receive", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: RECEIVE GOODS (GOODS RECEIPT / INWARD GRN)
// =========================================================================

@Composable
fun ReceiveGoodsTab(
    receipts: List<GoodsReceipt>,
    canEdit: Boolean,
    onOpenReceiveDialog: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val totalReceivedCartons = receipts.sumOf { it.cartonsReceived }
    val totalReceivedUnits = receipts.sumOf { it.totalUnits }
    val totalGrossWeight = receipts.sumOf { it.totalGrossWeightKg }
    val totalCbm = receipts.sumOf { it.totalCbm }

    Scaffold(
        floatingActionButton = {
            if (canEdit) {
                ExtendedFloatingActionButton(
                    onClick = onOpenReceiveDialog,
                    containerColor = SecondaryTeal,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Receive Goods (GRN)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_receive_goods")
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("receive_goods_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF064E3B).copy(alpha = 0.4f) else StatusGreenContainer
                    ),
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
                                    text = "INWARD GOODS RECEIPTS (GRN)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color(0xFF6EE7B7) else Color(0xFF065F46)
                                )
                                Text(
                                    text = "Factory Inward & Supplier Deliveries",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color.White else Color(0xFF064E3B)
                                )
                            }
                            IconButton(onClick = onOpenScanner) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = SecondaryTeal)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total GRNs", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${receipts.size} Receipts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Cartons", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "$totalReceivedCartons ctns", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Total Units", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "$totalReceivedUnits pcs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Gross Weight", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "%.1f kg".format(totalGrossWeight), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECEIPT LOG (${receipts.size} ENTRIES)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(receipts) { receipt ->
                GoodsReceiptCard(receipt = receipt)
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun GoodsReceiptCard(receipt: GoodsReceipt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = receipt.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryTeal)
                    Text(text = "Supplier: ${receipt.supplierName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${receipt.cartonsReceived} Cartons",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = receipt.productName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            if (!receipt.shipmentId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(13.dp), tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Linked Shipment: ${receipt.shipmentId}",
                        fontSize = 11.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dims: ${receipt.lengthCm.toInt()}x${receipt.widthCm.toInt()}x${receipt.heightCm.toInt()} cm • Wt: ${receipt.cartonGrossWeightKg} kg/ctn",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: %.1f kg • %.3f CBM".format(receipt.totalGrossWeightKg, receipt.totalCbm),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bay: ${receipt.locationBay} • By: ${receipt.receivedBy}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = receipt.receivedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (receipt.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${receipt.notes}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =========================================================================
// TAB 2: PALLETS & CARTON PACKING
// =========================================================================

@Composable
fun PalletsTab(
    pallets: List<Pallet>,
    shipment: Shipment?,
    products: List<ShipmentProduct>,
    canEdit: Boolean,
    onAddPallet: () -> Unit,
    onEditPallet: (Pallet) -> Unit,
    onAssignCartons: (Pallet) -> Unit,
    onDeletePallet: (Pallet) -> Unit
) {
    val totalAllocatedCartons = pallets.sumOf { it.cartons }
    val totalRequiredCartons = products.sumOf { it.cartons }
    val totalPalletWeight = pallets.sumOf { it.weightKg }
    val totalPalletCbm = pallets.sumOf { it.cbm }

    Scaffold(
        floatingActionButton = {
            if (canEdit && shipment != null) {
                FloatingActionButton(
                    onClick = onAddPallet,
                    containerColor = SecondaryTeal,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_pallet")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Pallet")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("warehouse_pallet_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WAREHOUSE PALLET STUFFING & PACKING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Shipment: ${shipment?.id ?: "N/A"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Container: ${shipment?.containerNumber ?: "N/A"} • Target: 1x20' GP",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PalletMetricHeader("Total Pallets", "${pallets.size}")
                            PalletMetricHeader("Cartons Allocated", "$totalAllocatedCartons / $totalRequiredCartons")
                            PalletMetricHeader("Total Gross Wt", "%.1f kg".format(totalPalletWeight))
                            PalletMetricHeader("Total CBM", "%.3f".format(totalPalletCbm))
                        }
                    }
                }
            }

            if (totalAllocatedCartons != totalRequiredCartons && totalRequiredCartons > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSystemInDarkTheme()) Color(0xFF78350F).copy(alpha = 0.4f) else StatusAmberContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else StatusAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Allocation Discrepancy: $totalAllocatedCartons cartons allocated on pallets, but shipment requires $totalRequiredCartons cartons.",
                                fontSize = 11.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFFDE68A) else Color(0xFF78350F)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PALLET LOAD PLAN (${pallets.size} PALLETS)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(pallets) { pallet ->
                EnhancedPalletCard(
                    pallet = pallet,
                    canEdit = canEdit,
                    onEdit = { onEditPallet(pallet) },
                    onAssign = { onAssignCartons(pallet) },
                    onDelete = { onDeletePallet(pallet) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun EnhancedPalletCard(
    pallet: Pallet,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onAssign: () -> Unit,
    onDelete: () -> Unit
) {
    val utilization = if (pallet.maxCartonCapacity > 0) {
        (pallet.cartons.toFloat() / pallet.maxCartonCapacity.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = pallet.palletNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryTeal)
                    Text(text = pallet.palletType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusBlueContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pallet.cartons} Cartons",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = pallet.productsSummary, fontSize = 12.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(10.dp))

            // Utilization Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Pallet Utilization", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${(utilization * 100).toInt()}% (${pallet.cartons}/${pallet.maxCartonCapacity} max)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { utilization },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (utilization >= 0.9f) StatusGreen else PrimaryBlue,
                trackColor = LightSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gross: ${pallet.weightKg} kg • Vol: ${pallet.cbm} CBM",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (canEdit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = onAssign,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Assign Cartons", fontSize = 11.sp)
                        }

                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PalletMetricHeader(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// =========================================================================
// TAB 3: DISPATCH & CONTAINER GATE-OUT
// =========================================================================

@Composable
fun DispatchTab(
    dispatches: List<WarehouseDispatch>,
    shipment: Shipment?,
    canEdit: Boolean,
    onRecordDispatch: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (canEdit) {
                ExtendedFloatingActionButton(
                    onClick = onRecordDispatch,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    text = { Text("Record Dispatch", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_record_dispatch")
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("dispatch_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WAREHOUSE DISPATCH & GATE OUT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Container Stuffing & Terminal Departures",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Record trailer numbers, container stuffing verification, seal checks, and driver release for Port of Colombo gate-in.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "DISPATCH RECORDS (${dispatches.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (dispatches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No dispatch records yet.", fontWeight = FontWeight.Bold)
                            Text("Tap 'Record Dispatch' to record stuffing and generate a Delivery Note.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(dispatches) { dispatch ->
                DispatchCard(dispatch = dispatch)
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun DispatchCard(dispatch: WarehouseDispatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = dispatch.dispatchNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                    Text(text = "Customer: ${dispatch.customer}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dispatch.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Container: ${dispatch.containerNumber} • Seal: ${dispatch.sealNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vehicle: ${dispatch.vehicleNumber} • Driver: ${dispatch.driverName} (${dispatch.driverNic})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dispatched: ${dispatch.totalCartonsDispatched} ctns on ${dispatch.totalPalletsDispatched} pallets",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%.1f kg • %.3f CBM".format(dispatch.totalGrossWeightKg, dispatch.totalCbm),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By: ${dispatch.dispatchedBy} • DN: ${dispatch.deliveryNoteNumber}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dispatch.dispatchDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =========================================================================
// TAB 4: DELIVERY NOTES
// =========================================================================

@Composable
fun DeliveryNotesTab(
    deliveryNotes: List<DeliveryNote>,
    shipment: Shipment?,
    canEdit: Boolean,
    onCreateNote: () -> Unit,
    onPreviewNote: (DeliveryNote) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (canEdit) {
                ExtendedFloatingActionButton(
                    onClick = onCreateNote,
                    containerColor = SecondaryTeal,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Delivery Note", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_create_delivery_note")
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("delivery_notes_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DELIVERY NOTES & CONTAINER GATE PASSES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Official Export Cargo Dispatch Notes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Professional delivery notes with Solas gross weight verification, driver receipts, container seals, and instant A4 printing & PDF download.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "GENERATED DELIVERY NOTES (${deliveryNotes.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(deliveryNotes) { note ->
                DeliveryNoteCard(
                    note = note,
                    onPreview = { onPreviewNote(note) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun DeliveryNoteCard(
    note: DeliveryNote,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = note.noteNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryTeal)
                    Text(text = "Consignee: ${note.consigneeName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = onPreview,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View & Print", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Container: ${note.containerNumber} • Seal: ${note.sealNumber}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Vehicle: ${note.vehicleNumber} • Driver: ${note.driverName} (${note.driverPhone})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${note.totalCartons} Cartons on ${note.totalPallets} Pallets",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%.1f kg • %.3f CBM".format(note.totalGrossWeightKg, note.totalCbm),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Prepared by: ${note.preparedBy}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = note.date,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =========================================================================
// BARCODE / QR SCANNER DIALOG
// =========================================================================

@Composable
fun BarcodeScannerDialog(
    stockItems: List<WarehouseStockItem>,
    onDismiss: () -> Unit,
    onItemAction: (action: String, item: WarehouseStockItem) -> Unit
) {
    var scannedInput by remember { mutableStateOf("EXP-PUCK-9506") }
    val matchedItem = stockItems.find { it.barcode.equals(scannedInput.trim(), ignoreCase = true) }

    // Animated scan line
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = SecondaryTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Barcode & QR Code Scanner", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Viewfinder simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(2.dp, SecondaryTeal, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.White.copy(alpha = 0.4f))
                        Text("Align barcode / QR within frame", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }

                    // Laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.04f)
                            .align(Alignment.TopCenter)
                            .offset(y = (laserOffset * 130).dp)
                            .background(Color.Red.copy(alpha = 0.8f))
                    )
                }

                Text(
                    text = "QUICK SCAN PRESETS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick presets chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "EXP-PUCK-9506" to "Hockey Pucks",
                        "EXP-LIDS-8309" to "Tin Lids",
                        "EXP-CUPS-3924" to "Plastic Cups",
                        "EXP-FEED-2309" to "Koi Feed",
                        "EXP-DOG-2309" to "Dog Clusters"
                    ).forEach { (code, name) ->
                        SuggestionChip(
                            onClick = { scannedInput = code },
                            label = { Text("$name ($code)", fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = scannedInput,
                    onValueChange = { scannedInput = it },
                    label = { Text("Scanned Barcode / QR String") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (matchedItem != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusGreenContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = matchedItem.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bay: ${matchedItem.locationBay} • Available: ${matchedItem.availableCartons} ctns • Allocated: ${matchedItem.allocatedCartons} ctns",
                                fontSize = 11.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Dims: ${matchedItem.lengthCm}x${matchedItem.widthCm}x${matchedItem.heightCm} cm • Gross: ${matchedItem.cartonGrossWeightKg} kg",
                                fontSize = 10.sp,
                                color = Color(0xFF065F46)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onItemAction("RECEIVE", matchedItem) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Receive", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onItemAction("MEASURE", matchedItem) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Measure", fontSize = 11.sp)
                                }
                                FilledTonalButton(
                                    onClick = { onItemAction("PALLETIZE", matchedItem) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Pallets", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No stock item found matching barcode '$scannedInput'.",
                        fontSize = 11.sp,
                        color = StatusAmber
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// =========================================================================
// RECEIVE GOODS DIALOG (INWARD GRN)
// =========================================================================

@Composable
fun ReceiveGoodsDialog(
    preselectedItem: WarehouseStockItem?,
    masterProducts: List<Product>,
    allShipments: List<Shipment>,
    currentShipment: Shipment?,
    currentUser: User,
    onDismiss: () -> Unit,
    onSaveDraft: ((GoodsReceipt) -> Unit)? = null,
    onConfirm: (receipt: GoodsReceipt, autoUpdateShipment: Boolean) -> Unit
) {
    val sdfDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val defaultReceiptNum = remember { "GRN-2026-${(1000..9999).random()}" }

    var selectedProductName by remember { mutableStateOf(preselectedItem?.productName ?: masterProducts.firstOrNull()?.name ?: "Ice Hockey Pucks") }
    var selectedProductId by remember { mutableLongStateOf(preselectedItem?.productId ?: masterProducts.firstOrNull()?.id ?: 1L) }
    var supplierName by remember { mutableStateOf("Explic Ceylon Factory") }
    var linkedShipmentId by remember { mutableStateOf(currentShipment?.id ?: "") }
    var batchNumber by remember { mutableStateOf(preselectedItem?.batchLotNumber ?: "LOT-2026-0${(10..99).random()}") }
    var barcode by remember { mutableStateOf(preselectedItem?.barcode ?: "EXP-PUCK-9506") }
    var cartonsReceivedText by remember { mutableStateOf("100") }
    var unitsPerCartonText by remember { mutableStateOf(preselectedItem?.unitsPerCarton?.toString() ?: "50") }
    var lengthCmText by remember { mutableStateOf(preselectedItem?.lengthCm?.toString() ?: "40.0") }
    var widthCmText by remember { mutableStateOf(preselectedItem?.widthCm?.toString() ?: "30.0") }
    var heightCmText by remember { mutableStateOf(preselectedItem?.heightCm?.toString() ?: "20.0") }
    var cartonGrossWeightText by remember { mutableStateOf(preselectedItem?.cartonGrossWeightKg?.toString() ?: "9.2") }
    var locationBay by remember { mutableStateOf(preselectedItem?.locationBay ?: "Bay A-01") }
    var notes by remember { mutableStateOf("Inspected and accepted in good order.") }
    var autoUpdateShipment by remember { mutableStateOf(true) }

    val cartonsInt = cartonsReceivedText.toIntOrNull() ?: 0
    val unitsInt = unitsPerCartonText.toIntOrNull() ?: 0
    val lengthDbl = lengthCmText.toDoubleOrNull() ?: 0.0
    val widthDbl = widthCmText.toDoubleOrNull() ?: 0.0
    val heightDbl = heightCmText.toDoubleOrNull() ?: 0.0
    val cartonGrossDbl = cartonGrossWeightText.toDoubleOrNull() ?: 0.0

    val calculatedTotalUnits = cartonsInt * unitsInt
    val calculatedTotalGrossWeight = cartonsInt * cartonGrossDbl
    val calculatedTotalCbm = if (lengthDbl > 0 && widthDbl > 0 && heightDbl > 0) {
        (cartonsInt * lengthDbl * widthDbl * heightDbl) / 1_000_000.0
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MoveToInbox, contentDescription = null, tint = SecondaryTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Receive Goods (Inward GRN)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Select Product:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        masterProducts.forEach { p ->
                            FilterChip(
                                selected = selectedProductName == p.name,
                                onClick = {
                                    selectedProductName = p.name
                                    selectedProductId = p.id
                                    unitsPerCartonText = p.unitsPerCarton.toString()
                                },
                                label = { Text(p.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Supplier / Factory") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Link to Active Shipment (Optional):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = linkedShipmentId.isEmpty(),
                            onClick = { linkedShipmentId = "" },
                            label = { Text("Unassigned Stock", fontSize = 11.sp) }
                        )
                        allShipments.forEach { s ->
                            FilterChip(
                                selected = linkedShipmentId == s.id,
                                onClick = { linkedShipmentId = s.id },
                                label = { Text(s.id, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = batchNumber,
                            onValueChange = { batchNumber = it },
                            label = { Text("Batch / Lot #") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode / QR") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cartonsReceivedText,
                            onValueChange = { cartonsReceivedText = it },
                            label = { Text("Cartons Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitsPerCartonText,
                            onValueChange = { unitsPerCartonText = it },
                            label = { Text("Units / Ctn") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Text("Carton Dimensions (cm):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = lengthCmText,
                            onValueChange = { lengthCmText = it },
                            label = { Text("L (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = widthCmText,
                            onValueChange = { widthCmText = it },
                            label = { Text("W (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightCmText,
                            onValueChange = { heightCmText = it },
                            label = { Text("H (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cartonGrossWeightText,
                            onValueChange = { cartonGrossWeightText = it },
                            label = { Text("Gross Wt / Ctn (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = locationBay,
                            onValueChange = { locationBay = it },
                            label = { Text("Location Bay") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("CALCULATED TOTALS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Units: $calculatedTotalUnits", fontSize = 11.sp)
                                Text("Gross Wt: %.1f kg".format(calculatedTotalGrossWeight), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Total CBM: %.3f".format(calculatedTotalCbm), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = autoUpdateShipment,
                            onCheckedChange = { autoUpdateShipment = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Update shipment quantities automatically but preserve an audit trail",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Observations") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val receipt = GoodsReceipt(
                        receiptNumber = defaultReceiptNum,
                        productId = selectedProductId,
                        productName = selectedProductName,
                        supplierName = supplierName,
                        shipmentId = linkedShipmentId.ifBlank { null },
                        cartonsReceived = cartonsInt,
                        unitsPerCarton = unitsInt,
                        totalUnits = calculatedTotalUnits,
                        cartonGrossWeightKg = cartonGrossDbl,
                        totalGrossWeightKg = calculatedTotalGrossWeight,
                        lengthCm = lengthDbl,
                        widthCm = widthDbl,
                        heightCm = heightDbl,
                        totalCbm = calculatedTotalCbm,
                        batchNumber = batchNumber,
                        barcode = barcode,
                        locationBay = locationBay,
                        receivedDate = sdfDate,
                        receivedBy = currentUser.fullName,
                        notes = notes
                    )
                    onConfirm(receipt, autoUpdateShipment && linkedShipmentId.isNotBlank())
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
            ) {
                Text("Confirm Receipt")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onSaveDraft != null) {
                    TextButton(onClick = {
                        val receipt = GoodsReceipt(
                            receiptNumber = defaultReceiptNum,
                            productId = selectedProductId,
                            productName = selectedProductName,
                            supplierName = supplierName,
                            shipmentId = linkedShipmentId.ifBlank { null },
                            cartonsReceived = cartonsInt,
                            unitsPerCarton = unitsInt,
                            totalUnits = calculatedTotalUnits,
                            cartonGrossWeightKg = cartonGrossDbl,
                            totalGrossWeightKg = calculatedTotalGrossWeight,
                            lengthCm = lengthDbl,
                            widthCm = widthDbl,
                            heightCm = heightDbl,
                            totalCbm = calculatedTotalCbm,
                            batchNumber = batchNumber,
                            barcode = barcode,
                            locationBay = locationBay,
                            receivedDate = sdfDate,
                            receivedBy = currentUser.fullName,
                            notes = notes
                        )
                        onSaveDraft(receipt)
                    }) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Draft", color = StatusAmber, fontSize = 12.sp)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// =========================================================================
// ASSIGN CARTONS TO PALLET DIALOG
// =========================================================================

@Composable
fun AssignCartonsToPalletDialog(
    pallet: Pallet,
    stockItems: List<WarehouseStockItem>,
    products: List<ShipmentProduct>,
    onDismiss: () -> Unit,
    onAssign: (
        palletId: Long,
        shipmentId: String,
        stockItemId: Long?,
        productName: String,
        cartons: Int,
        cartonGrossWeightKg: Double,
        cbmPerCarton: Double
    ) -> Unit
) {
    var selectedStockItem by remember { mutableStateOf(stockItems.firstOrNull()) }
    var cartonsText by remember { mutableStateOf("10") }

    val cartonsInt = cartonsText.toIntOrNull() ?: 0
    val item = selectedStockItem

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Assign Cartons to ${pallet.palletNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pallet currently has ${pallet.cartons}/${pallet.maxCartonCapacity} cartons (${pallet.weightKg} kg).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Select Product from Warehouse Stock:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                stockItems.forEach { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selectedStockItem?.id == stock.id) PrimaryBlueContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { selectedStockItem = stock }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stock.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Bay: ${stock.locationBay} • Available: ${stock.availableCartons} ctns", fontSize = 10.sp)
                        }
                        RadioButton(
                            selected = selectedStockItem?.id == stock.id,
                            onClick = { selectedStockItem = stock }
                        )
                    }
                }

                OutlinedTextField(
                    value = cartonsText,
                    onValueChange = { cartonsText = it },
                    label = { Text("Number of Cartons to Assign") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                if (item != null) {
                    val newTotal = pallet.cartons + cartonsInt
                    val newWeight = pallet.weightKg + (cartonsInt * item.cartonGrossWeightKg)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("PREVIEW AFTER ASSIGNMENT:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text("New Pallet Cartons: $newTotal / ${pallet.maxCartonCapacity} max", fontSize = 11.sp)
                            Text("Estimated Pallet Weight: %.1f kg".format(newWeight), fontSize = 11.sp)
                            Text("Stock Available Remaining: ${maxOf(0, item.availableCartons - cartonsInt)} ctns", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (item != null && cartonsInt > 0) {
                        onAssign(
                            pallet.id,
                            pallet.shipmentId,
                            item.id,
                            item.productName,
                            cartonsInt,
                            item.cartonGrossWeightKg,
                            item.cbmPerCarton
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Assign to Pallet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =========================================================================
// RECORD CARTON DIMENSIONS & WEIGHTS DIALOG
// =========================================================================

@Composable
fun RecordDimensionsDialog(
    item: WarehouseStockItem,
    onDismiss: () -> Unit,
    onSave: (stockId: Long, length: Double, width: Double, height: Double, grossWeight: Double, qty: Int, bay: String) -> Unit
) {
    var lengthText by remember { mutableStateOf(item.lengthCm.toString()) }
    var widthText by remember { mutableStateOf(item.widthCm.toString()) }
    var heightText by remember { mutableStateOf(item.heightCm.toString()) }
    var weightText by remember { mutableStateOf(item.cartonGrossWeightKg.toString()) }
    var quantityText by remember { mutableStateOf(item.availableCartons.toString()) }
    var bayText by remember { mutableStateOf(item.locationBay) }

    val l = lengthText.toDoubleOrNull() ?: 0.0
    val w = widthText.toDoubleOrNull() ?: 0.0
    val h = heightText.toDoubleOrNull() ?: 0.0
    val calculatedCbm = if (l > 0 && w > 0 && h > 0) (l * w * h) / 1_000_000.0 else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Straighten, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Carton Dimensions & Weight", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.imePadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Barcode: ${item.barcode} • Lot: ${item.batchLotNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(4.dp))
                Text("Dimensions (cm):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = { lengthText = it },
                        label = { Text("Length") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { widthText = it },
                        label = { Text("Width") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Carton Gross Wt (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Available Ctns") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(value = bayText, onValueChange = { bayText = it }, label = { Text("Storage Location Bay") }, modifier = Modifier.fillMaxWidth())

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Calculated Volume per carton: %.4f CBM".format(calculatedCbm), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Total Stock Volume: %.3f CBM".format(calculatedCbm * (quantityText.toIntOrNull() ?: 0)), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        item.id,
                        lengthText.toDoubleOrNull() ?: item.lengthCm,
                        widthText.toDoubleOrNull() ?: item.widthCm,
                        heightText.toDoubleOrNull() ?: item.heightCm,
                        weightText.toDoubleOrNull() ?: item.cartonGrossWeightKg,
                        quantityText.toIntOrNull() ?: item.availableCartons,
                        bayText
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save Measurements")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =========================================================================
// RECORD DISPATCH DIALOG
// =========================================================================

@Composable
fun RecordDispatchDialog(
    currentShipment: Shipment?,
    allShipments: List<Shipment>,
    pallets: List<Pallet>,
    currentUser: User,
    onDismiss: () -> Unit,
    onConfirm: (dispatch: WarehouseDispatch, createDeliveryNote: Boolean) -> Unit
) {
    val sdfDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val defaultDispatchNum = remember { "DSP-2026-${(100..999).random()}" }
    val defaultDnNum = remember { "DN-2026-00${(20..99).random()}" }

    var selectedShipmentId by remember { mutableStateOf(currentShipment?.id ?: allShipments.firstOrNull()?.id ?: "") }
    val ship = allShipments.find { it.id == selectedShipmentId } ?: currentShipment

    var customer by remember { mutableStateOf(ship?.customer ?: "OUTLAW DIP COMPANY INC.") }
    var containerNumber by remember { mutableStateOf(ship?.containerNumber ?: "EMCU6409544/20") }
    var sealNumber by remember { mutableStateOf("SL-940284") }
    var vehicleNumber by remember { mutableStateOf("WP-LH-8921") }
    var driverName by remember { mutableStateOf("Gamini Wijesinghe") }
    var driverNic by remember { mutableStateOf("+94 71 889 1234") }
    var cartonsText by remember { mutableStateOf("505") }
    var palletsText by remember { mutableStateOf("9") }
    var grossWeightText by remember { mutableStateOf("4029.6") }
    var cbmText by remember { mutableStateOf("14.214") }
    var autoCreateDn by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Warehouse Dispatch", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Select Shipment:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allShipments.forEach { s ->
                            FilterChip(
                                selected = selectedShipmentId == s.id,
                                onClick = {
                                    selectedShipmentId = s.id
                                    customer = s.customer
                                    containerNumber = s.containerNumber
                                },
                                label = { Text(s.id, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(value = customer, onValueChange = { customer = it }, label = { Text("Customer / Consignee") }, modifier = Modifier.fillMaxWidth())
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = containerNumber, onValueChange = { containerNumber = it }, label = { Text("Container Number") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sealNumber, onValueChange = { sealNumber = it }, label = { Text("Seal Number") }, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    OutlinedTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = { Text("Truck / Vehicle Number") }, modifier = Modifier.fillMaxWidth())
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = driverName, onValueChange = { driverName = it }, label = { Text("Driver Name") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = driverNic, onValueChange = { driverNic = it }, label = { Text("Driver Contact / NIC") }, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = cartonsText,
                            onValueChange = { cartonsText = it },
                            label = { Text("Dispatched Cartons") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = palletsText,
                            onValueChange = { palletsText = it },
                            label = { Text("Dispatched Pallets") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = grossWeightText,
                            onValueChange = { grossWeightText = it },
                            label = { Text("Total Gross Wt (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cbmText,
                            onValueChange = { cbmText = it },
                            label = { Text("Total CBM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = autoCreateDn, onCheckedChange = { autoCreateDn = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Automatically generate Delivery Note & Gate Pass", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dispatch = WarehouseDispatch(
                        dispatchNumber = defaultDispatchNum,
                        shipmentId = selectedShipmentId,
                        customer = customer,
                        deliveryNoteNumber = defaultDnNum,
                        containerNumber = containerNumber,
                        sealNumber = sealNumber,
                        vehicleNumber = vehicleNumber,
                        driverName = driverName,
                        driverNic = driverNic,
                        totalCartonsDispatched = cartonsText.toIntOrNull() ?: 505,
                        totalPalletsDispatched = palletsText.toIntOrNull() ?: 9,
                        totalGrossWeightKg = grossWeightText.toDoubleOrNull() ?: 4029.6,
                        totalCbm = cbmText.toDoubleOrNull() ?: 14.214,
                        dispatchDate = sdfDate,
                        dispatchedBy = currentUser.fullName
                    )
                    onConfirm(dispatch, autoCreateDn)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Confirm Dispatch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =========================================================================
// CREATE DELIVERY NOTE DIALOG
// =========================================================================

@Composable
fun CreateDeliveryNoteDialog(
    currentShipment: Shipment?,
    pallets: List<Pallet>,
    currentUser: User,
    onDismiss: () -> Unit,
    onConfirm: (DeliveryNote) -> Unit
) {
    val sdfDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val defaultNoteNum = remember { "DN-2026-00${(20..99).random()}" }

    var noteNumber by remember { mutableStateOf(defaultNoteNum) }
    var shipmentId by remember { mutableStateOf(currentShipment?.id ?: "ECGMIX_092826_016") }
    var consigneeName by remember { mutableStateOf(currentShipment?.customer ?: "OUTLAW DIP COMPANY INC.") }
    var consigneeAddress by remember { mutableStateOf(currentShipment?.placeOfDelivery ?: "820 Grand Blvd, Deer Park, NY 11729, USA") }
    var destinationPort by remember { mutableStateOf(currentShipment?.portOfDischarge ?: "New York, USA") }
    var containerNumber by remember { mutableStateOf(currentShipment?.containerNumber ?: "EMCU6409544/20") }
    var sealNumber by remember { mutableStateOf("SL-940284") }
    var vehicleNumber by remember { mutableStateOf("WP-LH-8921") }
    var driverName by remember { mutableStateOf("Gamini Wijesinghe") }
    var driverPhone by remember { mutableStateOf("+94 71 889 1234") }
    var summary by remember { mutableStateOf("505 Cartons on 9 Pallets / Ice Hockey Pucks, Tin Lids, Plastic Cups & Fish Feed") }
    var cartonsText by remember { mutableStateOf("505") }
    var palletsText by remember { mutableStateOf("9") }
    var weightText by remember { mutableStateOf("4029.6") }
    var cbmText by remember { mutableStateOf("14.214") }
    var notes by remember { mutableStateOf("Stuffed into container with high security seal SL-940284.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = SecondaryTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Delivery Note", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = noteNumber, onValueChange = { noteNumber = it }, label = { Text("DN Number") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = shipmentId, onValueChange = { shipmentId = it }, label = { Text("Shipment ID") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = consigneeName, onValueChange = { consigneeName = it }, label = { Text("Consignee") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = consigneeAddress, onValueChange = { consigneeAddress = it }, label = { Text("Delivery Address") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = containerNumber, onValueChange = { containerNumber = it }, label = { Text("Container #") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sealNumber, onValueChange = { sealNumber = it }, label = { Text("Seal #") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = { Text("Truck / Vehicle #") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = driverName, onValueChange = { driverName = it }, label = { Text("Driver Name") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Items Summary Description") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = cartonsText,
                            onValueChange = { cartonsText = it },
                            label = { Text("Total Cartons") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = palletsText,
                            onValueChange = { palletsText = it },
                            label = { Text("Total Pallets") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Gross Wt (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cbmText,
                            onValueChange = { cbmText = it },
                            label = { Text("Total CBM") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes & Instructions") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dn = DeliveryNote(
                        noteNumber = noteNumber,
                        shipmentId = shipmentId,
                        date = sdfDate,
                        consigneeName = consigneeName,
                        consigneeAddress = consigneeAddress,
                        destinationPort = destinationPort,
                        containerNumber = containerNumber,
                        sealNumber = sealNumber,
                        vehicleNumber = vehicleNumber,
                        driverName = driverName,
                        driverPhone = driverPhone,
                        itemsSummary = summary,
                        totalCartons = cartonsText.toIntOrNull() ?: 505,
                        totalPallets = palletsText.toIntOrNull() ?: 9,
                        totalGrossWeightKg = weightText.toDoubleOrNull() ?: 4029.6,
                        totalCbm = cbmText.toDoubleOrNull() ?: 14.214,
                        preparedBy = currentUser.fullName,
                        receivedBySignature = "$driverName (Driver)",
                        notes = notes
                    )
                    onConfirm(dn)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
            ) {
                Text("Generate Delivery Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =========================================================================
// DELIVERY NOTE FULL A4 PREVIEW DIALOG WITH NATIVE PRINT & SHARE
// =========================================================================

@Composable
fun DeliveryNotePreviewDialog(
    deliveryNote: DeliveryNote,
    shipment: Shipment?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Delivery Note Preview: ${deliveryNote.noteNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Shipment: ${deliveryNote.shipmentId} • Container: ${deliveryNote.containerNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                val html = DocumentGenerator.generateDeliveryNoteHtml(deliveryNote, shipment)
                                DocumentGenerator.printDocument(context, html, "DeliveryNote_${deliveryNote.noteNumber}")
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PrimaryBlueContainer)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Print / PDF", tint = PrimaryBlue)
                        }

                        IconButton(
                            onClick = {
                                val html = DocumentGenerator.generateDeliveryNoteHtml(deliveryNote, shipment)
                                DocumentGenerator.shareDocument(context, "Delivery Note ${deliveryNote.noteNumber}", html)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SecondaryTealContainer)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = SecondaryTeal)
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                HorizontalDivider(color = LightBorder)

                // Rendered A4 Document Card
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("EXPLIC CEYLON (PVT) LTD", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Navy900)
                                        Text("Logistics Operations • Warehouse Division", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("43/1, Hospital Road, Dehiwala, Sri Lanka", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("DELIVERY NOTE", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryBlue)
                                        Text(deliveryNote.noteNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                        Text("Date: ${deliveryNote.date}", fontSize = 11.sp, color = Color(0xFF475569))
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFF0284C7), thickness = 2.dp)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Grid info
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("CONSIGNEE & DESTINATION:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Text(deliveryNote.consigneeName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                        Text(deliveryNote.consigneeAddress, fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("Destination Port: ${deliveryNote.destinationPort}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("TRANSPORT & SEAL DETAILS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Text("Container: ${deliveryNote.containerNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                                        Text("Seal: ${deliveryNote.sealNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                        Text("Truck: ${deliveryNote.vehicleNumber}", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("Driver: ${deliveryNote.driverName} (${deliveryNote.driverPhone})", fontSize = 11.sp, color = Color(0xFF475569))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Cargo table
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("CARGO DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Text(deliveryNote.itemsSummary, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Total Pallets", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Text("${deliveryNote.totalPallets}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                            Column {
                                                Text("Total Cartons", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Text("${deliveryNote.totalCartons}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                            Column {
                                                Text("Gross Weight", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Text("%.1f kg".format(deliveryNote.totalGrossWeightKg), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                            Column {
                                                Text("Total CBM", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Text("%.3f".format(deliveryNote.totalCbm), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Notes: ${deliveryNote.notes}", fontSize = 10.sp, color = Color(0xFF475569))

                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("PREPARED BY:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(deliveryNote.preparedBy, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                                        Text("Warehouse Supervisor", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                        Text("RECEIVED BY (DRIVER):", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(deliveryNote.receivedBySignature.ifBlank { deliveryNote.driverName }, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                                        Text("Acknowledged in Good Order", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val html = DocumentGenerator.generateDeliveryNoteHtml(deliveryNote, shipment)
                            DocumentGenerator.printDocument(context, html, "DeliveryNote_${deliveryNote.noteNumber}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print / Export PDF")
                    }
                }
            }
        }
    }
}

@Composable
fun PalletEditDialog(
    pallet: Pallet,
    onDismiss: () -> Unit,
    onSave: (Pallet) -> Unit
) {
    var palletNumber by remember { mutableStateOf(pallet.palletNumber) }
    var palletType by remember { mutableStateOf(pallet.palletType) }
    var cartonsText by remember { mutableStateOf(pallet.cartons.toString()) }
    var weightText by remember { mutableStateOf(pallet.weightKg.toString()) }
    var cbmText by remember { mutableStateOf(pallet.cbm.toString()) }
    var summary by remember { mutableStateOf(pallet.productsSummary) }
    var maxCapacityText by remember { mutableStateOf(pallet.maxCartonCapacity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pallet.id == 0L) "New Pallet Record" else "Edit ${pallet.palletNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = palletNumber, onValueChange = { palletNumber = it }, label = { Text("Pallet Number (e.g. Pallet 1)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = palletType, onValueChange = { palletType = it }, label = { Text("Pallet Specification") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Allocated Products Description") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cartonsText, onValueChange = { cartonsText = it }, label = { Text("Cartons") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = maxCapacityText, onValueChange = { maxCapacityText = it }, label = { Text("Max Cartons") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Gross Weight (kg)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = cbmText, onValueChange = { cbmText = it }, label = { Text("CBM") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = pallet.copy(
                        palletNumber = palletNumber,
                        palletType = palletType,
                        cartons = cartonsText.toIntOrNull() ?: 0,
                        maxCartonCapacity = maxCapacityText.toIntOrNull() ?: 60,
                        weightKg = weightText.toDoubleOrNull() ?: 25.0,
                        cbm = cbmText.toDoubleOrNull() ?: 1.55,
                        productsSummary = summary
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
            ) {
                Text("Save Pallet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
