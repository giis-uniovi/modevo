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