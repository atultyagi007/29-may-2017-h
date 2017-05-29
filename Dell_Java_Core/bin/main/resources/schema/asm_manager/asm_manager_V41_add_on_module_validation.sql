-- Table: operating_system_version

-- DROP TABLE operating_system_version;

CREATE TABLE operating_system_version
(
   id character varying(255) NOT NULL, 
   operating_system character varying(255) NOT NULL, 
   version character varying(127) NOT NULL, 
   CONSTRAINT operating_system_id_pkey PRIMARY KEY (id), 
   CONSTRAINT operating_system_version_idx UNIQUE (operating_system, version)
);

-- Table: add_on_module_operating_system_version_map

-- DROP TABLE add_on_module_operating_system_version_map

CREATE TABLE add_on_module_operating_system_version_map
(
   add_on_module_id character varying(255) NOT NULL, 
   operating_system_version_id character varying(255) NOT NULL, 
   CONSTRAINT aomosv_map_pkey PRIMARY KEY (add_on_module_id, operating_system_version_id), 
   CONSTRAINT aomosv_map_to_add_on_module_fk FOREIGN KEY (add_on_module_id) 
        REFERENCES add_on_module (id) 
        ON UPDATE NO ACTION ON DELETE CASCADE, 
   CONSTRAINT aomosv_map_to_operating_system_version_fk FOREIGN KEY (operating_system_version_id) 
        REFERENCES operating_system_version (id) 
        ON UPDATE NO ACTION ON DELETE CASCADE
);
