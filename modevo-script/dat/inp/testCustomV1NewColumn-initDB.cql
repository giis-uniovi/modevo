TRUNCATE "testCustomV1NewColumn".table1;
TRUNCATE "testCustomV1NewColumn".table2;
Insert into "testCustomV1NewColumn".table1 (idauthor, idbook, title) values ('1', '2', 'lotr');
Insert into "testCustomV1NewColumn".table1 (idauthor, idbook, title) values ('3', '4', 'asoiaf');
Insert into "testCustomV1NewColumn".table1 (idauthor, idbook, title) values ('5', '6', 'frieren');
Insert into "testCustomV1NewColumn".table2 (idbook) values ('2');
Insert into "testCustomV1NewColumn".table2 (idbook) values ('4');
Insert into "testCustomV1NewColumn".table2 (idbook) values ('6');