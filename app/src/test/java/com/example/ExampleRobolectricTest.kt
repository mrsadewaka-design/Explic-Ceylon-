package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.engine.ValidationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Explic Export Pro", appName)
    }

    @Test
    fun `validation engine detects carton mismatch between packing list and isf`() {
        val shipment = Shipment(
            id = "ECGMIX_092826_016",
            reference = "EXP-2026-US-016",
            customer = "OUTLAW DIP COMPANY INC.",
            poNumber = "PO-ODC-9842",
            invoiceNumber = "EXP-INV-2026-092",
            invoiceDate = "2026-09-28",
            portOfLoading = "Colombo, Sri Lanka",
            portOfDischarge = "New York, USA",
            placeOfDelivery = "Deer Park, NY 11729, USA",
            vessel = "CMA CGM MEXICO",
            voyage = "1TUIUS1MA",
            etd = "2026-10-08",
            containerNumber = "EMCU6409544/20",
            masterBlNumber = "CMDU10294821",
            amsBlNumber = "AMS-CMDU-84920"
        )

        val products = listOf(
            ShipmentProduct(
                shipmentId = shipment.id,
                productId = 1,
                productName = "Ice Hockey Pucks",
                hsCode = "9506.99.2580",
                description = "Rubber pucks",
                cartons = 505,
                unitsPerCarton = 50,
                totalUnits = 25250,
                individualUnitWeightKg = 0.17,
                totalNetWeightKg = 4292.5,
                cartonGrossWeightKg = 9.2,
                totalGrossWeightKg = 4646.0,
                cbmPerCarton = 0.018,
                totalCbm = 9.09,
                countryOfOrigin = "Sri Lanka"
            )
        )

        val packingList = PackingList(
            shipmentId = shipment.id,
            consignee = "OUTLAW DIP COMPANY INC.",
            consigneeAddress = "Deer Park, NY",
            shipper = "EXPLIC CEYLON (PVT) LTD",
            shipperAddress = "Dehiwala, Sri Lanka",
            piNumber = "PI-016",
            date = "2026-09-28",
            portOfLoading = "Colombo, Sri Lanka",
            portOfDischarge = "New York, USA",
            containerNumber = "EMCU6409544/20",
            totalCartons = 505,
            totalGrossWeightKg = 4646.0
        )

        // Discrepancy: ISF has 608 cartons
        val isf = IsfFiling(
            shipmentId = shipment.id,
            manufacturerNameAndAddress = "Explic Ceylon",
            sellerNameAndAddress = "Explic Ceylon",
            consigneeNameAndAddress = "OUTLAW DIP COMPANY INC.",
            shipToNameAndAddress = "Deer Park, NY",
            containerStuffingLocation = "Colombo",
            consolidatorNameAndAddress = "CMA CGM",
            descriptionOfGoods = "Ice Hockey Pucks",
            productCodeHtsCode = "9506.99.2580",
            countryOfOrigin = "Sri Lanka",
            purchaseOrderNumber = "PO-ODC-9842",
            importerOfRecordNumber = "11-384029100",
            vessel = "CMA CGM MEXICO",
            voyage = "1TUIUS1MA",
            etd = "2026-10-08",
            masterBlNumber = "CMDU10294821",
            amsBlNumber = "AMS-CMDU-84920",
            containerNumber = "EMCU6409544/20",
            cartonsCount = 608
        )

        val result = ValidationEngine.validateShipment(
            shipment = shipment,
            products = products,
            pallets = emptyList(),
            invoice = null,
            packingList = packingList,
            isf = isf,
            bl = null
        )

        val mismatchItem = result.items.find { it.fieldName.contains("Carton", ignoreCase = true) }
        assertTrue(mismatchItem != null)
        assertTrue(mismatchItem?.severity == ValidationSeverity.ERROR || mismatchItem?.severity == ValidationSeverity.WARNING)
        assertTrue(result.totalErrors > 0 || result.totalWarnings > 0)
    }
}
