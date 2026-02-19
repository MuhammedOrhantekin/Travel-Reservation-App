package com.mobil.travelreservation.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reservation
 * Kullanıcının bilet bilgilerini tutar.
 */
@Entity(tableName = "reservations")
@Immutable
data class Reservation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val tripId: Long,
    val seatNumber: Int,
    val passengerName: String,
    val passengerGender: String = "Erkek",
    val passengerTc: String = "",
    val passengerEmail: String = "",
    val passengerPhone: String = "",
    val reservationDate: String,
    val paymentMethod: String = "Kredi Kartı"
)