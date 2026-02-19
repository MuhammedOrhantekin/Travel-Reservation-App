package com.mobil.travelreservation.data.repository

import com.mobil.travelreservation.data.local.database.dao.UserDao
import com.mobil.travelreservation.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UserRepository
 *
 * Kullanıcı kimlik doğrulama, kayıt ve profil güncelleme işlemlerini yönetir.
 * ViewModel'den gelen istekleri veritabanı komutlarına çevirir.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Giriş yapma işlemi.
     * @return Kullanıcı bulunursa User nesnesi, şifre yanlışsa null döner.
     */
    suspend fun login(email: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.login(email, password)
        }
    }

    /**
     * Yeni üye kaydı.
     */
    suspend fun register(user: User): Long {
        return withContext(Dispatchers.IO) {
            userDao.insert(user)
        }
    }

    /**
     * Kayıt olurken aynı mail adresi daha önce kullanılmış mı diye kontrol eder.
     * @return Varsa true, yoksa false döner.
     */
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

    /**
     * Profil bilgilerini günceller.
     */
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