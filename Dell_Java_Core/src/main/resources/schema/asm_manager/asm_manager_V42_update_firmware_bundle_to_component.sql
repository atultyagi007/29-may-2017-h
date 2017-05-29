ALTER TABLE software_component DROP CONSTRAINT software_component_software_bundle_fk;
ALTER TABLE software_component DROP COLUMN software_bundle;

CREATE TABLE software_bundle_component
(
  software_bundle_id character varying(100) NOT NULL,
  software_component_id character varying(100) NOT NULL,
  CONSTRAINT software_bundle_component_pk PRIMARY KEY (software_bundle_id, software_component_id),
  CONSTRAINT software_bundle_id_fkey FOREIGN KEY (software_bundle_id)
      REFERENCES software_bundle (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT software_component_id_fkey FOREIGN KEY (software_component_id)
      REFERENCES software_component (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);