TRUNCATE "testWireV2NewTableMigrationFromOneTable".user_team;
TRUNCATE "testWireV2NewTableMigrationFromOneTable".billing_team_member;
Insert into "testWireV2NewTableMigrationFromOneTable".user_team (user, team) values ('1', 'team1');
Insert into "testWireV2NewTableMigrationFromOneTable".user_team (user, team) values ('2', 'team2');
Insert into "testWireV2NewTableMigrationFromOneTable".user_team (user, team) values ('3', 'team3');