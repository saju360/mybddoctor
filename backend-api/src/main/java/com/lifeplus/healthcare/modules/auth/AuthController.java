package com.lifeplus.healthcare.modules.core;

import com.lifeplus.healthcare.modules.auth.JwtService;
import com.lifeplus.healthcare.modules.core.OtpVerification;
import com.lifeplus.healthcare.modules.core.OtpVerificationRepository;
import com.lifeplus.healthcare.modules.core.User;
import com.lifeplus.healthcare.modules.core.UserRepository;
import com.lifeplus.healthcare.modules.core.UserRole;
import com.lifeplus.healthcare.modules.core.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final OtpVerificationRepository otpRepo;
    private final boolean demoOtpEnabled;
    private final String demoOtpMasterCode;

    public AuthController(JwtService jwtService, BCryptPasswordEncoder passwordEncoder,
                          UserRepository userRepo, UserRoleRepository userRoleRepo,
                          OtpVerificationRepository otpRepo,
                          @Value("${app.demo-otp.enabled:true}") boolean demoOtpEnabled,
                          @Value("${app.demo-otp.master-code:123456}") String demoOtpMasterCode) {
        this.jwtService      = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepo        = userRepo;
        this.userRoleRepo    = userRoleRepo;
        this.otpRepo         = otpRepo;
        this.demoOtpEnabled  = demoOtpEnabled;
        this.demoOtpMasterCode = demoOtpMasterCode;
    }

    // â”€â”€ OTP â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/otp/request")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> payload) {
        String phone = payload.getOrDefault("phone", "").trim();
        if (phone.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone is required");

        String otpCode = demoOtpEnabled
                ? demoOtpMasterCode
                : String.format("%06d", new Random().nextInt(1_000_000));
        OtpVerification otp = new OtpVerification();
        otp.setPhone(phone);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otp.setVerified(false);
        otpRepo.save(otp);

        // TODO: send real SMS via SSL Wireless / Twilio
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to " + phone,
                "demoMode", String.valueOf(demoOtpEnabled),
                "demoOtpCode", demoOtpEnabled ? otpCode : ""
        ));
    }

    @PostMapping("/forgot-password/request")
    public ResponseEntity<?> forgotPasswordRequest(@RequestBody Map<String, String> payload) {
        String phone = payload.getOrDefault("phone", "").trim();
        if (phone.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone is required");
        }
        if (userRepo.findByPhone(phone).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for this phone");
        }
        return requestOtp(Map.of("phone", phone));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String phone = payload.getOrDefault("phone", "").trim();
        String code  = payload.getOrDefault("otpCode", "").trim();
        if (phone.isBlank() || code.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone and otpCode are required");

        if (demoOtpEnabled && demoOtpMasterCode.equals(code)) {
            return ResponseEntity.ok(Map.of(
                    "message", "OTP verified (demo mode)",
                    "demoMode", "true"
            ));
        }

        OtpVerification otp = otpRepo.findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active OTP for this phone"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.GONE, "OTP has expired");
        if (!otp.getOtpCode().equals(code))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");

        otp.setVerified(true);
        otpRepo.save(otp);
        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }

    // â”€â”€ Login â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String phone    = payload.getOrDefault("phone", "").trim();
        String password = payload.getOrDefault("password", "");

        if (phone.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone is required");
        if (password.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");

        User user = userRepo.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid phone or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid phone or password");

        if (!user.isActive())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive. Contact admin.");

        // Look up role from user_roles table; default to USER
        String role = userRoleRepo.findByUserId(user.getId())
                .map(UserRole::getRole)
                .orElse("USER");

        String accessToken  = jwtService.createAccessToken(user.getId(), role);
        String refreshToken = jwtService.createRefreshToken(user.getId(), role);

        return ResponseEntity.ok(Map.of(
                "accessToken",  accessToken,
                "refreshToken", refreshToken,
                "tokenType",    "Bearer",
                "userId",       user.getId(),
                "role",         role,
                "fullName",     user.getFullName(),
                "phone",        user.getPhone()
        ));
    }

    // â”€â”€ Register â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String fullName = payload.getOrDefault("fullName", "").trim();
        String phone    = payload.getOrDefault("phone", "").trim();
        String email    = payload.getOrDefault("email", "").trim();
        String password = payload.getOrDefault("password", "");
        String lang     = payload.getOrDefault("preferredLanguage", "en").trim();
        String bloodGroup = payload.getOrDefault("bloodGroup", "").trim();
        String district   = payload.getOrDefault("district", "").trim();

        if (fullName.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fullName is required");
        if (phone.isBlank())    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone is required");
        if (email.isBlank())    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        if (password.length() < 8) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password must be at least 8 characters");

        if (userRepo.existsByPhone(phone))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        if (userRepo.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");

        User user = new User();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPreferredLanguage(lang);
        user.setBloodGroup(bloodGroup);
        user.setDistrict(district);
        user.setActive(true);
        User saved = userRepo.save(user);

        // New users get USER role by default
        UserRole ur = new UserRole();
        ur.setUserId(saved.getId());
        ur.setRole("USER");
        userRoleRepo.save(ur);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "userId",   saved.getId(),
                "fullName", fullName,
                "phone",    phone,
                "email",    email,
                "bloodGroup", bloodGroup,
                "district", district,
                "role",     "USER",
                "message",  "Registration successful"
        ));
    }

    // â”€â”€ Refresh â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> payload) {
        String refresh = payload.getOrDefault("refreshToken", "");
        if (refresh.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refreshToken is required");

        io.jsonwebtoken.Claims claims;
        try { claims = jwtService.parse(refresh); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"); }

        if (!"refresh".equals(claims.get("type", String.class)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not a refresh token");

        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        // Re-check role from DB in case it was changed by admin
        role = userRoleRepo.findByUserId(userId).map(UserRole::getRole).orElse(role != null ? role : "USER");

        return ResponseEntity.ok(Map.of(
                "accessToken", jwtService.createAccessToken(userId, role),
                "tokenType",   "Bearer"
        ));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> forgotPasswordReset(@RequestBody Map<String, String> payload) {
        String phone = payload.getOrDefault("phone", "").trim();
        String code = payload.getOrDefault("otpCode", "").trim();
        String newPassword = payload.getOrDefault("newPassword", "");
        String confirmPassword = payload.getOrDefault("confirmPassword", "");

        if (phone.isBlank() || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phone and otpCode are required");
        }
        if (newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newPassword must be at least 8 characters");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        OtpVerification otp = null;
        if (!(demoOtpEnabled && demoOtpMasterCode.equals(code))) {
            otp = otpRepo.findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(phone)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active OTP for this phone"));
            if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.GONE, "OTP has expired");
            }
            if (!otp.getOtpCode().equals(code)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
            }
        }

        User user = userRepo.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found for this phone"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        if (otp != null) {
            otp.setVerified(true);
            otpRepo.save(otp);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successful",
                "demoMode", String.valueOf(demoOtpEnabled)
        ));
    }
}

