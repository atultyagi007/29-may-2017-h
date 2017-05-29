-- adding last deployed date attribute in the service_template table
ALTER TABLE service_template ADD COLUMN last_deployed_date timestamp without time zone;