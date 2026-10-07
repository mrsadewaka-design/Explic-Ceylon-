package com.example

import com.example.data.model.*
import com.example.ui.components.ShipmentMilestone
import com.example.ui.components.ShipmentSearchScope
import org.junit.Assert.*
import org.junit.Test

class ShipmentProgressAndSearchTest {

    private fun sampleShipments(): List<Shipment> {
        return listOf(
            Shipment(
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
                placeOfDelivery = "Deer Park, NY, USA",
                vessel = "CMA CGM MEXICO",
                voyage = "1TUIUS1MA",
                etd = "2026-10-08",
                containerNumber = "EMCU6409544/20",
                masterBlNumber = "CMDU10294821",
                amsBlNumber = "AMS-CMDU-84920",
                status = ShipmentStatus.PACKED
            ),
            Shipment(
                id = "EXP_AIR_2026_088",
                reference = "AIR-LK-UK-088",
                customer = "GLOBAL FINE FOODS LTD",
                poNumber = "PO-GFF-1029",
                invoiceNumber = "EXP-INV-2026-088",
                invoiceDate = "2026-09-29",
                currency = "GBP",
                shippingMode = ShippingMode.AIR,
                portOfLoading = "CMB Airport",
                portOfDischarge = "LHR London",
                placeOfDelivery = "London Distribution Center",
                vessel = "SriLankan Airlines UL503",
                voyage = "UL503",
                etd = "2026-10-02",
                containerNumber = "ULD-49201",
                masterBlNumber = "AWB-603-91823901",
                amsBlNumber = "",
                status = ShipmentStatus.DOCUMENTATION
            ),
            Shipment(
                id = "EXP_SEA_2026_099",
                reference = "SEA-EU-2026-099",
                customer = "HAMBURG BEVERAGES GMBH",
                poNumber = "PO-HBG-5541",
                invoiceNumber = "EXP-INV-2026-099",
                invoiceDate = "2026-09-30",
                currency = "EUR",
                shippingMode = ShippingMode.SEA,
                portOfLoading = "Colombo, Sri Lanka",
                portOfDischarge = "Hamburg, Germany",
                placeOfDelivery = "Hamburg Port Terminal",
                vessel = "MSC OSCAR",
                voyage = "OSC-26",
                etd = "2026-10-15",
                containerNumber = "MSCU9284102/40",
                masterBlNumber = "MSCU1092840",
                amsBlNumber = "",
                status = ShipmentStatus.SHIPPED
            )
        )
    }

    @Test
    fun testShipmentMilestoneIndexProgression() {
        // Milestone 0: Packing
        assertEquals(0, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.PACKED))

        // Milestone 1: Invoiced (DOCUMENTATION stage)
        assertEquals(1, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.DOCUMENTATION))

        // Milestone 2: Validated (ISF_FILED, BOOKING_CONFIRMED, LOADED, BL_RELEASED)
        assertEquals(2, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.ISF_FILED))
        assertEquals(2, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.BOOKING_CONFIRMED))
        assertEquals(2, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.LOADED))
        assertEquals(2, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.BL_RELEASED))

        // Milestone 3: Dispatched (SHIPPED, DELIVERED)
        assertEquals(3, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.SHIPPED))
        assertEquals(3, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.DELIVERED))

        // Before packing: -1
        assertEquals(-1, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.NEW))
        assertEquals(-1, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.ORDER_CONFIRMED))
        assertEquals(-1, ShipmentMilestone.getMilestoneIndex(ShipmentStatus.PRODUCTION))
    }

    @Test
    fun testShipmentMilestonesTitlesAndOrder() {
        val titles = ShipmentMilestone.entries.map { it.title }
        assertEquals(listOf("Packing", "Invoiced", "Validated", "Dispatched"), titles)

        assertEquals(ShipmentStatus.PACKED, ShipmentMilestone.PACKING.targetStatus)
        assertEquals(ShipmentStatus.DOCUMENTATION, ShipmentMilestone.INVOICED.targetStatus)
        assertEquals(ShipmentStatus.ISF_FILED, ShipmentMilestone.VALIDATED.targetStatus)
        assertEquals(ShipmentStatus.SHIPPED, ShipmentMilestone.DISPATCHED.targetStatus)
    }

    @Test
    fun testFilterShipmentsByTrackingId() {
        val shipments = sampleShipments()

        fun filter(query: String, scope: ShipmentSearchScope): List<Shipment> {
            val q = query.trim().lowercase()
            return shipments.filter { s ->
                if (q.isEmpty()) true
                else when (scope) {
                    ShipmentSearchScope.TRACKING_ID -> {
                        s.id.lowercase().contains(q) ||
                        s.reference.lowercase().contains(q) ||
                        s.masterBlNumber.lowercase().contains(q) ||
                        s.amsBlNumber.lowercase().contains(q) ||
                        s.containerNumber.lowercase().contains(q)
                    }
                    ShipmentSearchScope.CLIENT_NAME -> {
                        s.customer.lowercase().contains(q)
                    }
                    ShipmentSearchScope.ALL -> {
                        s.id.lowercase().contains(q) ||
                        s.reference.lowercase().contains(q) ||
                        s.customer.lowercase().contains(q) ||
                        s.poNumber.lowercase().contains(q) ||
                        s.invoiceNumber.lowercase().contains(q) ||
                        s.containerNumber.lowercase().contains(q) ||
                        s.vessel.lowercase().contains(q) ||
                        s.voyage.lowercase().contains(q) ||
                        s.masterBlNumber.lowercase().contains(q) ||
                        s.amsBlNumber.lowercase().contains(q)
                    }
                }
            }
        }

        // Search by tracking ID
        val resultsById = filter("ECGMIX", ShipmentSearchScope.TRACKING_ID)
        assertEquals(1, resultsById.size)
        assertEquals("ECGMIX_092826_016", resultsById[0].id)

        // Search by container tracking number
        val resultsByContainer = filter("EMCU6409544", ShipmentSearchScope.TRACKING_ID)
        assertEquals(1, resultsByContainer.size)
        assertEquals("ECGMIX_092826_016", resultsByContainer[0].id)

        // Search by B/L tracking number
        val resultsByBl = filter("CMDU10294821", ShipmentSearchScope.TRACKING_ID)
        assertEquals(1, resultsByBl.size)

        // Searching client name under TRACKING_ID scope should yield 0 results
        val resultsClientUnderTracking = filter("OUTLAW", ShipmentSearchScope.TRACKING_ID)
        assertEquals(0, resultsClientUnderTracking.size)
    }

    @Test
    fun testFilterShipmentsByClientName() {
        val shipments = sampleShipments()

        fun filter(query: String, scope: ShipmentSearchScope): List<Shipment> {
            val q = query.trim().lowercase()
            return shipments.filter { s ->
                if (q.isEmpty()) true
                else when (scope) {
                    ShipmentSearchScope.TRACKING_ID -> {
                        s.id.lowercase().contains(q) ||
                        s.reference.lowercase().contains(q) ||
                        s.masterBlNumber.lowercase().contains(q) ||
                        s.amsBlNumber.lowercase().contains(q) ||
                        s.containerNumber.lowercase().contains(q)
                    }
                    ShipmentSearchScope.CLIENT_NAME -> {
                        s.customer.lowercase().contains(q)
                    }
                    ShipmentSearchScope.ALL -> {
                        s.id.lowercase().contains(q) ||
                        s.reference.lowercase().contains(q) ||
                        s.customer.lowercase().contains(q) ||
                        s.poNumber.lowercase().contains(q) ||
                        s.invoiceNumber.lowercase().contains(q) ||
                        s.containerNumber.lowercase().contains(q) ||
                        s.vessel.lowercase().contains(q) ||
                        s.voyage.lowercase().contains(q) ||
                        s.masterBlNumber.lowercase().contains(q) ||
                        s.amsBlNumber.lowercase().contains(q)
                    }
                }
            }
        }

        // Search by client name
        val resultsByClient = filter("Outlaw Dip", ShipmentSearchScope.CLIENT_NAME)
        assertEquals(1, resultsByClient.size)
        assertEquals("OUTLAW DIP COMPANY INC.", resultsByClient[0].customer)

        val resultsByGmbH = filter("Hamburg", ShipmentSearchScope.CLIENT_NAME)
        assertEquals(1, resultsByGmbH.size)
        assertEquals("HAMBURG BEVERAGES GMBH", resultsByGmbH[0].customer)

        // Searching tracking ID under CLIENT_NAME scope should yield 0 results
        val resultsTrackingUnderClient = filter("ECGMIX", ShipmentSearchScope.CLIENT_NAME)
        assertEquals(0, resultsTrackingUnderClient.size)

        // Search under ALL scope should find either
        val resultsAllClient = filter("Global Fine", ShipmentSearchScope.ALL)
        assertEquals(1, resultsAllClient.size)
        val resultsAllTracking = filter("UL503", ShipmentSearchScope.ALL)
        assertEquals(1, resultsAllTracking.size)
    }
}
