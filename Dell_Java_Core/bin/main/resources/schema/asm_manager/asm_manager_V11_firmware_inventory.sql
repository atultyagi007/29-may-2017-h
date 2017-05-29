-- asm_manager schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)
drop table if exists firmware_deviceinventory cascade;
CREATE TABLE firmware_deviceinventory (
      id			character varying(100) PRIMARY KEY not null,
      parent_job_id         character varying(100), -- fk to server_inventory
      job_id                character varying(100), -- fk to server_inventory
      name                   character varying(100),
      device_inventory      character varying(255),
      discovery_result      character varying(100),
      version           character varying(100),
      fqdd              character varying(100),
      ipaddress	        character varying(100),
      servicetag        character varying(100),
      component_id      character varying(20),
      component_type    character varying(20),
      device_id	        character varying(20),
      vendor_id         character varying(20),
      subdevice_id	    character varying(20),
      subvendor_id      character varying(20),
      created_date timestamp with time zone DEFAULT now(),
      created_by character varying(255),
      updated_date timestamp with time zone DEFAULT now(),
      updated_by character varying(255),
      last_update_time	timestamp DEFAULT current_timestamp,
      CONSTRAINT firmware_deviceinventory_device_inventory_fk FOREIGN KEY (device_inventory) 
      REFERENCES device_inventory (ref_id) MATCH SIMPLE 
      ON UPDATE CASCADE ON DELETE CASCADE,
      CONSTRAINT firmware_deviceinventory_discovery_result_fk FOREIGN KEY (discovery_result) 
      REFERENCES discovery_result (id) MATCH SIMPLE 
      ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE INDEX idx_firmware_deviceinventory_id on firmware_deviceinventory(id);