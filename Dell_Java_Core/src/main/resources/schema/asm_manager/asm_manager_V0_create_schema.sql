-- asm_manager initial schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

CREATE TABLE device_inventory
(
 OPTLOCK integer,
 ref_id character varying(255) NOT NULL,
 ref_type character varying(255),
 ip_address inet NOT NULL,
 service_tag character varying(255) NOT NULL,
 model character varying(255),
 vendor character varying(255),
 display_name character varying(255),
 device_type character varying(255) NOT NULL,
 status character varying(255),
 status_message character varying(255),
 created_date timestamp without time zone,
 created_by character varying(255),
 updated_date timestamp without time zone,
 updated_by character varying(255),
 cred_id character varying(255),
 CONSTRAINT device_inventory_refid_pkey PRIMARY KEY (ref_id),
 CONSTRAINT device_inventory_service_tag_key UNIQUE (service_tag)
);
CREATE INDEX idx_device_inventory_ref_id on device_inventory(ref_id);

CREATE TABLE map_razornodename_to_serialnumber
(
  id character varying(100) PRIMARY KEY,
  serialnumber character varying(255) NOT NULL
);

CREATE TABLE discovery_result
(
  id character varying(100) PRIMARY KEY,
  parent_job_id character varying(255) NOT NULL,
  job_id character varying(255),
  ref_id character varying(255) NOT NULL,
  ref_type character varying(255),
  ip_address inet NOT NULL,
  service_tag character varying(255),
  model character varying(255),
  vendor character varying(255),
  device_type character varying(255) NOT NULL,
  server_type character varying(255),
  server_count integer,
  iom_count integer,
  status character varying(255),
  status_message character varying(255),
  health_state character varying(255),
  health_status_msg character varying(255),
  deviceRef_id character varying(255),
  created_date timestamp with time zone DEFAULT now(),
  created_by character varying(255),
  updated_date timestamp with time zone DEFAULT now(),
  updated_by character varying(255),
  CONSTRAINT discovery_result_refId UNIQUE (ref_id),
  CONSTRAINT discovery_result_jobid_key UNIQUE (parent_job_id, job_id)
);
CREATE INDEX idx_discovery_result_job_id on discovery_result(parent_job_id, job_id);


CREATE TABLE template
(
  template_id character varying(100) PRIMARY KEY,
  name character varying(255) NOT NULL,
  template_type character varying(255) NOT NULL,
  template_desc character varying(1024),
  wizard_page_number integer,
--  display_name character varying(255),
--  device_type character varying(255),
  created_date timestamp without time zone DEFAULT localtimestamp,
  created_by character varying(255),
  updated_date timestamp without time zone DEFAULT localtimestamp,
  updated_by character varying(255),
  state boolean,
  marshalledTemplateData character varying(10240),
  CONSTRAINT template_data_name_key UNIQUE (name)
);

CREATE TABLE service_template
(
  template_id character varying(100) PRIMARY KEY,
  name character varying(255) NOT NULL,
  template_desc character varying(1024),
  wizard_page_number integer,
  created_date timestamp without time zone DEFAULT localtimestamp,
  created_by character varying(255),
  updated_date timestamp without time zone DEFAULT localtimestamp,
  updated_by character varying(255),
  draft boolean,
  marshalledTemplateData character varying(1024000),
  CONSTRAINT service_template_data_name_key UNIQUE (name)
);


CREATE TABLE policy 
(
policy_ref_id character varying(100) PRIMARY KEY,
name character varying(255),
ref_id character varying(255) ,
ref_type character varying(255),
display_name character varying(255),
device_type character varying(255)
);

CREATE TABLE TemplatePolicyRef
(
  templateId character varying(100) NOT NULL,
  policyRefId character varying(100) NOT NULL ,
  CONSTRAINT TemplatePolicyRef_policyRefId_fkey FOREIGN KEY (policyRefId)
      REFERENCES policy (policy_ref_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT TemplatePolicyRef_templateId_fkey FOREIGN KEY (templateId)
      REFERENCES template (template_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);


CREATE TABLE DeviceConfigure
(
  id character varying(100) PRIMARY KEY,
  status character varying(255),
  marshalledDeviceConfigureData character varying(10240),
  created_date timestamp without time zone DEFAULT localtimestamp,
  created_by character varying(255),
  updated_date timestamp without time zone DEFAULT localtimestamp,
  updated_by character varying(255)
);

CREATE TABLE DeviceDiscover
(
  id character varying(100) PRIMARY KEY,
  status character varying(255),
  marshalledDeviceDiscoverData character varying(10240),
  created_date timestamp without time zone DEFAULT localtimestamp,
  created_by character varying(255),
  updated_date timestamp without time zone DEFAULT localtimestamp,
  updated_by character varying(255)
);