ALTER TABLE firmware_deviceinventory ADD COLUMN source varchar(120);
ALTER TABLE firmware_deviceinventory ALTER COLUMN component_id TYPE character varying(120);
