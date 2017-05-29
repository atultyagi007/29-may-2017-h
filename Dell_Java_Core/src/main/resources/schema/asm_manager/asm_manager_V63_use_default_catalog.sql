ALTER TABLE service_template ADD COLUMN use_default_catalog BOOLEAN DEFAULT FALSE;
ALTER TABLE deployment ADD COLUMN use_default_catalog BOOLEAN DEFAULT FALSE;

