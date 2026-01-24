package com.azizzade.travelreservation.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    // Yeni alanlar
    val features: String = "",      // Virgülle ayrılmış özellikler
    val route: String = ""          // Virgülle ayrılmış güzergah durakları
)