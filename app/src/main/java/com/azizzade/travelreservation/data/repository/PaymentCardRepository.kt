package com.azizzade.travelreservation.data.repository

import com.azizzade.travelreservation.data.local.dao.PaymentCardDao
import com.azizzade.travelreservation.data.model.PaymentCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PaymentCardRepository(private val paymentCardDao: PaymentCardDao) {

    fun getUserCards(userId: Long): Flow<List<PaymentCard>> {
        return paymentCardDao.getUserCards(userId)
    }

    suspend fun insertCard(card: PaymentCard): Long {
        return withContext(Dispatchers.IO) {
            paymentCardDao.insert(card)
        }
    }

    suspend fun deleteCard(card: PaymentCard) {
        withContext(Dispatchers.IO) {
            paymentCardDao.delete(card)
        }
    }

    suspend fun getCardById(id: Long): PaymentCard? {
        return withContext(Dispatchers.IO) {
            paymentCardDao.getCardById(id)
        }
    }
}