package com.mobil.travelreservation.data.local.database.dao

import androidx.room.*
import com.mobil.travelreservation.data.model.Reservation
import kotlinx.coroutines.flow.Flow

/**
 * SeatInfo
 *
 * Koltuk seçimi ekranında sadece koltuk numarası ve cinsiyet bilgisini
 * tutmak için kullanılan yardımcı veri sınıfıdır (DTO).
 */
data class SeatInfo(
    val seatNumber: Int,
    val passengerGender: String
)

/**
 * ReservationDao
 *
 * Rezervasyon işlemlerini ve koltuk doluluk durumlarını yöneten arabirimdir.
 */
@Dao
interface ReservationDao {

    /**
     * Yeni bir rezervasyon oluşturur.
     *
     * @param reservation Eklenecek rezervasyon bilgileri.
     * @return Oluşturulan rezervasyonun ID'si.
     */
    @Insert
    suspend fun insert(reservation: Reservation): Long




    /**
     * Var olan bir rezervasyonu iptal eder (siler).
     *
     * @param reservation Silinecek rezervasyon nesnesi.
     */
    @Delete
    suspend fun delete(reservation: Reservation)



    /**
     * Belirli bir sefere ait tüm rezervasyonları getirir.
     *
     * @param tripId Sefer ID'si.
     * @return Rezervasyon listesi akışı.
     */
    @Query("SELECT * FROM reservations WHERE tripId = :tripId")
    fun getReservationsByTrip(tripId: Long): Flow<List<Reservation>>



    /**
     * Bir seferdeki sadece dolu koltuk numaralarını getirir.
     *
     * @param tripId Sefer ID'si.
     * @return Dolu koltuk numaralarının listesi (Örn: [1, 5, 12]).
     */
    @Query("SELECT seatNumber FROM reservations WHERE tripId = :tripId")
    fun getReservedSeats(tripId: Long): Flow<List<Int>>




    /**
     * Koltuk seçimi ekranında cinsiyete göre renklendirme yapmak için;
     * dolu koltuk numaralarını ve yolcu cinsiyetlerini getirir.
     *
     * @param tripId Sefer ID'si.
     * @return SeatInfo (No ve Cinsiyet) nesnelerinden oluşan liste.
     */
    @Query("SELECT seatNumber, passengerGender FROM reservations WHERE tripId = :tripId")
    fun getReservedSeatsWithGender(tripId: Long): Flow<List<SeatInfo>>





    /**
     * Kullanıcının geçmiş ve gelecek tüm rezervasyonlarını tarihe göre yeniden eskiye sıralar.
     *
     * @param userId Kullanıcı ID'si.
     * @return Kullanıcının rezervasyon listesi.
     */
    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY reservationDate DESC")
    fun getUserReservations(userId: Long): Flow<List<Reservation>>




    /**
     * Rezervasyon detayını ID'ye göre getirir.
     *
     * @param id Rezervasyon ID'si.
     * @return Rezervasyon nesnesi veya null.
     */
    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getReservationById(id: Long): Reservation?
}