package com.lifeplus.healthcare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DoctorEntity::class, HospitalEntity::class, AmbulanceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doctorDao(): DoctorDao
    abstract fun hospitalDao(): HospitalDao
    abstract fun ambulanceDao(): AmbulanceDao
}
