ALTER TABLE software_bundle ADD COLUMN device_type character varying(255);
ALTER TABLE software_bundle ADD COLUMN device_model character varying(255);
ALTER TABLE software_bundle ADD COLUMN criticality character varying(255);
ALTER TABLE software_bundle ADD COLUMN description text;
ALTER TABLE software_bundle ADD COLUMN userbundle BOOLEAN DEFAULT false;
