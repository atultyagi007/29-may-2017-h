-- Table: add_on_module

-- DROP TABLE add_on_module;

CREATE TABLE add_on_module
(
  id character varying(255) NOT NULL,
  name character varying(255) NOT NULL,
  description character varying(1024),
  file_name character varying(255) NOT NULL,
  file_path character varying(255) NOT NULL,
  version character varying(127) NOT NULL,
  active boolean NOT NULL DEFAULT true,
  uploaded_by character varying(255),
  uploaded_date timestamp without time zone DEFAULT ('now'::text)::timestamp without time zone,
  CONSTRAINT addonmodule_id_pkey PRIMARY KEY (id),
  CONSTRAINT addonmodule_name_version_idx UNIQUE (name, version)
)
