package com.mobil.travelreservation.data.repository

import com.mobil.travelreservation.data.local.database.dao.TripDao
import com.mobil.travelreservation.data.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * TripRepository
 *
 * Otobüs ve Uçak seferleri ile ilgili tüm veri akışını yönetir.
 * Hem Admin (Ekle/Sil) hem de Kullanıcı (Ara/Listele) işlemleri buradan geçer.
 */
class TripRepository(private val tripDao: TripDao) {

    /**
     * Tüm seferleri getirir.
     */
    fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips()
    }

    /**
     * Kullanıcının kriterlerine göre sefer arar.
     * @param from Kalkış yeri
     * @param to Varış yeri
     * @param date Tarih
     */
    fun searchTrips(from: String, to: String, date: String): Flow<List<Trip>> {
        return tripDao.searchTrips(from, to, date)
    }

    /**
     * Araç tipine (Otobüs/Uçak) göre filtreli arama yapar.
     */
    fun searchTripsWithVehicleType(from: String, to: String, date: String, vehicleType: String): Flow<List<Trip>> {
        return tripDao.searchTripsWithVehicleType(from, to, date, vehicleType)
    }

    /**
     * Tek bir seferin detaylarını çeker.
     * IO Thread'de çalışır.
     */
    suspend fun getTripById(id: Long): Trip? {
        return withContext(Dispatchers.IO) {
            tripDao.getTripById(id)
        }
    }

    // --- ADMIN İŞLEMLERİ ---

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