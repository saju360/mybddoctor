package com.lifeplus.healthcare.data.api

import com.lifeplus.healthcare.data.model.*
import com.lifeplus.healthcare.model.DashboardSlide
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("auth/forgot-password/request")
    suspend fun forgotPasswordRequest(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("auth/forgot-password/reset")
    suspend fun forgotPasswordReset(@Body body: Map<String, String>): Response<Map<String, String>>

    // ── User Profile ──────────────────────────────────────────────────────────
    @GET("users/me")
    suspend fun getMyProfile(): Response<UserProfile>

    @PUT("users/{id}")
    suspend fun updateProfile(@Path("id") id: Long, @Body body: Map<String, String>): Response<UserProfile>

    // ── Hospitals ─────────────────────────────────────────────────────────────
    @GET("hospitals")
    suspend fun getHospitals(): Response<List<Hospital>>

    @GET("hospitals/search")
    suspend fun searchHospitals(@Query("district") district: String): Response<List<Hospital>>

    @GET("hospitals/my")
    suspend fun getMyHospitals(): Response<List<Hospital>>

    @POST("hospitals")
    suspend fun createHospital(@Body hospital: Hospital): Response<Hospital>

    @PUT("hospitals/{id}")
    suspend fun updateHospital(@Path("id") id: Long, @Body hospital: Hospital): Response<Hospital>

    @DELETE("hospitals/{id}")
    suspend fun deleteHospital(@Path("id") id: Long): Response<Unit>

    // ── Clinics ───────────────────────────────────────────────────────────────
    @GET("clinics")
    suspend fun getClinics(): Response<List<Clinic>>

    @GET("clinics/search")
    suspend fun searchClinics(@Query("district") district: String): Response<List<Clinic>>

    @GET("clinics/my")
    suspend fun getMyClinics(): Response<List<Clinic>>

    @POST("clinics")
    suspend fun createClinic(@Body clinic: Clinic): Response<Clinic>

    @PUT("clinics/{id}")
    suspend fun updateClinic(@Path("id") id: Long, @Body clinic: Clinic): Response<Clinic>

    @DELETE("clinics/{id}")
    suspend fun deleteClinic(@Path("id") id: Long): Response<Unit>

    // ── Doctors ───────────────────────────────────────────────────────────────
    @GET("doctors")
    suspend fun getDoctors(): Response<List<Doctor>>

    @GET("doctors/search")
    suspend fun searchDoctors(
        @Query("specialty") specialty: String? = null,
        @Query("hospitalId") hospitalId: Long? = null,
        @Query("telemedicine") telemedicine: Boolean? = null,
        @Query("district") district: String? = null
    ): Response<List<Doctor>>

    @GET("doctors/my")
    suspend fun getMyDoctors(): Response<List<Doctor>>

    @POST("doctors")
    suspend fun createDoctor(@Body doctor: Doctor): Response<Doctor>

    @PUT("doctors/{id}")
    suspend fun updateDoctor(@Path("id") id: Long, @Body doctor: Doctor): Response<Doctor>

    @DELETE("doctors/{id}")
    suspend fun deleteDoctor(@Path("id") id: Long): Response<Unit>

    // ── Ambulances ────────────────────────────────────────────────────────────
    @GET("ambulances")
    suspend fun getAmbulances(): Response<List<Ambulance>>

    @GET("ambulances/search")
    suspend fun searchAmbulances(
        @Query("district") district: String? = null,
        @Query("availableOnly") availableOnly: Boolean = true
    ): Response<List<Ambulance>>

    @GET("ambulances/my")
    suspend fun getMyAmbulances(): Response<List<Ambulance>>

    @POST("ambulances")
    suspend fun createAmbulance(@Body ambulance: Ambulance): Response<Ambulance>

    @PUT("ambulances/{id}")
    suspend fun updateAmbulance(@Path("id") id: Long, @Body ambulance: Ambulance): Response<Ambulance>

    @DELETE("ambulances/{id}")
    suspend fun deleteAmbulance(@Path("id") id: Long): Response<Unit>

    // ── Pharmacies ────────────────────────────────────────────────────────────
    @GET("pharmacies")
    suspend fun getPharmacies(): Response<List<Pharmacy>>

    @GET("pharmacies/search")
    suspend fun searchPharmacies(
        @Query("district") district: String? = null,
        @Query("open24h") open24h: Boolean = false
    ): Response<List<Pharmacy>>

    @GET("pharmacies/my")
    suspend fun getMyPharmacies(): Response<List<Pharmacy>>

    @POST("pharmacies")
    suspend fun createPharmacy(@Body pharmacy: Pharmacy): Response<Pharmacy>

    @PUT("pharmacies/{id}")
    suspend fun updatePharmacy(@Path("id") id: Long, @Body pharmacy: Pharmacy): Response<Pharmacy>

    @DELETE("pharmacies/{id}")
    suspend fun deletePharmacy(@Path("id") id: Long): Response<Unit>

    // ── Blood Banks ───────────────────────────────────────────────────────────
    @GET("blood-banks")
    suspend fun getBloodBanks(): Response<List<BloodBank>>

    @GET("blood-banks/search")
    suspend fun searchBloodBanks(@Query("district") district: String): Response<List<BloodBank>>

    @GET("blood-banks/my")
    suspend fun getMyBloodBanks(): Response<List<BloodBank>>

    @POST("blood-banks")
    suspend fun createBloodBank(@Body bank: BloodBank): Response<BloodBank>

    @PUT("blood-banks/{id}")
    suspend fun updateBloodBank(@Path("id") id: Long, @Body bank: BloodBank): Response<BloodBank>

    @DELETE("blood-banks/{id}")
    suspend fun deleteBloodBank(@Path("id") id: Long): Response<Unit>

    // ── Diagnostic Centers ────────────────────────────────────────────────────
    @GET("diagnostics")
    suspend fun getDiagnostics(): Response<List<DiagnosticCenter>>

    @GET("diagnostics/search")
    suspend fun searchDiagnostics(
        @Query("district") district: String? = null,
        @Query("test") test: String? = null
    ): Response<List<DiagnosticCenter>>

    @GET("diagnostics/my")
    suspend fun getMyDiagnostics(): Response<List<DiagnosticCenter>>

    @POST("diagnostics")
    suspend fun createDiagnostic(@Body diagnostic: DiagnosticCenter): Response<DiagnosticCenter>

    @PUT("diagnostics/{id}")
    suspend fun updateDiagnostic(@Path("id") id: Long, @Body diagnostic: DiagnosticCenter): Response<DiagnosticCenter>

    @DELETE("diagnostics/{id}")
    suspend fun deleteDiagnostic(@Path("id") id: Long): Response<Unit>

    // ── Blood Organizations ───────────────────────────────────────────────────
    @GET("blood-organizations")
    suspend fun getBloodOrgs(): Response<List<BloodOrganization>>

    @GET("blood-organizations/search")
    suspend fun searchBloodOrgs(@Query("district") district: String): Response<List<BloodOrganization>>

    @GET("blood-organizations/my")
    suspend fun getMyBloodOrgs(): Response<List<BloodOrganization>>

    @POST("blood-organizations")
    suspend fun createBloodOrg(@Body org: BloodOrganization): Response<BloodOrganization>

    @PUT("blood-organizations/{id}")
    suspend fun updateBloodOrg(@Path("id") id: Long, @Body org: BloodOrganization): Response<BloodOrganization>

    @DELETE("blood-organizations/{id}")
    suspend fun deleteBloodOrg(@Path("id") id: Long): Response<Unit>

    // ── Donors ────────────────────────────────────────────────────────────────
    @GET("donors")
    suspend fun getDonors(): Response<List<Donor>>

    @GET("donors/search")
    suspend fun searchDonors(
        @Query("bloodGroup") bloodGroup: String? = null,
        @Query("district") district: String? = null
    ): Response<List<Donor>>

    @GET("donors/my")
    suspend fun myDonors(): Response<List<Donor>>

    @POST("donors")
    suspend fun createDonor(@Body donor: Donor): Response<Donor>

    @PUT("donors/{id}")
    suspend fun updateDonor(@Path("id") id: Long, @Body donor: Donor): Response<Donor>

    @DELETE("donors/{id}")
    suspend fun deleteDonor(@Path("id") id: Long): Response<Unit>

    @GET("donors/{id}/eligibility")
    suspend fun checkEligibility(@Path("id") id: Long): Response<Map<String, Any>>

    // ── Blood Requests ────────────────────────────────────────────────────────
    @GET("blood-requests")
    suspend fun getBloodRequests(): Response<List<BloodRequest>>

    @GET("blood-requests/search")
    suspend fun searchBloodRequests(
        @Query("bloodGroup") bloodGroup: String? = null,
        @Query("district") district: String? = null
    ): Response<List<BloodRequest>>

    @GET("blood-requests/my")
    suspend fun myBloodRequests(): Response<List<BloodRequest>>

    @GET("blood-requests/for-donor")
    suspend fun getBloodRequestsForDonor(): Response<List<BloodRequest>>

    @POST("blood-requests")
    suspend fun createBloodRequest(@Body request: BloodRequest): Response<BloodRequest>

    @DELETE("blood-requests/{id}")
    suspend fun deleteBloodRequest(@Path("id") id: Long): Response<Unit>

    // ── Emergency Requests ────────────────────────────────────────────────────
    @POST("emergency-requests")
    suspend fun createEmergencyRequest(@Body request: EmergencyRequest): Response<EmergencyRequest>

    @GET("emergency-requests/my")
    suspend fun myEmergencyRequests(): Response<List<EmergencyRequest>>

    // ── Appointments ──────────────────────────────────────────────────────────
    @GET("appointments/my")
    suspend fun myAppointments(): Response<List<Appointment>>

    @POST("appointments")
    suspend fun bookAppointment(@Body appointment: Appointment): Response<Appointment>

    @DELETE("appointments/{id}")
    suspend fun cancelAppointment(@Path("id") id: Long): Response<Unit>

    // ── Extended Blood Features ───────────────────────────────────────────────
    @PUT("blood-requests/{id}/status")
    suspend fun updateBloodRequestStatus(
        @Path("id") id: Long,
        @Query("status") status: String
    ): Response<BloodRequest>

    @GET("donors/rewards")
    suspend fun getDonorRewards(): Response<Map<String, Any>>

    @POST("donors/verify-donation")
    suspend fun verifyDonation(@Body body: Map<String, Long>): Response<Map<String, Any>>

    @GET("blood-requests/dashboard")
    suspend fun getDashboardBloodRequests(): Response<List<BloodRequest>>

    // ── Telemedicine ──────────────────────────────────────────────────────────
    @GET("telemedicine/my")
    suspend fun myTelemedicine(): Response<List<TelemedicineSession>>

    @POST("telemedicine")
    suspend fun createTelemedicineSession(@Body session: TelemedicineSession): Response<TelemedicineSession>

    // ── Medicine Reminders ────────────────────────────────────────────────────
    @GET("reminders/my")
    suspend fun myReminders(): Response<List<MedicineReminder>>

    @POST("reminders")
    suspend fun createReminder(@Body reminder: MedicineReminder): Response<MedicineReminder>

    @PUT("reminders/{id}")
    suspend fun updateReminder(@Path("id") id: Long, @Body reminder: MedicineReminder): Response<MedicineReminder>

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: Long): Response<Unit>

    // ── Health Records ────────────────────────────────────────────────────────
    @GET("health-records/my")
    suspend fun myHealthRecords(): Response<List<HealthRecord>>

    @POST("health-records")
    suspend fun createHealthRecord(@Body record: HealthRecord): Response<HealthRecord>

    @PUT("health-records/{id}")
    suspend fun updateHealthRecord(@Path("id") id: Long, @Body record: HealthRecord): Response<HealthRecord>

    @DELETE("health-records/{id}")
    suspend fun deleteHealthRecord(@Path("id") id: Long): Response<Unit>

    // ── App Settings ──────────────────────────────────────────────────────────
    @GET("settings")
    suspend fun getSettings(): Response<List<AppSetting>>

    @GET("slides")
    suspend fun getDashboardSlides(): Response<List<DashboardSlide>>

    // ── Notifications ─────────────────────────────────────────────────────────
    @GET("notifications/my")
    suspend fun getNotifications(): Response<List<Notification>>

    // ── Reviews ───────────────────────────────────────────────────────────────
    @GET("reviews")
    suspend fun getReviews(@Query("type") type: String, @Query("id") id: Long): Response<List<Review>>

    @POST("reviews")
    suspend fun submitReview(@Body review: Review): Response<Review>

    // ── Chat ──────────────────────────────────────────────────────────────────
    @GET("chat/rooms")
    suspend fun getRooms(): Response<List<ChatRoom>>

    @GET("chat/messages/{roomId}")
    suspend fun getMessages(@Path("roomId") roomId: Long): Response<List<ChatMessage>>

    @POST("chat/send")
    suspend fun sendMessage(@Body msg: ChatMessage): Response<ChatMessage>

    @POST("chat/start/{userId}")
    suspend fun startChat(@Path("userId") userId: Long): Response<ChatRoom>
}
