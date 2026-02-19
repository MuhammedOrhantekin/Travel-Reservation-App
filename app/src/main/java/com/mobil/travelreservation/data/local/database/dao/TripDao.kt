package com.mobil.travelreservation.data.local.database.dao

import androidx.room.*
import com.mobil.travelreservation.data.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * TripDao
 *
 * Otobüs ve Uçak seferlerinin veritabanı işlemlerini yöneten arabirimdir.
 * Sefer arama, listeleme ve Admin işlemleri (Ekle/Sil) burada yapılır.
 */
@Dao
interface TripDao {

    /**
     * Veritabanına yeni bir sefer ekler (Admin işlemi).
     *
     * @param trip Eklenecek sefer nesnesi.
     * @return Eklenen seferin ID'si.
     */
    @Insert
    suspend fun insert(trip: Trip): Long



    /**
     * Mevcut bir seferin bilgilerini günceller.
     *
     * @param trip Güncellenecek verileri içeren sefer nesnesi.
     */
    @Update
    suspend fun update(trip: Trip)



    /**
     * Bir seferi veritabanından siler (Admin işlemi).
     *
     * @param trip Silinecek sefer nesnesi.
     */
    @Delete
    suspend fun delete(trip: Trip)




    /**
     * Sistemdeki tüm seferleri tarih ve saate göre sıralı getirir.
     *
     * @return Tüm seferlerin listesi.
     */
    @Query("SELECT * FROM trips ORDER BY date, time")
    fun getAllTrips(): Flow<List<Trip>>




    /**
     * Kullanıcının girdiği kriterlere göre sefer araması yapar.
     *
     * @param from Kalkış yeri.
     * @param to Varış yeri.
     * @param date Tarih.
     * @return Kriterlere uyan seferlerin listesi (Saate göre sıralı).
     */
    @Query("SELECT * FROM trips WHERE departure = :from AND destination = :to AND date = :date ORDER BY time")
    fun searchTrips(from: String, to: String, date: String): Flow<List<Trip>>




    /**
     * Kalkış, varış, tarih ve araç tipine (Otobüs/Uçak) göre filtreli arama yapar.
     *
     * @param from Kalkış yeri.
     * @param to Varış yeri.
     * @param date Tarih.
     * @param vehicleType Araç tipi ("Bus" veya "Plane").
     * @return Filtrelenmiş sefer listesi.
     */
    @Query("SELECT * FROM trips WHERE departure = :from AND destination = :to AND date = :date AND vehicleType = :vehicleType ORDER BY time")
    fun searchTripsWithVehicleType(from: String, to: String, date: String, vehicleType: String): Flow<List<Trip>>




    /**
     * ID'si verilen seferin detaylarını getirir.
     *
     * @param id Sefer ID'si.
     * @return Trip nesnesi veya null.
     */
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): Trip?
}