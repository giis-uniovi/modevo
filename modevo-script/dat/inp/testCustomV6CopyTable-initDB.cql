TRUNCATE "testCustomV6CopyTable".table1;
TRUNCATE "testCustomV6CopyTable".table1copied;
Insert into "testCustomV6CopyTable".table1 (idauthor, idbook, title) values ('1', '2', 'lotr');
Insert into "testCustomV6CopyTable".table1 (idauthor, idbook, title) values ('3', '4', 'asoiaf');
Insert into "testCustomV6CopyTable".table1 (idauthor, idbook, title) values ('5', '6', 'frieren');