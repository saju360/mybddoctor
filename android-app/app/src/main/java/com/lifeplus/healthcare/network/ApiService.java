package com.lifeplus.healthcare.network;

import com.lifeplus.healthcare.model.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ── App Settings & Slides (public — no auth needed) ───────────────────────
    @GET("settings")       Call<List<Map<String, Object>>> getSettings();
    @GET("settings/{key}") Call<Map<String, Object>>       getSetting(@Path("key") String key);
    @GET("slides")         Call<List<DashboardSlide>>      getDashboardSlides();
    @GET("walkthrough")    Call<List<WalkthroughSlide>>    getWalkthroughSlides();

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/login")    Call<AuthModels.AuthResponse> login(@Body AuthModels.LoginRequest b);
    @POST("auth/register") Call<AuthModels.AuthResponse> register(@Body AuthModels.RegisterRequest b);
    @POST("auth/refresh")  Call<AuthModels.AuthResponse> refresh(@Body AuthModels.RefreshRequest b);

    // ── Users ─────────────────────────────────────────────────────────────────
    @GET("users/me")       Call<Map<String, Object>> getMe();
    @PUT("users/{id}")     Call<Map<String, Object>> updateUser(@Path("id") long id, @Body Map<String, String> body);

    // ── Donors ────────────────────────────────────────────────────────────────
    @GET("donors")         Call<List<Donor>> getDonors();
    @GET("donors/{id}")    Call<Donor>       getDonor(@Path("id") long id);
    @GET("donors/search")  Call<List<Donor>> searchDonors(@Query("bloodGroup") String bg, @Query("district") String d);
    @GET("donors/my")      Call<List<Donor>> myDonors();
    @GET("donors/{id}/eligibility") Call<Map<String, Object>> checkEligibility(@Path("id") long id);
    @POST("donors")        Call<Donor>       createDonor(@Body Donor donor);
    @PUT("donors/{id}")    Call<Donor>       updateDonor(@Path("id") long id, @Body Donor donor);
    @DELETE("donors/{id}") Call<Void>        deleteDonor(@Path("id") long id);

    // ── Blood Requests ────────────────────────────────────────────────────────
    @GET("blood-requests")         Call<List<BloodRequest>> getBloodRequests();
    @GET("blood-requests/{id}")    Call<BloodRequest>       getBloodRequest(@Path("id") long id);
    @GET("blood-requests/search")  Call<List<BloodRequest>> searchBloodRequests(@Query("bloodGroup") String bg, @Query("district") String d);
    @GET("blood-requests/my")      Call<List<BloodRequest>> myBloodRequests();
    @GET("blood-requests/for-donor") Call<List<BloodRequest>> getBloodRequestsForDonor();
    @POST("blood-requests")        Call<BloodRequest>       createBloodRequest(@Body BloodRequest r);
    @PUT("blood-requests/{id}")    Call<BloodRequest>       updateBloodRequest(@Path("id") long id, @Body BloodRequest r);
    @PUT("blood-requests/{id}/status") Call<BloodRequest>    updateBloodRequestStatus(@Path("id") long id, @Query("status") String status);
    @DELETE("blood-requests/{id}") Call<Void>               deleteBloodRequest(@Path("id") long id);

    // ── Emergency Requests ────────────────────────────────────────────────────
    @GET("emergency-requests")             Call<List<EmergencyRequest>> getEmergencyRequests();
    @GET("emergency-requests/my")          Call<List<EmergencyRequest>> myEmergencyRequests();
    @POST("emergency-requests")            Call<EmergencyRequest>       createEmergencyRequest(@Body EmergencyRequest r);
    @PUT("emergency-requests/{id}/status") Call<EmergencyRequest>       updateEmergencyStatus(@Path("id") long id, @Query("status") String status);

    // ── Hospitals ─────────────────────────────────────────────────────────────
    @GET("hospitals")          Call<List<Hospital>> getHospitals();
    @GET("hospitals/{id}")     Call<Hospital>       getHospital(@Path("id") long id);
    @GET("hospitals/search")   Call<List<Hospital>> searchHospitals(@Query("district") String d);
    @POST("hospitals")         Call<Hospital>       createHospital(@Body Hospital h);
    @PUT("hospitals/{id}")     Call<Hospital>       updateHospital(@Path("id") long id, @Body Hospital h);
    @DELETE("hospitals/{id}")  Call<Void>           deleteHospital(@Path("id") long id);

    // ── Clinics ───────────────────────────────────────────────────────────────
    @GET("clinics")            Call<List<Clinic>> getClinics();
    @GET("clinics/{id}")       Call<Clinic>       getClinic(@Path("id") long id);
    @GET("clinics/search")     Call<List<Clinic>> searchClinics(@Query("district") String d);
    @POST("clinics")           Call<Clinic>       createClinic(@Body Clinic c);
    @PUT("clinics/{id}")       Call<Clinic>       updateClinic(@Path("id") long id, @Body Clinic c);
    @DELETE("clinics/{id}")    Call<Void>         deleteClinic(@Path("id") long id);

    // ── Doctors ───────────────────────────────────────────────────────────────
    @GET("doctors")            Call<List<Doctor>> getDoctors();
    @GET("doctors/{id}")       Call<Doctor>       getDoctor(@Path("id") long id);
    @GET("doctors/search")     Call<List<Doctor>> searchDoctors(@Query("specialty") String s, @Query("telemedicine") Boolean t);
    @POST("doctors")           Call<Doctor>       createDoctor(@Body Doctor d);
    @PUT("doctors/{id}")       Call<Doctor>       updateDoctor(@Path("id") long id, @Body Doctor d);
    @DELETE("doctors/{id}")    Call<Void>         deleteDoctor(@Path("id") long id);

    // ── Ambulances ────────────────────────────────────────────────────────────
    @GET("ambulances")         Call<List<Ambulance>> getAmbulances();
    @GET("ambulances/{id}")    Call<Ambulance>       getAmbulance(@Path("id") long id);
    @GET("ambulances/search")  Call<List<Ambulance>> searchAmbulances(@Query("district") String d, @Query("availableOnly") boolean a);
    @POST("ambulances")        Call<Ambulance>       createAmbulance(@Body Ambulance a);
    @PUT("ambulances/{id}")    Call<Ambulance>       updateAmbulance(@Path("id") long id, @Body Ambulance a);
    @DELETE("ambulances/{id}") Call<Void>            deleteAmbulance(@Path("id") long id);

    // ── Pharmacies ────────────────────────────────────────────────────────────
    @GET("pharmacies")         Call<List<Pharmacy>> getPharmacies();
    @GET("pharmacies/{id}")    Call<Pharmacy>       getPharmacy(@Path("id") long id);
    @GET("pharmacies/search")  Call<List<Pharmacy>> searchPharmacies(@Query("district") String d, @Query("open24h") boolean o);
    @POST("pharmacies")        Call<Pharmacy>       createPharmacy(@Body Pharmacy p);
    @PUT("pharmacies/{id}")    Call<Pharmacy>       updatePharmacy(@Path("id") long id, @Body Pharmacy p);
    @DELETE("pharmacies/{id}") Call<Void>           deletePharmacy(@Path("id") long id);

    // ── Diagnostic Centers ────────────────────────────────────────────────────
    @GET("diagnostics")        Call<List<DiagnosticCenter>> getDiagnostics();
    @GET("diagnostics/{id}")   Call<DiagnosticCenter>       getDiagnostic(@Path("id") long id);
    @GET("diagnostics/search") Call<List<DiagnosticCenter>> searchDiagnostics(@Query("district") String d, @Query("test") String t);
    @POST("diagnostics")       Call<DiagnosticCenter>       createDiagnostic(@Body DiagnosticCenter d);
    @PUT("diagnostics/{id}")   Call<DiagnosticCenter>       updateDiagnostic(@Path("id") long id, @Body DiagnosticCenter d);
    @DELETE("diagnostics/{id}")Call<Void>                   deleteDiagnostic(@Path("id") long id);

    // ── Blood Banks ───────────────────────────────────────────────────────────
    @GET("blood-banks")        Call<List<BloodBank>> getBloodBanks();
    @GET("blood-banks/{id}")   Call<BloodBank>       getBloodBank(@Path("id") long id);
    @GET("blood-banks/search") Call<List<BloodBank>> searchBloodBanks(@Query("district") String d);
    @POST("blood-banks")       Call<BloodBank>       createBloodBank(@Body BloodBank b);
    @PUT("blood-banks/{id}")   Call<BloodBank>       updateBloodBank(@Path("id") long id, @Body BloodBank b);
    @DELETE("blood-banks/{id}")Call<Void>            deleteBloodBank(@Path("id") long id);

    // ── Blood Organizations ───────────────────────────────────────────────────
    @GET("blood-organizations")          Call<List<BloodOrganization>> getBloodOrgs();
    @GET("blood-organizations/{id}")     Call<BloodOrganization>       getBloodOrg(@Path("id") long id);
    @GET("blood-organizations/search")   Call<List<BloodOrganization>> searchBloodOrgs(@Query("district") String d);
    @POST("blood-organizations")         Call<BloodOrganization>       createBloodOrg(@Body BloodOrganization o);
    @PUT("blood-organizations/{id}")     Call<BloodOrganization>       updateBloodOrg(@Path("id") long id, @Body BloodOrganization o);
    @DELETE("blood-organizations/{id}")  Call<Void>                    deleteBloodOrg(@Path("id") long id);

    // ── Appointments ──────────────────────────────────────────────────────────
    @GET("appointments/my")      Call<List<Appointment>> myAppointments();
    @GET("appointments/{id}")    Call<Appointment>       getAppointment(@Path("id") long id);
    @POST("appointments")        Call<Appointment>       bookAppointment(@Body Appointment a);
    @PUT("appointments/{id}")    Call<Appointment>       updateAppointment(@Path("id") long id, @Body Appointment a);
    @DELETE("appointments/{id}") Call<Void>              cancelAppointment(@Path("id") long id);

    // ── Telemedicine ──────────────────────────────────────────────────────────
    @GET("telemedicine/my")      Call<List<TelemedicineSession>> myTelemedicine();
    @GET("telemedicine/{id}")    Call<TelemedicineSession>       getTelemedicine(@Path("id") long id);
    @POST("telemedicine")        Call<TelemedicineSession>       createTelemedicine(@Body TelemedicineSession s);
    @PUT("telemedicine/{id}")    Call<TelemedicineSession>       updateTelemedicine(@Path("id") long id, @Body TelemedicineSession s);
    @DELETE("telemedicine/{id}") Call<Void>                      cancelTelemedicine(@Path("id") long id);

    // ── Medicine Reminders ────────────────────────────────────────────────────
    @GET("reminders/my")         Call<List<MedicineReminder>> myReminders();
    @GET("reminders/my/active")  Call<List<MedicineReminder>> myActiveReminders();
    @GET("reminders/{id}")       Call<MedicineReminder>       getReminder(@Path("id") long id);
    @POST("reminders")           Call<MedicineReminder>       createReminder(@Body MedicineReminder r);
    @PUT("reminders/{id}")       Call<MedicineReminder>       updateReminder(@Path("id") long id, @Body MedicineReminder r);
    @DELETE("reminders/{id}")    Call<Void>                   deleteReminder(@Path("id") long id);

    // ── Health Records ────────────────────────────────────────────────────────
    @GET("health-records/my")        Call<List<HealthRecord>> myHealthRecords();
    @GET("health-records/{id}")      Call<HealthRecord>       getHealthRecord(@Path("id") long id);
    @POST("health-records")          Call<HealthRecord>       createHealthRecord(@Body HealthRecord r);
    @PUT("health-records/{id}")      Call<HealthRecord>       updateHealthRecord(@Path("id") long id, @Body HealthRecord r);
    @DELETE("health-records/{id}")   Call<Void>               deleteHealthRecord(@Path("id") long id);

    // ── Approval Requests (user-submitted) ────────────────────────────────────
    @POST("approvals")
    Call<java.util.Map<String, Object>> submitApprovalRequest(@Body java.util.Map<String, Object> body);

    @GET("approvals/my")
    Call<List<java.util.Map<String, Object>>> myApprovals();
}
