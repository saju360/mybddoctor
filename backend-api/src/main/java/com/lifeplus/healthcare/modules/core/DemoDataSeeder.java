package com.lifeplus.healthcare.modules.core;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final RoleRepository roleRepo;
    private final DonorRepository donorRepo;
    private final BloodOrganizationRepository bloodOrgRepo;
    private final BloodBankRepository bloodBankRepo;
    private final BloodInventoryRepository bloodInventoryRepo;
    private final BloodRequestRepository bloodRequestRepo;
    private final ApprovalRequestRepository approvalRepo;
    private final HospitalRepository hospitalRepo;
    private final ClinicRepository clinicRepo;
    private final DoctorRepository doctorRepo;
    private final AmbulanceRepository ambulanceRepo;
    private final AppointmentRepository appointmentRepo;
    private final EmergencyRequestRepository emergencyRepo;
    private final PharmacyRepository pharmacyRepo;
    private final DiagnosticCenterRepository diagnosticRepo;
    private final TelemedicineSessionRepository telemedicineRepo;
    private final MedicineReminderRepository reminderRepo;
    private final HealthRecordRepository healthRecordRepo;
    private final NotificationRepository notificationRepo;
    private final ChatRoomRepository chatRoomRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final ReviewRepository reviewRepo;
    private final AppSettingRepository appSettingRepo;
    private final DashboardSlideRepository dashboardSlideRepo;
    private final WalkthroughSlideRepository walkthroughSlideRepo;
    private final HealthTipRepository healthTipRepo;
    private final VaccinationScheduleRepository vaccinationRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    DemoDataSeeder(
            UserRepository userRepo,
            UserRoleRepository userRoleRepo,
            RoleRepository roleRepo,
            DonorRepository donorRepo,
            BloodOrganizationRepository bloodOrgRepo,
            BloodBankRepository bloodBankRepo,
            BloodInventoryRepository bloodInventoryRepo,
            BloodRequestRepository bloodRequestRepo,
            ApprovalRequestRepository approvalRepo,
            HospitalRepository hospitalRepo,
            ClinicRepository clinicRepo,
            DoctorRepository doctorRepo,
            AmbulanceRepository ambulanceRepo,
            AppointmentRepository appointmentRepo,
            EmergencyRequestRepository emergencyRepo,
            PharmacyRepository pharmacyRepo,
            DiagnosticCenterRepository diagnosticRepo,
            TelemedicineSessionRepository telemedicineRepo,
            MedicineReminderRepository reminderRepo,
            HealthRecordRepository healthRecordRepo,
            NotificationRepository notificationRepo,
            ChatRoomRepository chatRoomRepo,
            ChatMessageRepository chatMessageRepo,
            ReviewRepository reviewRepo,
            AppSettingRepository appSettingRepo,
            DashboardSlideRepository dashboardSlideRepo,
            WalkthroughSlideRepository walkthroughSlideRepo,
            HealthTipRepository healthTipRepo,
            VaccinationScheduleRepository vaccinationRepo,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
        this.roleRepo = roleRepo;
        this.donorRepo = donorRepo;
        this.bloodOrgRepo = bloodOrgRepo;
        this.bloodBankRepo = bloodBankRepo;
        this.bloodInventoryRepo = bloodInventoryRepo;
        this.bloodRequestRepo = bloodRequestRepo;
        this.approvalRepo = approvalRepo;
        this.hospitalRepo = hospitalRepo;
        this.clinicRepo = clinicRepo;
        this.doctorRepo = doctorRepo;
        this.ambulanceRepo = ambulanceRepo;
        this.appointmentRepo = appointmentRepo;
        this.emergencyRepo = emergencyRepo;
        this.pharmacyRepo = pharmacyRepo;
        this.diagnosticRepo = diagnosticRepo;
        this.telemedicineRepo = telemedicineRepo;
        this.reminderRepo = reminderRepo;
        this.healthRecordRepo = healthRecordRepo;
        this.notificationRepo = notificationRepo;
        this.chatRoomRepo = chatRoomRepo;
        this.chatMessageRepo = chatMessageRepo;
        this.reviewRepo = reviewRepo;
        this.appSettingRepo = appSettingRepo;
        this.dashboardSlideRepo = dashboardSlideRepo;
        this.walkthroughSlideRepo = walkthroughSlideRepo;
        this.healthTipRepo = healthTipRepo;
        this.vaccinationRepo = vaccinationRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        upsertRoles();

        User admin = upsertUser("System Admin", "01700000001", "admin@lifeplus.local", "Admin@12345", "ADMIN");
        User user = upsertUser("Demo User", "01700000002", "user@lifeplus.local", "User@12345", "USER");
        User hospitalAdmin = upsertUser("Hospital Admin", "01700000003", "hospital@lifeplus.local", "Hospital@12345", "HOSPITAL_ADMIN");
        User bloodOrgAdmin = upsertUser("Blood Org Admin", "01700000004", "bloodorg@lifeplus.local", "BloodOrg@12345", "BLOOD_ORG_ADMIN");
        User ambulanceProvider = upsertUser("Ambulance Provider", "01700000005", "ambulance@lifeplus.local", "Ambulance@12345", "AMBULANCE_PROVIDER");

        seedFacilities(hospitalAdmin, bloodOrgAdmin, ambulanceProvider);
        seedClinicalData(user);
        seedEngagementData(admin, user);
        seedAppContent();

        log.info("LifePlus demo data ensured. Roles ready: ADMIN/USER/HOSPITAL_ADMIN/BLOOD_ORG_ADMIN/AMBULANCE_PROVIDER");
    }

    private void upsertRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (roleRepo.findByName(roleType).isEmpty()) {
                Role role = new Role();
                role.setName(roleType);
                roleRepo.save(role);
            }
        }
    }

    private User upsertUser(String fullName, String phone, String email, String rawPassword, String role) {
        User user = userRepo.findByPhone(phone).orElseGet(User::new);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPreferredLanguage("en");
        user.setBloodGroup("A_POS");
        user.setDistrict("Dhaka");
        user.setActive(true);
        user.setRole(role);
        User savedUser = userRepo.save(user);

        UserRole userRole = userRoleRepo.findByUserId(savedUser.getId()).orElseGet(UserRole::new);
        userRole.setUserId(savedUser.getId());
        userRole.setRole(role);
        userRoleRepo.save(userRole);

        return savedUser;
    }

    private void seedFacilities(User hospitalAdmin, User bloodOrgAdmin, User ambulanceProvider) {
        Hospital hospital;
        if (hospitalRepo.count() == 0) {
            Hospital h = new Hospital();
            h.setUserId(hospitalAdmin.getId());
            h.setName("LifePlus General Hospital");
            h.setDistrict("Dhaka");
            h.setUpazila("Dhanmondi");
            h.setPhone("01810000001");
            h.setAddress("Road 12, Dhanmondi, Dhaka");
            h.setType("Private");
            h.setIcuAvailable(true);
            h.setOpen24h(true);
            hospital = hospitalRepo.save(h);
        } else {
            hospital = hospitalRepo.findAll().get(0);
        }

        if (clinicRepo.count() == 0) {
            Clinic clinic = new Clinic();
            clinic.setUserId(hospitalAdmin.getId());
            clinic.setName("LifePlus City Clinic");
            clinic.setDistrict("Dhaka");
            clinic.setUpazila("Mirpur");
            clinic.setPhone("01810000002");
            clinic.setAddress("Section 10, Mirpur, Dhaka");
            clinic.setSpecialties("Medicine, Pediatrics");
            clinicRepo.save(clinic);
        }

        if (doctorRepo.count() == 0) {
            Doctor doctor = new Doctor();
            doctor.setFullName("Dr. Farhana Rahman");
            doctor.setSpecialty("Cardiology");
            doctor.setHospitalId(hospital.getId());
            doctor.setDistrict("Dhaka");
            doctor.setQualifications("MBBS, MD (Cardiology)");
            doctor.setChamberSchedule("Sat-Thu 5:00 PM - 9:00 PM");
            doctor.setPhone("01910000001");
            doctor.setAvailableForTelemedicine(true);
            doctor.setAvailable(true);
            doctorRepo.save(doctor);
        }

        if (ambulanceRepo.count() == 0) {
            Ambulance ambulance = new Ambulance();
            ambulance.setUserId(ambulanceProvider.getId());
            ambulance.setProviderName("LifePlus Ambulance Service");
            ambulance.setDistrict("Dhaka");
            ambulance.setUpazila("Tejgaon");
            ambulance.setPhone("01711000000");
            ambulance.setVehicleNumber("DHAKA-METRO-CHA-1234");
            ambulance.setIcuEquipped(true);
            ambulance.setAvailable(true);
            ambulanceRepo.save(ambulance);
        }

        if (pharmacyRepo.count() == 0) {
            Pharmacy pharmacy = new Pharmacy();
            pharmacy.setUserId(hospitalAdmin.getId());
            pharmacy.setName("LifePlus Pharmacy");
            pharmacy.setDistrict("Dhaka");
            pharmacy.setUpazila("Dhanmondi");
            pharmacy.setPhone("01610000001");
            pharmacy.setAddress("House 5, Road 4, Dhanmondi");
            pharmacy.setOpen24h(true);
            pharmacy.setActive(true);
            pharmacyRepo.save(pharmacy);
        }

        if (diagnosticRepo.count() == 0) {
            DiagnosticCenter dc = new DiagnosticCenter();
            dc.setUserId(hospitalAdmin.getId());
            dc.setName("LifePlus Diagnostic Center");
            dc.setDistrict("Dhaka");
            dc.setUpazila("Uttara");
            dc.setTestsOffered("CBC, ECG, X-Ray, CT Scan, Blood Sugar");
            dc.setPhone("01510000001");
            dc.setAddress("Sector 7, Uttara, Dhaka");
            diagnosticRepo.save(dc);
        }

        BloodOrganization bloodOrg;
        if (bloodOrgRepo.count() == 0) {
            BloodOrganization org = new BloodOrganization();
            org.setUserId(bloodOrgAdmin.getId());
            org.setName("LifePlus Blood Network");
            org.setDistrict("Dhaka");
            org.setUpazila("Mohammadpur");
            org.setPhone("01712000000");
            org.setAddress("Ring Road, Mohammadpur");
            bloodOrg = bloodOrgRepo.save(org);
        } else {
            bloodOrg = bloodOrgRepo.findAll().get(0);
        }

        BloodBank bloodBank;
        if (bloodBankRepo.count() == 0) {
            BloodBank bank = new BloodBank();
            bank.setUserId(bloodOrgAdmin.getId());
            bank.setName("LifePlus Central Blood Bank");
            bank.setDistrict("Dhaka");
            bank.setUpazila("Shahbag");
            bank.setPhone("01713000000");
            bank.setAddress("Shahbag Main Road");
            bloodBank = bloodBankRepo.save(bank);
        } else {
            bloodBank = bloodBankRepo.findAll().get(0);
        }

        if (bloodInventoryRepo.count() == 0) {
            seedBloodInventory(bloodBank.getId());
        }

        if (approvalRepo.count() == 0) {
            ApprovalRequest request = new ApprovalRequest();
            request.setEntityType("blood_org");
            request.setEntityId(bloodOrg.getId());
            request.setRequesterUserId(bloodOrgAdmin.getId());
            request.setRequestedRole("BLOOD_ORG_ADMIN");
            request.setAction(ApprovalAction.ADD);
            request.setEntityData("{\"name\":\"LifePlus Blood Network\",\"district\":\"Dhaka\"}");
            request.setStatus(ApprovalStatus.PENDING);
            request.setNotes("Initial sample approval request");
            approvalRepo.save(request);
        }
    }

    private void seedBloodInventory(Long bloodBankId) {
        List<BloodGroup> groups = List.of(BloodGroup.A_POS, BloodGroup.B_POS, BloodGroup.O_POS, BloodGroup.AB_POS, BloodGroup.O_NEG);
        int units = 6;
        for (BloodGroup group : groups) {
            BloodInventory inventory = new BloodInventory();
            inventory.setBloodBankId(bloodBankId);
            inventory.setBloodGroup(group);
            inventory.setUnitsAvailable(units);
            bloodInventoryRepo.save(inventory);
            units += 2;
        }
    }

    private void seedClinicalData(User user) {
        Doctor doctor = doctorRepo.findAll().isEmpty() ? null : doctorRepo.findAll().get(0);
        Donor donor;
        List<Donor> myDonors = donorRepo.findByUserId(user.getId());
        if (myDonors.isEmpty()) {
            Donor d = new Donor();
            d.setUserId(user.getId());
            d.setBloodGroup(BloodGroup.A_POS);
            d.setDistrict("Dhaka");
            d.setUpazila("Dhanmondi");
            d.setAvailableNow(true);
            d.setContactPhone(user.getPhone());
            d.setLastDonationDate("2026-03-10");
            d.setRewardPoints(120);
            d.setPhysicalHistory("No chronic disease. Fit for donation.");
            donor = donorRepo.save(d);
        } else {
            donor = myDonors.get(0);
        }

        if (bloodRequestRepo.findByRequestedByUserId(user.getId()).isEmpty()) {
            BloodRequest request = new BloodRequest();
            request.setRequestedByUserId(user.getId());
            request.setDonorId(donor.getId());
            request.setPatientName("Sample Patient");
            request.setHospitalName("LifePlus General Hospital");
            request.setBloodGroup(BloodGroup.A_POS);
            request.setDistrict("Dhaka");
            request.setUpazila("Dhanmondi");
            request.setContactPhone("01714000000");
            request.setUrgency("HIGH");
            request.setStatus(RequestStatus.PENDING);
            request.setNotes("Need 2 bags within today");
            bloodRequestRepo.save(request);
        }

        if (emergencyRepo.findByUserId(user.getId()).isEmpty()) {
            EmergencyRequest emergency = new EmergencyRequest();
            emergency.setUserId(user.getId());
            emergency.setCallerName("Demo User");
            emergency.setPhone(user.getPhone());
            emergency.setDistrict("Dhaka");
            emergency.setUpazila("Farmgate");
            emergency.setContactPhone("01715000000");
            emergency.setEmergencyType("Accident");
            emergency.setDescription("Road accident emergency support needed");
            emergency.setStatus(EmergencyStatus.OPEN);
            emergencyRepo.save(emergency);
        }

        if (doctor != null && appointmentRepo.findByUserId(user.getId()).isEmpty()) {
            Appointment appointment = new Appointment();
            appointment.setUserId(user.getId());
            appointment.setDoctorId(doctor.getId());
            appointment.setAppointmentDate("2026-05-15");
            appointment.setTimeSlot("10:30 AM");
            appointment.setStatus("PENDING");
            appointment.setNotes("Routine checkup");
            appointmentRepo.save(appointment);
        }

        if (doctor != null && telemedicineRepo.findByPatientUserId(user.getId()).isEmpty()) {
            TelemedicineSession session = new TelemedicineSession();
            session.setPatientUserId(user.getId());
            session.setDoctorId(doctor.getId());
            session.setDoctorName(doctor.getFullName());
            session.setScheduledAt("2026-05-20 20:00");
            session.setDate("2026-05-20");
            session.setTime("20:00");
            session.setPlatform("Google Meet");
            session.setMeetingLink("https://meet.google.com/demo-lifeplus-telemed");
            session.setNotes("Discuss follow-up medication");
            session.setStatus(TelemedicineStatus.SCHEDULED);
            telemedicineRepo.save(session);
        }

        if (reminderRepo.findByUserId(user.getId()).isEmpty()) {
            MedicineReminder reminder = new MedicineReminder();
            reminder.setUserId(user.getId());
            reminder.setMedicineName("Paracetamol 500mg");
            reminder.setReminderTime("08:00");
            reminder.setDosage("1 tablet");
            reminder.setFrequency("DAILY");
            reminder.setActive(true);
            reminderRepo.save(reminder);
        }

        if (healthRecordRepo.findByUserId(user.getId()).isEmpty()) {
            HealthRecord record = new HealthRecord();
            record.setUserId(user.getId());
            record.setRecordType("Lab");
            record.setRecordData("Hemoglobin: 13.2 g/dL, Blood Sugar (Fasting): 5.3 mmol/L");
            record.setRecordDate("2026-04-28");
            record.setDoctorName("Dr. Farhana Rahman");
            record.setFacilityName("LifePlus Diagnostic Center");
            record.setImageUrl("");
            healthRecordRepo.save(record);
        }
    }

    private void seedEngagementData(User admin, User user) {
        if (notificationRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty()) {
            Notification n1 = new Notification();
            n1.setUserId(user.getId());
            n1.setTitle("Welcome to LifePlus");
            n1.setMessage("Your account is ready. Explore hospitals, blood donors, and emergency support.");
            n1.setRead(false);
            n1.setType("SYSTEM");
            notificationRepo.save(n1);

            Notification n2 = new Notification();
            n2.setUserId(user.getId());
            n2.setTitle("Medicine Reminder Active");
            n2.setMessage("Your reminder for Paracetamol 500mg is set at 08:00.");
            n2.setRead(false);
            n2.setType("REMINDER");
            notificationRepo.save(n2);
        }

        if (chatRoomRepo.count() == 0) {
            ChatRoom room = new ChatRoom();
            room.setParticipantOneId(user.getId());
            room.setParticipantTwoId(admin.getId());
            room.setLastMessage("Thanks! We are reviewing your request.");
            room.setLastMessageTime(LocalDateTime.now());
            ChatRoom savedRoom = chatRoomRepo.save(room);

            ChatMessage msg1 = new ChatMessage();
            msg1.setRoomId(savedRoom.getId());
            msg1.setSenderId(user.getId());
            msg1.setMessage("Hi, I need help with booking appointment.");
            msg1.setTimestamp(LocalDateTime.now().minusMinutes(5));
            msg1.setRead(true);
            chatMessageRepo.save(msg1);

            ChatMessage msg2 = new ChatMessage();
            msg2.setRoomId(savedRoom.getId());
            msg2.setSenderId(admin.getId());
            msg2.setMessage("Thanks! We are reviewing your request.");
            msg2.setTimestamp(LocalDateTime.now().minusMinutes(2));
            msg2.setRead(false);
            chatMessageRepo.save(msg2);
        }

        if (reviewRepo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("hospital",
                hospitalRepo.findAll().isEmpty() ? 1L : hospitalRepo.findAll().get(0).getId()).isEmpty()) {
            Long hospitalId = hospitalRepo.findAll().isEmpty() ? 1L : hospitalRepo.findAll().get(0).getId();
            Review review = new Review();
            review.setUserId(user.getId());
            review.setUserName(user.getFullName());
            review.setEntityType("hospital");
            review.setEntityId(hospitalId);
            review.setRating(5);
            review.setComment("Great service and responsive doctors.");
            reviewRepo.save(review);
        }
    }

    private void seedAppContent() {
        upsertSetting("ads_enabled", "true", "Master ads switch", "ADS");
        upsertSetting("ads_provider_priority", "admob", "Primary ad provider", "ADS");
        upsertSetting("ads_banner_enabled", "true", "Enable banner ads", "ADS");
        upsertSetting("ads_interstitial_enabled", "true", "Enable interstitial ads", "ADS");
        upsertSetting("ads_rewarded_enabled", "true", "Enable rewarded ads", "ADS");
        upsertSetting("ads_rewarded_points", "5", "Reward points per completed rewarded ad", "ADS");
        upsertSetting("ads_rewarded_unit", "Support Points", "Reward unit label shown to user", "ADS");
        upsertSetting("ads_interstitial_every_n_clicks", "4", "Interstitial frequency in meaningful actions", "ADS");
        upsertSetting("ads_interstitial_cooldown_seconds", "90", "Minimum seconds between interstitial ads", "ADS");
        upsertSetting("ads_tag_for_child_directed_treatment", "false", "COPPA tag", "ADS");
        upsertSetting("ads_tag_for_under_age_of_consent", "false", "Under age of consent tag", "ADS");
        upsertSetting("ads_max_ad_content_rating", "T", "Max ad content rating", "ADS");
        upsertSetting("admob_banner_unit_id", "ca-app-pub-3940256099942544/6300978111", "Google test banner unit id", "ADS");
        upsertSetting("admob_interstitial_unit_id", "ca-app-pub-3940256099942544/1033173712", "Google test interstitial unit id", "ADS");
        upsertSetting("admob_rewarded_unit_id", "ca-app-pub-3940256099942544/5224354917", "Google test rewarded unit id", "ADS");
        upsertSetting("admob_app_open_unit_id", "ca-app-pub-3940256099942544/9257391923", "Google test app-open unit id", "ADS");
        upsertSetting("fb_banner_placement_id", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", "Meta test banner placement", "ADS");
        upsertSetting("fb_interstitial_placement_id", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", "Meta test interstitial placement", "ADS");
        // Legacy keys retained for backward compatibility.
        upsertSetting("admob_banner_android", "ca-app-pub-3940256099942544/6300978111", "Legacy Google test banner unit id", "ADS");
        upsertSetting("admob_interstitial_android", "ca-app-pub-3940256099942544/1033173712", "Legacy Google test interstitial unit id", "ADS");
        upsertSetting("admob_rewarded_android", "ca-app-pub-3940256099942544/5224354917", "Legacy Google test rewarded unit id", "ADS");
        upsertSetting("facebook_banner_android", "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", "Legacy Meta test banner placement", "ADS");
        upsertSetting("notifications_enabled", "true", "Enable push and in-app notifications", "FEATURES");
        upsertSetting("otp_demo_mode", "true", "Development OTP bypass switch", "AUTH");
        upsertSetting("otp_demo_code", "123456", "Development OTP fixed code", "AUTH");
        upsertSetting("supported_languages", "en,bn", "Supported app languages", "GENERAL");

        if (dashboardSlideRepo.count() == 0) {
            DashboardSlide s1 = new DashboardSlide();
            s1.setTitle("Emergency Help 24/7");
            s1.setSubtitle("Quick ambulance and hospital support nearby");
            s1.setImageUrl("https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=1200");
            s1.setActionUrl("/emergency");
            s1.setDisplayOrder(1);
            s1.setActive(true);
            dashboardSlideRepo.save(s1);

            DashboardSlide s2 = new DashboardSlide();
            s2.setTitle("Donate Blood, Save Life");
            s2.setSubtitle("Find verified blood donors and organizations");
            s2.setImageUrl("https://images.unsplash.com/photo-1615461066841-6116e61058f4?w=1200");
            s2.setActionUrl("/blood");
            s2.setDisplayOrder(2);
            s2.setActive(true);
            dashboardSlideRepo.save(s2);
        }

        if (walkthroughSlideRepo.count() == 0) {
            WalkthroughSlide w1 = new WalkthroughSlide();
            w1.setTitle("Find Care Fast");
            w1.setSubtitle("Search doctors, hospitals, and diagnostics instantly.");
            w1.setIconName("local_hospital");
            w1.setAccentColor("#0EA5E9");
            w1.setDisplayOrder(1);
            w1.setActive(true);
            walkthroughSlideRepo.save(w1);

            WalkthroughSlide w2 = new WalkthroughSlide();
            w2.setTitle("Emergency Response");
            w2.setSubtitle("Request ambulance and urgent support in one tap.");
            w2.setIconName("emergency");
            w2.setAccentColor("#EF4444");
            w2.setDisplayOrder(2);
            w2.setActive(true);
            walkthroughSlideRepo.save(w2);
        }

        if (healthTipRepo.count() == 0) {
            HealthTip tip = new HealthTip();
            tip.setTitle("Stay Hydrated in Summer");
            tip.setContent("Drink at least 8 glasses of water daily and avoid sugary drinks to stay hydrated.");
            tip.setCategory("General");
            healthTipRepo.save(tip);
        }

        if (vaccinationRepo.count() == 0) {
            VaccinationSchedule schedule = new VaccinationSchedule();
            schedule.setVaccineName("Hepatitis B");
            schedule.setAgeGroup("Birth");
            schedule.setNotes("First dose at birth; follow national schedule for subsequent doses.");
            vaccinationRepo.save(schedule);
        }
    }

    private void upsertSetting(String key, String value, String description, String category) {
        AppSetting setting = appSettingRepo.findBySettingKey(key).orElseGet(AppSetting::new);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        setting.setCategory(category);
        appSettingRepo.save(setting);
    }
}
