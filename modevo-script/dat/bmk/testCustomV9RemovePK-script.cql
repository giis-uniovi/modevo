FOR $1,$2 = SELECT idAuthor, idBook FROM table1BeforeChange
INSERT INTO table1(idAuthor, idBook) VALUES ($1, $2);
