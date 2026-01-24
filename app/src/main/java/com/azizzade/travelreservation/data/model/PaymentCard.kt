package com.azizzade.travelreservation.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_cards")
@Immutable
data class PaymentCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val cardName: String,           // Kart üzerindeki isim
    val cardNumber: String,         // Son 4 hane saklanacak
    val lastFourDigits: String,
    val expiryDate: String,
    val cardType: String = "Visa"   // Visa, Mastercard, Troy
)