package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.SeedData
import com.example.data.model.*
import com.example.data.repository.ExportRepository
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
class WarehouseModuleTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ExportRepository

    private val testUser = User(
        id = "usr_04",
        username = "kumara.s",
        fullName = "Kumara Silva",
        email = "warehouse@explicceylon.com",
        role = UserRole.WAREHOUSE_USER,
        department = "Dehiwala Logistics Hub",
        telephone = "+94 77 456 7890"
    )

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        SeedData.populateInitialData(database)
        repository = ExportRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testReceiveGoodsUpdatesStockAndShipmentAndCreatesAuditLog() = runBlocking {
        val initialStockList = repository.allWarehouseStock.first()
        val hockeyStock = initialStockList.find { it.productName.contains("Ice Hockey Pucks", ignoreCase = true) }
        assertNotNull(hockeyStock)
        val initialAvailable = hockeyStock!!.availableCartons

        val initialAuditCount = repository.allAuditLogs.first().size

        // Receive 50 more cartons of Ice Hockey Pucks linked to ECGMIX_092826_016
        val receipt = GoodsReceipt(
            receiptNumber = "GRN-2026-TEST01",
            productId = hockeyStock.productId,
            productName = hockeyStock.productName,
            supplierName = "Explic Ceylon Factory",
            shipmentId = "ECGMIX_092826_016",
            cartonsReceived = 50,
            unitsPerCarton = 50,
            totalUnits = 2500,
            cartonGrossWeightKg = 9.20,
            totalGrossWeightKg = 460.0,
            lengthCm = 40.0,
            widthCm = 30.0,
            heightCm = 15.0,
            totalCbm = 0.90,
            batchNumber = "LOT-2026-HP-TEST",
            barcode = "EXP-PUCK-9506",
            locationBay = "Bay A-01",
            receivedDate = "05 Oct 2026",
            receivedBy = testUser.fullName,
            notes = "Test receiving goods"
        )

        repository.receiveGoods(receipt, autoUpdateShipment = true, user = testUser)

        // 1. Stock item is updated
        val updatedStockList = repository.allWarehouseStock.first()
        val updatedHockey = updatedStockList.find { it.productId == hockeyStock.productId }
        assertNotNull(updatedHockey)
        assertEquals(initialAvailable + 50, updatedHockey!!.availableCartons)

        // 2. Linked shipment product is updated
        val shipmentProducts = repository.getShipmentProducts("ECGMIX_092826_016").first()
        val puckShipmentProduct = shipmentProducts.find { it.productName.contains("Ice Hockey Pucks", ignoreCase = true) }
        assertNotNull(puckShipmentProduct)
        assertTrue(puckShipmentProduct!!.cartons >= 50)

        // 3. Audit trail is preserved
        val auditLogs = repository.allAuditLogs.first()
        assertTrue(auditLogs.size > initialAuditCount)
        val receiptLog = auditLogs.find { it.action.contains("Received Goods") }
        assertNotNull(receiptLog)
        assertEquals("Kumara Silva", receiptLog!!.userName)
        assertTrue(receiptLog.newValue.contains("50 ctns"))
    }

    @Test
    fun testAssignCartonsToPalletUpdatesPalletAndPreservesAuditTrail() = runBlocking {
        val initialPallets = repository.getPallets("ECGMIX_092826_016").first()
        assertTrue(initialPallets.isNotEmpty())
        val firstPallet = initialPallets.first()
        val initialCartons = firstPallet.cartons

        val initialAuditCount = repository.allAuditLogs.first().size

        // Assign 10 cartons to this pallet
        repository.assignCartonsToPallet(
            palletId = firstPallet.id,
            shipmentId = "ECGMIX_092826_016",
            stockItemId = null,
            productName = "Ice Hockey Pucks",
            cartonsToAssign = 10,
            cartonGrossWeightKg = 9.20,
            cbmPerCarton = 0.018,
            user = testUser
        )

        // 1. Pallet updated
        val updatedPallets = repository.getPallets("ECGMIX_092826_016").first()
        val updatedPallet = updatedPallets.find { it.id == firstPallet.id }
        assertNotNull(updatedPallet)
        assertEquals(initialCartons + 10, updatedPallet!!.cartons)
        assertTrue(updatedPallet.weightKg > firstPallet.weightKg)

        // 2. Audit log created
        val auditLogs = repository.allAuditLogs.first()
        assertTrue(auditLogs.size > initialAuditCount)
        val assignLog = auditLogs.find { it.action == "Assigned Cartons to Pallet" }
        assertNotNull(assignLog)
        assertEquals("Kumara Silva", assignLog!!.userName)
    }

    @Test
    fun testRecordCartonMeasurementsUpdatesStockAndPreservesAuditTrail() = runBlocking {
        val stockItems = repository.allWarehouseStock.first()
        val firstStock = stockItems.first()

        repository.recordCartonMeasurements(
            stockItemId = firstStock.id,
            lengthCm = 55.0,
            widthCm = 35.0,
            heightCm = 25.0,
            cartonGrossWeightKg = 12.50,
            cartonQuantity = 80,
            locationBay = "Bay B-09",
            user = testUser
        )

        val updatedItems = repository.allWarehouseStock.first()
        val updated = updatedItems.find { it.id == firstStock.id }
        assertNotNull(updated)
        assertEquals(55.0, updated!!.lengthCm, 0.01)
        assertEquals(35.0, updated.widthCm, 0.01)
        assertEquals(25.0, updated.heightCm, 0.01)
        assertEquals(12.50, updated.cartonGrossWeightKg, 0.01)
        assertEquals(80, updated.availableCartons)
        assertEquals("Bay B-09", updated.locationBay)

        // Audit trail
        val auditLogs = repository.allAuditLogs.first()
        val measureLog = auditLogs.find { it.action == "Recorded Carton Dimensions & Weight" }
        assertNotNull(measureLog)
        assertEquals("Kumara Silva", measureLog!!.userName)
    }

    @Test
    fun testRecordDispatchSetsShipmentLoadedGeneratesDeliveryNoteAndAuditTrail() = runBlocking {
        val initialAuditCount = repository.allAuditLogs.first().size

        val dispatch = WarehouseDispatch(
            dispatchNumber = "DSP-2026-TEST99",
            shipmentId = "ECGMIX_092826_016",
            customer = "OUTLAW DIP COMPANY INC.",
            deliveryNoteNumber = "DN-2026-TEST99",
            containerNumber = "EMCU6409544/20",
            sealNumber = "SL-940284",
            vehicleNumber = "WP-LH-8921",
            driverName = "Gamini Wijesinghe",
            driverNic = "+94 71 889 1234",
            totalCartonsDispatched = 505,
            totalPalletsDispatched = 9,
            totalGrossWeightKg = 4029.6,
            totalCbm = 14.214,
            dispatchDate = "05 Oct 2026",
            dispatchedBy = testUser.fullName
        )

        val createdDn = repository.recordDispatch(dispatch, createDeliveryNote = true, user = testUser)
        assertNotNull(createdDn)
        assertEquals("DN-2026-TEST99", createdDn!!.noteNumber)

        // 1. Shipment status updated to LOADED
        val shipment = repository.getShipmentDirect("ECGMIX_092826_016")
        assertNotNull(shipment)
        assertEquals(ShipmentStatus.LOADED, shipment!!.status)

        // 2. Delivery Note saved
        val allDns = repository.allDeliveryNotes.first()
        val foundDn = allDns.find { it.noteNumber == "DN-2026-TEST99" }
        assertNotNull(foundDn)
        assertEquals(505, foundDn!!.totalCartons)
        assertEquals(9, foundDn.totalPallets)

        // 3. Audit trail preserved
        val auditLogs = repository.allAuditLogs.first()
        assertTrue(auditLogs.size > initialAuditCount)
        val dispatchLog = auditLogs.find { it.action == "Recorded Warehouse Dispatch" }
        assertNotNull(dispatchLog)
        assertEquals("Kumara Silva", dispatchLog!!.userName)
        assertTrue(dispatchLog.newValue.contains("505 ctns"))
    }
}
