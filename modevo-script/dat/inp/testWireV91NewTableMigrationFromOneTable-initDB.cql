TRUNCATE "testWireV91NewTableMigrationFromOneTable".user_team;
TRUNCATE "testWireV91NewTableMigrationFromOneTable".users_pending_activation;
Insert into "testWireV91NewTableMigrationFromOneTable".user_team (user, team) values ('1', 'team1');
Insert into "testWireV91NewTableMigrationFromOneTable".user_team (user, team) values ('2', 'team2');
Insert into "testWireV91NewTableMigrationFromOneTable".user_team (user, team) values ('3', 'team3');