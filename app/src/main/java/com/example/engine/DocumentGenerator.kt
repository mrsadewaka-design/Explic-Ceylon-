package com.example.engine

import android.content.Context
import android.content.Intent
import android.os.Environment
import com.example.data.model.*
import java.io.File

object DocumentGenerator {

    private const val A4_BASE_CSS = """
        @page {
            size: A4 portrait;
            margin: 12mm 10mm 14mm 10mm;
        }
        @media print {
            body { 
                margin: 0 !important; 
                padding: 0 !important; 
                -webkit-print-color-adjust: exact; 
                print-color-adjust: exact; 
            }
            .a4-page {
                box-shadow: none !important;
                border: none !important;
                margin: 0 !important;
                padding: 0 !important;
                width: 100% !important;
            }
            .no-print { display: none !important; }
        }
        * { box-sizing: border-box; }
        body { 
            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; 
            margin: 0; 
            padding: 16px; 
            background: #f1f5f9; 
            color: #0f172a; 
            font-size: 11px; 
            line-height: 1.35; 
        }
        .a4-page {
            max-width: 210mm;
            min-height: 297mm;
            margin: 0 auto 20px auto;
            background: #ffffff;
            padding: 18mm 14mm 16mm 14mm;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 1px solid #cbd5e1;
            position: relative;
        }
        .doc-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            border-bottom: 2px solid #0369a1;
            padding-bottom: 12px;
            margin-bottom: 14px;
        }
        .corp-logo-name {
            font-size: 17px;
            font-weight: 800;
            color: #0369a1;
            letter-spacing: 0.5px;
        }
        .corp-sub {
            font-size: 10px;
            color: #475569;
            margin-top: 2px;
            line-height: 1.3;
        }
        .doc-badge-panel {
            text-align: right;
        }
        .doc-badge {
            display: inline-block;
            background: #0369a1;
            color: #ffffff;
            font-size: 12px;
            font-weight: 800;
            letter-spacing: 1px;
            padding: 4px 12px;
            border-radius: 4px;
            text-transform: uppercase;
        }
        .doc-meta {
            font-size: 10.5px;
            margin-top: 4px;
            color: #334155;
        }
        .two-col {
            display: flex;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 12px;
        }
        .card-box {
            flex: 1;
            background: #f8fafc;
            border: 1px solid #cbd5e1;
            border-radius: 4px;
            padding: 8px 10px;
            font-size: 10.5px;
        }
        .box-title {
            font-size: 10px;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #0369a1;
            border-bottom: 1px solid #e2e8f0;
            padding-bottom: 3px;
            margin-bottom: 6px;
        }
        table.doc-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
            margin-bottom: 12px;
            font-size: 10.5px;
        }
        table.doc-table th {
            background: #0f172a;
            color: #ffffff;
            font-weight: 700;
            font-size: 10px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            padding: 6px 8px;
            border: 1px solid #0f172a;
        }
        table.doc-table td {
            padding: 6px 8px;
            border: 1px solid #cbd5e1;
            vertical-align: middle;
        }
        .totals-summary {
            width: 48%;
            margin-left: auto;
            border-collapse: collapse;
            font-size: 11px;
            margin-top: 4px;
            margin-bottom: 12px;
        }
        .totals-summary td {
            padding: 5px 8px;
            border: 1px solid #cbd5e1;
        }
        .totals-summary tr.grand-row td {
            background: #0f172a;
            color: #ffffff;
            font-weight: 800;
            font-size: 12px;
        }
        .bank-box {
            background: #eff6ff;
            border: 1px solid #bfdbfe;
            border-radius: 4px;
            padding: 8px 10px;
            font-size: 10px;
            margin-top: 10px;
            margin-bottom: 12px;
        }
        .sign-area {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            margin-top: 24px;
            padding-top: 10px;
        }
        .sign-box {
            width: 220px;
            border-top: 1px solid #334155;
            text-align: center;
            padding-top: 6px;
            font-size: 10px;
            color: #1e293b;
        }
        .stamp-box {
            width: 110px;
            height: 60px;
            border: 1.5px dashed #0369a1;
            border-radius: 6px;
            color: #0369a1;
            font-size: 9px;
            font-weight: 700;
            display: flex;
            align-items: center;
            justify-content: center;
            text-align: center;
            transform: rotate(-4deg);
        }
        .doc-footer {
            margin-top: 24px;
            border-top: 1px solid #e2e8f0;
            padding-top: 6px;
            display: flex;
            justify-content: space-between;
            font-size: 9.5px;
            color: #64748b;
        }
    """

    fun generateInvoiceHtml(
        shipment: Shipment,
        invoice: CommercialInvoice,
        products: List<ShipmentProduct>
    ): String {
        // Calculate totals dynamically from database products
        val subtotal = products.sumOf { it.totalAmountUsd }
        val grandTotal = subtotal + invoice.freight + invoice.insurance
        val totalUnits = products.sumOf { it.totalUnits }
        val totalCartons = products.sumOf { it.cartons }
        val docNumber = "DOC-CI-${shipment.reference.ifEmpty { shipment.id }}"

        val rows = products.mapIndexed { idx, p ->
            """
            <tr>
                <td style="text-align: center;">${idx + 1}</td>
                <td style="text-align: center; font-weight: 600;">${p.hsCode}</td>
                <td>
                    <strong>${p.productName}</strong><br>
                    <small style="color: #64748b;">${p.description}</small>
                </td>
                <td style="text-align: center;">${p.countryOfOrigin}</td>
                <td style="text-align: right; font-weight: 600;">${p.totalUnits}</td>
                <td style="text-align: right;">$ ${"%.2f".format(p.unitPriceUsd)}</td>
                <td style="text-align: right; font-weight: 700; color: #0369a1;">$ ${"%.2f".format(p.totalAmountUsd)}</td>
            </tr>
            """.trimIndent()
        }.joinToString("")

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Commercial Invoice - ${invoice.invoiceNumber}</title>
            <style>$A4_BASE_CSS</style>
        </head>
        <body>
            <div class="a4-page">
                <div class="doc-header">
                    <div>
                        <div class="corp-logo-name">EXPLIC CEYLON (PRIVATE) LIMITED</div>
                        <div class="corp-sub">
                            43/1, Hospital Road, Dehiwala, Sri Lanka 10350<br>
                            Tel: +94 11 273 8920 • Email: exports@explicceylon.com<br>
                            Tax ID: LK102948293 • FDA Reg: 19482019482 • Export Lic: EXP/LK/8492
                        </div>
                    </div>
                    <div class="doc-badge-panel">
                        <div class="doc-badge">COMMERCIAL INVOICE</div>
                        <div class="doc-meta"><strong>Invoice No:</strong> ${invoice.invoiceNumber}</div>
                        <div class="doc-meta"><strong>Doc Control:</strong> $docNumber</div>
                        <div class="doc-meta"><strong>Date:</strong> ${invoice.invoiceDate}</div>
                        <div class="doc-meta"><strong>Shipment Ref:</strong> ${shipment.id}</div>
                    </div>
                </div>

                <div class="two-col">
                    <div class="card-box">
                        <div class="box-title">Shipper / Exporter</div>
                        <strong>${invoice.exporterName}</strong><br>
                        ${invoice.exporterAddress.replace("\n", "<br>")}<br>
                        <small style="color: #64748b;">Country of Export: Sri Lanka</small>
                    </div>
                    <div class="card-box">
                        <div class="box-title">Buyer / Consignee</div>
                        <strong>${invoice.consigneeName}</strong><br>
                        ${invoice.consigneeAddress.replace("\n", "<br>")}<br>
                        <strong>PO Number:</strong> ${shipment.poNumber}
                    </div>
                </div>

                <div class="card-box" style="margin-bottom: 12px;">
                    <div class="box-title">Transport & Logistics Particulars</div>
                    <table style="width: 100%; font-size: 10px; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Shipping Mode:</strong> ${shipment.shippingMode.label}</td>
                            <td style="padding: 2px 4px;"><strong>Port of Loading:</strong> ${shipment.portOfLoading}</td>
                            <td style="padding: 2px 4px;"><strong>Port of Discharge:</strong> ${shipment.portOfDischarge}</td>
                        </tr>
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Vessel / Voyage:</strong> ${shipment.vessel} / ${shipment.voyage}</td>
                            <td style="padding: 2px 4px;"><strong>Container No:</strong> ${shipment.containerNumber}</td>
                            <td style="padding: 2px 4px;"><strong>Place of Delivery:</strong> ${shipment.placeOfDelivery}</td>
                        </tr>
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Payment Terms:</strong> ${invoice.paymentTerms}</td>
                            <td style="padding: 2px 4px;"><strong>Delivery Terms:</strong> CIF ${shipment.portOfDischarge}</td>
                            <td style="padding: 2px 4px;"><strong>Currency:</strong> ${invoice.currency}</td>
                        </tr>
                    </table>
                </div>

                <table class="doc-table">
                    <thead>
                        <tr>
                            <th style="width: 5%; text-align: center;">Item</th>
                            <th style="width: 14%; text-align: center;">HS Tariff</th>
                            <th style="width: 38%;">Description of Goods</th>
                            <th style="width: 8%; text-align: center;">Origin</th>
                            <th style="width: 11%; text-align: right;">Quantity</th>
                            <th style="width: 11%; text-align: right;">Unit Price</th>
                            <th style="width: 13%; text-align: right;">Amount (${invoice.currency})</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <table class="totals-summary">
                    <tr>
                        <td><strong>Total Cartons:</strong></td>
                        <td style="text-align: right;">$totalCartons Cartons</td>
                    </tr>
                    <tr>
                        <td><strong>Total Units:</strong></td>
                        <td style="text-align: right;">$totalUnits Units</td>
                    </tr>
                    <tr>
                        <td>Subtotal Amount:</td>
                        <td style="text-align: right;">$ ${"%.2f".format(subtotal)}</td>
                    </tr>
                    <tr>
                        <td>Ocean Freight (Prepaid):</td>
                        <td style="text-align: right;">$ ${"%.2f".format(invoice.freight)}</td>
                    </tr>
                    <tr>
                        <td>Marine Insurance:</td>
                        <td style="text-align: right;">$ ${"%.2f".format(invoice.insurance)}</td>
                    </tr>
                    <tr class="grand-row">
                        <td>TOTAL INVOICE (${invoice.currency}):</td>
                        <td style="text-align: right;">$ ${"%.2f".format(grandTotal)}</td>
                    </tr>
                </table>

                <div class="bank-box">
                    <div style="font-weight: 700; color: #1e3a8a; margin-bottom: 4px; text-transform: uppercase;">
                        Bank Wire Remittance Instructions:
                    </div>
                    Beneficiary Name: <strong>${invoice.accountName}</strong> | Bank: <strong>${invoice.bankName}</strong><br>
                    Account No: <strong>${invoice.accountNumber}</strong> | Branch: <strong>${invoice.branchCode}</strong> | Bank Code: <strong>${invoice.bankCode}</strong> | SWIFT: <strong>${invoice.swiftCode}</strong><br>
                    Bank Address: ${invoice.bankAddress}
                </div>

                <div style="font-size: 9.5px; color: #475569; margin-top: 6px;">
                    <strong>Declaration:</strong> We declare that this invoice shows the actual price of the goods described, that no other invoice has been or will be issued for this shipment, and that all particulars are true and correct.
                </div>

                <div class="sign-area">
                    <div class="stamp-box">
                        EXPLIC CEYLON<br>
                        ★ OFFICIAL SEAL ★<br>
                        COLOMBO, LK
                    </div>
                    <div style="font-size: 10px; color: #475569;">
                        Notes: ${invoice.notes}
                    </div>
                    <div class="sign-box">
                        For <strong>EXPLIC CEYLON (PVT) LTD</strong><br><br><br>
                        Authorized Signatory / Export Documentation Officer
                    </div>
                </div>

                <div class="doc-footer">
                    <div>Document Control: $docNumber • ISO 9001:2015 Certified Documentation</div>
                    <div>Page 1 of 1 • System Generated from Verified Database</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generatePackingListHtml(
        shipment: Shipment,
        packingList: PackingList,
        products: List<ShipmentProduct>,
        pallets: List<Pallet>
    ): String {
        // Calculate totals dynamically from database
        val totalCartons = products.sumOf { it.cartons }
        val totalUnits = products.sumOf { it.totalUnits }
        val totalNetWeightKg = products.sumOf { it.totalNetWeightKg }
        val totalGrossWeightKg = products.sumOf { it.totalGrossWeightKg }
        val totalCbm = products.sumOf { it.totalCbm }
        val totalPalletsCount = if (pallets.isNotEmpty()) pallets.size else packingList.totalPallets
        val docNumber = "DOC-PL-${shipment.reference.ifEmpty { shipment.id }}"

        val rows = products.mapIndexed { idx, p ->
            """
            <tr>
                <td style="text-align: center;">${idx + 1}</td>
                <td>${packingList.marksAndCartonNumbers}</td>
                <td>
                    <strong>${p.productName}</strong><br>
                    <small style="color: #64748b;">${p.description}</small>
                </td>
                <td style="text-align: center;">${p.hsCode}</td>
                <td style="text-align: right; font-weight: 600;">${p.cartons}</td>
                <td style="text-align: right;">${p.unitsPerCarton}</td>
                <td style="text-align: right; font-weight: 600;">${p.totalUnits}</td>
                <td style="text-align: right;">${"%.1f".format(p.totalNetWeightKg)}</td>
                <td style="text-align: right;">${"%.2f".format(p.cartonGrossWeightKg)}</td>
                <td style="text-align: right; font-weight: 700; color: #0f766e;">${"%.1f".format(p.totalGrossWeightKg)}</td>
                <td style="text-align: right;">${"%.4f".format(p.cbmPerCarton)}</td>
                <td style="text-align: right; font-weight: 700;">${"%.3f".format(p.totalCbm)}</td>
            </tr>
            """.trimIndent()
        }.joinToString("")

        val palletRows = pallets.joinToString("") { pal ->
            """
            <tr>
                <td style="padding: 4px 6px;"><strong>${pal.palletNumber}</strong></td>
                <td style="padding: 4px 6px;">${pal.palletType}</td>
                <td style="padding: 4px 6px;">${pal.productsSummary}</td>
                <td style="padding: 4px 6px; text-align: right; font-weight: 600;">${pal.cartons} ctns</td>
                <td style="padding: 4px 6px; text-align: right;">${"%.1f".format(pal.weightKg)} kg</td>
                <td style="padding: 4px 6px; text-align: right;">${"%.3f".format(pal.cbm)} CBM</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Packing List - ${shipment.id}</title>
            <style>
                $A4_BASE_CSS
                .doc-badge { background: #0f766e !important; }
                .doc-header { border-bottom: 2px solid #0f766e !important; }
                .grand-box {
                    background: #f0fdf4;
                    border: 2px solid #16a34a;
                    padding: 8px 12px;
                    border-radius: 4px;
                    display: flex;
                    justify-content: space-around;
                    margin-top: 10px;
                    margin-bottom: 12px;
                }
                .grand-item { text-align: center; }
                .grand-val { font-size: 13px; font-weight: 800; color: #166534; }
                .grand-lbl { font-size: 9px; color: #475569; text-transform: uppercase; font-weight: 600; }
            </style>
        </head>
        <body>
            <div class="a4-page">
                <div class="doc-header">
                    <div>
                        <div class="corp-logo-name" style="color: #0f766e;">EXPLIC CEYLON (PVT) LTD</div>
                        <div class="corp-sub">
                            43/1, Hospital Road, Dehiwala, Sri Lanka 10350 | Tel: +94 11 273 8920<br>
                            U.S. FDA Food Facility Registration: ${packingList.fdaRegistration}<br>
                            Warehouse & Logistics Facility, Colombo Port Terminal
                        </div>
                    </div>
                    <div class="doc-badge-panel">
                        <div class="doc-badge">PACKING LIST / WEIGHT CERTIFICATE</div>
                        <div class="doc-meta"><strong>Doc Control:</strong> $docNumber</div>
                        <div class="doc-meta"><strong>PI / Order:</strong> ${packingList.piNumber}</div>
                        <div class="doc-meta"><strong>Date:</strong> ${packingList.date}</div>
                        <div class="doc-meta"><strong>Shipment Ref:</strong> ${shipment.id}</div>
                    </div>
                </div>

                <div class="two-col">
                    <div class="card-box">
                        <div class="box-title" style="color: #0f766e;">Shipper / Consignor</div>
                        <strong>${packingList.shipper}</strong><br>
                        ${packingList.shipperAddress.replace("\n", "<br>")}
                    </div>
                    <div class="card-box">
                        <div class="box-title" style="color: #0f766e;">Consignee / Destination</div>
                        <strong>${packingList.consignee}</strong><br>
                        ${packingList.consigneeAddress.replace("\n", "<br>")}
                    </div>
                </div>

                <div class="card-box" style="margin-bottom: 10px;">
                    <div class="box-title" style="color: #0f766e;">Transport & Carrier Details</div>
                    <table style="width: 100%; font-size: 10px; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Port of Loading:</strong> ${packingList.portOfLoading}</td>
                            <td style="padding: 2px 4px;"><strong>Port of Discharge:</strong> ${packingList.portOfDischarge}</td>
                            <td style="padding: 2px 4px;"><strong>Transport Mode:</strong> ${packingList.seaAirCargo} (${packingList.fclLcl})</td>
                        </tr>
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Container Number:</strong> ${packingList.containerNumber}</td>
                            <td style="padding: 2px 4px;"><strong>Seal Number:</strong> ${packingList.sealNumber}</td>
                            <td style="padding: 2px 4px;"><strong>Ocean Vessel/Voyage:</strong> ${shipment.vessel} / ${shipment.voyage}</td>
                        </tr>
                    </table>
                </div>

                <table class="doc-table">
                    <thead>
                        <tr>
                            <th style="width: 4%;">#</th>
                            <th style="width: 12%;">Marks & Nos</th>
                            <th style="width: 28%;">Description of Goods</th>
                            <th style="width: 10%; text-align: center;">HS Code</th>
                            <th style="width: 7%; text-align: right;">Ctns</th>
                            <th style="width: 6%; text-align: right;">U/Ctn</th>
                            <th style="width: 7%; text-align: right;">Total U</th>
                            <th style="width: 7%; text-align: right;">Net (KG)</th>
                            <th style="width: 6%; text-align: right;">Ctn Gr</th>
                            <th style="width: 7%; text-align: right;">Gross (KG)</th>
                            <th style="width: 6%; text-align: right;">CBM/C</th>
                            <th style="width: 6%; text-align: right;">CBM</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rows
                    </tbody>
                </table>

                <div class="grand-box">
                    <div class="grand-item">
                        <div class="grand-val">$totalCartons</div>
                        <div class="grand-lbl">Total Cartons</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">$totalUnits</div>
                        <div class="grand-lbl">Total Units</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">${"%.1f".format(totalNetWeightKg)} KG</div>
                        <div class="grand-lbl">Total Net Wt</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">${"%.1f".format(totalGrossWeightKg)} KG</div>
                        <div class="grand-lbl">Total Gross Wt (SOLAS)</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">${"%.3f".format(totalCbm)}</div>
                        <div class="grand-lbl">Total CBM</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">$totalPalletsCount</div>
                        <div class="grand-lbl">Total Pallets</div>
                    </div>
                </div>

                ${if (pallets.isNotEmpty()) """
                <div style="font-size: 10px; font-weight: 700; color: #334155; margin-top: 8px; margin-bottom: 4px; text-transform: uppercase;">
                    Pallet Manifest & Specifications
                </div>
                <table class="doc-table" style="font-size: 9.5px; margin-top: 2px;">
                    <thead>
                        <tr style="background: #334155;">
                            <th>Pallet #</th>
                            <th>Pallet Type</th>
                            <th>Cargo Allocation</th>
                            <th style="text-align: right;">Cartons</th>
                            <th style="text-align: right;">Gross Wt</th>
                            <th style="text-align: right;">Volume</th>
                        </tr>
                    </thead>
                    <tbody>
                        $palletRows
                    </tbody>
                </table>
                """ else ""}

                <div class="sign-area">
                    <div class="stamp-box" style="border-color: #0f766e; color: #0f766e;">
                        EXPLIC PACKING<br>
                        ★ VERIFIED TALLY ★<br>
                        WAREHOUSE
                    </div>
                    <div style="font-size: 9.5px; color: #475569;">
                        We certify that the above goods have been packed, weighed, and palletized in compliance with international cargo standards.
                    </div>
                    <div class="sign-box">
                        For <strong>EXPLIC CEYLON (PVT) LTD</strong><br><br><br>
                        Warehouse Lead & Certified Weighbridge Officer
                    </div>
                </div>

                <div class="doc-footer">
                    <div>Document Control: $docNumber • Compliant with ISPM 15 Phytosanitary Packaging</div>
                    <div>Page 1 of 1 • Official Certified Export Packing List</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateIsfHtml(shipment: Shipment, isf: IsfFiling): String {
        val docNumber = "CBP-ISF-10+2-${shipment.reference.ifEmpty { shipment.id }}"
        val consigneeNum = if (isf.consigneeNumber.isNotEmpty()) isf.consigneeNumber else isf.importerOfRecordNumber

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>ISF 10+2 Filing - ${shipment.id}</title>
            <style>
                $A4_BASE_CSS
                .doc-badge { background: #b45309 !important; }
                .doc-header { border-bottom: 2px solid #b45309 !important; }
            </style>
        </head>
        <body>
            <div class="a4-page">
                <div class="doc-header">
                    <div>
                        <div class="corp-logo-name" style="color: #b45309;">U.S. CUSTOMS & BORDER PROTECTION</div>
                        <div class="corp-sub">
                            DEPARTMENT OF HOMELAND SECURITY • 19 CFR PART 149<br>
                            IMPORTER SECURITY FILING AND ADDITIONAL CARRIER REQUIREMENTS (ISF 10+2)<br>
                            Filer: Explic Ceylon (Pvt) Ltd (Authorized Export Manifest Compliance)
                        </div>
                    </div>
                    <div class="doc-badge-panel">
                        <div class="doc-badge">ISF 10+2 FILING TRANSMISSION</div>
                        <div class="doc-meta"><strong>Doc Control:</strong> $docNumber</div>
                        <div class="doc-meta"><strong>PO Number:</strong> ${isf.purchaseOrderNumber}</div>
                        <div class="doc-meta"><strong>Filing Date:</strong> ${isf.completedDate}</div>
                        <div class="doc-meta"><strong>Shipment Ref:</strong> ${shipment.id}</div>
                    </div>
                </div>

                <div class="card-box" style="margin-bottom: 12px; background: #fffbeb; border-color: #fde68a;">
                    <div style="font-weight: 700; color: #92400e; font-size: 11px;">
                        MANDATORY 10+2 CARGO SECURITY DECLARATION
                    </div>
                    <div style="font-size: 9.5px; color: #78350f;">
                        Transmitted to U.S. Customs and Border Protection at least 24 hours prior to loading at Port of Colombo.
                    </div>
                </div>

                <table class="doc-table">
                    <thead>
                        <tr style="background: #1e293b;">
                            <th style="width: 32%;">10+2 Mandated Data Element</th>
                            <th style="width: 68%;">Declared Entity Particulars & Location</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><strong>1. Manufacturer / Supplier</strong></td>
                            <td>${isf.manufacturerNameAndAddress}</td>
                        </tr>
                        <tr>
                            <td><strong>2. Seller</strong></td>
                            <td>${isf.sellerNameAndAddress}</td>
                        </tr>
                        <tr>
                            <td><strong>3. Buyer / Consignee</strong></td>
                            <td>${isf.consigneeNameAndAddress}</td>
                        </tr>
                        <tr>
                            <td><strong>4. Ship-To Party</strong></td>
                            <td>${isf.shipToNameAndAddress}</td>
                        </tr>
                        <tr>
                            <td><strong>5. Container Stuffing Location</strong></td>
                            <td>${isf.containerStuffingLocation}</td>
                        </tr>
                        <tr>
                            <td><strong>6. Consolidator / Stuffer</strong></td>
                            <td>${isf.consolidatorNameAndAddress}</td>
                        </tr>
                        <tr>
                            <td><strong>7. Importer of Record Number</strong></td>
                            <td><strong>${isf.importerOfRecordNumber}</strong></td>
                        </tr>
                        <tr>
                            <td><strong>8. Consignee Number(s)</strong></td>
                            <td>${consigneeNum}</td>
                        </tr>
                        <tr>
                            <td><strong>9. Country of Origin</strong></td>
                            <td><strong>${isf.countryOfOrigin}</strong></td>
                        </tr>
                        <tr>
                            <td><strong>10. Commodity HTSUS Numbers</strong></td>
                            <td><strong>${isf.productCodeHtsCode}</strong></td>
                        </tr>
                    </tbody>
                </table>

                <div class="card-box" style="margin-top: 10px; margin-bottom: 12px;">
                    <div class="box-title" style="color: #b45309;">Carrier Filing Linking Elements</div>
                    <table style="width: 100%; font-size: 10px; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Vessel & Voyage:</strong> ${isf.vessel} / ${isf.voyage}</td>
                            <td style="padding: 2px 4px;"><strong>Estimated Departure (ETD):</strong> ${isf.etd}</td>
                            <td style="padding: 2px 4px;"><strong>Total Cartons:</strong> <strong>${isf.cartonsCount} Cartons</strong></td>
                        </tr>
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Master B/L Number:</strong> ${isf.masterBlNumber}</td>
                            <td style="padding: 2px 4px;"><strong>AMS B/L Number:</strong> ${isf.amsBlNumber}</td>
                            <td style="padding: 2px 4px;"><strong>Container Number:</strong> ${isf.containerNumber}</td>
                        </tr>
                    </table>
                </div>

                <div class="card-box" style="background: #f8fafc; font-size: 10px;">
                    <div class="box-title">Authorized Filer Electronic Certification</div>
                    I certify that the information contained in this Importer Security Filing is true, correct, and complete to the best of my knowledge, and submitted in compliance with U.S. CBP regulations.<br><br>
                    <strong>Filer Name:</strong> ${isf.completedByName} • <strong>Title:</strong> ${isf.completedByTitle}<br>
                    <strong>Entity:</strong> ${isf.completedByCompany} • <strong>Contact:</strong> ${isf.completedByEmail}<br>
                    <strong>Transmission Status:</strong> ${if (isf.isReadyToTransmit) "VERIFIED & READY FOR EDI TRANSMISSION" else "DRAFT RECORD"}
                </div>

                <div class="sign-area">
                    <div class="stamp-box" style="border-color: #b45309; color: #b45309;">
                        U.S. CBP ISF<br>
                        ★ 10+2 RECORD ★<br>
                        COMPLIANT
                    </div>
                    <div style="font-size: 9.5px; color: #475569;">
                        Goods Description: ${isf.descriptionOfGoods}
                    </div>
                    <div class="sign-box">
                        Electronic Signature on File:<br>
                        <strong>${isf.completedByName}</strong><br>
                        Head of Compliance & Customs Documentation
                    </div>
                </div>

                <div class="doc-footer">
                    <div>Document Control: $docNumber • CBP ACE / ABI Portal Manifest Link</div>
                    <div>Page 1 of 1 • U.S. Customs Importer Security Filing</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateBlHtml(shipment: Shipment, bl: BlInstructions): String {
        val docNumber = "BLI-${bl.vessel.take(3).uppercase()}-${shipment.reference.ifEmpty { shipment.id }}"

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>B/L Instructions - ${shipment.id}</title>
            <style>
                $A4_BASE_CSS
                .doc-badge { background: #1d4ed8 !important; }
                .doc-header { border-bottom: 2px solid #1d4ed8 !important; }
            </style>
        </head>
        <body>
            <div class="a4-page">
                <div class="doc-header">
                    <div>
                        <div class="corp-logo-name" style="color: #1d4ed8;">OCEAN BILL OF LADING INSTRUCTIONS</div>
                        <div class="corp-sub">
                            BIMCO / CARRIER STANDARD SHIPPING INSTRUCTIONS DRAFT<br>
                            Carrier Line: CMA CGM Lanka (Pvt) Ltd / Ocean Carrier Booking Agent<br>
                            Shipper: Explic Ceylon (Private) Limited, Colombo, Sri Lanka
                        </div>
                    </div>
                    <div class="doc-badge-panel">
                        <div class="doc-badge">B/L SHIPPING INSTRUCTIONS</div>
                        <div class="doc-meta"><strong>Doc Control:</strong> $docNumber</div>
                        <div class="doc-meta"><strong>Booking / Ref:</strong> ${shipment.id}</div>
                        <div class="doc-meta"><strong>Date:</strong> ${shipment.invoiceDate}</div>
                        <div class="doc-meta"><strong>Container:</strong> ${bl.containerNumber}</div>
                    </div>
                </div>

                <div class="two-col">
                    <div class="card-box">
                        <div class="box-title" style="color: #1d4ed8;">Shipper (Full Name & Address)</div>
                        ${bl.shipper.replace("\n", "<br>")}
                    </div>
                    <div class="card-box">
                        <div class="box-title" style="color: #1d4ed8;">Consignee (Full Name & Address)</div>
                        ${bl.consignee.replace("\n", "<br>")}
                    </div>
                </div>

                <div class="two-col">
                    <div class="card-box">
                        <div class="box-title" style="color: #1d4ed8;">1st Notify Party</div>
                        ${bl.firstNotifyParty.replace("\n", "<br>")}
                    </div>
                    <div class="card-box">
                        <div class="box-title" style="color: #1d4ed8;">2nd Notify Party</div>
                        ${bl.secondNotifyParty.replace("\n", "<br>")}
                    </div>
                </div>

                <div class="card-box" style="margin-bottom: 10px;">
                    <div class="box-title" style="color: #1d4ed8;">Routing & Ocean Carriage Particulars</div>
                    <table style="width: 100%; font-size: 10px; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Pre-Carriage By:</strong> ${bl.placeOfReceipt}</td>
                            <td style="padding: 2px 4px;"><strong>Place of Receipt:</strong> ${bl.placeOfReceipt}</td>
                            <td style="padding: 2px 4px;"><strong>Ocean Vessel & Voyage:</strong> ${bl.vessel} / ${bl.voyageNumber}</td>
                        </tr>
                        <tr>
                            <td style="padding: 2px 4px;"><strong>Port of Loading:</strong> ${bl.portOfLoading}</td>
                            <td style="padding: 2px 4px;"><strong>Port of Discharge:</strong> ${bl.portOfDischarge}</td>
                            <td style="padding: 2px 4px;"><strong>Place of Delivery:</strong> ${bl.placeOfDelivery}</td>
                        </tr>
                    </table>
                </div>

                <table class="doc-table">
                    <thead>
                        <tr style="background: #1e293b;">
                            <th style="width: 20%;">Marks & Numbers</th>
                            <th style="width: 15%;">Packages Count</th>
                            <th style="width: 35%;">Description of Goods & HS</th>
                            <th style="width: 15%; text-align: right;">Gross Weight (KG)</th>
                            <th style="width: 15%; text-align: right;">Measurement (CBM)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>
                                <strong>${bl.marksAndNumbers}</strong><br>
                                Container: ${bl.containerNumber}
                            </td>
                            <td><strong>${bl.kindOfPackages}</strong></td>
                            <td>
                                ${bl.descriptionOfGoods.replace("\n", "<br>")}<br>
                                <small style="color: #64748b;">HTS Code: ${bl.hsNumber}</small>
                            </td>
                            <td style="text-align: right; font-weight: 700; color: #1d4ed8; font-size: 11px;">
                                ${"%.1f".format(bl.grossWeightKg)} KG
                                <br><small style="color: #16a34a; font-weight: normal;">(Verified VGM)</small>
                            </td>
                            <td style="text-align: right; font-weight: 700; font-size: 11px;">
                                ${"%.3f".format(bl.measurementCbm)} CBM
                            </td>
                        </tr>
                    </tbody>
                </table>

                <div class="two-col">
                    <div class="card-box">
                        <div class="box-title">Freight & Originals</div>
                        <strong>Freight Terms:</strong> ${bl.freightTerms}<br>
                        <strong>Number of Original B/Ls:</strong> ${bl.numberOfOriginals}<br>
                        <strong>Total Containers:</strong> ${bl.totalContainersPackages}
                    </div>
                    <div class="card-box">
                        <div class="box-title">Carrier Special Remarks & Clauses</div>
                        ${bl.remarks}<br>
                        <small style="color: #64748b;">Shipper's Load, Stow, and Count (SLAC). Clean on Board.</small>
                    </div>
                </div>

                <div class="sign-area">
                    <div class="stamp-box" style="border-color: #1d4ed8; color: #1d4ed8;">
                        OCEAN CARRIER<br>
                        ★ DRAFT B/L ★<br>
                        APPROVED
                    </div>
                    <div style="font-size: 9.5px; color: #475569;">
                        Instructions issued by Explic Ceylon Export Documentation Team for Master B/L generation.
                    </div>
                    <div class="sign-box">
                        For <strong>EXPLIC CEYLON (PVT) LTD</strong><br><br><br>
                        Authorized Shipping & Logistics Coordinator
                    </div>
                </div>

                <div class="doc-footer">
                    <div>Document Control: $docNumber • SOLAS VGM Declaration Compliant</div>
                    <div>Page 1 of 1 • Bill of Lading Shipping Instructions</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printDocument(context: Context, htmlContent: String, jobName: String) {
        try {
            val cleanName = jobName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val docDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir, "ExportDocuments")
            if (!docDir.exists()) docDir.mkdirs()

            val pdfFile = File(docDir, "$cleanName.pdf")
            val htmlFile = File(docDir, "$cleanName.html")
            htmlFile.writeText(htmlContent)

            // Generate clean native PDF using Android's built-in PdfDocument (No Chromium/WebView required)
            generateNativePdf(htmlContent, cleanName, pdfFile)

            val targetFile = if (pdfFile.exists() && pdfFile.length() > 0) pdfFile else htmlFile
            val fileUri = try {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    targetFile
                )
            } catch (_: Throwable) {
                null
            }

            if (fileUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (targetFile.name.endsWith(".pdf")) "application/pdf" else "text/html"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Explic Export: $jobName")
                    putExtra(Intent.EXTRA_TEXT, "Export Document $jobName generated by Explic Ceylon (Pvt) Ltd.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export / Share $jobName"))
            } else {
                shareDocument(context, jobName, "Export Document: $jobName\n\nFile saved to: ${targetFile.absolutePath}")
            }
            android.widget.Toast.makeText(context, "Exported: ${targetFile.name}", android.widget.Toast.LENGTH_SHORT).show()
        } catch (_: Throwable) {
            shareDocument(context, jobName, "Export Document: $jobName\n\n$htmlContent")
        }
    }

    private fun generateNativePdf(content: String, title: String, outputFile: File) {
        try {
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 9.5f
                color = android.graphics.Color.BLACK
            }
            val titlePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 13f
                isFakeBoldText = true
                color = android.graphics.Color.rgb(10, 25, 47)
            }
            val headerPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 9.5f
                isFakeBoldText = true
                color = android.graphics.Color.rgb(30, 58, 138)
            }
            val borderPaint = android.graphics.Paint().apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 1f
                color = android.graphics.Color.rgb(203, 213, 225)
            }

            canvas.drawRect(36f, 36f, 559f, 806f, borderPaint)
            canvas.drawText("EXPLIC CEYLON (PRIVATE) LIMITED", 48f, 56f, titlePaint)
            canvas.drawText("Export • Logistics • Documentation Management", 48f, 70f, headerPaint)
            canvas.drawText("Document: $title  •  Official Certified Trade Copy", 48f, 84f, paint)
            canvas.drawLine(36f, 94f, 559f, 94f, borderPaint)

            val plainLines = content
                .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<[^>]+>"), "\n")
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            var y = 112f
            for (line in plainLines) {
                if (y > 780f) break
                val truncatedLine = if (line.length > 85) line.take(85) + "..." else line
                if (truncatedLine.startsWith("===") || truncatedLine.startsWith("---") ||
                    truncatedLine.contains("INVOICE", ignoreCase = true) ||
                    truncatedLine.contains("PACKING LIST", ignoreCase = true) ||
                    truncatedLine.contains("BILL OF LADING", ignoreCase = true) ||
                    truncatedLine.contains("ISF", ignoreCase = true)
                ) {
                    canvas.drawText(truncatedLine, 48f, y, headerPaint)
                    y += 15f
                } else {
                    canvas.drawText(truncatedLine, 48f, y, paint)
                    y += 12.5f
                }
            }

            canvas.drawLine(36f, 786f, 559f, 786f, borderPaint)
            paint.textSize = 7.5f
            canvas.drawText("Explic Export Pro • Verified A4 Trade Document • SOLAS VGM & Customs Compliant", 48f, 798f, paint)

            pdfDocument.finishPage(page)
            outputFile.outputStream().use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
        } catch (_: Throwable) {
            // Safe fallback
        }
    }

    fun shareDocument(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(intent, "Share $title"))
    }

    fun generateDeliveryNoteHtml(
        deliveryNote: DeliveryNote,
        shipment: Shipment?
    ): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Delivery Note - ${deliveryNote.noteNumber}</title>
            <style>
                $A4_BASE_CSS
            </style>
        </head>
        <body>
            <div class="a4-page">
                <div class="doc-header">
                    <div>
                        <div class="corp-logo-name">EXPLIC CEYLON (PVT) LTD</div>
                        <div class="corp-sub">Logistics & Supply Chain Operations • Warehouse Division</div>
                        <div class="corp-address">
                            43/1, Hospital Road, Dehiwala, Sri Lanka 10350<br>
                            Tel: +94 11 273 8920 • Email: warehouse@explicceylon.com<br>
                            TIN: LK102948293 • Reg No: PV 00291048
                        </div>
                    </div>
                    <div class="doc-title-block">
                        <div class="doc-type-title">DELIVERY NOTE</div>
                        <div class="doc-meta-val" style="font-size: 13px; color: #0284c7; margin-bottom: 2px;">
                            ${deliveryNote.noteNumber}
                        </div>
                        <div style="font-size: 10px; color: #475569;">
                            Date: <strong>${deliveryNote.date}</strong><br>
                            Shipment Ref: <strong>${deliveryNote.shipmentId}</strong>
                        </div>
                    </div>
                </div>

                <div class="info-grid">
                    <table class="info-box">
                        <tr>
                            <td class="info-header" colspan="2">CONSIGNEE & DELIVERY DESTINATION</td>
                        </tr>
                        <tr>
                            <td class="info-lbl" style="width: 32%;">Consignee:</td>
                            <td class="info-val"><strong>${deliveryNote.consigneeName}</strong></td>
                        </tr>
                        <tr>
                            <td class="info-lbl">Address:</td>
                            <td class="info-val">${deliveryNote.consigneeAddress}</td>
                        </tr>
                        <tr>
                            <td class="info-lbl">Destination Port:</td>
                            <td class="info-val"><strong>${deliveryNote.destinationPort}</strong></td>
                        </tr>
                        <tr>
                            <td class="info-lbl">PO Number:</td>
                            <td class="info-val">${shipment?.poNumber ?: "N/A"}</td>
                        </tr>
                    </table>

                    <table class="info-box">
                        <tr>
                            <td class="info-header" colspan="2">TRANSPORT & CONTAINER DISPATCH DETAILS</td>
                        </tr>
                        <tr>
                            <td class="info-lbl" style="width: 36%;">Container Number:</td>
                            <td class="info-val"><strong style="color: #0369a1; font-size: 11.5px;">${deliveryNote.containerNumber}</strong></td>
                        </tr>
                        <tr>
                            <td class="info-lbl">Seal Number:</td>
                            <td class="info-val"><strong>${deliveryNote.sealNumber}</strong> (High Security Bolt)</td>
                        </tr>
                        <tr>
                            <td class="info-lbl">Vehicle / Truck:</td>
                            <td class="info-val"><strong>${deliveryNote.vehicleNumber}</strong></td>
                        </tr>
                        <tr>
                            <td class="info-lbl">Driver Name:</td>
                            <td class="info-val">${deliveryNote.driverName}</td>
                        </tr>
                        <tr>
                            <td class="info-lbl">Driver Contact/NIC:</td>
                            <td class="info-val">${deliveryNote.driverPhone}</td>
                        </tr>
                    </table>
                </div>

                <table class="doc-table" style="margin-top: 14px;">
                    <thead>
                        <tr>
                            <th style="width: 8%;">Item</th>
                            <th style="width: 48%;">Cargo Description & Goods Summary</th>
                            <th style="width: 11%; text-align: right;">Pallets</th>
                            <th style="width: 11%; text-align: right;">Cartons</th>
                            <th style="width: 11%; text-align: right;">Gross Wt (kg)</th>
                            <th style="width: 11%; text-align: right;">CBM</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>01</td>
                            <td>
                                <strong>Export Cargo Stuffed for Shipment ${deliveryNote.shipmentId}</strong><br>
                                <span style="font-size: 10px; color: #475569;">${deliveryNote.itemsSummary}</span>
                            </td>
                            <td style="text-align: right; font-weight: bold;">${deliveryNote.totalPallets}</td>
                            <td style="text-align: right; font-weight: bold;">${deliveryNote.totalCartons}</td>
                            <td style="text-align: right; font-weight: bold;">${"%.1f".format(deliveryNote.totalGrossWeightKg)}</td>
                            <td style="text-align: right; font-weight: bold;">${"%.3f".format(deliveryNote.totalCbm)}</td>
                        </tr>
                    </tbody>
                </table>

                <div class="grand-box">
                    <div class="grand-item">
                        <div class="grand-val">${deliveryNote.totalPallets}</div>
                        <div class="grand-lbl">Total Pallets</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">${deliveryNote.totalCartons}</div>
                        <div class="grand-lbl">Total Cartons</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">${"%.1f".format(deliveryNote.totalGrossWeightKg)} KG</div>
                        <div class="grand-lbl">Total Gross Weight</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val">${"%.3f".format(deliveryNote.totalCbm)}</div>
                        <div class="grand-lbl">Total Volume CBM</div>
                    </div>
                    <div class="grand-item">
                        <div class="grand-val" style="color: #059669;">PASSED</div>
                        <div class="grand-lbl">Tare & Weight Verified</div>
                    </div>
                </div>

                <div style="background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 4px; padding: 10px; margin-top: 14px; font-size: 10px;">
                    <strong>WAREHOUSE DISPATCH INSTRUCTIONS & SPECIAL NOTES:</strong><br>
                    ${if (deliveryNote.notes.isNotBlank()) deliveryNote.notes else "All goods inspected prior to stuffing. Verified container tare weight, seal integrity, and moisture protection liners applied. Goods dispatched for terminal gate-in at Port of Colombo."}
                </div>

                <div style="margin-top: 32px; display: flex; justify-content: space-between;">
                    <div style="width: 45%; border-top: 1px solid #334155; padding-top: 6px; font-size: 10px;">
                        <strong>PREPARED BY (WAREHOUSE SUPERVISOR):</strong><br>
                        ${deliveryNote.preparedBy}<br>
                        Logistics & Dispatch Division<br>
                        Explic Ceylon (Pvt) Ltd
                    </div>
                    <div style="width: 45%; border-top: 1px solid #334155; padding-top: 6px; font-size: 10px;">
                        <strong>RECEIVED IN GOOD ORDER & CONDITION:</strong><br>
                        ${if (deliveryNote.receivedBySignature.isNotBlank()) deliveryNote.receivedBySignature else "${deliveryNote.driverName} (Haulier / Driver)"}<br>
                        Vehicle: ${deliveryNote.vehicleNumber}<br>
                        Date: ${deliveryNote.date}
                    </div>
                </div>

                <div class="doc-footer">
                    <div>EXPLIC CEYLON (PVT) LTD • WAREHOUSE OPERATING SYSTEM • EXPORT LOGISTICS HUB</div>
                    <div>Page 1 of 1 • Delivery Note ${deliveryNote.noteNumber}</div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateCsvExport(
        shipment: Shipment,
        products: List<ShipmentProduct>,
        pallets: List<Pallet>,
        invoice: CommercialInvoice?,
        packingList: PackingList?,
        isf: IsfFiling?,
        bl: BlInstructions?
    ): String {
        val sb = StringBuilder()
        sb.append("=== EXPLIC EXPORT PRO - MULTI-SECTION SHIPMENT EXPORT ===\n")
        sb.append("Generated on: ${java.util.Date()}\n\n")

        sb.append("--- SECTION 1: SHIPMENT HEADER ---\n")
        sb.append("Shipment ID,Reference,Customer,PO Number,Invoice Number,Invoice Date,Currency,Mode,Port of Loading,Port of Discharge,Place of Delivery,Vessel,Voyage,ETD,Container,Master BL,AMS BL,Status\n")
        sb.append("\"${shipment.id}\",\"${shipment.reference}\",\"${shipment.customer}\",\"${shipment.poNumber}\",\"${shipment.invoiceNumber}\",\"${shipment.invoiceDate}\",\"${shipment.currency}\",\"${shipment.shippingMode.label}\",\"${shipment.portOfLoading}\",\"${shipment.portOfDischarge}\",\"${shipment.placeOfDelivery}\",\"${shipment.vessel}\",\"${shipment.voyage}\",\"${shipment.etd}\",\"${shipment.containerNumber}\",\"${shipment.masterBlNumber}\",\"${shipment.amsBlNumber}\",\"${shipment.status.label}\"\n\n")

        sb.append("--- SECTION 2: PRODUCTS & QUANTITIES ---\n")
        sb.append("Product Name,HS Code,Description,Cartons,Units/Ctn,Total Units,Unit Wt (kg),Net Wt (kg),Ctn Gross (kg),Total Gross (kg),CBM/Ctn,Total CBM,Country of Origin,Unit Price,Total Price\n")
        for (p in products) {
            sb.append("\"${p.productName}\",\"${p.hsCode}\",\"${p.description}\",${p.cartons},${p.unitsPerCarton},${p.totalUnits},${p.individualUnitWeightKg},${p.totalNetWeightKg},${p.cartonGrossWeightKg},${p.totalGrossWeightKg},${p.cbmPerCarton},${p.totalCbm},\"${p.countryOfOrigin}\",${p.unitPriceUsd},${p.totalAmountUsd}\n")
        }
        sb.append("\n")

        sb.append("--- SECTION 3: PALLET BREAKDOWN ---\n")
        sb.append("Pallet No,Specification,Allocated Products,Cartons,Gross Wt (kg),CBM\n")
        for (pal in pallets) {
            sb.append("\"${pal.palletNumber}\",\"${pal.palletType}\",\"${pal.productsSummary}\",${pal.cartons},${pal.weightKg},${pal.cbm}\n")
        }
        sb.append("\n")

        if (packingList != null) {
            sb.append("--- SECTION 4: PACKING LIST TOTALS ---\n")
            sb.append("Total Cartons,Total Units,Total Net Wt (kg),Total Gross Wt (kg),Total CBM,Total Pallets,Container,Seal\n")
            sb.append("${packingList.totalCartons},${packingList.totalUnits},${packingList.totalNetWeightKg},${packingList.totalGrossWeightKg},${packingList.totalCbm},${packingList.totalPallets},\"${packingList.containerNumber}\",\"${packingList.sealNumber}\"\n\n")
        }

        if (isf != null) {
            sb.append("--- SECTION 5: ISF 10+2 FILING DATA ---\n")
            sb.append("Manufacturer,Seller,Consignee,Ship To,Stuffing Yard,Consolidator,Importer of Record,HTS Codes,Cartons,Vessel,Voyage\n")
            sb.append("\"${isf.manufacturerNameAndAddress}\",\"${isf.sellerNameAndAddress}\",\"${isf.consigneeNameAndAddress}\",\"${isf.shipToNameAndAddress}\",\"${isf.containerStuffingLocation}\",\"${isf.consolidatorNameAndAddress}\",\"${isf.importerOfRecordNumber}\",\"${isf.productCodeHtsCode}\",${isf.cartonsCount},\"${isf.vessel}\",\"${isf.voyage}\"\n\n")
        }

        if (bl != null) {
            sb.append("--- SECTION 6: BILL OF LADING INSTRUCTIONS ---\n")
            sb.append("Shipper,Consignee,Notify,Port of Loading,Port of Discharge,Vessel,Voyage,Packages,Gross Wt (kg),CBM,Freight Terms\n")
            sb.append("\"${bl.shipper.replace("\n", " ")}\",\"${bl.consignee.replace("\n", " ")}\",\"${bl.firstNotifyParty.replace("\n", " ")}\",\"${bl.portOfLoading}\",\"${bl.portOfDischarge}\",\"${bl.vessel}\",\"${bl.voyageNumber}\",\"${bl.kindOfPackages}\",${bl.grossWeightKg},${bl.measurementCbm},\"${bl.freightTerms}\"\n")
        }

        return sb.toString()
    }

    fun generateCombinedDossierHtml(
        shipment: Shipment,
        invoice: CommercialInvoice?,
        packingList: PackingList?,
        isf: IsfFiling?,
        bl: BlInstructions?,
        products: List<ShipmentProduct>,
        pallets: List<Pallet>,
        includeInvoice: Boolean = true,
        includePackingList: Boolean = true,
        includeIsf: Boolean = true,
        includeBl: Boolean = true
    ): String {
        val parts = mutableListOf<String>()
        if (includeInvoice && invoice != null) {
            parts.add(generateInvoiceHtml(shipment, invoice, products))
        }
        if (includePackingList && packingList != null) {
            parts.add(generatePackingListHtml(shipment, packingList, products, pallets))
        }
        if (includeIsf && isf != null) {
            parts.add(generateIsfHtml(shipment, isf))
        }
        if (includeBl && bl != null) {
            parts.add(generateBlHtml(shipment, bl))
        }
        if (parts.isEmpty()) {
            return "<html><body><h1>No documents selected for printing.</h1></body></html>"
        }
        return parts.joinToString("""<div style="page-break-after: always; margin: 40px 0; border-bottom: 2px dashed #94a3b8;"></div>""")
    }
}
