package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
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

@Composable
fun DocumentCenterScreen(
    viewModel: ExportViewModel,
    onNavigateToDocumentDetail: (String) -> Unit,
    onNavigateToValidation: (String) -> Unit
) {
    val currentShipment by viewModel.currentShipment.collectAsState()
    val invoice by viewModel.currentInvoice.collectAsState()
    val packingList by viewModel.currentPackingList.collectAsState()
    val isf by viewModel.currentIsf.collectAsState()
    val bl by viewModel.currentBl.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val pallets by viewModel.currentPallets.collectAsState()
    val context = LocalContext.current

    var previewDocType by remember { mutableStateOf<String?>(null) }

    if (currentShipment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active shipment selected.")
        }
        return
    }

    val ship = currentShipment!!

    // Full PDF Preview Dialog triggered from Document Center
    if (previewDocType != null) {
        when (previewDocType) {
            "INVOICE" -> {
                if (invoice != null) {
                    PdfPreviewDialog(
                        title = "Commercial Invoice",
                        documentId = invoice!!.invoiceNumber,
                        onDismiss = { previewDocType = null },
                        onDownloadPdf = {
                            val html = DocumentGenerator.generateInvoiceHtml(ship, invoice!!, products)
                            DocumentGenerator.printDocument(context, html, "Invoice_${ship.id}")
                            previewDocType = null
                        },
                        onShare = {
                            val text = "Commercial Invoice for ${ship.id}\nInvoice: ${invoice!!.invoiceNumber}\nTotal: ${invoice!!.currency} ${"%.2f".format(products.sumOf { it.totalAmountUsd } + invoice!!.freight + invoice!!.insurance)}"
                            DocumentGenerator.shareDocument(context, "Invoice ${ship.id}", text)
                        }
                    ) {
                        A4CommercialInvoiceDocument(shipment = ship, invoice = invoice!!, products = products)
                    }
                }
            }
            "PACKING_LIST" -> {
                if (packingList != null) {
                    PdfPreviewDialog(
                        title = "Export Packing List",
                        documentId = packingList!!.piNumber,
                        onDismiss = { previewDocType = null },
                        onDownloadPdf = {
                            val html = DocumentGenerator.generatePackingListHtml(ship, packingList!!, products, pallets)
                            DocumentGenerator.printDocument(context, html, "PackingList_${ship.id}")
                            previewDocType = null
                        },
                        onShare = {
                            val text = "Packing List for ${ship.id}\nTotal Cartons: ${products.sumOf { it.cartons }}\nGross Weight: ${"%.1f".format(products.sumOf { it.totalGrossWeightKg })} kg\nContainer: ${packingList!!.containerNumber}"
                            DocumentGenerator.shareDocument(context, "Packing List ${ship.id}", text)
                        }
                    ) {
                        A4PackingListDocument(shipment = ship, packingList = packingList!!, products = products, pallets = pallets)
                    }
                }
            }
            "ISF" -> {
                if (isf != null) {
                    PdfPreviewDialog(
                        title = "ISF 10+2 Filing",
                        documentId = "CBP-ISF-${ship.reference}",
                        onDismiss = { previewDocType = null },
                        onDownloadPdf = {
                            val html = DocumentGenerator.generateIsfHtml(ship, isf!!)
                            DocumentGenerator.printDocument(context, html, "ISF_${ship.id}")
                            previewDocType = null
                        },
                        onShare = {
                            val text = "ISF 10+2 Filing for ${ship.id}\nVessel: ${isf!!.vessel}\nPO: ${isf!!.purchaseOrderNumber}\nCartons: ${isf!!.cartonsCount}"
                            DocumentGenerator.shareDocument(context, "ISF ${ship.id}", text)
                        }
                    ) {
                        A4IsfFilingDocument(shipment = ship, isf = isf!!)
                    }
                }
            }
            "BL" -> {
                if (bl != null) {
                    PdfPreviewDialog(
                        title = "B/L Instructions",
                        documentId = "BLI-${ship.reference}",
                        onDismiss = { previewDocType = null },
                        onDownloadPdf = {
                            val html = DocumentGenerator.generateBlHtml(ship, bl!!)
                            DocumentGenerator.printDocument(context, html, "BL_${ship.id}")
                            previewDocType = null
                        },
                        onShare = {
                            val text = "B/L Instructions for ${ship.id}\nVessel: ${bl!!.vessel}\nPackages: ${bl!!.kindOfPackages}\nGross Weight: ${bl!!.grossWeightKg} kg"
                            DocumentGenerator.shareDocument(context, "BL Instructions ${ship.id}", text)
                        }
                    ) {
                        A4BlInstructionsDocument(shipment = ship, bl = bl!!)
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("document_center_screen"),
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
                                text = "DOCUMENTATION CENTER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = ship.id,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = { onNavigateToValidation(ship.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Validate All", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vessel: ${ship.vessel} • Container: ${ship.containerNumber} • ${products.size} Products",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Generate Documents Checklist & Action Panel
        item {
            GenerateDocumentsCard(
                shipment = ship,
                invoice = invoice,
                packingList = packingList,
                isf = isf,
                bl = bl,
                products = products,
                pallets = pallets,
                onNavigateToValidation = { onNavigateToValidation(ship.id) }
            )
        }

        // 1. Commercial Invoice
        item {
            val invoiceCalculatedSubtotal = products.sumOf { it.totalAmountUsd }
            val invoiceCalculatedTotal = invoiceCalculatedSubtotal + (invoice?.freight ?: 0.0) + (invoice?.insurance ?: 0.0)
            DocItemCard(
                title = "Commercial Invoice",
                docNumber = invoice?.invoiceNumber ?: ship.invoiceNumber,
                status = invoice?.status ?: DocumentStatus.DRAFT,
                summary = "Amount: ${invoice?.currency ?: "USD"} ${"%.2f".format(invoiceCalculatedTotal)} • Terms: ${invoice?.paymentTerms ?: "T/T 30 Days"}",
                onViewEdit = { onNavigateToDocumentDetail("COMMERCIAL_INVOICE") },
                onPdfPreview = { previewDocType = "INVOICE" },
                onPrint = {
                    if (invoice != null) {
                        val html = DocumentGenerator.generateInvoiceHtml(ship, invoice!!, products)
                        DocumentGenerator.printDocument(context, html, "Invoice_${ship.id}")
                    }
                },
                onShare = {
                    if (invoice != null) {
                        val text = "Commercial Invoice for ${ship.id}\nInvoice: ${invoice!!.invoiceNumber}\nTotal: ${invoice!!.currency} ${"%.2f".format(invoiceCalculatedTotal)}\nBank: ${invoice!!.bankName} (${invoice!!.accountNumber})"
                        DocumentGenerator.shareDocument(context, "Invoice ${ship.id}", text)
                    }
                }
            )
        }

        // 2. Packing List
        item {
            val totalCtns = products.sumOf { it.cartons }.takeIf { it > 0 } ?: (packingList?.totalCartons ?: 0)
            val totalGw = products.sumOf { it.totalGrossWeightKg }.takeIf { it > 0.0 } ?: (packingList?.totalGrossWeightKg ?: 0.0)
            val palletCount = if (pallets.isNotEmpty()) pallets.size else (packingList?.totalPallets ?: 0)
            DocItemCard(
                title = "Packing List",
                docNumber = packingList?.piNumber ?: "PL-${ship.reference}",
                status = packingList?.status ?: DocumentStatus.DRAFT,
                summary = "Total: $totalCtns Cartons • ${"%.1f".format(totalGw)} kg Gross • $palletCount Pallets",
                onViewEdit = { onNavigateToDocumentDetail("PACKING_LIST") },
                onPdfPreview = { previewDocType = "PACKING_LIST" },
                onPrint = {
                    if (packingList != null) {
                        val html = DocumentGenerator.generatePackingListHtml(ship, packingList!!, products, pallets)
                        DocumentGenerator.printDocument(context, html, "PackingList_${ship.id}")
                    }
                },
                onShare = {
                    if (packingList != null) {
                        val text = "Packing List for ${ship.id}\nTotal Cartons: $totalCtns\nGross Weight: ${"%.1f".format(totalGw)} kg\nContainer: ${packingList!!.containerNumber}"
                        DocumentGenerator.shareDocument(context, "Packing List ${ship.id}", text)
                    }
                }
            )
        }

        // 3. ISF 10+2 Filing
        item {
            val isfCartons = isf?.cartonsCount ?: products.sumOf { it.cartons }
            DocItemCard(
                title = "ISF 10+2 Filing (U.S. CBP)",
                docNumber = "PO: ${isf?.purchaseOrderNumber ?: ship.poNumber}",
                status = isf?.status ?: DocumentStatus.DRAFT,
                summary = "Filer: ${isf?.completedByName ?: "Roshan Perera"} • Cartons: $isfCartons • Importer of Record: ${isf?.importerOfRecordNumber ?: "Pending"}",
                onViewEdit = { onNavigateToDocumentDetail("ISF") },
                onPdfPreview = { previewDocType = "ISF" },
                onPrint = {
                    if (isf != null) {
                        val html = DocumentGenerator.generateIsfHtml(ship, isf!!)
                        DocumentGenerator.printDocument(context, html, "ISF_${ship.id}")
                    }
                },
                onShare = {
                    if (isf != null) {
                        val text = "ISF 10+2 Filing for ${ship.id}\nVessel: ${isf!!.vessel}\nPO: ${isf!!.purchaseOrderNumber}\nCartons: $isfCartons\nHTS: ${isf!!.productCodeHtsCode}"
                        DocumentGenerator.shareDocument(context, "ISF ${ship.id}", text)
                    }
                }
            )
        }

        // 4. Bill of Lading Instructions
        item {
            val blGw = bl?.grossWeightKg?.takeIf { it > 0.0 } ?: products.sumOf { it.totalGrossWeightKg }
            DocItemCard(
                title = "Bill of Lading Instructions",
                docNumber = "B/L Draft - ${ship.vessel}",
                status = bl?.status ?: DocumentStatus.DRAFT,
                summary = "Packages: ${bl?.kindOfPackages ?: "9 Pallets / 505 Cartons"} • Gross Wt: ${"%.1f".format(blGw)} kg • Freight: ${bl?.freightTerms ?: "PREPAID"}",
                onViewEdit = { onNavigateToDocumentDetail("BL") },
                onPdfPreview = { previewDocType = "BL" },
                onPrint = {
                    if (bl != null) {
                        val html = DocumentGenerator.generateBlHtml(ship, bl!!)
                        DocumentGenerator.printDocument(context, html, "BL_Instructions_${ship.id}")
                    }
                },
                onShare = {
                    if (bl != null) {
                        val text = "B/L Instructions for ${ship.id}\nVessel: ${bl!!.vessel}\nPackages: ${bl!!.kindOfPackages}\nGross Weight: ${"%.1f".format(blGw)} kg"
                        DocumentGenerator.shareDocument(context, "BL Instructions ${ship.id}", text)
                    }
                }
            )
        }
    }
}

@Composable
fun DocItemCard(
    title: String,
    docNumber: String,
    status: DocumentStatus,
    summary: String,
    onViewEdit: () -> Unit,
    onPdfPreview: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit
) {
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
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = docNumber, fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
                DocStatusBadge(status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Action: View Official A4 Document
                Button(
                    onClick = onViewEdit,
                    modifier = Modifier.weight(1.1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("A4 Document", fontSize = 11.5.sp)
                }

                // PDF Preview Button
                OutlinedButton(
                    onClick = onPdfPreview,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF Preview", fontSize = 11.5.sp)
                }

                IconButton(
                    onClick = onPrint,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun DocStatusBadge(status: DocumentStatus) {
    val isDark = isSystemInDarkTheme()
    val (color, bgColor) = when (status) {
        DocumentStatus.DRAFT -> if (isDark) Pair(Color(0xFFCBD5E1), Color(0xFF334155)) else Pair(Navy700, LightSurfaceVariant)
        DocumentStatus.READY -> if (isDark) Pair(Color(0xFF93C5FD), Color(0xFF1E3A8A).copy(alpha = 0.5f)) else Pair(StatusBlue, StatusBlueContainer)
        DocumentStatus.VALIDATION_ERROR -> if (isDark) Pair(Color(0xFFFECACA), Color(0xFF7F1D1D).copy(alpha = 0.5f)) else Pair(StatusRed, StatusRedContainer)
        DocumentStatus.APPROVED -> if (isDark) Pair(Color(0xFF86EFAC), Color(0xFF064E3B).copy(alpha = 0.5f)) else Pair(StatusGreen, StatusGreenContainer)
        DocumentStatus.GENERATED -> if (isDark) Pair(Color(0xFF5EEAD4), Color(0xFF134E4A).copy(alpha = 0.5f)) else Pair(SecondaryTeal, LightSurfaceVariant)
        DocumentStatus.SENT -> if (isDark) Pair(Color(0xFF86EFAC), Color(0xFF064E3B).copy(alpha = 0.5f)) else Pair(StatusGreen, StatusGreenContainer)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = status.label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommercialInvoiceScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val shipment by viewModel.currentShipment.collectAsState()
    val invoice by viewModel.currentInvoice.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: A4 Official Document, 1: Edit Particulars
    var showPdfPreview by remember { mutableStateOf(false) }

    if (shipment == null || invoice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val ship = shipment!!
    val inv = invoice!!

    var invoiceNumber by remember { mutableStateOf(inv.invoiceNumber) }
    var invoiceDate by remember { mutableStateOf(inv.invoiceDate) }
    var paymentTerms by remember { mutableStateOf(inv.paymentTerms) }
    var freightText by remember { mutableStateOf(inv.freight.toString()) }
    var insuranceText by remember { mutableStateOf(inv.insurance.toString()) }
    var leadTime by remember { mutableStateOf(inv.leadTime) }
    var notes by remember { mutableStateOf(inv.notes) }
    var bankAccountName by remember { mutableStateOf(inv.accountName) }
    var bankName by remember { mutableStateOf(inv.bankName) }
    var accountNumber by remember { mutableStateOf(inv.accountNumber) }
    var branchCode by remember { mutableStateOf(inv.branchCode) }
    var bankCode by remember { mutableStateOf(inv.bankCode) }
    var swiftCode by remember { mutableStateOf(inv.swiftCode) }
    var bankAddress by remember { mutableStateOf(inv.bankAddress) }

    // Calculated totals from database
    val subtotal = products.sumOf { it.totalAmountUsd }
    val freight = freightText.toDoubleOrNull() ?: 0.0
    val insurance = insuranceText.toDoubleOrNull() ?: 0.0
    val grandTotal = subtotal + freight + insurance

    if (showPdfPreview) {
        PdfPreviewDialog(
            title = "Commercial Invoice",
            documentId = inv.invoiceNumber,
            onDismiss = { showPdfPreview = false },
            onDownloadPdf = {
                val html = DocumentGenerator.generateInvoiceHtml(ship, inv, products)
                DocumentGenerator.printDocument(context, html, "Invoice_${ship.id}")
                showPdfPreview = false
            },
            onShare = {
                val text = "Commercial Invoice for ${ship.id}\nInvoice: ${inv.invoiceNumber}\nTotal: ${inv.currency} ${"%.2f".format(grandTotal)}\nBank: ${inv.bankName}"
                DocumentGenerator.shareDocument(context, "Invoice ${ship.id}", text)
            }
        ) {
            A4CommercialInvoiceDocument(shipment = ship, invoice = inv, products = products)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Commercial Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(inv.invoiceNumber, fontSize = 11.sp, color = PrimaryBlue)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // PDF Preview Button
                    IconButton(onClick = { showPdfPreview = true }) {
                        Icon(Icons.Default.Visibility, contentDescription = "PDF Preview", tint = PrimaryBlue)
                    }
                    // Download / Print PDF Button
                    IconButton(onClick = {
                        val html = DocumentGenerator.generateInvoiceHtml(ship, inv, products)
                        DocumentGenerator.printDocument(context, html, "Invoice_${ship.id}")
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode Selector: A4 Document View vs Edit Form
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Document (A4)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Particulars")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Official Professional A4 Document View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quick Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPdfPreview = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Preview", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val html = DocumentGenerator.generateInvoiceHtml(ship, inv, products)
                                DocumentGenerator.printDocument(context, html, "Invoice_${ship.id}")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }

                    // Authentic A4 Rendered Document
                    A4CommercialInvoiceDocument(
                        shipment = ship,
                        invoice = inv,
                        products = products
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // Edit Particulars Form
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = inv.exporterName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                                Text(text = inv.exporterAddress, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "Consignee: ${inv.consigneeName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = "PO: ${ship.poNumber} • Container: ${ship.containerNumber}", fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = invoiceNumber, onValueChange = { invoiceNumber = it }, label = { Text("Invoice No") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = invoiceDate, onValueChange = { invoiceDate = it }, label = { Text("Date") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = paymentTerms, onValueChange = { paymentTerms = it }, label = { Text("Payment Terms") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = freightText, onValueChange = { freightText = it }, label = { Text("Ocean Freight ($)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = insuranceText, onValueChange = { insuranceText = it }, label = { Text("Insurance ($)") }, modifier = Modifier.weight(1f))
                        }
                    }

                    item {
                        Text(text = "BANK REMITTANCE DETAILS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        OutlinedTextField(value = bankAccountName, onValueChange = { bankAccountName = it }, label = { Text("Account Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank Name") }, modifier = Modifier.weight(1.2f))
                            OutlinedTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = { Text("Account No") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = branchCode, onValueChange = { branchCode = it }, label = { Text("Branch Code") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = bankCode, onValueChange = { bankCode = it }, label = { Text("Bank Code") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = swiftCode, onValueChange = { swiftCode = it }, label = { Text("SWIFT") }, modifier = Modifier.weight(1.2f))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = bankAddress, onValueChange = { bankAddress = it }, label = { Text("Bank Address") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes & Instructions") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Button(
                            onClick = {
                                val updated = inv.copy(
                                    invoiceNumber = invoiceNumber,
                                    invoiceDate = invoiceDate,
                                    paymentTerms = paymentTerms,
                                    freight = freight,
                                    insurance = insurance,
                                    leadTime = leadTime,
                                    notes = notes,
                                    accountName = bankAccountName,
                                    bankName = bankName,
                                    accountNumber = accountNumber,
                                    branchCode = branchCode,
                                    bankCode = bankCode,
                                    swiftCode = swiftCode,
                                    bankAddress = bankAddress,
                                    totalAmount = grandTotal,
                                    status = DocumentStatus.READY
                                )
                                viewModel.saveCommercialInvoice(updated)
                                Toast.makeText(context, "Invoice Saved & Regenerated", Toast.LENGTH_SHORT).show()
                                selectedTab = 0
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SAVE & UPDATE OFFICIAL INVOICE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingListScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val shipment by viewModel.currentShipment.collectAsState()
    val packingList by viewModel.currentPackingList.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val pallets by viewModel.currentPallets.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: A4 Official Document, 1: Edit Particulars
    var showPdfPreview by remember { mutableStateOf(false) }

    if (shipment == null || packingList == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val ship = shipment!!
    val pl = packingList!!

    var marksAndNumbers by remember { mutableStateOf(pl.marksAndCartonNumbers) }
    var sealNumber by remember { mutableStateOf(pl.sealNumber) }
    var fdaReg by remember { mutableStateOf(pl.fdaRegistration) }

    if (showPdfPreview) {
        PdfPreviewDialog(
            title = "Export Packing List",
            documentId = pl.piNumber,
            onDismiss = { showPdfPreview = false },
            onDownloadPdf = {
                val html = DocumentGenerator.generatePackingListHtml(ship, pl, products, pallets)
                DocumentGenerator.printDocument(context, html, "PackingList_${ship.id}")
                showPdfPreview = false
            },
            onShare = {
                val text = "Packing List for ${ship.id}\nTotal Cartons: ${products.sumOf { it.cartons }}\nGross Weight: ${"%.1f".format(products.sumOf { it.totalGrossWeightKg })} kg"
                DocumentGenerator.shareDocument(context, "Packing List ${ship.id}", text)
            }
        ) {
            A4PackingListDocument(shipment = ship, packingList = pl, products = products, pallets = pallets)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Export Packing List", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(pl.piNumber, fontSize = 11.sp, color = SecondaryTeal)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPdfPreview = true }) {
                        Icon(Icons.Default.Visibility, contentDescription = "PDF Preview", tint = SecondaryTeal)
                    }
                    IconButton(onClick = {
                        val html = DocumentGenerator.generatePackingListHtml(ship, pl, products, pallets)
                        DocumentGenerator.printDocument(context, html, "PackingList_${ship.id}")
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SecondaryTeal
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Document (A4)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Particulars")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPdfPreview = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Preview", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val html = DocumentGenerator.generatePackingListHtml(ship, pl, products, pallets)
                                DocumentGenerator.printDocument(context, html, "PackingList_${ship.id}")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }

                    A4PackingListDocument(
                        shipment = ship,
                        packingList = pl,
                        products = products,
                        pallets = pallets
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = "EXPLIC CEYLON (PVT) LTD", fontWeight = FontWeight.Bold, color = SecondaryTeal)
                                Text(text = "Shipper: ${pl.shipperAddress}", fontSize = 11.sp)
                                Text(text = "Consignee: ${pl.consignee}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = "Transport: ${pl.seaAirCargo} (${pl.fclLcl}) • Vessel: ${ship.vessel}", fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        OutlinedTextField(value = marksAndNumbers, onValueChange = { marksAndNumbers = it }, label = { Text("Marks & Carton Numbers") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = sealNumber, onValueChange = { sealNumber = it }, label = { Text("Container Seal No") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = fdaReg, onValueChange = { fdaReg = it }, label = { Text("FDA Registration No") }, modifier = Modifier.weight(1f))
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val updated = pl.copy(
                                    marksAndCartonNumbers = marksAndNumbers,
                                    sealNumber = sealNumber,
                                    fdaRegistration = fdaReg,
                                    totalCartons = products.sumOf { it.cartons },
                                    totalUnits = products.sumOf { it.totalUnits },
                                    totalNetWeightKg = products.sumOf { it.totalNetWeightKg },
                                    totalGrossWeightKg = products.sumOf { it.totalGrossWeightKg },
                                    totalCbm = products.sumOf { it.totalCbm },
                                    totalPallets = pallets.size,
                                    status = DocumentStatus.READY
                                )
                                viewModel.savePackingList(updated)
                                Toast.makeText(context, "Packing List Updated", Toast.LENGTH_SHORT).show()
                                selectedTab = 0
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SAVE & REGENERATE PACKING LIST", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsfScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val shipment by viewModel.currentShipment.collectAsState()
    val isf by viewModel.currentIsf.collectAsState()
    val packingList by viewModel.currentPackingList.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    var showPdfPreview by remember { mutableStateOf(false) }

    if (shipment == null || isf == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val ship = shipment!!
    val isfData = isf!!

    var cartonsText by remember { mutableStateOf(isfData.cartonsCount.toString()) }
    var importerOfRecord by remember { mutableStateOf(isfData.importerOfRecordNumber) }
    var stuffingLocation by remember { mutableStateOf(isfData.containerStuffingLocation) }
    var consolidator by remember { mutableStateOf(isfData.consolidatorNameAndAddress) }
    var completedByName by remember { mutableStateOf(isfData.completedByName) }
    var completedByTitle by remember { mutableStateOf(isfData.completedByTitle) }
    var completedByEmail by remember { mutableStateOf(isfData.completedByEmail) }

    val dbCartons = products.sumOf { it.cartons }.takeIf { it > 0 } ?: (packingList?.totalCartons ?: 0)
    val hasCartonMismatch = (cartonsText.toIntOrNull() ?: 0) != dbCartons

    if (showPdfPreview) {
        PdfPreviewDialog(
            title = "ISF 10+2 Filing",
            documentId = "CBP-ISF-${ship.reference}",
            onDismiss = { showPdfPreview = false },
            onDownloadPdf = {
                val html = DocumentGenerator.generateIsfHtml(ship, isfData)
                DocumentGenerator.printDocument(context, html, "ISF_${ship.id}")
                showPdfPreview = false
            },
            onShare = {
                val text = "ISF 10+2 Filing for ${ship.id}\nVessel: ${isfData.vessel}\nPO: ${isfData.purchaseOrderNumber}\nCartons: ${isfData.cartonsCount}"
                DocumentGenerator.shareDocument(context, "ISF ${ship.id}", text)
            }
        ) {
            A4IsfFilingDocument(shipment = ship, isf = isfData)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ISF 10+2 Filing", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("U.S. CBP Manifest", fontSize = 11.sp, color = TertiaryAmber)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPdfPreview = true }) {
                        Icon(Icons.Default.Visibility, contentDescription = "PDF Preview", tint = TertiaryAmber)
                    }
                    IconButton(onClick = {
                        val html = DocumentGenerator.generateIsfHtml(ship, isfData)
                        DocumentGenerator.printDocument(context, html, "ISF_${ship.id}")
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TertiaryAmber
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Document (A4)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Particulars")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPdfPreview = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Preview", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val html = DocumentGenerator.generateIsfHtml(ship, isfData)
                                DocumentGenerator.printDocument(context, html, "ISF_${ship.id}")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }

                    A4IsfFilingDocument(
                        shipment = ship,
                        isf = isfData
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasCartonMismatch) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StatusAmberContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = StatusAmber)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Carton Count Mismatch Flagged!",
                                            fontWeight = FontWeight.Bold,
                                            color = StatusAmber,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "ISF Cartons ($cartonsText) ≠ Database Cargo Cartons ($dbCartons)",
                                            fontSize = 11.sp
                                        )
                                    }
                                    TextButton(onClick = {
                                        cartonsText = dbCartons.toString()
                                    }) {
                                        Text("Sync to $dbCartons", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(text = "U.S. CBP 10+2 REQUIRED ELEMENTS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        DetailParamRow("1. Manufacturer/Supplier", isfData.manufacturerNameAndAddress)
                        DetailParamRow("2. Seller", isfData.sellerNameAndAddress)
                        DetailParamRow("3. Buyer/Consignee", isfData.consigneeNameAndAddress)
                        DetailParamRow("4. Ship-To Party", isfData.shipToNameAndAddress)
                        OutlinedTextField(value = stuffingLocation, onValueChange = { stuffingLocation = it }, label = { Text("5. Container Stuffing Location") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = consolidator, onValueChange = { consolidator = it }, label = { Text("6. Consolidator / Stuffer") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = importerOfRecord, onValueChange = { importerOfRecord = it }, label = { Text("7. Importer of Record Number") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        OutlinedTextField(
                            value = cartonsText,
                            onValueChange = { cartonsText = it },
                            label = { Text("Total Cartons Count on ISF") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = completedByName, onValueChange = { completedByName = it }, label = { Text("Authorized Filer Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = completedByTitle, onValueChange = { completedByTitle = it }, label = { Text("Title") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = completedByEmail, onValueChange = { completedByEmail = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val count = cartonsText.toIntOrNull() ?: 0
                                val isMatched = count == dbCartons
                                val updated = isfData.copy(
                                    cartonsCount = count,
                                    containerStuffingLocation = stuffingLocation,
                                    consolidatorNameAndAddress = consolidator,
                                    importerOfRecordNumber = importerOfRecord,
                                    completedByName = completedByName,
                                    completedByTitle = completedByTitle,
                                    completedByEmail = completedByEmail,
                                    status = if (isMatched) DocumentStatus.READY else DocumentStatus.VALIDATION_ERROR
                                )
                                viewModel.saveIsfFiling(updated)
                                Toast.makeText(context, "ISF Filing Saved", Toast.LENGTH_SHORT).show()
                                selectedTab = 0
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TertiaryAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SAVE & UPDATE ISF 10+2 RECORD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val shipment by viewModel.currentShipment.collectAsState()
    val bl by viewModel.currentBl.collectAsState()
    val packingList by viewModel.currentPackingList.collectAsState()
    val products by viewModel.currentProducts.collectAsState()
    val pallets by viewModel.currentPallets.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    var showPdfPreview by remember { mutableStateOf(false) }

    if (shipment == null || bl == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val ship = shipment!!
    val blData = bl!!

    var shipper by remember { mutableStateOf(blData.shipper) }
    var consignee by remember { mutableStateOf(blData.consignee) }
    var notifyParty by remember { mutableStateOf(blData.firstNotifyParty) }
    var marks by remember { mutableStateOf(blData.marksAndNumbers) }
    var packages by remember { mutableStateOf(blData.kindOfPackages) }
    var description by remember { mutableStateOf(blData.descriptionOfGoods) }
    var grossWeightText by remember { mutableStateOf(blData.grossWeightKg.toString()) }
    var cbmText by remember { mutableStateOf(blData.measurementCbm.toString()) }
    var freightTerms by remember { mutableStateOf(blData.freightTerms) }
    var originals by remember { mutableStateOf(blData.numberOfOriginals) }
    var remarks by remember { mutableStateOf(blData.remarks) }

    val dbGrossWeight = products.sumOf { it.totalGrossWeightKg }.takeIf { it > 0.0 } ?: (packingList?.totalGrossWeightKg ?: 0.0)
    val isGrossWeightMissing = (grossWeightText.toDoubleOrNull() ?: 0.0) <= 0.0

    if (showPdfPreview) {
        PdfPreviewDialog(
            title = "B/L Instructions",
            documentId = "BLI-${ship.reference}",
            onDismiss = { showPdfPreview = false },
            onDownloadPdf = {
                val html = DocumentGenerator.generateBlHtml(ship, blData)
                DocumentGenerator.printDocument(context, html, "BL_${ship.id}")
                showPdfPreview = false
            },
            onShare = {
                val text = "B/L Instructions for ${ship.id}\nVessel: ${blData.vessel}\nPackages: ${blData.kindOfPackages}\nGross Weight: ${blData.grossWeightKg} kg"
                DocumentGenerator.shareDocument(context, "BL Instructions ${ship.id}", text)
            }
        ) {
            A4BlInstructionsDocument(shipment = ship, bl = blData)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("B/L Shipping Instructions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(ship.vessel, fontSize = 11.sp, color = PrimaryBlue)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPdfPreview = true }) {
                        Icon(Icons.Default.Visibility, contentDescription = "PDF Preview", tint = PrimaryBlue)
                    }
                    IconButton(onClick = {
                        val html = DocumentGenerator.generateBlHtml(ship, blData)
                        DocumentGenerator.printDocument(context, html, "BL_${ship.id}")
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print PDF")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Document (A4)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Particulars")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPdfPreview = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Preview", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val html = DocumentGenerator.generateBlHtml(ship, blData)
                                DocumentGenerator.printDocument(context, html, "BL_${ship.id}")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }

                    A4BlInstructionsDocument(
                        shipment = ship,
                        bl = blData
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isGrossWeightMissing) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StatusRedContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Gross Weight Missing from B/L!",
                                            fontWeight = FontWeight.Bold,
                                            color = StatusRed,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Carrier requires gross weight for SOLAS VGM declaration.",
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (dbGrossWeight > 0.0) {
                                        TextButton(onClick = {
                                            grossWeightText = dbGrossWeight.toString()
                                        }) {
                                            Text("Set $dbGrossWeight kg", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(text = "PARTIES ON BILL OF LADING", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        OutlinedTextField(value = shipper, onValueChange = { shipper = it }, label = { Text("Shipper Name & Address") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = consignee, onValueChange = { consignee = it }, label = { Text("Consignee") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = notifyParty, onValueChange = { notifyParty = it }, label = { Text("1st Notify Party") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    }

                    item {
                        Text(text = "CARGO PARTICULARS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        OutlinedTextField(value = marks, onValueChange = { marks = it }, label = { Text("Marks & Numbers") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = packages, onValueChange = { packages = it }, label = { Text("Kind of Packages") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description of Goods") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = grossWeightText, onValueChange = { grossWeightText = it }, label = { Text("Gross Weight (KG)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = cbmText, onValueChange = { cbmText = it }, label = { Text("Measurement (CBM)") }, modifier = Modifier.weight(1f))
                        }
                    }

                    item {
                        Text(text = "TERMS & INSTRUCTIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = freightTerms, onValueChange = { freightTerms = it }, label = { Text("Freight Terms") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = originals, onValueChange = { originals = it }, label = { Text("Originals") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Carrier Special Remarks") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Button(
                            onClick = {
                                val gw = grossWeightText.toDoubleOrNull() ?: 0.0
                                val cbm = cbmText.toDoubleOrNull() ?: 0.0
                                val updated = blData.copy(
                                    shipper = shipper,
                                    consignee = consignee,
                                    firstNotifyParty = notifyParty,
                                    marksAndNumbers = marks,
                                    kindOfPackages = packages,
                                    descriptionOfGoods = description,
                                    grossWeightKg = gw,
                                    measurementCbm = cbm,
                                    freightTerms = freightTerms,
                                    numberOfOriginals = originals,
                                    remarks = remarks,
                                    status = if (gw > 0.0) DocumentStatus.READY else DocumentStatus.VALIDATION_ERROR
                                )
                                viewModel.saveBlInstructions(updated)
                                Toast.makeText(context, "B/L Instructions Saved", Toast.LENGTH_SHORT).show()
                                selectedTab = 0
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SAVE & UPDATE B/L INSTRUCTIONS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
