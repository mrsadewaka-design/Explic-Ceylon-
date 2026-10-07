package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.engine.ValidationEngine
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class ExportRepository(private val database: AppDatabase) {

    private val shipmentDao = database.shipmentDao()
    private val productDao = database.productDao()
    private val partyDao = database.partyDao()
    private val palletDao = database.palletDao()
    private val docDao = database.documentDao()
    private val auditDao = database.auditLogDao()
    private val notifDao = database.notificationDao()
    private val userDao = database.userDao()
    private val warehouseDao = database.warehouseDao()
    private val draftDao = database.offlineDraftDao()

    val allShipments: Flow<List<Shipment>> = shipmentDao.getAllShipments()
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allParties: Flow<List<Party>> = partyDao.getAllParties()
    val allAuditLogs: Flow<List<AuditLog>> = auditDao.getAllLogs()
    val allNotifications: Flow<List<AppNotification>> = notifDao.getAllNotifications()
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allWarehouseStock: Flow<List<WarehouseStockItem>> = warehouseDao.getAllStockFlow()
    val allGoodsReceipts: Flow<List<GoodsReceipt>> = warehouseDao.getAllReceiptsFlow()
    val allWarehouseDispatches: Flow<List<WarehouseDispatch>> = warehouseDao.getAllDispatchesFlow()
    val allDeliveryNotes: Flow<List<DeliveryNote>> = warehouseDao.getAllDeliveryNotesFlow()
    val allOfflineDrafts: Flow<List<OfflineDraft>> = draftDao.getAllDraftsFlow()
    val allDocuments: Flow<List<Document>> = docDao.getAllDocumentsFlow()
    val pendingSyncShipments: Flow<List<Shipment>> = shipmentDao.getPendingSyncShipmentsFlow()
    val pendingSyncDocuments: Flow<List<Document>> = docDao.getPendingSyncDocumentsFlow()
    val pendingSyncAuditLogs: Flow<List<AuditLog>> = auditDao.getPendingSyncLogsFlow()

    fun getShipment(id: String): Flow<Shipment?> = shipmentDao.getShipmentByIdFlow(id)
    suspend fun getShipmentDirect(id: String): Shipment? = shipmentDao.getShipmentById(id)

    fun getShipmentProducts(shipmentId: String): Flow<List<ShipmentProduct>> =
        shipmentDao.getProductsForShipmentFlow(shipmentId)

    fun getPallets(shipmentId: String): Flow<List<Pallet>> =
        palletDao.getPalletsForShipmentFlow(shipmentId)

    fun getCommercialInvoice(shipmentId: String): Flow<CommercialInvoice?> =
        docDao.getInvoiceFlow(shipmentId)

    fun getPackingList(shipmentId: String): Flow<PackingList?> =
        docDao.getPackingListFlow(shipmentId)

    fun getIsfFiling(shipmentId: String): Flow<IsfFiling?> =
        docDao.getIsfFilingFlow(shipmentId)

    fun getBlInstructions(shipmentId: String): Flow<BlInstructions?> =
        docDao.getBlInstructionsFlow(shipmentId)

    fun getDocumentsForShipment(shipmentId: String): Flow<List<Document>> =
        docDao.getDocumentsForShipmentFlow(shipmentId)

    suspend fun getDocumentsForShipmentDirect(shipmentId: String): List<Document> =
        docDao.getDocumentsForShipment(shipmentId)

    suspend fun saveDocument(document: Document): Long =
        docDao.insertDocument(document)

    suspend fun deleteDocument(document: Document) =
        docDao.deleteDocument(document)

    suspend fun deleteDocumentById(id: Long) =
        docDao.deleteDocumentById(id)

    fun searchDocuments(query: String): Flow<List<Document>> =
        docDao.searchDocumentsFlow(query)

    fun searchAuditLogs(query: String): Flow<List<AuditLog>> =
        auditDao.searchLogsFlow(query)

    fun getAuditLogsForShipment(shipmentId: String): Flow<List<AuditLog>> =
        auditDao.getLogsForShipment(shipmentId)

    suspend fun insertAuditLog(log: AuditLog): Long =
        auditDao.insertLog(log)

    suspend fun getFullShipmentBundle(shipmentId: String): FullShipmentBundle? {
        val s = shipmentDao.getShipmentById(shipmentId) ?: return null
        val prods = shipmentDao.getProductsForShipment(shipmentId)
        val pals = palletDao.getPalletsForShipment(shipmentId)
        val inv = docDao.getInvoice(shipmentId)
        val pl = docDao.getPackingList(shipmentId)
        val isf = docDao.getIsfFiling(shipmentId)
        val bl = docDao.getBlInstructions(shipmentId)
        val validation = ValidationEngine.validateShipment(s, prods, pals, inv, pl, isf, bl)
        return FullShipmentBundle(
            shipment = s,
            products = prods,
            pallets = pals,
            invoice = inv,
            packingList = pl,
            isf = isf,
            bl = bl,
            validationSummary = validation
        )
    }

    suspend fun createShipmentWithDocuments(
        shipment: Shipment,
        initialProducts: List<ShipmentProduct>,
        user: User
    ) {
        shipmentDao.insertShipment(shipment)
        if (initialProducts.isNotEmpty()) {
            shipmentDao.insertShipmentProducts(initialProducts)
        }

        // Calculate totals
        val totalCartons = initialProducts.sumOf { it.cartons }
        val totalUnits = initialProducts.sumOf { it.totalUnits }
        val totalNetWeight = initialProducts.sumOf { it.totalNetWeightKg }
        val totalGrossWeight = initialProducts.sumOf { it.totalGrossWeightKg }
        val totalCbm = initialProducts.sumOf { it.totalCbm }
        val totalAmount = initialProducts.sumOf { it.totalAmountUsd }

        // Automatically generate all 4 linked documents reusing shipment data ONCE
        val invoice = CommercialInvoice(
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
            totalAmount = totalAmount,
            status = DocumentStatus.DRAFT
        )
        docDao.saveInvoice(invoice)

        val packingList = PackingList(
            shipmentId = shipment.id,
            consignee = shipment.customer,
            consigneeAddress = shipment.placeOfDelivery,
            shipper = "EXPLIC CEYLON (PRIVATE) LIMITED",
            shipperAddress = "43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            piNumber = "PI-" + shipment.reference,
            date = shipment.invoiceDate,
            portOfLoading = shipment.portOfLoading,
            portOfDischarge = shipment.portOfDischarge,
            containerNumber = shipment.containerNumber,
            totalCartons = totalCartons,
            totalUnits = totalUnits,
            totalNetWeightKg = totalNetWeight,
            totalGrossWeightKg = totalGrossWeight,
            totalCbm = totalCbm,
            totalPallets = maxOf(1, (totalCartons + 59) / 60),
            status = DocumentStatus.DRAFT
        )
        docDao.savePackingList(packingList)

        val isf = IsfFiling(
            shipmentId = shipment.id,
            manufacturerNameAndAddress = "Explic Ceylon (Pvt) Ltd, 43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            sellerNameAndAddress = "Explic Ceylon (Pvt) Ltd, 43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            consigneeNameAndAddress = "${shipment.customer}, ${shipment.placeOfDelivery}",
            shipToNameAndAddress = "${shipment.customer}, ${shipment.placeOfDelivery}",
            containerStuffingLocation = "Colombo CFS, Sri Lanka",
            consolidatorNameAndAddress = "CMA CGM Lanka (Pvt) Ltd, Colombo 02, Sri Lanka",
            descriptionOfGoods = initialProducts.joinToString(", ") { it.productName },
            productCodeHtsCode = initialProducts.joinToString(" / ") { it.hsCode },
            countryOfOrigin = "Sri Lanka",
            purchaseOrderNumber = shipment.poNumber,
            importerOfRecordNumber = "11-384029100",
            vessel = shipment.vessel,
            voyage = shipment.voyage,
            etd = shipment.etd,
            masterBlNumber = shipment.masterBlNumber,
            amsBlNumber = shipment.amsBlNumber,
            containerNumber = shipment.containerNumber,
            cartonsCount = totalCartons,
            totalGrossWeightKg = totalGrossWeight,
            completedByCompany = "Explic Ceylon (Pvt) Ltd",
            completedByName = user.fullName,
            completedByTitle = user.role.label,
            completedByEmail = user.email,
            completedDate = shipment.invoiceDate,
            status = DocumentStatus.DRAFT
        )
        docDao.saveIsfFiling(isf)

        val bl = BlInstructions(
            shipmentId = shipment.id,
            shipper = "EXPLIC CEYLON (PRIVATE) LIMITED\n43/1, Hospital Road, Dehiwala, Sri Lanka 10350",
            consignee = "${shipment.customer}\n${shipment.placeOfDelivery}",
            firstNotifyParty = "${shipment.customer}\n${shipment.placeOfDelivery}",
            secondNotifyParty = "SAME AS CONSIGNEE",
            vessel = shipment.vessel,
            voyageNumber = shipment.voyage,
            placeOfDelivery = shipment.placeOfDelivery,
            portOfLoading = shipment.portOfLoading,
            portOfDischarge = shipment.portOfDischarge,
            marksAndNumbers = "C/NO. 1-$totalCartons",
            hsNumber = initialProducts.firstOrNull()?.hsCode ?: "",
            kindOfPackages = "$totalCartons Cartons",
            descriptionOfGoods = initialProducts.joinToString("\n") { "- ${it.cartons} CTNS ${it.productName}" },
            grossWeightKg = totalGrossWeight,
            measurementCbm = totalCbm,
            totalContainersPackages = "1 x 20' Dry Container ($totalCartons Cartons)",
            containerNumber = shipment.containerNumber,
            status = DocumentStatus.DRAFT
        )
        docDao.saveBlInstructions(bl)

        val documents = listOf(
            Document(
                shipmentId = shipment.id,
                documentType = "COMMERCIAL_INVOICE",
                documentNumber = shipment.invoiceNumber,
                title = "Commercial Invoice - ${shipment.id}",
                issueDate = shipment.invoiceDate,
                status = DocumentStatus.DRAFT,
                fileFormat = "PDF",
                generatedBy = user.fullName
            ),
            Document(
                shipmentId = shipment.id,
                documentType = "PACKING_LIST",
                documentNumber = packingList.piNumber,
                title = "Packing List - ${shipment.id}",
                issueDate = shipment.invoiceDate,
                status = DocumentStatus.DRAFT,
                fileFormat = "PDF",
                generatedBy = user.fullName
            ),
            Document(
                shipmentId = shipment.id,
                documentType = "ISF_10_2",
                documentNumber = "ISF-" + shipment.reference,
                title = "Importer Security Filing (ISF 10+2)",
                issueDate = shipment.invoiceDate,
                status = DocumentStatus.DRAFT,
                fileFormat = "PDF",
                generatedBy = user.fullName
            ),
            Document(
                shipmentId = shipment.id,
                documentType = "BILL_OF_LADING",
                documentNumber = shipment.masterBlNumber.ifBlank { "BL-DRAFT-" + shipment.reference },
                title = "Ocean Bill of Lading Instructions",
                issueDate = shipment.invoiceDate,
                status = DocumentStatus.DRAFT,
                fileFormat = "PDF",
                generatedBy = user.fullName
            )
        )
        docDao.insertAllDocuments(documents)

        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Created Shipment & Initial Documents",
            oldVal = "None",
            newVal = "${shipment.id} ($totalCartons cartons)",
            doc = "Shipment Record",
            shipmentId = shipment.id
        )
    }

    suspend fun updateShipment(shipment: Shipment, user: User) {
        val old = shipmentDao.getShipmentById(shipment.id)
        shipmentDao.updateShipment(shipment)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Updated Shipment Details",
            oldVal = old?.status?.label ?: "",
            newVal = shipment.status.label,
            doc = "Shipment Record",
            shipmentId = shipment.id
        )
    }

    suspend fun saveShipmentProduct(product: ShipmentProduct, user: User) {
        if (product.id == 0L) {
            shipmentDao.insertShipmentProduct(product)
            logAction(
                userName = user.fullName,
                userRole = user.role.label,
                action = "Added Product to Shipment",
                oldVal = "None",
                newVal = "${product.productName} (${product.cartons} ctns)",
                doc = "Packing List / Shipment Products",
                shipmentId = product.shipmentId
            )
        } else {
            shipmentDao.updateShipmentProduct(product)
            logAction(
                userName = user.fullName,
                userRole = user.role.label,
                action = if (product.isManualOverride) "Manual Override Product Cartons/Weight" else "Updated Shipment Product",
                oldVal = "Previous quantity",
                newVal = "${product.cartons} cartons (${product.totalGrossWeightKg} kg)",
                doc = "Shipment Products",
                shipmentId = product.shipmentId
            )
        }
        recalculateShipmentTotals(product.shipmentId)
    }

    suspend fun deleteShipmentProduct(product: ShipmentProduct, user: User) {
        shipmentDao.deleteShipmentProduct(product.id)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Removed Product",
            oldVal = product.productName,
            newVal = "Deleted",
            doc = "Shipment Products",
            shipmentId = product.shipmentId
        )
        recalculateShipmentTotals(product.shipmentId)
    }

    private suspend fun recalculateShipmentTotals(shipmentId: String) {
        val products = shipmentDao.getProductsForShipment(shipmentId)
        val totalCartons = products.sumOf { it.cartons }
        val totalUnits = products.sumOf { it.totalUnits }
        val totalNet = products.sumOf { it.totalNetWeightKg }
        val totalGross = products.sumOf { it.totalGrossWeightKg }
        val totalCbm = products.sumOf { it.totalCbm }

        val pl = docDao.getPackingList(shipmentId)
        if (pl != null) {
            docDao.savePackingList(
                pl.copy(
                    totalCartons = totalCartons,
                    totalUnits = totalUnits,
                    totalNetWeightKg = totalNet,
                    totalGrossWeightKg = totalGross,
                    totalCbm = totalCbm,
                    totalPallets = maxOf(1, (totalCartons + 59) / 60)
                )
            )
        }
    }

    suspend fun savePallet(pallet: Pallet, user: User) {
        if (pallet.id == 0L) {
            palletDao.insertPallet(pallet)
        } else {
            palletDao.updatePallet(pallet)
        }
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Updated Pallet Record",
            oldVal = "${pallet.palletNumber}",
            newVal = "${pallet.cartons} cartons / ${pallet.weightKg} kg",
            doc = "Pallet Management",
            shipmentId = pallet.shipmentId
        )
    }

    suspend fun saveCommercialInvoice(invoice: CommercialInvoice, user: User) {
        docDao.saveInvoice(invoice)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Saved Commercial Invoice",
            oldVal = "Draft",
            newVal = "Total ${invoice.currency} ${invoice.totalAmount} (${invoice.status.label})",
            doc = "Commercial Invoice",
            shipmentId = invoice.shipmentId
        )
    }

    suspend fun savePackingList(packingList: PackingList, user: User) {
        docDao.savePackingList(packingList)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Saved Packing List",
            oldVal = "Draft",
            newVal = "${packingList.totalCartons} Cartons, ${packingList.totalGrossWeightKg} kg Gross (${packingList.status.label})",
            doc = "Packing List",
            shipmentId = packingList.shipmentId
        )
    }

    suspend fun saveIsfFiling(isf: IsfFiling, user: User) {
        docDao.saveIsfFiling(isf)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Saved ISF Filing",
            oldVal = "Previous ISF",
            newVal = "${isf.cartonsCount} Cartons, Vessel: ${isf.vessel} (${isf.status.label})",
            doc = "ISF Filing",
            shipmentId = isf.shipmentId
        )
    }

    suspend fun saveBlInstructions(bl: BlInstructions, user: User) {
        docDao.saveBlInstructions(bl)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Saved B/L Instructions",
            oldVal = "Previous B/L",
            newVal = "Gross Wt: ${bl.grossWeightKg} kg, ${bl.kindOfPackages} (${bl.status.label})",
            doc = "B/L Instructions",
            shipmentId = bl.shipmentId
        )
    }

    suspend fun approveWarningOverride(shipmentId: String, user: User, reason: String) {
        val shipment = shipmentDao.getShipmentById(shipmentId) ?: return
        val updated = shipment.copy(
            isOverrideApproved = true,
            overrideApprovedBy = "${user.fullName} (${user.role.label})",
            overrideTimestamp = System.currentTimeMillis(),
            overrideNotes = reason
        )
        shipmentDao.updateShipment(updated)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Authorized Override of Validation Warnings",
            oldVal = "Warnings Active",
            newVal = "Override Approved: $reason",
            doc = "Validation Engine",
            shipmentId = shipmentId
        )
        notifDao.insertNotification(
            AppNotification(
                title = "Warnings Overridden for $shipmentId",
                message = "Authorized by ${user.fullName} (${user.role.label}). Reason: $reason",
                type = "INFO",
                shipmentId = shipmentId
            )
        )
    }

    suspend fun markDocumentStatus(shipmentId: String, docType: String, newStatus: DocumentStatus, user: User) {
        when (docType) {
            "COMMERCIAL_INVOICE" -> {
                val inv = docDao.getInvoice(shipmentId) ?: return
                docDao.saveInvoice(inv.copy(status = newStatus))
            }
            "PACKING_LIST" -> {
                val pl = docDao.getPackingList(shipmentId) ?: return
                docDao.savePackingList(pl.copy(status = newStatus))
            }
            "ISF" -> {
                val isf = docDao.getIsfFiling(shipmentId) ?: return
                docDao.saveIsfFiling(isf.copy(status = newStatus))
            }
            "BL" -> {
                val bl = docDao.getBlInstructions(shipmentId) ?: return
                docDao.saveBlInstructions(bl.copy(status = newStatus))
            }
        }
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Changed Document Status to ${newStatus.label}",
            oldVal = "Status",
            newVal = newStatus.label,
            doc = docType,
            shipmentId = shipmentId
        )
    }

    suspend fun addProductMaster(product: Product) = productDao.insertProduct(product)
    suspend fun updateProductMaster(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProductMaster(product: Product) = productDao.deleteProduct(product)

    suspend fun ensureMasterProductsSeeded() {
        val standardMasterProducts = listOf(
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
                name = "Dog Food Clusters",
                description = "Air-dried organic nutritional canine cluster nuggets",
                hsCode = "2309.10.90",
                countryOfOrigin = "Sri Lanka",
                defaultUnitWeightKg = 0.500,
                defaultCartonSize = "40 x 40 x 30 cm",
                unitsPerCarton = 20,
                defaultCbmPerCarton = 0.048,
                category = "Pet Nutrition"
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
            )
        )

        val existing = productDao.getAllProductsList()
        if (existing.isEmpty()) {
            productDao.insertAllProducts(standardMasterProducts)
        } else {
            for (std in standardMasterProducts) {
                val match = existing.find {
                    it.name.equals(std.name, ignoreCase = true) ||
                    (std.name == "Plastic Cups" && it.name.contains("Plastic Cups", ignoreCase = true)) ||
                    (std.name == "Tin Lids" && it.name.contains("Tin Lids", ignoreCase = true))
                }
                if (match == null) {
                    productDao.insertProduct(std)
                } else if (match.name != std.name || match.hsCode != std.hsCode) {
                    productDao.updateProduct(match.copy(name = std.name, hsCode = std.hsCode))
                }
            }
        }
    }

    suspend fun addParty(party: Party) = partyDao.insertParty(party)
    suspend fun updateParty(party: Party) = partyDao.updateParty(party)
    suspend fun deleteParty(party: Party) = partyDao.deleteParty(party)

    suspend fun markNotificationAsRead(id: Long) = notifDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead() = notifDao.markAllAsRead()

    // ==========================================
    // WAREHOUSE OPERATIONS & AUTOMATIC SYNC
    // ==========================================

    suspend fun receiveGoods(receipt: GoodsReceipt, autoUpdateShipment: Boolean, user: User) {
        warehouseDao.insertReceipt(receipt)

        // 1. Update or create WarehouseStockItem
        val existingStock = if (receipt.productId > 0) {
            warehouseDao.getStockByProductId(receipt.productId)
        } else {
            warehouseDao.getStockByBarcode(receipt.barcode)
        }

        if (existingStock != null) {
            val updatedStock = existingStock.copy(
                totalReceivedCartons = existingStock.totalReceivedCartons + receipt.cartonsReceived,
                availableCartons = existingStock.availableCartons + receipt.cartonsReceived,
                cartonGrossWeightKg = if (receipt.cartonGrossWeightKg > 0) receipt.cartonGrossWeightKg else existingStock.cartonGrossWeightKg,
                cbmPerCarton = if (receipt.lengthCm > 0 && receipt.widthCm > 0 && receipt.heightCm > 0) {
                    (receipt.lengthCm * receipt.widthCm * receipt.heightCm) / 1_000_000.0
                } else existingStock.cbmPerCarton,
                lengthCm = if (receipt.lengthCm > 0) receipt.lengthCm else existingStock.lengthCm,
                widthCm = if (receipt.widthCm > 0) receipt.widthCm else existingStock.widthCm,
                heightCm = if (receipt.heightCm > 0) receipt.heightCm else existingStock.heightCm,
                locationBay = if (receipt.locationBay.isNotBlank()) receipt.locationBay else existingStock.locationBay,
                lastUpdated = System.currentTimeMillis()
            )
            warehouseDao.updateStock(updatedStock)
        } else {
            val cbm = if (receipt.lengthCm > 0 && receipt.widthCm > 0 && receipt.heightCm > 0) {
                (receipt.lengthCm * receipt.widthCm * receipt.heightCm) / 1_000_000.0
            } else 0.025
            val newStock = WarehouseStockItem(
                productId = receipt.productId,
                productName = receipt.productName,
                hsCode = "EXP-${receipt.productId}",
                batchLotNumber = receipt.batchNumber,
                barcode = receipt.barcode,
                totalReceivedCartons = receipt.cartonsReceived,
                availableCartons = receipt.cartonsReceived,
                allocatedCartons = 0,
                dispatchedCartons = 0,
                unitsPerCarton = receipt.unitsPerCarton,
                unitWeightKg = if (receipt.unitsPerCarton > 0 && receipt.cartonGrossWeightKg > 0) receipt.cartonGrossWeightKg / receipt.unitsPerCarton else 0.1,
                cartonGrossWeightKg = receipt.cartonGrossWeightKg,
                cbmPerCarton = cbm,
                lengthCm = receipt.lengthCm,
                widthCm = receipt.widthCm,
                heightCm = receipt.heightCm,
                locationBay = receipt.locationBay,
                lastUpdated = System.currentTimeMillis()
            )
            warehouseDao.insertStock(newStock)
        }

        // 2. Automatically update shipment quantities if requested and linked to a shipment
        val shipId = receipt.shipmentId
        if (autoUpdateShipment && !shipId.isNullOrBlank()) {
            val shipment = shipmentDao.getShipmentById(shipId)
            if (shipment != null) {
                val existingProducts = shipmentDao.getProductsForShipment(shipId)
                val matchedProduct = existingProducts.find {
                    it.productId == receipt.productId || it.productName.equals(receipt.productName, ignoreCase = true)
                }
                if (matchedProduct != null) {
                    val oldCartons = matchedProduct.cartons
                    val newCartons = oldCartons + receipt.cartonsReceived
                    val newTotalUnits = newCartons * matchedProduct.unitsPerCarton
                    val newNet = newTotalUnits * matchedProduct.individualUnitWeightKg
                    val newGross = newCartons * (if (receipt.cartonGrossWeightKg > 0) receipt.cartonGrossWeightKg else matchedProduct.cartonGrossWeightKg)
                    val cbmCtn = if (receipt.lengthCm > 0 && receipt.widthCm > 0 && receipt.heightCm > 0) {
                        (receipt.lengthCm * receipt.widthCm * receipt.heightCm) / 1_000_000.0
                    } else matchedProduct.cbmPerCarton
                    val newTotalCbm = newCartons * cbmCtn
                    val updatedProduct = matchedProduct.copy(
                        cartons = newCartons,
                        totalUnits = newTotalUnits,
                        totalNetWeightKg = newNet,
                        cartonGrossWeightKg = if (receipt.cartonGrossWeightKg > 0) receipt.cartonGrossWeightKg else matchedProduct.cartonGrossWeightKg,
                        totalGrossWeightKg = newGross,
                        cbmPerCarton = cbmCtn,
                        totalCbm = newTotalCbm,
                        totalAmountUsd = newTotalUnits * matchedProduct.unitPriceUsd
                    )
                    shipmentDao.updateShipmentProduct(updatedProduct)
                } else {
                    val cbmCtn = if (receipt.lengthCm > 0 && receipt.widthCm > 0 && receipt.heightCm > 0) {
                        (receipt.lengthCm * receipt.widthCm * receipt.heightCm) / 1_000_000.0
                    } else 0.025
                    val unitWt = if (receipt.unitsPerCarton > 0 && receipt.cartonGrossWeightKg > 0) receipt.cartonGrossWeightKg / receipt.unitsPerCarton else 0.1
                    val newProd = ShipmentProduct(
                        shipmentId = shipId,
                        productId = receipt.productId,
                        productName = receipt.productName,
                        hsCode = "EXP-HS",
                        description = "${receipt.productName} (Received via ${receipt.receiptNumber})",
                        cartons = receipt.cartonsReceived,
                        unitsPerCarton = receipt.unitsPerCarton,
                        totalUnits = receipt.cartonsReceived * receipt.unitsPerCarton,
                        individualUnitWeightKg = unitWt,
                        totalNetWeightKg = (receipt.cartonsReceived * receipt.unitsPerCarton) * unitWt,
                        cartonGrossWeightKg = receipt.cartonGrossWeightKg,
                        totalGrossWeightKg = receipt.cartonsReceived * receipt.cartonGrossWeightKg,
                        cbmPerCarton = cbmCtn,
                        totalCbm = receipt.cartonsReceived * cbmCtn,
                        countryOfOrigin = "Sri Lanka",
                        unitPriceUsd = 2.50,
                        totalAmountUsd = (receipt.cartonsReceived * receipt.unitsPerCarton) * 2.50
                    )
                    shipmentDao.insertShipmentProduct(newProd)
                }
                recalculateShipmentTotals(shipId)
            }
        }

        // 3. PRESERVE AUDIT TRAIL
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Received Goods (GRN ${receipt.receiptNumber})",
            oldVal = "Prior stock",
            newVal = "+${receipt.cartonsReceived} ctns of ${receipt.productName} in ${receipt.locationBay}${if (!shipId.isNullOrBlank()) " (Updated Shipment $shipId)" else ""}",
            doc = "Warehouse GRN / ${receipt.receiptNumber}",
            shipmentId = receipt.shipmentId ?: "WAREHOUSE"
        )

        notifDao.insertNotification(
            AppNotification(
                title = "Goods Received: ${receipt.receiptNumber}",
                message = "${receipt.cartonsReceived} cartons of ${receipt.productName} received by ${user.fullName} into ${receipt.locationBay}.",
                type = "SUCCESS",
                shipmentId = receipt.shipmentId
            )
        )
    }

    suspend fun assignCartonsToPallet(
        palletId: Long,
        shipmentId: String,
        stockItemId: Long?,
        productName: String,
        cartonsToAssign: Int,
        cartonGrossWeightKg: Double,
        cbmPerCarton: Double,
        user: User
    ) {
        val pallets = palletDao.getPalletsForShipment(shipmentId)
        val pallet = pallets.find { it.id == palletId } ?: return

        val oldCartons = pallet.cartons
        val newCartons = oldCartons + cartonsToAssign
        val newWeight = 25.0 + (newCartons * (if (cartonGrossWeightKg > 0) cartonGrossWeightKg else 8.5))
        val newCbm = 0.20 + (newCartons * (if (cbmPerCarton > 0) cbmPerCarton else 0.025))
        val updatedSummary = if (pallet.productsSummary.isBlank() || pallet.productsSummary == "General Cargo") {
            "$newCartons ctns $productName"
        } else if (pallet.productsSummary.contains(productName)) {
            pallet.productsSummary.replace(Regex("""\d+\s+ctns\s+${Regex.escape(productName)}"""), "$newCartons ctns $productName")
        } else {
            "${pallet.productsSummary}, $cartonsToAssign ctns $productName"
        }

        val updatedPallet = pallet.copy(
            cartons = newCartons,
            weightKg = (newWeight * 10).toInt() / 10.0,
            cbm = (newCbm * 1000).toInt() / 1000.0,
            productsSummary = updatedSummary
        )
        palletDao.updatePallet(updatedPallet)

        // Update WarehouseStockItem if stockItemId provided
        if (stockItemId != null && stockItemId > 0) {
            val stockItems = warehouseDao.getAllStock()
            val item = stockItems.find { it.id == stockItemId }
            if (item != null) {
                val updatedStock = item.copy(
                    availableCartons = maxOf(0, item.availableCartons - cartonsToAssign),
                    allocatedCartons = item.allocatedCartons + cartonsToAssign,
                    lastUpdated = System.currentTimeMillis()
                )
                warehouseDao.updateStock(updatedStock)
            }
        }

        // PRESERVE AUDIT TRAIL
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Assigned Cartons to Pallet",
            oldVal = "${pallet.palletNumber}: $oldCartons ctns",
            newVal = "${pallet.palletNumber}: $newCartons ctns (+$cartonsToAssign ctns $productName)",
            doc = "Pallet Management",
            shipmentId = shipmentId
        )
    }

    suspend fun recordCartonMeasurements(
        stockItemId: Long,
        lengthCm: Double,
        widthCm: Double,
        heightCm: Double,
        cartonGrossWeightKg: Double,
        cartonQuantity: Int,
        locationBay: String,
        user: User
    ) {
        val stockItems = warehouseDao.getAllStock()
        val item = stockItems.find { it.id == stockItemId } ?: return
        val oldDesc = "${item.lengthCm}x${item.widthCm}x${item.heightCm} cm, ${item.cartonGrossWeightKg} kg, ${item.totalReceivedCartons} ctns"
        val cbm = if (lengthCm > 0 && widthCm > 0 && heightCm > 0) {
            (lengthCm * widthCm * heightCm) / 1_000_000.0
        } else item.cbmPerCarton

        val updatedStock = item.copy(
            lengthCm = if (lengthCm > 0) lengthCm else item.lengthCm,
            widthCm = if (widthCm > 0) widthCm else item.widthCm,
            heightCm = if (heightCm > 0) heightCm else item.heightCm,
            cartonGrossWeightKg = if (cartonGrossWeightKg > 0) cartonGrossWeightKg else item.cartonGrossWeightKg,
            cbmPerCarton = cbm,
            availableCartons = if (cartonQuantity > 0) cartonQuantity else item.availableCartons,
            totalReceivedCartons = if (cartonQuantity > 0) maxOf(item.totalReceivedCartons, cartonQuantity) else item.totalReceivedCartons,
            locationBay = if (locationBay.isNotBlank()) locationBay else item.locationBay,
            lastUpdated = System.currentTimeMillis()
        )
        warehouseDao.updateStock(updatedStock)

        val newDesc = "${updatedStock.lengthCm}x${updatedStock.widthCm}x${updatedStock.heightCm} cm, ${updatedStock.cartonGrossWeightKg} kg, ${updatedStock.availableCartons} ctns in ${updatedStock.locationBay}"

        // PRESERVE AUDIT TRAIL
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Recorded Carton Dimensions & Weight",
            oldVal = oldDesc,
            newVal = newDesc,
            doc = "Warehouse Stock / ${item.productName}",
            shipmentId = "WAREHOUSE"
        )
    }

    suspend fun recordDispatch(
        dispatch: WarehouseDispatch,
        createDeliveryNote: Boolean,
        user: User
    ): DeliveryNote? {
        warehouseDao.insertDispatch(dispatch)

        // Update linked shipment status
        val shipment = shipmentDao.getShipmentById(dispatch.shipmentId)
        if (shipment != null) {
            val updatedShipment = shipment.copy(
                status = ShipmentStatus.LOADED,
                updatedAt = System.currentTimeMillis()
            )
            shipmentDao.updateShipment(updatedShipment)
        }

        // Update warehouse stock dispatched counts
        val stockItems = warehouseDao.getAllStock()
        if (stockItems.isNotEmpty()) {
            for (item in stockItems) {
                if (item.allocatedCartons > 0) {
                    val toDispatch = minOf(item.allocatedCartons, dispatch.totalCartonsDispatched)
                    if (toDispatch > 0) {
                        val updated = item.copy(
                            allocatedCartons = maxOf(0, item.allocatedCartons - toDispatch),
                            dispatchedCartons = item.dispatchedCartons + toDispatch,
                            lastUpdated = System.currentTimeMillis()
                        )
                        warehouseDao.updateStock(updated)
                    }
                }
            }
        }

        var deliveryNote: DeliveryNote? = null
        if (createDeliveryNote) {
            val noteNumber = if (dispatch.deliveryNoteNumber.isNotBlank()) {
                dispatch.deliveryNoteNumber
            } else {
                "DN-${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}-${(100..999).random()}"
            }
            val dn = DeliveryNote(
                noteNumber = noteNumber,
                shipmentId = dispatch.shipmentId,
                date = dispatch.dispatchDate,
                consigneeName = dispatch.customer,
                consigneeAddress = shipment?.placeOfDelivery ?: "Overseas Port / Terminal",
                destinationPort = shipment?.portOfDischarge ?: "Destination Port",
                containerNumber = dispatch.containerNumber,
                sealNumber = dispatch.sealNumber,
                vehicleNumber = dispatch.vehicleNumber,
                driverName = dispatch.driverName,
                driverPhone = dispatch.driverNic,
                itemsSummary = "${dispatch.totalCartonsDispatched} Cartons on ${dispatch.totalPalletsDispatched} Pallets / Stuffed into ${dispatch.containerNumber}",
                totalCartons = dispatch.totalCartonsDispatched,
                totalPallets = dispatch.totalPalletsDispatched,
                totalGrossWeightKg = dispatch.totalGrossWeightKg,
                totalCbm = dispatch.totalCbm,
                preparedBy = user.fullName,
                receivedBySignature = "${dispatch.driverName} (Haulier / Driver)",
                notes = "Container loaded and sealed with bolt seal ${dispatch.sealNumber} at Colombo Logistics Hub."
            )
            warehouseDao.insertDeliveryNote(dn)
            deliveryNote = dn
        }

        // PRESERVE AUDIT TRAIL
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Recorded Warehouse Dispatch",
            oldVal = "Warehouse Terminal Stock",
            newVal = "${dispatch.dispatchNumber}: ${dispatch.totalCartonsDispatched} ctns, ${dispatch.totalPalletsDispatched} pallets on Truck ${dispatch.vehicleNumber} (Container ${dispatch.containerNumber})",
            doc = "Warehouse Dispatch / ${dispatch.dispatchNumber}",
            shipmentId = dispatch.shipmentId
        )

        notifDao.insertNotification(
            AppNotification(
                title = "Dispatch Recorded: ${dispatch.dispatchNumber}",
                message = "Dispatched ${dispatch.totalCartonsDispatched} ctns on Truck ${dispatch.vehicleNumber} for ${dispatch.shipmentId}.",
                type = "SUCCESS",
                shipmentId = dispatch.shipmentId
            )
        )

        return deliveryNote
    }

    suspend fun createDeliveryNote(note: DeliveryNote, user: User) {
        warehouseDao.insertDeliveryNote(note)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Created Delivery Note",
            oldVal = "Draft",
            newVal = "${note.noteNumber} for ${note.consigneeName} (${note.totalCartons} Cartons / Container ${note.containerNumber})",
            doc = "Delivery Note / ${note.noteNumber}",
            shipmentId = note.shipmentId
        )
    }

    suspend fun updateDeliveryNote(note: DeliveryNote, user: User) {
        warehouseDao.insertDeliveryNote(note)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Updated Delivery Note",
            oldVal = "${note.noteNumber}",
            newVal = "Updated: ${note.totalCartons} ctns, ${note.totalPallets} pallets",
            doc = "Delivery Note / ${note.noteNumber}",
            shipmentId = note.shipmentId
        )
    }

    suspend fun deletePallet(pallet: Pallet, user: User) {
        palletDao.deletePallet(pallet)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Deleted Pallet",
            oldVal = "${pallet.palletNumber} (${pallet.cartons} ctns)",
            newVal = "Removed from Load Plan",
            doc = "Pallet Management",
            shipmentId = pallet.shipmentId
        )
    }

    suspend fun updateWarehouseStockItem(item: WarehouseStockItem, user: User) {
        warehouseDao.updateStock(item)
        logAction(
            userName = user.fullName,
            userRole = user.role.label,
            action = "Updated Warehouse Stock Item",
            oldVal = "${item.productName}",
            newVal = "Available: ${item.availableCartons}, Bay: ${item.locationBay}",
            doc = "Warehouse Stock",
            shipmentId = "WAREHOUSE"
        )
    }

    suspend fun getStockByBarcode(barcode: String): WarehouseStockItem? =
        warehouseDao.getStockByBarcode(barcode)

    // ==========================================
    // OFFLINE DRAFT OPERATIONS
    // ==========================================

    suspend fun saveOfflineDraft(draft: OfflineDraft) = draftDao.saveDraft(draft)
    suspend fun getOfflineDraft(draftId: String): OfflineDraft? = draftDao.getDraftById(draftId)
    suspend fun deleteOfflineDraft(draftId: String) = draftDao.deleteDraft(draftId)
    suspend fun clearAllOfflineDrafts() = draftDao.clearAllDrafts()

    private suspend fun logAction(
        userName: String,
        userRole: String,
        action: String,
        oldVal: String,
        newVal: String,
        doc: String,
        shipmentId: String
    ) {
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        auditDao.insertLog(
            AuditLog(
                formattedDate = sdf.format(Date()),
                userName = userName,
                userRole = userRole,
                action = action,
                oldValue = oldVal,
                newValue = newVal,
                document = doc,
                shipmentId = shipmentId
            )
        )
    }
}
