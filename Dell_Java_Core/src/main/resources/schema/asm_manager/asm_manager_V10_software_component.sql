-- asm_manager schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)
drop table if exists software_bundle, software_component cascade;
CREATE TABLE software_bundle (
    id                   character varying(100) PRIMARY KEY not null,
    name                 text,
    version              character varying(100),
    bundle_date          timestamp with time zone,
    created_date         timestamp with time zone DEFAULT now(),
 	created_by character varying(255),
 	updated_date timestamp with time zone DEFAULT now(),
 	updated_by character varying(255),
	firmware_repository character varying(100)  NOT NULL,
    CONSTRAINT software_bundle_firmware_repository_fk FOREIGN KEY (firmware_repository) 
	REFERENCES firmware_repository (id) MATCH SIMPLE 
    ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE INDEX idx_software_bundle_id on software_bundle(id);

CREATE TABLE software_component
(
	id character varying(100) NOT NULL PRIMARY KEY,
	name text,
	category character varying(255),
	component_type character varying(255),
	path character varying(255),
	firmware_repository character varying(100)  NOT NULL,
	dell_version character varying(255),
	vendor_version character varying(255),
	component_id character varying(255),
	device_id character varying(255), 
	sub_device_id character varying(255),
	vendor_id character varying(255),
	sub_vendor_id character varying(255),	
	created_date timestamp with time zone DEFAULT now(),
 	created_by character varying(255),
 	updated_date timestamp with time zone DEFAULT now(),
 	updated_by character varying(255),
 	CONSTRAINT software_component_firmware_repository_FK FOREIGN KEY (firmware_repository) 
	REFERENCES firmware_repository (id) MATCH SIMPLE 
    ON UPDATE CASCADE ON DELETE CASCADE,
    software_bundle character varying(100),
    CONSTRAINT software_component_software_bundle_fk FOREIGN KEY (software_bundle) 
	REFERENCES software_bundle (id) MATCH SIMPLE 
    ON UPDATE CASCADE
);
CREATE INDEX idx_software_component_id on software_component(id);