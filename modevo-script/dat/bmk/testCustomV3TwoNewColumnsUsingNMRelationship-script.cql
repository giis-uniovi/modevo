FOR $1 = SELECT idAuthor FROM table2
$2,$3 = SELECT title, idBook FROM table1 WHERE idAuthor=$1 ALLOW FILTERING
INSERT INTO table2(idAuthor, title, idBook) VALUES ($1, $2, $3);
