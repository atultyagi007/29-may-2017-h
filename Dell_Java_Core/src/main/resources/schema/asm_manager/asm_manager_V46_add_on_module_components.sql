ALTER TABLE add_on_module DROP COLUMN active;

CREATE TABLE add_on_module_components
(
  id character varying(255) NOT NULL PRIMARY KEY,
  name character varying(255) NOT NULL,
  type character varying(255) NOT NULL,
  add_on_module CHARACTER VARYING(255) NOT NULL,
  marshalled_data text,
  CONSTRAINT components_to_add_on_module_fk FOREIGN KEY (add_on_module)
      REFERENCES add_on_module (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE service_template_add_on_module
(
  service_template_id character varying(100) NOT NULL,
  add_on_module_id character varying(100) NOT NULL,
  CONSTRAINT service_template_add_on_module_pk PRIMARY KEY (service_template_id, add_on_module_id),
  CONSTRAINT service_template_id_fkey FOREIGN KEY (service_template_id)
      REFERENCES service_template (template_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT add_on_module_id_fkey FOREIGN KEY (add_on_module_id)
      REFERENCES add_on_module (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE deployment_add_on_module
(
  deployment_id character varying(100) NOT NULL,
  add_on_module_id character varying(100) NOT NULL,
  CONSTRAINT deployment_add_on_module_pk PRIMARY KEY (deployment_id, add_on_module_id),
  CONSTRAINT deployment_id_fkey FOREIGN KEY (deployment_id)
      REFERENCES deployment (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT add_on_module_id_fkey FOREIGN KEY (add_on_module_id)
      REFERENCES add_on_module (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);