TRUNCATE "testThingsBoardV11NewColumnNonKey".device;
TRUNCATE "testThingsBoardV11NewColumnNonKey".entity_view;
INSERT INTO "testThingsBoardV11NewColumnNonKey".device (id, tenant_id, customer_id, name, type) VALUES ('1', 'ten1', 'cus1', 'name1', 'type1');
INSERT INTO "testThingsBoardV11NewColumnNonKey".device (id, tenant_id, customer_id, name, type) VALUES ('2', 'ten2', 'cus2', 'name2', 'type2');
INSERT INTO "testThingsBoardV11NewColumnNonKey".device (id, tenant_id, customer_id, name, type) VALUES ('3', 'ten3', 'cus3', 'name3', 'type3');
INSERT INTO "testThingsBoardV11NewColumnNonKey".device (id, tenant_id, customer_id, name, type) VALUES ('4', 'ten4', 'cus4', 'name4', 'type4');
INSERT INTO "testThingsBoardV11NewColumnNonKey".entity_view (id, tenant_id, customer_id, name, type) VALUES ('1', 'ten1', 'cus1', 'name', 'typeent');
INSERT INTO "testThingsBoardV11NewColumnNonKey".entity_view (id, tenant_id, customer_id, name, type) VALUES ('2', 'ten2', 'cus2', 'name', 'typeent');
INSERT INTO "testThingsBoardV11NewColumnNonKey".entity_view (id, tenant_id, customer_id, name, type) VALUES ('3', 'ten3', 'cus3', 'name', 'typeent');
INSERT INTO "testThingsBoardV11NewColumnNonKey".entity_view (id, tenant_id, customer_id, name, type) VALUES ('4', 'ten4', 'cus4', 'name', 'typeent');