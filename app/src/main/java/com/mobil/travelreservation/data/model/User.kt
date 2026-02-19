package com.mobil.travelreservation.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * User
 * Uygulama kullanıcılarını (ve Adminleri) tutar.
 */
@Entity(tableName = "users")
@Immutable
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val fullName: String,
    val isAdmin: Boolean = false,
    val tcNumber: String = "",
    val phone: String = ""
)