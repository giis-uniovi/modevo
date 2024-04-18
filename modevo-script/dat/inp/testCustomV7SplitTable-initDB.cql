TRUNCATE "testCustomV7SplitTable".table1;
TRUNCATE "testCustomV7SplitTable".table1copied1;
TRUNCATE "testCustomV7SplitTable".table1copied2;
Insert into "testCustomV7SplitTable".table1 (idauthor, idbook, title) values ('1', '2', 'lotr');
Insert into "testCustomV7SplitTable".table1 (idauthor, idbook, title) values ('3', '4', 'asoiaf');
Insert into "testCustomV7SplitTable".table1 (idauthor, idbook, title) values ('5', '6', 'frieren');