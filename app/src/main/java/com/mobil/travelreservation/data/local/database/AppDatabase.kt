package com.mobil.travelreservation.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobil.travelreservation.data.local.dao.*
import com.mobil.travelreservation.data.local.database.dao.ReservationDao
import com.mobil.travelreservation.data.local.database.dao.TripDao
import com.mobil.travelreservation.data.local.database.dao.UserDao
import com.mobil.travelreservation.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AppDatabase
 *
 * Room veritabanının ana abstract sınıfıdır.
 * entities: Veritabanında yer alacak tabloları (User, Trip vb.) belirtir.
 * version: Veritabanı versiyonudur. Tablo yapısı değişirse artırılmalıdır.
 */
@Database(
    entities = [User::class, Trip::class, Reservation::class, PaymentCard::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO'lara erişim sağlayan abstract fonksiyonlar
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun reservationDao(): ReservationDao
    abstract fun paymentCardDao(): PaymentCardDao

    companion object {
        // @Volatile: Değişkenin değerinin tüm thread'ler tarafından anında görülmesini sağlar.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton Pattern: Uygulama genelinde tek bir veritabanı örneği oluşturur.
         * Eğer daha önce oluşturulmuşsa onu döndürür, yoksa yenisini yaratır.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_reservation_db"
                )
                    .addCallback(DatabaseCallback()) // İlk açılışta veri yüklemek için
                    .fallbackToDestructiveMigration() // Versiyon değişirse eski veriyi silip yeniden kurar
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Veritabanı İLK KEZ oluşturulduğunda çalışacak Callback sınıfı.
     * İçinde hazır admin, kullanıcı ve sefer verileri eklenir.
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                // Veritabanı işlemleri uzun sürdüğü için IO Thread'de çalıştırılır.
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(database: AppDatabase) {
            // Hazır kullanıcılar
            database.userDao().insert(User(
                email = "admin@test.com",
                password = "123456",
                fullName = "Admin Kullanıcı",
                isAdmin = true,
                tcNumber = "12345678901",
                phone = "05551234567"
            ))
            database.userDao().insert(User(
                email = "user@test.com",
                password = "123456",
                fullName = "Test Kullanıcı",
                isAdmin = false,
                tcNumber = "98765432109",
                phone = "05559876543"
            ))

            // OTOBÜS SEFERLERİ - 2+1 düzen, 40 koltuk
            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "Ankara",
                date = "2025-12-29",
                time = "08:00",
                price = 650.0,
                vehicleType = "Otobüs",
                totalSeats = 40,
                seatsPerRow = 3,
                companyName = "Metro Turizm",
                duration = "5s 30dk",
                seatLayout = "2+1",
                features = "WiFi,220V Priz,TV,Rahat Koltuk,İkram",
                route = "08:00 İstanbul Esenler,09:30 Bolu,11:00 Ankara AŞTİ"
            ))

            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "Ankara",
                date = "2025-12-29",
                time = "10:30",
                price = 600.0,
                vehicleType = "Otobüs",
                totalSeats = 40,
                seatsPerRow = 3,
                companyName = "Kamil Koç",
                duration = "5s 45dk",
                seatLayout = "2+1",
                features = "WiFi,USB Şarj,Klima,Çay-Kahve",
                route = "10:30 İstanbul Esenler,12:00 Bolu,14:15 Ankara AŞTİ"
            ))

            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "Ankara",
                date = "2025-12-29",
                time = "14:00",
                price = 550.0,
                vehicleType = "Otobüs",
                totalSeats = 40,
                seatsPerRow = 3,
                companyName = "Pamukkale",
                duration = "6s",
                seatLayout = "2+1",
                features = "WiFi,Priz,Battaniye,İkram",
                route = "14:00 İstanbul Alibeyköy,15:30 Bolu,17:00 Kızılcahamam,18:00 Ankara AŞTİ"
            ))

            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "İzmir",
                date = "2025-12-29",
                time = "09:00",
                price = 500.0,
                vehicleType = "Otobüs",
                totalSeats = 40,
                seatsPerRow = 3,
                companyName = "Varan Turizm",
                duration = "6s 30dk",
                seatLayout = "2+1",
                features = "WiFi,TV,Priz,Lüks Koltuk",
                route = "09:00 İstanbul Esenler,11:30 Bursa,14:00 Balıkesir,15:30 İzmir"
            ))

            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "İzmir",
                date = "2025-12-29",
                time = "22:00",
                price = 450.0,
                vehicleType = "Otobüs",
                totalSeats = 40,
                seatsPerRow = 3,
                companyName = "Ulusoy",
                duration = "7s",
                seatLayout = "2+1",
                features = "WiFi,Priz,Battaniye,Gece Seferi",
                route = "22:00 İstanbul Esenler,00:30 Bursa,03:00 Balıkesir,05:00 İzmir"
            ))

            database.tripDao().insert(Trip(
                departure = "Ankara",
                destination = "Antalya",
                date = "2025-12-30",
                time = "08:00",
                price = 400.0,
                vehicleType = "Otobüs",
                totalSeats = 40,
                seatsPerRow = 3,
                companyName = "Süha Turizm",
                duration = "7s 30dk",
                seatLayout = "2+1",
                features = "WiFi,USB,Klima,İkram",
                route = "08:00 Ankara AŞTİ,10:00 Konya,13:00 Isparta,15:30 Antalya"
            ))

            database.tripDao().insert(Trip(
                departure = "Antalya",
                destination = "İzmir",
                date = "2025-12-30",
                time = "07:00",
                price = 350.0,
                vehicleType = "Otobüs",
                totalSeats = 40,
                seatsPerRow = 3,
                companyName = "Özkaymak",
                duration = "6s",
                seatLayout = "2+1",
                features = "WiFi,Priz,TV Yayını",
                route = "07:00 Antalya,09:00 Burdur,11:00 Denizli,13:00 İzmir"
            ))

            // UÇAK SEFERLERİ - 3+3 düzen, 180 koltuk
            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "Antalya",
                date = "2025-12-29",
                time = "07:30",
                price = 1200.0,
                vehicleType = "Uçak",
                totalSeats = 180,
                seatsPerRow = 6,
                companyName = "Türk Hava Yolları",
                duration = "1s 15dk",
                seatLayout = "3+3",
                features = "Yemek,Eğlence Sistemi,USB Şarj,Geniş Koltuk",
                route = "07:30 İstanbul Havalimanı,08:45 Antalya Havalimanı"
            ))

            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "Antalya",
                date = "2025-12-29",
                time = "12:00",
                price = 900.0,
                vehicleType = "Uçak",
                totalSeats = 180,
                seatsPerRow = 6,
                companyName = "Pegasus",
                duration = "1s 20dk",
                seatLayout = "3+3",
                features = "Ekonomi Sınıf,Bagaj 15kg",
                route = "12:00 Sabiha Gökçen,13:20 Antalya Havalimanı"
            ))

            database.tripDao().insert(Trip(
                departure = "İstanbul",
                destination = "Ankara",
                date = "2025-12-29",
                time = "08:00",
                price = 850.0,
                vehicleType = "Uçak",
                totalSeats = 180,
                seatsPerRow = 6,
                companyName = "AnadoluJet",
                duration = "1s 05dk",
                seatLayout = "3+3",
                features = "Atıştırmalık,Bagaj 20kg",
                route = "08:00 İstanbul Havalimanı,09:05 Ankara Esenboğa"
            ))

            database.tripDao().insert(Trip(
                departure = "Ankara",
                destination = "İzmir",
                date = "2025-12-30",
                time = "10:00",
                price = 950.0,
                vehicleType = "Uçak",
                totalSeats = 180,
                seatsPerRow = 6,
                companyName = "Türk Hava Yolları",
                duration = "1s 10dk",
                seatLayout = "3+3",
                features = "Yemek,Eğlence,WiFi",
                route = "10:00 Ankara Esenboğa,11:10 İzmir Adnan Menderes"
            ))

            database.tripDao().insert(Trip(
                departure = "İzmir",
                destination = "İstanbul",
                date = "2025-12-30",
                time = "18:30",
                price = 800.0,
                vehicleType = "Uçak",
                totalSeats = 180,
                seatsPerRow = 6,
                companyName = "SunExpress",
                duration = "1s 15dk",
                seatLayout = "3+3",
                features = "Ekonomi,Atıştırmalık",
                route = "18:30 İzmir Adnan Menderes,19:45 İstanbul Havalimanı"
            ))

            // Hazır rezervasyonlar
            database.reservationDao().insert(Reservation(
                userId = 2, tripId = 1, seatNumber = 1,
                passengerName = "Ahmet Yılmaz", passengerGender = "Erkek",
                passengerTc = "12345678901", passengerEmail = "ahmet@test.com",
                passengerPhone = "05551111111", reservationDate = "2025-12-13"
            ))
            database.reservationDao().insert(Reservation(
                userId = 2, tripId = 1, seatNumber = 4,
                passengerName = "Ayşe Demir", passengerGender = "Kadın",
                passengerTc = "98765432109", passengerEmail = "ayse@test.com",
                passengerPhone = "05552222222", reservationDate = "2025-12-13"
            ))
            database.reservationDao().insert(Reservation(
                userId = 2, tripId = 1, seatNumber = 7,
                passengerName = "Mehmet Kaya", passengerGender = "Erkek",
                passengerTc = "11122233344", passengerEmail = "mehmet@test.com",
                passengerPhone = "05553333333", reservationDate = "2025-12-13"
            ))
        }
    }
}