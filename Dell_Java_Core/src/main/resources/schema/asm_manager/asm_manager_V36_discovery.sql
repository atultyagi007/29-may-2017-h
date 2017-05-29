ALTER TABLE discovery_result ADD COLUMN server_pool character varying(255);
ALTER TABLE discovery_result ADD COLUMN reserved boolean NOT NULL DEFAULT false;