package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiExportAssistant
import com.example.data.model.*
import com.example.data.repository.ExportRepository
import com.example.engine.ValidationEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ExportViewModel(private val repository: ExportRepository) : ViewModel() {

    val allShipments: StateFlow<List<Shipment>> = repository.allShipments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParties: StateFlow<List<Party>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWarehouseStock: StateFlow<List<WarehouseStockItem>> = repository.allWarehouseStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoodsReceipts: StateFlow<List<GoodsReceipt>> = repository.allGoodsReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWarehouseDispatches: StateFlow<List<WarehouseDispatch>> = repository.allWarehouseDispatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeliveryNotes: StateFlow<List<DeliveryNote>> = repository.allDeliveryNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOfflineDrafts: StateFlow<List<OfflineDraft>> = repository.allOfflineDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<User>(
        User(
            id = "usr_01",
            username = "roshan.p",
            fullName = "Roshan Perera",
            email = "roshan@explicceylon.com",
            role = UserRole.DOCUMENTATION_OFFICER,
            department = "Export Documentation & Compliance",
            telephone = "+94 77 123 4567"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _selectedShipmentId = MutableStateFlow("ECGMIX_092826_016")
    val selectedShipmentId: StateFlow<String> = _selectedShipmentId.asStateFlow()

    private val _syncState = MutableStateFlow("ONLINE")
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    // Global Search & Filters
    val searchQuery = MutableStateFlow("")
    val filterStatus = MutableStateFlow<ShipmentStatus?>(null)
    val filterMode = MutableStateFlow<ShippingMode?>(null)

    // Current active shipment and linked documents
    val currentShipment: StateFlow<Shipment?> = _selectedShipmentId
        .flatMapLatest { id -> repository.getShipment(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentProducts: StateFlow<List<ShipmentProduct>> = _selectedShipmentId
        .flatMapLatest { id -> repository.getShipmentProducts(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentPallets: StateFlow<List<Pallet>> = _selectedShipmentId
        .flatMapLatest { id -> repository.getPallets(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentInvoice: StateFlow<CommercialInvoice?> = _selectedShipmentId
        .flatMapLatest { id -> repository.getCommercialInvoice(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentPackingList: StateFlow<PackingList?> = _selectedShipmentId
        .flatMapLatest { id -> repository.getPackingList(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentIsf: StateFlow<IsfFiling?> = _selectedShipmentId
        .flatMapLatest { id -> repository.getIsfFiling(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentBl: StateFlow<BlInstructions?> = _selectedShipmentId
        .flatMapLatest { id -> repository.getBlInstructions(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive validation summary
    val validationSummary: StateFlow<ShipmentValidationSummary?> = combine(
        listOf(
            currentShipment,
            currentProducts,
            currentPallets,
            currentInvoice,
            currentPackingList,
            currentIsf,
            currentBl
        )
    ) { args ->
        val ship = args[0] as? Shipment
        @Suppress("UNCHECKED_CAST")
        val prods = args[1] as? List<ShipmentProduct> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val pals = args[2] as? List<Pallet> ?: emptyList()
        val inv = args[3] as? CommercialInvoice
        val pl = args[4] as? PackingList
        val isf = args[5] as? IsfFiling
        val bl = args[6] as? BlInstructions

        if (ship != null) {
            ValidationEngine.validateShipment(ship, prods, pals, inv, pl, isf, bl)
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // AI Chat History
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                sender = "AI",
                text = "Hello! I am 'Ask Export AI' for Explic Ceylon (Pvt) Ltd. Try asking:\n• \"Check shipment ECGMIX_092826_016.\"\n• \"Create B/L instructions from this shipment.\"\n• \"Check my ISF information.\"\n• \"Explain why these documents don't match.\"\n• \"Find all shipments with missing HS codes.\""
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureMasterProductsSeeded()
        }
    }

    fun selectShipment(id: String) {
        _selectedShipmentId.value = id
    }

    fun switchUser(user: User) {
        _currentUser.value = user
    }

    fun triggerSync() {
        viewModelScope.launch {
            _syncState.value = "SYNCING"
            delay(1200)
            _syncState.value = "SYNC COMPLETE"
            delay(2000)
            _syncState.value = "ONLINE"
        }
    }

    fun toggleOffline() {
        _syncState.value = if (_syncState.value == "OFFLINE") "ONLINE" else "OFFLINE"
    }

    fun createShipment(
        shipment: Shipment,
        products: List<ShipmentProduct>
    ) {
        viewModelScope.launch {
            repository.createShipmentWithDocuments(shipment, products, _currentUser.value)
            _selectedShipmentId.value = shipment.id
        }
    }

    fun updateShipment(shipment: Shipment) {
        viewModelScope.launch {
            repository.updateShipment(shipment, _currentUser.value)
        }
    }

    fun saveProduct(product: ShipmentProduct) {
        viewModelScope.launch {
            repository.saveShipmentProduct(product, _currentUser.value)
        }
    }

    fun deleteProduct(product: ShipmentProduct) {
        viewModelScope.launch {
            repository.deleteShipmentProduct(product, _currentUser.value)
        }
    }

    fun savePallet(pallet: Pallet) {
        viewModelScope.launch {
            repository.savePallet(pallet, _currentUser.value)
        }
    }

    fun saveCommercialInvoice(invoice: CommercialInvoice) {
        viewModelScope.launch {
            repository.saveCommercialInvoice(invoice, _currentUser.value)
        }
    }

    fun savePackingList(packingList: PackingList) {
        viewModelScope.launch {
            repository.savePackingList(packingList, _currentUser.value)
        }
    }

    fun saveIsfFiling(isf: IsfFiling) {
        viewModelScope.launch {
            repository.saveIsfFiling(isf, _currentUser.value)
        }
    }

    fun saveBlInstructions(bl: BlInstructions) {
        viewModelScope.launch {
            repository.saveBlInstructions(bl, _currentUser.value)
        }
    }

    fun approveOverride(reason: String) {
        val shipId = _selectedShipmentId.value
        viewModelScope.launch {
            repository.approveWarningOverride(shipId, _currentUser.value, reason)
        }
    }

    fun markDocumentStatus(docType: String, status: DocumentStatus) {
        val shipId = _selectedShipmentId.value
        viewModelScope.launch {
            repository.markDocumentStatus(shipId, docType, status, _currentUser.value)
        }
    }

    fun resolveIsfCartonDiscrepancy() {
        // Automatically sync ISF cartons to 505 to resolve warning
        val current = currentIsf.value ?: return
        val pl = currentPackingList.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                cartonsCount = pl.totalCartons,
                totalGrossWeightKg = pl.totalGrossWeightKg,
                status = DocumentStatus.READY
            )
            repository.saveIsfFiling(updated, _currentUser.value)
        }
    }

    fun resolveBlGrossWeightDiscrepancy() {
        val bl = currentBl.value ?: return
        val pl = currentPackingList.value ?: return
        viewModelScope.launch {
            val updated = bl.copy(
                grossWeightKg = pl.totalGrossWeightKg,
                kindOfPackages = "${pl.totalPallets} Pallets / ${pl.totalCartons} Cartons",
                descriptionOfGoods = "505 CARTONS ON 9 PALLETS CONTAINING:\n- 143 CTNS ICE HOCKEY PUCKS\n- 120 CTNS TIN LIDS\n- 110 CTNS PLASTIC CUPS\n- 72 CTNS KOI FISH FEED\n- 60 CTNS DOG FOOD CLUSTERS",
                status = DocumentStatus.READY
            )
            repository.saveBlInstructions(updated, _currentUser.value)
        }
    }

    fun fixAllValidationIssues() {
        val pl = currentPackingList.value ?: return
        val isf = currentIsf.value
        val bl = currentBl.value
        viewModelScope.launch {
            if (isf != null) {
                val updatedIsf = isf.copy(
                    cartonsCount = pl.totalCartons,
                    totalGrossWeightKg = pl.totalGrossWeightKg,
                    status = DocumentStatus.READY
                )
                repository.saveIsfFiling(updatedIsf, _currentUser.value)
            }
            if (bl != null) {
                val updatedBl = bl.copy(
                    grossWeightKg = pl.totalGrossWeightKg,
                    measurementCbm = pl.totalCbm,
                    kindOfPackages = "${pl.totalPallets} Pallets / ${pl.totalCartons} Cartons",
                    descriptionOfGoods = "${pl.totalCartons} CARTONS ON ${pl.totalPallets} PALLETS CONTAINING:\n- 143 CTNS ICE HOCKEY PUCKS\n- 120 CTNS TIN LIDS\n- 110 CTNS PLASTIC CUPS\n- 72 CTNS KOI FISH FEED\n- 60 CTNS DOG FOOD CLUSTERS",
                    status = DocumentStatus.READY
                )
                repository.saveBlInstructions(updatedBl, _currentUser.value)
            }
        }
    }

    fun resetPreviewDiscrepancies() {
        val ship = currentShipment.value ?: return
        val isf = currentIsf.value
        val bl = currentBl.value
        viewModelScope.launch {
            if (isf != null) {
                val resetIsf = isf.copy(
                    cartonsCount = 608,
                    totalGrossWeightKg = 4520.0,
                    status = DocumentStatus.VALIDATION_ERROR
                )
                repository.saveIsfFiling(resetIsf, _currentUser.value)
            }
            if (bl != null) {
                val resetBl = bl.copy(
                    grossWeightKg = 0.0,
                    kindOfPackages = "9 Pallets / 505 Cartons",
                    descriptionOfGoods = "505 CARTONS ON 9 PALLETS CONTAINING:\n- 333 CTNS FISH FEED / ICE HOCKEY PUCKS\n- 120 CTNS TIN LIDS\n- 110 CTNS PLASTIC CUPS\n- 72 CTNS KOI FISH FEED\n- 60 CTNS DOG FOOD CLUSTERS",
                    status = DocumentStatus.VALIDATION_ERROR
                )
                repository.saveBlInstructions(resetBl, _currentUser.value)
            }
            if (ship.isOverrideApproved) {
                val resetShipment = ship.copy(
                    isOverrideApproved = false,
                    overrideApprovedBy = null,
                    overrideNotes = null,
                    status = ShipmentStatus.DOCUMENTATION
                )
                repository.updateShipment(resetShipment, _currentUser.value)
            }
        }
    }

    fun sendAiMessage(prompt: String) {
        val userMsg = AiChatMessage(sender = "USER", text = prompt)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            // If the user references a specific shipment in the prompt (e.g. ECGMIX_092826_016), target that shipment!
            val matchedShipment = allShipments.value.find { s ->
                prompt.contains(s.id, ignoreCase = true) || prompt.contains(s.reference, ignoreCase = true)
            } ?: currentShipment.value ?: allShipments.value.firstOrNull()

            if (matchedShipment != null && matchedShipment.id != _selectedShipmentId.value) {
                _selectedShipmentId.value = matchedShipment.id
            }

            val bundle = if (matchedShipment != null) {
                repository.getFullShipmentBundle(matchedShipment.id)
            } else null

            val responseText = GeminiExportAssistant.queryExportAssistant(
                prompt = prompt,
                currentShipment = bundle?.shipment ?: matchedShipment ?: currentShipment.value,
                products = bundle?.products ?: currentProducts.value,
                invoice = bundle?.invoice ?: currentInvoice.value,
                packingList = bundle?.packingList ?: currentPackingList.value,
                isf = bundle?.isf ?: currentIsf.value,
                bl = bundle?.bl ?: currentBl.value,
                allShipments = allShipments.value,
                validationSummary = bundle?.validationSummary ?: validationSummary.value,
                masterProducts = allProducts.value,
                currentUser = _currentUser.value
            )
            _isAiLoading.value = false
            _aiMessages.value = _aiMessages.value + AiChatMessage(sender = "AI", text = responseText)
        }
    }

    fun addProductMaster(product: Product) {
        viewModelScope.launch {
            repository.addProductMaster(product)
        }
    }

    fun updateProductMaster(product: Product) {
        viewModelScope.launch {
            repository.updateProductMaster(product)
        }
    }

    fun deleteProductMaster(product: Product) {
        viewModelScope.launch {
            repository.deleteProductMaster(product)
        }
    }

    fun addParty(party: Party) {
        viewModelScope.launch {
            repository.addParty(party)
        }
    }

    fun updateParty(party: Party) {
        viewModelScope.launch {
            repository.updateParty(party)
        }
    }

    fun deleteParty(party: Party) {
        viewModelScope.launch {
            repository.deleteParty(party)
        }
    }

    // ==========================================
    // WAREHOUSE OPERATIONS
    // ==========================================

    fun receiveGoods(receipt: GoodsReceipt, autoUpdateShipment: Boolean = true) {
        viewModelScope.launch {
            repository.receiveGoods(receipt, autoUpdateShipment, _currentUser.value)
        }
    }

    fun assignCartonsToPallet(
        palletId: Long,
        shipmentId: String,
        stockItemId: Long?,
        productName: String,
        cartonsToAssign: Int,
        cartonGrossWeightKg: Double = 8.5,
        cbmPerCarton: Double = 0.025
    ) {
        viewModelScope.launch {
            repository.assignCartonsToPallet(
                palletId = palletId,
                shipmentId = shipmentId,
                stockItemId = stockItemId,
                productName = productName,
                cartonsToAssign = cartonsToAssign,
                cartonGrossWeightKg = cartonGrossWeightKg,
                cbmPerCarton = cbmPerCarton,
                user = _currentUser.value
            )
        }
    }

    fun recordCartonMeasurements(
        stockItemId: Long,
        lengthCm: Double,
        widthCm: Double,
        heightCm: Double,
        cartonGrossWeightKg: Double,
        cartonQuantity: Int = 0,
        locationBay: String = ""
    ) {
        viewModelScope.launch {
            repository.recordCartonMeasurements(
                stockItemId = stockItemId,
                lengthCm = lengthCm,
                widthCm = widthCm,
                heightCm = heightCm,
                cartonGrossWeightKg = cartonGrossWeightKg,
                cartonQuantity = cartonQuantity,
                locationBay = locationBay,
                user = _currentUser.value
            )
        }
    }

    fun recordDispatch(
        dispatch: WarehouseDispatch,
        createDeliveryNote: Boolean = true,
        onSuccess: (DeliveryNote?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val dn = repository.recordDispatch(dispatch, createDeliveryNote, _currentUser.value)
            onSuccess(dn)
        }
    }

    fun createDeliveryNote(note: DeliveryNote) {
        viewModelScope.launch {
            repository.createDeliveryNote(note, _currentUser.value)
        }
    }

    fun updateDeliveryNote(note: DeliveryNote) {
        viewModelScope.launch {
            repository.updateDeliveryNote(note, _currentUser.value)
        }
    }

    fun deletePallet(pallet: Pallet) {
        viewModelScope.launch {
            repository.deletePallet(pallet, _currentUser.value)
        }
    }

    fun updateWarehouseStockItem(item: WarehouseStockItem) {
        viewModelScope.launch {
            repository.updateWarehouseStockItem(item, _currentUser.value)
        }
    }

    // ==========================================
    // OFFLINE DRAFT OPERATIONS
    // ==========================================

    fun saveOfflineDraft(
        draftId: String,
        draftType: String,
        title: String,
        summary: String,
        payloadJson: String
    ) {
        viewModelScope.launch {
            repository.saveOfflineDraft(
                OfflineDraft(
                    draftId = draftId,
                    draftType = draftType,
                    title = title,
                    summary = summary,
                    payloadJson = payloadJson,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteOfflineDraft(draftId: String) {
        viewModelScope.launch {
            repository.deleteOfflineDraft(draftId)
        }
    }

    fun clearAllOfflineDrafts() {
        viewModelScope.launch {
            repository.clearAllOfflineDrafts()
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
}

class ExportViewModelFactory(private val repository: ExportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
