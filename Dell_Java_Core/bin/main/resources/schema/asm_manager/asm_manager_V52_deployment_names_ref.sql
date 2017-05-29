
CREATE TABLE deployment_names_map
(
  id character varying(100) PRIMARY KEY,
  deployment_id character varying(255),
  type character varying(100),
  name character varying(255),
  CONSTRAINT deployment_names_type_name_constraint UNIQUE (type,name),
  CONSTRAINT deployment_names_map_fk FOREIGN KEY (deployment_id)
          REFERENCES deployment (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE CASCADE
);
