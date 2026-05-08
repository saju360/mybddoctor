INSERT INTO app_settings (setting_key, setting_value, description, category) VALUES
  ('ads_enabled', 'true', 'Enable all in-app ads', 'ADS'),
  ('ads_provider_priority', 'admob', 'Primary ads provider: admob or facebook', 'ADS'),
  ('ads_banner_enabled', 'true', 'Enable banner ads', 'ADS'),
  ('ads_interstitial_enabled', 'true', 'Enable interstitial ads', 'ADS'),
  ('ads_rewarded_enabled', 'true', 'Enable rewarded ads', 'ADS'),
  ('ads_interstitial_every_n_clicks', '4', 'Show interstitial after N actions', 'ADS'),
  ('ads_interstitial_cooldown_seconds', '90', 'Minimum seconds between interstitial ads', 'ADS'),
  ('admob_banner_unit_id', 'ca-app-pub-3940256099942544/6300978111', 'AdMob banner ad unit id', 'ADS'),
  ('admob_interstitial_unit_id', 'ca-app-pub-3940256099942544/1033173712', 'AdMob interstitial ad unit id', 'ADS'),
  ('admob_rewarded_unit_id', 'ca-app-pub-3940256099942544/5224354917', 'AdMob rewarded ad unit id', 'ADS'),
  ('fb_banner_placement_id', 'IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID', 'Facebook banner placement id', 'ADS'),
  ('fb_interstitial_placement_id', 'IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID', 'Facebook interstitial placement id', 'ADS')
ON DUPLICATE KEY UPDATE
  setting_value = VALUES(setting_value),
  description = VALUES(description),
  category = VALUES(category);

