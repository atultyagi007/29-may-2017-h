
ALTER TABLE service_template_add_on_module DROP CONSTRAINT add_on_module_id_fkey;
ALTER TABLE service_template_add_on_module DROP CONSTRAINT service_template_id_fkey;
ALTER TABLE service_template_add_on_module ADD CONSTRAINT service_template_id_fkey FOREIGN KEY (service_template_id)
      REFERENCES service_template (template_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE service_template_add_on_module ADD CONSTRAINT add_on_module_id_fkey FOREIGN KEY (add_on_module_id)
      REFERENCES add_on_module (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE deployment_add_on_module DROP CONSTRAINT add_on_module_id_fkey;
ALTER TABLE deployment_add_on_module DROP CONSTRAINT deployment_id_fkey;
ALTER TABLE deployment_add_on_module ADD CONSTRAINT deployment_id_fkey FOREIGN KEY (deployment_id)
      REFERENCES deployment (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE deployment_add_on_module ADD CONSTRAINT add_on_module_id_fkey FOREIGN KEY (add_on_module_id)
      REFERENCES add_on_module (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;