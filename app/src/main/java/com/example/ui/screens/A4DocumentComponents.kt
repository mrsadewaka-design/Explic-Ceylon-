package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.engine.DocumentGenerator
import com.example.ui.theme.*

/**
 * Photorealistic A4 paper sheet container for rendering official export documents.
 */
@Composable
fun A4DocumentSheet(
    documentTitle: String,
    documentControlNumber: String,
    pageNumber: String = "Page 1 of 1",
    themeColor: Color = PrimaryBlue,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(2.dp))
            .border(width = 1.dp, color = Color(0xFFCBD5E1), shape = RoundedCornerShape(2.dp)),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            // Corporate Letterhead & Document Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "EXPLIC CEYLON (PRIVATE) LIMITED",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = themeColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350\nTel: +94 11 273 8920 • Email: exports@explicceylon.com\nTax ID: LK102948293 • FDA Reg: 19482019482",
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        color = Color(0xFF475569)
                    )
                }

                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = themeColor, shape = RoundedCornerShape(3.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = documentTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Doc No: $documentControlNumber",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = pageNumber,
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            HorizontalDivider(thickness = 2.dp, color = themeColor)
            Spacer(modifier = Modifier.height(10.dp))

            // Main Document Body
            content()

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(6.dp))

            // Running Official Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: $documentControlNumber • ISO 9001:2015 Verified Manifest",
                    fontSize = 8.5.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "$pageNumber • Explic Export Pro Security Audit",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

/**
 * Official Commercial Invoice A4 layout
 */
@Composable
fun A4CommercialInvoiceDocument(
    shipment: Shipment,
    invoice: CommercialInvoice,
    products: List<ShipmentProduct>,
    modifier: Modifier = Modifier
) {
    // Dynamically calculate all totals from the database
    val subtotal = products.sumOf { it.totalAmountUsd }
    val grandTotal = subtotal + invoice.freight + invoice.insurance
    val totalUnits = products.sumOf { it.totalUnits }
    val totalCartons = products.sumOf { it.cartons }
    val docNumber = "DOC-CI-${shipment.reference.ifEmpty { shipment.id }}"

    A4DocumentSheet(
        documentTitle = "COMMERCIAL INVOICE",
        documentControlNumber = docNumber,
        themeColor = PrimaryBlue,
        modifier = modifier
    ) {
        // Document Metadata Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .border(0.8.dp, Color(0xFFCBD5E1))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Invoice No: ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A))
            Text("Date: ${invoice.invoiceDate}", fontSize = 10.sp, color = Color(0xFF334155))
            Text("PO Ref: ${shipment.poNumber}", fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = PrimaryBlue)
            Text("Currency: ${invoice.currency}", fontSize = 10.sp, color = Color(0xFF334155))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Exporter & Consignee 2-column Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Shipper Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("EXPORTER / SHIPPER:", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(2.dp))
                Text(invoice.exporterName, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A))
                Text(invoice.exporterAddress, fontSize = 9.sp, color = Color(0xFF475569))
                Text("Country of Origin: Sri Lanka", fontSize = 8.5.sp, color = Color(0xFF64748B))
            }

            // Buyer / Consignee Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("CONSIGNEE / BUYER:", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(2.dp))
                Text(invoice.consigneeName, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A))
                Text(invoice.consigneeAddress, fontSize = 9.sp, color = Color(0xFF475569))
                Text("Destination: ${shipment.placeOfDelivery}", fontSize = 8.5.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logistics Particulars Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .border(0.8.dp, Color(0xFFCBD5E1))
                .padding(8.dp)
        ) {
            Text("SHIPPING & LOGISTICS PARTICULARS:", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mode: ${shipment.shippingMode.label}", fontSize = 9.sp)
                Text("Port of Loading: ${shipment.portOfLoading}", fontSize = 9.sp)
                Text("Port of Discharge: ${shipment.portOfDischarge}", fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vessel: ${shipment.vessel} / ${shipment.voyage}", fontSize = 9.sp)
                Text("Container: ${shipment.containerNumber}", fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
                Text("Terms: ${invoice.paymentTerms}", fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
            Text("HS Code", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Text("Description of Goods", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.6f))
            Text("Origin", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
            Text("Units", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
            Text("Price", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.0f))
            Text("Total (${invoice.currency})", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
        }

        // Table Rows
        products.forEachIndexed { idx, p ->
            val bg = if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .border(width = 0.5.dp, color = Color(0xFFE2E8F0))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${idx + 1}", fontSize = 8.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.4f))
                Text(p.hsCode, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.3f))
                Column(modifier = Modifier.weight(2.6f)) {
                    Text(p.productName, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF0F172A))
                    Text(p.description, fontSize = 8.sp, color = Color(0xFF64748B), maxLines = 1)
                }
                Text(p.countryOfOrigin, fontSize = 8.5.sp, modifier = Modifier.weight(0.8f))
                Text("${p.totalUnits}", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                Text("$ ${"%.2f".format(p.unitPriceUsd)}", fontSize = 8.5.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.0f))
                Text("$ ${"%.2f".format(p.totalAmountUsd)}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Totals Box (Right Aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Cargo:", fontSize = 9.sp, color = Color(0xFF475569))
                    Text("$totalCartons Ctns • $totalUnits Units", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFCBD5E1))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = 9.sp)
                    Text("$ ${"%.2f".format(subtotal)}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ocean Freight:", fontSize = 9.sp)
                    Text("$ ${"%.2f".format(invoice.freight)}", fontSize = 9.sp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marine Insurance:", fontSize = 9.sp)
                    Text("$ ${"%.2f".format(invoice.insurance)}", fontSize = 9.sp)
                }
                HorizontalDivider(thickness = 0.8.dp, color = Color(0xFF0F172A))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GRAND TOTAL (${invoice.currency}):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                    Text("$ ${"%.2f".format(grandTotal)}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Banking Particulars Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFF6FF))
                .border(0.8.dp, Color(0xFFBFDBFE))
                .padding(8.dp)
        ) {
            Text("WIRE REMITTANCE BANK DETAILS:", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF1E3A8A))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Beneficiary: ${invoice.accountName} | Bank: ${invoice.bankName}\nAccount No: ${invoice.accountNumber} | Branch: ${invoice.branchCode} | Bank Code: ${invoice.bankCode} | SWIFT: ${invoice.swiftCode}\nAddress: ${invoice.bankAddress}",
                fontSize = 8.sp,
                lineHeight = 11.sp,
                color = Color(0xFF1E293B)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Declaration & Signatures
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Official Corporate Stamp
            Box(
                modifier = Modifier
                    .rotate(-4f)
                    .border(1.5.dp, PrimaryBlue, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EXPLIC CEYLON", fontWeight = FontWeight.ExtraBold, fontSize = 8.sp, color = PrimaryBlue)
                    Text("★ EXPORT CERTIFIED ★", fontWeight = FontWeight.Bold, fontSize = 7.sp, color = PrimaryBlue)
                    Text("COLOMBO, SRI LANKA", fontSize = 7.sp, color = PrimaryBlue)
                }
            }

            // Authorized Signature Line
            Column(
                modifier = Modifier.width(180.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "For EXPLIC CEYLON (PVT) LTD",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Authorized Signatory / Export Documentation Officer",
                    fontSize = 7.5.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Official Packing List A4 layout
 */
@Composable
fun A4PackingListDocument(
    shipment: Shipment,
    packingList: PackingList,
    products: List<ShipmentProduct>,
    pallets: List<Pallet>,
    modifier: Modifier = Modifier
) {
    // Calculated totals from database
    val totalCartons = products.sumOf { it.cartons }
    val totalUnits = products.sumOf { it.totalUnits }
    val totalNetWeightKg = products.sumOf { it.totalNetWeightKg }
    val totalGrossWeightKg = products.sumOf { it.totalGrossWeightKg }
    val totalCbm = products.sumOf { it.totalCbm }
    val totalPalletsCount = if (pallets.isNotEmpty()) pallets.size else packingList.totalPallets
    val docNumber = "DOC-PL-${shipment.reference.ifEmpty { shipment.id }}"

    A4DocumentSheet(
        documentTitle = "EXPORT PACKING LIST",
        documentControlNumber = docNumber,
        themeColor = SecondaryTeal,
        modifier = modifier
    ) {
        // Document Metadata Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDFA))
                .border(0.8.dp, Color(0xFF99F6E4))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PI / Order No: ${packingList.piNumber}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F172A))
            Text("Date: ${packingList.date}", fontSize = 10.sp, color = Color(0xFF334155))
            Text("FDA Reg: ${packingList.fdaRegistration}", fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = SecondaryTeal)
            Text("FCL / LCL: ${packingList.fclLcl}", fontSize = 10.sp, color = Color(0xFF334155))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Parties Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("SHIPPER / EXPORTER:", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = SecondaryTeal)
                Text(packingList.shipper, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Text(packingList.shipperAddress, fontSize = 8.5.sp, color = Color(0xFF475569))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("CONSIGNEE:", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = SecondaryTeal)
                Text(packingList.consignee, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Text(packingList.consigneeAddress, fontSize = 8.5.sp, color = Color(0xFF475569))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transport & Container Data Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .border(0.8.dp, Color(0xFFCBD5E1))
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Port of Loading: ${packingList.portOfLoading}", fontSize = 9.sp)
                Text("Port of Discharge: ${packingList.portOfDischarge}", fontSize = 9.sp)
                Text("Transport: ${packingList.seaAirCargo}", fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Container: ${packingList.containerNumber}", fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = Color(0xFF0F172A))
                Text("Seal No: ${packingList.sealNumber}", fontSize = 9.sp)
                Text("Vessel: ${shipment.vessel} / ${shipment.voyage}", fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 4.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f))
            Text("Description", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.0f))
            Text("HS Tariff", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
            Text("Ctns", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
            Text("U/Ctn", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
            Text("Units", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
            Text("Net (KG)", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
            Text("Ctn Gr", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
            Text("Gross (KG)", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
            Text("CBM", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
        }

        // Table Rows
        products.forEachIndexed { idx, p ->
            val bg = if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .border(width = 0.5.dp, color = Color(0xFFE2E8F0))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${idx + 1}", fontSize = 8.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.3f))
                Text(p.productName, fontWeight = FontWeight.SemiBold, fontSize = 8.5.sp, modifier = Modifier.weight(2.0f))
                Text(p.hsCode, fontSize = 8.sp, modifier = Modifier.weight(1.0f))
                Text("${p.cartons}", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
                Text("${p.unitsPerCarton}", fontSize = 8.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
                Text("${p.totalUnits}", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
                Text("${"%.1f".format(p.totalNetWeightKg)}", fontSize = 8.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                Text("${"%.2f".format(p.cartonGrossWeightKg)}", fontSize = 8.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
                Text("${"%.1f".format(p.totalGrossWeightKg)}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = SecondaryTeal, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                Text("${"%.3f".format(p.totalCbm)}", fontSize = 8.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Calculated Grand Totals Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(3.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = BorderStroke(1.dp, Color(0xFF16A34A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalCartons", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF166534))
                    Text("TOTAL CARTONS", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalUnits", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF166534))
                    Text("TOTAL UNITS", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${"%.1f".format(totalNetWeightKg)} KG", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF166534))
                    Text("NET WEIGHT", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${"%.1f".format(totalGrossWeightKg)} KG", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF166534))
                    Text("GROSS WT (SOLAS)", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${"%.3f".format(totalCbm)}", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF166534))
                    Text("TOTAL CBM", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalPalletsCount", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color(0xFF166534))
                    Text("TOTAL PALLETS", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                }
            }
        }

        // Pallet Specification Summary
        if (pallets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("PALLET LOADING ALLOCATION (HEAT-TREATED ISPM-15):", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(4.dp))
            pallets.forEach { pal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .border(0.5.dp, Color(0xFFE2E8F0))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${pal.palletNumber}: ${pal.palletType}", fontWeight = FontWeight.SemiBold, fontSize = 8.sp)
                    Text(pal.productsSummary, fontSize = 8.sp, color = Color(0xFF475569))
                    Text("${pal.cartons} ctns • ${"%.1f".format(pal.weightKg)} kg • ${"%.3f".format(pal.cbm)} CBM", fontWeight = FontWeight.Bold, fontSize = 8.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Signatures
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .rotate(-4f)
                    .border(1.5.dp, SecondaryTeal, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EXPLIC WAREHOUSE", fontWeight = FontWeight.ExtraBold, fontSize = 8.sp, color = SecondaryTeal)
                    Text("★ TALLY VERIFIED ★", fontWeight = FontWeight.Bold, fontSize = 7.sp, color = SecondaryTeal)
                }
            }

            Column(
                modifier = Modifier.width(180.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "For EXPLIC CEYLON (PVT) LTD",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Warehouse Lead & Packing Supervisor",
                    fontSize = 7.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Official ISF 10+2 Filing A4 layout
 */
@Composable
fun A4IsfFilingDocument(
    shipment: Shipment,
    isf: IsfFiling,
    modifier: Modifier = Modifier
) {
    val docNumber = "CBP-ISF-10+2-${shipment.reference.ifEmpty { shipment.id }}"

    A4DocumentSheet(
        documentTitle = "ISF 10+2 TRANSMISSION",
        documentControlNumber = docNumber,
        themeColor = TertiaryAmber,
        modifier = modifier
    ) {
        // CBP Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(3.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            border = BorderStroke(1.dp, Color(0xFFF59E0B))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "U.S. CUSTOMS & BORDER PROTECTION (CBP) • 19 CFR PART 149",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "Importer Security Filing and Additional Carrier Requirements • 24 Hours Prior to Lading",
                    fontSize = 8.sp,
                    color = Color(0xFF78350F)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 10+2 Mandated Elements Grid
        val elements = listOf(
            "1. Manufacturer / Supplier" to isf.manufacturerNameAndAddress,
            "2. Seller" to isf.sellerNameAndAddress,
            "3. Buyer / Consignee" to isf.consigneeNameAndAddress,
            "4. Ship-To Party" to isf.shipToNameAndAddress,
            "5. Container Stuffing Location" to isf.containerStuffingLocation,
            "6. Consolidator / Stuffer" to isf.consolidatorNameAndAddress,
            "7. Importer of Record Number" to isf.importerOfRecordNumber,
            "8. Consignee Number(s)" to if (isf.consigneeNumber.isNotEmpty()) isf.consigneeNumber else isf.importerOfRecordNumber,
            "9. Country of Origin" to isf.countryOfOrigin,
            "10. Commodity HTSUS Numbers" to isf.productCodeHtsCode
        )

        elements.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(0.5.dp, Color(0xFFCBD5E1))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = value,
                    fontSize = 8.5.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Carrier Linking Particulars
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .border(0.8.dp, Color(0xFFCBD5E1))
                .padding(8.dp)
        ) {
            Text("CARRIER LINKING & MANIFEST IDENTIFIERS:", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = TertiaryAmber)
            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vessel: ${isf.vessel} / ${isf.voyage}", fontSize = 8.5.sp)
                Text("ETD: ${isf.etd}", fontSize = 8.5.sp)
                Text("Declared Cartons: ${isf.cartonsCount} Ctns", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = TertiaryAmber)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Master B/L: ${isf.masterBlNumber}", fontWeight = FontWeight.SemiBold, fontSize = 8.5.sp)
                Text("AMS B/L: ${isf.amsBlNumber}", fontSize = 8.5.sp)
                Text("Container: ${isf.containerNumber}", fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filer Declaration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .rotate(-4f)
                    .border(1.5.dp, TertiaryAmber, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("U.S. CBP ISF", fontWeight = FontWeight.ExtraBold, fontSize = 8.sp, color = TertiaryAmber)
                    Text("★ 10+2 VERIFIED ★", fontWeight = FontWeight.Bold, fontSize = 7.sp, color = TertiaryAmber)
                }
            }

            Column(
                modifier = Modifier.width(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Electronic Signature on File:",
                    fontSize = 7.5.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "${isf.completedByName} (${isf.completedByTitle})",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${isf.completedByCompany} • ${isf.completedDate}",
                    fontSize = 7.5.sp,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFF334155))
            }
        }
    }
}

/**
 * Official Ocean Bill of Lading Instructions A4 layout
 */
@Composable
fun A4BlInstructionsDocument(
    shipment: Shipment,
    bl: BlInstructions,
    modifier: Modifier = Modifier
) {
    val docNumber = "BLI-CMA-${shipment.reference.ifEmpty { shipment.id }}"

    A4DocumentSheet(
        documentTitle = "B/L SHIPPING INSTRUCTIONS",
        documentControlNumber = docNumber,
        themeColor = Color(0xFF1D4ED8),
        modifier = modifier
    ) {
        // Ocean Carrier Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFF6FF))
                .border(0.8.dp, Color(0xFFBFDBFE))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Carrier: CMA CGM (Sri Lanka)", fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = Color(0xFF1D4ED8))
            Text("Booking Ref: ${shipment.id}", fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
            Text("Freight: ${bl.freightTerms}", fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = Color(0xFF15803D))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Shipper & Consignee 2-col
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("SHIPPER / EXPORTER:", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF1D4ED8))
                Text(bl.shipper, fontSize = 8.5.sp, lineHeight = 11.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("CONSIGNEE:", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF1D4ED8))
                Text(bl.consignee, fontSize = 8.5.sp, lineHeight = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Notify Parties 2-col
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("1ST NOTIFY PARTY:", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF334155))
                Text(bl.firstNotifyParty, fontSize = 8.5.sp, lineHeight = 11.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF8FAFC))
                    .border(0.8.dp, Color(0xFFCBD5E1))
                    .padding(8.dp)
            ) {
                Text("2ND NOTIFY PARTY:", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF334155))
                Text(bl.secondNotifyParty, fontSize = 8.5.sp, lineHeight = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Routing Particulars
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .border(0.8.dp, Color(0xFFCBD5E1))
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pre-Carriage: ${bl.placeOfReceipt}", fontSize = 8.5.sp)
                Text("Port of Loading: ${bl.portOfLoading}", fontSize = 8.5.sp)
                Text("Port of Discharge: ${bl.portOfDischarge}", fontSize = 8.5.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vessel: ${bl.vessel} / ${bl.voyageNumber}", fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                Text("Place of Delivery: ${bl.placeOfDelivery}", fontSize = 8.5.sp)
                Text("Originals: ${bl.numberOfOriginals}", fontSize = 8.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cargo Table
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Text("Marks & Numbers", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Packages Count", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
            Text("Description of Goods", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
            Text("Gross Weight", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
            Text("Measurement", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(0.5.dp, Color(0xFFCBD5E1))
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(bl.marksAndNumbers, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                Text("Container: ${bl.containerNumber}", fontSize = 8.sp, color = Color(0xFF64748B))
            }
            Text(bl.kindOfPackages, fontWeight = FontWeight.SemiBold, fontSize = 8.5.sp, modifier = Modifier.weight(0.9f))
            Column(modifier = Modifier.weight(1.5f)) {
                Text(bl.descriptionOfGoods, fontSize = 8.sp, lineHeight = 10.sp)
                Text("HTS: ${bl.hsNumber}", fontSize = 7.5.sp, color = Color(0xFF1D4ED8))
            }
            Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(bl.grossWeightKg)} KG", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color(0xFF1D4ED8))
                Text("SOLAS VGM", fontSize = 7.sp, color = Color(0xFF15803D))
            }
            Text("${"%.3f".format(bl.measurementCbm)} CBM", fontWeight = FontWeight.Bold, fontSize = 9.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Special Remarks
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .border(0.8.dp, Color(0xFFCBD5E1))
                .padding(8.dp)
        ) {
            Text("CARRIER SPECIAL INSTRUCTIONS & CLAUSES:", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF334155))
            Text(bl.remarks, fontSize = 8.sp, color = Color(0xFF475569))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Signatures
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .rotate(-4f)
                    .border(1.5.dp, Color(0xFF1D4ED8), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CARRIER B/L DRAFT", fontWeight = FontWeight.ExtraBold, fontSize = 8.sp, color = Color(0xFF1D4ED8))
                    Text("★ INSTRUCTIONS ★", fontWeight = FontWeight.Bold, fontSize = 7.sp, color = Color(0xFF1D4ED8))
                }
            }

            Column(
                modifier = Modifier.width(180.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "For EXPLIC CEYLON (PVT) LTD",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Authorized Shipping Coordinator",
                    fontSize = 7.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Interactive full-screen / dialog PDF preview before downloading
 */
@Composable
fun PdfPreviewDialog(
    title: String,
    documentId: String,
    onDismiss: () -> Unit,
    onDownloadPdf: () -> Unit,
    onShare: () -> Unit,
    documentContent: @Composable () -> Unit
) {
    var zoomScale by remember { mutableStateOf(1.0f) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = Navy900,
                    contentColor = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Preview", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "PDF DOCUMENT PREVIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "$title ($documentId)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }

                        // Zoom Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (zoomScale > 0.8f) zoomScale -= 0.15f }) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                            }
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { if (zoomScale < 1.6f) zoomScale += 0.15f }) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onShare,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Doc")
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Generating certified PDF for $title...", Toast.LENGTH_SHORT).show()
                                onDownloadPdf()
                            },
                            modifier = Modifier.weight(1.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFE2E8F0))
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(max = 680.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Document Page indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Navy800)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "A4 Page 1 of 1 • 210 x 297 mm",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // The Rendered A4 Document Page with horizontal scrolling support for small phone screens
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        documentContent()
                    }
                }
            }
        }
    }
}
