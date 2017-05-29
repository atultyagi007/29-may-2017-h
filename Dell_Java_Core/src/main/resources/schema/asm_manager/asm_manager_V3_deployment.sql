-- asm_manager schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

CREATE TABLE deployment
(
 id character varying(255) NOT NULL,
 name character varying(255),
 deployment_desc character varying(1024), 
 created_date timestamp without time zone DEFAULT now(),
 created_by character varying(255),
 updated_date timestamp without time zone DEFAULT now(), -- not visible in UI
 updated_by character varying(255), -- not visible in UI
 expiration_date timestamp without time zone,
 marshalledTemplateData character varying(1024000),
 job_id character varying(255),
 CONSTRAINT deployment_id_pkey PRIMARY KEY (id),
 CONSTRAINT deployment_name UNIQUE (name)

);
CREATE INDEX idx_deployment_id on deployment(id);

CREATE TABLE deployment_to_device_map
(
  device_id character varying(255) NOT NULL,
  deployment_id character varying(255) NOT NULL,
  CONSTRAINT deployment_map_device_fk FOREIGN KEY (device_id)
          REFERENCES device_inventory (ref_id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT deployment_map_deployment_fk FOREIGN KEY (deployment_id)
          REFERENCES deployment (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE NO ACTION

);

CREATE TABLE deployment_to_vm_map
(
  deployment_id character varying(255),
  vm_id character varying(255) PRIMARY KEY,
  vm_model character varying(255),
  vm_ipaddress character varying(255),
  vm_servicetag character varying(255),
  vm_manufacturer character varying(255),
  CONSTRAINT vm_deployment_map FOREIGN KEY (deployment_id)
            REFERENCES deployment (id) MATCH SIMPLE
            ON UPDATE NO ACTION ON DELETE NO ACTION
);
