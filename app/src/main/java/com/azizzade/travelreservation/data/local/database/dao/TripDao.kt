package com.azizzade.travelreservation.data.local.database.dao

import androidx.room.*
import com.azizzade.travelreservation.data.model.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT * FROM trips ORDER BY date, time")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE departure = :from AND destination = :to AND date = :date ORDER BY time")
    fun searchTrips(from: String, to: String, date: String): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE departure = :from AND destination = :to AND date = :date AND vehicleType = :vehicleType ORDER BY time")
    fun searchTripsWithVehicleType(from: String, to: String, date: String, vehicleType: String): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): Trip?
}