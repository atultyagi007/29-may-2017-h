-- asm_manager initial schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

CREATE TABLE device_last_job_state
(
 device_ref_id character varying(255) NOT NULL,
 job_type character varying(255) NOT NULL,
 job_state character varying(255),
 description character varying(255) NOT NULL,
 created_date timestamp without time zone,
 updated_date timestamp without time zone,
 CONSTRAINT device_refid_fkey FOREIGN KEY (device_ref_id)
      REFERENCES device_inventory (ref_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
 CONSTRAINT device_last_job_state_ukey UNIQUE (device_ref_id, job_type)
);
CREATE INDEX idx_device_last_job_state_ref_id on device_last_job_state(device_ref_id);
