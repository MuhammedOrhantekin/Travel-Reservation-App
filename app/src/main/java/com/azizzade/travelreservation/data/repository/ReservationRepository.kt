package com.azizzade.travelreservation.data.repository

import com.azizzade.travelreservation.data.local.database.dao.ReservationDao
import com.azizzade.travelreservation.data.local.database.dao.SeatInfo
import com.azizzade.travelreservation.data.model.Reservation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ReservationRepository(private val reservationDao: ReservationDao) {

    fun getReservationsByTrip(tripId: Long): Flow<List<Reservation>> {
        return reservationDao.getReservationsByTrip(tripId)
    }

    fun getReservedSeats(tripId: Long): Flow<List<Int>> {
        return reservationDao.getReservedSeats(tripId)
    }

    fun getReservedSeatsWithGender(tripId: Long): Flow<List<SeatInfo>> {
        return reservationDao.getReservedSeatsWithGender(tripId)
    }

    fun getUserReservations(userId: Long): Flow<List<Reservation>> {
        return reservationDao.getUserReservations(userId)
    }

    suspend fun insertReservation(reservation: Reservation): Long {
        return withContext(Dispatchers.IO) {
            reservationDao.insert(reservation)
        }
    }

    suspend fun deleteReservation(reservation: Reservation) {
        withContext(Dispatchers.IO) {
            reservationDao.delete(reservation)
        }
    }

    suspend fun getReservationById(id: Long): Reservation? {
        return withContext(Dispatchers.IO) {
            reservationDao.getReservationById(id)
        }
    }
}