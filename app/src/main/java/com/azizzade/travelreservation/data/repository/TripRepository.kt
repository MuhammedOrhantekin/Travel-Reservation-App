package com.azizzade.travelreservation.data.repository

import com.azizzade.travelreservation.data.local.database.dao.TripDao
import com.azizzade.travelreservation.data.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TripRepository(private val tripDao: TripDao) {

    fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips()
    }

    fun searchTrips(from: String, to: String, date: String): Flow<List<Trip>> {
        return tripDao.searchTrips(from, to, date)
    }

    fun searchTripsWithVehicleType(from: String, to: String, date: String, vehicleType: String): Flow<List<Trip>> {
        return tripDao.searchTripsWithVehicleType(from, to, date, vehicleType)
    }

    suspend fun getTripById(id: Long): Trip? {
        return withContext(Dispatchers.IO) {
            tripDao.getTripById(id)
        }
    }

    suspend fun insertTrip(trip: Trip): Long {
        return withContext(Dispatchers.IO) {
            tripDao.insert(trip)
        }
    }

    suspend fun deleteTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao.delete(trip)
        }
    }

    suspend fun updateTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao.update(trip)
        }
    }
}