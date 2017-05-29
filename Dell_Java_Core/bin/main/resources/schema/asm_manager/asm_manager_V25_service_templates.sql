ALTER TABLE service_template ADD COLUMN template_version CHARACTER VARYING(100);
ALTER TABLE service_template ADD COLUMN template_valid BOOLEAN DEFAULT true;
