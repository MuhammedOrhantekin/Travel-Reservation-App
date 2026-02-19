package com.mobil.travelreservation.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PaymentCard
 * @Entity: Bu sınıfın bir veritabanı tablosu olduğunu belirtir.
 * @Immutable: Jetpack Compose'un bu veri değişmediği sürece ekranı
 * tekrar çizmemesini (Recomposition) sağlar. Performans içindir.
 */
@Entity(tableName = "payment_cards")
@Immutable
data class PaymentCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val cardName: String,
    val cardNumber: String,
    val lastFourDigits: String,
    val expiryDate: String,
    val cardType: String = "Visa"
)