package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole(val label: String, val description: String) {
    ADMIN("Admin", "Full access to all modules, users, master data & audit logs"),
    EXPORT_MANAGER("Export Manager", "Can approve documents, override warnings, view shipments & reports"),
    DOCUMENTATION_OFFICER("Documentation Officer", "Can create shipments, generate docs, run validations, export files"),
    WAREHOUSE_USER("Warehouse User", "Update packing info, cartons, gross weights & pallets"),
    VIEWER("Viewer", "Read-only access to shipments, packing lists & documents")
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val department: String,
    val telephone: String
)

enum class ShippingMode(val label: String) {
    SEA("Sea Freight"),
    AIR("Air Cargo"),
    COURIER("Express Courier")
}

enum class ShipmentStatus(val label: String, val stepIndex: Int, val description: String = "") {
    NEW("NEW", 0, "Shipment record created, awaiting order confirmation"),
    ORDER_CONFIRMED("ORDER CONFIRMED", 1, "Buyer confirmed purchase order & commercial terms"),
    PRODUCTION("PRODUCTION", 2, "Scheduled & currently in factory manufacturing"),
    PACKED("PACKED", 3, "Cartons packed, measured, weighed & palletized"),
    DOCUMENTATION("DOCUMENTATION", 4, "Commercial invoice & packing list prepared"),
    ISF_FILED("ISF FILED", 5, "ISF 10+2 transmitted to US Customs & Border Protection"),
    BOOKING_CONFIRMED("BOOKING CONFIRMED", 6, "Carrier vessel booking confirmed with ocean liner"),
    LOADED("LOADED", 7, "Container stuffed, weighed & loaded onto vessel"),
    BL_RELEASED("BL RELEASED", 8, "Original Bill of Lading instruction released & verified"),
    SHIPPED("SHIPPED", 9, "Vessel departed & in transit to destination port"),
    DELIVERED("DELIVERED", 10, "Customs cleared & safely received at consignee warehouse"),
    CANCELLED("CANCELLED", -1, "Shipment cancelled");

    companion object {
        val ALL_STAGES = listOf(
            NEW,
            ORDER_CONFIRMED,
            PRODUCTION,
            PACKED,
            DOCUMENTATION,
            ISF_FILED,
            BOOKING_CONFIRMED,
            LOADED,
            BL_RELEASED,
            SHIPPED,
            DELIVERED
        )
    }
}

enum class DocumentStatus(val label: String) {
    DRAFT("Draft"),
    READY("Ready"),
    VALIDATION_ERROR("Validation Error"),
    APPROVED("Approved"),
    GENERATED("Generated (PDF)"),
    SENT("Sent to Carrier / Buyer")
}

enum class PartyType(val label: String) {
    EXPORTER("Exporter"),
    MANUFACTURER("Manufacturer"),
    SUPPLIER("Supplier"),
    SELLER("Seller"),
    CONSIGNEE("Consignee"),
    NOTIFY_PARTY("Notify Party"),
    IMPORTER_OF_RECORD("Importer of Record"),
    SHIP_TO("Ship-To Party"),
    STUFFING_LOCATION("Container Stuffing Location"),
    CONSOLIDATOR("Consolidator / Stuffer")
}

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyName: String,
    val address: String,
    val country: String,
    val contactPerson: String,
    val telephone: String,
    val email: String,
    val taxId: String = "",
    val fdaRegistration: String = "",
    val importerOfRecordNumber: String = "",
    val partyType: PartyType
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val hsCode: String,
    val countryOfOrigin: String,
    val defaultUnitWeightKg: Double,
    val defaultCartonSize: String,
    val unitsPerCarton: Int,
    val defaultCbmPerCarton: Double,
    val category: String,
    val barcode: String = "",
    val unitPriceUsd: Double = 0.0,
    val isSyncPending: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shipments")
data class Shipment(
    @PrimaryKey val id: String, // e.g. ECGMIX_092826_016
    val reference: String,
    val customer: String,
    val poNumber: String,
    val invoiceNumber: String,
    val invoiceDate: String,
    val currency: String = "USD",
    val shippingMode: ShippingMode = ShippingMode.SEA,
    val portOfLoading: String,
    val portOfDischarge: String,
    val placeOfDelivery: String,
    val vessel: String,
    val voyage: String,
    val etd: String,
    val containerNumber: String,
    val masterBlNumber: String,
    val amsBlNumber: String,
    val status: ShipmentStatus = ShipmentStatus.DOCUMENTATION,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isOverrideApproved: Boolean = false,
    val overrideApprovedBy: String? = null,
    val overrideTimestamp: Long? = null,
    val overrideNotes: String? = null,
    val isSyncPending: Boolean = false,
    val syncStatus: String = "SYNCED",
    val lastSyncedAt: Long = 0L
)

@Entity(tableName = "shipment_products")
data class ShipmentProduct(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shipmentId: String,
    val productId: Long,
    val productName: String,
    val hsCode: String,
    val description: String,
    val cartons: Int,
    val unitsPerCarton: Int,
    val totalUnits: Int, // Cartons * unitsPerCarton
    val individualUnitWeightKg: Double,
    val totalNetWeightKg: Double, // totalUnits * individualUnitWeightKg
    val cartonGrossWeightKg: Double,
    val totalGrossWeightKg: Double, // cartons * cartonGrossWeightKg
    val cbmPerCarton: Double,
    val totalCbm: Double, // cartons * cbmPerCarton
    val countryOfOrigin: String,
    val unitPriceUsd: Double = 0.0,
    val totalAmountUsd: Double = 0.0,
    val isManualOverride: Boolean = false
)

@Entity(tableName = "pallets")
data class Pallet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shipmentId: String,
    val palletNumber: String, // "Pallet 1" ...
    val palletType: String = "Standard Wooden (1200x1000)",
    val lengthCm: Double = 120.0,
    val widthCm: Double = 100.0,
    val heightCm: Double = 140.0,
    val weightKg: Double = 25.0,
    val cbm: Double = 1.68,
    val cartons: Int = 0,
    val productsSummary: String = "",
    val maxCartonCapacity: Int = 60
)

@Entity(tableName = "warehouse_stock")
data class WarehouseStockItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val hsCode: String,
    val batchLotNumber: String,
    val barcode: String,
    val totalReceivedCartons: Int,
    val availableCartons: Int,
    val allocatedCartons: Int,
    val dispatchedCartons: Int,
    val unitsPerCarton: Int,
    val unitWeightKg: Double,
    val cartonGrossWeightKg: Double,
    val cbmPerCarton: Double,
    val lengthCm: Double = 40.0,
    val widthCm: Double = 30.0,
    val heightCm: Double = 25.0,
    val locationBay: String = "Bay A-01",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "goods_receipts")
data class GoodsReceipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val productId: Long,
    val productName: String,
    val supplierName: String,
    val shipmentId: String? = null,
    val cartonsReceived: Int,
    val unitsPerCarton: Int,
    val totalUnits: Int,
    val cartonGrossWeightKg: Double,
    val totalGrossWeightKg: Double,
    val lengthCm: Double,
    val widthCm: Double,
    val heightCm: Double,
    val totalCbm: Double,
    val batchNumber: String,
    val barcode: String,
    val locationBay: String,
    val receivedDate: String,
    val receivedBy: String,
    val notes: String = ""
)

@Entity(tableName = "warehouse_dispatches")
data class WarehouseDispatch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dispatchNumber: String,
    val shipmentId: String,
    val customer: String,
    val deliveryNoteNumber: String,
    val containerNumber: String,
    val sealNumber: String,
    val vehicleNumber: String,
    val driverName: String,
    val driverNic: String,
    val totalCartonsDispatched: Int,
    val totalPalletsDispatched: Int,
    val totalGrossWeightKg: Double,
    val totalCbm: Double,
    val dispatchDate: String,
    val dispatchedBy: String,
    val status: String = "DISPATCHED" // DISPATCHED, IN_TRANSIT, DELIVERED
)

@Entity(tableName = "delivery_notes")
data class DeliveryNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteNumber: String,
    val shipmentId: String,
    val date: String,
    val consigneeName: String,
    val consigneeAddress: String,
    val destinationPort: String,
    val containerNumber: String,
    val sealNumber: String,
    val vehicleNumber: String,
    val driverName: String,
    val driverPhone: String,
    val itemsSummary: String,
    val totalCartons: Int,
    val totalPallets: Int,
    val totalGrossWeightKg: Double,
    val totalCbm: Double,
    val preparedBy: String,
    val receivedBySignature: String = "",
    val notes: String = ""
)

@Entity(tableName = "commercial_invoices")
data class CommercialInvoice(
    @PrimaryKey val shipmentId: String,
    val invoiceNumber: String,
    val invoiceDate: String,
    val exporterName: String,
    val exporterAddress: String,
    val buyerName: String,
    val buyerAddress: String,
    val consigneeName: String,
    val consigneeAddress: String,
    val currency: String = "USD",
    val paymentTerms: String = "T/T 30 Days Net from B/L Date",
    val freight: Double = 0.0,
    val insurance: Double = 0.0,
    val leadTime: String = "14 Working Days",
    val notes: String = "Goods manufactured under strict quality standards in Sri Lanka.",
    val accountName: String = "Explic Ceylon (Private) Limited",
    val bankName: String = "Commercial Bank of Ceylon PLC",
    val accountNumber: String = "1000-8472-9102",
    val branchCode: String = "018",
    val bankCode: String = "7056",
    val swiftCode: String = "CCEYLKX",
    val bankAddress: String = "Colombo 01 Corporate Branch, Sri Lanka",
    val status: DocumentStatus = DocumentStatus.DRAFT,
    val totalAmount: Double = 0.0
)

@Entity(tableName = "packing_lists")
data class PackingList(
    @PrimaryKey val shipmentId: String,
    val consignee: String,
    val consigneeAddress: String,
    val shipper: String,
    val shipperAddress: String,
    val piNumber: String,
    val date: String,
    val portOfLoading: String,
    val portOfDischarge: String,
    val fclLcl: String = "FCL 1x20' GP",
    val seaAirCargo: String = "Sea Cargo",
    val fdaRegistration: String = "19482019482",
    val containerNumber: String,
    val sealNumber: String = "SL-940284",
    val marksAndCartonNumbers: String = "EXP/2026/016 1-UP",
    val totalCartons: Int = 0,
    val totalUnits: Int = 0,
    val totalNetWeightKg: Double = 0.0,
    val totalGrossWeightKg: Double = 0.0,
    val totalCbm: Double = 0.0,
    val totalPallets: Int = 0,
    val status: DocumentStatus = DocumentStatus.DRAFT
)

@Entity(tableName = "isf_filings")
data class IsfFiling(
    @PrimaryKey val shipmentId: String,
    val manufacturerNameAndAddress: String,
    val sellerNameAndAddress: String,
    val consigneeNameAndAddress: String,
    val shipToNameAndAddress: String,
    val containerStuffingLocation: String,
    val consolidatorNameAndAddress: String,
    val descriptionOfGoods: String,
    val productCodeHtsCode: String,
    val countryOfOrigin: String,
    val purchaseOrderNumber: String,
    val importerOfRecordNumber: String,
    val consigneeNumber: String = "",
    val vessel: String,
    val voyage: String,
    val etd: String,
    val masterBlNumber: String,
    val amsBlNumber: String,
    val containerNumber: String,
    val cartonsCount: Int = 0,
    val totalGrossWeightKg: Double = 0.0,
    val completedByCompany: String = "Explic Ceylon (Pvt) Ltd",
    val completedByName: String = "Roshan Perera",
    val completedByTitle: String = "Head of Documentation & Compliance",
    val completedByEmail: String = "docs@explicceylon.com",
    val completedDate: String = "",
    val isReadyToTransmit: Boolean = false,
    val status: DocumentStatus = DocumentStatus.DRAFT
)

@Entity(tableName = "bl_instructions")
data class BlInstructions(
    @PrimaryKey val shipmentId: String,
    val shipper: String,
    val consignee: String,
    val firstNotifyParty: String,
    val secondNotifyParty: String = "SAME AS CONSIGNEE",
    val vessel: String,
    val voyageNumber: String,
    val placeOfDelivery: String,
    val portOfLoading: String,
    val portOfDischarge: String,
    val marksAndNumbers: String,
    val hsNumber: String,
    val kindOfPackages: String = "9 Pallets / 505 Cartons",
    val descriptionOfGoods: String,
    val grossWeightKg: Double = 0.0,
    val measurementCbm: Double = 0.0,
    val totalContainersPackages: String = "1 x 20' Dry Container (9 Pallets)",
    val placeOfReceipt: String = "Colombo CFS, Sri Lanka",
    val freightTerms: String = "FREIGHT PREPAID",
    val numberOfOriginals: String = "THREE (3) ORIGINALS",
    val remarks: String = "CLEAN ON BOARD. TRANSSHIPMENT PERMITTED.",
    val containerNumber: String,
    val status: DocumentStatus = DocumentStatus.DRAFT
)

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["shipmentId"]),
        Index(value = ["documentType"]),
        Index(value = ["documentNumber"])
    ]
)
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shipmentId: String,
    val documentType: String, // "COMMERCIAL_INVOICE", "PACKING_LIST", "ISF_10_2", "BILL_OF_LADING", "CERTIFICATE_OF_ORIGIN", "SOLAS_VGM"
    val documentNumber: String,
    val title: String,
    val issueDate: String,
    val status: DocumentStatus = DocumentStatus.DRAFT,
    val fileFormat: String = "PDF", // "PDF", "EXCEL", "JSON"
    val filePath: String = "",
    val fileSizeKb: Long = 0,
    val generatedBy: String = "",
    val version: Int = 1,
    val checksum: String = "",
    val metadataJson: String = "",
    val isOfflineAvailable: Boolean = true,
    val isSyncPending: Boolean = false,
    val lastSyncedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

typealias ExportDocument = Document

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["shipmentId"]),
        Index(value = ["timestamp"]),
        Index(value = ["userName"])
    ]
)
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String,
    val userName: String,
    val userRole: String,
    val action: String,
    val oldValue: String,
    val newValue: String,
    val document: String,
    val shipmentId: String,
    val isSyncPending: Boolean = false,
    val details: String = ""
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val type: String, // "ERROR", "WARNING", "SUCCESS", "INFO"
    val shipmentId: String? = null,
    val isRead: Boolean = false
)

enum class ValidationSeverity {
    MATCH,    // GREEN
    WARNING,  // YELLOW
    ERROR     // RED
}

data class ValidationItem(
    val fieldName: String,
    val severity: ValidationSeverity,
    val message: String,
    val documentA: String,
    val valueA: String,
    val documentB: String,
    val valueB: String,
    val category: String = "",
    val isResolved: Boolean = false,
    val recommendedAction: String = ""
)

data class ShipmentValidationSummary(
    val shipmentId: String,
    val totalMatches: Int,
    val totalWarnings: Int,
    val totalErrors: Int,
    val canMarkComplete: Boolean,
    val items: List<ValidationItem>
)

data class FullShipmentBundle(
    val shipment: Shipment,
    val products: List<ShipmentProduct>,
    val pallets: List<Pallet>,
    val invoice: CommercialInvoice?,
    val packingList: PackingList?,
    val isf: IsfFiling?,
    val bl: BlInstructions?,
    val validationSummary: ShipmentValidationSummary
)

@Entity(tableName = "offline_drafts")
data class OfflineDraft(
    @PrimaryKey val draftId: String,
    val draftType: String, // "SHIPMENT", "GOODS_RECEIPT", "PALLET", "DISPATCH"
    val title: String,
    val summary: String,
    val payloadJson: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

