package com.azizzade.travelreservation.data.local.database.dao

import androidx.room.*
import com.azizzade.travelreservation.data.model.Reservation
import kotlinx.coroutines.flow.Flow

data class SeatInfo(
    val seatNumber: Int,
    val passengerGender: String
)

@Dao
interface ReservationDao {
    @Insert
    suspend fun insert(reservation: Reservation): Long

    @Delete
    suspend fun delete(reservation: Reservation)

    @Query("SELECT * FROM reservations WHERE tripId = :tripId")
    fun getReservationsByTrip(tripId: Long): Flow<List<Reservation>>

    @Query("SELECT seatNumber FROM reservations WHERE tripId = :tripId")
    fun getReservedSeats(tripId: Long): Flow<List<Int>>

    @Query("SELECT seatNumber, passengerGender FROM reservations WHERE tripId = :tripId")
    fun getReservedSeatsWithGender(tripId: Long): Flow<List<SeatInfo>>

    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY reservationDate DESC")
    fun getUserReservations(userId: Long): Flow<List<Reservation>>

    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getReservationById(id: Long): Reservation?
}