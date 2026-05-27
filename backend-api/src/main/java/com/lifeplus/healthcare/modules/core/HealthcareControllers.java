package com.lifeplus.healthcare.modules.core;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// USER CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/users")
class UserController {
    private final UserRepository repo;
    UserController(UserRepository repo) { this.repo = repo; }

    @GetMapping
    List<User> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureAdmin("User list access");
        return repo.findAll();
    }

    @GetMapping("/{id}")
    User getById(@PathVariable Long id,
                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSameUserOrAdmin(id, authUser, "User access");
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/me")
    User me(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PutMapping("/{id}")
    User update(@PathVariable Long id,
                @RequestBody User body,
                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSameUserOrAdmin(id, authUser, "User update");
        return repo.findById(id).map(u -> {
            if (body.getFullName() != null) u.setFullName(body.getFullName());
            if (body.getPreferredLanguage() != null) u.setPreferredLanguage(body.getPreferredLanguage());
            if (body.getBloodGroup() != null) u.setBloodGroup(body.getBloodGroup());
            if (body.getDistrict() != null) u.setDistrict(body.getDistrict());
            return repo.save(u);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSameUserOrAdmin(id, authUser, "User delete");
        repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// DONOR CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/donors")
class DonorController {
    private final DonorRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    DonorController(DonorRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping
    List<Donor> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    Donor getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
    }

    @GetMapping("/search")
    List<Donor> search(@RequestParam(required = false) BloodGroup bloodGroup,
                       @RequestParam(required = false) String district) {
        if (bloodGroup != null) {
            if (district != null && !district.isBlank()) {
                return repo.findByBloodGroupAndDistrictAndAvailableNowTrue(bloodGroup, district);
            }
            return repo.findByBloodGroupAndAvailableNowTrue(bloodGroup);
        }
        if (district != null && !district.isBlank()) {
            return repo.findByDistrict(district);
        }
        return repo.findAll();
    }

    @GetMapping("/my")
    List<Donor> myDonors(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @GetMapping("/{id}/eligibility")
    Map<String, Object> checkEligibility(@PathVariable Long id) {
        Donor donor = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
        boolean eligible = true;
        long daysRemaining = 0;
        if (donor.getLastDonationDate() != null && !donor.getLastDonationDate().isBlank()) {
            try {
                java.time.LocalDate last = java.time.LocalDate.parse(donor.getLastDonationDate());
                java.time.LocalDate next = last.plusDays(120);
                eligible = java.time.LocalDate.now().isAfter(next) || java.time.LocalDate.now().isEqual(next);
                if (!eligible) {
                    daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), next);
                }
            } catch (Exception ignored) {}
        }
        Map<String, Object> res = new HashMap<>();
        res.put("eligible", eligible);
        res.put("daysRemaining", daysRemaining);
        res.put("lastDonationDate", donor.getLastDonationDate());
        return res;
    }

    @PostMapping
    Object create(@RequestBody Donor donor,
                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        donor.setUserId(userId);
        return repo.save(donor);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                 @RequestBody Donor donor,
                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Donor existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Donor update");
        donor.setId(id);
        donor.setUserId(existing.getUserId());
        return repo.save(donor);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Donor existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Donor delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// BLOOD ORGANIZATION CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping({"/api/v1/blood-organizations", "/api/v1/blood-orgs"})
class BloodOrganizationController {
    private final BloodOrganizationRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    BloodOrganizationController(BloodOrganizationRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<BloodOrganization> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    BloodOrganization getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood organization not found"));
    }

    @GetMapping("/search")
    List<BloodOrganization> search(@RequestParam String district) {
        return repo.findByDistrict(district);
    }

    @GetMapping("/my")
    List<BloodOrganization> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @PostMapping
    Object create(@RequestBody BloodOrganization entity,
                             @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setUserId(userId); return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                             @RequestBody BloodOrganization entity,
                             @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodOrganization existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood organization not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Blood organization update");
        entity.setId(id);
        entity.setUserId(existing.getUserId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodOrganization existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood organization not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Blood organization delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// BLOOD BANK CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/blood-banks")
class BloodBankController {
    private final BloodBankRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    BloodBankController(BloodBankRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<BloodBank> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    BloodBank getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood bank not found"));
    }

    @GetMapping("/search")
    List<BloodBank> search(@RequestParam String district) {
        return repo.findByDistrict(district);
    }

    @GetMapping("/my")
    List<BloodBank> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @PostMapping
    Object create(@RequestBody BloodBank entity,
                     @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setUserId(userId); return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                     @RequestBody BloodBank entity,
                     @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodBank existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood bank not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Blood bank update");
        entity.setId(id);
        entity.setUserId(existing.getUserId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodBank existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood bank not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Blood bank delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// BLOOD INVENTORY CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/blood-inventory")
class BloodInventoryController {
    private final BloodInventoryRepository repo;
    private final BloodBankRepository bloodBankRepo;
    BloodInventoryController(BloodInventoryRepository repo, BloodBankRepository bloodBankRepo) {
        this.repo = repo;
        this.bloodBankRepo = bloodBankRepo;
    }

    @GetMapping List<BloodInventory> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    BloodInventory getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory record not found"));
    }

    @GetMapping("/by-bank/{bloodBankId}")
    List<BloodInventory> byBank(@PathVariable Long bloodBankId) {
        return repo.findByBloodBankId(bloodBankId);
    }

    @GetMapping("/available")
    List<BloodInventory> available(@RequestParam BloodGroup bloodGroup) {
        return repo.findByBloodGroupAndUnitsAvailableGreaterThan(bloodGroup, 0);
    }

    @PostMapping
    BloodInventory create(@RequestBody BloodInventory entity,
                          @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodBank bank = bloodBankRepo.findById(entity.getBloodBankId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood bank not found"));
        ControllerGuards.ensureOwnerOrAdmin(bank.getUserId(), authUser, "Blood inventory create");
        if (entity.getUnitsAvailable() == null) entity.setUnitsAvailable(0);
        return repo.save(entity);
    }

    @PutMapping("/{id}")
    BloodInventory update(@PathVariable Long id,
                          @RequestBody BloodInventory entity,
                          @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            BloodBank bank = bloodBankRepo.findById(e.getBloodBankId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood bank not found"));
            ControllerGuards.ensureOwnerOrAdmin(bank.getUserId(), authUser, "Blood inventory update");
            entity.setId(e.getId());
            entity.setBloodBankId(e.getBloodBankId());
            return repo.save(entity);
        })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory record not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodInventory existing = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory record not found"));
        BloodBank bank = bloodBankRepo.findById(existing.getBloodBankId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood bank not found"));
        ControllerGuards.ensureOwnerOrAdmin(bank.getUserId(), authUser, "Blood inventory delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// BLOOD REQUEST CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/blood-requests")
class BloodRequestController {
    private final BloodRequestRepository repo;
    private final DonorRepository donorRepo;
    private final NotificationService notificationService;

    BloodRequestController(BloodRequestRepository repo, DonorRepository donorRepo, NotificationService notificationService) {
        this.repo = repo;
        this.donorRepo = donorRepo;
        this.notificationService = notificationService;
    }

    @GetMapping List<BloodRequest> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    BloodRequest getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood request not found"));
    }

    @GetMapping("/search")
    List<BloodRequest> search(@RequestParam(required = false) BloodGroup bloodGroup,
                              @RequestParam(required = false) String district) {
        if (bloodGroup != null) {
            if (district != null && !district.isBlank()) {
                return repo.findByBloodGroupAndDistrict(bloodGroup, district);
            }
            return repo.findAll().stream()
                    .filter(r -> r.getBloodGroup() == bloodGroup).toList();
        }
        if (district != null && !district.isBlank()) {
            return repo.findByStatus(RequestStatus.OPEN).stream()
                    .filter(r -> district.equalsIgnoreCase(r.getDistrict())).toList();
        }
        return repo.findAll();
    }

    @GetMapping("/my")
    List<BloodRequest> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByRequestedByUserId(userId);
    }

    @GetMapping("/for-donor")
    List<BloodRequest> forDonor(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        List<Donor> donors = donorRepo.findByUserId(userId);
        if (donors.isEmpty()) return List.of();
        return repo.findByDonorId(donors.get(0).getId());
    }

    /** Guest-friendly: no auth required to submit a blood request */
    @PostMapping
    BloodRequest create(@RequestBody BloodRequest entity,
                        @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        // Set userId from JWT if signed in, otherwise leave null (anonymous request)
        String uid = ControllerGuards.resolveUserIdSafe(authUser);
        if (uid != null) {
            try { entity.setRequestedByUserId(Long.parseLong(uid)); } catch (NumberFormatException ignored) {}
        }
        
        if (entity.getDonorId() != null) {
            entity.setStatus(RequestStatus.PENDING);
            // Notify specific donor
            donorRepo.findById(entity.getDonorId()).ifPresent(d -> {
                notificationService.sendToTopic("user_" + d.getUserId(), 
                    "Direct Blood Request", 
                    "You have a new blood donation request from " + (entity.getPatientName() != null ? entity.getPatientName() : "a user"));
            });
        } else {
            entity.setStatus(RequestStatus.OPEN);
            // Notify potential donors in the district
            if (entity.getDistrict() != null) {
                notificationService.sendToTopic("blood_requests_" + entity.getDistrict().toLowerCase().replace(" ", "_"),
                        "New Blood Request",
                        "Blood Group " + entity.getBloodGroup() + " needed in " + entity.getDistrict());
            }
        }
        
        return repo.save(entity);
    }

    @PutMapping("/{id}/status")
    @Transactional
    public BloodRequest updateStatus(@PathVariable Long id,
                                   @RequestParam RequestStatus status,
                                   @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodRequest req = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood request not found"));
        ControllerGuards.ensureOwnerOrAdmin(req.getRequestedByUserId(), authUser, "Blood request status update");
        
        RequestStatus oldStatus = req.getStatus();
        req.setStatus(status);
        BloodRequest saved = repo.save(req);
        
        // Logic for rewards and history update
        if (status == RequestStatus.FULFILLED && oldStatus != RequestStatus.FULFILLED) {
            if (saved.getDonorId() != null) {
                donorRepo.findById(saved.getDonorId()).ifPresent(donor -> {
                    donor.setRewardPoints(donor.getRewardPoints() + 50); // Give 50 points
                    donor.setLastDonationDate(java.time.LocalDate.now().toString());
                    donorRepo.save(donor);
                    
                    notificationService.sendToTopic("user_" + donor.getUserId(), 
                        "Reward Earned!", 
                        "You earned 50 reward points for your donation!");
                });
            }
        }
        
        // Notify requester
        if (saved.getRequestedByUserId() != null) {
            notificationService.sendToTopic("user_" + saved.getRequestedByUserId(), 
                "Blood Request Update", 
                "Your request status is now: " + status);
        }
        
        return saved;
    }

    @PutMapping("/{id}")
    BloodRequest update(@PathVariable Long id,
                        @RequestBody BloodRequest entity,
                        @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getRequestedByUserId(), authUser, "Blood request update");
            entity.setId(e.getId());
            entity.setRequestedByUserId(e.getRequestedByUserId());
            return repo.save(entity);
        })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood request not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        BloodRequest existing = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Blood request not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getRequestedByUserId(), authUser, "Blood request delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// HOSPITAL CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/hospitals")
class HospitalController {
    private final HospitalRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    HospitalController(HospitalRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<Hospital> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    Hospital getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
    }

    @GetMapping("/search")
    List<Hospital> search(@RequestParam String district,
                          @RequestParam(required = false) String upazila) {
        if (upazila != null && !upazila.isBlank()) {
            return repo.findByDistrictAndUpazila(district, upazila);
        }
        return repo.findByDistrict(district);
    }

    @GetMapping("/my")
    List<Hospital> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @PostMapping
    Object create(@RequestBody Hospital entity,
                    @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setUserId(userId); return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                    @RequestBody Hospital entity,
                    @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Hospital existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Hospital update");
        entity.setId(id);
        entity.setUserId(existing.getUserId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Hospital existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Hospital delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// CLINIC CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/clinics")
class ClinicController {
    private final ClinicRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    ClinicController(ClinicRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<Clinic> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    Clinic getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));
    }

    @GetMapping("/search")
    List<Clinic> search(@RequestParam String district) {
        return repo.findByDistrict(district);
    }

    @GetMapping("/my")
    List<Clinic> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @PostMapping
    Object create(@RequestBody Clinic entity,
                  @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setUserId(userId); return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                  @RequestBody Clinic entity,
                  @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Clinic existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Clinic update");
        entity.setId(id);
        entity.setUserId(existing.getUserId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Clinic existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Clinic delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// DOCTOR CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/doctors")
class DoctorController {
    private final DoctorRepository repo;
    private final HospitalRepository hospitalRepo;
    private final ApprovalSubmitter approvalSubmitter;

    DoctorController(DoctorRepository repo, HospitalRepository hospitalRepo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.hospitalRepo = hospitalRepo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<Doctor> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    Doctor getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    @GetMapping("/search")
    List<Doctor> search(@RequestParam(required = false) String specialty,
                        @RequestParam(required = false) Long hospitalId,
                        @RequestParam(required = false) Boolean telemedicine,
                        @RequestParam(required = false) String district) {
        if (Boolean.TRUE.equals(telemedicine)) {
            if (district != null && !district.isBlank()) return repo.findByAvailableForTelemedicineTrueAndDistrict(district);
            return repo.findByAvailableForTelemedicineTrue();
        }
        if (hospitalId != null) return repo.findByHospitalId(hospitalId);
        if (specialty != null && !specialty.isBlank()) {
            if (district != null && !district.isBlank()) return repo.findBySpecialtyContainingIgnoreCaseAndDistrict(specialty, district);
            return repo.findBySpecialtyContainingIgnoreCase(specialty);
        }
        if (district != null && !district.isBlank()) return repo.findByDistrict(district);
        return repo.findAll();
    }

    @GetMapping("/my")
    List<Doctor> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        // Find hospitals owned by this user
        List<Hospital> myHospitals = hospitalRepo.findByUserId(userId);
        List<Long> hospitalIds = myHospitals.stream().map(Hospital::getId).toList();
        return repo.findByHospitalIdIn(hospitalIds);
    }

    @PostMapping
    Object create(@RequestBody Doctor entity,
                    @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Hospital hospital = hospitalRepo.findById(entity.getHospitalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        ControllerGuards.ensureOwnerOrAdmin(hospital.getUserId(), authUser, "Doctor create");
        return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                  @RequestBody Doctor entity,
                  @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Doctor existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Hospital hospital = hospitalRepo.findById(existing.getHospitalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        ControllerGuards.ensureOwnerOrAdmin(hospital.getUserId(), authUser, "Doctor update");
        entity.setId(id);
        entity.setHospitalId(existing.getHospitalId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Doctor existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        Hospital hospital = hospitalRepo.findById(existing.getHospitalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        ControllerGuards.ensureOwnerOrAdmin(hospital.getUserId(), authUser, "Doctor delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// AMBULANCE CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/ambulances")
class AmbulanceController {
    private final AmbulanceRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    AmbulanceController(AmbulanceRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<Ambulance> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    Ambulance getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambulance not found"));
    }

    @GetMapping("/search")
    List<Ambulance> search(@RequestParam String district,
                           @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        if (availableOnly) return repo.findByDistrictAndAvailableTrue(district);
        return repo.findByDistrict(district);
    }

    @GetMapping("/my")
    List<Ambulance> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @PostMapping
    Object create(@RequestBody Ambulance entity,
                     @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setUserId(userId); return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                     @RequestBody Ambulance entity,
                     @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Ambulance existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambulance not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Ambulance update");
        entity.setId(id);
        entity.setUserId(existing.getUserId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                 @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Ambulance existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ambulance not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Ambulance delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// SYSTEM SETTINGS CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/system-settings")
class SystemSettingsController {
    private final AppSettingRepository repo;
    SystemSettingsController(AppSettingRepository repo) { this.repo = repo; }

    @GetMapping
    List<AppSetting> all() { return repo.findAll(); }

    @GetMapping("/{key}")
    AppSetting getByKey(@PathVariable String key) {
        return repo.findBySettingKey(key).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found"));
    }

    @PostMapping
    AppSetting save(@RequestBody AppSetting entity,
                       @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findBySettingKey(entity.getSettingKey())
                .map(existing -> {
                    existing.setSettingValue(entity.getSettingValue());
                    existing.setDescription(entity.getDescription());
                    return repo.save(existing);
                }).orElseGet(() -> repo.save(entity));
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// APPROVAL CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/approvals")
class ApprovalController {
    private final ApprovalRequestRepository repo;
    private final HospitalRepository hospitalRepo;
    private final DoctorRepository doctorRepo;
    private final DonorRepository donorRepo;
    private final AmbulanceRepository ambulanceRepo;
    private final PharmacyRepository pharmacyRepo;
    private final DiagnosticCenterRepository diagnosticRepo;
    private final BloodBankRepository bloodBankRepo;
    private final BloodOrganizationRepository bloodOrgRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    ApprovalController(ApprovalRequestRepository repo, HospitalRepository hospitalRepo,
                       DoctorRepository doctorRepo, DonorRepository donorRepo,
                       AmbulanceRepository ambulanceRepo, PharmacyRepository pharmacyRepo,
                       DiagnosticCenterRepository diagnosticRepo, BloodBankRepository bloodBankRepo,
                       BloodOrganizationRepository bloodOrgRepo, UserRepository userRepo,
                       NotificationService notificationService) {
        this.repo = repo;
        this.hospitalRepo = hospitalRepo;
        this.doctorRepo = doctorRepo;
        this.donorRepo = donorRepo;
        this.ambulanceRepo = ambulanceRepo;
        this.pharmacyRepo = pharmacyRepo;
        this.diagnosticRepo = diagnosticRepo;
        this.bloodBankRepo = bloodBankRepo;
        this.bloodOrgRepo = bloodOrgRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    @GetMapping
    List<ApprovalRequest> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findAll();
    }

    @GetMapping("/{id}")
    ApprovalRequest getById(@PathVariable Long id,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found"));
    }

    @GetMapping("/my")
    List<ApprovalRequest> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByRequesterUserId(userId);
    }

    @GetMapping("/pending")
    List<ApprovalRequest> pending(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findByStatus(ApprovalStatus.PENDING);
    }

    @PostMapping
    ApprovalRequest create(@RequestBody ApprovalRequest entity,
                           @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setRequesterUserId(userId);
        entity.setStatus(ApprovalStatus.PENDING);
        return repo.save(entity);
    }

    @PutMapping("/{id}/approve")
    @Transactional
    public ApprovalRequest approve(@PathVariable Long id,
                                   @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long reviewerId = ControllerGuards.ensureSignedInAndGetId(authUser);
        ApprovalRequest request = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found"));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request already processed");
        }

        try {
            processApproval(request);
            request.setStatus(ApprovalStatus.APPROVED);
            request.setReviewedByUserId(reviewerId);
            request.setReviewedAt(java.time.Instant.now().toString());
            ApprovalRequest saved = repo.save(request);

            // Notify user
            notificationService.sendToTopic("user_" + saved.getRequesterUserId(),
                    "Listing Approved",
                    "Your request for " + saved.getEntityType() + " has been approved.");

            return saved;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to apply changes: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ApprovalRequest reject(@PathVariable Long id,
                                  @RequestBody(required = false) Map<String, String> body,
                                  @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long reviewerId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findById(id).map(e -> {
            e.setStatus(ApprovalStatus.REJECTED);
            e.setReviewedByUserId(reviewerId);
            e.setReviewedAt(java.time.Instant.now().toString());
            if (body != null && body.containsKey("notes")) e.setNotes(body.get("notes"));
            ApprovalRequest saved = repo.save(e);

            // Notify user
            notificationService.sendToTopic("user_" + saved.getRequesterUserId(),
                    "Listing Rejected",
                    "Your request for " + saved.getEntityType() + " was rejected." + (saved.getNotes() != null ? " Reason: " + saved.getNotes() : ""));

            return saved;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found"));
    }

    private void processApproval(ApprovalRequest request) throws Exception {
        String type = request.getEntityType().toLowerCase();
        ApprovalAction action = request.getAction();
        String data = request.getEntityData();

        if (action == ApprovalAction.DELETE) {
            deleteEntity(type, request.getEntityId());
            return;
        }

        if (type.equals("hospital")) {
            Hospital h = objectMapper.readValue(data, Hospital.class);
            if (action == ApprovalAction.EDIT) h.setId(request.getEntityId());
            hospitalRepo.save(h);
        } else if (type.equals("doctor")) {
            Doctor d = objectMapper.readValue(data, Doctor.class);
            if (action == ApprovalAction.EDIT) d.setId(request.getEntityId());
            doctorRepo.save(d);
        } else if (type.equals("blood_donor")) {
            Donor d = objectMapper.readValue(data, Donor.class);
            if (action == ApprovalAction.EDIT) d.setId(request.getEntityId());
            d.setStatus("APPROVED");
            donorRepo.save(d);
        } else if (type.equals("ambulance")) {
            Ambulance a = objectMapper.readValue(data, Ambulance.class);
            if (action == ApprovalAction.EDIT) a.setId(request.getEntityId());
            ambulanceRepo.save(a);
        } else if (type.equals("pharmacy")) {
            Pharmacy p = objectMapper.readValue(data, Pharmacy.class);
            if (action == ApprovalAction.EDIT) p.setId(request.getEntityId());
            pharmacyRepo.save(p);
        } else if (type.equals("diagnostic")) {
            DiagnosticCenter dc = objectMapper.readValue(data, DiagnosticCenter.class);
            if (action == ApprovalAction.EDIT) dc.setId(request.getEntityId());
            diagnosticRepo.save(dc);
        } else if (type.equals("blood_bank")) {
            BloodBank bb = objectMapper.readValue(data, BloodBank.class);
            if (action == ApprovalAction.EDIT) bb.setId(request.getEntityId());
            bloodBankRepo.save(bb);
        } else if (type.equals("blood_org")) {
            BloodOrganization bo = objectMapper.readValue(data, BloodOrganization.class);
            if (action == ApprovalAction.EDIT) bo.setId(request.getEntityId());
            bloodOrgRepo.save(bo);
        } else if (type.equals("role_upgrade")) {
            User user = userRepo.findById(request.getRequesterUserId()).orElseThrow();
            user.setRole(request.getRequestedRole());
            userRepo.save(user);
        }
    }

    private void deleteEntity(String type, Long id) {
        if (type.equals("hospital")) hospitalRepo.deleteById(id);
        else if (type.equals("doctor")) doctorRepo.deleteById(id);
        else if (type.equals("blood_donor")) donorRepo.deleteById(id);
        else if (type.equals("ambulance")) ambulanceRepo.deleteById(id);
        else if (type.equals("pharmacy")) pharmacyRepo.deleteById(id);
        else if (type.equals("diagnostic")) diagnosticRepo.deleteById(id);
        else if (type.equals("blood_bank")) bloodBankRepo.deleteById(id);
        else if (type.equals("blood_org")) bloodOrgRepo.deleteById(id);
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// APPOINTMENT CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/appointments")
class AppointmentController {
    private final AppointmentRepository repo;
    private final HospitalRepository hospitalRepo;
    private final DoctorRepository doctorRepo;
    private final NotificationService notificationService;

    AppointmentController(AppointmentRepository repo, HospitalRepository hospitalRepo, DoctorRepository doctorRepo, NotificationService notificationService) {
        this.repo = repo;
        this.hospitalRepo = hospitalRepo;
        this.doctorRepo = doctorRepo;
        this.notificationService = notificationService;
    }

    @GetMapping
    List<Appointment> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return enrich(repo.findAll());
    }

    @GetMapping("/{id}")
    Appointment getById(@PathVariable Long id,
                        @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return enrich(repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found")));
    }

    @GetMapping("/my")
    List<Appointment> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return enrich(repo.findByUserId(userId));
    }

    @PostMapping
    Appointment create(@RequestBody Appointment entity,
                       @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setStatus("PENDING");
        Appointment saved = repo.save(entity);

        // Notify hospital owner through doctor's linked hospital
        doctorRepo.findById(saved.getDoctorId()).ifPresent(d ->
                hospitalRepo.findById(d.getHospitalId()).ifPresent(h -> {
                    if (h.getUserId() != null) {
                        notificationService.sendToTopic("user_" + h.getUserId(),
                                "New Appointment",
                                "A new appointment has been booked for " + h.getName());
                    }
                })
        );

        return enrich(saved);
    }

    @PutMapping("/{id}")
    Appointment update(@PathVariable Long id,
                       @RequestBody Appointment entity,
                       @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Appointment update");
            String oldStatus = e.getStatus();
            entity.setId(e.getId());
            entity.setUserId(e.getUserId());
            Appointment saved = repo.save(entity);

            // Notify user if status changed
            if (!saved.getStatus().equals(oldStatus)) {
                notificationService.sendToTopic("user_" + saved.getUserId(),
                        "Appointment Update",
                        "Your appointment status is now: " + saved.getStatus());
            }

            return enrich(saved);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> cancel(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Appointment cancel");
            e.setStatus("CANCELLED");
            repo.save(e);
            return ResponseEntity.<Void>noContent().build();
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private List<Appointment> enrich(List<Appointment> appointments) {
        appointments.forEach(this::enrich);
        return appointments;
    }

    private Appointment enrich(Appointment appointment) {
        if (appointment.getDoctorId() != null) {
            doctorRepo.findById(appointment.getDoctorId()).ifPresent(doctor -> {
                appointment.setDoctorName(doctor.getFullName());
                appointment.setSpecialty(doctor.getSpecialty());
            });
        }
        return appointment;
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// EMERGENCY REQUEST CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/emergency-requests")
class EmergencyRequestController {
    private final EmergencyRequestRepository repo;
    private final NotificationService notificationService;

    EmergencyRequestController(EmergencyRequestRepository repo, NotificationService notificationService) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    @GetMapping List<EmergencyRequest> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    EmergencyRequest getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Emergency request not found"));
    }

    @GetMapping("/search")
    List<EmergencyRequest> search(@RequestParam(required = false) String district,
                                  @RequestParam(required = false) EmergencyStatus status) {
        if (district != null && !district.isBlank()) return repo.findByDistrict(district);
        if (status != null) return repo.findByStatus(status);
        return repo.findAll();
    }

    @GetMapping("/my")
    List<EmergencyRequest> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    /** No auth required â€” emergency requests are open access */
    @PostMapping
    EmergencyRequest create(@RequestBody EmergencyRequest entity,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        String uid = ControllerGuards.resolveUserIdSafe(authUser);
        if (uid != null) {
            try { entity.setUserId(Long.parseLong(uid)); } catch (NumberFormatException ignored) {}
        }
        entity.setStatus(EmergencyStatus.OPEN);
        EmergencyRequest saved = repo.save(entity);

        // Notify emergency responders or broad channel
        notificationService.sendToTopic("emergencies_" + saved.getDistrict().toLowerCase().replace(" ", "_"),
                "EMERGENCY ALERT",
                "New emergency request in " + saved.getDistrict() + (saved.getUpazila() != null ? " (" + saved.getUpazila() + ")" : ""));

        return saved;
    }

    @PutMapping("/{id}/status")
    EmergencyRequest updateStatus(@PathVariable Long id,
                                  @RequestParam EmergencyStatus status,
                                  @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Emergency status update");
            e.setStatus(status);
            return repo.save(e);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emergency request not found"));
    }

    @PutMapping("/{id}")
    EmergencyRequest update(@PathVariable Long id,
                            @RequestBody EmergencyRequest entity,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Emergency request update");
            if (entity.getDistrict() != null) e.setDistrict(entity.getDistrict());
            if (entity.getUpazila() != null) e.setUpazila(entity.getUpazila());
            if (entity.getContactPhone() != null) e.setContactPhone(entity.getContactPhone());
            if (entity.getDescription() != null) e.setDescription(entity.getDescription());
            if (entity.getStatus() != null) e.setStatus(entity.getStatus());
            return repo.save(e);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emergency request not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        EmergencyRequest existing = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Emergency request not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Emergency request delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// PHARMACY CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/pharmacies")
class PharmacyController {
    private final PharmacyRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    PharmacyController(PharmacyRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<Pharmacy> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    Pharmacy getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));
    }

    @GetMapping("/search")
    List<Pharmacy> search(@RequestParam(required = false) String district,
                          @RequestParam(required = false, defaultValue = "false") boolean open24h) {
        if (district != null && !district.isBlank() && open24h) return repo.findByDistrictAndOpen24hTrue(district);
        if (open24h) return repo.findByOpen24hTrue();
        if (district != null && !district.isBlank()) return repo.findByDistrict(district);
        return repo.findAll();
    }

    @GetMapping("/my")
    List<Pharmacy> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @PostMapping
    Object create(@RequestBody Pharmacy entity,
                    @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setUserId(userId); return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                    @RequestBody Pharmacy entity,
                    @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Pharmacy existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Pharmacy update");
        entity.setId(id);
        entity.setUserId(existing.getUserId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Pharmacy existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pharmacy not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Pharmacy delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// DIAGNOSTIC CENTER CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/diagnostics")
class DiagnosticController {
    private final DiagnosticCenterRepository repo;
    private final ApprovalSubmitter approvalSubmitter;

    DiagnosticController(DiagnosticCenterRepository repo, ApprovalRequestRepository approvalRepo) {
        this.repo = repo;
        this.approvalSubmitter = new ApprovalSubmitter(approvalRepo);
    }

    @GetMapping List<DiagnosticCenter> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    DiagnosticCenter getById(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnostic center not found"));
    }

    @GetMapping("/search")
    List<DiagnosticCenter> search(@RequestParam(required = false) String district,
                                  @RequestParam(required = false) String test) {
        if (test != null && !test.isBlank()) {
            if (district != null && !district.isBlank()) return repo.findByTestsOfferedContainingIgnoreCaseAndDistrict(test, district);
            return repo.findByTestsOfferedContainingIgnoreCase(test);
        }
        if (district != null && !district.isBlank()) return repo.findByDistrict(district);
        return repo.findAll();
    }

    @GetMapping("/my")
    List<DiagnosticCenter> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @PostMapping
    Object create(@RequestBody DiagnosticCenter entity,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setUserId(userId); return repo.save(entity);
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id,
                            @RequestBody DiagnosticCenter entity,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        DiagnosticCenter existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnostic center not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Diagnostic center update");
        entity.setId(id);
        entity.setUserId(existing.getUserId());
        return repo.save(entity);
    }

    @DeleteMapping("/{id}")
    Object delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        DiagnosticCenter existing = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnostic center not found"));
        ControllerGuards.ensureOwnerOrAdmin(existing.getUserId(), authUser, "Diagnostic center delete");
        repo.deleteById(id);
        return ResponseEntity.<Void>noContent().build();
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// TELEMEDICINE CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/telemedicine")
class TelemedicineController {
    private final TelemedicineSessionRepository repo;
    TelemedicineController(TelemedicineSessionRepository repo) { this.repo = repo; }

    @GetMapping
    List<TelemedicineSession> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findAll();
    }

    @GetMapping("/{id}")
    TelemedicineSession getById(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Telemedicine session not found"));
    }

    @GetMapping("/my")
    List<TelemedicineSession> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByPatientUserId(userId);
    }

    @PostMapping
    TelemedicineSession create(@RequestBody TelemedicineSession entity,
                               @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setPatientUserId(userId);
        entity.setStatus(TelemedicineStatus.SCHEDULED);
        return repo.save(entity);
    }

    @PutMapping("/{id}")
    TelemedicineSession update(@PathVariable Long id,
                               @RequestBody TelemedicineSession entity,
                               @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getPatientUserId(), authUser, "Telemedicine update");
            entity.setId(e.getId());
            entity.setPatientUserId(e.getPatientUserId());
            return repo.save(entity);
        })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telemedicine session not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> cancel(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getPatientUserId(), authUser, "Telemedicine cancel");
            e.setStatus(TelemedicineStatus.CANCELLED);
            repo.save(e);
            return ResponseEntity.<Void>noContent().build();
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telemedicine session not found"));
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// MEDICINE REMINDER CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping({"/api/v1/reminders", "/api/v1/medicine-reminders"})
class ReminderController {
    private final MedicineReminderRepository repo;
    ReminderController(MedicineReminderRepository repo) { this.repo = repo; }

    @GetMapping
    List<MedicineReminder> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findAll();
    }

    @GetMapping("/my")
    List<MedicineReminder> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @GetMapping("/my/active")
    List<MedicineReminder> myActive(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserIdAndActiveTrue(userId);
    }

    @GetMapping("/{id}")
    MedicineReminder getById(@PathVariable Long id,
                             @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
    }

    @PostMapping
    MedicineReminder create(@RequestBody MedicineReminder entity,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        entity.setActive(true);
        return repo.save(entity);
    }

    @PutMapping("/{id}")
    MedicineReminder update(@PathVariable Long id,
                            @RequestBody MedicineReminder entity,
                            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Reminder update");
            entity.setId(e.getId());
            entity.setUserId(e.getUserId());
            return repo.save(entity);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Reminder delete");
            repo.deleteById(id);
            return ResponseEntity.<Void>noContent().build();
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// HEALTH RECORD CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/health-records")
class HealthRecordController {
    private final HealthRecordRepository repo;
    HealthRecordController(HealthRecordRepository repo) { this.repo = repo; }

    @GetMapping
    List<HealthRecord> all(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.findAll();
    }

    @GetMapping("/my")
    List<HealthRecord> my(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserId(userId);
    }

    @GetMapping("/my/type/{recordType}")
    List<HealthRecord> myByType(@PathVariable String recordType,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return repo.findByUserIdAndRecordType(userId, recordType);
    }

    @GetMapping("/{id}")
    HealthRecord getById(@PathVariable Long id,
                         @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        HealthRecord record = repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Health record not found"));
        ControllerGuards.ensureOwnerOrAdmin(record.getUserId(), authUser, "Health record access");
        return record;
    }

    @PostMapping
    HealthRecord create(@RequestBody HealthRecord entity,
                        @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        entity.setUserId(userId);
        return repo.save(entity);
    }

    @PutMapping("/{id}")
    HealthRecord update(@PathVariable Long id,
                        @RequestBody HealthRecord entity,
                        @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Health record update");
            entity.setId(e.getId());
            entity.setUserId(e.getUserId());
            return repo.save(entity);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Health record not found"));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id,
                                @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        return repo.findById(id).map(e -> {
            ControllerGuards.ensureOwnerOrAdmin(e.getUserId(), authUser, "Health record delete");
            repo.deleteById(id);
            return ResponseEntity.<Void>noContent().build();
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Health record not found"));
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// HEALTH TIPS CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/health-tips")
class HealthTipController {
    private final HealthTipRepository repo;
    HealthTipController(HealthTipRepository repo) { this.repo = repo; }

    @GetMapping
    List<HealthTip> list(@RequestParam(required = false) String category) {
        return category == null ? repo.findAll() : repo.findByCategory(category);
    }

    @PostMapping
    HealthTip create(@RequestBody HealthTip entity, @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.save(entity);
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// VACCINATION SCHEDULE CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/vaccinations")
class VaccinationController {
    private final VaccinationScheduleRepository repo;
    VaccinationController(VaccinationScheduleRepository repo) { this.repo = repo; }

    @GetMapping
    List<VaccinationSchedule> list() { return repo.findAll(); }

    @PostMapping
    VaccinationSchedule create(@RequestBody VaccinationSchedule entity, @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureSignedIn(authUser);
        return repo.save(entity);
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// PREMIUM ACCESS CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/premium")
class PremiumAccessController {

    @GetMapping("/check")
    java.util.Map<String, Object> check(
            @RequestParam String feature,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        boolean premiumFeature = !("emergency".equalsIgnoreCase(feature)
                || "blood_request".equalsIgnoreCase(feature));
        // Resolve from JWT SecurityContext OR explicit header â€” both are valid
        String uid = ControllerGuards.resolveUserIdSafe(authUser);
        boolean signedIn = uid != null && !uid.isBlank();
        boolean allowed = !premiumFeature || signedIn;
        return java.util.Map.of(
                "feature", feature,
                "premium", premiumFeature,
                "allowed", allowed,
                "reason", allowed ? "ACCESS_GRANTED" : "SIGNIN_REQUIRED"
        );
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// JOIN REQUEST CONTROLLER
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@RestController
@RequestMapping("/api/v1/join")
class JoinRequestController {
    private final ApprovalRequestRepository approvalRepo;
    JoinRequestController(ApprovalRequestRepository approvalRepo) { this.approvalRepo = approvalRepo; }

    @PostMapping("/{entityType}")
    ApprovalRequest requestJoin(
            @PathVariable String entityType,
            @RequestParam Long entityId,
            @RequestParam String role,
            @RequestBody(required = false) java.util.Map<String, String> payload,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        ApprovalRequest request = new ApprovalRequest();
        request.setEntityType(entityType);
        request.setEntityId(entityId);
        request.setRequesterUserId(userId);
        request.setRequestedRole(role);
        request.setNotes(payload == null ? null : payload.get("notes"));
        request.setStatus(ApprovalStatus.PENDING);
        return approvalRepo.save(request);
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// CONTROLLER GUARDS â€” shared auth helpers
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
class ApprovalSubmitter {
    private final ApprovalRequestRepository approvalRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    ApprovalSubmitter(ApprovalRequestRepository approvalRepo) {
        this.approvalRepo = approvalRepo;
    }

    public <T> Object submit(String entityType, Long entityId, ApprovalAction action, T entityData, Long userId) {
        try {
            ApprovalRequest req = new ApprovalRequest();
            req.setEntityType(entityType);
            req.setEntityId(entityId);
            req.setAction(action);
            req.setRequesterUserId(userId);
            req.setStatus(ApprovalStatus.PENDING);
            if (entityData != null) {
                req.setEntityData(objectMapper.writeValueAsString(entityData));
            }
            approvalRepo.save(req);
            return Map.of("message", "Request submitted for admin approval", "status", "PENDING");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to submit approval request");
        }
    }
}

final class ControllerGuards {
    private ControllerGuards() {}

    public static void ensureSignedIn(String authUser) {
        String uid = resolveUserId(authUser);
        if (uid == null || uid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign-in required for this feature");
        }
    }

    public static Long ensureSignedInAndGetId(String authUser) {
        String userId = resolveUserId(authUser);
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign-in required for this feature");
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user id must be numeric");
        }
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    public static void ensureAdmin(String action) {
        ensureSignedIn(null);
        if (!isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, action + " is allowed only for admin");
        }
    }

    public static void ensureSameUserOrAdmin(Long targetUserId, String authUser, String action) {
        Long actorUserId = ensureSignedInAndGetId(authUser);
        if (!isAdmin() && !actorUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, action + " is allowed only for owner or admin");
        }
    }

    public static void ensureOwnerOrAdmin(Long ownerUserId, String authUser, String action) {
        Long actorUserId = ensureSignedInAndGetId(authUser);
        if (isAdmin()) {
            return;
        }
        if (ownerUserId == null || !ownerUserId.equals(actorUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, action + " is allowed only for owner or admin");
        }
    }

    /** Returns userId string or null â€” never throws. Used for optional-auth endpoints. */
    public static String resolveUserIdSafe(String explicitUserId) {
        try {
            return resolveUserId(explicitUserId);
        } catch (Exception e) {
            return null;
        }
    }

    private static String resolveUserId(String explicitUserId) {
        // Prefer explicit header (set by JwtAuthFilter downstream or direct header)
        if (explicitUserId != null && !explicitUserId.isBlank()) {
            return explicitUserId;
        }
        // Fall back to JWT SecurityContext principal set by JwtAuthFilter
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        String principal = String.valueOf(authentication.getPrincipal());
        // "anonymousUser" is Spring Security's default unauthenticated principal
        if ("anonymousUser".equals(principal)) {
            return null;
        }
        return principal;
    }
}






