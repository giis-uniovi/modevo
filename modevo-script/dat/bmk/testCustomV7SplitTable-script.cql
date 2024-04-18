FOR $1,$2 = SELECT idBook, title FROM table1
INSERT INTO table1Copied1(idBook, title) VALUES ($1, $2);
FOR $1 = SELECT idAuthor FROM table1
INSERT INTO table1Copied2(idAuthor) VALUES ($1);
