-- asm_manager schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

CREATE TABLE firmware_repository
( 
 id character varying(100) NOT NULL PRIMARY KEY,
 name character varying(255) NOT NULL,
 source_location character varying(255),
 source_type character varying(5),
 disk_location character varying(255) NOT NULL,
 is_default boolean default false,
 is_embeded boolean default false,
 bundle_count int, 
 component_count int,
 filename character varying(255) NOT NULL, 
 md5_hash character varying(255),
 username character varying(255),
 password character varying(255),
 download_status character varying(100), 
 created_date timestamp with time zone DEFAULT now(),
 created_by character varying(255),
 updated_date timestamp with time zone DEFAULT now(),
 updated_by character varying(255)  
);
CREATE INDEX idx_firmware_repository_id on firmware_repository(id);

