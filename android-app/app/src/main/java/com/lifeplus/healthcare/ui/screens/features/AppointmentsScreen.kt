package com.lifeplus.healthcare.ui.screens.features

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.data.model.Appointment
import com.lifeplus.healthcare.presentation.viewmodel.AppointmentViewModel
import com.lifeplus.healthcare.ui.components.*
import com.lifeplus.healthcare.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    initialDoctorId: Long = 0L,
    initialDoctorName: String? = null,
    initialSpecialty: String? = null,
    viewModel: AppointmentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showBookForm by remember { mutableStateOf(initialDoctorName != null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var pendingAppointment by remember { mutableStateOf<Appointment?>(null) }

    LaunchedEffect(initialDoctorName) {
        if (initialDoctorName != null) {
            showBookForm = true
        }
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = if (showBookForm) "Book Appointment" else "My Appointments",
                subtitle = if (showBookForm) "Secure your consultation slot" else "Manage your medical visits",
                onBackClick = {
                    if (showBookForm) showBookForm = false else onNavigateBack()
                }
            )

            if (showBookForm) {
                BookingFormContent(
                    viewModel = viewModel,
                    initialDoctorId = initialDoctorId,
                    initialDoctorName = initialDoctorName,
                    initialSpecialty = initialSpecialty,
                    onCancel = { showBookForm = false },
                    onConfirmPayment = { appt ->
                        pendingAppointment = appt
                        showPaymentDialog = true
                    }
                )
            } else {
                AppointmentListContent(
                    appointments = state.data,
                    isLoading = state.isLoading,
                    onAddClick = { onNavigate("browse_doctors") },
                    onNavigate = onNavigate,
                    onCancelAppointment = { viewModel.cancel(it) }
                )
            }
        }
    }

    if (showPaymentDialog && pendingAppointment != null) {
        PaymentDialog(
            amount = "Tk 500",
            onDismiss = { showPaymentDialog = false },
            onPaymentSuccess = {
                pendingAppointment?.let { viewModel.book(it) }
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun AppointmentListContent(
    appointments: List<Appointment>,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onCancelAppointment: (Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (appointments.isEmpty()) {
            com.lifeplus.healthcare.ui.screens.browse.EmptyState(query = "")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        text = "Upcoming Consultations",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                items(appointments) { appointment ->
                    PremiumAppointmentCard(
                        appointment = appointment,
                        onCancel = {
                            onCancelAppointment(appointment.id)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        Surface(
            onClick = onAddClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(60.dp).shadow(12.dp, CircleShape),
            shape = CircleShape,
            color = Primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun PremiumAppointmentCard(appointment: Appointment, onCancel: () -> Unit) {
    var showCancelConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(18.dp), color = Primary.copy(alpha = 0.08f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(appointment.doctorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(appointment.specialty, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                
                StatusBadge(status = appointment.status)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoIconText(Icons.Default.CalendarMonth, appointment.date)
                InfoIconText(Icons.Default.Schedule, appointment.time)
            }
            if (appointment.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { showCancelConfirm = true }, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel Appointment", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Cancel Appointment") },
            text = { Text("Are you sure you want to cancel this appointment?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancel()
                        showCancelConfirm = false
                    }
                ) {
                    Text("Cancel Appointment", color = ErrorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("Keep")
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun BookingFormContent(
    viewModel: AppointmentViewModel,
    initialDoctorId: Long = 0L,
    initialDoctorName: String? = null,
    initialSpecialty: String? = null,
    onCancel: () -> Unit,
    onConfirmPayment: (Appointment) -> Unit
) {
    val context = LocalContext.current
    val actionState by viewModel.action.collectAsState()
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) } }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(dateFormat.format(calendar.time)) }
    var selectedTime by remember { mutableStateOf("10:00 AM") }

    val datePickerDialog = remember {
        DatePickerDialog(context, { _, y, m, d ->
            val p = Calendar.getInstance().apply { set(y, m, d) }
            selectedDate = dateFormat.format(p.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    LaunchedEffect(actionState.isSuccess) { if (actionState.isSuccess) onCancel() }

    LaunchedEffect(actionState.error) {
        if (!actionState.error.isNullOrBlank()) {
            android.widget.Toast.makeText(context, actionState.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Primary) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                    Icon(Icons.Default.Medication, null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(initialDoctorName ?: "Select Doctor", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(initialSpecialty ?: "Healthcare Provider", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text("Choose Date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Surface(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = Primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(selectedDate, fontWeight = FontWeight.Bold)
            }
        }

        Text("Time Slot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        val slots = listOf("09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            slots.forEach { slot ->
                PremiumTimeSlot(slot, selectedTime == slot, { selectedTime = slot })
            }
        }

        PrimaryButton(
            text = if (actionState.isLoading) "Processing..." else "Confirm & Pay (Tk 500)",
            onClick = {
                onConfirmPayment(
                    Appointment(
                        doctorId = initialDoctorId,
                        doctorName = initialDoctorName ?: "Selected Doctor",
                        specialty = initialSpecialty ?: "General",
                        date = selectedDate,
                        time = selectedTime
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            isLoading = actionState.isLoading
        )
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = TextSecondary) }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = if (status == "PENDING") WarningColor else SuccessColor
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)) {
        Text(status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoIconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PaymentDialog(amount: String, onDismiss: () -> Unit, onPaymentSuccess: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = SuccessColor.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AccountBalanceWallet, null, tint = SuccessColor, modifier = Modifier.size(32.dp)) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Secure Payment", fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Amount Payable", style = MaterialTheme.typography.labelMedium, color = TextHint)
                Text(amount, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Primary)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PaymentIcon("bKash", Color(0xFFD81B60))
                    PaymentIcon("Nagad", Color(0xFFFF5722))
                    PaymentIcon("Card", Primary)
                }
            }
        },
        confirmButton = { PrimaryButton(text = "Pay Now", onClick = onPaymentSuccess, modifier = Modifier.fillMaxWidth().height(52.dp)) },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = TextHint) } }
    )
}

@Composable
fun PaymentIcon(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.1f)) {
            Box(contentAlignment = Alignment.Center) { Text(name.take(1), color = color, fontWeight = FontWeight.Black) }
        }
        Text(name, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun PremiumTimeSlot(time: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Primary else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Surface2Light),
        modifier = Modifier.width(80.dp)
    ) {
        Text(time, modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, color = if (isSelected) Color.White else TextPrimary, fontSize = 10.sp)
    }
}

