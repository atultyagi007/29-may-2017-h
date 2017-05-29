-- service templates were overflowing the size
ALTER TABLE deployment ALTER COLUMN marshalledtemplatedata TYPE text;

-- service templates were overflowing the size
ALTER TABLE template ALTER COLUMN marshalledtemplatedata TYPE text;
