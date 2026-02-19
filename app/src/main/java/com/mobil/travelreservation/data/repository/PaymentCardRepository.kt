package com.mobil.travelreservation.data.repository

import com.mobil.travelreservation.data.local.dao.PaymentCardDao
import com.mobil.travelreservation.data.model.PaymentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * PaymentCardRepository
 *
 * Veri Kaynağı (DAO) ile ViewModel arasındaki iletişim köprüsüdür.
 * ViewModel'in veritabanı işlemlerini doğrudan yapmasını engeller (Abstraction).
 * Tüm veritabanı yazma/okuma işlemlerinin IO Thread'de yapılmasını garanti eder.
 */
class PaymentCardRepository(private val paymentCardDao: PaymentCardDao) {

    /**
     * Kullanıcının kayıtlı kartlarını anlık akış (Flow) olarak getirir.
     * @param userId Kartları getirilecek kullanıcı ID'si.
     * @return Kart listesini içeren Flow.
     */
    fun getUserCards(userId: Long): Flow<List<PaymentCard>> {
        return paymentCardDao.getUserCards(userId)
    }

    /**
     * Yeni bir kartı veritabanına ekler.
     * İşlem 'Dispatchers.IO' üzerinde (arka planda) çalıştırılır.
     */
    suspend fun insertCard(card: PaymentCard): Long {
        return withContext(Dispatchers.IO) {
            paymentCardDao.insert(card)
        }
    }

    /**
     * Kayıtlı bir kartı siler.
     * UI Thread'i bloklamamak için IO Thread kullanılır.
     */
    suspend fun deleteCard(card: PaymentCard) {
        withContext(Dispatchers.IO) {
            paymentCardDao.delete(card)
        }
    }

    /**
     * ID'ye göre tek bir kart detayını getirir.
     */
    suspend fun getCardById(id: Long): PaymentCard? {
        return withContext(Dispatchers.IO) {
            paymentCardDao.getCardById(id)
        }
    }
}