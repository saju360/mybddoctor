-- V4: User roles table, OTP verifications, and admin user role assignment

CREATE TABLE IF NOT EXISTS user_roles (
  id         BIGINT      PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  user_id    BIGINT      NOT NULL UNIQUE,
  role       VARCHAR(50) NOT NULL DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS otp_verifications (
  id         BIGINT      PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  phone      VARCHAR(30) NOT NULL,
  otp_code   VARCHAR(10) NOT NULL,
  expires_at DATETIME    NOT NULL,
  verified   BOOLEAN     NOT NULL DEFAULT FALSE,
  INDEX idx_otp_phone (phone)
);

-- Assign ADMIN role to the default admin user (phone: 01700000000)
-- The user must exist first (inserted in lifeplus_healthcare_db.sql or V3)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE phone = '01700000000'
ON DUPLICATE KEY UPDATE role = 'ADMIN';
