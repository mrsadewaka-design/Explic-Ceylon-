package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.model.*
import com.example.engine.DocumentGenerator
import com.example.ui.ExportViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentListScreen(
    viewModel: ExportViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val shipments by viewModel.allShipments.collectAsState()
    val activeId by viewModel.selectedShipmentId.collectAsState()
    val offlineDrafts by viewModel.allOfflineDrafts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var searchScope by remember { mutableStateOf(ShipmentSearchScope.ALL) }
    var selectedFilterMode by remember { mutableStateOf<ShippingMode?>(null) }
    var selectedFilterStatus by remember { mutableStateOf<ShipmentStatus?>(null) }
    var showDraftsSheet by remember { mutableStateOf(false) }

    val filteredList = shipments.filter { s ->
        val query = searchQuery.trim().lowercase()
        val matchesQuery = if (query.isEmpty()) {
            true
        } else {
            when (searchScope) {
                ShipmentSearchScope.TRACKING_ID -> {
                    s.id.lowercase().contains(query) ||
                    s.reference.lowercase().contains(query) ||
                    s.masterBlNumber.lowercase().contains(query) ||
                    s.amsBlNumber.lowercase().contains(query) ||
                    s.containerNumber.lowercase().contains(query)
                }
                ShipmentSearchScope.CLIENT_NAME -> {
                    s.customer.lowercase().contains(query)
                }
                ShipmentSearchScope.ALL -> {
                    s.id.lowercase().contains(query) ||
                    s.reference.lowercase().contains(query) ||
                    s.customer.lowercase().contains(query) ||
                    s.poNumber.lowercase().contains(query) ||
                    s.invoiceNumber.lowercase().contains(query) ||
                    s.containerNumber.lowercase().contains(query) ||
                    s.vessel.lowercase().contains(query) ||
                    s.voyage.lowercase().contains(query) ||
                    s.masterBlNumber.lowercase().contains(query) ||
                    s.amsBlNumber.lowercase().contains(query)
                }
            }
        }

        val matchesMode = selectedFilterMode == null || s.shippingMode == selectedFilterMode
        val matchesStatus = selectedFilterStatus == null || s.status == selectedFilterStatus
        matchesQuery && matchesMode && matchesStatus
    }

    if (showDraftsSheet) {
        OfflineDraftsSheet(
            drafts = offlineDrafts,
            onSelectDraft = { draft ->
                showDraftsSheet = false
                onNavigateToCreate()
            },
            onDeleteDraft = { viewModel.deleteOfflineDraft(it) },
            onClearAll = { viewModel.clearAllOfflineDrafts() },
            onDismiss = { showDraftsSheet = false }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("NEW SHIPMENT", fontWeight = FontWeight.Bold) },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_shipment")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Bar at top of shipment list (Filters by Tracking ID or Client Name)
            ShipmentSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                searchScope = searchScope,
                onSearchScopeChange = { searchScope = it },
                totalShipmentsCount = shipments.size,
                filteredShipmentsCount = filteredList.size
            )

            // Offline Draft notification banner if drafts exist and not searching
            if (offlineDrafts.isNotEmpty() && searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                OfflineDraftBanner(
                    draft = offlineDrafts.first(),
                    onResume = onNavigateToCreate,
                    onDiscard = { viewModel.deleteOfflineDraft(offlineDrafts.first().draftId) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (Scrollable Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drafts Chip
                FilterChip(
                    selected = showDraftsSheet,
                    onClick = { showDraftsSheet = true },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (offlineDrafts.isNotEmpty()) StatusAmber else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Drafts (${offlineDrafts.size})") }
                )

                FilterChip(
                    selected = selectedFilterMode == null && selectedFilterStatus == null,
                    onClick = {
                        selectedFilterMode = null
                        selectedFilterStatus = null
                    },
                    label = { Text("All (${shipments.size})") }
                )
                FilterChip(
                    selected = selectedFilterMode == ShippingMode.SEA,
                    onClick = {
                        selectedFilterMode = if (selectedFilterMode == ShippingMode.SEA) null else ShippingMode.SEA
                    },
                    label = { Text("Sea Freight") }
                )
                FilterChip(
                    selected = selectedFilterMode == ShippingMode.AIR,
                    onClick = {
                        selectedFilterMode = if (selectedFilterMode == ShippingMode.AIR) null else ShippingMode.AIR
                    },
                    label = { Text("Air Cargo") }
                )
                FilterChip(
                    selected = selectedFilterStatus == ShipmentStatus.DOCUMENTATION,
                    onClick = {
                        selectedFilterStatus = if (selectedFilterStatus == ShipmentStatus.DOCUMENTATION) null else ShipmentStatus.DOCUMENTATION
                    },
                    label = { Text("Documentation") }
                )
                FilterChip(
                    selected = selectedFilterStatus == ShipmentStatus.SHIPPED,
                    onClick = {
                        selectedFilterStatus = if (selectedFilterStatus == ShipmentStatus.SHIPPED) null else ShipmentStatus.SHIPPED
                    },
                    label = { Text("In Transit") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                if (searchQuery.isNotEmpty()) {
                    ShipmentSearchEmptyState(
                        query = searchQuery,
                        scope = searchScope,
                        onResetSearch = {
                            searchQuery = ""
                            searchScope = ShipmentSearchScope.ALL
                            selectedFilterMode = null
                            selectedFilterStatus = null
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = LightTextMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No shipments found", fontWeight = FontWeight.SemiBold, color = LightTextSecondary)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { shipment ->
                        ShipmentSummaryCard(
                            shipment = shipment,
                            isSelected = shipment.id == activeId,
                            onClick = {
                                viewModel.selectShipment(shipment.id)
                                onNavigateToDetail(shipment.id)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ShipmentDetailScreen(
    shipmentId: String,
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToValidation: () -> Unit,
    onNavigateToWarehouse: () -> Unit,
    onNavigateToAi: (() -> Unit)? = null
) {
    val shipment by viewModel.currentShipment.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val pallets by viewModel.currentPallets.collectAsState()
    val validationSummary by viewModel.validationSummary.collectAsState()
    val masterProducts by viewModel.allProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val invoice by viewModel.currentInvoice.collectAsState()
    val packingList by viewModel.currentPackingList.collectAsState()
    val isf by viewModel.currentIsf.collectAsState()
    val bl by viewModel.currentBl.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ShipmentProduct?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val totalCartons = products.sumOf { it.cartons }
    val totalUnits = products.sumOf { it.totalUnits }
    val totalNetWeight = products.sumOf { it.totalNetWeightKg }
    val totalGrossWeight = products.sumOf { it.totalGrossWeightKg }
    val totalCbm = products.sumOf { it.totalCbm }

    if (shipment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val ship = shipment!!

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Totals Bottom Bar - Responsive Horizontal Scroll for Phone Screens
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = Navy900
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TotalSummaryCol(label = "TOTAL CARTONS", value = "$totalCartons")
                        TotalSummaryCol(label = "TOTAL UNITS", value = "$totalUnits")
                        TotalSummaryCol(label = "NET WEIGHT", value = "%.1f kg".format(totalNetWeight))
                        TotalSummaryCol(label = "GROSS WEIGHT", value = "%.1f kg".format(totalGrossWeight))
                        TotalSummaryCol(label = "TOTAL CBM", value = "%.3f".format(totalCbm))
                        TotalSummaryCol(label = "PALLETS", value = "${pallets.size}")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ship.id,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Ref: ${ship.reference} • ${ship.shippingMode.label}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = ship.status)
                }
            }

            // Interactive Single Source of Truth Export Workflow Stepper
            // Order → Shipment → Products → Packing → Invoice → ISF → B/L → Validation → Approval → PDF/Excel → Shipment tracking
            item {
                SingleSourceOfTruthWorkflowCard(
                    shipment = ship,
                    products = products,
                    pallets = pallets,
                    invoice = invoice,
                    packingList = packingList,
                    isf = isf,
                    bl = bl,
                    validationSummary = validationSummary,
                    onNavigateToStage = { stageName ->
                        when (stageName) {
                            "Order", "Shipment", "Products" -> { /* stays on detail */ }
                            "Packing" -> onNavigateToWarehouse()
                            "Invoice", "ISF", "B/L", "PDF/Excel" -> onNavigateToDocuments()
                            "Validation", "Approval" -> onNavigateToValidation()
                            "Tracking" -> onNavigateToWarehouse()
                        }
                    }
                )
            }

            // Visual Progress Tracker Component - Milestones: Packing → Invoiced → Validated → Dispatched
            item {
                ShipmentProgressTrackerComponent(
                    shipment = ship,
                    products = products,
                    pallets = pallets,
                    invoice = invoice,
                    validationSummary = validationSummary,
                    canEdit = currentUser.role != UserRole.VIEWER,
                    onStatusUpdate = { newStatus ->
                        viewModel.updateShipment(ship.copy(status = newStatus))
                    }
                )
            }

            // Extended 11-Stage Pipeline Progress Tracker
            item {
                ShipmentProgressTracker(
                    currentStatus = ship.status,
                    canEdit = currentUser.role != UserRole.VIEWER,
                    onStatusClick = { newStatus ->
                        viewModel.updateShipment(ship.copy(status = newStatus))
                    }
                )
            }

            // Quick Nav Links to Docs & Validation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToDocuments,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Doc Center", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onNavigateToValidation,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (validationSummary?.totalErrors ?: 0 > 0) StatusRed else if (validationSummary?.totalWarnings ?: 0 > 0) StatusAmber else StatusGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validation", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToWarehouse,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp), tint = SecondaryTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pallets", fontSize = 12.sp, color = SecondaryTeal)
                    }
                }
            }

            // Ask Export AI Card
            item {
                Card(
                    onClick = { onNavigateToAi?.invoke() },
                    colors = CardDefaults.cardColors(containerColor = StatusBlueContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_ask_export_ai_shipment")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ask Export AI about this shipment",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "Check document consistency, ISF 10+2, or generate B/L instructions",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryBlue)
                    }
                }
            }

            // =======================================================
            // GENERATE DOCUMENTS SECTION
            // =======================================================
            item {
                GenerateDocumentsCard(
                    shipment = ship,
                    invoice = invoice,
                    packingList = packingList,
                    isf = isf,
                    bl = bl,
                    products = products,
                    pallets = pallets,
                    onNavigateToValidation = onNavigateToValidation
                )
            }

            // Shipment Specifications Card with requested fields
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
                                text = "SHIPMENT SPECIFICATIONS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            StatusBadge(status = ship.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        DetailParamRow("Shipment No.", ship.id)
                        DetailParamRow("Customer", ship.customer)
                        DetailParamRow("PO Number", ship.poNumber)
                        DetailParamRow("Invoice Number", ship.invoiceNumber)
                        DetailParamRow("Invoice Date", ship.invoiceDate)
                        DetailParamRow("Currency", ship.currency)
                        DetailParamRow("Destination", ship.portOfDischarge)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Shipping Mode: [ SEA ] [ AIR ]
                        ShippingModeSelector(
                            selectedMode = ship.shippingMode,
                            onModeSelected = { newMode ->
                                if (currentUser.role != UserRole.VIEWER) {
                                    viewModel.updateShipment(ship.copy(shippingMode = newMode))
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DetailParamRow("Port of Loading", ship.portOfLoading)
                        DetailParamRow("Port of Discharge", ship.portOfDischarge)
                        DetailParamRow("Place of Delivery", ship.placeOfDelivery)

                        HorizontalDivider(color = LightBorder, modifier = Modifier.padding(vertical = 10.dp))

                        DetailParamRow("Vessel & Voyage", "${ship.vessel} / ${ship.voyage}")
                        DetailParamRow("ETD (Departure)", ship.etd)
                        DetailParamRow("Container Number", ship.containerNumber)
                        DetailParamRow("Master B/L No.", ship.masterBlNumber)
                        DetailParamRow("AMS B/L No.", ship.amsBlNumber)

                        if (ship.isOverrideApproved) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreenContainer)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "✓ Warning Override Authorized by: ${ship.overrideApprovedBy}\nNote: ${ship.overrideNotes ?: ""}",
                                    fontSize = 11.sp,
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // =======================================================
            // THE SYSTEM AUTOMATICALLY CALCULATES:
            // Total Units, Net Weight, Gross Weight, CBM, Total Cartons
            // =======================================================
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
                            Column {
                                Text(
                                    text = "AUTOMATIC SYSTEM CALCULATIONS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Real-time totals synced across all documents",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSystemInDarkTheme()) Color(0xFF064E3B).copy(alpha = 0.4f) else StatusGreenContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("AUTO-SYNCED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSystemInDarkTheme()) Color(0xFF86EFAC) else StatusGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Top Row: Total Units, Net Weight, Gross Weight
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CalculationMetricBox("Total Units", "$totalUnits units", "Cartons × Units/Ctn")
                            CalculationMetricBox("Net Weight", "%.1f kg".format(totalNetWeight), "Units × Unit Wt")
                            CalculationMetricBox("Gross Weight", "%.1f kg".format(totalGrossWeight), "Cartons × Gross Wt")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bottom Row: CBM, Total Cartons, Pallets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CalculationMetricBox("CBM", "%.3f m³".format(totalCbm), "Cartons × CBM/Ctn")
                            CalculationMetricBox("Total Cartons", "$totalCartons ctns", "Sum of all lines")
                            CalculationMetricBox("Total Pallets", "${pallets.size} pallets", "Warehouse load")
                        }
                    }
                }
            }

            // Products Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SHIPMENT PRODUCTS (${products.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-calculated Net Wt, Gross Wt & CBM",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentUser.role != UserRole.VIEWER) {
                        Button(
                            onClick = { showAddProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_add_product")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Product Items List
            items(
                items = products,
                key = { it.id }
            ) { product ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (currentUser.role != UserRole.VIEWER && (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd)) {
                            viewModel.deleteProduct(product)
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Removed '${product.productName}'",
                                    actionLabel = "UNDO",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.saveProduct(product)
                                }
                            }
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = currentUser.role != UserRole.VIEWER,
                    enableDismissFromEndToStart = currentUser.role != UserRole.VIEWER,
                    backgroundContent = {
                        val isSwiping = dismissState.targetValue != SwipeToDismissBoxValue.Settled
                        val color by animateColorAsState(
                            if (isSwiping) Color(0xFFDC2626) else Color(0xFFEF4444).copy(alpha = 0.85f),
                            label = "dismiss_color"
                        )
                        val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                            Alignment.CenterStart else Alignment.CenterEnd

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                Text("Delete Item", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    },
                    modifier = Modifier.animateItem()
                ) {
                    ShipmentProductCard(
                        product = product,
                        canEdit = currentUser.role != UserRole.VIEWER,
                        onEditClick = { productToEdit = product },
                        onDeleteClick = {
                            viewModel.deleteProduct(product)
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Removed '${product.productName}'",
                                    actionLabel = "UNDO",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.saveProduct(product)
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog || productToEdit != null) {
        val editingProduct = productToEdit
        ProductEditDialog(
            product = editingProduct,
            shipmentId = ship.id,
            masterProducts = masterProducts,
            onDismiss = {
                showAddProductDialog = false
                productToEdit = null
            },
            onSave = { savedProd ->
                viewModel.saveProduct(savedProd)
                showAddProductDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
fun TotalSummaryCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.85f), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailParamRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ShipmentProgressTracker(
    currentStatus: ShipmentStatus,
    canEdit: Boolean = true,
    onStatusClick: (ShipmentStatus) -> Unit
) {
    val statuses = ShipmentStatus.ALL_STAGES
    val currentIndex = currentStatus.stepIndex.coerceIn(0, statuses.size - 1)
    var isFlowExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shipment_visual_progress_tracker"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: VISUAL PROGRESS TRACKER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
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
                            text = "11-Stage Progression Pipeline (NEW ↓ DELIVERED)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(
                    onClick = { isFlowExpanded = !isFlowExpanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_toggle_tracker_view")
                ) {
                    Icon(
                        imageVector = if (isFlowExpanded) Icons.Default.ViewStream else Icons.Default.VerticalAlignBottom,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFlowExpanded) "Compact Bar" else "Vertical Flow (↓)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Stage & Completion Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Current Stage: ${currentIndex + 1} of 11",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentStatus.label,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryBlue
                    )
                }

                val percent = (((currentIndex + 1) * 100) / 11)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (percent == 100) StatusGreenContainer else StatusBlueContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$percent% COMPLETED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (percent == 100) StatusGreen else PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1f) / 11f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (currentIndex == 10) StatusGreen else PrimaryBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Stage Advance Quick Actions
            if (canEdit) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentIndex > 0) {
                        OutlinedButton(
                            onClick = { onStatusClick(statuses[currentIndex - 1]) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_prev_stage"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("‹ ${statuses[currentIndex - 1].label}", fontSize = 10.sp, maxLines = 1)
                        }
                    }

                    if (currentIndex < statuses.size - 1) {
                        Button(
                            onClick = { onStatusClick(statuses[currentIndex + 1]) },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_next_stage"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Next: ${statuses[currentIndex + 1].label}", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isFlowExpanded) {
                // Vertical Flow View with ↓ connecting arrows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp)
                ) {
                    statuses.forEachIndexed { index, status ->
                        val isPassed = index < currentIndex
                        val isCurrent = index == currentIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isCurrent -> PrimaryBlue.copy(alpha = 0.12f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = canEdit) { onStatusClick(status) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("progress_stage_${status.name}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step Node Circle
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
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
                                        contentDescription = "Passed",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Current",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = status.label,
                                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                        fontSize = if (isCurrent) 13.sp else 12.sp,
                                        color = when {
                                            isCurrent -> PrimaryBlue
                                            isPassed -> StatusGreen
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PrimaryBlue)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                                if (status.description.isNotBlank()) {
                                    Text(
                                        text = status.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (canEdit && !isCurrent) {
                                TextButton(
                                    onClick = { onStatusClick(status) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPassed) "Revert" else "Set",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPassed) MaterialTheme.colorScheme.onSurfaceVariant else PrimaryBlue
                                    )
                                }
                            }
                        }

                        // Connecting downward indicator (↓)
                        if (index < statuses.size - 1) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 22.dp, top = 1.dp, bottom = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(18.dp)
                                        .background(if (isPassed) StatusGreen else MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Next step down",
                                    tint = if (isPassed) StatusGreen else if (isCurrent) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Horizontal Timeline bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    statuses.forEachIndexed { index, status ->
                        val isPassed = index < currentIndex
                        val isCurrent = index == currentIndex

                        val bgColor = when {
                            isCurrent -> PrimaryBlue
                            isPassed -> StatusGreen
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = when {
                            isCurrent || isPassed -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable(enabled = canEdit) { onStatusClick(status) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isPassed) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = status.label,
                                    color = textColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        if (index < statuses.size - 1) {
                            Text(text = "↓", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShipmentTimeline(
    currentStatus: ShipmentStatus,
    onStatusClick: (ShipmentStatus) -> Unit
) {
    ShipmentProgressTracker(
        currentStatus = currentStatus,
        canEdit = true,
        onStatusClick = onStatusClick
    )
}

@Composable
fun ShipmentProductCard(
    product: ShipmentProduct,
    canEdit: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
            .testTag("shipment_product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "HS Code: ${product.hsCode} • Origin: ${product.countryOfOrigin}",
                        fontSize = 11.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (product.isManualOverride) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusAmberContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("MANUAL OVERRIDE", color = StatusAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 10 Product Specification Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProductMetricItem("Quantity", "${product.totalUnits} units")
                    ProductMetricItem("Cartons", "${product.cartons} ctns")
                    ProductMetricItem("Units / Carton", "${product.unitsPerCarton}")
                }
                HorizontalDivider(color = LightBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProductMetricItem("Unit Weight", "%.3f kg".format(product.individualUnitWeightKg))
                    ProductMetricItem("Net Weight", "%.1f kg".format(product.totalNetWeightKg))
                    ProductMetricItem("Gross Weight", "%.1f kg".format(product.totalGrossWeightKg))
                    ProductMetricItem("CBM", "%.3f m³".format(product.totalCbm))
                }
            }

            if (canEdit) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }
                    TextButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp, color = StatusRed)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CalculationMetricBox(label: String, value: String, formula: String) {
    Column {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        Text(text = formula, fontSize = 9.sp, color = PrimaryBlue)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditDialog(
    product: ShipmentProduct?,
    shipmentId: String,
    masterProducts: List<Product>,
    onDismiss: () -> Unit,
    onSave: (ShipmentProduct) -> Unit
) {
    var selectedMaster by remember {
        mutableStateOf<Product?>(
            masterProducts.firstOrNull { it.name.equals(product?.productName, ignoreCase = true) } ?: masterProducts.firstOrNull()
        )
    }

    var productName by remember { mutableStateOf(product?.productName ?: selectedMaster?.name ?: "") }
    var hsCode by remember { mutableStateOf(product?.hsCode ?: selectedMaster?.hsCode ?: "") }
    var countryOfOrigin by remember { mutableStateOf(product?.countryOfOrigin ?: selectedMaster?.countryOfOrigin ?: "Sri Lanka") }
    var description by remember { mutableStateOf(product?.description ?: selectedMaster?.description ?: "") }
    var cartonsText by remember { mutableStateOf(product?.cartons?.toString() ?: "100") }
    var unitsPerCartonText by remember { mutableStateOf(product?.unitsPerCarton?.toString() ?: (selectedMaster?.unitsPerCarton?.toString() ?: "50")) }
    var quantityText by remember { mutableStateOf(product?.totalUnits?.toString() ?: "") }
    var unitWeightText by remember { mutableStateOf(product?.individualUnitWeightKg?.toString() ?: (selectedMaster?.defaultUnitWeightKg?.toString() ?: "0.170")) }
    var cartonGrossWeightText by remember { mutableStateOf(product?.cartonGrossWeightKg?.toString() ?: "9.0") }
    var cbmPerCartonText by remember { mutableStateOf(product?.cbmPerCarton?.toString() ?: (selectedMaster?.defaultCbmPerCarton?.toString() ?: "0.0281")) }
    var unitPriceText by remember { mutableStateOf(product?.unitPriceUsd?.toString() ?: "1.50") }
    var isManualOverride by remember { mutableStateOf(product?.isManualOverride ?: false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Dynamic calculations
    val cartons = cartonsText.toIntOrNull() ?: 0
    val unitsPerCarton = unitsPerCartonText.toIntOrNull() ?: 0
    val calculatedQuantity = if (quantityText.isNotBlank()) (quantityText.toIntOrNull() ?: (cartons * unitsPerCarton)) else (cartons * unitsPerCarton)
    val unitWeight = unitWeightText.toDoubleOrNull() ?: 0.0
    val netWeight = calculatedQuantity * unitWeight
    val cartonGrossWeight = cartonGrossWeightText.toDoubleOrNull() ?: 0.0
    val grossWeight = cartons * cartonGrossWeight
    val cbmPerCarton = cbmPerCartonText.toDoubleOrNull() ?: 0.0
    val totalCbm = cartons * cbmPerCarton
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0
    val totalAmount = calculatedQuantity * unitPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "+ Add Product to Shipment" else "Edit Product",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick pick from Product Master
                item {
                    Text(
                        text = "PRODUCT MASTER (Auto-Fills HS Code):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        masterProducts.forEach { mp ->
                            val isSelected = productName.equals(mp.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedMaster = mp
                                    productName = mp.name
                                    hsCode = mp.hsCode // Automatically fill HS Code!
                                    description = mp.description
                                    countryOfOrigin = mp.countryOfOrigin
                                    unitsPerCartonText = mp.unitsPerCarton.toString()
                                    unitWeightText = mp.defaultUnitWeightKg.toString()
                                    cbmPerCartonText = mp.defaultCbmPerCarton.toString()
                                    quantityText = (cartons * mp.unitsPerCarton).toString()
                                },
                                label = {
                                    Column {
                                        Text(mp.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("HS: ${mp.hsCode}", fontSize = 9.sp, color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                modifier = Modifier.testTag("chip_product_${mp.name.replace(" ", "_")}")
                            )
                        }
                    }
                }

                // 1. Product Name with Dropdown Selector
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = productName,
                            onValueChange = { newName ->
                                productName = newName
                                // Auto-fill HS Code when user types or matches a master product
                                val match = masterProducts.find {
                                    it.name.equals(newName.trim(), ignoreCase = true) ||
                                    (newName.trim().length >= 3 && it.name.contains(newName.trim(), ignoreCase = true))
                                }
                                if (match != null) {
                                    selectedMaster = match
                                    hsCode = match.hsCode // Automatically fill HS Code!
                                    countryOfOrigin = match.countryOfOrigin
                                    description = match.description
                                    unitsPerCartonText = match.unitsPerCarton.toString()
                                    unitWeightText = match.defaultUnitWeightKg.toString()
                                    cbmPerCartonText = match.defaultCbmPerCarton.toString()
                                }
                            },
                            label = { Text("Product * (Select or Type)") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { dropdownExpanded = !dropdownExpanded },
                                    modifier = Modifier.testTag("btn_toggle_product_dropdown")
                                ) {
                                    Icon(
                                        imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Show products dropdown"
                                    )
                                }
                            },
                            supportingText = {
                                Text("Select from master database to auto-fill HS code")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_product_name")
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            masterProducts.forEach { mp ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(mp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("HS: ${mp.hsCode} • ${mp.category}", fontSize = 11.sp, color = PrimaryBlue)
                                        }
                                    },
                                    onClick = {
                                        selectedMaster = mp
                                        productName = mp.name
                                        hsCode = mp.hsCode // Automatically fill HS Code!
                                        description = mp.description
                                        countryOfOrigin = mp.countryOfOrigin
                                        unitsPerCartonText = mp.unitsPerCarton.toString()
                                        unitWeightText = mp.defaultUnitWeightKg.toString()
                                        cbmPerCartonText = mp.defaultCbmPerCarton.toString()
                                        quantityText = (cartons * mp.unitsPerCarton).toString()
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("dropdown_item_${mp.name.replace(" ", "_")}")
                                )
                            }
                        }
                    }
                }

                // Auto-filled confirmation badge
                if (hsCode.isNotBlank()) {
                    item {
                        Surface(
                            color = StatusGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-filled HS Code: $hsCode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                            }
                        }
                    }
                }

                // 2. HS Code & 3. Country of Origin
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hsCode,
                            onValueChange = { hsCode = it },
                            label = { Text("HS Code *") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified HS code",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            supportingText = {
                                Text("Auto-filled from Master", fontSize = 10.sp)
                            },
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("input_hs_code")
                        )
                        OutlinedTextField(
                            value = countryOfOrigin,
                            onValueChange = { countryOfOrigin = it },
                            label = { Text("Origin *") },
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("input_country_of_origin")
                        )
                    }
                }

                // 4. Quantity (Units)
                item {
                    OutlinedTextField(
                        value = if (quantityText.isNotBlank()) quantityText else "$calculatedQuantity",
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity (Total Units)") },
                        supportingText = { Text("Auto-calculated: $cartons ctns × $unitsPerCarton units/ctn") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_quantity")
                    )
                }

                // 5. Cartons & 6. Units / Carton
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cartonsText,
                            onValueChange = {
                                cartonsText = it
                                quantityText = ""
                            },
                            label = { Text("Cartons *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_cartons")
                        )
                        OutlinedTextField(
                            value = unitsPerCartonText,
                            onValueChange = {
                                unitsPerCartonText = it
                                quantityText = ""
                            },
                            label = { Text("Units / Carton *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_units_per_carton")
                        )
                    }
                }

                // 7. Unit Weight
                item {
                    OutlinedTextField(
                        value = unitWeightText,
                        onValueChange = { unitWeightText = it },
                        label = { Text("Unit Weight (kg) *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_unit_weight")
                    )
                }

                // The system automatically calculates: Total Units, Net Weight, Gross Weight, CBM, Total Cartons
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "THE SYSTEM AUTOMATICALLY CALCULATES:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PrimaryBlue,
                                letterSpacing = 0.5.sp
                            )
                            DetailParamRow("Total Units", "$calculatedQuantity units ($cartons ctns × $unitsPerCarton units/ctn)")
                            DetailParamRow("Net Weight", "%.2f kg ($calculatedQuantity units × %.3f kg)".format(netWeight, unitWeight))
                            DetailParamRow("Gross Weight", "%.2f kg ($cartons ctns × %.2f kg/ctn)".format(grossWeight, cartonGrossWeight))
                            DetailParamRow("CBM", "%.4f m³ ($cartons ctns × %.4f cbm/ctn)".format(totalCbm, cbmPerCarton))
                            DetailParamRow("Total Cartons", "$cartons cartons")
                        }
                    }
                }

                // 10. CBM / Carton & Carton Gross Weight inputs for tuning
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cartonGrossWeightText,
                            onValueChange = { cartonGrossWeightText = it },
                            label = { Text("Carton Gross Wt (kg)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cbmPerCartonText,
                            onValueChange = { cbmPerCartonText = it },
                            label = { Text("CBM / Carton (m³)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Manual Override Flag
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isManualOverride,
                            onCheckedChange = { isManualOverride = it }
                        )
                        Text("Flag as Manual Override (logged in Audit Trail)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank() && cartons > 0) {
                        val saved = ShipmentProduct(
                            id = product?.id ?: 0L,
                            shipmentId = shipmentId,
                            productId = selectedMaster?.id ?: 1L,
                            productName = productName.trim(),
                            hsCode = hsCode.trim(),
                            description = description.ifBlank { productName.trim() },
                            cartons = cartons,
                            unitsPerCarton = unitsPerCarton,
                            totalUnits = calculatedQuantity,
                            individualUnitWeightKg = unitWeight,
                            totalNetWeightKg = netWeight,
                            cartonGrossWeightKg = cartonGrossWeight,
                            totalGrossWeightKg = grossWeight,
                            cbmPerCarton = cbmPerCarton,
                            totalCbm = totalCbm,
                            countryOfOrigin = countryOfOrigin.trim(),
                            unitPriceUsd = unitPrice,
                            totalAmountUsd = totalAmount,
                            isManualOverride = isManualOverride
                        )
                        onSave(saved)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_product_btn")
            ) {
                Text(
                    text = "SAVE PRODUCT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentEditScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit,
    onShipmentCreated: (String) -> Unit
) {
    val masterProducts by viewModel.allProducts.collectAsState()
    val parties by viewModel.allParties.collectAsState()
    val offlineDrafts by viewModel.allOfflineDrafts.collectAsState()
    val context = LocalContext.current
    val shipmentDraft = offlineDrafts.firstOrNull { it.draftType == "SHIPMENT" }

    var shipmentId by remember { mutableStateOf("EXP_" + System.currentTimeMillis().toString().takeLast(6)) }
    var reference by remember { mutableStateOf("EXP-2026-US-" + (10..99).random()) }
    var customer by remember { mutableStateOf("OUTLAW DIP COMPANY INC.") }
    var poNumber by remember { mutableStateOf("PO-ODC-" + (1000..9999).random()) }
    var invoiceNumber by remember { mutableStateOf("EXP-INV-2026-" + (100..999).random()) }
    var invoiceDate by remember { mutableStateOf("2026-09-30") }
    var currency by remember { mutableStateOf("USD") }
    var destination by remember { mutableStateOf("New York, USA") }
    var shippingMode by remember { mutableStateOf(ShippingMode.SEA) }
    var portOfLoading by remember { mutableStateOf("Colombo, Sri Lanka") }
    var portOfDischarge by remember { mutableStateOf("New York, USA") }
    var placeOfDelivery by remember { mutableStateOf("820 Grand Blvd, Deer Park, NY 11729, USA") }
    var vessel by remember { mutableStateOf("CMA CGM COLOMBO") }
    var voyage by remember { mutableStateOf("048W") }
    var etd by remember { mutableStateOf("2026-10-15") }
    var containerNumber by remember { mutableStateOf("CMAU" + (1000000..9999999).random() + "/20") }
    var masterBlNumber by remember { mutableStateOf("CMDU" + (10000000..99999999).random()) }
    var amsBlNumber by remember { mutableStateOf("AMS-CMDU-" + (10000..99999).random()) }

    // Selected initial products
    var selectedProductsList by remember {
        mutableStateOf(
            masterProducts.take(2).map { mp ->
                ShipmentProduct(
                    shipmentId = shipmentId,
                    productId = mp.id,
                    productName = mp.name,
                    hsCode = mp.hsCode,
                    description = mp.description,
                    cartons = 100,
                    unitsPerCarton = mp.unitsPerCarton,
                    totalUnits = 100 * mp.unitsPerCarton,
                    individualUnitWeightKg = mp.defaultUnitWeightKg,
                    totalNetWeightKg = 100 * mp.unitsPerCarton * mp.defaultUnitWeightKg,
                    cartonGrossWeightKg = 8.5,
                    totalGrossWeightKg = 850.0,
                    cbmPerCarton = mp.defaultCbmPerCarton,
                    totalCbm = 100 * mp.defaultCbmPerCarton,
                    countryOfOrigin = mp.countryOfOrigin,
                    unitPriceUsd = 2.0,
                    totalAmountUsd = 100 * mp.unitsPerCarton * 2.0
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Export Shipment") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Offline Draft Banner if a draft is available
            if (shipmentDraft != null) {
                item {
                    OfflineDraftBanner(
                        draft = shipmentDraft,
                        onResume = {
                            customer = shipmentDraft.title.substringAfter(" - ").ifBlank { customer }
                            Toast.makeText(context, "Restored offline draft: ${shipmentDraft.title}", Toast.LENGTH_SHORT).show()
                        },
                        onDiscard = {
                            viewModel.deleteOfflineDraft(shipmentDraft.draftId)
                            Toast.makeText(context, "Draft discarded", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusBlueContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Single-Entry Workflow: Enter shipment info once. Commercial Invoice, Packing List, ISF 10+2 and B/L will be automatically generated with matching data!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Text("Shipment Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = shipmentId,
                    onValueChange = { shipmentId = it },
                    label = { Text("Shipment No.") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Customer") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Quick pick from registered Consignees
                val consigneeParties = parties.filter { it.partyType == PartyType.CONSIGNEE }
                if (consigneeParties.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Select Consignee:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        consigneeParties.forEach { cp ->
                            FilterChip(
                                selected = customer == cp.companyName,
                                onClick = {
                                    customer = cp.companyName
                                    destination = cp.country
                                    placeOfDelivery = cp.address
                                },
                                label = { Text(cp.companyName, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = poNumber,
                        onValueChange = { poNumber = it },
                        label = { Text("PO Number") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Invoice Number") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppDatePickerField(
                        label = "Invoice Date",
                        value = invoiceDate,
                        onDateSelected = { invoiceDate = it },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = destination,
                    onValueChange = {
                        destination = it
                        portOfDischarge = it
                    },
                    label = { Text("Destination") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // [ SEA ] [ AIR ]
                ShippingModeSelector(
                    selectedMode = shippingMode,
                    onModeSelected = { shippingMode = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = portOfLoading,
                        onValueChange = { portOfLoading = it },
                        label = { Text("Port of Loading") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = portOfDischarge,
                        onValueChange = { portOfDischarge = it },
                        label = { Text("Port of Discharge") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = placeOfDelivery,
                    onValueChange = { placeOfDelivery = it },
                    label = { Text("Place of Delivery") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Vessel & Carrier Routing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = vessel, onValueChange = { vessel = it }, label = { Text("Vessel Name") }, modifier = Modifier.weight(1.2f))
                    OutlinedTextField(value = voyage, onValueChange = { voyage = it }, label = { Text("Voyage") }, modifier = Modifier.weight(0.8f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = containerNumber, onValueChange = { containerNumber = it }, label = { Text("Container Number") }, modifier = Modifier.weight(1f))
                    AppDatePickerField(
                        label = "ETD Date",
                        value = etd,
                        onDateSelected = { etd = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = masterBlNumber, onValueChange = { masterBlNumber = it }, label = { Text("Master B/L") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = amsBlNumber, onValueChange = { amsBlNumber = it }, label = { Text("AMS B/L") }, modifier = Modifier.weight(1f))
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SHIPMENT PRODUCTS (${selectedProductsList.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to add from Product Master (auto-fills HS Code):",
                            fontSize = 11.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            masterProducts.forEach { mp ->
                                val isAdded = selectedProductsList.any { it.productName.equals(mp.name, ignoreCase = true) }
                                FilterChip(
                                    selected = isAdded,
                                    onClick = {
                                        if (isAdded) {
                                            selectedProductsList = selectedProductsList.filterNot { it.productName.equals(mp.name, ignoreCase = true) }
                                        } else {
                                            selectedProductsList = selectedProductsList + ShipmentProduct(
                                                shipmentId = shipmentId,
                                                productId = mp.id,
                                                productName = mp.name,
                                                hsCode = mp.hsCode, // Automatically fill HS Code!
                                                description = mp.description,
                                                cartons = 100,
                                                unitsPerCarton = mp.unitsPerCarton,
                                                totalUnits = 100 * mp.unitsPerCarton,
                                                individualUnitWeightKg = mp.defaultUnitWeightKg,
                                                totalNetWeightKg = 100 * mp.unitsPerCarton * mp.defaultUnitWeightKg,
                                                cartonGrossWeightKg = 8.5,
                                                totalGrossWeightKg = 850.0,
                                                cbmPerCarton = mp.defaultCbmPerCarton,
                                                totalCbm = 100 * mp.defaultCbmPerCarton,
                                                countryOfOrigin = mp.countryOfOrigin,
                                                unitPriceUsd = 2.0,
                                                totalAmountUsd = 100 * mp.unitsPerCarton * 2.0
                                            )
                                        }
                                    },
                                    label = {
                                        Column {
                                            Text(mp.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("HS: ${mp.hsCode}", fontSize = 9.sp)
                                        }
                                    }
                                )
                            }
                        }

                        if (selectedProductsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            selectedProductsList.forEach { sp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sp.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("HS: ${sp.hsCode} • ${sp.countryOfOrigin}", fontSize = 11.sp, color = PrimaryBlue)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    val newCartons = maxOf(10, sp.cartons - 10)
                                                    selectedProductsList = selectedProductsList.map {
                                                        if (it.productName == sp.productName) it.copy(
                                                            cartons = newCartons,
                                                            totalUnits = newCartons * it.unitsPerCarton,
                                                            totalNetWeightKg = newCartons * it.unitsPerCarton * it.individualUnitWeightKg,
                                                            totalGrossWeightKg = newCartons * it.cartonGrossWeightKg,
                                                            totalCbm = newCartons * it.cbmPerCarton,
                                                            totalAmountUsd = newCartons * it.unitsPerCarton * it.unitPriceUsd
                                                        ) else it
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease 10 ctns", tint = PrimaryBlue)
                                            }
                                            Text("${sp.cartons} ctns", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            IconButton(
                                                onClick = {
                                                    val newCartons = sp.cartons + 10
                                                    selectedProductsList = selectedProductsList.map {
                                                        if (it.productName == sp.productName) it.copy(
                                                            cartons = newCartons,
                                                            totalUnits = newCartons * it.unitsPerCarton,
                                                            totalNetWeightKg = newCartons * it.unitsPerCarton * it.individualUnitWeightKg,
                                                            totalGrossWeightKg = newCartons * it.cartonGrossWeightKg,
                                                            totalCbm = newCartons * it.cbmPerCarton,
                                                            totalAmountUsd = newCartons * it.unitsPerCarton * it.unitPriceUsd
                                                        ) else it
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase 10 ctns", tint = PrimaryBlue)
                                            }
                                        }
                                    }
                                    IconButton(onClick = {
                                        selectedProductsList = selectedProductsList.filterNot { it.id == sp.id && it.productName == sp.productName }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = StatusRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveOfflineDraft(
                                draftId = shipmentId,
                                draftType = "SHIPMENT",
                                title = "$shipmentId - $customer",
                                summary = "PO: $poNumber • $destination • ${selectedProductsList.sumOf { it.cartons }} ctns",
                                payloadJson = """{"id":"$shipmentId","customer":"$customer","po":"$poNumber","invoice":"$invoiceNumber","etd":"$etd"}"""
                            )
                            Toast.makeText(context, "Saved offline draft for $shipmentId", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_offline_draft_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = StatusAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE DRAFT", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = StatusAmber)
                    }

                    Button(
                        onClick = {
                            val newShip = Shipment(
                                id = shipmentId,
                                reference = reference,
                                customer = customer,
                                poNumber = poNumber,
                                invoiceNumber = invoiceNumber,
                                invoiceDate = invoiceDate,
                                currency = currency,
                                shippingMode = shippingMode,
                                portOfLoading = portOfLoading,
                                portOfDischarge = portOfDischarge,
                                placeOfDelivery = placeOfDelivery,
                                vessel = vessel,
                                voyage = voyage,
                                etd = etd,
                                containerNumber = containerNumber,
                                masterBlNumber = masterBlNumber,
                                amsBlNumber = amsBlNumber,
                                status = ShipmentStatus.DOCUMENTATION
                            )
                            viewModel.createShipment(newShip, selectedProductsList)
                            viewModel.deleteOfflineDraft(shipmentId)
                            Toast.makeText(context, "Shipment $shipmentId created successfully!", Toast.LENGTH_SHORT).show()
                            onShipmentCreated(newShip.id)
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .height(50.dp)
                            .testTag("submit_create_shipment_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CREATE & GENERATE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ShippingModeSelector(
    selectedMode: ShippingMode,
    onModeSelected: (ShippingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Shipping Mode",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isSea = selectedMode == ShippingMode.SEA
            OutlinedButton(
                onClick = { onModeSelected(ShippingMode.SEA) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("mode_sea_btn"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSea) PrimaryBlue else Color.Transparent,
                    contentColor = if (isSea) Color.White else Navy800
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isSea) PrimaryBlue else LightBorder)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isSea) Color.White else PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SEA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            val isAir = selectedMode == ShippingMode.AIR
            OutlinedButton(
                onClick = { onModeSelected(ShippingMode.AIR) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("mode_air_btn"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isAir) PrimaryBlue else Color.Transparent,
                    contentColor = if (isAir) Color.White else Navy800
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isAir) PrimaryBlue else LightBorder)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    Icons.Default.Flight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isAir) Color.White else PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AIR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: ShipmentStatus) {
    val (color, bgColor) = when (status) {
        ShipmentStatus.NEW -> Pair(Navy700, LightSurfaceVariant)
        ShipmentStatus.ORDER_CONFIRMED, ShipmentStatus.PRODUCTION -> Pair(PrimaryBlue, StatusBlueContainer)
        ShipmentStatus.PACKED, ShipmentStatus.DOCUMENTATION -> Pair(TertiaryAmber, StatusAmberContainer)
        ShipmentStatus.ISF_FILED, ShipmentStatus.BOOKING_CONFIRMED, ShipmentStatus.LOADED, ShipmentStatus.BL_RELEASED -> Pair(SecondaryTeal, LightSurfaceVariant)
        ShipmentStatus.SHIPPED -> Pair(StatusBlue, StatusBlueContainer)
        ShipmentStatus.DELIVERED -> Pair(StatusGreen, StatusGreenContainer)
        ShipmentStatus.CANCELLED -> Pair(StatusRed, StatusRedContainer)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ShipmentSummaryCard(
    shipment: Shipment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("shipment_item_${shipment.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StatusBlueContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TRACKING ID: ${shipment.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CLIENT: ${shipment.customer}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (shipment.shippingMode == ShippingMode.SEA) StatusBlueContainer else StatusAmberContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (shipment.shippingMode == ShippingMode.SEA) "[ SEA ]" else "[ AIR ]",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (shipment.shippingMode == ShippingMode.SEA) PrimaryBlue else TertiaryAmber
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = shipment.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4-Milestone Mini Progress Tracker: Packing → Invoiced → Validated → Dispatched
            ShipmentMiniMilestoneTracker(status = shipment.status)

            Spacer(modifier = Modifier.height(8.dp))

            // Key parameters row: PO Number, Invoice Number, Currency
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PO Number", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(shipment.poNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Invoice No.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${shipment.invoiceNumber} (${shipment.invoiceDate})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Currency", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(shipment.currency, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Destination: ${shipment.portOfDischarge}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Port of Loading: ${shipment.portOfLoading} → Port of Discharge: ${shipment.portOfDischarge}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Place of Delivery: ${shipment.placeOfDelivery}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Vessel: ${shipment.vessel}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ETD: ${shipment.etd}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun GenerateDocumentsCard(
    shipment: Shipment,
    invoice: CommercialInvoice?,
    packingList: PackingList?,
    isf: IsfFiling?,
    bl: BlInstructions?,
    products: List<ShipmentProduct>,
    pallets: List<Pallet>,
    onNavigateToValidation: () -> Unit
) {
    val context = LocalContext.current
    var checkInvoice by remember { mutableStateOf(true) }
    var checkPackingList by remember { mutableStateOf(true) }
    var checkIsf by remember { mutableStateOf(true) }
    var checkBl by remember { mutableStateOf(true) }
    var showDossierPreview by remember { mutableStateOf(false) }

    if (showDossierPreview) {
        PdfPreviewDialog(
            title = "Export Dossier",
            documentId = shipment.id,
            onDismiss = { showDossierPreview = false },
            onDownloadPdf = {
                val html = DocumentGenerator.generateCombinedDossierHtml(
                    shipment = shipment,
                    invoice = invoice,
                    packingList = packingList,
                    isf = isf,
                    bl = bl,
                    products = products,
                    pallets = pallets,
                    includeInvoice = checkInvoice,
                    includePackingList = checkPackingList,
                    includeIsf = checkIsf,
                    includeBl = checkBl
                )
                DocumentGenerator.printDocument(context, html, "ExportDossier_${shipment.id}")
                showDossierPreview = false
            },
            onShare = {
                val summaryText = "Export Dispatch Dossier: ${shipment.id}\nVessel: ${shipment.vessel}\nContainer: ${shipment.containerNumber}"
                DocumentGenerator.shareDocument(context, "Dossier ${shipment.id}", summaryText)
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (checkInvoice && invoice != null) {
                    A4CommercialInvoiceDocument(shipment = shipment, invoice = invoice, products = products)
                }
                if (checkPackingList && packingList != null) {
                    A4PackingListDocument(shipment = shipment, packingList = packingList, products = products, pallets = pallets)
                }
                if (checkIsf && isf != null) {
                    A4IsfFilingDocument(shipment = shipment, isf = isf)
                }
                if (checkBl && bl != null) {
                    A4BlInstructionsDocument(shipment = shipment, bl = bl)
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("generate_documents_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "GENERATE DOCUMENTS",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Select documents to bundle, validate, and export:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Checkboxes: Commercial Invoice, Packing List, ISF Filing, B/L Instructions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checkInvoice,
                        onCheckedChange = { checkInvoice = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Text("Commercial Invoice", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checkPackingList,
                        onCheckedChange = { checkPackingList = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Text("Packing List", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checkIsf,
                        onCheckedChange = { checkIsf = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Text("ISF Filing", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checkBl,
                        onCheckedChange = { checkBl = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Text("B/L Instructions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // [ VALIDATE ALL ]
            Button(
                onClick = onNavigateToValidation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("validate_all_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("VALIDATE ALL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // [ PREVIEW DOSSIER (PDF) ]
            OutlinedButton(
                onClick = { showDossierPreview = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("preview_dossier_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(6.dp))
                Text("PREVIEW EXPORT DOSSIER (A4 PDF)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row: [ GENERATE PDF ] [ EXPORT EXCEL ] [ SHARE ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        val html = DocumentGenerator.generateCombinedDossierHtml(
                            shipment = shipment,
                            invoice = invoice,
                            packingList = packingList,
                            isf = isf,
                            bl = bl,
                            products = products,
                            pallets = pallets,
                            includeInvoice = checkInvoice,
                            includePackingList = checkPackingList,
                            includeIsf = checkIsf,
                            includeBl = checkBl
                        )
                        DocumentGenerator.printDocument(context, html, "ExportDossier_${shipment.id}")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("generate_pdf_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("GENERATE PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        val csv = DocumentGenerator.generateCsvExport(
                            shipment, products, pallets, invoice, packingList, isf, bl
                        )
                        DocumentGenerator.shareDocument(context, "Export Manifest ${shipment.id}", csv)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("export_excel_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("EXPORT EXCEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        val summaryText = buildString {
                            appendLine("EXPORT DISPATCH DOSSIER: ${shipment.id}")
                            appendLine("Customer: ${shipment.customer}")
                            appendLine("Port: ${shipment.portOfLoading} -> ${shipment.portOfDischarge}")
                            appendLine("Vessel: ${shipment.vessel} ${shipment.voyage}")
                            appendLine("Container: ${shipment.containerNumber}")
                            if (checkInvoice && invoice != null) appendLine("• Commercial Invoice: ${invoice.invoiceNumber} ($ ${invoice.totalAmount})")
                            if (checkPackingList && packingList != null) appendLine("• Packing List: ${packingList.totalCartons} Cartons / ${packingList.totalGrossWeightKg} kg")
                            if (checkIsf && isf != null) appendLine("• ISF 10+2: Transmitted (${isf.cartonsCount} ctns)")
                            if (checkBl && bl != null) appendLine("• B/L Instructions: ${bl.kindOfPackages}")
                        }
                        DocumentGenerator.shareDocument(context, "Dossier ${shipment.id}", summaryText)
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(40.dp)
                        .testTag("share_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TertiaryAmber),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("SHARE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
