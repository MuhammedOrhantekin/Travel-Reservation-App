package com.azizzade.travelreservation.data.local.dao

import androidx.room.*
import com.azizzade.travelreservation.data.model.PaymentCard
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentCardDao {
    @Insert
    suspend fun insert(card: PaymentCard): Long

    @Delete
    suspend fun delete(card: PaymentCard)

    @Query("SELECT * FROM payment_cards WHERE userId = :userId")
    fun getUserCards(userId: Long): Flow<List<PaymentCard>>

    @Query("SELECT * FROM payment_cards WHERE id = :id")
    suspend fun getCardById(id: Long): PaymentCard?
}