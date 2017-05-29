ALTER TABLE device_inventory ADD COLUMN managed_state VARCHAR(255) NOT NULL DEFAULT 'MANAGED';

UPDATE device_inventory SET managed_state='UNMANAGED' where state='UNMANAGED';
UPDATE device_inventory SET managed_state='RESERVED' where state like '%RESERVED%';

UPDATE device_inventory SET state='READY' where state like '%RESERVED%';
UPDATE device_inventory SET state='READY' where state like 'UNMANAGED';
UPDATE device_inventory SET state='READY' where state like 'DISCOVERED';
