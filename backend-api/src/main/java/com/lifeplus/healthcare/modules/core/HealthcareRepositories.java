package com.lifeplus.healthcare.modules.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

// ── Public interface (used by AuthController and AdminController) ─────────────
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}

interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByUserId(Long userId);
    List<UserRole> findByRole(String role);
    boolean existsByUserIdAndRole(Long userId, String role);
}

interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}

interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByPhoneOrderByCreatedAtDesc(String phone);
    Optional<OtpVerification> findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(String phone);
}

interface AppSettingRepository extends JpaRepository<AppSetting, Long> {
    Optional<AppSetting> findBySettingKey(String settingKey);
    List<AppSetting> findByCategory(String category);
}

interface DashboardSlideRepository extends JpaRepository<DashboardSlide, Long> {
    List<DashboardSlide> findByActiveTrueOrderByDisplayOrderAsc();
    List<DashboardSlide> findAllByOrderByDisplayOrderAsc();
}

interface WalkthroughSlideRepository extends JpaRepository<WalkthroughSlide, Long> {
    List<WalkthroughSlide> findByActiveTrueOrderByDisplayOrderAsc();
    List<WalkthroughSlide> findAllByOrderByDisplayOrderAsc();
}

// ── Package-private interfaces (used only within this package) ────────────────
interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodGroupAndDistrictAndAvailableNowTrue(BloodGroup bloodGroup, String district);
    List<Donor> findByBloodGroupAndAvailableNowTrue(BloodGroup bloodGroup);
    List<Donor> findByUserId(Long userId);
    List<Donor> findByDistrict(String district);
}

interface BloodOrganizationRepository extends JpaRepository<BloodOrganization, Long> {
    List<BloodOrganization> findByDistrict(String district);
    List<BloodOrganization> findByUserId(Long userId);
}

interface BloodBankRepository extends JpaRepository<BloodBank, Long> {
    List<BloodBank> findByDistrict(String district);
    List<BloodBank> findByUserId(Long userId);
}

interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {
    List<BloodInventory> findByBloodBankId(Long bloodBankId);
    List<BloodInventory> findByBloodGroup(BloodGroup bloodGroup);
    List<BloodInventory> findByBloodGroupAndUnitsAvailableGreaterThan(BloodGroup bloodGroup, int minUnits);
}

interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByStatus(RequestStatus status);
    List<BloodRequest> findByBloodGroupAndDistrict(BloodGroup bloodGroup, String district);
    List<BloodRequest> findByRequestedByUserId(Long userId);
    List<BloodRequest> findByDonorId(Long donorId);
}

interface HospitalRepository extends JpaRepository<Hospital, Long> {
    List<Hospital> findByDistrict(String district);
    List<Hospital> findByDistrictAndUpazila(String district, String upazila);
    List<Hospital> findByUserId(Long userId);
}

interface ClinicRepository extends JpaRepository<Clinic, Long> {
    List<Clinic> findByDistrict(String district);
    List<Clinic> findByUserId(Long userId);
}

interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalId(Long hospitalId);
    List<Doctor> findByHospitalIdIn(List<Long> hospitalIds);
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);
    List<Doctor> findByDistrict(String district);
    List<Doctor> findBySpecialtyContainingIgnoreCaseAndDistrict(String specialty, String district);
    List<Doctor> findByAvailableForTelemedicineTrue();
    List<Doctor> findByAvailableForTelemedicineTrueAndDistrict(String district);
}

interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByDistrict(String district);
    List<Ambulance> findByDistrictAndAvailableTrue(String district);
    List<Ambulance> findByUserId(Long userId);
}

interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByStatus(ApprovalStatus status);
    List<ApprovalRequest> findByRequesterUserId(Long userId);
    List<ApprovalRequest> findByEntityTypeAndEntityId(String entityType, Long entityId);
}

interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByUserIdAndStatus(Long userId, String status);
}

interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    List<EmergencyRequest> findByStatus(EmergencyStatus status);
    List<EmergencyRequest> findByDistrict(String district);
    List<EmergencyRequest> findByUserId(Long userId);
}

interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    List<Pharmacy> findByDistrict(String district);
    List<Pharmacy> findByOpen24hTrue();
    List<Pharmacy> findByDistrictAndOpen24hTrue(String district);
    List<Pharmacy> findByUserId(Long userId);
}

interface DiagnosticCenterRepository extends JpaRepository<DiagnosticCenter, Long> {
    List<DiagnosticCenter> findByDistrict(String district);
    List<DiagnosticCenter> findByTestsOfferedContainingIgnoreCase(String test);
    List<DiagnosticCenter> findByTestsOfferedContainingIgnoreCaseAndDistrict(String test, String district);
    List<DiagnosticCenter> findByUserId(Long userId);
}

interface TelemedicineSessionRepository extends JpaRepository<TelemedicineSession, Long> {
    List<TelemedicineSession> findByPatientUserId(Long patientUserId);
    List<TelemedicineSession> findByDoctorId(Long doctorId);
    List<TelemedicineSession> findByStatus(TelemedicineStatus status);
}

interface MedicineReminderRepository extends JpaRepository<MedicineReminder, Long> {
    List<MedicineReminder> findByUserId(Long userId);
    List<MedicineReminder> findByUserIdAndActiveTrue(Long userId);
}

interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByUserId(Long userId);
    List<HealthRecord> findByUserIdAndRecordType(Long userId, String recordType);
}

interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
}

interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}

interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT r FROM ChatRoom r WHERE r.participantOneId = :userId OR r.participantTwoId = :userId ORDER BY r.lastMessageTime DESC")
    List<ChatRoom> findMyRooms(Long userId);

    @Query("SELECT r FROM ChatRoom r WHERE (r.participantOneId = :p1 AND r.participantTwoId = :p2) OR (r.participantOneId = :p2 AND r.participantTwoId = :p1)")
    java.util.Optional<ChatRoom> findBetween(Long p1, Long p2);
}

interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(Long roomId);
}

interface HealthTipRepository extends JpaRepository<HealthTip, Long> {
    List<HealthTip> findByCategory(String category);
}

interface VaccinationScheduleRepository extends JpaRepository<VaccinationSchedule, Long> {}
