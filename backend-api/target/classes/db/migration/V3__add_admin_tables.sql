-- V3: Admin management tables

CREATE TABLE app_settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  setting_key VARCHAR(120) NOT NULL UNIQUE,
  setting_value TEXT NOT NULL,
  description VARCHAR(255) NULL,
  category VARCHAR(60) NOT NULL DEFAULT 'GENERAL'
);

CREATE TABLE dashboard_slides (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  title VARCHAR(160) NOT NULL,
  subtitle VARCHAR(255) NULL,
  image_url VARCHAR(500) NOT NULL,
  action_url VARCHAR(500) NULL,
  display_order INT NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE walkthrough_slides (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  title VARCHAR(160) NOT NULL,
  subtitle VARCHAR(500) NOT NULL,
  icon_name VARCHAR(80) NOT NULL,
  accent_color VARCHAR(20) NOT NULL DEFAULT '#EF4444',
  display_order INT NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Default app settings
INSERT INTO app_settings (setting_key, setting_value, description, category) VALUES
  ('app_maintenance_mode',    'false',  'Put app in maintenance mode',                    'SYSTEM'),
  ('emergency_sla_minutes',   '5',      'Emergency dispatch SLA in minutes',              'EMERGENCY'),
  ('blood_request_guest',     'true',   'Allow guest blood requests without login',        'BLOOD'),
  ('max_reminders_per_user',  '20',     'Maximum medicine reminders per user',             'REMINDERS'),
  ('telemedicine_enabled',    'true',   'Enable telemedicine feature',                     'FEATURES'),
  ('donor_search_enabled',    'true',   'Enable blood donor search',                       'FEATURES'),
  ('app_version_android',     '1.0',    'Minimum required Android app version',            'SYSTEM'),
  ('force_update_android',    'false',  'Force users to update Android app',               'SYSTEM'),
  ('announcement_text',       '',       'Announcement banner text (empty = hidden)',        'UI'),
  ('announcement_color',      '#F59E0B','Announcement banner background color',            'UI');

-- Default walkthrough slides
INSERT INTO walkthrough_slides (title, subtitle, icon_name, accent_color, display_order) VALUES
  ('LifePlus Healthcare',
   'Bangladesh\'s complete digital health companion — hospitals, doctors, blood, emergency and more in one app.',
   'ic_app_logo', '#EF4444', 1),
  ('Emergency Help Anytime',
   'Submit emergency requests without login. Ambulance dispatch in under 5 minutes. Available 24/7 across all districts.',
   'ic_emergency', '#EF4444', 2),
  ('Blood When It Matters',
   'Find blood donors by group and district instantly. Submit blood requests as a guest. Connect with blood banks.',
   'ic_blooddrop', '#EF4444', 3),
  ('Your Health, Digitized',
   'Book doctor appointments, track medicine reminders, store health records, and access the EPI vaccination schedule.',
   'ic_health_record', '#22C55E', 4);
