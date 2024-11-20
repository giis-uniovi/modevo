TRUNCATE "testCustomV9RemovePK".table1beforechange;
TRUNCATE "testCustomV9RemovePK".table1;
Insert into "testCustomV9RemovePK".table1beforechange (idauthor, idbook, title) values ('1', '2', 'lotr');
Insert into "testCustomV9RemovePK".table1beforechange (idauthor, idbook, title) values ('3', '4', 'asoiaf');
Insert into "testCustomV9RemovePK".table1beforechange (idauthor, idbook, title) values ('5', '6', 'frieren');