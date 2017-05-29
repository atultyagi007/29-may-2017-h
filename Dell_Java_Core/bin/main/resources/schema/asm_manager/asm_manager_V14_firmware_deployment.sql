-- asm_manager schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

alter table deployment add column firmware_repository varchar(100);
alter table deployment add column manage_firmware boolean default false;
alter table device_inventory add column system_id character varying(100);
alter table discovery_result add column system_id character varying(100);
alter table deployment add CONSTRAINT deployment_firmware_repository_fk FOREIGN KEY (firmware_repository) 
      REFERENCES firmware_repository (id) MATCH SIMPLE 
      ON UPDATE CASCADE;
