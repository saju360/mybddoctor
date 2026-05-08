INSERT INTO app_settings (setting_key, setting_value, description, category) VALUES
  ('ads_tag_for_child_directed_treatment', 'false', 'COPPA child-directed treatment flag for ad requests', 'ADS'),
  ('ads_tag_for_under_age_of_consent', 'false', 'Under age of consent ad request flag', 'ADS'),
  ('ads_max_ad_content_rating', 'T', 'Maximum ad content rating: G, PG, T, MA', 'ADS')
ON DUPLICATE KEY UPDATE
  setting_value = VALUES(setting_value),
  description = VALUES(description),
  category = VALUES(category);

