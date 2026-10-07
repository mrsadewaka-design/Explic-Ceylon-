package com.example

import com.example.data.model.*
import com.example.engine.ValidationEngine
import org.junit.Assert.*
import org.junit.Test

class ExportValidationTest {

    private fun createStandardShipment(): Shipment {
        return Shipment(
            id = "ECGMIX_092826_016",
            reference = "EXP-2026-US-016",
            customer = "OUTLAW DIP COMPANY INC.",
            poNumber = "PO-ODC-9842",
            invoiceNumber = "EXP-INV-2026-092",
            invoiceDate = "2026-09-28",
            currency = "USD",
            shippingMode = ShippingMode.SEA,
            portOfLoading = "Colombo, Sri Lanka",
            portOfDischarge = "New York, USA",
            placeOfDelivery = "820 Grand Blvd, Deer Park, NY 11729, USA",
            vessel = "CMA CGM MEXICO",
            voyage = "1TUIUS1MA",
            etd = "2026-10-08",
            containerNumber = "EMCU6409544/20",
            masterBlNumber = "CMDU10294821",
            amsBlNumber = "AMS-CMDU-84920",
            status = ShipmentStatus.DOCUMENTATION
        )
    }

    private fun createStandardProducts(): List<ShipmentProduct> {
        return listOf(
            ShipmentProduct(
                id = 1L,
                shipmentId = "ECGMIX_092826_016",
                productId = 1L,
                productName = "Ice Hockey Pucks",
                hsCode = "9506.99.2580",
                description = "Vulcanized rubber hockey pucks",
                cartons = 143,
                unitsPerCarton = 50,
                totalUnits = 7150,
                individualUnitWeightKg = 0.170,
                totalNetWeightKg = 1215.5,
                cartonGrossWeightKg = 9.20,
                totalGrossWeightKg = 1315.6,
                cbmPerCarton = 0.018,
                totalCbm = 2.574,
                countryOfOrigin = "Sri Lanka"
            ),
            ShipmentProduct(
                id = 2L,
                shipmentId = "ECGMIX_092826_016",
                productId = 2L,
                productName = "Tin Lids",
                hsCode = "8309.90.0085",
                description = "Tin container lids",
                cartons = 120,
                unitsPerCarton = 250,
                totalUnits = 30000,
                individualUnitWeightKg = 0.020,
                totalNetWeightKg = 600.0,
                cartonGrossWeightKg = 5.60,
                totalGrossWeightKg = 672.0,
                cbmPerCarton = 0.0175,
                totalCbm = 2.100,
                countryOfOrigin = "Sri Lanka"
            ),
            ShipmentProduct(
                id = 3L,
                shipmentId = "ECGMIX_092826_016",
                productId = 3L,
                productName = "Plastic Cups",
                hsCode = "3924.10.90",
                description = "Polypropylene cups",
                cartons = 110,
                unitsPerCarton = 100,
                totalUnits = 11000,
                individualUnitWeightKg = 0.050,
                totalNetWeightKg = 550.0,
                cartonGrossWeightKg = 5.80,
                totalGrossWeightKg = 638.0,
                cbmPerCarton = 0.036,
                totalCbm = 3.960,
                countryOfOrigin = "Sri Lanka"
            ),
            ShipmentProduct(
                id = 4L,
                shipmentId = "ECGMIX_092826_016",
                productId = 4L,
                productName = "Sho Koi Fish Feed",
                hsCode = "2309.90.10",
                description = "Koi floating pellet feed",
                cartons = 72,
                unitsPerCarton = 10,
                totalUnits = 720,
                individualUnitWeightKg = 1.000,
                totalNetWeightKg = 720.0,
                cartonGrossWeightKg = 10.50,
                totalGrossWeightKg = 756.0,
                cbmPerCarton = 0.0375,
                totalCbm = 2.700,
                countryOfOrigin = "Sri Lanka"
            ),
            ShipmentProduct(
                id = 5L,
                shipmentId = "ECGMIX_092826_016",
                productId = 5L,
                productName = "Dog Food Clusters",
                hsCode = "2309.10.90",
                description = "Air-dried canine clusters",
                cartons = 60,
                unitsPerCarton = 20,
                totalUnits = 1200,
                individualUnitWeightKg = 0.500,
                totalNetWeightKg = 600.0,
                cartonGrossWeightKg = 10.80,
                totalGrossWeightKg = 648.0,
                cbmPerCarton = 0.048,
                totalCbm = 2.880,
                countryOfOrigin = "Sri Lanka"
            )
        )
    }

    private fun createStandardPallets(): List<Pallet> {
        return listOf(
            Pallet(id = 1L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 1", cartons = 58, weightKg = 558.6, cbm = 1.58),
            Pallet(id = 2L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 2", cartons = 58, weightKg = 558.6, cbm = 1.58),
            Pallet(id = 3L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 3", cartons = 57, weightKg = 441.4, cbm = 1.55),
            Pallet(id = 4L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 4", cartons = 60, weightKg = 361.0, cbm = 1.52),
            Pallet(id = 5L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 5", cartons = 55, weightKg = 344.0, cbm = 1.62),
            Pallet(id = 6L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 6", cartons = 55, weightKg = 344.0, cbm = 1.62),
            Pallet(id = 7L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 7", cartons = 52, weightKg = 428.6, cbm = 1.60),
            Pallet(id = 8L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 8", cartons = 54, weightKg = 597.4, cbm = 1.65),
            Pallet(id = 9L, shipmentId = "ECGMIX_092826_016", palletNumber = "Pallet 9", cartons = 56, weightKg = 619.0, cbm = 1.68)
        )
    }

    private fun createStandardInvoice(shipment: Shipment): CommercialInvoice {
        return CommercialInvoice(
            shipmentId = shipment.id,
            invoiceNumber = shipment.invoiceNumber,
            invoiceDate = shipment.invoiceDate,
            exporterName = "EXPLIC CEYLON (PRIVATE) LIMITED",
            exporterAddress = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            buyerName = shipment.customer,
            buyerAddress = shipment.placeOfDelivery,
            consigneeName = shipment.customer,
            consigneeAddress = shipment.placeOfDelivery,
            currency = shipment.currency,
            paymentTerms = "T/T 30 Days Net from B/L Date",
            freight = 2850.0,
            insurance = 380.0,
            totalAmount = 45880.0
        )
    }

    private fun createStandardPackingList(shipment: Shipment): PackingList {
        return PackingList(
            shipmentId = shipment.id,
            consignee = shipment.customer,
            consigneeAddress = shipment.placeOfDelivery,
            shipper = "EXPLIC CEYLON (PRIVATE) LIMITED",
            shipperAddress = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            piNumber = "PI-EXP-2026-092",
            date = shipment.invoiceDate,
            portOfLoading = shipment.portOfLoading,
            portOfDischarge = shipment.portOfDischarge,
            containerNumber = shipment.containerNumber,
            sealNumber = "SL-940284",
            totalCartons = 505,
            totalUnits = 50070,
            totalNetWeightKg = 3685.5,
            totalGrossWeightKg = 4029.6,
            totalCbm = 14.214,
            totalPallets = 9
        )
    }

    private fun createDiscrepantIsf(shipment: Shipment): IsfFiling {
        return IsfFiling(
            shipmentId = shipment.id,
            manufacturerNameAndAddress = "Explic Ceylon (Pvt) Ltd, Dehiwala, Sri Lanka",
            sellerNameAndAddress = "Explic Ceylon (Pvt) Ltd, Dehiwala, Sri Lanka",
            consigneeNameAndAddress = "Outlaw Dip Company Inc., Deer Park, NY",
            shipToNameAndAddress = "Outlaw Dip Company Inc., Deer Park, NY",
            containerStuffingLocation = "Ceylon Container Terminal, Colombo 15",
            consolidatorNameAndAddress = "CMA CGM Lanka (Pvt) Ltd, Colombo 02",
            descriptionOfGoods = "Ice Hockey Pucks, Tin Lids, Plastic Cups & Koi Feed",
            productCodeHtsCode = "9506.99.2580 / 2309.90.10",
            countryOfOrigin = "Sri Lanka",
            purchaseOrderNumber = shipment.poNumber,
            importerOfRecordNumber = "11-384029100",
            vessel = shipment.vessel,
            voyage = shipment.voyage,
            etd = shipment.etd,
            masterBlNumber = shipment.masterBlNumber,
            amsBlNumber = shipment.amsBlNumber,
            containerNumber = shipment.containerNumber,
            cartonsCount = 608 // DISCREPANCY: 608 vs 505 cartons
        )
    }

    private fun createDiscrepantBl(shipment: Shipment): BlInstructions {
        return BlInstructions(
            shipmentId = shipment.id,
            shipper = "EXPLIC CEYLON (PRIVATE) LIMITED\nColombo, Sri Lanka",
            consignee = "OUTLAW DIP COMPANY INC.\nDeer Park, NY, USA",
            firstNotifyParty = "OUTLAW DIP COMPANY INC.\nDeer Park, NY, USA",
            secondNotifyParty = "SAME AS CONSIGNEE",
            vessel = shipment.vessel,
            voyageNumber = shipment.voyage,
            placeOfDelivery = shipment.placeOfDelivery,
            portOfLoading = shipment.portOfLoading,
            portOfDischarge = shipment.portOfDischarge,
            marksAndNumbers = "OUTLAW / DEER PARK / NY / C/NO. 1-505",
            hsNumber = "9506.99 / 2309.90",
            kindOfPackages = "9 Pallets / 505 Cartons",
            descriptionOfGoods = "505 CARTONS ON 9 PALLETS CONTAINING:\n- 333 CTNS ICE HOCKEY PUCKS (Note: Discrepancy with PL 143)\n- 72 CTNS KOI FISH FEED",
            grossWeightKg = 0.0, // DISCREPANCY: Missing gross weight (0.0 kg)
            measurementCbm = 14.214,
            containerNumber = shipment.containerNumber
        )
    }

    private val required14Fields = listOf(
        "consignee",
        "shipper",
        "product",
        "hs code",
        "cartons",
        "pallets",
        "weights",
        "cbm",
        "container number",
        "vessel",
        "voyage",
        "etd",
        "ports",
        "po number"
    )

    // =========================================================================
    // 1. TEST ALL 4 CATEGORIES EXIST & HAVE ALL 14 FIELDS TESTED
    // =========================================================================

    @Test
    fun testCategory1_InvoiceVsPackingList_TestsAll14Fields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val invPlItems = summary.items.filter { it.category == ValidationEngine.CATEGORY_INV_VS_PL }

        assertEquals("Invoice vs PL must validate all 14 fields", 14, invPlItems.size)
        val fieldsChecked = invPlItems.map { it.fieldName.lowercase() }
        for (requiredField in required14Fields) {
            assertTrue(
                "Category '$${ValidationEngine.CATEGORY_INV_VS_PL}' must evaluate '$requiredField'",
                fieldsChecked.any { it.contains(requiredField) }
            )
        }
    }

    @Test
    fun testCategory2_PackingListVsIsf_TestsAll14Fields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val plIsfItems = summary.items.filter { it.category == ValidationEngine.CATEGORY_PL_VS_ISF }

        assertEquals("Packing List vs ISF must validate all 14 fields", 14, plIsfItems.size)
        val fieldsChecked = plIsfItems.map { it.fieldName.lowercase() }
        for (requiredField in required14Fields) {
            assertTrue(
                "Category '${ValidationEngine.CATEGORY_PL_VS_ISF}' must evaluate '$requiredField'",
                fieldsChecked.any { it.contains(requiredField) }
            )
        }
    }

    @Test
    fun testCategory3_PackingListVsBl_TestsAll14Fields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val plBlItems = summary.items.filter { it.category == ValidationEngine.CATEGORY_PL_VS_BL }

        assertEquals("Packing List vs B/L must validate all 14 fields", 14, plBlItems.size)
        val fieldsChecked = plBlItems.map { it.fieldName.lowercase() }
        for (requiredField in required14Fields) {
            assertTrue(
                "Category '${ValidationEngine.CATEGORY_PL_VS_BL}' must evaluate '$requiredField'",
                fieldsChecked.any { it.contains(requiredField) }
            )
        }
    }

    @Test
    fun testCategory4_IsfVsBl_TestsAll14Fields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val isfBlItems = summary.items.filter { it.category == ValidationEngine.CATEGORY_ISF_VS_BL }

        assertEquals("ISF vs B/L must validate all 14 fields", 14, isfBlItems.size)
        val fieldsChecked = isfBlItems.map { it.fieldName.lowercase() }
        for (requiredField in required14Fields) {
            assertTrue(
                "Category '${ValidationEngine.CATEGORY_ISF_VS_BL}' must evaluate '$requiredField'",
                fieldsChecked.any { it.contains(requiredField) }
            )
        }
    }

    // =========================================================================
    // 2. TEST TOTAL COVERAGE: 56 AUTOMATED RULES (14 FIELDS x 4 CATEGORIES)
    // =========================================================================

    @Test
    fun testTotalRuleCountIsExactlyFiftySix() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        assertEquals("Validation engine must evaluate 56 total rules (14 fields x 4 categories)", 56, summary.items.size)
    }

    // =========================================================================
    // 3. EVERY MISMATCH MUST DISPLAY ALL 7 REQUIRED METADATA ELEMENTS:
    // Field, Document A, Value A, Document B, Value B, Severity, Recommended action
    // =========================================================================

    @Test
    fun testEveryMismatchShowsAllRequiredMetadataProperties() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val mismatches = summary.items.filter { it.severity != ValidationSeverity.MATCH }

        assertTrue("Mismatches must exist in the sample shipment", mismatches.isNotEmpty())

        for (item in mismatches) {
            assertNotNull("Field name must not be null", item.fieldName)
            assertTrue("Field name must not be blank: $item", item.fieldName.isNotBlank())

            assertNotNull("Document A must not be null", item.documentA)
            assertTrue("Document A must not be blank: $item", item.documentA.isNotBlank())

            assertNotNull("Value A must not be null", item.valueA)
            assertTrue("Value A must not be blank: $item", item.valueA.isNotBlank())

            assertNotNull("Document B must not be null", item.documentB)
            assertTrue("Document B must not be blank: $item", item.documentB.isNotBlank())

            assertNotNull("Value B must not be null", item.valueB)
            assertTrue("Value B must not be blank: $item", item.valueB.isNotBlank())

            assertTrue("Severity must be WARNING or ERROR", item.severity == ValidationSeverity.WARNING || item.severity == ValidationSeverity.ERROR)

            assertNotNull("Recommended action must not be null", item.recommendedAction)
            assertTrue("Recommended action must not be blank: $item", item.recommendedAction.isNotBlank())
            assertFalse("Recommended action for mismatch must not be generic 'No action required'", item.recommendedAction.equals("No action required", ignoreCase = true))
        }
    }

    // =========================================================================
    // 4. TEST SPECIFIC CRITICAL MISMATCH: PACKING LIST VS ISF CARTONS (505 vs 608)
    // =========================================================================

    @Test
    fun testPlVsIsf_CartonMismatch_DisplaysStructuredFields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment) // 608 cartons vs PL 505 cartons
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val item = summary.items.firstOrNull {
            it.category == ValidationEngine.CATEGORY_PL_VS_ISF && it.fieldName == "Cartons"
        }

        assertNotNull("Cartons rule must trigger in PL vs ISF", item)
        assertEquals("Cartons", item!!.fieldName)
        assertEquals("Packing List", item.documentA)
        assertTrue("Value A must contain 505 cartons", item.valueA.contains("505"))
        assertEquals("ISF Filing", item.documentB)
        assertTrue("Value B must contain 608 cartons", item.valueB.contains("608"))
        assertEquals(ValidationSeverity.ERROR, item.severity)
        assertTrue("Recommended action must specify amending ISF to match 505 cartons", item.recommendedAction.contains("505"))
    }

    // =========================================================================
    // 5. TEST SPECIFIC CRITICAL MISMATCH: PACKING LIST VS B/L PRODUCT QUANTITY (143 vs 333)
    // =========================================================================

    @Test
    fun testPlVsBl_ProductQuantityMismatch_DisplaysStructuredFields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment) // 333 ctns vs 143 ctns in PL

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val item = summary.items.firstOrNull {
            it.category == ValidationEngine.CATEGORY_PL_VS_BL && it.fieldName == "Product"
        }

        assertNotNull("Product rule must trigger in PL vs B/L", item)
        assertEquals("Product", item!!.fieldName)
        assertEquals("Packing List", item.documentA)
        assertTrue("Value A must contain 143 cartons", item.valueA.contains("143"))
        assertEquals("B/L Instructions", item.documentB)
        assertTrue("Value B must contain 333 cartons", item.valueB.contains("333"))
        assertEquals(ValidationSeverity.ERROR, item.severity)
        assertTrue("Recommended action must instruct correcting B/L from 333 to 143", item.recommendedAction.contains("143") && item.recommendedAction.contains("333"))
    }

    // =========================================================================
    // 6. TEST SPECIFIC CRITICAL MISMATCH: PACKING LIST VS B/L GROSS WEIGHT MISSING
    // =========================================================================

    @Test
    fun testPlVsBl_GrossWeightMissing_DisplaysStructuredFields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment) // grossWeightKg = 0.0

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val item = summary.items.firstOrNull {
            it.category == ValidationEngine.CATEGORY_PL_VS_BL && it.fieldName == "Weights"
        }

        assertNotNull("Weights rule must trigger in PL vs B/L", item)
        assertEquals("Weights", item!!.fieldName)
        assertEquals("Packing List", item.documentA)
        assertTrue("Value A must contain 4029.6 kg", item.valueA.contains("4029.6"))
        assertEquals("B/L Instructions", item.documentB)
        assertTrue("Value B must flag missing from B/L", item.valueB.contains("Missing"))
        assertEquals(ValidationSeverity.ERROR, item.severity)
        assertTrue("Recommended action must mention SOLAS VGM or gross weight", item.recommendedAction.contains("SOLAS VGM") || item.recommendedAction.contains("gross weight"))
    }

    // =========================================================================
    // 7. TEST SPECIFIC CRITICAL MISMATCH: ISF VS B/L CARTONS (608 vs 505)
    // =========================================================================

    @Test
    fun testIsfVsBl_CartonMismatch_DisplaysStructuredFields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment) // 608 ctns
        val bl = createDiscrepantBl(shipment) // 505 ctns on B/L

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val item = summary.items.firstOrNull {
            it.category == ValidationEngine.CATEGORY_ISF_VS_BL && it.fieldName == "Cartons"
        }

        assertNotNull("Cartons rule must trigger in ISF vs B/L", item)
        assertEquals("Cartons", item!!.fieldName)
        assertEquals("ISF Filing", item.documentA)
        assertTrue("Value A must show 608 cartons", item.valueA.contains("608"))
        assertEquals("B/L Instructions", item.documentB)
        assertTrue("Value B must show 505 cartons", item.valueB.contains("505"))
        assertEquals(ValidationSeverity.ERROR, item.severity)
        assertTrue("Recommended action must specify amending ISF count", item.recommendedAction.contains("ISF 10+2"))
    }

    // =========================================================================
    // 8. TEST SPECIFIC CRITICAL MISMATCH: ISF VS B/L GROSS WEIGHT MISSING
    // =========================================================================

    @Test
    fun testIsfVsBl_GrossWeightMissing_DisplaysStructuredFields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val item = summary.items.firstOrNull {
            it.category == ValidationEngine.CATEGORY_ISF_VS_BL && it.fieldName == "Weights"
        }

        assertNotNull("Weights rule must trigger in ISF vs B/L", item)
        assertEquals("Weights", item!!.fieldName)
        assertEquals("ISF Filing", item.documentA)
        assertEquals("B/L Instructions", item.documentB)
        assertTrue("Value B must state missing from B/L", item.valueB.contains("Missing"))
        assertEquals(ValidationSeverity.ERROR, item.severity)
        assertTrue("Recommended action must specify populating certified gross weight", item.recommendedAction.contains("gross weight") || item.recommendedAction.contains("VGM"))
    }

    // =========================================================================
    // 9. TEST RULE: "DO NOT AUTOMATICALLY MODIFY CONFLICTING DATA"
    // =========================================================================

    @Test
    fun testEngineNeverModifiesConflictingDataAutomatically() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment)

        val beforeIsfCartons = isf.cartonsCount // 608
        val beforeBlGross = bl.grossWeightKg   // 0.0
        val beforeBlGoods = bl.descriptionOfGoods // Contains 333 ctns
        val beforePlCartons = packingList.totalCartons // 505
        val beforeShipmentStatus = shipment.status

        // Run validation repeatedly
        for (i in 1..5) {
            val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
            assertNotNull(summary)
        }

        // Assert all objects remain 100% unaltered
        assertEquals("ISF cartons count must remain 608 (never automatically overwritten)", beforeIsfCartons, isf.cartonsCount)
        assertEquals("B/L gross weight must remain 0.0 (never automatically overwritten)", beforeBlGross, bl.grossWeightKg, 0.0001)
        assertEquals("B/L goods description must remain unchanged", beforeBlGoods, bl.descriptionOfGoods)
        assertEquals("Packing List cartons must remain 505", beforePlCartons, packingList.totalCartons)
        assertEquals("Shipment status must remain unchanged", beforeShipmentStatus, shipment.status)
    }

    // =========================================================================
    // 10. TEST INJECTED MISMATCHES ACROSS ALL 14 FIELDS
    // =========================================================================

    @Test
    fun testInjectedConsigneeMismatch_TriggersErrorWithCompleteFields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment).copy(consigneeName = "OTHER BUYER CORP")
        val packingList = createStandardPackingList(shipment).copy(consignee = "DIFFERENT RECEIVER LLC")
        val isf = createDiscrepantIsf(shipment).copy(cartonsCount = 505)
        val bl = createDiscrepantBl(shipment).copy(grossWeightKg = 4029.6, descriptionOfGoods = "505 cartons")

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val item = summary.items.firstOrNull { it.category == ValidationEngine.CATEGORY_INV_VS_PL && it.fieldName == "Consignee" }

        assertNotNull(item)
        assertEquals(ValidationSeverity.ERROR, item!!.severity)
        assertEquals("Commercial Invoice", item.documentA)
        assertTrue(item.valueA.contains("OTHER BUYER"))
        assertEquals("Packing List", item.documentB)
        assertTrue(item.valueB.contains("DIFFERENT RECEIVER"))
        assertTrue(item.recommendedAction.isNotBlank())
    }

    @Test
    fun testInjectedContainerMismatch_TriggersErrorWithCompleteFields() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment).copy(containerNumber = "TGHU9821430/40")
        val isf = createDiscrepantIsf(shipment).copy(containerNumber = "MSCU1234567/20")
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val item = summary.items.firstOrNull { it.category == ValidationEngine.CATEGORY_PL_VS_ISF && it.fieldName == "Container Number" }

        assertNotNull(item)
        assertEquals(ValidationSeverity.ERROR, item!!.severity)
        assertEquals("Packing List", item.documentA)
        assertEquals("TGHU9821430/40", item.valueA)
        assertEquals("ISF Filing", item.documentB)
        assertEquals("MSCU1234567/20", item.valueB)
        assertTrue("Recommended action must mention container stuffing tally", item.recommendedAction.contains("container"))
    }

    @Test
    fun testInjectedVesselAndVoyageMismatch_TriggersErrorWithCompleteFields() {
        val shipment = createStandardShipment().copy(vessel = "MSC OSCAR", voyage = "V-2026-X")
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)
        val isf = createDiscrepantIsf(shipment).copy(vessel = "EVER GIVEN", voyage = "EG-9988")
        val bl = createDiscrepantBl(shipment)

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)

        val vesselItem = summary.items.firstOrNull { it.category == ValidationEngine.CATEGORY_PL_VS_ISF && it.fieldName == "Vessel" }
        assertNotNull(vesselItem)
        assertEquals(ValidationSeverity.ERROR, vesselItem!!.severity)
        assertEquals("MSC OSCAR", vesselItem.valueA)
        assertEquals("EVER GIVEN", vesselItem.valueB)
        assertTrue(vesselItem.recommendedAction.isNotBlank())

        val voyageItem = summary.items.firstOrNull { it.category == ValidationEngine.CATEGORY_PL_VS_ISF && it.fieldName == "Voyage" }
        assertNotNull(voyageItem)
        assertEquals(ValidationSeverity.ERROR, voyageItem!!.severity)
        assertEquals("V-2026-X", voyageItem.valueA)
        assertEquals("EG-9988", voyageItem.valueB)
        assertTrue(voyageItem.recommendedAction.isNotBlank())
    }

    @Test
    fun testInjectedPortsMismatch_TriggersErrorWithCompleteFields() {
        val shipment = createStandardShipment().copy(portOfLoading = "Colombo", portOfDischarge = "New York")
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment).copy(portOfLoading = "Colombo", portOfDischarge = "New York")
        val isf = createDiscrepantIsf(shipment)
        val bl = createDiscrepantBl(shipment).copy(portOfLoading = "Singapore", portOfDischarge = "Rotterdam")

        val summary = ValidationEngine.validateShipment(shipment, products, pallets, invoice, packingList, isf, bl)
        val portsItem = summary.items.firstOrNull { it.category == ValidationEngine.CATEGORY_PL_VS_BL && it.fieldName == "Ports" }

        assertNotNull(portsItem)
        assertEquals(ValidationSeverity.ERROR, portsItem!!.severity)
        assertTrue(portsItem.valueA.contains("New York"))
        assertTrue(portsItem.valueB.contains("Rotterdam"))
        assertTrue(portsItem.recommendedAction.isNotBlank())
    }

    // =========================================================================
    // 11. TEST HARMONIZED DOCUMENTS: 0 ERRORS AND CAN MARK COMPLETE
    // =========================================================================

    @Test
    fun testHarmonizedDocuments_ZeroErrorsAndCanMarkComplete() {
        val shipment = createStandardShipment()
        val products = createStandardProducts()
        val pallets = createStandardPallets()
        val invoice = createStandardInvoice(shipment)
        val packingList = createStandardPackingList(shipment)

        // Harmonize all documents to ensure complete consistency
        val harmonizedPackingList = createStandardPackingList(shipment).copy(
            piNumber = shipment.poNumber
        )
        val harmonizedIsf = createDiscrepantIsf(shipment).copy(
            cartonsCount = 505, // Harmonized from 608 to 505
            purchaseOrderNumber = shipment.poNumber,
            totalGrossWeightKg = 4029.6
        )
        val harmonizedBl = createDiscrepantBl(shipment).copy(
            descriptionOfGoods = "505 CARTONS ON 9 PALLETS CONTAINING 143 CTNS PUCKS AND 72 CTNS KOI FEED",
            grossWeightKg = 4029.6, // Harmonized from 0.0 to 4029.6 kg
            hsNumber = "9506.99.2580"
        )

        val summary = ValidationEngine.validateShipment(
            shipment = shipment,
            products = products,
            pallets = pallets,
            invoice = invoice,
            packingList = harmonizedPackingList,
            isf = harmonizedIsf,
            bl = harmonizedBl
        )

        val errors = summary.items.filter { it.severity == ValidationSeverity.ERROR }
        val warnings = summary.items.filter { it.severity == ValidationSeverity.WARNING }
        assertEquals("Zero errors expected for harmonized documents: $errors", 0, summary.totalErrors)
        assertEquals("Zero warnings expected for harmonized documents: $warnings", 0, summary.totalWarnings)
        assertTrue("canMarkComplete must be true when 0 errors and 0 warnings exist", summary.canMarkComplete)
    }
}
