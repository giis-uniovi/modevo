TRUNCATE "testMindsV10NewTableMigrationFromOneTable".user;
TRUNCATE "testMindsV10NewTableMigrationFromOneTable".user_hashtags;
TRUNCATE "testMindsV10NewTableMigrationFromOneTable".hidden_hashtags;
Insert into "testMindsV10NewTableMigrationFromOneTable".user (key) values ('1');
Insert into "testMindsV10NewTableMigrationFromOneTable".user (key) values ('2');
Insert into "testMindsV10NewTableMigrationFromOneTable".user (key) values ('3');
Insert into "testMindsV10NewTableMigrationFromOneTable".user_hashtags (user_guid, hashtag) values ('1', 'asoiaf');
Insert into "testMindsV10NewTableMigrationFromOneTable".user_hashtags (user_guid, hashtag) values ('2', 'asoiaf');
Insert into "testMindsV10NewTableMigrationFromOneTable".user_hashtags (user_guid, hashtag) values ('3', 'asoiaf');