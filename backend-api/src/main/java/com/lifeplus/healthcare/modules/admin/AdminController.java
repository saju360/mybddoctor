package com.lifeplus.healthcare.modules.core;

import com.lifeplus.healthcare.modules.core.*;
import com.lifeplus.healthcare.modules.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

// Import ControllerGuards from the core package
import static com.lifeplus.healthcare.modules.core.ControllerGuards.ensureSignedIn;
import static com.lifeplus.healthcare.modules.core.ControllerGuards.ensureSignedInAndGetId;
@RestController
@RequestMapping("/api/v1/settings")
class AppSettingController {
    private final AppSettingRepository repo;
    AppSettingController(AppSettingRepository repo) { this.repo = repo; }

    /** Public — Android reads settings on startup */
    @GetMapping
    List<AppSetting> all() { return repo.findAll(); }

    @GetMapping("/{key}")
    AppSetting getByKey(@PathVariable String key) {
        return repo.findBySettingKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found: " + key));
    }

    @GetMapping("/category/{cat}")
    List<AppSetting> byCategory(@PathVariable String cat) { return repo.findByCategory(cat); }

    /** Admin upsert — creates or updates a setting */
    @PostMapping
    ResponseEntity<AppSetting> upsert(@RequestBody Map<String, String> body,
                                      @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        String key   = body.get("settingKey");
        String value = body.get("settingValue");
        String desc  = body.getOrDefault("description", "");
        String cat   = body.getOrDefault("category", "GENERAL");
        if (key == null || key.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "settingKey is required");

        AppSetting s = repo.findBySettingKey(key).orElse(new AppSetting());
        s.setSettingKey(key);
        s.setSettingValue(value != null ? value : "");
        s.setDescription(desc);
        s.setCategory(cat);
        return ResponseEntity.ok(repo.save(s));
    }

    @DeleteMapping("/{key}")
    ResponseEntity<Void> delete(@PathVariable String key,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        AppSetting s = repo.findBySettingKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found"));
        repo.delete(s);
        return ResponseEntity.noContent().build();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DASHBOARD SLIDES CONTROLLER  (Android reads active slides for home screen)
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/slides")
class DashboardSlideController {
    private final DashboardSlideRepository repo;
    DashboardSlideController(DashboardSlideRepository repo) { this.repo = repo; }

    /** Public — Android fetches active slides */
    @GetMapping
    List<DashboardSlide> active() { return repo.findByActiveTrueOrderByDisplayOrderAsc(); }

    /** Admin — all slides including inactive */
    @GetMapping("/all")
    List<DashboardSlide> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return repo.findAllByOrderByDisplayOrderAsc();
    }

    @GetMapping("/{id}")
    DashboardSlide getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Slide not found"));
    }

    @PostMapping
    DashboardSlide create(@RequestBody DashboardSlide slide,
                          @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return repo.save(slide);
    }

    @PutMapping("/{id}")
    DashboardSlide update(@PathVariable Long id, @RequestBody DashboardSlide slide,
                          @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return repo.findById(id).map(s -> { slide.setId(s.getId()); return repo.save(slide); })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slide not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slide not found"));
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Reorder slides */
    @PutMapping("/reorder")
    ResponseEntity<Void> reorder(@RequestBody List<Map<String, Object>> orders,
                                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        for (Map<String, Object> o : orders) {
            long id    = Long.parseLong(o.get("id").toString());
            int  order = Integer.parseInt(o.get("displayOrder").toString());
            repo.findById(id).ifPresent(s -> { s.setDisplayOrder(order); repo.save(s); });
        }
        return ResponseEntity.ok().build();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WALKTHROUGH SLIDES CONTROLLER  (Android shows on first launch)
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/walkthrough")
class WalkthroughSlideController {
    private final WalkthroughSlideRepository repo;
    WalkthroughSlideController(WalkthroughSlideRepository repo) { this.repo = repo; }

    /** Public — Android fetches active walkthrough slides */
    @GetMapping
    List<WalkthroughSlide> active() { return repo.findByActiveTrueOrderByDisplayOrderAsc(); }

    @GetMapping("/all")
    List<WalkthroughSlide> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return repo.findAllByOrderByDisplayOrderAsc();
    }

    @PostMapping
    WalkthroughSlide create(@RequestBody WalkthroughSlide slide,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return repo.save(slide);
    }

    @PutMapping("/{id}")
    WalkthroughSlide update(@PathVariable Long id, @RequestBody WalkthroughSlide slide,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return repo.findById(id).map(s -> { slide.setId(s.getId()); return repo.save(slide); })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slide not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slide not found"));
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADMIN USER MANAGEMENT CONTROLLER
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserController {
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;

    AdminUserController(UserRepository userRepo, BCryptPasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder  = encoder;
        this.jwtService = jwtService;
    }

    /** Admin creates a user with a specific role */
    @PostMapping
    ResponseEntity<?> createUser(@RequestBody Map<String, String> body,
                                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        String fullName = body.getOrDefault("fullName", "").trim();
        String phone    = body.getOrDefault("phone", "").trim();
        String email    = body.getOrDefault("email", "").trim();
        String password = body.getOrDefault("password", "");
        String lang     = body.getOrDefault("preferredLanguage", "en");

        if (fullName.isBlank() || phone.isBlank() || email.isBlank() || password.length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fullName, phone, email, password(8+) required");

        if (userRepo.existsByPhone(phone))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        if (userRepo.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");

        User u = new User();
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setPreferredLanguage(lang);
        u.setActive(true);
        User saved = userRepo.save(u);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "fullName", saved.getFullName(),
                "phone", saved.getPhone(),
                "email", saved.getEmail(),
                "message", "User created successfully"
        ));
    }

    /** Admin bulk delete users */
    @DeleteMapping("/bulk")
    ResponseEntity<?> bulkDelete(@RequestBody Map<String, List<Long>> body,
                                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ids list required");
        userRepo.deleteAllById(ids);
        return ResponseEntity.ok(Map.of("deleted", ids.size()));
    }

    /** Admin toggle user active status */
    @PutMapping("/{id}/toggle-active")
    ResponseEntity<?> toggleActive(@PathVariable Long id,
                                   @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return userRepo.findById(id).map(u -> {
            u.setActive(!u.isActive());
            userRepo.save(u);
            return ResponseEntity.ok(Map.of("id", id, "active", u.isActive()));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /** Admin reset user password */
    @PutMapping("/{id}/reset-password")
    ResponseEntity<?> resetPassword(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        String newPassword = body.getOrDefault("password", "");
        if (newPassword.length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        return userRepo.findById(id).map(u -> {
            u.setPasswordHash(encoder.encode(newPassword));
            userRepo.save(u);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BULK OPERATIONS CONTROLLER  (admin bulk insert/delete for any entity)
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/bulk")
class BulkController {
    private final HospitalRepository hospitalRepo;
    private final DoctorRepository doctorRepo;
    private final DonorRepository donorRepo;
    private final PharmacyRepository pharmacyRepo;
    private final AmbulanceRepository ambulanceRepo;

    BulkController(HospitalRepository hospitalRepo, DoctorRepository doctorRepo,
                   DonorRepository donorRepo, PharmacyRepository pharmacyRepo,
                   AmbulanceRepository ambulanceRepo) {
        this.hospitalRepo = hospitalRepo;
        this.doctorRepo   = doctorRepo;
        this.donorRepo    = donorRepo;
        this.pharmacyRepo = pharmacyRepo;
        this.ambulanceRepo = ambulanceRepo;
    }

    @PostMapping("/hospitals")
    ResponseEntity<?> bulkHospitals(@RequestBody List<Hospital> items,
                                    @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        List<Hospital> saved = hospitalRepo.saveAll(items);
        return ResponseEntity.ok(Map.of("inserted", saved.size()));
    }

    @PostMapping("/doctors")
    ResponseEntity<?> bulkDoctors(@RequestBody List<Doctor> items,
                                  @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        List<Doctor> saved = doctorRepo.saveAll(items);
        return ResponseEntity.ok(Map.of("inserted", saved.size()));
    }

    @PostMapping("/donors")
    ResponseEntity<?> bulkDonors(@RequestBody List<Donor> items,
                                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        List<Donor> saved = donorRepo.saveAll(items);
        return ResponseEntity.ok(Map.of("inserted", saved.size()));
    }

    @PostMapping("/pharmacies")
    ResponseEntity<?> bulkPharmacies(@RequestBody List<Pharmacy> items,
                                     @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        List<Pharmacy> saved = pharmacyRepo.saveAll(items);
        return ResponseEntity.ok(Map.of("inserted", saved.size()));
    }

    @PostMapping("/ambulances")
    ResponseEntity<?> bulkAmbulances(@RequestBody List<Ambulance> items,
                                     @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        List<Ambulance> saved = ambulanceRepo.saveAll(items);
        return ResponseEntity.ok(Map.of("inserted", saved.size()));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ROLE MANAGEMENT CONTROLLER
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/admin/roles")
class RoleManagementController {
    private final UserRoleRepository roleRepo;
    private final UserRepository userRepo;

    RoleManagementController(UserRoleRepository roleRepo, UserRepository userRepo) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
    }

    /** Get all user-role assignments */
    @GetMapping
    List<UserRole> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        return roleRepo.findAll();
    }

    /** Get role for a specific user */
    @GetMapping("/user/{userId}")
    ResponseEntity<?> getRole(@PathVariable Long userId,
                              @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        String role = roleRepo.findByUserId(userId).map(UserRole::getRole).orElse("USER");
        return ResponseEntity.ok(Map.of("userId", userId, "role", role));
    }

    /** Assign or update role for a user */
    @PostMapping("/assign")
    ResponseEntity<?> assignRole(@RequestBody Map<String, String> body,
                                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        String userIdStr = body.get("userId");
        String role      = body.get("role");
        if (userIdStr == null || role == null || role.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId and role are required");

        Long userId = Long.parseLong(userIdStr);
        userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserRole ur = roleRepo.findByUserId(userId).orElse(new UserRole());
        ur.setUserId(userId);
        ur.setRole(role.toUpperCase());
        roleRepo.save(ur);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "role",   role.toUpperCase(),
                "message", "Role assigned successfully"
        ));
    }

    /** Remove role (resets to USER) */
    @DeleteMapping("/user/{userId}")
    ResponseEntity<?> removeRole(@PathVariable Long userId,
                                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ensureSignedIn(authUser);
        roleRepo.findByUserId(userId).ifPresent(ur -> {
            ur.setRole("USER");
            roleRepo.save(ur);
        });
        return ResponseEntity.ok(Map.of("userId", userId, "role", "USER", "message", "Role reset to USER"));
    }
}

