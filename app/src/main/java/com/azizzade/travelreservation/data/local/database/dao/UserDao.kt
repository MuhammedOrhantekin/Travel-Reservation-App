package com.azizzade.travelreservation.data.local.database.dao

import androidx.room.*
import com.azizzade.travelreservation.data.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)

    @Query("UPDATE users SET email = :newEmail WHERE id = :userId")
    suspend fun updateEmail(userId: Long, newEmail: String)

    @Query("UPDATE users SET fullName = :name, tcNumber = :tc, phone = :phone WHERE id = :userId")
    suspend fun updateProfile(userId: Long, name: String, tc: String, phone: String)
}