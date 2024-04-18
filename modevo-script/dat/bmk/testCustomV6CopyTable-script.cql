FOR $1,$2,$3 = SELECT idAuthor, idBook, title FROM table1
INSERT INTO table1Copied(idAuthor, idBook, title) VALUES ($1, $2, $3);
