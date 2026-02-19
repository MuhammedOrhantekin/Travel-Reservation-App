package com.mobil.travelreservation.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager
 *
 * Kullanıcının giriş durumunu ve temel bilgilerini (ID, İsim, Email)
 * cihazın SharedPreferences (Basit Veri Saklama) alanında tutar.
 * Veritabanı yerine burayı kullanmamızın sebebi, verilere çok hızlı erişebilmektir.
 */
class SessionManager(context: Context) {
    // "user_session" adında özel bir dosya oluşturur.
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_ADMIN = "is_admin"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * Başarılı girişten sonra kullanıcı bilgilerini kaydeder.
     */
    fun saveLoginSession(userId: Long, email: String, name: String, isAdmin: Boolean) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putBoolean(KEY_IS_ADMIN, isAdmin)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply() // Değişiklikleri asenkron olarak kaydeder.
        }
    }

    // Kullanıcı adı güncelleme
    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    // Kullanıcı email güncelleme
    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    // Kullanıcı giriş yapmış mı?
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, 0)
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun isAdmin(): Boolean = prefs.getBoolean(KEY_IS_ADMIN, false)

    /**
     * Çıkış yapıldığında oturum verilerini temizler.
     */
    fun logout() {
        prefs.edit().clear().apply()
    }
}