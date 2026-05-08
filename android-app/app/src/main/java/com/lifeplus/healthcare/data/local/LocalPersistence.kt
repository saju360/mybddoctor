package com.lifeplus.healthcare.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Entities ─────────────────────────────────────────────────────────────────

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val specialty: String,
    val phone: String,
    val district: String,
    val telemedicineAvailable: Boolean,
    val available: Boolean
)

@Entity(tableName = "hospitals")
data class HospitalEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val address: String,
    val district: String,
    val phone: String,
    val icuAvailable: Boolean,
    val open24h: Boolean
)

@Entity(tableName = "ambulances")
data class AmbulanceEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val phone: String,
    val district: String,
    val icuEquipped: Boolean,
    val available: Boolean
)

// ── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors")
    fun getAll(): Flow<List<DoctorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doctors: List<DoctorEntity>)

    @Query("DELETE FROM doctors")
    suspend fun deleteAll()
}

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospitals")
    fun getAll(): Flow<List<HospitalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hospitals: List<HospitalEntity>)

    @Query("DELETE FROM hospitals")
    suspend fun deleteAll()
}

@Dao
interface AmbulanceDao {
    @Query("SELECT * FROM ambulances")
    fun getAll(): Flow<List<AmbulanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ambulances: List<AmbulanceEntity>)

    @Query("DELETE FROM ambulances")
    suspend fun deleteAll()
}
