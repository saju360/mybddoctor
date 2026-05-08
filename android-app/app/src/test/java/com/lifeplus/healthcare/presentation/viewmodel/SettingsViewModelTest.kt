package com.lifeplus.healthcare.presentation.viewmodel

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultNotificationValueIsTrue() = runTest {
        val store = PreferenceDataStoreFactory.create(
            scope = TestScope(StandardTestDispatcher(testScheduler)),
            produceFile = { tempFolder.newFile("default.preferences_pb") }
        )
        val vm = SettingsViewModel(store)

        assertEquals(true, vm.notificationsEnabled.first())
    }

    @Test
    fun setNotificationsEnabledFalsePersistsFalse() = runTest {
        val store = PreferenceDataStoreFactory.create(
            scope = TestScope(StandardTestDispatcher(testScheduler)),
            produceFile = { tempFolder.newFile("false.preferences_pb") }
        )
        val vm = SettingsViewModel(store)

        vm.setNotificationsEnabled(false)
        testScheduler.advanceUntilIdle()

        val persisted = store.data.first()[SettingsViewModel.NOTIFICATIONS_KEY] ?: true
        assertEquals(false, persisted)
    }
}
