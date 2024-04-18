TRUNCATE "testCustomV5JoinTableKeyEntityInTargetTable".table1;
TRUNCATE "testCustomV5JoinTableKeyEntityInTargetTable".table2;
Insert into "testCustomV5JoinTableKeyEntityInTargetTable".table1 (idauthor, idbook, title) values ('1', '2', 'lotr');
Insert into "testCustomV5JoinTableKeyEntityInTargetTable".table1 (idauthor, idbook, title) values ('3', '4', 'asoiaf');
Insert into "testCustomV5JoinTableKeyEntityInTargetTable".table1 (idauthor, idbook, title) values ('5', '6', 'frieren');
Insert into "testCustomV5JoinTableKeyEntityInTargetTable".table2 (idauthor, idbook) values ('1', '2');
Insert into "testCustomV5JoinTableKeyEntityInTargetTable".table2 (idauthor, idbook) values ('3', '4');
Insert into "testCustomV5JoinTableKeyEntityInTargetTable".table2 (idauthor, idbook) values ('5', '6');