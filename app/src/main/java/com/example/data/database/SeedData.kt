package com.example.data.database

import com.example.data.model.*

object SeedData {

    suspend fun populateInitialData(db: AppDatabase) {
        val userDao = db.userDao()
        val partyDao = db.partyDao()
        val productDao = db.productDao()
        val shipmentDao = db.shipmentDao()
        val palletDao = db.palletDao()
        val docDao = db.documentDao()
        val auditDao = db.auditLogDao()
        val notifDao = db.notificationDao()

        // 1. Initial Users
        val users = listOf(
            User(
                id = "usr_01",
                username = "roshan.p",
                fullName = "Roshan Perera",
                email = "roshan@explicceylon.com",
                role = UserRole.DOCUMENTATION_OFFICER,
                department = "Export Documentation & Compliance",
                telephone = "+94 77 123 4567"
            ),
            User(
                id = "usr_02",
                username = "nimal.f",
                fullName = "Nimal Fernando",
                email = "admin@explicceylon.com",
                role = UserRole.ADMIN,
                department = "Executive Management",
                telephone = "+94 77 234 5678"
            ),
            User(
                id = "usr_03",
                username = "priyantha.j",
                fullName = "Priyantha Jayasuriya",
                email = "export.mgr@explicceylon.com",
                role = UserRole.EXPORT_MANAGER,
                department = "Global Logistics & Shipping",
                telephone = "+94 77 345 6789"
            ),
            User(
                id = "usr_04",
                username = "kumara.s",
                fullName = "Kumara Silva",
                email = "warehouse@explicceylon.com",
                role = UserRole.WAREHOUSE_USER,
                department = "Dehiwala Logistics Hub",
                telephone = "+94 77 456 7890"
            ),
            User(
                id = "usr_05",
                username = "david.m",
                fullName = "David Miller",
                email = "dmiller@outlawdip.com",
                role = UserRole.VIEWER,
                department = "Overseas Importer (US)",
                telephone = "+1 516 666 1891"
            )
        )
        userDao.insertUsers(users)

        // 2. Parties
        val parties = listOf(
            Party(
                companyName = "EXPLIC CEYLON (PRIVATE) LIMITED",
                address = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
                country = "Sri Lanka",
                contactPerson = "Nimal Fernando",
                telephone = "+94 11 273 8920",
                email = "exports@explicceylon.com",
                taxId = "LK102948293",
                fdaRegistration = "19482019482",
                partyType = PartyType.EXPORTER
            ),
            Party(
                companyName = "EXPLIC CEYLON (PRIVATE) LIMITED",
                address = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
                country = "Sri Lanka",
                contactPerson = "Kumara Silva",
                telephone = "+94 11 273 8920",
                email = "factory@explicceylon.com",
                taxId = "LK102948293",
                fdaRegistration = "19482019482",
                partyType = PartyType.MANUFACTURER
            ),
            Party(
                companyName = "EXPLIC GLOBAL EXPORTS (PVT) LTD",
                address = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
                country = "Sri Lanka",
                contactPerson = "Priyantha Jayasuriya",
                telephone = "+94 11 273 8925",
                email = "seller@explicceylon.com",
                taxId = "LK102948293",
                partyType = PartyType.SELLER
            ),
            Party(
                companyName = "OUTLAW DIP COMPANY INC.",
                address = "820 Grand Blvd, Deer Park, New York 11729, USA",
                country = "United States",
                contactPerson = "George Statler",
                telephone = "+1 516 666 1891",
                email = "gstatler@outlawdip.com",
                taxId = "US-113840291",
                importerOfRecordNumber = "11-384029100",
                partyType = PartyType.CONSIGNEE
            ),
            Party(
                companyName = "KUEHNE + NAGEL LOGISTICS USA INC.",
                address = "10 Exchange Place, 19th Floor, Jersey City, NJ 07302, USA",
                country = "United States",
                contactPerson = "Ocean Import Desk",
                telephone = "+1 201 413 5500",
                email = "ocean.import@kuehne-nagel.com",
                taxId = "US-229481023",
                partyType = PartyType.NOTIFY_PARTY
            ),
            Party(
                companyName = "OUTLAW BRANDS HOLDINGS LLC",
                address = "820 Grand Blvd, Suite B, Deer Park, New York 11729, USA",
                country = "United States",
                contactPerson = "Compliance Officer",
                telephone = "+1 516 666 1895",
                email = "ior@outlawbrands.com",
                taxId = "US-113840291",
                importerOfRecordNumber = "11-384029100",
                partyType = PartyType.IMPORTER_OF_RECORD
            ),
            Party(
                companyName = "OUTLAW DISTRIBUTION CENTER",
                address = "820 Grand Blvd, Deer Park, New York 11729, USA",
                country = "United States",
                contactPerson = "Receiving Dept",
                telephone = "+1 516 666 1891",
                email = "warehouse@outlawdip.com",
                partyType = PartyType.SHIP_TO
            ),
            Party(
                companyName = "CMA CGM LANKA (PVT) LTD",
                address = "Level 8, Hemas House, 75 Braybrooke Place, Colombo 02",
                country = "Sri Lanka",
                contactPerson = "Operations Desk",
                telephone = "+94 11 472 8000",
                email = "col.ops@cma-cgm.com",
                taxId = "LK990184719",
                partyType = PartyType.CONSOLIDATOR
            ),
            Party(
                companyName = "CEYLON CONTAINER TERMINAL STUFFING YARD",
                address = "Block C, Port of Colombo Access Road, Colombo 15",
                country = "Sri Lanka",
                contactPerson = "Yard Supervisor",
                telephone = "+94 11 242 1190",
                email = "cfs@colomboterminal.lk",
                partyType = PartyType.STUFFING_LOCATION
            )
        )
        partyDao.insertAllParties(parties)

        // 3. Products
        val products = listOf(
            Product(
                name = "Ice Hockey Pucks",
                description = "Vulcanized rubber official ice hockey pucks, standard 6oz",
                hsCode = "9506.99.2580",
                countryOfOrigin = "Sri Lanka",
                defaultUnitWeightKg = 0.170,
                defaultCartonSize = "30 x 30 x 20 cm",
                unitsPerCarton = 50,
                defaultCbmPerCarton = 0.018,
                category = "Sporting Goods"
            ),
            Product(
                name = "Tin Lids",
                description = "Embossed coated tinplate container lids with hermetic seal",
                hsCode = "8309.90.0085",
                countryOfOrigin = "Sri Lanka",
                defaultUnitWeightKg = 0.020,
                defaultCartonSize = "35 x 25 x 20 cm",
                unitsPerCarton = 250,
                defaultCbmPerCarton = 0.0175,
                category = "Packaging"
            ),
            Product(
                name = "Plastic Cups",
                description = "Food-grade injection molded polypropylene branded cups",
                hsCode = "3924.10.90",
                countryOfOrigin = "Sri Lanka",
                defaultUnitWeightKg = 0.050,
                defaultCartonSize = "40 x 30 x 30 cm",
                unitsPerCarton = 100,
                defaultCbmPerCarton = 0.036,
                category = "Plastics & Tableware"
            ),
            Product(
                name = "Sho Koi Fish Feed",
                description = "Premium spirulina enriched ornamental koi floating pellet feed",
                hsCode = "2309.90.10",
                countryOfOrigin = "Sri Lanka",
                defaultUnitWeightKg = 1.000,
                defaultCartonSize = "50 x 30 x 25 cm",
                unitsPerCarton = 10,
                defaultCbmPerCarton = 0.0375,
                category = "Aquaculture Nutrition"
            ),
            Product(
                name = "Dog Food Clusters",
                description = "Air-dried organic nutritional canine cluster nuggets",
                hsCode = "2309.10.90",
                countryOfOrigin = "Sri Lanka",
                defaultUnitWeightKg = 0.500,
                defaultCartonSize = "40 x 40 x 30 cm",
                unitsPerCarton = 20,
                defaultCbmPerCarton = 0.048,
                category = "Pet Nutrition"
            )
        )
        productDao.insertAllProducts(products)

        // 4. Sample Shipments
        val sampleShipmentId = "ECGMIX_092826_016"
        val shipments = listOf(
            Shipment(
                id = sampleShipmentId,
                reference = "EXP-2026-US-016",
                customer = "OUTLAW DIP COMPANY INC.",
                poNumber = "PO-ODC-9842",
                invoiceNumber = "EXP-INV-2026-092",
                invoiceDate = "2026-09-28",
                currency = "USD",
                shippingMode = ShippingMode.SEA,
                portOfLoading = "Colombo, Sri Lanka",
                portOfDischarge = "New York, USA",
                placeOfDelivery = "Deer Park, NY 11729, USA",
                vessel = "CMA CGM MEXICO",
                voyage = "1TUIUS1MA",
                etd = "2026-10-08",
                containerNumber = "EMCU6409544/20",
                masterBlNumber = "CMDU10294821",
                amsBlNumber = "AMS-CMDU-84920",
                status = ShipmentStatus.DOCUMENTATION,
                createdAt = System.currentTimeMillis() - 86400000 * 2,
                updatedAt = System.currentTimeMillis()
            ),
            Shipment(
                id = "ECG_AIR_100226_017",
                reference = "EXP-2026-EU-017",
                customer = "NORDIC ACTIVE OY",
                poNumber = "PO-NA-2026-77",
                invoiceNumber = "EXP-INV-2026-093",
                invoiceDate = "2026-09-29",
                currency = "EUR",
                shippingMode = ShippingMode.AIR,
                portOfLoading = "Colombo Bandaranaike (CMB)",
                portOfDischarge = "Helsinki Vantaa (HEL)",
                placeOfDelivery = "Helsinki Logistics Park",
                vessel = "Qatar Airways Cargo QR8421",
                voyage = "QR8421",
                etd = "2026-10-03",
                containerNumber = "PMC-49201-QR",
                masterBlNumber = "157-94820194",
                amsBlNumber = "N/A",
                status = ShipmentStatus.PRODUCTION,
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis()
            ),
            Shipment(
                id = "ECGMIX_091526_015",
                reference = "EXP-2026-US-015",
                customer = "TORONTO ATHLETIC GEAR LTD",
                poNumber = "PO-TAG-4912",
                invoiceNumber = "EXP-INV-2026-088",
                invoiceDate = "2026-09-14",
                currency = "USD",
                shippingMode = ShippingMode.SEA,
                portOfLoading = "Colombo, Sri Lanka",
                portOfDischarge = "Halifax, Canada",
                placeOfDelivery = "Toronto ON M5V 2T6",
                vessel = "MSC OSCAR",
                voyage = "2408W",
                etd = "2026-09-20",
                containerNumber = "MSCU9482104/40",
                masterBlNumber = "MSCU78219402",
                amsBlNumber = "AMS-MSCU-782194",
                status = ShipmentStatus.SHIPPED,
                createdAt = System.currentTimeMillis() - 86400000 * 15,
                updatedAt = System.currentTimeMillis() - 86400000 * 8
            )
        )
        for (s in shipments) {
            shipmentDao.insertShipment(s)
        }

        // 5. Shipment Products for ECGMIX_092826_016 (Total = 505 cartons)
        val shipmentProducts = listOf(
            ShipmentProduct(
                shipmentId = sampleShipmentId,
                productId = 1,
                productName = "Ice Hockey Pucks",
                hsCode = "9506.99.2580",
                description = "Vulcanized rubber official ice hockey pucks, standard 6oz",
                cartons = 143,
                unitsPerCarton = 50,
                totalUnits = 7150,
                individualUnitWeightKg = 0.170,
                totalNetWeightKg = 1215.5,
                cartonGrossWeightKg = 9.20,
                totalGrossWeightKg = 1315.6,
                cbmPerCarton = 0.018,
                totalCbm = 2.574,
                countryOfOrigin = "Sri Lanka",
                unitPriceUsd = 1.85,
                totalAmountUsd = 13227.50
            ),
            ShipmentProduct(
                shipmentId = sampleShipmentId,
                productId = 2,
                productName = "Tin Lids",
                hsCode = "8309.90.0085",
                description = "Embossed coated tinplate container lids with hermetic seal",
                cartons = 120,
                unitsPerCarton = 250,
                totalUnits = 30000,
                individualUnitWeightKg = 0.020,
                totalNetWeightKg = 600.0,
                cartonGrossWeightKg = 5.60,
                totalGrossWeightKg = 672.0,
                cbmPerCarton = 0.0175,
                totalCbm = 2.100,
                countryOfOrigin = "Sri Lanka",
                unitPriceUsd = 0.32,
                totalAmountUsd = 9600.00
            ),
            ShipmentProduct(
                shipmentId = sampleShipmentId,
                productId = 3,
                productName = "Plastic Cups",
                hsCode = "3924.10.90",
                description = "Food-grade injection molded polypropylene branded cups",
                cartons = 110,
                unitsPerCarton = 100,
                totalUnits = 11000,
                individualUnitWeightKg = 0.050,
                totalNetWeightKg = 550.0,
                cartonGrossWeightKg = 5.80,
                totalGrossWeightKg = 638.0,
                cbmPerCarton = 0.036,
                totalCbm = 3.960,
                countryOfOrigin = "Sri Lanka",
                unitPriceUsd = 0.65,
                totalAmountUsd = 7150.00
            ),
            ShipmentProduct(
                shipmentId = sampleShipmentId,
                productId = 4,
                productName = "Sho Koi Fish Feed",
                hsCode = "2309.90.10",
                description = "Premium spirulina enriched ornamental koi floating pellet feed",
                cartons = 72,
                unitsPerCarton = 10,
                totalUnits = 720,
                individualUnitWeightKg = 1.000,
                totalNetWeightKg = 720.0,
                cartonGrossWeightKg = 10.50,
                totalGrossWeightKg = 756.0,
                cbmPerCarton = 0.0375,
                totalCbm = 2.700,
                countryOfOrigin = "Sri Lanka",
                unitPriceUsd = 8.50,
                totalAmountUsd = 6120.00
            ),
            ShipmentProduct(
                shipmentId = sampleShipmentId,
                productId = 5,
                productName = "Dog Food Clusters",
                hsCode = "2309.10.90",
                description = "Air-dried organic nutritional canine cluster nuggets",
                cartons = 60,
                unitsPerCarton = 20,
                totalUnits = 1200,
                individualUnitWeightKg = 0.500,
                totalNetWeightKg = 600.0,
                cartonGrossWeightKg = 10.80,
                totalGrossWeightKg = 648.0,
                cbmPerCarton = 0.048,
                totalCbm = 2.880,
                countryOfOrigin = "Sri Lanka",
                unitPriceUsd = 5.46,
                totalAmountUsd = 6552.50
            )
        )
        shipmentDao.insertShipmentProducts(shipmentProducts)

        // 6. Pallets (9 Pallets for ECGMIX_092826_016 summing to 505 cartons)
        val pallets = listOf(
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 1", palletType = "Standard Wooden 1200x1000", cartons = 58, productsSummary = "Ice Hockey Pucks (58 ctns)", weightKg = 558.6, cbm = 1.58),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 2", palletType = "Standard Wooden 1200x1000", cartons = 58, productsSummary = "Ice Hockey Pucks (58 ctns)", weightKg = 558.6, cbm = 1.58),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 3", palletType = "Standard Wooden 1200x1000", cartons = 57, productsSummary = "Ice Hockey Pucks (27 ctns), Tin Lids (30 ctns)", weightKg = 441.4, cbm = 1.55),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 4", palletType = "Standard Wooden 1200x1000", cartons = 60, productsSummary = "Tin Lids (60 ctns)", weightKg = 361.0, cbm = 1.52),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 5", palletType = "Standard Wooden 1200x1000", cartons = 55, productsSummary = "Plastic Cups (55 ctns)", weightKg = 344.0, cbm = 1.62),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 6", palletType = "Standard Wooden 1200x1000", cartons = 55, productsSummary = "Plastic Cups (55 ctns)", weightKg = 344.0, cbm = 1.62),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 7", palletType = "Standard Wooden 1200x1000", cartons = 52, productsSummary = "Tin Lids (30 ctns), Dog Food (22 ctns)", weightKg = 428.6, cbm = 1.60),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 8", palletType = "Standard Wooden 1200x1000", cartons = 54, productsSummary = "Sho Koi Feed (36 ctns), Dog Food (18 ctns)", weightKg = 597.4, cbm = 1.65),
            Pallet(shipmentId = sampleShipmentId, palletNumber = "Pallet 9", palletType = "Standard Wooden 1200x1000", cartons = 56, productsSummary = "Sho Koi Feed (36 ctns), Dog Food (20 ctns)", weightKg = 619.0, cbm = 1.68)
        )
        palletDao.insertAllPallets(pallets)

        // 7. Commercial Invoice
        val invoice = CommercialInvoice(
            shipmentId = sampleShipmentId,
            invoiceNumber = "EXP-INV-2026-092",
            invoiceDate = "2026-09-28",
            exporterName = "EXPLIC CEYLON (PRIVATE) LIMITED",
            exporterAddress = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            buyerName = "OUTLAW DIP COMPANY INC.",
            buyerAddress = "820 Grand Blvd, Deer Park, New York 11729, USA",
            consigneeName = "OUTLAW DIP COMPANY INC.",
            consigneeAddress = "820 Grand Blvd, Deer Park, New York 11729, USA",
            currency = "USD",
            paymentTerms = "T/T 30 Days Net from B/L Date",
            freight = 2850.00,
            insurance = 380.00,
            leadTime = "Shipped on CMA CGM MEXICO ETD 08-OCT-2026",
            notes = "Commercial export of manufactured goods from Sri Lanka. Payment in USD via wire transfer.",
            accountName = "Explic Ceylon (Private) Limited",
            bankName = "Commercial Bank of Ceylon PLC",
            accountNumber = "1000-8472-9102",
            branchCode = "018",
            bankCode = "7056",
            swiftCode = "CCEYLKX",
            bankAddress = "Corporate Branch, York Street, Colombo 01, Sri Lanka",
            status = DocumentStatus.READY,
            totalAmount = 45880.00
        )
        docDao.saveInvoice(invoice)

        // 8. Packing List (505 Cartons, 4029.6 kg Gross)
        val packingList = PackingList(
            shipmentId = sampleShipmentId,
            consignee = "OUTLAW DIP COMPANY INC.",
            consigneeAddress = "820 Grand Blvd, Deer Park, New York 11729, USA",
            shipper = "EXPLIC CEYLON (PRIVATE) LIMITED",
            shipperAddress = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            piNumber = "PI-EXP-2026-092",
            date = "2026-09-28",
            portOfLoading = "Colombo, Sri Lanka",
            portOfDischarge = "New York, USA",
            fclLcl = "FCL 1x20' GP",
            seaAirCargo = "Sea Cargo",
            fdaRegistration = "19482019482",
            containerNumber = "EMCU6409544/20",
            sealNumber = "SL-940284",
            marksAndCartonNumbers = "OUTLAW / DEER PARK / NY / C/NO. 1-505",
            totalCartons = 505,
            totalUnits = 50070,
            totalNetWeightKg = 3685.5,
            totalGrossWeightKg = 4029.6,
            totalCbm = 14.214,
            totalPallets = 9,
            status = DocumentStatus.READY
        )
        docDao.savePackingList(packingList)

        // 9. ISF Filing (Intentionally seeded with 608 Cartons to demonstrate Validation Warning as requested)
        val isf = IsfFiling(
            shipmentId = sampleShipmentId,
            manufacturerNameAndAddress = "Explic Ceylon (Pvt) Ltd, 43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            sellerNameAndAddress = "Explic Ceylon (Pvt) Ltd, 43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            consigneeNameAndAddress = "Outlaw Dip Company Inc., 820 Grand Blvd, Deer Park, NY 11729, USA",
            shipToNameAndAddress = "Outlaw Dip Company Inc., 820 Grand Blvd, Deer Park, NY 11729, USA",
            containerStuffingLocation = "Ceylon Container Terminal Stuffing Yard, Colombo 15, Sri Lanka",
            consolidatorNameAndAddress = "CMA CGM Lanka (Pvt) Ltd, Level 8, Hemas House, Colombo 02, Sri Lanka",
            descriptionOfGoods = "Ice Hockey Pucks, Tin Lids, Plastic Cups, Koi Fish Feed & Pet Treats",
            productCodeHtsCode = "9506.99.2580 / 8309.90.0085 / 3924.10.90 / 2309.90.10 / 2309.10.90",
            countryOfOrigin = "Sri Lanka",
            purchaseOrderNumber = "PO-ODC-9842",
            importerOfRecordNumber = "11-384029100",
            vessel = "CMA CGM MEXICO",
            voyage = "1TUIUS1MA",
            etd = "2026-10-08",
            masterBlNumber = "CMDU10294821",
            amsBlNumber = "AMS-CMDU-84920",
            containerNumber = "EMCU6409544/20",
            cartonsCount = 608, // DISCREPANCY: 608 instead of 505!
            totalGrossWeightKg = 4520.0,
            completedByCompany = "Explic Ceylon (Pvt) Ltd",
            completedByName = "Roshan Perera",
            completedByTitle = "Compliance Officer",
            completedByEmail = "compliance@explicceylon.com",
            completedDate = "2026-09-28",
            isReadyToTransmit = false,
            status = DocumentStatus.VALIDATION_ERROR
        )
        docDao.saveIsfFiling(isf)

        // 10. B/L Instructions (Intentionally demonstrates Product quantity mismatch: 143 vs 333 & gross wt check)
        val bl = BlInstructions(
            shipmentId = sampleShipmentId,
            shipper = "EXPLIC CEYLON (PRIVATE) LIMITED\n43/1, Hospital Road, Dehiwala, Sri Lanka 10350\nTel: +94 11 273 8920",
            consignee = "OUTLAW DIP COMPANY INC.\n820 Grand Blvd, Deer Park, New York 11729, USA\nAttn: George Statler",
            firstNotifyParty = "OUTLAW DIP COMPANY INC.\n820 Grand Blvd, Deer Park, New York 11729, USA",
            secondNotifyParty = "SAME AS CONSIGNEE",
            vessel = "CMA CGM MEXICO",
            voyageNumber = "1TUIUS1MA",
            placeOfDelivery = "Deer Park, NY 11729, USA",
            portOfLoading = "Colombo, Sri Lanka",
            portOfDischarge = "New York, USA",
            marksAndNumbers = "OUTLAW / DEER PARK / NY / C/NO. 1-505",
            hsNumber = "9506.99 / 8309.90 / 3924.10 / 2309.90",
            kindOfPackages = "9 Pallets / 505 Cartons",
            descriptionOfGoods = "505 CARTONS ON 9 PALLETS CONTAINING:\n- 333 CTNS ICE HOCKEY PUCKS (Note: Discrepancy with PL 143)\n- 120 CTNS TIN LIDS\n- 110 CTNS PLASTIC CUPS\n- 72 CTNS KOI FISH FEED\n- 60 CTNS DOG FOOD CLUSTERS",
            grossWeightKg = 0.0, // DISCREPANCY: Gross weight missing/zero from B/L draft!
            measurementCbm = 14.214,
            totalContainersPackages = "1 x 20' Dry Container (9 Pallets / 505 Cartons)",
            placeOfReceipt = "Colombo CFS, Sri Lanka",
            freightTerms = "FREIGHT PREPAID",
            numberOfOriginals = "THREE (3) ORIGINALS",
            remarks = "CLEAN ON BOARD. TRANSSHIPMENT PERMITTED. 14 DAYS FREE DEMURRAGE AT DESTINATION.",
            containerNumber = "EMCU6409544/20",
            status = DocumentStatus.VALIDATION_ERROR
        )
        docDao.saveBlInstructions(bl)

        // Unified Documents Registry
        val initialDocuments = listOf(
            Document(
                shipmentId = sampleShipmentId,
                documentType = "COMMERCIAL_INVOICE",
                documentNumber = "EXP-INV-2026-092",
                title = "Commercial Invoice - $sampleShipmentId",
                issueDate = "2026-09-28",
                status = DocumentStatus.DRAFT,
                fileFormat = "PDF",
                generatedBy = "Roshan Perera",
                fileSizeKb = 142
            ),
            Document(
                shipmentId = sampleShipmentId,
                documentType = "PACKING_LIST",
                documentNumber = "PI-EXP-2026-US-016",
                title = "Packing List - $sampleShipmentId",
                issueDate = "2026-09-28",
                status = DocumentStatus.DRAFT,
                fileFormat = "PDF",
                generatedBy = "Kumara Silva",
                fileSizeKb = 188
            ),
            Document(
                shipmentId = sampleShipmentId,
                documentType = "ISF_10_2",
                documentNumber = "ISF-EXP-2026-US-016",
                title = "Importer Security Filing (ISF 10+2)",
                issueDate = "2026-09-28",
                status = DocumentStatus.DRAFT,
                fileFormat = "PDF",
                generatedBy = "Roshan Perera",
                fileSizeKb = 95
            ),
            Document(
                shipmentId = sampleShipmentId,
                documentType = "BILL_OF_LADING",
                documentNumber = "CMDU10294821",
                title = "Ocean Bill of Lading Instructions",
                issueDate = "2026-09-28",
                status = DocumentStatus.VALIDATION_ERROR,
                fileFormat = "PDF",
                generatedBy = "Roshan Perera",
                fileSizeKb = 210
            )
        )
        docDao.insertAllDocuments(initialDocuments)

        // 11. Initial Audit Logs
        val auditLogs = listOf(
            AuditLog(
                formattedDate = "28 Sep 2026 14:32",
                userName = "Roshan Perera",
                userRole = "Documentation Officer",
                action = "Generated Initial Drafts",
                oldValue = "None",
                newValue = "Commercial Invoice & Packing List Created",
                document = "Commercial Invoice",
                shipmentId = sampleShipmentId
            ),
            AuditLog(
                formattedDate = "29 Sep 2026 09:15",
                userName = "Kumara Silva",
                userRole = "Warehouse User",
                action = "Updated Pallet Counts",
                oldValue = "8 Pallets",
                newValue = "9 Pallets (505 Cartons verified)",
                document = "Packing List",
                shipmentId = sampleShipmentId
            ),
            AuditLog(
                formattedDate = "29 Sep 2026 11:40",
                userName = "Roshan Perera",
                userRole = "Documentation Officer",
                action = "Triggered Validation Engine",
                oldValue = "Pre-validation",
                newValue = "Validation Warnings Flagged (ISF cartons 608 vs PL 505)",
                document = "Validation Engine",
                shipmentId = sampleShipmentId
            )
        )
        for (log in auditLogs) {
            auditDao.insertLog(log)
        }

        // 12. Initial Notifications
        val notifs = listOf(
            AppNotification(
                title = "Validation Warning: ECGMIX_092826_016",
                message = "ISF Carton count (608) does not match Packing List (505). Please resolve before final submission.",
                type = "WARNING",
                shipmentId = sampleShipmentId
            ),
            AppNotification(
                title = "Gross Weight Missing on B/L Draft",
                message = "Shipment ECGMIX_092826_016 Bill of Lading instruction requires gross weight.",
                type = "ERROR",
                shipmentId = sampleShipmentId
            ),
            AppNotification(
                title = "Upcoming Vessel ETD: 08-OCT-2026",
                message = "CMA CGM MEXICO departing Colombo in 9 days. ISF 10+2 must be transmitted 48h prior.",
                type = "INFO",
                shipmentId = sampleShipmentId
            )
        )
        for (n in notifs) {
            notifDao.insertNotification(n)
        }

        // 13. Initial Warehouse Stock Items
        val warehouseDao = db.warehouseDao()
        val stockItems = listOf(
            WarehouseStockItem(
                productId = 1L,
                productName = "Ice Hockey Pucks",
                hsCode = "9506.99.2580",
                batchLotNumber = "LOT-2026-HP01",
                barcode = "EXP-PUCK-9506",
                totalReceivedCartons = 250,
                availableCartons = 107,
                allocatedCartons = 143,
                dispatchedCartons = 0,
                unitsPerCarton = 50,
                unitWeightKg = 0.170,
                cartonGrossWeightKg = 9.20,
                cbmPerCarton = 0.018,
                lengthCm = 40.0,
                widthCm = 30.0,
                heightCm = 15.0,
                locationBay = "Bay A-01"
            ),
            WarehouseStockItem(
                productId = 2L,
                productName = "Tin Lids",
                hsCode = "8309.90.0085",
                batchLotNumber = "LOT-2026-TL04",
                barcode = "EXP-LIDS-8309",
                totalReceivedCartons = 200,
                availableCartons = 80,
                allocatedCartons = 120,
                dispatchedCartons = 0,
                unitsPerCarton = 250,
                unitWeightKg = 0.020,
                cartonGrossWeightKg = 5.60,
                cbmPerCarton = 0.0175,
                lengthCm = 35.0,
                widthCm = 25.0,
                heightCm = 20.0,
                locationBay = "Bay A-02"
            ),
            WarehouseStockItem(
                productId = 3L,
                productName = "Plastic Cups",
                hsCode = "3924.10.90",
                batchLotNumber = "LOT-2026-PC02",
                barcode = "EXP-CUPS-3924",
                totalReceivedCartons = 180,
                availableCartons = 70,
                allocatedCartons = 110,
                dispatchedCartons = 0,
                unitsPerCarton = 100,
                unitWeightKg = 0.050,
                cartonGrossWeightKg = 5.80,
                cbmPerCarton = 0.036,
                lengthCm = 45.0,
                widthCm = 40.0,
                heightCm = 20.0,
                locationBay = "Bay B-01"
            ),
            WarehouseStockItem(
                productId = 4L,
                productName = "Sho Koi Fish Feed",
                hsCode = "2309.90.10",
                batchLotNumber = "LOT-2026-KF09",
                barcode = "EXP-FEED-2309",
                totalReceivedCartons = 120,
                availableCartons = 48,
                allocatedCartons = 72,
                dispatchedCartons = 0,
                unitsPerCarton = 10,
                unitWeightKg = 1.000,
                cartonGrossWeightKg = 10.50,
                cbmPerCarton = 0.0375,
                lengthCm = 50.0,
                widthCm = 30.0,
                heightCm = 25.0,
                locationBay = "Bay C-01"
            ),
            WarehouseStockItem(
                productId = 5L,
                productName = "Dog Food Clusters",
                hsCode = "2309.10.90",
                batchLotNumber = "LOT-2026-DC07",
                barcode = "EXP-DOG-2309",
                totalReceivedCartons = 100,
                availableCartons = 40,
                allocatedCartons = 60,
                dispatchedCartons = 0,
                unitsPerCarton = 20,
                unitWeightKg = 0.500,
                cartonGrossWeightKg = 10.80,
                cbmPerCarton = 0.048,
                lengthCm = 60.0,
                widthCm = 40.0,
                heightCm = 20.0,
                locationBay = "Bay C-02"
            )
        )
        warehouseDao.insertAllStock(stockItems)

        // 14. Initial Goods Receipts
        val receipts = listOf(
            GoodsReceipt(
                receiptNumber = "GRN-2026-0041",
                productId = 1L,
                productName = "Ice Hockey Pucks",
                supplierName = "Explic Ceylon Factory",
                shipmentId = sampleShipmentId,
                cartonsReceived = 150,
                unitsPerCarton = 50,
                totalUnits = 7500,
                cartonGrossWeightKg = 9.20,
                totalGrossWeightKg = 1380.0,
                lengthCm = 40.0,
                widthCm = 30.0,
                heightCm = 15.0,
                totalCbm = 2.700,
                batchNumber = "LOT-2026-HP01",
                barcode = "EXP-PUCK-9506",
                locationBay = "Bay A-01",
                receivedDate = "28 Sep 2026",
                receivedBy = "Kumara Silva",
                notes = "Received from moulding facility in prime export packing"
            ),
            GoodsReceipt(
                receiptNumber = "GRN-2026-0042",
                productId = 2L,
                productName = "Tin Lids",
                supplierName = "Metalpack Industries (Pvt) Ltd",
                shipmentId = sampleShipmentId,
                cartonsReceived = 120,
                unitsPerCarton = 250,
                totalUnits = 30000,
                cartonGrossWeightKg = 5.60,
                totalGrossWeightKg = 672.0,
                lengthCm = 35.0,
                widthCm = 25.0,
                heightCm = 20.0,
                totalCbm = 2.100,
                batchNumber = "LOT-2026-TL04",
                barcode = "EXP-LIDS-8309",
                locationBay = "Bay A-02",
                receivedDate = "28 Sep 2026",
                receivedBy = "Kumara Silva",
                notes = "Food grade coated tinplate lids inspected"
            )
        )
        for (r in receipts) {
            warehouseDao.insertReceipt(r)
        }

        // 15. Initial Delivery Note
        val deliveryNote = DeliveryNote(
            noteNumber = "DN-2026-0016",
            shipmentId = sampleShipmentId,
            date = "28 Sep 2026",
            consigneeName = "OUTLAW DIP COMPANY INC.",
            consigneeAddress = "820 Grand Blvd, Deer Park, NY 11729, USA",
            destinationPort = "New York, USA",
            containerNumber = "EMCU6409544/20",
            sealNumber = "SL-940284",
            vehicleNumber = "WP-LH-8921",
            driverName = "Gamini Wijesinghe",
            driverPhone = "+94 71 889 1234",
            itemsSummary = "505 Cartons on 9 Pallets / Ice Hockey Pucks, Tin Lids, Plastic Cups & Fish Feed",
            totalCartons = 505,
            totalPallets = 9,
            totalGrossWeightKg = 4029.6,
            totalCbm = 14.214,
            preparedBy = "Kumara Silva",
            receivedBySignature = "Gamini Wijesinghe (Driver)",
            notes = "Stuffed in container EMCU6409544/20 with high security seal SL-940284"
        )
        warehouseDao.insertDeliveryNote(deliveryNote)
    }
}
