ALTER TABLE deployment ADD COLUMN deployment_started_date timestamp without time zone;
ALTER TABLE deployment ADD COLUMN deployment_finished_date timestamp without time zone;

UPDATE deployment SET deployment_started_date = created_date WHERE deployment_started_date is NULL;
