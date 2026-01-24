package com.azizzade.travelreservation.data.repository

import com.azizzade.travelreservation.data.local.database.dao.UserDao
import com.azizzade.travelreservation.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.login(email, password)
        }
    }

    suspend fun register(user: User): Long {
        return withContext(Dispatchers.IO) {
            userDao.insert(user)
        }
    }

    suspend fun isEmailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            userDao.getUserByEmail(email) != null
        }
    }

    suspend fun getUserById(id: Long): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(id)
        }
    }

    suspend fun updateProfile(userId: Long, name: String, tc: String, phone: String) {
        withContext(Dispatchers.IO) {
            userDao.updateProfile(userId, name, tc, phone)
        }
    }

    suspend fun updatePassword(userId: Long, newPassword: String) {
        withContext(Dispatchers.IO) {
            userDao.updatePassword(userId, newPassword)
        }
    }

    suspend fun updateEmail(userId: Long, newEmail: String) {
        withContext(Dispatchers.IO) {
            userDao.updateEmail(userId, newEmail)
        }
    }
}