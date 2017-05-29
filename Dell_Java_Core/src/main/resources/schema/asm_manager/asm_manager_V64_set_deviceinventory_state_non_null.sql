UPDATE device_inventory SET state='READY' WHERE state IS NULL;

ALTER TABLE device_inventory ALTER COLUMN state SET NOT NULL;
