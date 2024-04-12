TRUNCATE "testWireV92NewTableMigrationFromSeveralTables".user_team;
TRUNCATE "testWireV92NewTableMigrationFromSeveralTables".scim_external_ids;
TRUNCATE "testWireV92NewTableMigrationFromSeveralTables".scim_external;
Insert into "testWireV92NewTableMigrationFromSeveralTables".user_team (user, team) values ('1', 'team1');
Insert into "testWireV92NewTableMigrationFromSeveralTables".user_team (user, team) values ('2', 'team2');
Insert into "testWireV92NewTableMigrationFromSeveralTables".user_team (user, team) values ('3', 'team3');
Insert into "testWireV92NewTableMigrationFromSeveralTables".scim_external_ids (user, external) values ('1', '123');
Insert into "testWireV92NewTableMigrationFromSeveralTables".scim_external_ids (user, external) values ('2', '456');
Insert into "testWireV92NewTableMigrationFromSeveralTables".scim_external_ids (user, external) values ('3', '789');