package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.engine.DocumentGenerator
import com.example.ui.ExportViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMasterScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val canManage = currentUser.role != UserRole.VIEWER

    val filteredProducts = remember(products, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            products
        } else {
            products.filter { p ->
                p.name.lowercase().contains(query) ||
                p.hsCode.lowercase().contains(query) ||
                p.category.lowercase().contains(query) ||
                p.description.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Product Master Database", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${products.size} standard HS tariff items", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canManage) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showDialog = true
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_product_master")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Product name, HS code or category") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_product_master")
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SwipeLeft, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Swipe-to-Delete Enabled",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Swipe left or right on any product to remove outdated or incorrect entries. Tap pencil to edit.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No products found matching query", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(
                items = filteredProducts,
                key = { it.id }
            ) { p ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (canManage && (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd)) {
                            viewModel.deleteProductMaster(p)
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Removed '${p.name}' (HS: ${p.hsCode})",
                                    actionLabel = "UNDO",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.addProductMaster(p)
                                }
                            }
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = canManage,
                    enableDismissFromEndToStart = canManage,
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                Text("Delete Entry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_master_item_${p.name.replace(" ", "_")}"),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StatusBlueContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "HS: ${p.hsCode}",
                                                fontSize = 11.sp,
                                                color = PrimaryBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "• ${p.category}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (canManage) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingProduct = p
                                                showDialog = true
                                            },
                                            modifier = Modifier.testTag("edit_product_${p.id}")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteProductMaster(p)
                                                coroutineScope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "Removed '${p.name}' (HS: ${p.hsCode})",
                                                        actionLabel = "UNDO",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.addProductMaster(p)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.testTag("delete_product_${p.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                                        }
                                    }
                                }
                            }

                            if (p.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = p.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Unit Wt", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${p.defaultUnitWeightKg} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column {
                                    Text("Units/Ctn", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${p.unitsPerCarton}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column {
                                    Text("Carton Size", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(p.defaultCartonSize, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Column {
                                    Text("CBM/Ctn", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${p.defaultCbmPerCarton}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (showDialog) {
        val p = editingProduct
        var name by remember { mutableStateOf(p?.name ?: "") }
        var hsCode by remember { mutableStateOf(p?.hsCode ?: "") }
        var description by remember { mutableStateOf(p?.description ?: "") }
        var origin by remember { mutableStateOf(p?.countryOfOrigin ?: "Sri Lanka") }
        var unitWt by remember { mutableStateOf(p?.defaultUnitWeightKg?.toString() ?: "0.20") }
        var size by remember { mutableStateOf(p?.defaultCartonSize ?: "30 x 30 x 20 cm") }
        var unitsPerCtn by remember { mutableStateOf(p?.unitsPerCarton?.toString() ?: "50") }
        var cbm by remember { mutableStateOf(p?.defaultCbmPerCarton?.toString() ?: "0.018") }
        var category by remember { mutableStateOf(p?.category ?: "Manufactured") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (p == null) "New Product Master" else "Edit Product", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth().testTag("input_pm_name")) }
                    item { OutlinedTextField(value = hsCode, onValueChange = { hsCode = it }, label = { Text("HS Tariff Code *") }, modifier = Modifier.fillMaxWidth().testTag("input_pm_hscode")) }
                    item { OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = unitWt, onValueChange = { unitWt = it }, label = { Text("Unit Wt (kg)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = unitsPerCtn, onValueChange = { unitsPerCtn = it }, label = { Text("Units / Ctn") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Carton Size") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = cbm, onValueChange = { cbm = it }, label = { Text("CBM / Ctn") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newProd = Product(
                            id = p?.id ?: 0L,
                            name = name,
                            description = description,
                            hsCode = hsCode,
                            countryOfOrigin = origin,
                            defaultUnitWeightKg = unitWt.toDoubleOrNull() ?: 0.2,
                            defaultCartonSize = size,
                            unitsPerCarton = unitsPerCtn.toIntOrNull() ?: 50,
                            defaultCbmPerCarton = cbm.toDoubleOrNull() ?: 0.018,
                            category = category
                        )
                        if (p == null) {
                            viewModel.addProductMaster(newProd)
                        } else {
                            viewModel.updateProductMaster(newProd)
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.testTag("btn_save_product_master")
                ) {
                    Text("Save Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyMasterScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val parties by viewModel.allParties.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingParty by remember { mutableStateOf<Party?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf<PartyType?>(null) }

    val canManage = currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.DOCUMENTATION_OFFICER

    val filteredParties = parties.filter { p ->
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isEmpty() ||
                p.companyName.lowercase().contains(query) ||
                p.country.lowercase().contains(query) ||
                p.contactPerson.lowercase().contains(query) ||
                p.taxId.lowercase().contains(query) ||
                p.fdaRegistration.lowercase().contains(query) ||
                p.importerOfRecordNumber.lowercase().contains(query)
        val matchesType = selectedFilterType == null || p.partyType == selectedFilterType
        matchesQuery && matchesType
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Parties & Trade Entities", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${filteredParties.size} of ${parties.size} registered entities", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canManage) {
                FloatingActionButton(
                    onClick = {
                        editingParty = null
                        showDialog = true
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Party")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search by name, country, tax ID, FDA...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 8 Party Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilterType == null,
                    onClick = { selectedFilterType = null },
                    label = { Text("All (${parties.size})", fontSize = 11.sp) }
                )
                PartyType.values().forEach { type ->
                    val count = parties.count { it.partyType == type }
                    FilterChip(
                        selected = selectedFilterType == type,
                        onClick = { selectedFilterType = if (selectedFilterType == type) null else type },
                        label = { Text("${type.label} ($count)", fontSize = 11.sp) }
                    )
                }
            }

            HorizontalDivider(color = LightBorder, modifier = Modifier.padding(vertical = 4.dp))

            if (filteredParties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = LightTextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching party entities found", fontWeight = FontWeight.Bold, color = Navy800)
                        Text("Try clearing the search or category filter", fontSize = 12.sp, color = LightTextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredParties) { party ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                    Text(
                                        text = party.companyName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (party.partyType) {
                                                    PartyType.EXPORTER -> StatusBlueContainer
                                                    PartyType.CONSIGNEE -> StatusGreenContainer
                                                    PartyType.NOTIFY_PARTY -> StatusAmberContainer
                                                    PartyType.IMPORTER_OF_RECORD -> Color(0xFFF3E8FF)
                                                    PartyType.MANUFACTURER -> Color(0xFFE0F2FE)
                                                    PartyType.SELLER -> Color(0xFFFEF3C7)
                                                    PartyType.STUFFING_LOCATION -> Color(0xFFF1F5F9)
                                                    PartyType.CONSOLIDATOR -> Color(0xFFCCFBF1)
                                                    else -> LightSurfaceVariant
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = party.partyType.label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (party.partyType) {
                                                PartyType.EXPORTER -> PrimaryBlue
                                                PartyType.CONSIGNEE -> StatusGreen
                                                PartyType.NOTIFY_PARTY -> TertiaryAmber
                                                PartyType.IMPORTER_OF_RECORD -> Color(0xFF7E22CE)
                                                PartyType.MANUFACTURER -> Color(0xFF0284C7)
                                                PartyType.SELLER -> Color(0xFFD97706)
                                                PartyType.STUFFING_LOCATION -> Navy800
                                                PartyType.CONSOLIDATOR -> SecondaryTeal
                                                else -> Navy700
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${party.address}, ${party.country}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = LightTextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${party.contactPerson} • ${party.telephone} • ${party.email}",
                                        fontSize = 11.sp,
                                        color = Navy800
                                    )
                                }

                                if (party.taxId.isNotEmpty() || party.fdaRegistration.isNotEmpty() || party.importerOfRecordNumber.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LightSurfaceVariant, RoundedCornerShape(6.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (party.taxId.isNotEmpty()) {
                                            Column {
                                                Text("TAX / VAT ID", fontSize = 9.sp, color = LightTextSecondary, fontWeight = FontWeight.Bold)
                                                Text(party.taxId, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        if (party.fdaRegistration.isNotEmpty()) {
                                            Column {
                                                Text("FDA REGISTRATION", fontSize = 9.sp, color = SecondaryTeal, fontWeight = FontWeight.Bold)
                                                Text(party.fdaRegistration, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SecondaryTeal)
                                            }
                                        }
                                        if (party.importerOfRecordNumber.isNotEmpty()) {
                                            Column {
                                                Text("IOR NUMBER", fontSize = 9.sp, color = Color(0xFF7E22CE), fontWeight = FontWeight.Bold)
                                                Text(party.importerOfRecordNumber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7E22CE))
                                            }
                                        }
                                    }
                                }

                                if (canManage) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                editingParty = party
                                                showDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        val p = editingParty
        var name by remember { mutableStateOf(p?.companyName ?: "") }
        var address by remember { mutableStateOf(p?.address ?: "") }
        var country by remember { mutableStateOf(p?.country ?: "Sri Lanka") }
        var contact by remember { mutableStateOf(p?.contactPerson ?: "") }
        var phone by remember { mutableStateOf(p?.telephone ?: "") }
        var email by remember { mutableStateOf(p?.email ?: "") }
        var taxId by remember { mutableStateOf(p?.taxId ?: "") }
        var fdaReg by remember { mutableStateOf(p?.fdaRegistration ?: "") }
        var iorNum by remember { mutableStateOf(p?.importerOfRecordNumber ?: "") }
        var selectedType by remember { mutableStateOf(p?.partyType ?: PartyType.CONSIGNEE) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (p == null) "Add Trade Entity / Party" else "Edit Party Record",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "PARTY TYPE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Grid/Row of all 8 party types
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    PartyType.EXPORTER,
                                    PartyType.CONSIGNEE,
                                    PartyType.NOTIFY_PARTY,
                                    PartyType.IMPORTER_OF_RECORD
                                ).forEach { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    PartyType.MANUFACTURER,
                                    PartyType.SELLER,
                                    PartyType.STUFFING_LOCATION,
                                    PartyType.CONSOLIDATOR
                                ).forEach { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name *") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Street Address *") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country *") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Person") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telephone") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = taxId, onValueChange = { taxId = it }, label = { Text("Tax / VAT ID") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = fdaReg, onValueChange = { fdaReg = it }, label = { Text("FDA Registration (If applicable)") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = iorNum, onValueChange = { iorNum = it }, label = { Text("Importer of Record # (If US IOR)") }, modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val party = Party(
                                id = p?.id ?: 0L,
                                companyName = name.trim(),
                                address = address.trim(),
                                country = country.trim(),
                                contactPerson = contact.trim(),
                                telephone = phone.trim(),
                                email = email.trim(),
                                taxId = taxId.trim(),
                                fdaRegistration = fdaReg.trim(),
                                importerOfRecordNumber = iorNum.trim(),
                                partyType = selectedType
                            )
                            if (p == null) {
                                viewModel.addParty(party)
                            } else {
                                viewModel.updateParty(party)
                            }
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(if (p == null) "Save Party" else "Update Party")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val auditLogs by viewModel.allAuditLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Trail & Activity Log") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Immutable audit log: Tracks all user revisions, carton adjustments, manager overrides, and document transmissions.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            items(auditLogs) { log ->
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
                            Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                            Text(text = log.formattedDate, fontSize = 10.sp, color = LightTextMuted)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "User: ${log.userName} (${log.userRole}) • Ref: ${log.shipmentId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Target Doc: ${log.document}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Text(text = "${log.oldValue} → ${log.newValue}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelImportExportScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val currentShipment by viewModel.currentShipment.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val pallets by viewModel.currentPallets.collectAsState()
    val invoice by viewModel.currentInvoice.collectAsState()
    val packingList by viewModel.currentPackingList.collectAsState()
    val isf by viewModel.currentIsf.collectAsState()
    val bl by viewModel.currentBl.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    var importedFileName by remember { mutableStateOf<String?>("ECG_Export_Consignment_Sept2026.xlsx") }
    var importConfirmed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Excel Import & Multi-Sheet Export") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("EXPORT EXCEL / CSV") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("IMPORT FROM EXCEL") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Multi-sheet export section
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StatusGreenContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Structured Multi-Sheet Export Package", fontWeight = FontWeight.Bold, color = StatusGreen)
                                Text("Exports complete shipment data formatted into distinct functional worksheets:", fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Sheet 1: Shipment Header & Ports\n• Sheet 2: Commercial Invoice & Banking\n• Sheet 3: Packing List & Weights\n• Sheet 4: ISF 10+2 Customs Elements\n• Sheet 5: Ocean B/L Instructions\n• Sheet 6: Products & Tariffs\n• Sheet 7: Pallet Load Allocation\n• Sheet 8: Document Integrity Report", fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                if (currentShipment != null) {
                                    val csvContent = DocumentGenerator.generateCsvExport(
                                        currentShipment!!,
                                        products,
                                        pallets,
                                        invoice,
                                        packingList,
                                        isf,
                                        bl
                                    )
                                    DocumentGenerator.shareDocument(context, "Explic_Export_${currentShipment!!.id}.csv", csvContent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORT COMPREHENSIVE SPREADSHEET (CSV)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Excel Import simulator with sheet detection
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Spreadsheet Parser & Sheet Detection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Upload legacy export spreadsheets to automatically map columns into Explic Export Pro database.", fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        Card(
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = SecondaryTeal)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(importedFileName ?: "No file uploaded", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                    TextButton(onClick = {
                                        importedFileName = "Explic_Consignment_Upload_${System.currentTimeMillis().toString().takeLast(4)}.xlsx"
                                        importConfirmed = false
                                    }) {
                                        Text("Re-scan")
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Detected Spreadsheet Tabs:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                Text("✓ 'Copy of Sheet1' → Mapped to Shipment Record", fontSize = 11.sp)
                                Text("✓ 'Com Inv' → Mapped to Commercial Invoice", fontSize = 11.sp)
                                Text("✓ 'Packing List' → Mapped to Packing List & Cartons", fontSize = 11.sp)
                                Text("✓ 'ISF filing' → Mapped to ISF 10+2 Elements", fontSize = 11.sp)
                                Text("✓ 'BL Instructions' → Mapped to B/L Shipping Instructions", fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Mapped Fields Preview:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("• Invoice No: EXP-INV-2026-092\n• Consignee: OUTLAW DIP COMPANY INC.\n• Total Cartons: 505 (5 products)\n• Container: EMCU6409544/20\n• Vessel: CMA CGM MEXICO", fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        importConfirmed = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (importConfirmed) "✓ IMPORT COMPLETED & SYNCED" else "CONFIRM SPREADSHEET MAPPING")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
