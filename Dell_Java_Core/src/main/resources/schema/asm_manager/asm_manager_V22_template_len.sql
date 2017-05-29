-- service templates were overflowing the size; note this looks like we attempated to
-- fix this in V4, but that changed the wrong table (template instead of service_template)
ALTER TABLE service_template ALTER COLUMN marshalledtemplatedata TYPE text;
