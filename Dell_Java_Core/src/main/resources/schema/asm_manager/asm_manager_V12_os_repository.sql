-- asm_manager schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

drop table if exists os_repository cascade;
CREATE TABLE os_repository
(
 id character varying(100) NOT NULL PRIMARY KEY,
 name character varying(255) NOT NULL,
 source_path character varying(255) NOT NULL,
 state character varying(100),
 image_type character varying(100),
 created_date timestamp with time zone DEFAULT now(),
 created_by character varying(255),
 updated_date timestamp with time zone DEFAULT now(),
 updated_by character varying(255)
);
CREATE INDEX idx_os_repository_id on os_repository(id);

