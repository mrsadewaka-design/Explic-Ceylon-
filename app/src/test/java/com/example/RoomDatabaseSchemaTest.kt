package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.*
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDatabaseSchemaTest {

    private lateinit var database: AppDatabase
    private lateinit var shipmentDao: ShipmentDao
    private lateinit var productDao: ProductDao
    private lateinit var documentDao: DocumentDao
    private lateinit var auditLogDao: AuditLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        shipmentDao = database.shipmentDao()
        productDao = database.productDao()
        documentDao = database.documentDao()
        auditLogDao = database.auditLogDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==========================================
    // 1. SHIPMENTS ENTITY & DAO TESTS
    // ==========================================
    @Test
    fun testShipmentEntityAndDaoLocalPersistence() = runBlocking {
        val shipment = Shipment(
            id = "EXP-TEST-001",
            reference = "REF-2026-001",
            customer = "ACME GLOBAL LOGISTICS",
            poNumber = "PO-9991",
            invoiceNumber = "INV-001",
            invoiceDate = "2026-10-01",
            currency = "USD",
            shippingMode = ShippingMode.SEA,
            portOfLoading = "Colombo, Sri Lanka",
            portOfDischarge = "Long Beach, CA, USA",
            placeOfDelivery = "Los Angeles DC, USA",
            vessel = "MSC ISABELLA",
            voyage = "ISB-26",
            etd = "2026-10-20",
            containerNumber = "MSCU1234567/40",
            masterBlNumber = "MSCU98765432",
            amsBlNumber = "AMS-98765432",
            status = ShipmentStatus.PACKED,
            isSyncPending = true,
            syncStatus = "PENDING_SYNC"
        )

        // Insert
        shipmentDao.insertShipment(shipment)

        // Read by ID
        val retrieved = shipmentDao.getShipmentById("EXP-TEST-001")
        assertNotNull(retrieved)
        assertEquals("EXP-TEST-001", retrieved?.id)
        assertEquals("ACME GLOBAL LOGISTICS", retrieved?.customer)
        assertEquals(ShipmentStatus.PACKED, retrieved?.status)
        assertTrue(retrieved?.isSyncPending == true)

        // Reactive Flow
        val allShipments = shipmentDao.getAllShipments().first()
        assertEquals(1, allShipments.size)

        // Search Flow
        val searchResults = shipmentDao.searchShipmentsFlow("ACME").first()
        assertEquals(1, searchResults.size)
        assertEquals("EXP-TEST-001", searchResults[0].id)

        // Offline-first sync status update
        val pendingSync = shipmentDao.getPendingSyncShipments()
        assertEquals(1, pendingSync.size)

        shipmentDao.updateSyncStatus("EXP-TEST-001", isPending = false, status = "SYNCED", syncedAt = 12345678L)
        val afterSync = shipmentDao.getShipmentById("EXP-TEST-001")
        assertFalse(afterSync?.isSyncPending ?: true)
        assertEquals("SYNCED", afterSync?.syncStatus)
        assertEquals(12345678L, afterSync?.lastSyncedAt)

        // Update status
        shipmentDao.updateShipmentStatus("EXP-TEST-001", ShipmentStatus.SHIPPED, System.currentTimeMillis())
        val updated = shipmentDao.getShipmentById("EXP-TEST-001")
        assertEquals(ShipmentStatus.SHIPPED, updated?.status)

        // Delete
        shipmentDao.deleteShipmentById("EXP-TEST-001")
        assertNull(shipmentDao.getShipmentById("EXP-TEST-001"))
    }

    // ==========================================
    // 2. PRODUCTS ENTITY & DAO TESTS
    // ==========================================
    @Test
    fun testProductEntityAndDaoLocalPersistence() = runBlocking {
        val product = Product(
            name = "Ceylon Cinnamon Quills",
            description = "Grade C5 Special pure Ceylon cinnamon",
            hsCode = "0906.11.0000",
            countryOfOrigin = "Sri Lanka",
            defaultUnitWeightKg = 0.5,
            defaultCartonSize = "40x30x25 cm",
            unitsPerCarton = 40,
            defaultCbmPerCarton = 0.03,
            category = "Spices",
            barcode = "794820194820",
            unitPriceUsd = 12.50,
            isSyncPending = true
        )

        // Insert
        val generatedId = productDao.insertProduct(product)
        assertTrue(generatedId > 0)

        // Read by ID
        val retrieved = productDao.getProductById(generatedId)
        assertNotNull(retrieved)
        assertEquals("Ceylon Cinnamon Quills", retrieved?.name)
        assertEquals("0906.11.0000", retrieved?.hsCode)
        assertEquals(12.50, retrieved?.unitPriceUsd ?: 0.0, 0.001)
        assertEquals("794820194820", retrieved?.barcode)

        // Read by HS Code
        val byHsCode = productDao.getProductByHsCode("0906.11.0000")
        assertNotNull(byHsCode)
        assertEquals(generatedId, byHsCode?.id)

        // Filter by category Flow
        val spices = productDao.getProductsByCategoryFlow("Spices").first()
        assertEquals(1, spices.size)

        // Search Flow
        val search = productDao.searchProductsFlow("Cinnamon").first()
        assertEquals(1, search.size)

        // Offline-first sync Flow
        val pendingSyncProds = productDao.getPendingSyncProductsFlow().first()
        assertEquals(1, pendingSyncProds.size)

        productDao.updateProductSyncStatus(generatedId, isPending = false, timestamp = System.currentTimeMillis())
        val afterSyncProds = productDao.getPendingSyncProductsFlow().first()
        assertEquals(0, afterSyncProds.size)

        // Delete
        productDao.deleteProductById(generatedId)
        assertNull(productDao.getProductById(generatedId))
    }

    // ==========================================
    // 3. DOCUMENTS ENTITY & DAO TESTS
    // ==========================================
    @Test
    fun testDocumentEntityAndDaoLocalPersistence() = runBlocking {
        val doc1 = Document(
            shipmentId = "EXP-TEST-001",
            documentType = "COMMERCIAL_INVOICE",
            documentNumber = "INV-2026-001",
            title = "Commercial Invoice - EXP-TEST-001",
            issueDate = "2026-10-01",
            status = DocumentStatus.READY,
            fileFormat = "PDF",
            filePath = "/storage/documents/INV-2026-001.pdf",
            fileSizeKb = 245,
            generatedBy = "Documentation Officer",
            isOfflineAvailable = true,
            isSyncPending = true
        )

        val doc2 = Document(
            shipmentId = "EXP-TEST-001",
            documentType = "PACKING_LIST",
            documentNumber = "PL-2026-001",
            title = "Packing List - EXP-TEST-001",
            issueDate = "2026-10-01",
            status = DocumentStatus.READY,
            fileFormat = "PDF",
            filePath = "/storage/documents/PL-2026-001.pdf",
            fileSizeKb = 190,
            generatedBy = "Warehouse Manager",
            isOfflineAvailable = true,
            isSyncPending = false
        )

        // Insert
        val doc1Id = documentDao.insertDocument(doc1)
        val doc2Id = documentDao.insertDocument(doc2)
        assertTrue(doc1Id > 0)
        assertTrue(doc2Id > 0)

        // Flow query by shipment
        val shipmentDocs = documentDao.getDocumentsForShipmentFlow("EXP-TEST-001").first()
        assertEquals(2, shipmentDocs.size)

        // Direct query by shipment
        val directDocs = documentDao.getDocumentsForShipment("EXP-TEST-001")
        assertEquals(2, directDocs.size)

        // Query by Number
        val byNumber = documentDao.getDocumentByNumber("INV-2026-001")
        assertNotNull(byNumber)
        assertEquals("Commercial Invoice - EXP-TEST-001", byNumber?.title)

        // Query by Type and Shipment
        val byType = documentDao.getDocumentByShipmentAndType("EXP-TEST-001", "PACKING_LIST")
        assertNotNull(byType)
        assertEquals("PL-2026-001", byType?.documentNumber)

        // Search Flow
        val search = documentDao.searchDocumentsFlow("Invoice").first()
        assertEquals(1, search.size)
        assertEquals("INV-2026-001", search[0].documentNumber)

        // Offline-first sync status
        val pendingDocs = documentDao.getPendingSyncDocumentsFlow().first()
        assertEquals(1, pendingDocs.size)
        assertEquals(doc1Id, pendingDocs[0].id)

        documentDao.updateDocumentSyncStatus(doc1Id, isPending = false, timestamp = 999999L)
        val pendingAfter = documentDao.getPendingSyncDocumentsFlow().first()
        assertEquals(0, pendingAfter.size)

        // Delete documents for shipment
        documentDao.deleteDocumentsForShipment("EXP-TEST-001")
        val docsAfterDelete = documentDao.getDocumentsForShipment("EXP-TEST-001")
        assertEquals(0, docsAfterDelete.size)
    }

    // ==========================================
    // 4. AUDIT LOGS ENTITY & DAO TESTS
    // ==========================================
    @Test
    fun testAuditLogEntityAndDaoLocalPersistence() = runBlocking {
        val log1 = AuditLog(
            formattedDate = "01 Oct 2026 10:00",
            userName = "Roshan Perera",
            userRole = "Documentation Officer",
            action = "Created Shipment Record",
            oldValue = "None",
            newValue = "EXP-TEST-001 Created",
            document = "Shipment Record",
            shipmentId = "EXP-TEST-001",
            isSyncPending = true,
            details = "Initial shipment creation with 500 cartons"
        )

        val log2 = AuditLog(
            formattedDate = "01 Oct 2026 11:30",
            userName = "Kumara Silva",
            userRole = "Warehouse User",
            action = "Verified Gross Weight",
            oldValue = "Estimated: 4500 kg",
            newValue = "Weighed: 4580 kg",
            document = "Packing List",
            shipmentId = "EXP-TEST-001",
            isSyncPending = true,
            details = "VGM scale weight verified"
        )

        // Insert
        val log1Id = auditLogDao.insertLog(log1)
        val log2Id = auditLogDao.insertLog(log2)
        assertTrue(log1Id > 0)
        assertTrue(log2Id > 0)

        // Flow query all logs
        val allLogs = auditLogDao.getAllLogs().first()
        assertEquals(2, allLogs.size)

        // Flow query by shipment
        val shipmentLogs = auditLogDao.getLogsForShipment("EXP-TEST-001").first()
        assertEquals(2, shipmentLogs.size)

        // Query by ID
        val singleLog = auditLogDao.getLogById(log1Id)
        assertNotNull(singleLog)
        assertEquals("Roshan Perera", singleLog?.userName)
        assertEquals("Shipment Record", singleLog?.document)

        // Query by User Flow
        val roshanLogs = auditLogDao.getLogsByUserFlow("Roshan Perera").first()
        assertEquals(1, roshanLogs.size)

        // Query by Document Flow
        val packingLogs = auditLogDao.getLogsByDocumentFlow("Packing List").first()
        assertEquals(1, packingLogs.size)

        // Search logs Flow
        val search = auditLogDao.searchLogsFlow("Weight").first()
        assertEquals(1, search.size)

        // Recent logs Flow
        val recent = auditLogDao.getRecentLogsFlow(1).first()
        assertEquals(1, recent.size)

        // Offline-first sync
        val pendingLogs = auditLogDao.getPendingSyncLogsFlow().first()
        assertEquals(2, pendingLogs.size)

        auditLogDao.markLogsAsSynced(listOf(log1Id, log2Id))
        val pendingAfterSync = auditLogDao.getPendingSyncLogsFlow().first()
        assertEquals(0, pendingAfterSync.size)

        // Delete logs for shipment
        auditLogDao.deleteLogsForShipment("EXP-TEST-001")
        val afterDelete = auditLogDao.getLogsForShipment("EXP-TEST-001").first()
        assertEquals(0, afterDelete.size)
    }
}
