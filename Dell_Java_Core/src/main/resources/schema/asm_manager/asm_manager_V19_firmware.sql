alter table service_template add column firmware_repository character varying(100);
alter table service_template add CONSTRAINT firmware_service_template_fk FOREIGN KEY (firmware_repository) REFERENCES firmware_repository (id) MATCH SIMPLE; 
alter table service_template add column manage_firmware boolean default false;

