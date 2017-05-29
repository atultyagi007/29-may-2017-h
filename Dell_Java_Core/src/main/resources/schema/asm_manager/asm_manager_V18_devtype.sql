
alter table device_inventory add column discover_device_type character varying(255);
alter table discovery_result add column discover_device_type character varying(100);
