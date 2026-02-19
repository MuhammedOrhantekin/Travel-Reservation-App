package com.mobil.travelreservation.data.local.dao

import androidx.room.*
import com.mobil.travelreservation.data.model.PaymentCard
import kotlinx.coroutines.flow.Flow

/**
 * PaymentCardDao
 *
 * Veritabanındaki 'payment_cards' tablosu ile ilgili işlemlerin yapıldığı arabirimdir.
 * Kullanıcıların kayıtlı ödeme yöntemlerini ekleme, silme ve listeleme işlemlerini yönetir.
 */
@Dao
interface PaymentCardDao {

    /**
     * Veritabanına yeni bir ödeme kartı ekler.
     *
     * @param card Eklenecek olan PaymentCard nesnesi.
     * @return Eklenen kartın veritabanındaki benzersiz ID'sini (primary key) döndürür.
     */
    @Insert
    suspend fun insert(card: PaymentCard): Long




    /**
     * Veritabanından mevcut bir kartı siler.
     *
     * @param card Silinecek olan PaymentCard nesnesi.
     */
    @Delete
    suspend fun delete(card: PaymentCard)





    /**
     * Belirli bir kullanıcıya ait tüm kayıtlı kartları getirir.
     *
     * @param userId Kartları istenen kullanıcının ID'si.
     * @return Kart listesini bir Flow (akış) olarak döndürür. Veritabanında değişiklik olduğunda liste otomatik güncellenir.
     */
    @Query("SELECT * FROM payment_cards WHERE userId = :userId")
    fun getUserCards(userId: Long): Flow<List<PaymentCard>>





    /**
     * ID'si verilen tek bir kartın detaylarını getirir.
     *
     * @param id Getirilmek istenen kartın ID'si.
     * @return Eşleşen PaymentCard nesnesi döner, bulunamazsa null döner.
     */
    @Query("SELECT * FROM payment_cards WHERE id = :id")
    suspend fun getCardById(id: Long): PaymentCard?
}