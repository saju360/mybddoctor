package com.lifeplus.healthcare.data.repository

import com.lifeplus.healthcare.data.api.ApiService
import com.lifeplus.healthcare.data.model.*
import com.lifeplus.healthcare.data.util.Resource
import com.lifeplus.healthcare.data.util.safeApiCall
import com.lifeplus.healthcare.data.local.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

private fun String?.normalized(): String = this
    ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
    .orEmpty()

// ── Hospital ──────────────────────────────────────────────────────────────────
@Singleton
class HospitalRepository @Inject constructor(
    private val api: ApiService,
    private val dao: HospitalDao
) {
    suspend fun getAll(): Resource<List<Hospital>> {
        val remote = safeApiCall { api.getHospitals() }
        if (remote is Resource.Success) {
            dao.deleteAll()
            dao.insertAll(remote.data.map { it.toEntity() })
        }
        return remote
    }

    fun getAllCached(): Flow<List<Hospital>> = dao.getAll().map { list -> list.map { it.toModel() } }

    private fun Hospital.toEntity() = HospitalEntity(
        id = id,
        name = name.normalized(),
        address = address.normalized(),
        district = district.normalized(),
        phone = phone.normalized(),
        icuAvailable = icuAvailable,
        open24h = open24h
    )
    private fun HospitalEntity.toModel() = Hospital(id, name, address, district, "", phone, icuAvailable, open24h)

    suspend fun search(district: String): Resource<List<Hospital>> = safeApiCall { api.searchHospitals(district) }
    suspend fun getMy(): Resource<List<Hospital>> = safeApiCall { api.getMyHospitals() }
    suspend fun create(hospital: Hospital): Resource<Hospital> = safeApiCall { api.createHospital(hospital) }
    suspend fun update(id: Long, hospital: Hospital): Resource<Hospital> = safeApiCall { api.updateHospital(id, hospital) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteHospital(id) }
}

// ── Clinic ────────────────────────────────────────────────────────────────────
@Singleton
class ClinicRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<Clinic>> = safeApiCall { api.getClinics() }
    suspend fun search(district: String): Resource<List<Clinic>> = safeApiCall { api.searchClinics(district) }
    suspend fun getMy(): Resource<List<Clinic>> = safeApiCall { api.getMyClinics() }
    suspend fun create(clinic: Clinic): Resource<Clinic> = safeApiCall { api.createClinic(clinic) }
    suspend fun update(id: Long, clinic: Clinic): Resource<Clinic> = safeApiCall { api.updateClinic(id, clinic) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteClinic(id) }
}

// ── Doctor ────────────────────────────────────────────────────────────────────
@Singleton
class DoctorRepository @Inject constructor(
    private val api: ApiService,
    private val dao: DoctorDao
) {
    suspend fun getAll(): Resource<List<Doctor>> {
        val remote = safeApiCall { api.getDoctors() }
        if (remote is Resource.Success) {
            dao.deleteAll()
            dao.insertAll(remote.data.map { it.toEntity() })
        }
        return remote
    }

    fun getAllCached(): Flow<List<Doctor>> = dao.getAll().map { list -> list.map { it.toModel() } }

    private fun Doctor.toEntity() = DoctorEntity(
        id = id,
        name = name.normalized(),
        specialty = specialty.normalized(),
        phone = phone.normalized(),
        district = district.normalized(),
        telemedicineAvailable = telemedicineAvailable,
        available = available
    )
    private fun DoctorEntity.toModel() = Doctor(id, name, specialty, phone, district, "", id, telemedicineAvailable, available)

    suspend fun search(specialty: String? = null, hospitalId: Long? = null, telemedicine: Boolean? = null, district: String? = null): Resource<List<Doctor>> =
        safeApiCall { api.searchDoctors(specialty, hospitalId, telemedicine, district) }
    suspend fun getMy(): Resource<List<Doctor>> = safeApiCall { api.getMyDoctors() }
    suspend fun create(doctor: Doctor): Resource<Doctor> = safeApiCall { api.createDoctor(doctor) }
    suspend fun update(id: Long, doctor: Doctor): Resource<Doctor> = safeApiCall { api.updateDoctor(id, doctor) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteDoctor(id) }
}

// ── Ambulance ─────────────────────────────────────────────────────────────────
@Singleton
class AmbulanceRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<Ambulance>> = safeApiCall { api.getAmbulances() }
    suspend fun search(district: String? = null): Resource<List<Ambulance>> =
        safeApiCall { api.searchAmbulances(district, true) }
    suspend fun getMy(): Resource<List<Ambulance>> = safeApiCall { api.getMyAmbulances() }
    suspend fun create(ambulance: Ambulance): Resource<Ambulance> = safeApiCall { api.createAmbulance(ambulance) }
    suspend fun update(id: Long, ambulance: Ambulance): Resource<Ambulance> = safeApiCall { api.updateAmbulance(id, ambulance) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteAmbulance(id) }
}

// ── Pharmacy ──────────────────────────────────────────────────────────────────
@Singleton
class PharmacyRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<Pharmacy>> = safeApiCall { api.getPharmacies() }
    suspend fun search(district: String? = null, open24h: Boolean = false): Resource<List<Pharmacy>> =
        safeApiCall { api.searchPharmacies(district, open24h) }
    suspend fun getMy(): Resource<List<Pharmacy>> = safeApiCall { api.getMyPharmacies() }
    suspend fun create(pharmacy: Pharmacy): Resource<Pharmacy> = safeApiCall { api.createPharmacy(pharmacy) }
    suspend fun update(id: Long, pharmacy: Pharmacy): Resource<Pharmacy> = safeApiCall { api.updatePharmacy(id, pharmacy) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deletePharmacy(id) }
}

// ── Blood Bank ────────────────────────────────────────────────────────────────
@Singleton
class BloodBankRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<BloodBank>> = safeApiCall { api.getBloodBanks() }
    suspend fun search(district: String): Resource<List<BloodBank>> = safeApiCall { api.searchBloodBanks(district) }
    suspend fun getMy(): Resource<List<BloodBank>> = safeApiCall { api.getMyBloodBanks() }
    suspend fun create(bank: BloodBank): Resource<BloodBank> = safeApiCall { api.createBloodBank(bank) }
    suspend fun update(id: Long, bank: BloodBank): Resource<BloodBank> = safeApiCall { api.updateBloodBank(id, bank) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteBloodBank(id) }
}

// ── Diagnostic Center ─────────────────────────────────────────────────────────
@Singleton
class DiagnosticRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<DiagnosticCenter>> = safeApiCall { api.getDiagnostics() }
    suspend fun search(district: String? = null, test: String? = null): Resource<List<DiagnosticCenter>> =
        safeApiCall { api.searchDiagnostics(district, test) }
    suspend fun getMy(): Resource<List<DiagnosticCenter>> = safeApiCall { api.getMyDiagnostics() }
    suspend fun create(diagnostic: DiagnosticCenter): Resource<DiagnosticCenter> = safeApiCall { api.createDiagnostic(diagnostic) }
    suspend fun update(id: Long, diagnostic: DiagnosticCenter): Resource<DiagnosticCenter> = safeApiCall { api.updateDiagnostic(id, diagnostic) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteDiagnostic(id) }
}

// ── Blood Organization ────────────────────────────────────────────────────────
@Singleton
class BloodOrgRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<BloodOrganization>> = safeApiCall { api.getBloodOrgs() }
    suspend fun search(district: String): Resource<List<BloodOrganization>> = safeApiCall { api.searchBloodOrgs(district) }
    suspend fun getMy(): Resource<List<BloodOrganization>> = safeApiCall { api.getMyBloodOrgs() }
    suspend fun create(org: BloodOrganization): Resource<BloodOrganization> = safeApiCall { api.createBloodOrg(org) }
    suspend fun update(id: Long, org: BloodOrganization): Resource<BloodOrganization> = safeApiCall { api.updateBloodOrg(id, org) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteBloodOrg(id) }
}

// ── Donor ─────────────────────────────────────────────────────────────────────
@Singleton
class DonorRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<Donor>> = safeApiCall { api.getDonors() }
    suspend fun search(bloodGroup: String? = null, district: String? = null): Resource<List<Donor>> =
        safeApiCall { api.searchDonors(bloodGroup, district) }
    suspend fun myDonors(): Resource<List<Donor>> = safeApiCall { api.myDonors() }
    suspend fun create(donor: Donor): Resource<Donor> = safeApiCall { api.createDonor(donor) }
    suspend fun update(id: Long, donor: Donor): Resource<Donor> = safeApiCall { api.updateDonor(id, donor) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteDonor(id) }
    suspend fun getRewards(): Resource<Map<String, Any>> = safeApiCall { api.getDonorRewards() }
    suspend fun verifyDonation(requestId: Long): Resource<Map<String, Any>> = safeApiCall { api.verifyDonation(mapOf("requestId" to requestId)) }
    suspend fun checkEligibility(id: Long): Resource<Map<String, Any>> = safeApiCall { api.checkEligibility(id) }
}

// ── Blood Request ─────────────────────────────────────────────────────────────
@Singleton
class BloodRequestRepository @Inject constructor(private val api: ApiService) {
    suspend fun getAll(): Resource<List<BloodRequest>> = safeApiCall { api.getBloodRequests() }
    suspend fun search(bloodGroup: String? = null, district: String? = null): Resource<List<BloodRequest>> =
        safeApiCall { api.searchBloodRequests(bloodGroup, district) }
    suspend fun myRequests(): Resource<List<BloodRequest>> = safeApiCall { api.myBloodRequests() }
    suspend fun getForDonor(): Resource<List<BloodRequest>> = safeApiCall { api.getBloodRequestsForDonor() }
    suspend fun create(request: BloodRequest): Resource<BloodRequest> = safeApiCall { api.createBloodRequest(request) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteBloodRequest(id) }
    suspend fun updateStatus(id: Long, status: String): Resource<BloodRequest> = safeApiCall { api.updateBloodRequestStatus(id, status) }
    suspend fun getDashboardRequests(): Resource<List<BloodRequest>> = safeApiCall { api.getDashboardBloodRequests() }
}

// ── Emergency ─────────────────────────────────────────────────────────────────
@Singleton
class EmergencyRepository @Inject constructor(private val api: ApiService) {
    suspend fun sendEmergency(request: EmergencyRequest): Resource<EmergencyRequest> =
        safeApiCall { api.createEmergencyRequest(request) }
    suspend fun myRequests(): Resource<List<EmergencyRequest>> =
        safeApiCall { api.myEmergencyRequests() }
}

// ── Appointments ──────────────────────────────────────────────────────────────
@Singleton
class AppointmentRepository @Inject constructor(private val api: ApiService) {
    suspend fun myAppointments(): Resource<List<Appointment>> = safeApiCall { api.myAppointments() }
    suspend fun book(appointment: Appointment): Resource<Appointment> = safeApiCall { api.bookAppointment(appointment) }
    suspend fun cancel(id: Long): Resource<Unit> = safeApiCall { api.cancelAppointment(id) }
}

// ── Telemedicine ──────────────────────────────────────────────────────────────
@Singleton
class TelemedicineRepository @Inject constructor(private val api: ApiService) {
    suspend fun mySessions(): Resource<List<TelemedicineSession>> = safeApiCall { api.myTelemedicine() }
    suspend fun create(session: TelemedicineSession): Resource<TelemedicineSession> =
        safeApiCall { api.createTelemedicineSession(session) }
}

// ── Reminders ─────────────────────────────────────────────────────────────────
@Singleton
class ReminderRepository @Inject constructor(private val api: ApiService) {
    suspend fun myReminders(): Resource<List<MedicineReminder>> = safeApiCall { api.myReminders() }
    suspend fun create(reminder: MedicineReminder): Resource<MedicineReminder> = safeApiCall { api.createReminder(reminder) }
    suspend fun update(id: Long, reminder: MedicineReminder): Resource<MedicineReminder> =
        safeApiCall { api.updateReminder(id, reminder) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteReminder(id) }
}

// ── Health Records ────────────────────────────────────────────────────────────
@Singleton
class HealthRecordRepository @Inject constructor(private val api: ApiService) {
    suspend fun myRecords(): Resource<List<HealthRecord>> = safeApiCall { api.myHealthRecords() }
    suspend fun create(record: HealthRecord): Resource<HealthRecord> = safeApiCall { api.createHealthRecord(record) }
    suspend fun update(id: Long, record: HealthRecord): Resource<HealthRecord> = safeApiCall { api.updateHealthRecord(id, record) }
    suspend fun delete(id: Long): Resource<Unit> = safeApiCall { api.deleteHealthRecord(id) }
}

// ── Notifications ─────────────────────────────────────────────────────────────
@Singleton
class NotificationRepository @Inject constructor(private val api: ApiService) {
    suspend fun getNotifications(): Resource<List<Notification>> = safeApiCall { api.getNotifications() }
}

// ── Reviews ──────────────────────────────────────────────────────────────────
@Singleton
class ReviewRepository @Inject constructor(private val api: ApiService) {
    suspend fun getReviews(type: String, id: Long): Resource<List<Review>> = safeApiCall { api.getReviews(type, id) }
    suspend fun submit(review: Review): Resource<Review> = safeApiCall { api.submitReview(review) }
}

// ── Chat ─────────────────────────────────────────────────────────────────────
@Singleton
class ChatRepository @Inject constructor(private val api: ApiService) {
    suspend fun getRooms(): Resource<List<ChatRoom>> = safeApiCall { api.getRooms() }
    suspend fun getMessages(roomId: Long): Resource<List<ChatMessage>> = safeApiCall { api.getMessages(roomId) }
    suspend fun sendMessage(msg: ChatMessage): Resource<ChatMessage> = safeApiCall { api.sendMessage(msg) }
    suspend fun startChat(userId: Long): Resource<ChatRoom> = safeApiCall { api.startChat(userId) }
}
