-- Table: device_inventory_compliance_map

-- DROP TABLE device_inventory_compliance_map;

CREATE TABLE device_inventory_compliance_map
(
  device_inventory_id character varying(255) NOT NULL,
  firmware_repository_id character varying(255) NOT NULL,
  compliance character varying(50) NOT NULL,
  created_date timestamp with time zone DEFAULT now(),
  created_by character varying(255),
  updated_date timestamp with time zone DEFAULT now(),
  updated_by character varying(255),
  CONSTRAINT device_inventory_compliance_map_pkey PRIMARY KEY (device_inventory_id, firmware_repository_id),
  CONSTRAINT compliance_map_to_device_inventory_fk FOREIGN KEY (device_inventory_id)
      REFERENCES device_inventory (ref_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT compliance_map_to_firmware_repo_fk FOREIGN KEY (firmware_repository_id)
      REFERENCES firmware_repository (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
