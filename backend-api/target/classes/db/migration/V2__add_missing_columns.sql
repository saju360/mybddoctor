-- V2: Add missing columns to match updated entities

-- users: active flag
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- donors: optional contact phone and last donation date
ALTER TABLE donors ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(30) NULL;
ALTER TABLE donors ADD COLUMN IF NOT EXISTS last_donation_date VARCHAR(40) NULL;

-- blood_organizations: phone and address
ALTER TABLE blood_organizations ADD COLUMN IF NOT EXISTS phone VARCHAR(30) NULL;
ALTER TABLE blood_organizations ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL;

-- blood_banks: address
ALTER TABLE blood_banks ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL;

-- blood_requests: upazila, contact_phone, notes
ALTER TABLE blood_requests ADD COLUMN IF NOT EXISTS upazila VARCHAR(80) NULL;
ALTER TABLE blood_requests ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(30) NULL;
ALTER TABLE blood_requests ADD COLUMN IF NOT EXISTS notes VARCHAR(255) NULL;

-- hospitals: phone, address, type
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS phone VARCHAR(30) NULL;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL;
ALTER TABLE hospitals ADD COLUMN IF NOT EXISTS type VARCHAR(80) NULL;

-- clinics: upazila, phone, address
ALTER TABLE clinics ADD COLUMN IF NOT EXISTS upazila VARCHAR(80) NULL;
ALTER TABLE clinics ADD COLUMN IF NOT EXISTS phone VARCHAR(30) NULL;
ALTER TABLE clinics ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL;

-- doctors: qualifications, chamber_schedule, phone, available_for_telemedicine
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS qualifications VARCHAR(255) NULL;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS chamber_schedule VARCHAR(255) NULL;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS phone VARCHAR(30) NULL;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS available_for_telemedicine BOOLEAN NOT NULL DEFAULT FALSE;

-- ambulances: vehicle_number, available
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS vehicle_number VARCHAR(40) NULL;
ALTER TABLE ambulances ADD COLUMN IF NOT EXISTS available BOOLEAN NOT NULL DEFAULT TRUE;

-- approval_requests: reviewed_by_user_id, reviewed_at
ALTER TABLE approval_requests ADD COLUMN IF NOT EXISTS reviewed_by_user_id BIGINT NULL;
ALTER TABLE approval_requests ADD COLUMN IF NOT EXISTS reviewed_at VARCHAR(40) NULL;

-- appointments: time_slot, notes (status already exists as VARCHAR)
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS time_slot VARCHAR(40) NULL;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS notes VARCHAR(255) NULL;

-- emergency_requests: upazila, contact_phone, description
-- Also change status column to match EmergencyStatus enum values
ALTER TABLE emergency_requests ADD COLUMN IF NOT EXISTS upazila VARCHAR(80) NULL;
ALTER TABLE emergency_requests ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(30) NULL;
ALTER TABLE emergency_requests ADD COLUMN IF NOT EXISTS description VARCHAR(255) NULL;

-- pharmacies: upazila, address, active
ALTER TABLE pharmacies ADD COLUMN IF NOT EXISTS upazila VARCHAR(80) NULL;
ALTER TABLE pharmacies ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL;
ALTER TABLE pharmacies ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- diagnostic_centers: upazila, phone, address
ALTER TABLE diagnostic_centers ADD COLUMN IF NOT EXISTS upazila VARCHAR(80) NULL;
ALTER TABLE diagnostic_centers ADD COLUMN IF NOT EXISTS phone VARCHAR(30) NULL;
ALTER TABLE diagnostic_centers ADD COLUMN IF NOT EXISTS address VARCHAR(255) NULL;

-- telemedicine_sessions: meeting_link, notes
ALTER TABLE telemedicine_sessions ADD COLUMN IF NOT EXISTS meeting_link VARCHAR(255) NULL;
ALTER TABLE telemedicine_sessions ADD COLUMN IF NOT EXISTS notes VARCHAR(255) NULL;

-- medicine_reminders: dosage, frequency
ALTER TABLE medicine_reminders ADD COLUMN IF NOT EXISTS dosage VARCHAR(80) NULL;
ALTER TABLE medicine_reminders ADD COLUMN IF NOT EXISTS frequency VARCHAR(40) NULL;

-- health_records: record_date, doctor_name, facility_name
ALTER TABLE health_records ADD COLUMN IF NOT EXISTS record_date VARCHAR(40) NULL;
ALTER TABLE health_records ADD COLUMN IF NOT EXISTS doctor_name VARCHAR(120) NULL;
ALTER TABLE health_records ADD COLUMN IF NOT EXISTS facility_name VARCHAR(160) NULL;
