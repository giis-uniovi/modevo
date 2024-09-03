FOR $1 = SELECT idAuthor FROM table2
$2,$3 = SELECT idBook, title FROM table1 WHERE idAuthor=$1 ALLOW FILTERING
INSERT INTO table2new(idAuthor, idBook, title) VALUES ($1, $2, $3);
