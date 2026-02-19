package com.mobil.travelreservation.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Trip
 * Sefer (Otobüs veya Uçak) bilgilerini tutar.
 */
@Entity(tableName = "trips")
@Immutable
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val departure: String,
    val destination: String,
    val date: String,
    val time: String,
    val price: Double,
    val vehicleType: String,
    val totalSeats: Int,
    val seatsPerRow: Int,
    val companyName: String = "",
    val duration: String = "",
    val seatLayout: String = "2+1",
    val features: String = "",
    val route: String = ""
)