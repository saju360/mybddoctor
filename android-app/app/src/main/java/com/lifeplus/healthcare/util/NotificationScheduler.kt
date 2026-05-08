package com.lifeplus.healthcare.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lifeplus.healthcare.data.model.MedicineReminder
import com.lifeplus.healthcare.R
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

import android.media.RingtoneManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NotificationScheduler {
    const val CHANNEL_ID = "medicine_reminders"
    private const val CHANNEL_NAME = "Medicine Reminders"
    private const val CHANNEL_DESC = "Notifications for medicine reminder alarms."

    private const val EXTRA_REMINDER_ID = "extra_reminder_id"
    private const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
    private const val EXTRA_DOSAGE = "extra_dosage"
    private const val EXTRA_IMAGE_URL = "extra_image_url"
    private const val DEFAULT_TIME = "08:00"

    fun ensureReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, reminder: MedicineReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context, reminder)
        val nextTriggerAt = parseReminderTime(reminder.nextTime).timeInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerAt, pendingIntent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerAt, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerAt, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context, reminderId)
        alarmManager.cancel(pendingIntent)
    }

    internal fun parseReminderTime(time: String): Calendar {
        val normalized = time.trim().ifBlank { DEFAULT_TIME }
        val (hour, minute) = parseHourMinute(normalized)
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!trigger.after(now)) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger
    }

    private fun parseHourMinute(time: String): Pair<Int, Int> {
        val patterns = listOf("H:mm", "HH:mm", "h:mm a", "hh:mm a")
        patterns.forEach { pattern ->
            try {
                val formatter = SimpleDateFormat(pattern, Locale.ENGLISH).apply { isLenient = false }
                val parsed = formatter.parse(time.uppercase(Locale.ENGLISH)) ?: return@forEach
                val cal = Calendar.getInstance().apply { this.time = parsed }
                return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
            } catch (_: Exception) {
            }
        }
        return 8 to 0
    }

    private fun reminderPendingIntent(context: Context, reminder: MedicineReminder): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_MEDICINE_NAME, reminder.medicineName)
            putExtra(EXTRA_DOSAGE, reminder.dosage)
            putExtra(EXTRA_IMAGE_URL, reminder.imageUrl)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun reminderPendingIntent(context: Context, reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    class ReminderAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ensureReminderChannel(context)

                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@launch
                    }

                    val id = intent.getLongExtra(EXTRA_REMINDER_ID, 0L).toInt()
                    val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME).orEmpty().ifBlank { "Medicine" }
                    val dosage = intent.getStringExtra(EXTRA_DOSAGE).orEmpty().ifBlank { "Time for your dose." }
                    val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

                    val largeIcon = if (!imageUrl.isNullOrBlank()) {
                        try {
                            val url = URL(imageUrl)
                            BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        } catch (e: Exception) { null }
                    } else null

                    val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_reminder)
                        .setContentTitle("Medicine Reminder")
                        .setContentText("$medicineName • $dosage")
                        .setStyle(NotificationCompat.BigTextStyle().bigText("Time to take $medicineName\nDosage: $dosage"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                        .setAutoCancel(true)

                    if (largeIcon != null) {
                        notificationBuilder.setLargeIcon(largeIcon)
                        notificationBuilder.setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(largeIcon)
                                .setBigContentTitle("Time for $medicineName")
                                .setSummaryText(dosage)
                        )
                    }

                    NotificationManagerCompat.from(context).notify(id, notificationBuilder.build())

                    // Reschedule for the same time tomorrow
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val nextTrigger = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    fun showGenericNotification(context: Context, id: Int, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Health Updates", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }
}

