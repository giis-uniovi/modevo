TRUNCATE "testMindsV9NewColumnsPreviousVersion".notificationsPrev;
TRUNCATE "testMindsV9NewColumnsPreviousVersion".notifications;
Insert into "testMindsV9NewColumnsPreviousVersion".notificationsPrev (owner_guid, guid, data, type) values ('1', '4', 'dat1', 'a');
Insert into "testMindsV9NewColumnsPreviousVersion".notificationsPrev (owner_guid, guid, data, type) values ('2', '5', 'dat1', 'a');
Insert into "testMindsV9NewColumnsPreviousVersion".notificationsPrev (owner_guid, guid, data, type) values ('3', '6', 'dat1', 'a');