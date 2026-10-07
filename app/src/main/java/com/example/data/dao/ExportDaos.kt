package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {
    @Query("SELECT * FROM shipments ORDER BY createdAt DESC")
    fun getAllShipments(): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE id = :shipmentId LIMIT 1")
    fun getShipmentByIdFlow(shipmentId: String): Flow<Shipment?>

    @Query("SELECT * FROM shipments WHERE id = :shipmentId LIMIT 1")
    suspend fun getShipmentById(shipmentId: String): Shipment?

    @Query("SELECT * FROM shipments WHERE status = :status ORDER BY createdAt DESC")
    fun getShipmentsByStatusFlow(status: ShipmentStatus): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE customer LIKE '%' || :query || '%' OR id LIKE '%' || :query || '%' OR reference LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchShipmentsFlow(query: String): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE isSyncPending = 1 ORDER BY updatedAt DESC")
    fun getPendingSyncShipmentsFlow(): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE isSyncPending = 1")
    suspend fun getPendingSyncShipments(): List<Shipment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: Shipment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllShipments(shipments: List<Shipment>)

    @Update
    suspend fun updateShipment(shipment: Shipment)

    @Query("UPDATE shipments SET isSyncPending = :isPending, syncStatus = :status, lastSyncedAt = :syncedAt WHERE id = :shipmentId")
    suspend fun updateSyncStatus(shipmentId: String, isPending: Boolean, status: String, syncedAt: Long)

    @Query("UPDATE shipments SET status = :status, updatedAt = :timestamp WHERE id = :shipmentId")
    suspend fun updateShipmentStatus(shipmentId: String, status: ShipmentStatus, timestamp: Long)

    @Delete
    suspend fun deleteShipment(shipment: Shipment)

    @Query("DELETE FROM shipments WHERE id = :shipmentId")
    suspend fun deleteShipmentById(shipmentId: String)

    // Shipment Products
    @Query("SELECT * FROM shipment_products WHERE shipmentId = :shipmentId")
    fun getProductsForShipmentFlow(shipmentId: String): Flow<List<ShipmentProduct>>

    @Query("SELECT * FROM shipment_products WHERE shipmentId = :shipmentId")
    suspend fun getProductsForShipment(shipmentId: String): List<ShipmentProduct>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipmentProduct(product: ShipmentProduct)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipmentProducts(products: List<ShipmentProduct>)

    @Update
    suspend fun updateShipmentProduct(product: ShipmentProduct)

    @Query("DELETE FROM shipment_products WHERE id = :productId")
    suspend fun deleteShipmentProduct(productId: Long)

    @Query("DELETE FROM shipment_products WHERE shipmentId = :shipmentId")
    suspend fun deleteProductsForShipment(shipmentId: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsList(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductByIdFlow(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE hsCode = :hsCode LIMIT 1")
    suspend fun getProductByHsCode(hsCode: String): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR hsCode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProductsFlow(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategoryFlow(category: String): Flow<List<Product>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getAllCategoriesFlow(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE isSyncPending = 1")
    fun getPendingSyncProductsFlow(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET isSyncPending = :isPending, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateProductSyncStatus(id: Long, isPending: Boolean, timestamp: Long)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)
}

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY companyName ASC")
    fun getAllParties(): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE partyType = :type ORDER BY companyName ASC")
    fun getPartiesByType(type: PartyType): Flow<List<Party>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: Party)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllParties(parties: List<Party>)

    @Update
    suspend fun updateParty(party: Party)

    @Delete
    suspend fun deleteParty(party: Party)
}

@Dao
interface PalletDao {
    @Query("SELECT * FROM pallets WHERE shipmentId = :shipmentId ORDER BY id ASC")
    fun getPalletsForShipmentFlow(shipmentId: String): Flow<List<Pallet>>

    @Query("SELECT * FROM pallets ORDER BY id ASC")
    fun getAllPalletsFlow(): Flow<List<Pallet>>

    @Query("SELECT * FROM pallets WHERE shipmentId = :shipmentId ORDER BY id ASC")
    suspend fun getPalletsForShipment(shipmentId: String): List<Pallet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPallet(pallet: Pallet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPallets(pallets: List<Pallet>)

    @Update
    suspend fun updatePallet(pallet: Pallet)

    @Delete
    suspend fun deletePallet(pallet: Pallet)

    @Query("DELETE FROM pallets WHERE shipmentId = :shipmentId")
    suspend fun deletePalletsForShipment(shipmentId: String)
}

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouse_stock ORDER BY productName ASC")
    fun getAllStockFlow(): Flow<List<WarehouseStockItem>>

    @Query("SELECT * FROM warehouse_stock ORDER BY productName ASC")
    suspend fun getAllStock(): List<WarehouseStockItem>

    @Query("SELECT * FROM warehouse_stock WHERE productId = :productId LIMIT 1")
    suspend fun getStockByProductId(productId: Long): WarehouseStockItem?

    @Query("SELECT * FROM warehouse_stock WHERE barcode = :code LIMIT 1")
    suspend fun getStockByBarcode(code: String): WarehouseStockItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(item: WarehouseStockItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStock(items: List<WarehouseStockItem>)

    @Update
    suspend fun updateStock(item: WarehouseStockItem)

    // Goods Receipts
    @Query("SELECT * FROM goods_receipts ORDER BY id DESC")
    fun getAllReceiptsFlow(): Flow<List<GoodsReceipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: GoodsReceipt)

    // Dispatches
    @Query("SELECT * FROM warehouse_dispatches ORDER BY id DESC")
    fun getAllDispatchesFlow(): Flow<List<WarehouseDispatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatch(dispatch: WarehouseDispatch)

    // Delivery Notes
    @Query("SELECT * FROM delivery_notes ORDER BY id DESC")
    fun getAllDeliveryNotesFlow(): Flow<List<DeliveryNote>>

    @Query("SELECT * FROM delivery_notes WHERE id = :id LIMIT 1")
    suspend fun getDeliveryNoteById(id: Long): DeliveryNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryNote(note: DeliveryNote): Long
}

@Dao
interface DocumentDao {
    // Unified Documents Table (PDF, Excel, Dossier, Certificate records)
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun getAllDocumentsFlow(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE shipmentId = :shipmentId ORDER BY createdAt DESC")
    fun getDocumentsForShipmentFlow(shipmentId: String): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE shipmentId = :shipmentId ORDER BY createdAt DESC")
    suspend fun getDocumentsForShipment(shipmentId: String): List<Document>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentByIdFlow(id: Long): Flow<Document?>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): Document?

    @Query("SELECT * FROM documents WHERE documentNumber = :docNumber LIMIT 1")
    suspend fun getDocumentByNumber(docNumber: String): Document?

    @Query("SELECT * FROM documents WHERE shipmentId = :shipmentId AND documentType = :docType LIMIT 1")
    suspend fun getDocumentByShipmentAndType(shipmentId: String, docType: String): Document?

    @Query("SELECT * FROM documents WHERE documentType = :docType ORDER BY createdAt DESC")
    fun getDocumentsByTypeFlow(docType: String): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR documentNumber LIKE '%' || :query || '%' OR shipmentId LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchDocumentsFlow(query: String): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE isSyncPending = 1 ORDER BY updatedAt DESC")
    fun getPendingSyncDocumentsFlow(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDocuments(documents: List<Document>)

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("DELETE FROM documents WHERE shipmentId = :shipmentId")
    suspend fun deleteDocumentsForShipment(shipmentId: String)

    @Query("UPDATE documents SET isSyncPending = :isPending, lastSyncedAt = :timestamp WHERE id = :id")
    suspend fun updateDocumentSyncStatus(id: Long, isPending: Boolean, timestamp: Long)

    // Commercial Invoice
    @Query("SELECT * FROM commercial_invoices WHERE shipmentId = :shipmentId LIMIT 1")
    fun getInvoiceFlow(shipmentId: String): Flow<CommercialInvoice?>

    @Query("SELECT * FROM commercial_invoices WHERE shipmentId = :shipmentId LIMIT 1")
    suspend fun getInvoice(shipmentId: String): CommercialInvoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInvoice(invoice: CommercialInvoice)

    // Packing List
    @Query("SELECT * FROM packing_lists WHERE shipmentId = :shipmentId LIMIT 1")
    fun getPackingListFlow(shipmentId: String): Flow<PackingList?>

    @Query("SELECT * FROM packing_lists WHERE shipmentId = :shipmentId LIMIT 1")
    suspend fun getPackingList(shipmentId: String): PackingList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePackingList(packingList: PackingList)

    // ISF Filing
    @Query("SELECT * FROM isf_filings WHERE shipmentId = :shipmentId LIMIT 1")
    fun getIsfFilingFlow(shipmentId: String): Flow<IsfFiling?>

    @Query("SELECT * FROM isf_filings WHERE shipmentId = :shipmentId LIMIT 1")
    suspend fun getIsfFiling(shipmentId: String): IsfFiling?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveIsfFiling(isf: IsfFiling)

    // B/L Instructions
    @Query("SELECT * FROM bl_instructions WHERE shipmentId = :shipmentId LIMIT 1")
    fun getBlInstructionsFlow(shipmentId: String): Flow<BlInstructions?>

    @Query("SELECT * FROM bl_instructions WHERE shipmentId = :shipmentId LIMIT 1")
    suspend fun getBlInstructions(shipmentId: String): BlInstructions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBlInstructions(bl: BlInstructions)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE shipmentId = :shipmentId ORDER BY timestamp DESC")
    fun getLogsForShipment(shipmentId: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): AuditLog?

    @Query("SELECT * FROM audit_logs WHERE userName = :userName ORDER BY timestamp DESC")
    fun getLogsByUserFlow(userName: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE document = :document ORDER BY timestamp DESC")
    fun getLogsByDocumentFlow(document: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE action LIKE '%' || :query || '%' OR document LIKE '%' || :query || '%' OR shipmentId LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchLogsFlow(query: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE isSyncPending = 1 ORDER BY timestamp DESC")
    fun getPendingSyncLogsFlow(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<AuditLog>)

    @Query("UPDATE audit_logs SET isSyncPending = 0 WHERE id IN (:ids)")
    suspend fun markLogsAsSynced(ids: List<Long>)

    @Query("DELETE FROM audit_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun pruneOldLogs(cutoffTimestamp: Long)

    @Query("DELETE FROM audit_logs WHERE shipmentId = :shipmentId")
    suspend fun deleteLogsForShipment(shipmentId: String)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAllLogs()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Insert
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface OfflineDraftDao {
    @Query("SELECT * FROM offline_drafts ORDER BY lastUpdated DESC")
    fun getAllDraftsFlow(): Flow<List<OfflineDraft>>

    @Query("SELECT * FROM offline_drafts WHERE draftId = :draftId LIMIT 1")
    suspend fun getDraftById(draftId: String): OfflineDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: OfflineDraft)

    @Query("DELETE FROM offline_drafts WHERE draftId = :draftId")
    suspend fun deleteDraft(draftId: String)

    @Query("DELETE FROM offline_drafts")
    suspend fun clearAllDrafts()
}

