ALTER TABLE software_component ADD COLUMN operating_system varchar(120);
ALTER TABLE software_component ALTER COLUMN package_id TYPE character varying(120);
