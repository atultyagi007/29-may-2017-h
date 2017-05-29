CREATE TABLE template_user_map
(
  template_id character varying(100),
  user_id bigint,
  id character varying(100) PRIMARY KEY,
  CONSTRAINT template_users_fk FOREIGN KEY (template_id)
          REFERENCES service_template (template_id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE deployment_user_map
(
  deployment_id character varying(255),
  user_id bigint,
  id character varying(100) PRIMARY KEY,
  CONSTRAINT deployment_users_fk FOREIGN KEY (deployment_id)
          REFERENCES deployment (id) MATCH SIMPLE
          ON UPDATE NO ACTION ON DELETE CASCADE
);

ALTER TABLE service_template ADD COLUMN all_users boolean NOT NULL DEFAULT true;

ALTER TABLE deployment ADD COLUMN all_users boolean NOT NULL DEFAULT true;