-- store inventory facts for some puppet devices i.e. scvmm for caching
ALTER TABLE device_inventory ADD COLUMN facts TEXT;
ALTER TABLE discovery_result ADD COLUMN facts TEXT;
