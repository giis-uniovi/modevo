-- Export of keyspace testCustomV1NewColumn
CREATE KEYSPACE "testCustomV1NewColumn"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testCustomV1NewColumn".table2 (
	idbook text,
	title text,
	PRIMARY KEY (idbook)
);

CREATE TABLE "testCustomV1NewColumn".table1 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
) WITH CLUSTERING ORDER BY ( idbook ASC );

-- Export of keyspace testCustomV2TwoNewColumnSourceTwoTables
CREATE KEYSPACE "testCustomV2TwoNewColumnSourceTwoTables"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testCustomV2TwoNewColumnSourceTwoTables".table3 (
	idbook text,
	idauthor text,
	publisher text,
	PRIMARY KEY (idbook, idauthor)
);

CREATE TABLE "testCustomV2TwoNewColumnSourceTwoTables".table2 (
	idbook text,
	publisher text,
	title text,
	PRIMARY KEY (idbook)
);

CREATE TABLE "testCustomV2TwoNewColumnSourceTwoTables".table1 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
) WITH CLUSTERING ORDER BY ( idbook ASC );

-- Export of keyspace testCustomV3TwoNewColumnsUsingNMRelationship
CREATE KEYSPACE "testCustomV3TwoNewColumnsUsingNMRelationship"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};
CREATE TABLE "testCustomV3TwoNewColumnsUsingNMRelationship".table2new (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
);

CREATE TABLE "testCustomV3TwoNewColumnsUsingNMRelationship".table2 (
	idauthor text,
	PRIMARY KEY (idauthor)
);

CREATE TABLE "testCustomV3TwoNewColumnsUsingNMRelationship".table1 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
) WITH CLUSTERING ORDER BY ( idbook ASC );

-- Export of keyspace testCustomV4JoinTable
CREATE KEYSPACE "testCustomV4JoinTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};
CREATE TABLE "testCustomV4JoinTable".table2new (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
);

CREATE TABLE "testCustomV4JoinTable".table2 (
	idauthor text,
	PRIMARY KEY (idauthor)
) ;

CREATE TABLE "testCustomV4JoinTable".table1 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
);

-- Export of keyspace testCustomV5JoinTableKeyEntityInTargetTable
CREATE KEYSPACE "testCustomV5JoinTableKeyEntityInTargetTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testCustomV5JoinTableKeyEntityInTargetTable".table2 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
);

CREATE TABLE "testCustomV5JoinTableKeyEntityInTargetTable".table1 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
);

-- Export of keyspace testCustomV6CopyTable
CREATE KEYSPACE "testCustomV6CopyTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testCustomV6CopyTable".table1 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
) WITH CLUSTERING ORDER BY ( idbook ASC );

CREATE TABLE "testCustomV6CopyTable".table1copied (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
) WITH CLUSTERING ORDER BY ( idbook ASC );

-- Export of keyspace testCustomV7SplitTable
CREATE KEYSPACE "testCustomV7SplitTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testCustomV7SplitTable".table1 (
	idauthor text,
	idbook text,
	title text,
	PRIMARY KEY (idauthor, idbook)
) WITH CLUSTERING ORDER BY ( idbook ASC );

CREATE TABLE "testCustomV7SplitTable".table1copied1 (
	idbook text,
	title text,
	PRIMARY KEY (idbook)
) ;

CREATE TABLE "testCustomV7SplitTable".table1copied2 (
	idauthor text,
	PRIMARY KEY (idauthor)
);

-- Export of keyspace testMindsV10NewTableMigrationFromOneTable
CREATE KEYSPACE "testMindsV10NewTableMigrationFromOneTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testMindsV10NewTableMigrationFromOneTable".hidden_hashtags (
	hashtag text,
	admin_guid text,
	hidden_since text,
	PRIMARY KEY (hashtag)
);

CREATE TABLE "testMindsV10NewTableMigrationFromOneTable".user_hashtags (
	user_guid text,
	hashtag text,
	PRIMARY KEY (user_guid, hashtag)
) WITH CLUSTERING ORDER BY ( hashtag ASC );

CREATE TABLE "testMindsV10NewTableMigrationFromOneTable".user (
	key text,
	PRIMARY KEY (key)
);

-- Export of keyspace testMindsV25NewTableMigrationFromOneTable
CREATE KEYSPACE "testMindsV25NewTableMigrationFromOneTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testMindsV25NewTableMigrationFromOneTable".wire (
	receiver_guid text,
	amount text,
	entity_guid text,
	method text,
	recurring text,
	sender_guid text,
	status text,
	timestamp text,
	wei text,
	wei_amount text,
	wire_guid text,
	PRIMARY KEY (receiver_guid)
);

CREATE TABLE "testMindsV25NewTableMigrationFromOneTable".wire_support_tiers (
	entity_guid text,
	currency text,
	guid text,
	amount text,
	description text,
	name text,
	PRIMARY KEY (entity_guid, currency, guid)
);

CREATE TABLE "testMindsV25NewTableMigrationFromOneTable".user_hashtags (
	user_guid text,
	hashtag text,
	PRIMARY KEY (user_guid, hashtag)
) WITH CLUSTERING ORDER BY ( hashtag ASC );

CREATE TABLE "testMindsV25NewTableMigrationFromOneTable".user (
	key text,
	PRIMARY KEY (key)
);

-- Export of keyspace testMindsV27NewTableMigrationFromOneTable
CREATE KEYSPACE "testMindsV27NewTableMigrationFromOneTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testMindsV27NewTableMigrationFromOneTable".wire (
	receiver_guid text,
	amount text,
	entity_guid text,
	method text,
	recurring text,
	sender_guid text,
	status text,
	timestamp text,
	wei text,
	wei_amount text,
	wire_guid text,
	PRIMARY KEY (receiver_guid)
);

CREATE TABLE "testMindsV27NewTableMigrationFromOneTable".wire_support_tiers (
	address text,
	user_guid text,
	PRIMARY KEY (address)
);

CREATE TABLE "testMindsV27NewTableMigrationFromOneTable".withholdings (
	user_guid text,
	wallet_address text,
	PRIMARY KEY (user_guid, wallet_address)
);

CREATE TABLE "testMindsV27NewTableMigrationFromOneTable".user_hashtags (
	user_guid text,
	hashtag text,
	PRIMARY KEY (user_guid, hashtag)
) WITH CLUSTERING ORDER BY ( hashtag ASC );

CREATE TABLE "testMindsV27NewTableMigrationFromOneTable".user (
	key text,
	PRIMARY KEY (key)
);

-- Export of keyspace testMindsV9NewColumnsPreviousVersion
CREATE KEYSPACE "testMindsV9NewColumnsPreviousVersion"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testMindsV9NewColumnsPreviousVersion".notificationsprev (
	owner_guid text,
	guid text,
	data text,
	type text,
	PRIMARY KEY (owner_guid, guid, data, type)
) WITH CLUSTERING ORDER BY ( guid ASC, data ASC, type ASC );

CREATE TABLE "testMindsV9NewColumnsPreviousVersion".notifications (
	to_guid text,
	from_guid text,
	PRIMARY KEY (to_guid, from_guid)
) WITH CLUSTERING ORDER BY ( from_guid ASC );

-- Export of keyspace testMindsV9NewTableMigrationFromOneTable
CREATE KEYSPACE "testMindsV9NewTableMigrationFromOneTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testMindsV9NewTableMigrationFromOneTable".sendwyre_accounts (
	user_guid text,
	sendwyre_account_id text,
	PRIMARY KEY (user_guid)
);

CREATE TABLE "testMindsV9NewTableMigrationFromOneTable".user (
	key text,
	PRIMARY KEY (key)
);

-- Export of keyspace testThingsBoardV11NewColumnNonKey
CREATE KEYSPACE "testThingsBoardV11NewColumnNonKey"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testThingsBoardV11NewColumnNonKey".entity_view (
	id text,
	tenant_id text,
	customer_id text,
	dev_type text,
	name text,
	type text,
	PRIMARY KEY (id, tenant_id, customer_id)
) WITH CLUSTERING ORDER BY ( tenant_id ASC, customer_id ASC );

CREATE TABLE "testThingsBoardV11NewColumnNonKey".device (
	id text,
	tenant_id text,
	customer_id text,
	name text,
	type text,
	PRIMARY KEY (id, tenant_id, customer_id)
) WITH CLUSTERING ORDER BY ( tenant_id ASC, customer_id ASC );

-- Export of keyspace testThingsBoardV12NewColumnPK
CREATE KEYSPACE "testThingsBoardV12NewColumnPK"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testThingsBoardV12NewColumnPK".entity_view2_old (
	id text,
	tenant_id text,
	customer_id text,
	name text,
	type text,
	PRIMARY KEY (id, tenant_id, customer_id)
) WITH CLUSTERING ORDER BY ( tenant_id ASC, customer_id ASC );

CREATE TABLE "testThingsBoardV12NewColumnPK".entity_view2 (
	id text,
	tenant_id text,
	customer_id text,
	dev_type text,
	name text,
	type text,
	PRIMARY KEY (id, tenant_id, customer_id, dev_type)
) WITH CLUSTERING ORDER BY ( tenant_id ASC, customer_id ASC, dev_type ASC );

CREATE TABLE "testThingsBoardV12NewColumnPK".device (
	id text,
	tenant_id text,
	customer_id text,
	name text,
	type text,
	PRIMARY KEY (id, tenant_id, customer_id)
) WITH CLUSTERING ORDER BY ( tenant_id ASC, customer_id ASC );

-- Export of keyspace testThingsBoardV13NewColumnKeyInTable
CREATE KEYSPACE "testThingsBoardV13NewColumnKeyInTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testThingsBoardV13NewColumnKeyInTable".device (
	id text,
	tenant_id text,
	customer_id text,
	name text,
	type text,
	PRIMARY KEY (id, tenant_id, customer_id)
) WITH CLUSTERING ORDER BY ( tenant_id ASC, customer_id ASC );

CREATE TABLE "testThingsBoardV13NewColumnKeyInTable".entity_view_devpk (
	id text,
	tenant_id text,
	customer_id text,
	device_id text,
	dev_type text,
	name text,
	type text,
	PRIMARY KEY (id, tenant_id, customer_id, device_id)
) WITH CLUSTERING ORDER BY ( tenant_id ASC, customer_id ASC, device_id ASC );

-- Export of keyspace testWireV2NewTableMigrationFromOneTable
CREATE KEYSPACE "testWireV2NewTableMigrationFromOneTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testWireV2NewTableMigrationFromOneTable".billing_team_member (
	user text,
	team text,
	PRIMARY KEY (user, team)
) WITH CLUSTERING ORDER BY ( team ASC );

CREATE TABLE "testWireV2NewTableMigrationFromOneTable".user_team (
	user text,
	team text,
	PRIMARY KEY (user, team)
) WITH CLUSTERING ORDER BY ( team ASC );

-- Export of keyspace testWireV8NewTableMigrationFromPreviousVersion
CREATE KEYSPACE "testWireV8NewTableMigrationFromPreviousVersion"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testWireV8NewTableMigrationFromPreviousVersion".scim_user (
	id text,
	PRIMARY KEY (id)
);

CREATE TABLE "testWireV8NewTableMigrationFromPreviousVersion".scim_user_times (
	created_at text,
	last_updated_at text,
	uid text,
	PRIMARY KEY (created_at, last_updated_at)
) WITH CLUSTERING ORDER BY ( last_updated_at ASC );

-- Export of keyspace testWireV91NewTableMigrationFromOneTable
CREATE KEYSPACE "testWireV91NewTableMigrationFromOneTable"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testWireV91NewTableMigrationFromOneTable".users_pending_activation (
	user text,
	expires_at text,
	PRIMARY KEY (user)
);

CREATE TABLE "testWireV91NewTableMigrationFromOneTable".user_team (
	user text,
	team text,
	PRIMARY KEY (user, team)
) WITH CLUSTERING ORDER BY ( team ASC );

-- Export of keyspace testWireV92NewTableMigrationFromSeveralTables
CREATE KEYSPACE "testWireV92NewTableMigrationFromSeveralTables"
WITH durable_writes = true
AND replication = {
	'class' : 'SimpleStrategy',
	'replication_factor' : 1
};

CREATE TABLE "testWireV92NewTableMigrationFromSeveralTables".scim_external_ids (
	user text,
	external text,
	PRIMARY KEY (user, external)
) WITH CLUSTERING ORDER BY ( external ASC );

CREATE TABLE "testWireV92NewTableMigrationFromSeveralTables".scim_external (
	user text,
	external text,
	team text,
	PRIMARY KEY (user, external)
) WITH CLUSTERING ORDER BY ( external ASC );

CREATE TABLE "testWireV92NewTableMigrationFromSeveralTables".user_team (
	user text,
	team text,
	PRIMARY KEY (user, team)
) WITH CLUSTERING ORDER BY ( team ASC );

