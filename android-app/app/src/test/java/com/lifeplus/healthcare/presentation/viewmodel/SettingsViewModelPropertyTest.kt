package com.lifeplus.healthcare.presentation.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// Feature: android-app-ui-improvements, Property 4: Notification preference persists across reads

/**
 * Property-based test for [SettingsViewModel] DataStore persistence.
 *
 * **Property 4: Notification preference persists across reads**
 * **Validates: Requirements 4.4, 15.5**
 *
 * For any boolean value `v` written to DataStore via
 * [SettingsViewModel.setNotificationsEnabled], a subsequent read of
 * [SettingsViewModel.notificationsEnabled] from the same DataStore instance
 * SHALL return `v`.
 *
 * Uses [PreferenceDataStoreFactory] with a temp file per iteration so each
 * iteration starts with a clean store, ensuring the written value is the only
 * one present when we read back.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelPropertyTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Install a test dispatcher as Main so viewModelScope uses it
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Property 4: Notification preference persists across reads.
     *
     * For any boolean `v`, writing via [SettingsViewModel.setNotificationsEnabled]
     * and then reading from the DataStore SHALL return `v`.
     *
     * Validates: Requirements 4.4, 15.5
     */
    @Test
    fun notificationPreferencePersistsAcrossReads() = runTest {
        // Feature: android-app-ui-improvements, Property 4: Notification preference persists across reads
        checkAll(iterations = 100, Arb.boolean()) { v ->
            // Each iteration gets its own DataStore file to avoid cross-iteration state
            val dataStoreFile = tmpFolder.newFile("prefs_${v}_${System.nanoTime()}.preferences_pb")
            val dataStoreScope = TestScope(StandardTestDispatcher())
            val dataStore = PreferenceDataStoreFactory.create(
                scope = dataStoreScope,
                produceFile = { dataStoreFile }
            )

            val viewModel = SettingsViewModel(dataStore)

            // Write the value via the ViewModel (internally uses viewModelScope.launch)
            viewModel.setNotificationsEnabled(v)

            // Advance the main test dispatcher so the launched coroutine (DataStore edit) completes
            testScheduler.advanceUntilIdle()

            // Also advance the DataStore scope's scheduler
            dataStoreScope.testScheduler.advanceUntilIdle()

            // Read back from DataStore directly to verify the value was persisted
            val persisted = dataStore.data.first()[SettingsViewModel.NOTIFICATIONS_KEY]
                ?: true  // matches ViewModel default of true when key is absent

            assertEquals(v, persisted)
        }
    }
}
