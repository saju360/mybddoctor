package com.lifeplus.healthcare.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.lifeplus.healthcare.data.local.AppDatabase
import com.lifeplus.healthcare.data.local.DoctorDao
import com.lifeplus.healthcare.data.local.HospitalDao
import com.lifeplus.healthcare.data.local.AmbulanceDao
import com.lifeplus.healthcare.data.local.SessionDataStore
import com.lifeplus.healthcare.data.util.LocationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocationHelper(@ApplicationContext context: Context): LocationHelper {
        return LocationHelper(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lifeplus_db"
        ).build()
    }

    @Provides
    fun provideDoctorDao(db: AppDatabase): DoctorDao = db.doctorDao()

    @Provides
    fun provideHospitalDao(db: AppDatabase): HospitalDao = db.hospitalDao()

    @Provides
    fun provideAmbulanceDao(db: AppDatabase): AmbulanceDao = db.ambulanceDao()

    @Provides
    @Singleton
    fun provideDataStore(sessionDataStore: SessionDataStore): DataStore<Preferences> =
        sessionDataStore.dataStore
}
