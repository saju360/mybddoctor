package com.lifeplus.healthcare.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────
data class LoginRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("bloodGroup") val bloodGroup: String,
    @SerializedName("district") val district: String,
    @SerializedName("otpCode") val otpCode: String? = null
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("userId") val userId: Long = 0,
    @SerializedName("role") val role: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("isApproved") val isApproved: Boolean = true,
    @SerializedName("isVerified") val isVerified: Boolean = true
)

// ── Hospital ──────────────────────────────────────────────────────────────────
data class Hospital(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("icuAvailable") val icuAvailable: Boolean = false,
    @SerializedName("open24h") val open24h: Boolean = false,
    @SerializedName("status") val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("adminNotes") val adminNotes: String? = null
)

// ── Clinic ────────────────────────────────────────────────────────────────────
data class Clinic(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("specialties") val specialties: String = "",
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("adminNotes") val adminNotes: String? = null
)

// ── Doctor ────────────────────────────────────────────────────────────────────
data class Doctor(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("fullName") val name: String = "",
    @SerializedName("specialty") val specialty: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("qualifications") val qualifications: String = "",
    @SerializedName("hospitalId") val hospitalId: Long = 0,
    @SerializedName(value = "availableForTelemedicine", alternate = ["telemedicineAvailable"]) val telemedicineAvailable: Boolean = false,
    @SerializedName("available") val available: Boolean = true,
    @SerializedName("consultationHours") val consultationHours: String = "10:00 AM - 05:00 PM",
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("adminNotes") val adminNotes: String? = null
)

// ── Ambulance ─────────────────────────────────────────────────────────────────
data class Ambulance(
    @SerializedName("id") val id: Long = 0,
    @SerializedName(value = "providerName", alternate = ["name"]) val name: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("icuEquipped") val icuEquipped: Boolean = false,
    @SerializedName("available") val available: Boolean = true,
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("adminNotes") val adminNotes: String? = null
)

// ── Pharmacy ──────────────────────────────────────────────────────────────────
data class Pharmacy(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("open24h") val open24h: Boolean = false,
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("adminNotes") val adminNotes: String? = null
)

// ── Blood Bank ────────────────────────────────────────────────────────────────
data class BloodBank(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("availableGroups") val availableGroups: List<String> = emptyList(),
    @SerializedName("donorCount") val donorCount: Int = 0
)

// ── Diagnostic Center ─────────────────────────────────────────────────────────
data class DiagnosticCenter(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("testsOffered") val testsOffered: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("adminNotes") val adminNotes: String? = null
)

// ── Blood Organization ────────────────────────────────────────────────────────
data class BloodOrganization(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("donorCount") val donorCount: Int = 0
)

// ── Donor ─────────────────────────────────────────────────────────────────────
data class Donor(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("bloodGroup") val bloodGroup: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("contactPhone") val contactPhone: String = "",
    @SerializedName("availableNow") val availableNow: Boolean = true,
    @SerializedName("lastDonationDate") val lastDonationDate: String? = null,
    @SerializedName("rewardPoints") val rewardPoints: Int = 0,
    @SerializedName("physicalHistory") val physicalHistory: String = "",
    @SerializedName("donationCount") val donationCount: Int = 0,
    @SerializedName("rank") val rank: String = "Bronze",
    @SerializedName("status") val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("adminNotes") val adminNotes: String? = null,
    @SerializedName("organizationId") val organizationId: Long? = null,
    @SerializedName("organizationName") val organizationName: String? = null
) {
    val isApproved: Boolean get() = status == "APPROVED"

    companion object {
        fun parseBloodGroup(s: String?): String {
            if (s == null || s.contains("Select")) return "O_POS"
            if (s.contains("_")) return s
            return s.replace("+", "_POS").replace("-", "_NEG").replace(" ", "")
        }

        fun formatBloodGroup(s: String?): String {
            if (s == null) return "O_POS"
            return s
        }
    }
}

// ── Blood Request ─────────────────────────────────────────────────────────────
data class BloodRequest(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("donorId") val donorId: Long? = null,
    @SerializedName("bloodGroup") val bloodGroup: String? = null,
    @SerializedName("patientName") val patientName: String? = null,
    @SerializedName("hospitalName") val hospitalName: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("upazila") val upazila: String? = null,
    @SerializedName("urgency") val urgency: String? = null,
    @SerializedName("contactPhone") val contactPhone: String? = null,
    @SerializedName("status") val status: String = "OPEN", // OPEN, PENDING, ACCEPTED, FULFILLED, CANCELLED
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
) {
    companion object {
        fun parseBloodGroup(s: String?): String {
            if (s == null || s.contains("Select")) return "O_POS"
            if (s.contains("_")) return s
            return s.replace("+", "_POS").replace("-", "_NEG").replace(" ", "")
        }
    }
}

// ── Emergency Request ─────────────────────────────────────────────────────────
data class EmergencyRequest(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("userId") val userId: Long? = null,
    @SerializedName("callerName") val callerName: String = "",
    @SerializedName(value = "contactPhone", alternate = ["phone"]) val contactPhone: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("upazila") val upazila: String = "",
    @SerializedName("emergencyType") val emergencyType: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("status") val status: String = "PENDING"
)

// ── Appointment ───────────────────────────────────────────────────────────────
data class Appointment(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("doctorId") val doctorId: Long = 0,
    @SerializedName("doctorName") val doctorName: String = "",
    @SerializedName("specialty") val specialty: String = "",
    @SerializedName(value = "appointmentDate", alternate = ["date"]) val date: String = "",
    @SerializedName(value = "timeSlot", alternate = ["time"]) val time: String = "",
    @SerializedName("status") val status: String = "PENDING"
)

// ── Telemedicine ──────────────────────────────────────────────────────────────
data class TelemedicineSession(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("doctorId") val doctorId: Long = 0,
    @SerializedName("patientUserId") val patientUserId: Long = 0,
    @SerializedName("doctorName") val doctorName: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("time") val time: String = "",
    @SerializedName("platform") val platform: String = "",
    @SerializedName("meetingLink") val meetingLink: String = "",
    @SerializedName("status") val status: String = "SCHEDULED"
)

// ── Medicine Reminder ─────────────────────────────────────────────────────────
data class MedicineReminder(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("medicineName") val medicineName: String = "",
    @SerializedName("dosage") val dosage: String = "",
    @SerializedName("frequency") val frequency: String = "",
    @SerializedName(value = "reminderTime", alternate = ["nextTime"]) val nextTime: String = "",
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

// ── Health Record ─────────────────────────────────────────────────────────────
data class HealthRecord(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("recordType") val type: String = "",
    @SerializedName("recordData") val title: String = "",
    @SerializedName("recordDate") val date: String = "",
    @SerializedName("doctorName") val doctorName: String? = null,
    @SerializedName("facilityName") val facilityName: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

// ── User Profile ──────────────────────────────────────────────────────────────
data class UserProfile(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("bloodGroup") val bloodGroup: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("role") val role: String = "USER",
    @SerializedName("isApproved") val isApproved: Boolean = true,
    @SerializedName("isVerified") val isVerified: Boolean = true
)

// ── App Settings ──────────────────────────────────────────────────────────────
data class AppSetting(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("settingKey") val settingKey: String = "",
    @SerializedName("settingValue") val settingValue: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("category") val category: String = "GENERAL"
)

// ── Notification ─────────────────────────────────────────────────────────────
data class Notification(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("type") val type: String? = null
)

// ── Review ───────────────────────────────────────────────────────────────────
data class Review(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("userId") val userId: Long = 0,
    @SerializedName("userName") val userName: String = "",
    @SerializedName("entityType") val entityType: String = "",
    @SerializedName("entityId") val entityId: Long = 0,
    @SerializedName("rating") val rating: Int = 0,
    @SerializedName("comment") val comment: String = "",
    @SerializedName("createdAt") val createdAt: String = ""
)

// ── Chat ─────────────────────────────────────────────────────────────────────
data class ChatRoom(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("participantOneId") val p1: Long = 0,
    @SerializedName("participantTwoId") val p2: Long = 0,
    @SerializedName("lastMessage") val lastMessage: String? = null,
    @SerializedName("lastMessageTime") val lastMessageTime: String? = null,
    val otherUserName: String = "User" // UI Helper
)

data class ChatMessage(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("roomId") val roomId: Long = 0,
    @SerializedName("senderId") val senderId: Long = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("timestamp") val timestamp: String = ""
)
