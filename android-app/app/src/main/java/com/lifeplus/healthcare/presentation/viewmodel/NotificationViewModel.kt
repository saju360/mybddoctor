package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.model.Notification
import com.lifeplus.healthcare.data.repository.NotificationRepository
import com.lifeplus.healthcare.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.lifeplus.healthcare.data.local.SessionDataStore
import com.lifeplus.healthcare.data.repository.*
import com.lifeplus.healthcare.util.NotificationScheduler
import kotlinx.coroutines.flow.firstOrNull

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repo: NotificationRepository,
    private val appointmentRepo: AppointmentRepository,
    private val bloodRepo: BloodRequestRepository,
    private val session: SessionDataStore,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Notification>())
    val state: StateFlow<ListUiState<Notification>> = _state

    init {
        load()
        checkDailyReminders()
    }

    fun load() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        when (val r = repo.getNotifications()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error -> _state.value = ListUiState(error = r.message)
            Resource.Loading -> _state.value = ListUiState(isLoading = true)
        }
    }

    private fun checkDailyReminders() = viewModelScope.launch {
        val isLoggedIn = session.isLoggedIn.firstOrNull() ?: false
        if (!isLoggedIn) return@launch

        // Check for upcoming appointments today
        val appointments = appointmentRepo.myAppointments()
        if (appointments is Resource.Success) {
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
            appointments.data.filter { it.date == todayStr && it.status != "CANCELLED" }.forEach { appt ->
                NotificationScheduler.showGenericNotification(
                    context, 
                    appt.id.toInt() + 10000, 
                    "📅 Appointment Today", 
                    "You have an appointment with ${appt.doctorName} at ${appt.time}"
                )
            }
        }
        
        // Check for relevant blood requests
        val bloodGroup = session.bloodGroup.firstOrNull()
        val district = session.district.firstOrNull()
        if (!bloodGroup.isNullOrBlank()) {
            val requests = bloodRepo.getDashboardRequests()
            if (requests is Resource.Success) {
                val matches = requests.data.filter { it.bloodGroup == bloodGroup && (district == null || it.district == district) }
                if (matches.isNotEmpty()) {
                    NotificationScheduler.showGenericNotification(
                        context,
                        20000,
                        "🩸 Blood Needed",
                        "There are ${matches.size} urgent blood requests matching your group in your area."
                    )
                }
            }
        }
    }
}
