-- asm_manager schema version 1 upgrade script

-- add health, compliant, last infrastructure template applied date/name
-- last management template applied date/name
-- last inventory date, last compliance check date, discovered date
ALTER TABLE device_inventory RENAME COLUMN status to state;
ALTER TABLE device_inventory ADD COLUMN health character varying(255);
ALTER TABLE device_inventory ADD COLUMN health_message character varying(255);
ALTER TABLE device_inventory ADD COLUMN compliant character varying(255);
ALTER TABLE device_inventory ADD COLUMN infra_template_date timestamp without time zone;
ALTER TABLE device_inventory ADD COLUMN infra_template_id character varying(255);
ALTER TABLE device_inventory ADD COLUMN server_template_date timestamp without time zone;
ALTER TABLE device_inventory ADD COLUMN server_template_id character varying(255);
ALTER TABLE device_inventory ADD COLUMN inventory_date timestamp without time zone;
ALTER TABLE device_inventory ADD COLUMN compliance_check_date timestamp without time zone;
ALTER TABLE device_inventory ADD COLUMN discovered_date timestamp without time zone;
ALTER TABLE device_inventory ADD COLUMN identity_refId character varying(255);