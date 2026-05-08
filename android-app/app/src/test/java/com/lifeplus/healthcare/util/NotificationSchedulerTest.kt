package com.lifeplus.healthcare.util

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lifeplus.healthcare.data.model.MedicineReminder
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationSchedulerTest {

    @Test
    fun parseReminderTimeParsesHourAndMinute() {
        val morning = NotificationScheduler.parseReminderTime("08:30")
        assertEquals(8, morning.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(30, morning.get(java.util.Calendar.MINUTE))

        val night = NotificationScheduler.parseReminderTime("23:59")
        assertEquals(23, night.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(59, night.get(java.util.Calendar.MINUTE))
    }

    @Test
    fun reminderAlarmStateConsistentWithActiveState() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)

        checkAll(iterations = 60, Arb.long(1L..10_000L), Arb.int(0..23), Arb.int(0..59)) { id, hour, minute ->
            val before = shadowAlarmManager.scheduledAlarms.size
            val reminder = MedicineReminder(
                id = id,
                medicineName = "Dose$id",
                dosage = "1 tablet",
                nextTime = String.format("%02d:%02d", hour, minute),
                active = true
            )

            NotificationScheduler.scheduleReminder(context, reminder)
            assertTrue(shadowAlarmManager.scheduledAlarms.size > before)

            NotificationScheduler.cancelReminder(context, reminder.id)
            assertEquals(before, shadowAlarmManager.scheduledAlarms.size)
        }
    }
}
