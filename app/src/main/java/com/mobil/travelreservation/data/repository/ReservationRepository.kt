package com.mobil.travelreservation.data.repository

import com.mobil.travelreservation.data.local.database.dao.ReservationDao
import com.mobil.travelreservation.data.local.database.dao.SeatInfo
import com.mobil.travelreservation.data.model.Reservation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * ReservationRepository
 *
 * Rezervasyon işlemlerini yöneten ve koltuk doluluk bilgilerini sağlayan depodur.
 * "Single Source of Truth" (Tek Doğruluk Kaynağı) prensibine hizmet eder.
 */
class ReservationRepository(private val reservationDao: ReservationDao) {

    /**
     * Bir sefere ait tüm rezervasyonları getirir.
     */
    fun getReservationsByTrip(tripId: Long): Flow<List<Reservation>> {
        return reservationDao.getReservationsByTrip(tripId)
    }

    /**
     * Sadece dolu koltuk numaralarını getirir.
     * Basit doluluk kontrolü için kullanılır.
     */
    fun getReservedSeats(tripId: Long): Flow<List<Int>> {
        return reservationDao.getReservedSeats(tripId)
    }

    /**
     * Dolu koltukları cinsiyet bilgisiyle birlikte getirir.
     * Koltuk seçimi ekranında "Kadın yanı", "Erkek yanı" renklendirmesi için gereklidir.
     */
    fun getReservedSeatsWithGender(tripId: Long): Flow<List<SeatInfo>> {
        return reservationDao.getReservedSeatsWithGender(tripId)
    }

    /**
     * Kullanıcının geçmiş ve gelecek biletlerini listeler.
     */
    fun getUserReservations(userId: Long): Flow<List<Reservation>> {
        return reservationDao.getUserReservations(userId)
    }

    /**
     * Yeni rezervasyon ekler (Bilet Satın Alma).
     * @param reservation Oluşturulacak rezervasyon nesnesi.
     * @return Oluşan kaydın ID'si.
     */
    suspend fun insertReservation(reservation: Reservation): Long {
        return withContext(Dispatchers.IO) {
            reservationDao.insert(reservation)
        }
    }

    /**
     * Bilet iptali (Veritabanından silme).
     */
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