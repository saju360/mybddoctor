package com.lifeplus.healthcare.modules.core;

import com.lifeplus.healthcare.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

enum RoleType { ADMIN, USER, HOSPITAL_ADMIN, BLOOD_ORG_ADMIN, AMBULANCE_PROVIDER }
enum BloodGroup { A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG }
enum RequestStatus { OPEN, PENDING, ACCEPTED, REJECTED, FULFILLED, CANCELLED }
enum ApprovalStatus { PENDING, APPROVED, REJECTED }
enum ApprovalAction { ADD, EDIT, DELETE }
enum EmergencyStatus { OPEN, DISPATCHED, RESOLVED, CANCELLED }
enum TelemedicineStatus { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }

@Entity @Table(name = "roles") @Getter @Setter
class Role extends BaseEntity {
    @Enumerated(EnumType.STRING) @Column(nullable = false, unique = true) private RoleType name;
}

@Entity @Table(name = "users") @Getter @Setter
class User extends BaseEntity {
    @Column(nullable = false) private String fullName;
    @Column(nullable = false, unique = true) private String phone;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false) private String preferredLanguage;
    @Column(nullable = true) private String bloodGroup;
    @Column(nullable = true) private String district;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = true) private String role;
}

@Entity @Table(name = "user_roles") @Getter @Setter
class UserRole extends BaseEntity {
    @Column(nullable = false, unique = true) private Long userId;
    @Column(nullable = false) private String role = "USER";
}

@Entity @Table(name = "otp_verifications") @Getter @Setter
class OtpVerification extends BaseEntity {
    @Column(nullable = false) private String phone;
    @Column(nullable = false) private String otpCode;
    @Column(nullable = false) private LocalDateTime expiresAt;
    @Column(nullable = false) private boolean verified = false;
}

@Entity @Table(name = "donors") @Getter @Setter
class Donor extends BaseEntity {
    @Column(nullable = false) private Long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodGroup bloodGroup;
    @Column(nullable = false) private String district;
    @Column(nullable = false) private String upazila;
    @Column(nullable = false) private boolean availableNow = true;
    @Column(nullable = false) private String status = "PENDING";
    @Column(nullable = true) private String contactPhone;
    @Column(nullable = true) private String lastDonationDate;
    @Column(nullable = false) private Integer rewardPoints = 0;
    @Column(nullable = true, columnDefinition = "TEXT") private String physicalHistory;
}

@Entity @Table(name = "blood_organizations") @Getter @Setter
class BloodOrganization extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String district;
    @Column(nullable = false) private String upazila;
    @Column(nullable = true) private String phone;
    @Column(nullable = true) private String address;
}

@Entity @Table(name = "blood_banks") @Getter @Setter
class BloodBank extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String district;
    @Column(nullable = false) private String upazila;
    @Column(nullable = false) private String phone;
    @Column(nullable = true) private String address;
}

@Entity @Table(name = "blood_inventory") @Getter @Setter
class BloodInventory extends BaseEntity {
    @Column(nullable = false) private Long bloodBankId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodGroup bloodGroup;
    @Column(nullable = false) private Integer unitsAvailable = 0;
}

@Entity @Table(name = "blood_requests") @Getter @Setter
class BloodRequest extends BaseEntity {
    @Column(nullable = true) private Long requestedByUserId;
    @Column(nullable = true) private Long donorId;
    @Column(nullable = true) private String patientName;
    @Column(nullable = true) private String hospitalName;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BloodGroup bloodGroup;
    @Column(nullable = false) private String district;
    @Column(nullable = true) private String upazila;
    @Column(nullable = true) private String contactPhone;
    @Column(nullable = true) private String urgency;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RequestStatus status = RequestStatus.OPEN;
    @Column(nullable = true) private String notes;
}

@Entity @Table(name = "approval_requests") @Getter @Setter
class ApprovalRequest extends BaseEntity {
    @Column(nullable = false) private String entityType;
    @Column(nullable = true) private Long entityId;
    @Column(nullable = false) private Long requesterUserId;
    @Column(nullable = true) private String requestedRole;
    @Enumerated(EnumType.STRING) @Column(nullable = true) private ApprovalAction action = ApprovalAction.ADD;
    @Column(nullable = true, columnDefinition = "LONGTEXT") private String entityData;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ApprovalStatus status = ApprovalStatus.PENDING;
    @Column(nullable = true) private Long reviewedByUserId;
    @Column(nullable = true) private String reviewedAt;
    @Column(nullable = true, columnDefinition = "TEXT") private String notes;
}

@Entity @Table(name = "hospitals") @Getter @Setter
class Hospital extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String district;
    @Column(nullable = false) private String upazila;
    @Column(nullable = true) private String phone;
    @Column(nullable = true) private String address;
    @Column(nullable = true) private String type;
    @Column(nullable = false) private boolean icuAvailable = false;
    @Column(nullable = false) private boolean open24h = false;
}

@Entity @Table(name = "clinics") @Getter @Setter
class Clinic extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String district;
    @Column(nullable = false) private String upazila;
    @Column(nullable = true) private String phone;
    @Column(nullable = true) private String address;
    @Column(nullable = true) private String specialties;
}

@Entity @Table(name = "doctors") @Getter @Setter
class Doctor extends BaseEntity {
    @Column(nullable = false) private String fullName;
    @Column(nullable = false) private String specialty;
    @Column(nullable = false) private Long hospitalId;
    @Column(nullable = true) private String district;
    @Column(nullable = true) private String qualifications;
    @Column(nullable = true) private String chamberSchedule;
    @Column(nullable = true) private String phone;
    @Column(nullable = false) private boolean availableForTelemedicine = false;
    @Column(nullable = false) private boolean available = true;
}

@Entity @Table(name = "ambulances") @Getter @Setter
class Ambulance extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = false) private String providerName;
    @Column(nullable = false) private String district;
    @Column(nullable = false) private String upazila;
    @Column(nullable = false) private String phone;
    @Column(nullable = true) private String vehicleNumber;
    @Column(nullable = false) private boolean icuEquipped = false;
    @Column(nullable = false) private boolean available = true;
}

@Entity @Table(name = "appointments") @Getter @Setter
class Appointment extends BaseEntity {
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private Long doctorId;
    @Column(nullable = false) private String appointmentDate;
    @Column(nullable = true) private String timeSlot;
    @Column(nullable = false) private String status = "PENDING";
    @Column(nullable = true) private String notes;
    @Transient private String doctorName;
    @Transient private String specialty;
}

@Entity @Table(name = "emergency_requests") @Getter @Setter
class EmergencyRequest extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = true) private String callerName;
    @Column(nullable = true) private String phone;
    @Column(nullable = false) private String district;
    @Column(nullable = true) private String upazila;
    @Column(nullable = true) private String contactPhone;
    @Column(nullable = true) private String emergencyType;
    @Column(nullable = true, columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EmergencyStatus status = EmergencyStatus.OPEN;
}

@Entity @Table(name = "pharmacies") @Getter @Setter
class Pharmacy extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String district;
    @Column(nullable = true) private String upazila;
    @Column(nullable = false) private String phone;
    @Column(nullable = true) private String address;
    @Column(nullable = false) private Boolean open24h = false;
    @Column(nullable = false) private boolean active = true;
}

@Entity @Table(name = "diagnostic_centers") @Getter @Setter
class DiagnosticCenter extends BaseEntity {
    @Column(nullable = true) private Long userId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String district;
    @Column(nullable = true) private String upazila;
    @Column(nullable = false) private String testsOffered;
    @Column(nullable = true) private String phone;
    @Column(nullable = true) private String address;
}

@Entity @Table(name = "telemedicine_sessions") @Getter @Setter
class TelemedicineSession extends BaseEntity {
    @Column(nullable = false) private Long patientUserId;
    @Column(nullable = false) private Long doctorId;
    @Column(nullable = true) private String doctorName;
    @Column(nullable = true) private String scheduledAt;
    @Column(nullable = true) private String date;
    @Column(nullable = true) private String time;
    @Column(nullable = true) private String platform;
    @Column(nullable = true) private String meetingLink;
    @Column(nullable = true, columnDefinition = "TEXT") private String notes;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TelemedicineStatus status = TelemedicineStatus.SCHEDULED;
}

@Entity @Table(name = "medicine_reminders") @Getter @Setter
class MedicineReminder extends BaseEntity {
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private String medicineName;
    @Column(nullable = false) private String reminderTime;
    @Column(nullable = true) private String dosage;
    @Column(nullable = true) private String frequency;
    @Column(nullable = false) private Boolean active = true;
}

@Entity @Table(name = "health_records") @Getter @Setter
class HealthRecord extends BaseEntity {
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private String recordType;
    @Column(nullable = false, columnDefinition = "TEXT") private String recordData;
    @Column(nullable = true) private String recordDate;
    @Column(nullable = true) private String doctorName;
    @Column(nullable = true) private String facilityName;
    @Column(nullable = true) private String imageUrl;
}

@Entity @Table(name = "notifications") @Getter @Setter
class Notification extends BaseEntity {
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String message;
    private boolean isRead = false;
    @Column(nullable = true) private String type;
}

// ── Chat ─────────────────────────────────────────────────────────────────────
@Entity @Table(name = "chat_rooms") @Getter @Setter
class ChatRoom extends BaseEntity {
    @Column(nullable = false) private Long participantOneId;
    @Column(nullable = false) private Long participantTwoId;
    private String lastMessage;
    private LocalDateTime lastMessageTime = LocalDateTime.now();
}

@Entity @Table(name = "chat_messages") @Getter @Setter
class ChatMessage extends BaseEntity {
    @Column(nullable = false) private Long roomId;
    @Column(nullable = false) private Long senderId;
    @Column(nullable = false, columnDefinition = "TEXT") private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean isRead = false;
}

@Entity @Table(name = "reviews") @Getter @Setter
class Review extends BaseEntity {
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private String userName;
    @Column(nullable = false) private String entityType;
    @Column(nullable = false) private Long entityId;
    @Column(nullable = false) private int rating;
    @Column(length = 1000) private String comment;
}

@Entity @Table(name = "app_settings") @Getter @Setter
class AppSetting extends BaseEntity {
    @Column(nullable = false, unique = true) private String settingKey;
    @Column(nullable = false, columnDefinition = "TEXT") private String settingValue;
    @Column(nullable = true) private String description;
    @Column(nullable = false) private String category = "GENERAL";
}

@Entity @Table(name = "dashboard_slides") @Getter @Setter
class DashboardSlide extends BaseEntity {
    @Column(nullable = false) private String title;
    @Column(nullable = true) private String subtitle;
    @Column(nullable = false) private String imageUrl;
    @Column(nullable = true) private String actionUrl;
    @Column(nullable = false) private int displayOrder = 0;
    @Column(nullable = false) private boolean active = true;
}

@Entity @Table(name = "walkthrough_slides") @Getter @Setter
class WalkthroughSlide extends BaseEntity {
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String subtitle;
    @Column(nullable = false) private String iconName;
    @Column(nullable = false) private String accentColor;
    @Column(nullable = false) private int displayOrder = 0;
    @Column(nullable = false) private boolean active = true;
}

@Entity @Table(name = "health_tips") @Getter @Setter
class HealthTip extends BaseEntity {
    @Column(nullable = false) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(nullable = true) private String category;
}

@Entity @Table(name = "vaccination_schedules") @Getter @Setter
class VaccinationSchedule extends BaseEntity {
    @Column(nullable = false) private String vaccineName;
    @Column(nullable = false) private String ageGroup;
    @Column(nullable = true) private String notes;
}
