-- asm_manager initial schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

CREATE TABLE groups
(
  seq_id bigint NOT NULL,
  name character varying(255) NOT NULL,
  description character varying(255),
  created_by character varying(255) NOT NULL,
  created_date timestamp with time zone DEFAULT now(),
  updated_by character varying(255) NOT NULL,
  updated_date timestamp with time zone DEFAULT now(),
  CONSTRAINT group_pkey PRIMARY KEY (seq_id),
  CONSTRAINT group_name_unique_key UNIQUE (name)
);

CREATE SEQUENCE seq_groups START WITH 1 CACHE 10;

CREATE TABLE groups_device_inventory
(
  devices_inventory_seq_id character varying(255) NOT NULL,
  groups_seq_id bigint NOT NULL,
  CONSTRAINT group_device_inventory_pkey PRIMARY KEY (groups_seq_id, devices_inventory_seq_id),
  CONSTRAINT device_inventory_fkey FOREIGN KEY (devices_inventory_seq_id)
      REFERENCES device_inventory (ref_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT groups_fkey FOREIGN KEY (groups_seq_id)
      REFERENCES groups (seq_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE groups_users
(
  groups_seq_id bigint NOT NULL,
  user_seq_id bigint NOT NULL,
  CONSTRAINT groups_fkey FOREIGN KEY (groups_seq_id)
      REFERENCES groups (seq_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
 
