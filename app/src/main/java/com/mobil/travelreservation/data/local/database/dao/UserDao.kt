package com.mobil.travelreservation.data.local.database.dao

import androidx.room.*
import com.mobil.travelreservation.data.model.User

/**
 * UserDao
 *
 * Kullanıcı kimlik doğrulama (Login/Register) ve profil yönetimi işlemlerini
 * gerçekleştiren arabirimdir.
 */
@Dao
interface UserDao {

    /**
     * Yeni bir kullanıcı kaydeder.
     *
     * @param user Kayıt olacak kullanıcı bilgileri.
     * @return Yeni kullanıcının ID'si.
     */
    @Insert
    suspend fun insert(user: User): Long



    /**
     * Kullanıcı bilgilerini günceller (Tüm alanları).
     *
     * @param user Güncellenmiş kullanıcı nesnesi.
     */
    @Update
    suspend fun update(user: User)



    /**
     * Kullanıcı girişi (Login) işlemi için e-posta ve şifre kontrolü yapar.
     *
     * @param email Kullanıcının e-postası.
     * @param password Kullanıcının şifresi.
     * @return Eşleşen kullanıcı varsa User nesnesi, yoksa null döner.
     */
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?



    /**
     * E-posta adresine göre kullanıcıyı bulur (Duplicate kontrolü için kullanılır).
     *
     * @param email Aranan e-posta.
     * @return User nesnesi veya null.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?



    /**
     * ID'ye göre kullanıcı detaylarını getirir.
     */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?



    /**
     * Sadece kullanıcının şifresini günceller.
     *
     * @param userId Kullanıcı ID'si.
     * @param newPassword Yeni şifre.
     */
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)



    /**
     * Sadece kullanıcının e-postasını günceller.
     *
     * @param userId Kullanıcı ID'si.
     * @param newEmail Yeni e-posta adresi.
     */
    @Query("UPDATE users SET email = :newEmail WHERE id = :userId")
    suspend fun updateEmail(userId: Long, newEmail: String)



    /**
     * Profil bilgilerini (İsim, TC, Telefon) toplu günceller.
     *
     * @param userId Güncellenecek kullanıcı ID'si.
     * @param name Yeni isim.
     * @param tc Yeni TC kimlik no.
     * @param phone Yeni telefon numarası.
     */
    @Query("UPDATE users SET fullName = :name, tcNumber = :tc, phone = :phone WHERE id = :userId")
    suspend fun updateProfile(userId: Long, name: String, tc: String, phone: String)
}