TRUNCATE "testCustomV4JoinTable".table1;
TRUNCATE "testCustomV4JoinTable".table2;
Insert into "testCustomV4JoinTable".table1 (idauthor, idbook, title) values ('1', '2', 'lotr');
Insert into "testCustomV4JoinTable".table1 (idauthor, idbook, title) values ('3', '4', 'asoiaf');
Insert into "testCustomV4JoinTable".table1 (idauthor, idbook, title) values ('5', '6', 'frieren');
Insert into "testCustomV4JoinTable".table2 (idauthor) values ('1');
Insert into "testCustomV4JoinTable".table2 (idauthor) values ('3');
Insert into "testCustomV4JoinTable".table2 (idauthor) values ('5');