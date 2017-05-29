-- asm_manager schema.  Read by AsmManagerDBDefs and passed to the DatabaseManager
-- library to create the database if it does not exist.

-- DatabaseManager will automatically add an orion_version table to this schema and
-- set the version to "0.0.0" (IDatabase.PROP_DB_BUILD_VERSION_DEFAULT)

-- Fix spelling
alter table firmware_repository add column is_embedded boolean default false;
update firmware_repository set is_embedded = is_embeded;
alter table firmware_repository drop column is_embeded;

alter table software_component add column hash_md5 char(32);
alter table software_component add column package_id varchar(32);
alter table software_component add column system_ids text [];

create table softwarecomponent_systemid (
id character varying(100) not null PRIMARY KEY, 
system_id character varying(100) not null, 
software_component character varying(100) not null,
name character varying(255),
created_date timestamp with time zone DEFAULT now(),
created_by character varying(255),
updated_date timestamp with time zone DEFAULT now(),
updated_by character varying(255)  
);
