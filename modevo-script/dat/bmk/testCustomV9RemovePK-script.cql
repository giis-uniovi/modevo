FOR $1,$2,$3 = SELECT idAuthor, idBook, title FROM table1BeforeChange
INSERT INTO table1(idAuthor, idBook, title) VALUES ($1, $2, $3);
