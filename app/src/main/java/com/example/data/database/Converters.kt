package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.VIEWER)

    @TypeConverter
    fun fromShippingMode(value: ShippingMode): String = value.name

    @TypeConverter
    fun toShippingMode(value: String): ShippingMode = runCatching { ShippingMode.valueOf(value) }.getOrDefault(ShippingMode.SEA)

    @TypeConverter
    fun fromShipmentStatus(value: ShipmentStatus): String = value.name

    @TypeConverter
    fun toShipmentStatus(value: String): ShipmentStatus {
        if (value == "BOOKED") return ShipmentStatus.BOOKING_CONFIRMED
        return runCatching { ShipmentStatus.valueOf(value) }.getOrDefault(ShipmentStatus.DOCUMENTATION)
    }

    @TypeConverter
    fun fromDocumentStatus(value: DocumentStatus): String = value.name

    @TypeConverter
    fun toDocumentStatus(value: String): DocumentStatus = runCatching { DocumentStatus.valueOf(value) }.getOrDefault(DocumentStatus.DRAFT)

    @TypeConverter
    fun fromPartyType(value: PartyType): String = value.name

    @TypeConverter
    fun toPartyType(value: String): PartyType = runCatching { PartyType.valueOf(value) }.getOrDefault(PartyType.EXPORTER)
}
